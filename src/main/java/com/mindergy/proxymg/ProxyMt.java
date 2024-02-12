package com.mindergy.proxymg;

import java.util.Date;

import com.mindergy.mg.engine.SenSMS;
import com.mindergy.mg.interfaces.XConnSenSMS;

/**
 * This class represents a ProxyMT object and
 * is responsible for holding a certain amount of
 * parameters. It has getters and setters for them so they can be
 * read and written when necessary.
 * Also contains some methods for the conversion of messages. 
 * @author TIMwe
 *
 */
public class ProxyMt {

	/**
	 * This is the unique identifier for senq.
	 */
	private long senqId;
	/**
	 * This is the send time
	 */
	private Date stime;
	/**
	 * This is the queue time
	 */
	private Date qtime;
	/**
	 * This is the origin of the message
	 */
	private String origin;
	/**
	 * This is the destination of the message
	 */
	private String dest;
	/**
	 * This is the message itself
	 */
	private String message;
	/**
	 * This is an additional message
	 * Used in WAPPUSH to send the text that complements the WEBPUSH url
	 */
	private String aditionalMessage;
	/**
	 * This is the service unique identifier
	 */
	private int serviceId;
	/**
	 * This is the xconn unique identifier (key)
	 */
	private int xconnKey;
	/**
	 * This flag indicates if the sms message is to be charged or not.
	 */
	private boolean charge;
	/**
	 * TODO(Help)
	 */
	private String extId;
	/**
	 * Propety for reference 1 (can be used for anything)
	 */
	private long ref1;
	/**
	 * Propety for reference 2 (can be used for anything)
	 */
	private long ref2;
	
	/**
	 * Propety for reference 3 (can be used for anything)
	 */
	private long ref3;
	/**
	 * This is the number of retries for a message.
	 */
	private int retries;
	/**
	 * This is the priority for the message
	 */
	private int priority;
	/**
	 * This is the general status of the message
	 */
	private int status=0;
	
	/**
	 * This is the send queue status
	 */
	private int sendStatus=0;
	/**
	 * This is the charge status
	 */
	private int chargeStatus=0;
	/**
	 * This is the notification status
	 */
	private int notifStatus=0;
	/**
	 * This is the operator unique identifier
	 */
	private int opId;
	/**
	 * This is the connection type TODO(Help)
	 */
	private int ctype;
	
    private int purposeId;

