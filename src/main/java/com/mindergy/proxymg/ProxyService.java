package com.mindergy.proxymg;

import com.mindergy.util.commons.IFactory;
import com.mindergy.util.log.Log;

/**
 * This class represents a proxy service with all its properties.
 * @author TIMwe
 *
 */
public class ProxyService {

	//The service unique identifier
	private int serviceId;

	//charge from operator
	private String chargeClass;

	//pre processing class
	private String prePrClass;

	//post processing class
	private String postPrClass;

	private IFactory prePrFact =null;
	private IFactory postFact  =null;
	private IFactory chargeFact=null;
	
	public String getPostPrClass() {
		return postPrClass;
	}
	public void setPostPrClass(String postPrClass) {
		this.postPrClass = postPrClass;
	}
	
	public IFactory getPostFact() {
		return postFact;
	}
	public void setPostFact(IFactory postFact) {
		this.postFact = postFact;
	}

	/**This method gets the charge class
	 * @return the chargeClass
	 */
	public String getChargeClass() {
		return chargeClass;
	}
	/**This method sets the charge class
	 * @param chargeClass the chargeClass to set
	 */
	public void setChargeClass(String chargeClass) {
		this.chargeClass = chargeClass;
	}
	/**This method gets the prePrClass
	 * @return the prePrClass
	 */
	public String getPrePrClass() {
		return prePrClass;
	}
	/**This method sets the prePrClass
	 * @param prePrClass the prePrClass to set
	 */
	public void setPrePrClass(String prePrClass) {
		this.prePrClass = prePrClass;
	}
	/**This method gets the service ID
	 * @return the serviceId
	 */
	public int getServiceId() {
		return serviceId;
	}
	/**This method sets the service ID
	 * @param serviceId the serviceId to set
	 */
	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}


	/**This method gets the charge factory
	 * @return the chargeFact
	 */
	public IFactory getChargeFact() {
		return chargeFact;
	}
	/**This method sets the charge factory
	 * @param chargeFact the chargeFact to set
	 */
	public void setChargeFact(IFactory chargeFact) {
		this.chargeFact = chargeFact;
	}
	/**This method gets the prePrFact
	 * @return the prePrFact
	 */
	public IFactory getPrePrFact() {
		return prePrFact;
	}
	/**This method sets the prePrFact
	 * @param prePrFact the prePrFact to set
	 */
	public void setPrePrFact(IFactory prePrFact) {
		this.prePrFact = prePrFact;
	}

	/**
	 * This method initializes the factories
	 *
	 */
	public void initialize()
	{
		try {
			if(this.prePrClass!=null)
			{
				Class c = Class.forName(this.prePrClass);
				this.prePrFact=(IFactory) c.newInstance();
			}
		} catch (Exception e) {
			Log.info("Class does not exist: "+this.prePrClass);
		}

		try {
			if(this.chargeClass!=null)
			{
				Class c = Class.forName(this.chargeClass);
				this.chargeFact=(IFactory) c.newInstance();
			}
		} catch (Exception e) {
			Log.info("Class does not exist: "+this.chargeClass,e);
		}
		
		try {
			if(this.postPrClass!=null)
			{
				Class c = Class.forName(this.postPrClass);
				this.postFact =(IFactory) c.newInstance();
			}
		} catch (Exception e) {
			Log.info("Class does not exist: "+this.chargeClass,e);
		}
	}
}
