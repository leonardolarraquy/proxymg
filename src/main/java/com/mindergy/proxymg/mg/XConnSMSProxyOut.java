/*
 * Created on Oct 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.mindergy.proxymg.mg;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

import com.mindergy.mg.interfaces.IBatchXConn;
import com.mindergy.mg.interfaces.ISMSReceiver;
import com.mindergy.mg.interfaces.IXConnController;
import com.mindergy.mg.interfaces.XConn;
import com.mindergy.mg.interfaces.XConnSenSMS;
import com.mindergy.mg.interfaces.XConnSenSMSResp;
import com.mindergy.util.commons.contentformat.Constants;
import com.mindergy.util.log.Log;

/**
 * This class handles messages to send in batch.
 * It appears to be a always up connection.
 */
public class XConnSMSProxyOut implements IBatchXConn {

	protected static volatile MultiThreadedHttpConnectionManager connectionManager=null;

	XConn conDef;
	String baseUrl = null;
	int urlTimeout = 30000;
	/**
	 * Simple constructor
	 */
	public XConnSMSProxyOut()
	{
	}

	/**
	 * This method starts connection for batch sending messages.
	 * @param p0 -> SMSReceiver
	 * @param p1 -> XConnController
	 * @param p2 -> XConn
	 * @return -> True/False
	 */
	public boolean start(ISMSReceiver p0, IXConnController p1, XConn p2)
	{ 
		this.conDef = p2;
		this.getConfig();
		return true;
	}



	/**
	 * This method stops the connection.
	 * @return -> True/False
	 */
	public boolean stop()
	{
		return true;
	}

	/**
	 * This method gets the configuration for the connection.
	 */
	private void getConfig()
	{
		try
		{

			Properties systemProps = com.mindergy.util.commons.MiscUtil.getProperties(conDef.propertiesFile);
			this.baseUrl=systemProps.getProperty("url");
			this.urlTimeout = Integer.parseInt(systemProps.getProperty("http.timeout"));
		}
		catch(Exception e)
		{
			Log.fatal(e);
		}
	}

	/**
	 * This method sends one SMS message
	 * @param sms -> Sms Message
	 * @return -> Status of the sent message
	 */
	public XConnSenSMSResp sendSMS(XConnSenSMS sms)
	{
		XConnSenSMSResp resp = null;
		try
		{
			resp = this.sendMessage(sms);
		}catch (Exception e)
		{
			resp = new XConnSenSMSResp();
			resp.sendStatus = false;
			resp.statusResult = "-1";
			Log.error(e);
		}
		return resp;
	}

	/**
	 * This method sends an array of SMS mesages
	 * @param sms -> Messages
	 * @return -> Array of resp
	 */
	public XConnSenSMSResp sendMessage(XConnSenSMS sms)
	{
		XConnSenSMSResp[] resp = new XConnSenSMSResp[1];
		XConnSenSMS[] toSend = new XConnSenSMS[1];
		toSend[0] = sms;
		resp = this.sendSMS(toSend);
		return resp[0];

	}
	/**
	 * Sets the batch parameters and creates an object namevaluepair 
	 * TODO(Help)
	 * @param sms
	 * @param index
	 * @return
	 */
	public NameValuePair[] getTextMessageParams(XConnSenSMS sms, int index){
		NameValuePair[] params = new NameValuePair[15];
		params[0] = new NameValuePair("o" + index, sms.origin);
		params[1] = new NameValuePair("d" + index, sms.destination);
		if(sms.message==null )
			sms.message="";
		if(sms.cType==Constants.MGCTypes.CTYPE_TEXT || sms.cType==Constants.MGCTypes.CTYPE_TEXT_WITH_SCKL)
			params[2] = new NameValuePair("t" + index, (String) sms.message);
		else
			params[2] = new NameValuePair("t" + index, org.marre.util.StringUtil.bytesToHexString((byte[]) sms.message));
		params[3] = new NameValuePair("i" + index, sms.mgId);
		params[4] = new NameValuePair("sid" + index, ""+sms.serviceId);
		params[5] = new NameValuePair("xc" + index, ""+conDef.key);
		if(sms.charge)
			params[6] = new NameValuePair("ch" + index, "1");
		else
			params[6] = new NameValuePair("ch" + index, "0");
		params[7] = new NameValuePair("op" + index, ""+sms.operatorId);
		params[8] = new NameValuePair("ct" + index, ""+sms.cType);

		params[9] = new NameValuePair("adt" + index, ""+sms.additionalTextMessage);
		params[10] = new NameValuePair("pri" + index, ""+sms.priority);
		params[11] = new NameValuePair("ext" + index, ""+sms.extId);
		params[12] = new NameValuePair("ref1" + index, "0");
		params[13] = new NameValuePair("ref2" + index, "0");
		params[14] = new NameValuePair("ref3" + index, "0");

		return params;
	}