    private String sender;
	
	
	/**
	 * This method gets the ctype.
	 * @return the ctype
	 */
	public int getCtype() {
		return ctype;
	}
	/**
	 * This method sets the ctype
	 * @param ctype the ctype to set
	 */
	public void setCtype(int ctype) {
		this.ctype = ctype;
	}
	/**
	 * This method gets the operator unique identifier.
	 * @return the opId
	 */
	public int getOpId() {
		return opId;
	}
	/**
	 * This method sets the operator unique identifier.
	 * @param opId the opId to set
	 */
	public void setOpId(int opId) {
		this.opId = opId;
	}
	/**
	 * This method returns or gets the additional message.
	 * @return the aditionalMessage
	 */
	public String getAditionalMessage() {
		return aditionalMessage;
	}
	/**
	 * This method sets the additional messages.
	 * @param aditionalMessage the aditionalMessage to set
	 */
	public void setAditionalMessage(String aditionalMessage) {
		this.aditionalMessage = aditionalMessage;
	}
	/**
	 * This method returns the is charge flag.
	 * @return the charge
	 */
	public boolean isCharge() {
		return charge;
	}
	/** This method sets the charge for the message.
	 * @param charge the charge to set
	 */
	public void setCharge(boolean charge) {
		this.charge = charge;
	}
	/** This method returns the charge status
	 * @return the chargeStatus
	 */
	public int getChargeStatus() {
		return chargeStatus;
	}
	/**This method sets the chargStatus
	 * @param chargeStatus the chargeStatus to set
	 */
	public void setChargeStatus(int chargeStatus) {
		this.chargeStatus = chargeStatus;
	}
	/**This method gets the destination
	 * @return the dest
	 */
	public String getDest() {
		return dest;
	}
	/**This method sets the destination
	 * @param dest the dest to set
	 */
	public void setDest(String dest) {
		this.dest = dest;
	}
	/**This method returns the external identifier.
	 * @return the extId
	 */
	public String getExtId() {
		return extId;
	}
	/**This method  sets the external id
	 * @param extId the extId to set
	 */
	public void setExtId(String extId) {
		this.extId = extId;
	}
	/**This method returns the message
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**This method sets the message
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**This method  returns the notification status
	 * @return the notifStatus
	 */
	public int getNotifStatus() {
		return notifStatus;
	}
	/**This method  sets the notification status
	 * @param notifStatus the notifStatus to set
	 */
	public void setNotifStatus(int notifStatus) {
		this.notifStatus = notifStatus;
	}
	/**This method gets the origin
	 * @return the origin
	 */
	public String getOrigin() {
		return origin;
	}
	/**This method sets the origin
	 * @param origin the origin to set
	 */
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	/**This method  gets the priority
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}
	/**This method sets the priority.
	 * @param priority the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
	/**This method gets the queue time
	 * @return the qtime
	 */
	public Date getQtime() {
		return qtime;
	}
	/**This method sets the queue time
	 * @param qtime the qtime to set
	 */
	public void setQtime(Date qtime) {
		this.qtime = qtime;
	}
	/** This method gets reference 1
	 * @return the ref1
	 */
	public long getRef1() {
		return ref1;
	}
	/**This method sets reference 1
	 * @param ref1 the ref1 to set
	 */
	public void setRef1(long ref1) {
		this.ref1 = ref1;
	}
	/**This method  gets reference 2
	 * @return the ref2
	 */
	public long getRef2() {
		return ref2;
	}
	/**This method  sets reference 2
	 * @param ref2 the ref2 to set
	 */
	public void setRef2(long ref2) {
		this.ref2 = ref2;
	}
	/**This method gets reference 3
	 * @return the ref3
	 */
	public long getRef3() {
		return ref3;
	}
	/**This method sets reference 3
	 * @param ref3 the ref3 to set
	 */
	public void setRef3(long ref3) {
		this.ref3 = ref3;
	}
	/**This method gets the retries
	 * @return the retries
	 */
	public int getRetries() {
		return retries;
	}
	/**This method sets the retries
	 * @param retries the retries to set
	 */
	public void setRetries(int retries) {
		this.retries = retries;
	}
	/**This method gets the send status
	 * @return the sendStatus
	 */
	public int getSendStatus() {
		return sendStatus;
	}
	/**This method sets the send status
	 * @param sendStatus the sendStatus to set
	 */
	public void setSendStatus(int sendStatus) {
		this.sendStatus = sendStatus;
	}
	/**This method gets the send queue ID
	 * @return the senqId
	 */
	public long getSenqId() {
		return senqId;
	}
	/**This method sets the send queue id
	 * @param senqId the senqId to set
	 */
	public void setSenqId(long senqId) {
		this.senqId = senqId;
	}
	/**This method gets the service id
	 * @return the serviceId
	 */
	public int getServiceId() {
		return serviceId;
	}
	/**This method sets the service id
	 * @param serviceId the serviceId to set
	 */
	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}
	/**This method gets the status
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}
	/**This method sets the ststus
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	/**This method gets stime
	 * @return the stime
	 */
	public Date getStime() {
		return stime;
	}
	/**This method sets stime
	 * @param stime the stime to set
	 */
	public void setStime(Date stime) {
		this.stime = stime;
	}
	/**This method gets the xconn key
	 * @return the xconnKey
	 */
	public int getXconnKey() {
		return xconnKey;
	}
	/**This method sets the xconn key
	 * @param xconnKey the xconnKey to set
	 */
	public void setXconnKey(int xconnKey) {
		this.xconnKey = xconnKey;
	}
	
	public int getPurposeId() {
		return purposeId;
	}

	public void setPurposeId(int purposeId) {
		this.purposeId = purposeId;
	}
	
	public String getSender() {
		return sender;
	}
	
	public void setSender(String sender) {
		this.sender = sender;
	}
	
	
	/**
	 * This method creates a new SenSMS object and gets all the 
	 * properties for the object.
	 * @return
	 */
	public SenSMS convertToSenSMS()
	{
		SenSMS sms=new SenSMS();
		sms.charge=this.charge;
		sms.ctype=this.ctype;
		sms.destination=this.dest;
		sms.extId=this.extId;
		sms.message=this.message;
		sms.operatorId=this.opId;
		sms.origin=this.origin;
		sms.priority=this.priority;
		sms.ref1=this.ref1;
		sms.ref2=this.ref2;
		sms.ref3=this.ref3;
		sms.retries=this.retries;
		sms.senqId=(int) this.senqId;
		sms.servId=this.serviceId;
		sms.status=this.sendStatus;
		sms.sTime=this.stime;
		sms.procTime=this.qtime;
		sms.xconnKey=this.xconnKey;
		sms.purpose = this.purposeId;
		sms.sender = this.sender;
		return sms;
	}
	
	/**
	 * This method creates a new XConnSenSMS object and 
	 * populates its properties.
	 * @return
	 */
	public XConnSenSMS convertToXConnSenSMS()
	{
		XConnSenSMS sms=new XConnSenSMS();
		sms.charge=this.charge;
		sms.cType=this.ctype;
		sms.destination=this.dest;
		sms.extId=this.extId;
		
		if(sms.cType == 6){
			byte[] tobytes = org.marre.util.StringUtil.hexStringToBytes(this.message.toString());
			sms.message = tobytes;
		}
		else sms.message=this.message;
		
		sms.additionalTextMessage = this.aditionalMessage;
		sms.operatorId=this.opId;
		sms.origin=this.origin;
		sms.priority=this.priority;
		sms.retries=this.retries;
		sms.senqId=(int) this.senqId;
		sms.serviceId=this.serviceId;
		sms.status=this.status;
		sms.stime=this.stime;
		sms.ptime=this.qtime;
		sms.xconnKey=this.xconnKey;
		sms.mgId=""+this.senqId;
		sms.refId= "" + this.getRef1();
		sms.purposeId = this.purposeId;
		sms.sender = this.sender;
		return sms;
	}
	
	/**This method converts a SenSMS object to a ProxyMT object
	 * with the corresponding properties.
	 * @param sms -> Sms object to convert
	 */
	public void convertFrom(SenSMS sms)
	{
		this.charge=sms.charge;
		this.ctype=sms.ctype;
		this.dest=sms.destination;
		this.extId=sms.extId;
		this.message=sms.message;
		this.opId=sms.operatorId;
		this.origin=sms.origin;
		this.priority=sms.priority;
		this.ref1=sms.ref1;
		this.ref2=sms.ref2;
		this.ref3=sms.ref3;
		this.retries=sms.retries;
		this.senqId=sms.senqId; 
		this.serviceId=sms.servId;
		this.status=sms.status;
		this.stime=sms.sTime;
		this.qtime=sms.procTime;
		this.xconnKey=sms.xconnKey;
		this.purposeId = sms.purpose;
		this.sender = sms.sender;
	}
}
