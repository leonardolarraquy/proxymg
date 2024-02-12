/*
 * Created on Mar 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.mindergy.proxymg.queues;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.buffer.PriorityBuffer;
import org.apache.commons.collections.buffer.SynchronizedBuffer;

import com.mindergy.proxymg.ProxyMain;
import com.mindergy.proxymg.ProxyMt;
import com.mindergy.proxymg.ProxyXconn;
import com.mindergy.proxymg.SQLData;
import com.mindergy.util.database.DBConnections;
import com.mindergy.util.log.Log;


/**
 * This class monitors the notifq table and the cahed data.
 * @author TIMwe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NotifQFullCacheMonitor extends Thread
{
    /**
     * The father 
     */
    private ProxyMain main;

    /** The time to wait between sql queries (in ms)*/
    public int  waitTime;

    /** The maximum number of threads to launch.    */
    public volatile int maxthreads;

    /**
     * The query to perform
     */
    protected String sql;

    /**
     * The number of lines to retrieve in each iteration
     */
    public int queryLines;

    /**
     * Maximum size of cache
     */
    public int maxCacheSize=0;

    /**
     * The total number of threads launched so far.
     */
    protected volatile int totalThreads = 0;

    /** The number of running threads in each instant.    */
    protected volatile int runningThreads = 0;

    /** Controls where the engine is attempting to quit in a clean way, i.e.,
     * waiting for all the current running threads to finnish. */
    public boolean ending = false;

    /** Controls wether the engine is in suspended mode or not,
      * therefore stopping the thread launch process. */
    protected volatile boolean suspended = false;

    /**
     * Thread sleep cycle dividor
     */
    private int tsdiv;

    /**
     * Number of messages batch processors will handle
     */
    protected int batchSize = 0;
    
  
    /**
     * Running ids
     */
    public volatile Vector lockIds=new Vector();
    
    /**
     * Buffer Running ids
     */   
    public volatile  Buffer runningIds =
        	SynchronizedBuffer.decorate(new PriorityBuffer(new SenSMSPriorityComparator()));
    
    
    /**
     * Record definition data: Xconn Key
     */
    public int xConnKey;
    /**
     * Record definition data: Processor ID
     */
    public int procId;
    
  /**
   * This methd creates the relationship between this class
   * and the fatger class.
 * @param main -> Father class object
 */