	/**
	 * This methods sends messages
	 * @param sms -> Messages
	 * @return -> Last status returned when sending the message
	 */
	public XConnSenSMSResp[] sendSMS(XConnSenSMS[] sms)
	{
		XConnSenSMSResp[] resp = new XConnSenSMSResp[sms.length];

		String result = null;

		NameValuePair[] msgparams = null;
		NameValuePair[] params = new NameValuePair[1+15*sms.length];
		params[0] = new NameValuePair("n",""+sms.length);
		for (int i = 0; i < sms.length; i++)
		{
			resp[i] = new XConnSenSMSResp();
			// Force ask conf
			sms[i].askConf = true;
			// Remove post pr since all post pr processing will actually be done on notif processing
			sms[i].callPostPr = false;
			switch (sms[i].cType){

			case Constants.MGCTypes.CTYPE_TEXT:{

				msgparams = this.getTextMessageParams(sms[i],i);

				break;
			}
			case Constants.MGCTypes.CTYPE_TEXT_WITH_SCKL:{

				msgparams = this.getTextMessageParams(sms[i],i);


				break;
			}
			default:{ // Handle as text

				msgparams = this.getTextMessageParams(sms[i],i);
				break;
			}
			}
			System.arraycopy(msgparams,0,params,(i*15)+1,msgparams.length);
		}

		try{
			String postparams = "";
			for (int i = 0; i < params.length; i++) {
				postparams += params[i].getName() + "=" + params[i].getValue()+"&";
			}
			Log.info("Params:" + postparams);
			result = callGatewayPost(baseUrl,params,this.urlTimeout);
			String[] status = new String[sms.length];
			if(result!=null && result.toLowerCase().indexOf("error")<0)
			{
				status = result.split("\\|");
			}

			for (int i = 0; i < sms.length; i++)
			{

				if(result==null || result.toLowerCase().indexOf("error")>=0 || status[i].equals("0")){
					Log.info("Kannel Queue id: " + sms[i].mgId + ":Send ERROR!");
					resp[i].sendStatus=false;
					resp[i].statusResult = "2";
					resp[i].totalSent = 0;
					resp[i].deliConfId=sms[i].mgId;

				}else{
					// If reached here, it is ok<a
					Log.info("Kannel Queue id: " + sms[i].mgId + ":Message sent");
					resp[i].statusResult = result.substring(0,result.length()-1);
					resp[i].sendStatus=true;
					resp[i].totalSent=1;
					resp[i].deliConfId=sms[i].mgId;
				}
			}
		}catch(Exception ex){
			Log.error("Error sending to proxy ", ex);
			for (int i = 0; i < sms.length; i++){
				resp[i].sendStatus=false;
				resp[i].statusResult = "2";
				resp[i].totalSent = 0;
				resp[i].deliConfId=sms[i].mgId;
			}

		}

		// If it reached here message was sent ok. Save last status returned.

		return resp;
	}


	/**
	 * This method  makes the actual call to the gateway. 
	 * This can be called multiple times in the same request.
	 * @param url -> Gatewauy URL
	 * @param params -> Gateway parameters
	 * @param urlTimeout -> Timeout
	 * @return -> Returns the httprequest response
	 * 
	 */
	private String callGatewayPost(String url,NameValuePair[] params,  int urlTimeout){
		try{
//			HttpClient client = new HttpClient(getConnectionManager());
			HttpClient client = new HttpClient();
			
			HttpClientParams parameters = new HttpClientParams();
			parameters.setSoTimeout(urlTimeout);
			
			client.setParams(parameters);
			
			//   Log.info("Calling url: " + url );
			Log.debug("Params:" + params.length);
			/*     for (int i = 0; i < params.length; i++)
        {
            Log.info(params[i].getName() + ":" + params[i].getValue());
        }*/

			PostMethod method = new PostMethod(url);
			method.setFollowRedirects(true); 
			method.addRequestHeader("Accept","*/*");
			method.addRequestHeader("Connection","Keep-Alive");
			method.addParameters(params);

			//execute the method
			String responseBody = null;

			client.executeMethod(method);
			responseBody =  method.getResponseBodyAsString();

			//clean up the connection resources
			method.releaseConnection();
			//method.recycle();
			Log.info("Kannel RESPONSE :" + responseBody);
			return responseBody.trim();
		} 
		catch (HttpException he) {
			Log.error("Kannel Http error connecting to '" + url + "'", he);
			return null;
		} catch (IOException ioe){
			Log.error("Kannel Unable to connect to '" + url + "'",ioe);
			return null;
		}
	}

	protected synchronized MultiThreadedHttpConnectionManager getConnectionManager()
	{
		if(connectionManager==null)	{
			connectionManager=new MultiThreadedHttpConnectionManager();
			HttpConnectionManagerParams params=new HttpConnectionManagerParams();
			params.setConnectionTimeout(urlTimeout);
			params.setDefaultMaxConnectionsPerHost(500);
			params.setSoTimeout(urlTimeout);
			// disable can save 15-30ms per request, but may cause io exceptions
			params.setStaleCheckingEnabled(true);
			connectionManager.setParams(params);
		}
		return connectionManager;
	}

}