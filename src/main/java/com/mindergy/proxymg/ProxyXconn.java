package com.mindergy.proxymg;

import com.mindergy.mg.interfaces.IXConn;

/**
 * This class represents the proxy xconn connection with all
 * its properties.
 * @author TIMwe
 *
 */
public class ProxyXconn {

	/**
	 * Proxy XCONN key
	 */
	private int key;
	/**
	 * The "father" of this proxy
	 */
	private int proxyOf;
	/**
	 * Flag for notification control
	 */
	private boolean notifyFailed=true;
	/**
	 * Number of retries for this XCONN
	 */
	private int sendRetries;
	/**
	 * Number of charge retries for this XCONN
	 */
	private int chargeRetries;
	/**
	 * TODO (Help)
	 * notification retries
	 */
	private int notifRetries;
	/**
	 * Start hour to charge  TODO (Help)
	 * Defines lower limit of the allowed time interval for sending mesages with ~
	 * priority >=8. Messages with priority <8 are no affected 
	 */
	private int sendStartHour;
	/**
	 * End hour to send information
	 */
	private int sendEndHour;
	/**
	 * Start hour to charge  TODO (Help)
	 * Defines lower limit of the allowed time interval for charging mesages with ~
	 * priority >=8. Messages with priority <8 are no affected 
	 */
	private int chargeStartHour;
	/**

	 */
	private int chargeEndHour;
	/**
	 * TODO (Help)
	 * knows how to send to the operator
	 */
	private String xconnCl;
	/**
	 * Number of threads used to send messages
	 */
	private int sendThreads;
	/**
	 * Number of threads to charge messages
	 */
	private int chargeThreads;
	/**
	 * Number of threads for notifications
	 */
	private int notifThreads;
	/**
	 * Number for assyncronous acknoledgements
	 */
	private int asyncAck;
	/**
	 * ProxyXCONN name
	 */
	private String name;
	/**
	 * ProxyXCONN properties file
	 */
	private String propsFile;
	
	/**
	 * Number minutes between retries
	 */
	private int retryMinutes;
	
	/**
	 * Operator which this xconn belongs
	 */
	private int opId;
	
	  // xconn implementation class
	private IXConn xconnObj = null;
	
	
	/**
	 * Use Send Q 
	 */
	private int useSenqQ;
	
	public int getUseSenqQ() {
		return useSenqQ;
	}
	public void setUseSenqQ(int useSenqQ) {
		this.useSenqQ = useSenqQ;
	}
	
