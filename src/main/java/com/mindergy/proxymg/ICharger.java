package com.mindergy.proxymg;

/**
 * This class represents an interface for the Charger which basically
 * has the responsibility of sending SMS messages.
 * @author TIMwe
 *
 */
public interface ICharger {

	public XChargerResp chargeSms(ProxyMt sms);
}
