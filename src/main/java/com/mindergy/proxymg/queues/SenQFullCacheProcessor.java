/*
 * Created on Mar 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.mindergy.proxymg.queues;

import java.util.Calendar;
import java.util.Date;

import com.mindergy.mg.engine.SenSMS;
import com.mindergy.mg.interfaces.IXAppPostPr;
import com.mindergy.mg.interfaces.IXAppPrePr;
import com.mindergy.mg.interfaces.XAppPostPr;
import com.mindergy.mg.interfaces.XAppPrePrResp;
import com.mindergy.mg.interfaces.XConnSenSMS;
import com.mindergy.mg.interfaces.XConnSenSMSResp;
import com.mindergy.proxymg.ProxyMain;
import com.mindergy.proxymg.ProxyMt;
import com.mindergy.proxymg.ProxyService;
import com.mindergy.proxymg.ProxyXconn;
import com.mindergy.util.commons.MiscUtil;
import com.mindergy.util.log.Log;

/**
 * This class processes the messages from the senq database. 
 * @author TIMwe
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class SenQFullCacheProcessor extends Thread
{
	/**
	 * The father
	 */
	private ProxyMain main;

	/**
	 * The responsible monitor
	 */
	private SenQFullCacheMonitor mon;

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
	 * The message in XConnSenSMS format
	 */
	private XConnSenSMS smsForSend;


	/**
	 * Controls where the engine is attempting to quit in a clean way, i.e.,
	 * waiting for all the current running threads to finnish.
	 */
	protected boolean ending = false;

	/**
	 * This method establishes the father object of this class and
	 * the associated monitor class object
	 * @param main -> Father
	 * @param mon -> Monitor
	 */
	public SenQFullCacheProcessor(ProxyMain main, SenQFullCacheMonitor mon)
	{
		this.main = main;
		this.mon = mon;
	}

	/** This method processes the messages from cache and sends them.
	 * 
	 */
	public void run()
	{
		try
		{
			xconnKey=this.mon.xConnKey;
			int stime = 1;
			Log.info("SENQ Cache Processor for xconn " + this.mon.xConnKey
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
						this.sendSMS();
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
	 * This method sends a message
	 */
	public void sendSMS()
	{
		try
		{
			Thread.currentThread().setName("__MG");
			Date start=new Date();
			// get data from queue
			Log.debug("SENQPROC START " + senQid);
			Log.debug("SendQProc get queuedata");
			XConnSenSMSResp resp = new XConnSenSMSResp();
			Thread.currentThread().setName("__MG_SEND_" + xconnKey);
			Log.info("SENQPROC START " + senQid);
			boolean moveToNotif=false;
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
			// check allowed hours
			if(this.checkAllowedHours(xc))
			{
				resp.sendStatus=false;

				// call pre pr and check proceed
				XAppPrePrResp presp=this.callPrePr();
				part1_1=new Date();
				if(presp.cancelSend)
				{
					sms.setSendStatus(30); // failed on prepr
					moveToNotif=true;
					// check retry
					if(presp.retriesOverride>xc.getSendRetries())
					{	
						sms.setRetries(presp.retriesOverride);
						if(this.doRetry(xc))
							moveToNotif=false;
					} else if (presp.retriesOverride == -1) {   // -1, Don't use retriesOverride to retry. The previous IF seems with a bug since it puts sms.retries=retriesOverride and then it cannot be retried in the doRetry method
						if(this.doRetry(xc))
							moveToNotif=false;
					}
				}
				else
				{
					// call send
					resp=this.sendToXconn(xc);

					part2=new Date();
					// if not sent
					if(!resp.sendStatus)
					{
						// do retry
						if(!this.doRetry(xc))
						{
							moveToNotif=true;
							sms.setSendStatus(20);
						}
					}
					else
					{
						// if async ack delegate process to reception
						if(xc.getAsyncAck()==1)
						{
							this.waitForAsyncAck();
						}
						else
						{
							//if not asynck and sent
							moveToNotif=true;
							sms.setSendStatus(10);
						}
					}
				}
			}

			this.callPostPr();

			if(moveToNotif)
				this.moveToNotif(xc);

			end = new Date();
			Log.info("SENQPROC END " + senQid  +" SID: "+sms.getServiceId()+ " LA: "+sms.getOrigin()+ " RES: " + sms.getSendStatus()+ " PREPR:" +(part1_1.getTime()-part1.getTime())+ " SEND: "+(part2.getTime()-part1_1.getTime())+" POST: "+(end.getTime()-part2.getTime())+   " TIME: "+(end.getTime()-start.getTime()) + " FST: "+moveToNotif+ " CH: "+sms.isCharge());            

		} 
		catch (Exception e){ 	
			Log.error("mgid: "+this.senQid,e);
		}
	}

	/**
	 * This method checks for allowed hours to send messages
	 * @param xc -> Proxy xconn
	 * @return -> True/False
	 */
	private boolean checkAllowedHours(ProxyXconn xc)
	{
		boolean result=true;
		Calendar cal = Calendar.getInstance();
		int curHour=cal.get(Calendar.HOUR_OF_DAY);
		if(sms.getPriority()>=8 && xc.getSendStartHour()!=0 && xc.getSendEndHour()!=0)
		{
			if(curHour>=xc.getSendStartHour() && curHour<xc.getSendEndHour())
				result=true;
			else
			{
				long dtn=MiscUtil.getTruncateDateInMs(new Date());
				long offset=xc.getSendStartHour()*60*60*1000;
				long addDay=0;
				if(curHour>xc.getSendEndHour())
					addDay=24*60*60*1000;
				Date newStime=new Date(dtn+offset+addDay-cal.getTimeZone().getRawOffset());
				Senq.retry(sms.getSenqId(), sms.getXconnKey(), sms.getRetries(), newStime);
				Log.debug("ID: "+sms.getSenqId() + " Check allowed hours. Moved to "+ newStime);
				result=false;
			}
		}
		Log.debug("ID: "+sms.getSenqId() + " Check allowed hours "+ result);
		return result;
	}




	/**
	 * This method calls the pre-processing of messages
	 * @return -> result of the pre processing of the message
	 */
	private XAppPrePrResp callPrePr()
	{
		XAppPrePrResp result=new XAppPrePrResp();
		ProxyService serv = (ProxyService) this.main.services.get(""+sms.getServiceId());

		if(serv!=null && serv.getPrePrFact()!=null)
		{
			Log.debug("Going to Call Pre Pr");
			IXAppPrePr pr = (IXAppPrePr) serv.getPrePrFact().getInstance();
			result = pr.preProcess(sms.convertToXConnSenSMS());
			Log.debug("After Call Pre Pr");
		}
		Log.debug("ID: "+sms.getSenqId() + " Called Prepr. Proceed"+ !result.cancelSend);
		return result;
	}

	/**
	 * This method calls the pre-processing of messages
	 * @return -> result of the pre processing of the message
	 */
	private void callPostPr(){
		ProxyService serv = (ProxyService) this.main.services.get(""+sms.getServiceId());
		ProxyXconn xc     = (ProxyXconn) ProxyMain.getProxyMain().xConns.get("" + sms.getXconnKey());

		//ONLY CALL HERE IF ITS NOT ASYNC!
		if(serv!=null && serv.getPostFact()!=null && xc.getAsyncAck() == 0){
			IXAppPostPr pr = (IXAppPostPr) serv.getPostFact().getInstance();
			SenSMS sensms = sms.convertToSenSMS();
			
			if(sms.getSendStatus() == 10)
				sensms.totalSent = 1;
			
			if(sms.getSendStatus() == 20 && sensms.retries >=xc.getSendRetries())
				sensms.totalSent = -1;
			
			pr.postProcess(new XAppPostPr(sensms));
		}
	}


	/**
	 * This method sends the messages to the Xconn
	 * @param xc -> Xconn to send the message
	 * @return -> result from the sending SMS command
	 */
	private XConnSenSMSResp sendToXconn(ProxyXconn xc )
	{
		XConnSenSMSResp resp=new XConnSenSMSResp ();
		if(sms.getCtype()==1 && sms.getMessage()==null)
		{
			Log.error("MESSAGE SEND CANCEL. WAS NULL " + sms.getSenqId());
			resp.sendStatus=false;
		}
		else{
			this.smsForSend = sms.convertToXConnSenSMS();

			if(xc.getUseSenqQ() == 0)
				resp = xc.getXconnObj().sendSMS(smsForSend);
			else{
				ProxyXconn xconn = (ProxyXconn) main.getProxyMain().xConns.get(String.valueOf(xc.getUseSenqQ()));
				resp = xconn.getXconnObj().sendSMS(smsForSend);
			}

		}
		Log.info("ID: "+sms.getSenqId() + " Send To Xconn"+ resp.sendStatus);
		return resp;
	}

	private void moveToNotif(ProxyXconn xc)
	{
		// delete from senq
		Senq.delete(sms.getSenqId(),sms.getXconnKey());

		sms.setRetries(0);
		sms.setStatus(0);

		// check if failed notifs also be notified
		if((sms.getSendStatus()==10 || (sms.getSendStatus()!=10 && xc.isNotifyFailed())) && sms.isCharge())
		{
			// add to notifq
			Notifq.insert(sms);
			Log.info("ID: "+sms.getSenqId() + " Moved to notif");
		}
		else
		{
			//			add to notifq
			//	 SmsFinal.insert(sms);
			Log.info("ID: "+sms.getSenqId() + " Moved to final");
		}
	}

	private void waitForAsyncAck()
	{
		this.main.addToAckWaitings(sms,smsForSend);
		Log.info("ID: "+sms.getSenqId() + " Put in AsyncAck");
	}

	/** This method retries to send a message
	 * @param xc -> Xconn
	 * @return -> True/False
	 */
	private boolean doRetry(ProxyXconn xc)
	{
		// check if reached limit
		if(sms.getRetries()>=xc.getSendRetries())
		{
			Log.debug("ID: "+sms.getSenqId() + " Max retries reached");
			return false;
		}
		else
		{
			final int ONE_MINUTE = 1000 * 60;
			final int ONE_HOUR   = ONE_MINUTE * 60;

			long delay = ONE_HOUR * 4;

			if(xc.getRetryMinutes() != 0)
				delay = ONE_MINUTE * xc.getRetryMinutes();

			//increment retries
			sms.setRetries(sms.getRetries()+1);

			Date newStime=new Date(System.currentTimeMillis() + delay);
			Senq.retry(sms.getSenqId(), sms.getXconnKey(), sms.getRetries(), newStime);
			Log.info("ID: "+sms.getSenqId() + " Retry #"+sms.getRetries() + " done. Next retry: " + newStime);
			return true;
		}
	}

}
