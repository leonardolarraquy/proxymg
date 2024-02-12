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
 * This class is responsible for updating, inserting and deleting
 * messages in the chargeq table.
 * @author TImwe
 *
 */
public class Chargeq {

	/**
	 * This method deletes messages from the chargeq table.
	 * @param senqId -> Id of the message to delete
	 * @param xconn -> Xconn to use
	 */
	public static void delete(long senqId, int xconn)
	{
		Connection db=null;
		PreparedStatement s=null;
		try {
			db=DBConnections.GetConnection("proxymg");
			s=db.prepareStatement(ProxyXconn.getXconnReplacedSql(SQLData.Chargeq.DEL_1,xconn));
			s.setLong(1,senqId);
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
	
	
	/**
	 * This method is responsible for inserting messages in the 
	 * chargeq table.
	 * @param sms -> Message to insert
	 */
	public static boolean insert(ProxyMt sms) {
		boolean ok = true;
		
		Connection db=null;
		PreparedStatement s=null;
		try {
			db=DBConnections.GetConnection("proxymg");
			s=db.prepareStatement(ProxyXconn.getXconnReplacedSql(SQLData.Chargeq.INS_1,sms.getXconnKey()));
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
	 * This method is responsible for updating the number
	 * of retries of messages in the chargeq table.
	 * @param senqId -> ID os the message
	 * @param xconn -> Xconn to use
	 * @param retry -> Number of retries
	 * @param stime -> Start Time
	 */
	public static void retry(long senqId, int xconn, int retry, Date stime, int chargeStatus)
	{
		Connection db=null;
		PreparedStatement s=null;
		try {
			db=DBConnections.GetConnection("proxymg");
			s=db.prepareStatement(ProxyXconn.getXconnReplacedSql(SQLData.Chargeq.UPD_RETRY,xconn));
			s.setInt(1,0);
			s.setInt(2,retry);
			s.setTimestamp(3, new Timestamp(stime.getTime()));
			s.setInt(4,chargeStatus);
			s.setLong(5,senqId);
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
