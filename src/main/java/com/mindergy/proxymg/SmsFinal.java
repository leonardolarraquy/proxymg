package com.mindergy.proxymg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.mindergy.util.database.DBConnections;
import com.mindergy.util.log.Log;

/**
 * This class inserts a proxyMT sms message in the database.
 * @author TIMwe
 *
 */
public class SmsFinal {

	
	
	/** This method is responsible for inserting a proxyMT 
	 * sms message in the database.
	 * @param sms -> Sms message to insert in the database 
	 */
	public static void insert(ProxyMt sms)
	{
		Connection db=null;
		PreparedStatement s=null;
		try {
			db=DBConnections.GetConnection("proxymg");
			s=db.prepareStatement(SQLData.SmsFinal.INS_1);
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
			s.setInt(21, sms.getSendStatus());
			s.setInt(22, sms.getNotifStatus());
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
