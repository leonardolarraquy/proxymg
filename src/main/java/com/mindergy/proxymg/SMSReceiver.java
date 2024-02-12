package com.mindergy.proxymg;
import java.util.Date;
import java.util.List;

import com.mindergy.mg.interfaces.ISMSReceiver;
import com.mindergy.mg.interfaces.IXAppPostPr;
import com.mindergy.mg.interfaces.XAppPostPr;
import com.mindergy.mg.interfaces.XConnDeliConf;
import com.mindergy.mg.interfaces.XConnRecSMS;
import com.mindergy.mg.interfaces.XConnRecSMSResp;
import com.mindergy.proxymg.queues.Notifq;
import com.mindergy.proxymg.queues.Senq;
import com.mindergy.util.log.Log;
import com.mindergy.mg.engine.SenSMS;
import com.mindergy.proxymg.ProxyXconn;

/* 
 * $Source$
 * $Revision: 104774 $
 * $Author: leonardo.larraquy $
 * $Date: 2010-03-08 15:07:08 -0300 (Mon, 08 Mar 2010) $
 * $Name$
 * $State$
 * $Log$
 * Revision 1.2  2007/05/31 18:22:32  pps
 * *** empty log message ***
 *
 * Revision 1.1  2007/04/13 10:27:31  pps
 * *** empty log message ***
 *
 * Revision 1.8  2006/11/30 16:22:42  pps
 * *** empty log message ***
 *
 * Revision 1.7  2006/11/26 16:36:21  pps
 * *** empty log message ***
 *
 * Revision 1.6  2006/11/25 13:22:01  pps
 * *** empty log message ***
 *
 * Revision 1.5  2006/09/26 21:07:46  pps
 * *** empty log message ***
 *
 * Revision 1.4  2006/09/17 23:32:39  pps
 * *** empty log message ***
 *
 * Revision 1.3  2006/08/20 20:15:12  pps
 * *** empty log message ***
 *
 * Revision 1.2  2006/08/20 14:57:58  pps
 * *** empty log message ***
 *
 * Revision 1.1  2006/08/18 20:00:03  pps
 * *** empty log message ***
 *
 * Revision 1.41  2006/06/16 15:49:09  ics
 * Include service id in receive sms response
 *
 * Revision 1.40  2006/05/19 17:18:08  pps
 * add control of repeated gw ids on SMSReceiver. Block them, if repeated
 *
 * Revision 1.39  2006/05/19 14:59:03  pps
 * add control of repeated gw ids on SMSReceiver. Block them, if repeated
 *
 * Revision 1.38  2006/04/15 00:06:22  pps
 * async pospr
 * notif queues
 * notif post pr
 *
 * Revision 1.37  2006/03/04 17:06:04  pps
 * add support for status reception and sen conf control
 *
 * Revision 1.36  2006/02/17 20:48:13  pps
 * add optional replaceptchars based on prop
 *
 * Revision 1.35  2006/01/23 17:37:34  ics
 * mg id is now being set on XConnRecSMSResp result object
 *
 * Revision 1.34  2006/01/23 16:01:24  ics
 * Conf delivery fix
 *
 * Revision 1.33  2005/12/16 17:18:43  ics
 * Added xconn key to debug line
 *
 * Revision 1.32  2005/12/02 00:18:57  pps
 * add new IntegrationType=10 STATIC_RESPONSE_WAP_PUSH
 *
 * Revision 1.31  2005/09/29 21:15:48  pps
 * prevent origins bigger than 16 chars
 *
 * Revision 1.30  2005/09/25 23:39:23  pps
 * hammer para a black list da Movistar Col
 *
 * Revision 1.29  2005/09/14 18:12:48  pps
 * bug fix in mt route
 *
 * Revision 1.28  2005/08/26 15:20:44  pps
 * implement receiveConf
 *
 * Revision 1.27  2005/08/23 11:33:27  pps
 * new props for origin and xconn in auto_response
 *
 * Revision 1.26  2005/07/22 15:09:41  cmm
 * method SMSReceiver# insertRecSMS(XConnRecSMS) adds the msgRecId to the request SMS object.
 *
 * Revision 1.25  2005/07/08 16:39:03  ics
 * Origin of auto responses can now be set on mg property file based on input origin, xconn and service id
 *
 * Revision 1.24  2005/06/27 23:38:02  pps
 * add mt.route.xxxx to mg.properties in auto response
 *
 * Revision 1.23  2005/05/23 11:40:50  pps
 * add user access control
 *
 * Revision 1.22  2005/05/04 14:39:07  pps
 * use same xconn in auto async response (8)
 *
 * Revision 1.21  2005/04/16 15:39:50  pps
 * add xconn to service reception discovery process
 *
 * Revision 1.20  2005/04/11 19:25:09  pps
 * add xconn to XAPPPostPR
 *
 * Revision 1.19  2005/02/06 03:58:57  pps
 * query optimize
 *
 * Revision 1.18  2005/01/26 04:54:40  pps
 * add fwd message service reception action
 *
 * Revision 1.17  2005/01/12 21:20:27  pps
 * no message
 *
 * Revision 1.16  2004/12/28 01:35:02  pps
 * prevent nonnumbers in origin and dest fields
 *
 * Revision 1.15  2004/12/13 12:03:35  pps
 * add TIME print info for SSReceive and RecqProcessor
 *
 * Revision 1.14  2004/11/29 15:50:13  pps
 * OperatorId support in all rec and send classes
 *
 * Revision 1.13  2004/10/23 02:41:49  pps
 * memory optimizations
 *
 * Revision 1.12  2004/08/09 18:58:13  pps
 * add ext_id field to mo-mt flow interface classes
 *
 * Revision 1.11  2004/07/07 00:34:00  pps
 * implement can_sync auto control for xconn
 *
 * Revision 1.10  2004/07/03 23:58:59  pps
 * add a try catch in senqprocessor run
 *
 * Revision 1.9  2004/06/08 15:27:57  pps
 * add support for operator id field in sent and received messages
 * * Revision 1.8  2004/06/05 23:25:31  pps
 * fix parameter  get in smsreceiver
 *
 * Revision 1.7  2004/04/27 12:40:45  pps
 * add cvs keyrwords
 * set req_qdate to systimestamp
 *
 *
 */

