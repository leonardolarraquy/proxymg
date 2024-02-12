/*
 * Created on Mar 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.mindergy.proxymg.queues;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import com.mindergy.proxymg.ProxyMain;
import com.mindergy.proxymg.ProxyMt;
import com.mindergy.proxymg.ProxyXconn;
import com.mindergy.proxymg.SmsFinal;
import com.mindergy.util.log.Log;

/**
 * This class processes the message of the notifq table.
 * @author TIMwe
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class NotifQFullCacheProcessor extends Thread
{
	/**
	 * The father
	 */
	private ProxyMain main;

	/**
	 * The cache monitor
	 */
	private NotifQFullCacheMonitor mon;

	/**
	 * The xconn id
	 */
	private int xconnKey=0;
	/**
	 * The senq id
	 */
	private long senQid;



	/**
	 * The message
	 */
	private ProxyMt sms;
	/**
	 * Controls where the engine is attempting to quit in a clean way, i.e.,
	 * waiting for all the current running threads to finnish.
	 */
	protected boolean ending = false;

	/**
	 * This method sets the father class for this class and
	 * establishes the monitor.
	 * @param main -> Father class object
	 * @param mon -> NotifQFullCacheMonitor object
	 */
	public NotifQFullCacheProcessor(ProxyMain main, NotifQFullCacheMonitor mon)
	{
		this.main = main;
		this.mon = mon;
	}

	/** TODO Help
	 */
	public void run()
	{
		try
		{
			xconnKey=this.mon.xConnKey;
			int stime = 1;
			Log.info("NOTIFQ Cache Processor for xconn " + this.mon.xConnKey
					+ " started");
			while (true)
			{
				try
				{
					this.sms = this.mon.giveMeNext();
					if((this.ending || this.main.shutdownInProgress) && this.sms==null)
						break;
					if (this.sms!=null)
					{
						this.senQid=sms.getSenqId();
						this.doNotify();
						stime = 0;
					} else
						stime = 500;

					if (stime > 0)
					{
						//Log.debug("nothing to process. Sleeping");
						Thread.sleep(stime);
					}
				} catch (Exception e)
				{
					Log.error(e);
				}
			}
		} catch (Exception e)
		{
			Log.error(e);
		}
	}


	/**
	 * This method sends the message
	 */
	public void doNotify()
	{
		try
		{
			Thread.currentThread().setName("__MG");
			Date start=new Date();
			// get data from queue
			Log.debug("NOTIFQPROC START " + senQid);
			Log.debug("SendQProc get queuedata");
			Thread.currentThread().setName("__MG_NOTIF");
			Log.info("NOTIFQPROC START " + senQid);
			boolean moveToFinal=false;
			Date part1=new Date();
			Date part1_1=new Date();
			Date part2=new Date();
			Date end=new Date();
			ProxyXconn xc=(ProxyXconn) main.xConns.get(String.valueOf(xconnKey));
			if(xc==null)
			{
				Log.error("ID: "+sms.getSenqId() +"XCONN NOT FOUND IN MAP FOR:" +xconnKey);
				return;
			}

			// call charge
			boolean result=this.doNotify(xc);

			part2=new Date();
			// if not sent
			if(!result)
			{
				if(!this.doRetry(xc))
				{
					moveToFinal=true;
					sms.setNotifStatus(20);
				}
			}
			else
			{
				sms.setNotifStatus(10);
				moveToFinal=true;
			}

			if(moveToFinal)
				this.moveToFinal();

			end = new Date();
			Log.info("NOTIFQPROC END " + senQid  +" SID: "+sms.getServiceId()+ " LA: "+sms.getOrigin()+ " OP: " + sms.getOpId() + " RES: " + sms.getNotifStatus()+ " PREPR:" +(part1_1.getTime()-part1.getTime())+ " SEND: "+(part2.getTime()-part1_1.getTime())+" POST: "+(end.getTime()-part2.getTime())+   " TIME: "+(end.getTime()-start.getTime()) + " FST: "+moveToFinal+ " CH: "+sms.isCharge());            

		} catch (Exception e)
		{
			Log.error("mgid: "+this.senQid,e);
		}

	}








	/**
	 * This method seds a notification through a proxy xconn
	 * @param xc -> proxy xconn to use
	 * @return -> True/False
	 */
	private boolean doNotify(ProxyXconn xc )
	{

		StringBuffer url=new StringBuffer(main.props.getProperty("notif.url"));
		url.append("id=");
		url.append(sms.getSenqId());
		url.append("&status=");
		if(sms.getSendStatus()==0 && sms.isCharge())
			url.append(sms.getChargeStatus());
		else
			url.append(sms.getSendStatus());
		String result=this.callGatewayGet(url.toString());
		Log.info("ID: "+sms.getSenqId() + " Notified "+ result);
		return result!=null;
	}




	/**
	 * This method deletes a message from the notifq table
	 */
	private void moveToFinal()
	{
		// delete from senq
		Notifq.delete(sms.getSenqId());

		sms.setStatus(0);

		// add to SMSFinal
		SmsFinal.insert(sms);
		Log.info("ID: "+sms.getSenqId() + " Moved to SMSFinal");
	}

	/**
	 * This method increments the retry count if their limit is
	 * not esceeded yet.
	 * @param xc -> Proxy Xconn to use
	 * @return -> True/False
	 */
	private boolean doRetry(ProxyXconn xc)
	{
		// check if reached limit
		if(sms.getRetries()>=xc.getNotifRetries())
		{
			Log.debug("ID: "+sms.getSenqId() + " Max retries reached");
			return false;
		}
		else
		{
			final int ONE_MINUTE = 1000 * 60;
			final int ONE_HOUR   = ONE_MINUTE * 60;

			//increment retries
			sms.setRetries(sms.getRetries()+1);
			
			Date newStime=new Date(System.currentTimeMillis() + ONE_HOUR);
			Notifq.retry(sms.getSenqId(), sms.getXconnKey(), sms.getRetries(), newStime);
			Log.info("ID: "+sms.getSenqId() + " Retry #"+sms.getRetries() + " done. Next retry: " + newStime);
			return true;
		}
	}




	/**TODO Help
	 * http call to notify mg
	 * @param url
	 * @param urlTimeout
	 * @return
	 */
	private String callGatewayGet(String url){
		try{
			HttpClient client = new HttpClient();
			//establish a connection within 5 seconds
			client.setConnectionTimeout(10000);
			client.setStrictMode(false);
			client.setTimeout(15000);

			Log.info("Calling url: " + url );
			GetMethod method = new GetMethod(url);
			method.setFollowRedirects(true);

			method.addRequestHeader("Accept","*/*");
			method.addRequestHeader("Connection","Keep-Alive");

			//execute the method

			client.executeMethod(method);
			String responseBody =  method.getResponseBodyAsStream().toString();

			//clean up the connection resources
			method.releaseConnection();
			//method.recycle();
			Log.debug("Minimg RESPONSE :" + responseBody);
			return responseBody.trim();
		} catch (HttpException he) {
			Log.error("Notif Http error connecting to '" + url + "'", he);
			return null;
		} catch (IOException ioe){
			Log.error("Notif Unable to connect to '" + url + "'",ioe);
			return null;
		} catch (Exception ioe2){
			Log.error(ioe2);
			return null;
		}
	}
}