	/**This method gets the assincronous acknoledgements
	 * @return the asyncAck
	 */
	public int getAsyncAck() {
		return asyncAck;
	}
	/**This method sets the assincronous acknoledgments
	 * @param asyncAck the asyncAck to set
	 */
	public void setAsyncAck(int asyncAck) {
		this.asyncAck = asyncAck;
	}
	/** This method gets the Proxy XCONN name
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**This method sets the ProxyXCONN name
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**This method gets the charge end hour
	 * @return the chargeEndHour
	 */
	public int getChargeEndHour() {
		return chargeEndHour;
	}
	/**This method sets the charge end hour
	 * @param chargeEndHour the chargeEndHour to set
	 */
	public void setChargeEndHour(int chargeEndHour) {
		this.chargeEndHour = chargeEndHour;
	}
	/**This method gets the charge retries
	 * @return the chargeRetries
	 */
	public int getChargeRetries() {
		return chargeRetries;
	}
	/**This method sets the charge retries
	 * @param chargeRetries the chargeRetries to set
	 */
	public void setChargeRetries(int chargeRetries) {
		this.chargeRetries = chargeRetries;
	}
	/**This method gets the charge start hour
	 * @return the chargeStartHour
	 */
	public int getChargeStartHour() {
		return chargeStartHour;
	}
	/**This method sets the charge start hour
	 * @param chargeStartHour the chargeStartHour to set
	 */
	public void setChargeStartHour(int chargeStartHour) {
		this.chargeStartHour = chargeStartHour;
	}
	/**This method gets the charge threads
	 * @return the chargeThreads
	 */
	public int getChargeThreads() {
		return chargeThreads;
	}
	/**This method sets the charge threads
	 * @param chargeThreads the chargeThreads to set
	 */
	public void setChargeThreads(int chargeThreads) {
		this.chargeThreads = chargeThreads;
	}
	/** This method gets the ProxyXCONN key
	 * @return the key
	 */
	public int getKey() {
		return key;
	}
	/**This method sets the ProxyXCONN key 
	 * @param key the key to set
	 */
	public void setKey(int key) {
		this.key = key;
	}
	/**This method  gets the notification retries
	 * @return the notifRetries
	 */
	public int getNotifRetries() {
		return notifRetries;
	}
	/**This method sets the notification retries
	 * @param notifRetries the notifRetries to set
	 */
	public void setNotifRetries(int notifRetries) {
		this.notifRetries = notifRetries;
	}
	/**This method gets the notification threads
	 * @return the notifThreads
	 */
	public int getNotifThreads() {
		return notifThreads;
	}
	/**This method sets the notification threads
	 * @param notifThreads the notifThreads to set
	 */
	public void setNotifThreads(int notifThreads) {
		this.notifThreads = notifThreads;
	}
	/**This method gets the notifications failed
	 * @return the notifyFailed
	 */
	public boolean isNotifyFailed() {
		return notifyFailed;
	}
	/**This method sets the failed notifications
	 * @param notifyFailed the notifyFailed to set
	 */
	public void setNotifyFailed(boolean notifyFailed) {
		this.notifyFailed = notifyFailed;
	}
	/**This method gets the "father" proxy
	 * @return the proxyOf
	 */
	public int getProxyOf() {
		return proxyOf;
	}
	/**This method sets the "father" proxy
	 * @param proxyOf the proxyOf to set
	 */
	public void setProxyOf(int proxyOf) {
		this.proxyOf = proxyOf;
	}
	/**This method gets the send end hour
	 * @return the sendEndHour
	 */
	public int getSendEndHour() {
		return sendEndHour;
	}
	/**This method sets the send end hour
	 * @param sendEndHour the sendEndHour to set
	 */
	public void setSendEndHour(int sendEndHour) {
		this.sendEndHour = sendEndHour;
	}
	/**This method gets the send retries
	 * @return the sendRetries
	 */
	public int getSendRetries() {
		return sendRetries;
	}
	/**This method sets the send retries
	 * @param sendRetries the sendRetries to set
	 */
	public void setSendRetries(int sendRetries) {
		this.sendRetries = sendRetries;
	}
	/**This method gets the send start hour
	 * @return the sendStartHour
	 */
	public int getSendStartHour() {
		return sendStartHour;
	}
	/**This method sets the send start hour
	 * @param sendStartHour the sendStartHour to set
	 */
	public void setSendStartHour(int sendStartHour) {
		this.sendStartHour = sendStartHour;
	}
	/**This method gets the number of send threads
	 * @return the sendThreads
	 */
	public int getSendThreads() {
		return sendThreads;
	}
	/**This method sets the number of send threads
	 * @param sendThreads the sendThreads to set
	 */
	public void setSendThreads(int sendThreads) {
		this.sendThreads = sendThreads;
	}
	/** This method gets the xconnCl
	 * @return the xconnCl
	 */
	public String getXconnCl() {
		return xconnCl;
	}
	/**This method sets de xconnCl
	 * @param xconnCl the xconnCl to set
	 */
	public void setXconnCl(String xconnCl) {
		this.xconnCl = xconnCl;
	}
	
	/** This method replaces an xconnkey in a sql string.
	 * @param source -> Source Sql string
	 * @param xconnKey -> New xconn key to replace in the sql string 
	 * @return -> String with xconn key replaced
	 */
	public static String getXconnReplacedSql(String source,int xconnKey)
	{
		return source.replaceAll("###", String.valueOf(xconnKey));
	}
	
	
	
	/**Ths method gets the properties file.
	 * @return the propsFile
	 */
	public String getPropsFile() {
		return propsFile;
	}
	/**This method sets the properties file
	 * @param propsFile the propsFile to set
	 */
	public void setPropsFile(String propsFile) {
		this.propsFile = propsFile;
	}
	
	/**
	 * This method gets the xconn Object
	 * @return the xconnObj
	 */
	public IXConn getXconnObj() {
		return xconnObj;
	}
	/**This method sets the xconn Object
	 * @param xconnObj the xconnObj to set
	 */
	public void setXconnObj(IXConn xconnObj) {
		this.xconnObj = xconnObj;
	}
	
	public int getRetryMinutes() {
		return retryMinutes;
	}
	public void setRetryMinutes(int retryMinutes) {
		this.retryMinutes = retryMinutes;
	}
	
	public int getOpId() {
		return opId;
	}
	public void setOpId(int opId) {
		this.opId = opId;
	}
}