/**
 * This class represents the receiver that will be called by all XCOnn when receiving messages or deliconfs
 * do a lot of stuff
 * service finder, check async/sync ptype, calls targets, handles responses
 * yes, it handles responses, which means it can simply put on sendq a response message
 * or even better, return it to the Xconn and cross you fingers, to see if he is able 
 * to process it on the current receive process
 * it's quite big but not scary
 * has main function for receiving SMS, called receiveSMS (of course)
 * this a typical ps (it's my name) function, short and clear
 * with main logical and calling methods for detail code
 * 
 * oops, i forgot something
 * this also receives delivery confirmations, has a function for that
 * guess its name? receiveConf
 * i'll talk about it later (it's not yet done :( )
 * @author TIMwe
 *
 */
public class SMSReceiver implements ISMSReceiver 
{



	/**
	 * Main class object declaration
	 */
	private ProxyMain main;

	/**
	 * This method creates the relationship between the
	 * smsreceiver and the main class.
	 * @param main
	 */
	public SMSReceiver(ProxyMain main) 
	{
		this.main=main;
	}

	/**
	 * This method is the main function for receiving SMS
	 */
	public XConnRecSMSResp receiveSMS(XConnRecSMS recSms)
	{
		XConnRecSMSResp result = new XConnRecSMSResp();
		return result;
	}

	public void receiveConf(XConnDeliConf recConf)
	{

	}
	
	public void receiveAck(String mgId, String gwId, int status){
		
	}
	
	public boolean receiveConf(List<XConnDeliConf> recConfList){
		return false;
	}

