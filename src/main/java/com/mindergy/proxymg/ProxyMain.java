package com.mindergy.proxymg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.collections.FastArrayList;
import org.apache.commons.collections.FastHashMap;

import com.mindergy.mg.interfaces.XConnSenSMS;
import com.mindergy.proxymg.queues.ChargeQFullCacheMonitor;
import com.mindergy.proxymg.queues.NotifQFullCacheMonitor;
import com.mindergy.proxymg.queues.SenQFullCacheMonitor;
import com.mindergy.util.commons.MiscUtil;
import com.mindergy.util.log.Log;

/**
 * This is the Main class of the application.
 * Proxy_MG is an application used to simulate the activities of the MG 
 * application in countries or servers with slow or problematic connections
 * (usually slow conections). At the time of this writing Argentina and 
 * Paraguay are examples of countries with slow connections and where 
 * there are servers where we have to use proxy_MG.
 * Next is a brief explanation just to get a general idea of how the whole
 * process works.
 * The messages are sent from the Portuguese servers through the MG in 
 * http format and there is a servlet in TomCat that is waiting for 
 * them in the same server where the proxy_MG is implemented, and
 * storing them in a MySql Database or in a file system (with text 
 * files and directories).
 * Then, the DRUM of the server who received the message in the MySql 
 * Database (or file system), gets the message, gives the appropriate 
 * treatment and sends it to the operator, controlling the whole 
 * process of sending and receiving. This is a way of simulating 
 * the same behavior of the MG application when we are connecting directly
 * to the operators through the XConns, without the use of Proxy_MG.
 * When the message process is complete, the results are stored in the 
 * MySql Database and I believe that the DRUM sends the results to Portugal
 * to a TomCat servlet so the information can be centralized in the Oracle
 * 10g Database. (confirm this process with Paulo)  
 * All the communication between the MG and the Proxy_MG’s is done in batches
 * of (n) messages. This is a way of better controlling the sent and received
 * messages and using the slow connections more efficiently.
 * The proxy_MG’s based on file systems are more difficult to manage and
 * also slower to work with so, our teams are making an effort to migrate 
 * from the “file system based” to “MySql based” Proxy_MG’s.
 * @author TIMwe
 */
public class ProxyMain {

	// all xcons of this provider identified by xconnkey
	// Class: XConn 

	/**
	 * Hashmap of the XCONNS for this server
	 */
	public FastHashMap xConns=new FastHashMap();
	/**
	 * Hashmap with XCONNS by proxy
	 */
	public FastHashMap xConnsByProxyOf=new FastHashMap();
	/**
	 * A list with all XCONNS
	 */
	public FastArrayList xConnsList = new FastArrayList();

	/**
	 * 
	 */
	public FastHashMap services=new FastHashMap();
	/**
	 * The list with the services
	 */
	public FastArrayList servicesList = new FastArrayList();

	/**
	 * Persistent set of properties for the proxymg application
	 */
	public Properties props=null;


	/**
	 * The receiver that will be called by all XCOnn when receiving 
	 * messages or deliconfs.
	 */
	public SMSReceiver smsReceiver=new SMSReceiver(this);


	/**
	 * The xconn controller called by xconn when teir status changes
	 */
	public XConnController xConnController=new XConnController(this);

	/**
	 * Array list for the send queue monitors
	 */
	public ArrayList senqmons=new ArrayList();
	/**
	 * Array list for the notification monitors
	 */
	public ArrayList notifmons=new ArrayList();
	/**
	 * Array list for the charge monitors
	 */
	public ArrayList chargemons=new ArrayList();

	/**
	 * Flag to know if there is a shutdown in progress.
	 * Helps when the application is trying to close and certain threads are
	 * running.
	 */
	public boolean shutdownInProgress=false;

	/**
	 * Hashmap with acknowledge waits
	 */
	public volatile HashMap ackWaitings=new HashMap();

	/**
	 * Proxy Main object memory space
	 */
	private static ProxyMain main=null;

	/**
	 * This method creates a new proxy in memory if one is not yet created,
	 * loads the corresponding properties file and creates the proxy object.
	 * It also loads all the services and the xconns.
	 * @return
	 */
	public static ProxyMain getProxyMain()
	{
		if(ProxyMain.main!=null)
			return ProxyMain.main;
		else
		{
			ProxyMain main = new ProxyMain();
			main.props=MiscUtil.getProperties("proxymg.properties");
			ProxyMain.main=main;


			// load services
			ServiceLoader sl=new ServiceLoader(main);
			sl.load(true);

			// load xconns
			XConnLoader xcl=new XConnLoader(main);
			xcl.load(true);

			return ProxyMain.main;
		}
	}

	/**
	 * This method starts the proxymg application by getting and loading
	 * the properties file, loading services and xconns.
	 */
	public void startProxyMg()
	{
		props=MiscUtil.getProperties("proxymg.properties");


		// load services
		ServiceLoader sl=new ServiceLoader(this);
		sl.load(false);


		// load xconns
		XConnLoader xcl=new XConnLoader(this);
		xcl.load(false);

		ProxyMain.main=this;

	}


	/**
	 * This method closes the application by setting the closing flag
	 * to true and by closing all the monitors doing the same thing.
	 */
	public void endMg()	{
		this.shutdownInProgress=true;
		try{
			Log.info("ENDING PROXY MG");
			for (int i = 0; i < senqmons.size(); i++)
			{
				((SenQFullCacheMonitor) senqmons.get(i)).ending=true;
			}
			for (int i = 0; i < notifmons.size(); i++)
			{
				((NotifQFullCacheMonitor) notifmons.get(i)).ending=true;
			}
			for (int i = 0; i < chargemons.size(); i++)
			{
				((ChargeQFullCacheMonitor) chargemons.get(i)).ending=true;
			}
			Thread.sleep(20000);
			Log.info("ENDED PROXY MG");
		}
		catch(Exception e){
			Log.error(e);
		}
	}




	/**
	 *TODO(Help)
	 * @param sms
	 * Acrescenta msgs no buffer de acknowledgmets
	 * 
	 */
	public void addToAckWaitings(ProxyMt sms, XConnSenSMS xconnSms)	{
		synchronized (this.ackWaitings) {
			this.ackWaitings.put(String.valueOf(sms.getSenqId()), new PairBean(sms, xconnSms));
		}
	}

	public PairBean getFromAckWaitings(String senqId) {
		synchronized (this.ackWaitings) {
			return (PairBean) this.ackWaitings.remove(senqId);
		}
	}
	
	class PairBean{
		
		public ProxyMt sms;
		public XConnSenSMS xconnSms;
		
		private PairBean(ProxyMt aSms, XConnSenSMS aXconnSms){
			sms = aSms;
			xconnSms = aXconnSms;
		}
	}
}
