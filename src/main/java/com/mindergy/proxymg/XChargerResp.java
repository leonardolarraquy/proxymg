package com.mindergy.proxymg;

/**
 * This class represents a charger response with all its properties
 * and methods.
 * @author TIMwe
 *
 */
public class XChargerResp {

	/**
	 * Integer code representing a charge result.
	 */
	private int chargeResult;
	/**
	 * Proceed with charge flag.
	 */
	private boolean proceed=true;
	/**
	 * Number of retries that overrides the configured ones by default. 
	 */
	private int retriesOverride=0;
	/**
	 * This method gets the charge result.
	 * @return the chargeResult
	 */
	public int getChargeResult() {
		return chargeResult;
	}
	/**
	 *This method sets the charge result
	 * @param chargeResult the chargeResult to set
	 */
	public void setChargeResult(int chargeResult) {
		this.chargeResult = chargeResult;
	}
	/** This method gets the proceed flag
	 * @return the proceed
	 */
	public boolean isProceed() {
		return proceed;
	}
	/** This method sets the proceed flag
	 * @param proceed the proceed to set
	 */
	public void setProceed(boolean proceed) {
		this.proceed = proceed;
	}
	/** Tis method gets the retries override number
	 * @return the retriesOverride
	 */
	public int getRetriesOverride() {
		return retriesOverride;
	}
	/** This method sets the retries override number
	 * @param retriesOverride the retriesOverride to set
	 */
	public void setRetriesOverride(int retriesOverride) {
		this.retriesOverride = retriesOverride;
	}
		
}