public NotifQFullCacheMonitor(ProxyMain main)
  {
    this.main=main;
  }


      /**
     * This method incrememnts the running threads
     * @author Paulo Salgado
     * @version 1.0.0
     */
    public void incrementRunningThreads()
    {
        this.runningThreads++;
    }

    /**
     * This method decrements the running threads
     * @author Paulo Salgado
     * @version 1.0.0
     */
    public void decrementRunningThreads()
    {
        this.runningThreads--;
    }

    /**
     * Here is the thread starting point
     * @author Paulo Salgado
     * @version 1.0.2
     */
    public void run()
    {
      long alivec=0;
      Thread.currentThread().setName("__MG_NOTIF");
      // set thread cycle dividor
        if(waitTime > 10000)
          tsdiv=10000;
        else if(waitTime > 1000)
          tsdiv=1000;
        else if(waitTime > 100)
          tsdiv=100;
        else
          tsdiv=1;
        
      maxCacheSize=Math.round( maxthreads*5*waitTime/500);
      
      try
      {
          

          // lauch cache threads
          this.launchProcessorThreads();
          
          
          Log.info("NOTIFQ CACHED MON for xconn " + this.xConnKey + " started");
          while (!this.ending && !this.main.shutdownInProgress)
          {
            try
            {
          Date start = new Date();    
              if(alivec%1000==0)
                Log.info("NOTIFQMONITOR "+this.xConnKey + " I AM ALIVE!!!");
              else if(alivec%15==0)
            	  Log.info("FullCache Monitor for xconn: "+xConnKey + " has "+runningIds.size() + " to process");
              if(alivec==10000000)
                alivec=0;
              alivec++;
              
              int bef=this.runningIds.size();
              // get queue entries
              if(bef<maxCacheSize)
            	  this.getQueueEntries();
              Date end=new Date();
              if(alivec%100==0)
                  Log.info("NOTIFQMON CACHED " + this.xConnKey + " getQ:" + (end.getTime()-start.getTime())+ " bef: " + bef + " ids:" + this.runningIds.size());
                for (int i = 0;!ending &&  i < (waitTime/tsdiv); i++) 
                  Thread.sleep(tsdiv);
            }
            catch(Exception e)
            {
              Log.error(e);
            }
          }
          Log.info("NOTIFQ CACHED MON for xconn " + this.xConnKey + " stopped");
      }
      catch(Exception e)
      {
        Log.error(e);
      }
    }

    /**
       * This method performs the query to the database, 
       * to get the requests to process and
       * updates each row.
       * @author Paulo Salgado
       * @version 1.0.0
       */

    private void getQueueEntries()
    {
      Connection db=null;
      PreparedStatement s=null;
      PreparedStatement su=null;
      ResultSet rs = null;
      try
      {
          int targetLines=maxCacheSize-this.runningIds.size();
          if(targetLines>0)
          {
		      db = DBConnections.GetConnection("proxymg");
		      db.setAutoCommit(false);
		      //s = db.prepareStatement(SQLData.SenQ.SELN_QUEUEMON_FULL_CACHE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
		      s = db.prepareStatement(ProxyXconn.getXconnReplacedSql(SQLData.Notifq.SELN_MON,this.xConnKey));
		      su = db.prepareStatement(ProxyXconn.getXconnReplacedSql(SQLData.Notifq.UPD_MON_STATUS,this.xConnKey));
		      s.setInt(1,this.xConnKey);
		      s.setInt(2,0);
		      s.setInt(3,targetLines);
		      
	        rs = s.executeQuery();
	        int count = 0;
	        Long id;
	
	        ArrayList temp = new ArrayList();
	        while (rs.next() && count<targetLines)
	        {
	        	 /*select senq_id           ,origin , dest              ,message           ,"+
	 			" aditional_message , xconn_key  ,service_id        , charge            , ext_id            ,"+
	 			" retries           , priority          ,qtime             ,stime             ,status            ,"+
	 			"op_id             , ctype             , ref1              ,ref2              ,ref3 "+*/
	        	ProxyMt sms = new ProxyMt ();
	            sms.setSenqId(rs.getLong(1));
	            id= new Long(sms.getSenqId());
	            sms.setOrigin(rs.getString(2));
	            sms.setDest(rs.getString(3));
	            sms.setMessage(rs.getString(4));
	            sms.setAditionalMessage(rs.getString(5));
	            sms.setXconnKey(rs.getInt(6));
	            sms.setServiceId(rs.getInt(7));
	            sms.setCharge(rs.getInt(8)==1);
	            sms.setExtId(rs.getString(9));
	            sms.setRetries(rs.getInt(10));
	            sms.setPriority(rs.getInt(11));
	            sms.setQtime(rs.getTimestamp(12));
	            sms.setStime(rs.getTimestamp(13));
	            
	            sms.setStatus(rs.getInt(14));
	            sms.setOpId(rs.getInt(15));
	            sms.setCtype(rs.getInt(16));
	            sms.setRef1(rs.getLong(17));
	            sms.setRef2(rs.getLong(18));
	            sms.setRef3(rs.getLong(19));
	            sms.setChargeStatus(rs.getInt(20));
	            sms.setSendStatus(rs.getInt(21));
	            if(!lockIds.contains(id))
	            {
	            	temp.add(sms);
	                count++;
	                // put status in 90
	                su.setInt(1,90);
	                su.setLong(2,sms.getSenqId());
	                // su.setInt(3,0);
	                su.addBatch();
	            }
	            if(count%50==0)
	            {
	            	int[] res=su.executeBatch();
	            	db.commit();
	            	for (int i = 0; i < res.length; i++) {
	            		if(res[i]>0)
	            		{
		       	        	sms=(ProxyMt) temp.get(i);
		       	        	id = new Long(sms.getSenqId());
		       	        	runningIds.add(sms);
		                    lockIds.add(id);
	            		}
	    			}
	            	Log.debug("FullCache Monitor for xconn: "+xConnKey + " checkpoint commit");
	            	temp = new ArrayList();
	            }
	            
	        }
	        
	        rs.close();
	        rs=null;
	        s.close();
	        int res[]=su.executeBatch();
	        su.close();
	        db.commit();
   	        db.setAutoCommit(true);
   	        db.close();
   	        // pass to running ids only after commit
   	        for (int i = 0; i < res.length; i++) {
   	        	if(res[i]>0)
   	        	{
   	        		ProxyMt sms=(ProxyMt) temp.get(i);
   	        		id = new Long(sms.getSenqId());
   	        		runningIds.add(sms);
   	        		lockIds.add(id);
   	        	}
			}
   	        if(count!=0)
	        {
	        	Log.info("FullChargeCache Monitor for xconn: "+xConnKey + " has "+runningIds.size() + " to process");
	        }
	        s=null;
          }
      }
      catch(Exception e)
      {
        // recover db connection if fails
        Log.error(e);
        try { s.close(); } catch (Exception ee) { ; }
        try { db.close(); } catch (Exception ee) { ; }
        try { rs.close(); } catch (Exception ee) { ; }
        s=null;db=null;rs=null;
        
      }
      finally
      {
        // Always make sure result sets and statements are closed,
        // and the connection is returned to the pool
//        Always make sure result sets and statements are closed,
          // and the connection is returned to the pool
          if (rs != null) {
              try { rs.close(); } catch (SQLException e) { ; }
              rs = null;
            }
          if (s != null) {
            try { s.close(); } catch (SQLException e) { ; }
            s = null;
          }
          if (db != null) {
            try { db.close(); } catch (SQLException e) { ; }
            db = null;
          }
        
      }
    }

    /**
      * This method launches the threads that will process 
      * each request
      * @author Paulo Salgado
      * @version 1.0.0
      */
    private void launchProcessorThreads() 
    {
        try
        {
            // Launch the threads
            for (int k = 0; k < maxthreads; k++)
            {
                Log.debug("NOTIFQ Cache Monitor prepare to launch thread ");
                if (ending)
                {
                    break; // The user wants to quit, so dont bother
                }
               
                // get the class
                NotifQFullCacheProcessor processor = new NotifQFullCacheProcessor(this.main, this);
                // lauch
                processor.start();
                this.incrementRunningThreads();
            }
        }
        catch (Exception e)
        {
            Log.error(e);
        }
    }
    
    
    
    
    /**TODO Help
     * @return
     */
    public synchronized ProxyMt giveMeNext()
    {
    	ProxyMt result=null;
        synchronized (this.lockIds)
        {
        synchronized (this.runningIds)
        {
	        try
	        {
	          int size=this.runningIds.size();
	          if(size>0)
	          {
	            result= (ProxyMt) this.runningIds.remove();
	            
	            lockIds.remove(new Long(result.getSenqId()));
	          }
	        }
	        catch(Exception e)
	        {
	          Log.error(e);
	        }
        }
        }
        return result;
    }
    
    
}