	/**This method treats and receives acknowledgments
	 * @param mgId -> Id of the proxy mg
	 * @param status -> TODO (Help)
	 */
	public void receiveAck(String mgId,int status)
	{
		boolean moveToNotif=false;
		ProxyMain.PairBean bean = main.getFromAckWaitings(mgId);

		if(bean==null)
		{
			Log.error("Ack NOT FOUND "+mgId);
		}
		else if(status!=10 && status!=91)
		{
			Log.info("Ack: NOTSENT "+mgId+" ST: "+status + " LA: "+ bean.xconnSms.origin + " DEST: " + bean.xconnSms.destination + " SID: "+ bean.xconnSms.serviceId + " OP: "+ bean.xconnSms.operatorId + " RET#: " + bean.xconnSms.retries + " REF1: " + bean.sms.getRef1());
			// do retry
			if(!this.doRetry(bean.sms))
			{
				moveToNotif=true;
				bean.sms.setSendStatus(20);
			}
		}
		else
		{
			//if not asynck and sent
			moveToNotif=true;
			bean.sms.setSendStatus(10);
			Log.info("Ack: SENT "+mgId+" ST: "+status + " LA: "+ bean.xconnSms.origin + " DEST: " + bean.xconnSms.destination + " SID: "+ bean.xconnSms.serviceId + " OP: "+ bean.xconnSms.operatorId + " RET#: " + bean.xconnSms.retries + " REF1: " + bean.sms.getRef1());
		}
		
		this.callPostPr(bean);
		
		if(moveToNotif)
			this.moveToNotif(bean.sms);
	}

	/**
	 * This method calls the pre-processing of messages
	 * @return -> result of the pre processing of the message
	 */
	private void callPostPr(ProxyMain.PairBean bean){
		ProxyService serv = (ProxyService) this.main.services.get(""+bean.sms.getServiceId());
		ProxyXconn xc     = (ProxyXconn) ProxyMain.getProxyMain().xConns.get("" + bean.sms.getXconnKey());
		
		if(serv!=null && serv.getPostFact()!=null){
			IXAppPostPr pr = (IXAppPostPr) serv.getPostFact().getInstance();
			SenSMS sensms = bean.sms.convertToSenSMS();
			
			if(bean.sms.getSendStatus() == 10)
				sensms.totalSent = 1;

			if(bean.sms.getSendStatus() == 20 && sensms.retries >=xc.getSendRetries())
				sensms.totalSent = -1;
			
			pr.postProcess(new XAppPostPr(sensms));
			
		}
	}

	/** This method moves acknowledgments to to notifications.
	 * @param sms -> Proxy MT object (sms message)
	 */
	private void moveToNotif(ProxyMt sms)
	{
		// delete from senq
		Senq.delete(sms.getSenqId(),sms.getXconnKey());

		sms.setRetries(0);
		sms.setStatus(0);


		ProxyXconn xc=(ProxyXconn) main.xConns.get(String.valueOf(sms.getXconnKey()));
		// check if failed notifs also be notified
		if((sms.getSendStatus()==10 || (sms.getSendStatus()!=10 && xc.isNotifyFailed())) && sms.isCharge())
		{
			// add to notifq
			Notifq.insert(sms);
			Log.info("ID: "+sms.getSenqId() + " From Ack Moved to notif");
		}
		else
		{
//			add to notifq
			//	 SmsFinal.insert(sms);
			Log.info("ID: "+sms.getSenqId() + " From Ack Moved to final");
		}




	}




	/**
	 * This method retries to send an SMS message.
	 * @param sms
	 * @return -> True/False
	 */
	private boolean doRetry(ProxyMt sms)
	{
		ProxyXconn xc=(ProxyXconn) main.xConns.get(String.valueOf(sms.getXconnKey()));
		if(xc==null)
		{
			Log.error("ID: "+sms.getSenqId() +"XCONN NOT FOUND IN MAP FOR:" +sms.getXconnKey());
			return false;
		}
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

			// increment retries
			sms.setRetries(sms.getRetries()+1);
			Date newStime=new Date(System.currentTimeMillis() + delay);
			Senq.retry(sms.getSenqId(), sms.getXconnKey(), sms.getRetries(), newStime);
			Log.info("ID: "+sms.getSenqId() + " Retry #"+sms.getRetries() + " done. Next retry: " + newStime);
			return true;
		}
	}	
}