package com.mindergy.proxymg;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.collections.FastArrayList;
import org.apache.commons.collections.FastHashMap;

import com.mindergy.mg.interfaces.IFactory;
import com.mindergy.mg.interfaces.ISMSReceiver;
import com.mindergy.mg.interfaces.IXConn;
import com.mindergy.mg.interfaces.IXConnController;
import com.mindergy.proxymg.queues.ChargeQFullCacheMonitor;
import com.mindergy.proxymg.queues.NotifQFullCacheMonitor;
import com.mindergy.proxymg.queues.RecoverThread;
import com.mindergy.proxymg.queues.SenQFullCacheMonitor;
import com.mindergy.util.database.DBConnections;
import com.mindergy.util.log.Log;

/* 
 * $Source$
 * $Revision: 100829 $
 * $Author: leonardo.larraquy $
 * $Date: 2010-01-29 10:35:00 -0300 (Fri, 29 Jan 2010) $
 * $Name$
 * $State$
 * $Log$
 * Revision 1.1  2007/04/13 10:27:31  pps
 * *** empty log message ***
 *
 * Revision 1.4  2006/12/20 08:35:04  pps
 * *** empty log message ***
 *
 * Revision 1.3  2006/08/20 20:15:12  pps
 * *** empty log message ***
 *
 * Revision 1.2  2006/08/19 14:10:59  pps
 * *** empty log message ***
 *
 * Revision 1.1  2006/08/18 20:00:03  pps
 * *** empty log message ***
 *
 * Revision 1.11  2006/06/13 16:46:34  pps
 * *** empty log message ***
 *
 * Revision 1.10  2006/06/03 13:51:01  ics
 * Batch Processor Implementation
 *
 * Revision 1.9  2006/06/01 18:49:28  pps
 * fix bugs in async notif
 *
 * Revision 1.8  2006/05/21 19:23:21  pps
 * complete notification system
 *
 * Revision 1.7  2006/05/19 17:18:08  pps
 * add control of repeated gw ids on SMSReceiver. Block them, if repeated
 *
 * Revision 1.6  2006/04/20 10:04:14  pps
 * *** empty log message ***
 *
 * Revision 1.5  2004/11/29 15:50:13  pps
 * OperatorId support in all rec and send classes
 *
 * Revision 1.4  2004/11/14 17:06:55  pps
 * fix error nullpointerex in wrapper.log in smsreceiver
 *
 * Revision 1.3  2004/07/07 00:34:00  pps
 * implement can_sync auto control for xconn
 *
 * Revision 1.2  2004/04/27 12:40:45  pps
 * add cvs keyrwords
 * set req_qdate to systimestamp
 *
 *
 */

/**
 * This class (thread) loads all active connections from database and puts
 * them on the connections hashmap of mg
 * @author TIMwe
 *
 */
public class XConnLoader extends Thread
{
	/**
	 * Variable for the main class of the project.
	 */
	private ProxyMain main;

	/**
	 * Proxy xconn
	 */
	private ProxyXconn xc;

	/**
	 * This method creates the relationship between this thread with the
	 * main object of the application.
	 * @param main -> Main object of the application
	 */
	public XConnLoader(ProxyMain main)
	{
		this.main = main;
	}

	/** Constructor
	 * @param main -> Main object of the application
	 * @param xc -> Xconn
	 */
	public XConnLoader(ProxyMain main, ProxyXconn xc)
	{
		this.main = main;
		this.xc=xc;
	}

