/*
 * Created on Mar 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.mindergy.proxymg.queues;

import java.util.Calendar;
import java.util.Date;

import com.mindergy.proxymg.ICharger;
import com.mindergy.proxymg.ProxyMain;
import com.mindergy.proxymg.ProxyMt;
import com.mindergy.proxymg.ProxyService;
import com.mindergy.proxymg.ProxyXconn;
import com.mindergy.proxymg.XChargerResp;
import com.mindergy.util.commons.MiscUtil;
import com.mindergy.util.log.Log;

/**
 * This class processes the messages for the chargeq. 
 * @author TIMwe
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ChargeQFullCacheProcessor extends Thread
{
	/**
	 * The father
	 */
	private ProxyMain main;

	/**
	 * Object declaration
	 */
	private ChargeQFullCacheMonitor mon;

	/**
	 * Xconn key property declaration
	 */
	private int xconnKey=0;
	/**
	 * SenQid property declaration
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
	 * Constructor.
	 * This method established the proxymain object as the father
	 * and sets the charge ful cache monitor.
	 * @param main -> Father class
	 * @param mon -> Full cache monitor
	 */
	public ChargeQFullCacheProcessor(ProxyMain main, ChargeQFullCacheMonitor mon)
	{
		this.main = main;
		this.mon = mon;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		try
		{
			xconnKey=this.mon.xConnKey;
			int stime = 1;
			Log.info("CHARGEQ Cache Processor for xconn " + this.mon.xConnKey
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
						this.charge();
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
	public void charge()
	{
		try
		{
			Thread.currentThread().setName("__MG");
			Date start=new Date();
			// get data from queue
			Log.debug("CHARGEQPROC START " + senQid);
			Log.debug("SendQProc get queuedata");
			XChargerResp resp = new XChargerResp();
			Thread.currentThread().setName("__MG_CHARGE_" + xconnKey);
			Log.info("CHARGEQPROC START " + senQid);
			boolean moveToSenq=false;

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
				resp.setChargeResult(20);
				sms.setSendStatus(0);
				// call charge
				resp=this.doCharge(xc);

				part2=new Date();
				// if not sent
				if(!resp.isProceed())
				{
					// do retry
					if(resp.getRetriesOverride()>0)
						sms.setRetries(resp.getRetriesOverride());
					if(!this.doRetry(xc))
					{
						moveToSenq=true;
						sms.setChargeStatus(20);
					}
				}
				else
				{
					//if not asynck and sent
					moveToSenq=true;
					sms.setChargeStatus(resp.getChargeResult());
				}
			}

			if(moveToSenq)
				this.moveToSenq(xc);

			end = new Date();
			Log.info("CHARGEQPROC END " + senQid  +" SID: "+sms.getServiceId()+ " LA: "+sms.getOrigin()+ " RES: " + resp.isProceed() + " POST: "+(end.getTime()-part2.getTime())+ " TIME: "+(end.getTime()-start.getTime()) + " FST: "+moveToSenq+ " CH: "+sms.isCharge() + " REF1: " + sms.getRef1());            

		} catch (Exception e)
		{
			Log.error("mgid: "+this.senQid,e);
		}

	}



	/**
	 * This method checks for allowed hours to send messages.
	 * @param xc -> Proxy XConn
	 * @return -> True/False
	 */
	private boolean checkAllowedHours(ProxyXconn xc)
	{
		boolean result=true;
		Calendar cal = Calendar.getInstance();
		int curHour=cal.get(Calendar.HOUR_OF_DAY);
		if(sms.getPriority()>=8 && xc.getChargeStartHour()!=0 && xc.getChargeEndHour()!=0)
		{
			if(curHour>=xc.getChargeStartHour() && curHour<xc.getChargeEndHour())
				result=true;
			else
			{
				long dtn=MiscUtil.getTruncateDateInMs(new Date());
				long offset=xc.getChargeStartHour()*60*60*1000;
				long addDay=0;
				if(curHour>xc.getChargeEndHour())
					addDay=24*60*60*1000;
				Date newStime=new Date(dtn+offset+addDay-cal.getTimeZone().getRawOffset());
				Chargeq.retry(sms.getSenqId(), sms.getXconnKey(), sms.getRetries(), newStime, sms.getChargeStatus());
				Log.debug("ID: "+sms.getSenqId() + " Check allowed hours. Moved to "+ newStime);		 
				result=false;
			}
		}
		Log.debug("ID: "+sms.getSenqId() + " Check allowed hours "+ result);
		return result;
	}



	/**
	 * This method is responsible for charging the messages.
	 * @param xc -> Proxy Xconn
	 * @return -> Charge result
	 */
	private XChargerResp doCharge(ProxyXconn xc )
	{
		XChargerResp resp=new XChargerResp ();
		if(sms.getCtype()==1 && sms.getMessage()==null)
		{
			Log.error("MESSAGE SEND CANCEL. WAS NULL " + sms.getSenqId());
			resp.setChargeResult(21);
			resp.setProceed(false);
		}
		else{

			ProxyService serv = (ProxyService) this.main.services.get(""+sms.getServiceId());

			if(serv.getChargeFact()!=null)
			{
				Log.debug("Going to Call Charger");
				ICharger pr = (ICharger) serv.getChargeFact().getInstance();
				resp=pr.chargeSms(sms);
				Log.debug("After Call Charger");
			}
			Log.debug("ID: "+sms.getSenqId() + " Called Prepr. Proceed"+ resp.isProceed());
		}
		Log.debug("ID: "+sms.getSenqId() + " Called Charge "+ resp.getChargeResult());
		return resp;
	}




	/**
	 * This method is responsible for moving the mesages to the
	 * senq table.
	 * @param xc -> Proxy Xconn to use
	 */
	private void moveToSenq(ProxyXconn xc)
	{
		// delete from senq
		Chargeq.delete(sms.getSenqId(),sms.getXconnKey());

		// chekc if is going to move to senq or notifq
		sms.setRetries(0);
		sms.setStatus(0);
		if(sms.getChargeStatus()==10)
		{
			// add to senq
			Senq.insert(sms);
			Log.info("ID: "+sms.getSenqId() + " Moved to senq");
		}
		else
		{
//			// check if failed notifs also be notified
			if(sms.getChargeStatus()==10 || (sms.getSendStatus()!=10 && xc.isNotifyFailed()))
			{
				// add to notifq
				Notifq.insert(sms);
				Log.info("ID: "+sms.getSenqId() + " Moved to notif");
			}
			else
			{
//				add to notifq
				// SmsFinal.insert(sms);
				Log.info("ID: "+sms.getSenqId() + " Moved to final");
			}
		}

	}

	/**
	 * This method verifies if the retry number of the messages
	 * has reachaed its limits and if not it increments one to the 
	 * counter
	 * @param xc -> Proxy Xconn to use
	 * @return -> True /False
	 */
	private boolean doRetry(ProxyXconn xc)
	{
		// check if reached limit
		if(sms.getRetries()>=xc.getChargeRetries())
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

			// increment retries
			sms.setRetries(sms.getRetries()+1);
			Date newStime=new Date(System.currentTimeMillis() + delay);
			Chargeq.retry(sms.getSenqId(), sms.getXconnKey(), sms.getRetries(), newStime, sms.getChargeStatus());
			Log.info("ID: "+sms.getSenqId() + " Retry #"+sms.getRetries() + " done. Next retry: " + newStime);
			return true;
		}
	}
}