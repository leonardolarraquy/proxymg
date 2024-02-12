package com.mindergy.proxymg.queues;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.mindergy.proxymg.ProxyMt;
import com.mindergy.proxymg.ProxyXconn;
import com.mindergy.proxymg.SQLData;
import com.mindergy.util.database.DBConnections;
import com.mindergy.util.log.Log;

/**
 * This class s responsible for deleting,inserting and updating
 * the number of retries in the senq table.
 * @author TIMwe
 */
public class Senq {

	/**
	 * This method deletes a message from the senq table
	 * @param senqId -> message to delete
	 * @param xconn -> proxy xconn to use
	 */
	public static void delete(long senqId, int xconn)
	{
		Connection db=null;
		PreparedStatement s=null;
		try {
			db=DBConnections.GetConnection("proxymg");
			s=db.prepareStatement(ProxyXconn.getXconnReplacedSql(SQLData.Senq.DEL_1,xconn));
			s.setLong(1,senqId);
			int cantRows = s.executeUpdate();
			s.close();
			s=null;
			db.close();
			db=null;
						
		} catch (Exception e) {
			Log.error(e);
		}
	    finally
	    {
	      // Always make sure result sets and statements are closed,
	      // and the connection is returned to the pool
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
	 * This method inserts a message in the senq database
	 * @param sms -> Message to insert
	 */
	public static boolean insert(ProxyMt sms) {
		boolean ok = true;
		
		Connection db=null;
		PreparedStatement s=null;
		try {
			db=DBConnections.GetConnection("proxymg");
			s=db.prepareStatement(ProxyXconn.getXconnReplacedSql(SQLData.Senq.INS_1,sms.getXconnKey()));
			/*senq_id           ,origin , dest              ,message           ,"+
			" aditional_message , xconn_key  ,service_id        , charge            , ext_id            ,"+
			" retries           , priority          ,qtime             ,stime             ,status            ,"+
			"op_id             , ctype             , ref1              ,ref2              ,ref3 */
			s.setLong(1,sms.getSenqId());
			s.setString(2, sms.getOrigin());
			s.setString(3, sms.getDest());
			s.setString(4, sms.getMessage());
			s.setString(5, sms.getAditionalMessage());
			s.setInt(6, sms.getXconnKey());
			s.setInt(7, sms.getServiceId());
			if(sms.isCharge())
				s.setInt(8, 1);
			else
				s.setInt(8, 0);
			s.setString(9, sms.getExtId());
			s.setInt(10, sms.getRetries());
			s.setInt(11, sms.getPriority());
			s.setTimestamp(12, new Timestamp(sms.getQtime().getTime()));
			s.setTimestamp(13, new Timestamp(sms.getStime().getTime()));
			s.setInt(14, sms.getStatus());
			s.setInt(15, sms.getOpId());
			s.setInt(16, sms.getCtype());
			s.setLong(17, sms.getRef1());
			s.setLong(18, sms.getRef2());
			s.setLong(19, sms.getRef3());
			s.setInt(20, sms.getChargeStatus());
			s.setInt(21, sms.getPurposeId());
			s.setString(22, sms.getSender());

			s.execute();
			s.close();
			s=null;
			db.close();
			db=null;
			
		} 
		catch (Exception e) {
			Log.error(e);
			
			if(e.getClass().getName().equalsIgnoreCase("com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException"))
				 Log.info("Primary key exception for id: " + sms.getSenqId() + " found but will ignore it.");
			else ok = false;
		}
	    finally
	    {
	      // Always make sure result sets and statements are closed,
	      // and the connection is returned to the pool
	//      Always make sure result sets and statements are closed,
	        // and the connection is returned to the pool
	        if (s != null) {
	          try { s.close(); } catch (SQLException e) { ; }
	          s = null;
	        }
	        if (db != null) {
	          try { db.close(); } catch (SQLException e) { ; }
	          db = null;
	        }
	      
	    }
	    
	    return ok;
	}
	

	/** 
	 * Thos method updates the number of retries for the
	 * message in the senq table.
	 * @param senqId -> message id
	 * @param xconn -> Proxy Xconn to use
	 * @param retry -> Number of retries
	 * @param stime -> start time
	 */
	public static void retry(long senqId, int xconn, int retry, Date stime)
	{
		Connection db=null;
		PreparedStatement s=null;
		try {
			db=DBConnections.GetConnection("proxymg");
			s=db.prepareStatement(ProxyXconn.getXconnReplacedSql(SQLData.Senq.UPD_RETRY,xconn));
			s.setInt(1,0);
			s.setInt(2,retry);
			s.setTimestamp(3, new Timestamp(stime.getTime()));
			s.setLong(4,senqId);
			s.execute();
			s.close();
			s=null;
			db.close();
			db=null;
			
		} catch (Exception e) {
			Log.error(e);
		}
	    finally
	    {
	      // Always make sure result sets and statements are closed,
	      // and the connection is returned to the pool
	//      Always make sure result sets and statements are closed,
	        // and the connection is returned to the pool
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
}
