package com.mindergy.proxymg;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import com.mindergy.mg.interfaces.IXConnController;
import com.mindergy.util.log.Log;

/**
 * This class controls and changes the state of an XConn.
 * @author TIMwe
 *
 */
public class XConnController implements IXConnController 
{

	/**
	 * The variable for the Main class of the project.
	 */
	private ProxyMain main;

	/**
	 * This mehod creates the relationship with the Main class.
	 * This is a child of Main. 
	 * @param main
	 */
	public XConnController(ProxyMain main)
	{
		this.main = main;
	}

	/**
	 * This method changes the state of an XCONN and 
	 * establishes a new connection.
	 * @param connKey
	 * @param procId
	 * @param newState
	 */
	public void changeState(int connKey, int procId, boolean newState)
	{
		Log.info("CONNECTION "+ connKey + " CHANGESTATE TO "+newState);
		StringBuffer url=new StringBuffer(main.props.getProperty("xconn.change.state.url"));
		url.append("xc=");
		url.append(connKey);
		url.append("&procid=0");
		url.append("&state=");
		if(newState)
			url.append(1);
		else
			url.append(0);
		String result=this.callGatewayGet(url.toString(),20000);
		Log.info("Call Notif: " +result);
	}




	/**
	 * This method establishes a new http client to handle the connection.
	 * 
	 * @param url -> URL to be called
	 * @param urlTimeout -> Timeout for the response
	 * @return -> String with mini mg response
	 */
	private String callGatewayGet(String url, int urlTimeout){
		try{
			HttpClient client = new HttpClient();
			//establish a connection within 5 seconds
			client.setConnectionTimeout(10000);
			client.setStrictMode(false);
			client.setTimeout(urlTimeout);

			Log.debug("Calling url: " + url );
			GetMethod method = new GetMethod(url);
			method.setFollowRedirects(true);

			method.addRequestHeader("Accept","*/*");
			method.addRequestHeader("Connection","Keep-Alive");

			//execute the method
			String responseBody = null;

			client.executeMethod(method);
			responseBody =  method.getResponseBodyAsStream().toString();

			//clean up the connection resources
			method.releaseConnection();
			//method.recycle();
			Log.debug("Minimg RESPONSE :" + responseBody);
			return responseBody.trim();
		} 
		catch (HttpException he) {
			Log.error("Minimg Http error connecting to '" + url + "'", he);
			return null;
		} 
		catch (IOException ioe){
			Log.error("Minimg Unable to connect to '" + url + "'",ioe);
			return null;
		} 
		catch (Exception ioe2){
			Log.error(ioe2);
			return null;
		}
	}
}