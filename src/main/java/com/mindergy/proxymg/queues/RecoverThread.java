package com.mindergy.proxymg.queues;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.mindergy.proxymg.ProxyXconn;
import com.mindergy.proxymg.SQLData;
import com.mindergy.util.database.DBConnections;
import com.mindergy.util.log.Log;

public class RecoverThread extends Thread {

	private ProxyXconn xc;

	public RecoverThread(ProxyXconn aXconn){
		xc = aXconn;
	}

	public void run() {
		Thread.currentThread().setName("__RECOVER");
		
		Connection db=null;
		PreparedStatement s=null;
		PreparedStatement s1=null;
		try{
			db=DBConnections.GetConnection("proxymg");

			if(xc.getSendThreads() > 0){
				s=db.prepareStatement(ProxyXconn.getXconnReplacedSql(SQLData.Senq.RECOVER_MTS,xc.getKey()));
				int cantRows = s.executeUpdate();
				
				Log.info("Rows updated for send" + xc.getKey() + ": " + cantRows);
			}

			if(xc.getChargeThreads() > 0){
				s1=db.prepareStatement(ProxyXconn.getXconnReplacedSql(SQLData.Chargeq.RECOVER_MTS,xc.getKey()));
				int cantRows = s1.executeUpdate();
				
				Log.info("Rows updated for charge" + xc.getKey() + ": " + cantRows);
			}
		}
		catch(Exception e){
			Log.error(e);
		}
		finally
		{
			// Always make sure result sets and statements are closed,
			// and the connection is returned to the pool
			//				Always make sure result sets and statements are closed,
			// and the connection is returned to the pool
			if (s != null) {
				try { s.close(); } catch (SQLException e) { ; }
				s = null;
			}
			if (s1 != null) {
				try { s1.close(); } catch (SQLException e) { ; }
				s1 = null;
			}
			if (db != null) {
				try { db.close(); } catch (SQLException e) { ; }
				db = null;
			}
		}
	}
}