	/**
	 * This method loads all connections
	 */
	public void load(boolean cacheOnly)
	{

		FastHashMap conns = new FastHashMap();
		FastHashMap connsProxyOf = new FastHashMap();
		FastArrayList clist = new FastArrayList();
		Connection db=null;
		PreparedStatement s=null;
		ResultSet rs=null;
		try
		{
			Log.debug("XConn Loader init");

			db=DBConnections.GetConnection("proxymg");
			s=db.prepareStatement(SQLData.Xconn.SELN_ALL);
			rs=s.executeQuery();
			ProxyXconn px=null;
			while(rs.next()) {
				px=new ProxyXconn();
				
				/* select "+
			" xconn_key ,"+          
			" xconn_charge_threads,"+    
			" xconn_send_threads      ,"+
			" xconn_notif_threads,"+     
			" xconn_send_class,"+        
			" xconn_charge_start_hour,"+ 
			" xconn_charge_end_hour,"+   
			" xconn_send_start_hour,"+   
			" xconn_send_end_hour,"+     
			" xconn_proxyof,"+           
			" xconn_notif_failed,"+      
			" xconn_charge_retries,"+    
			" xconn_send_retries,"+      
			" xconn_notif_retries,"+     
			" xconn_async_ack,"+
			" xconn_name, " +
			" xconn_props," +
			" xconn_retry_minutes, "+  
			" xconn_op_id, "
			" xconn_send_queue "
				 */
				
				px.setKey(rs.getInt(1));
				px.setChargeThreads(rs.getInt(2));
				px.setSendThreads(rs.getInt(3));
				px.setNotifThreads(rs.getInt(4));
				px.setXconnCl(rs.getString(5));
				px.setChargeStartHour(rs.getInt(6));
				px.setChargeEndHour(rs.getInt(7));
				px.setSendStartHour(rs.getInt(8));
				px.setSendEndHour(rs.getInt(9));
				px.setProxyOf(rs.getInt(10));
				px.setNotifyFailed(rs.getInt(11)==1);
				px.setChargeRetries(rs.getInt(12));
				px.setSendRetries(rs.getInt(13));
				px.setNotifRetries(rs.getInt(14));
				px.setAsyncAck(rs.getInt(15));
				px.setName(rs.getString(16));
				px.setPropsFile(rs.getString(17));
				px.setRetryMinutes(rs.getInt(18));
				px.setOpId(rs.getInt(19));
				px.setUseSenqQ(rs.getInt(20));
				
				// put on hash map
				conns.put(String.valueOf(px.getKey()),px);
				clist.add(px);
				Log.debug("XConn Loader Added XConn " + px.getKey());
				connsProxyOf.put(String.valueOf(px.getProxyOf()),px);
			}

			// now replace the hasmap
			synchronized(main.xConns)
			{
				main.xConns=conns;
				main.xConns.setFast(true);
				main.xConnsByProxyOf=connsProxyOf;
				main.xConnsByProxyOf.setFast(true);
				main.xConnsList = clist;
				main.xConnsList.setFast(true);

				Log.debug("XConn Loader Replaced On Main");
			}

			// start
			if(!cacheOnly)
			{
				for (int i = 0; i < main.xConnsList.size(); i++) 
				{
					ProxyXconn xc = (ProxyXconn) main.xConnsList.get(i);

					XConnLoader l = new XConnLoader(this.main,xc);
					l.start();
				}
			}
			// 

			Log.info("XConn Loader End");
		}
		catch(Exception e)
		{
			Log.error(e);
		}
		finally
		{
			// Always make sure result sets and statements are closed,
			// and the connection is returned to the pool
//			Always make sure result sets and statements are closed,
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

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run(){
		Thread.currentThread().setName("__MG");

		try {
			//    create prospr factory
			if(xc.getXconnCl()!= null && !xc.getXconnCl().equalsIgnoreCase("")) {
				
				// first get the factory
				Class c = Class.forName(xc.getXconnCl());
				IFactory i = (IFactory) c.newInstance();

				// now get the class
				xc.setXconnObj((IXConn) i.getInstance());

				//      start connection
				com.mindergy.mg.interfaces.XConn xcc=new com.mindergy.mg.interfaces.XConn();
				xcc.key=xc.getKey();
				xcc.procId=1;
				xcc.propertiesFile=xc.getPropsFile();
				xcc.ackAsync=xc.getAsyncAck()==1;
				Log.debug("SXCONN KEY " + xc.getKey());
				xc.getXconnObj().start((ISMSReceiver) this.main.smsReceiver,(IXConnController) this.main.xConnController,xcc);

				Log.info("Xconn Started " + xc.getKey());

				if(xc.getChargeThreads() > 0){
					// launch charge monitor
					ChargeQFullCacheMonitor mon=new ChargeQFullCacheMonitor(this.main);
					mon.procId=0;
					mon.queryLines=0;
					mon.waitTime=60000;
					mon.maxthreads=xc.getChargeThreads();
					mon.xConnKey = xc.getKey();     
					main.chargemons.add(mon);
					mon.start();
					Log.info("Charge Mon Started " + xc.getKey());
				}

				if(xc.getNotifThreads() > 0){
					// launch Notif monitor
					NotifQFullCacheMonitor mon=new NotifQFullCacheMonitor(this.main);
					mon.procId=0;
					mon.queryLines=0;
					mon.waitTime=60000;
					mon.maxthreads=xc.getNotifThreads();
					mon.xConnKey = xc.getKey();     
					main.notifmons.add(mon);
					mon.start();
					Log.info("Notif  Mon Started " + xc.getKey());					
				}

				if(xc.getSendThreads() > 0){
					// Senq Notif monitor
					SenQFullCacheMonitor mon=new SenQFullCacheMonitor(this.main);
					mon.procId=0;
					mon.queryLines=0;
					mon.waitTime=60000;
					mon.maxthreads=xc.getSendThreads();
					mon.xConnKey = xc.getKey();     
					main.senqmons.add(mon);
					mon.start();
					Log.info("Senq  Mon Started " + xc.getKey());
				}
				
				RecoverThread rt = new RecoverThread(this.xc);
				rt.start();
				Log.info("Recover Theread Started " + xc.getKey());
			}
			
			Log.info("XConn Loader XConn Full Started " + xc.getKey());
		} 
		catch (Exception e) {
			Log.error(e);
			Log.info("XConn Loader XConn ERROR On Start " + xc.getKey());
		}
	}
}