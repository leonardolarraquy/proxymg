/*
 * Created on May 22, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.mindergy.proxymg.queues;

import java.util.Comparator;

import com.mindergy.proxymg.ProxyMt;

/**
 * This class has the responsibility of comparing the priorities
 *  for the messages
 * @author TIMwe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SenSMSPriorityComparator implements Comparator
{

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	/**
	 * Compare message 1 with message 2 and sees witch one has
	 * the most priority through a series of calculations.
	 */
	public int compare(Object o1, Object o2){
		ProxyMt s1 = (ProxyMt) o1;
		ProxyMt s2 = (ProxyMt) o2;
		long dt1=s1.getStime().getTime();
		long dt2=s2.getStime().getTime();

		if(s1.getPriority()!=s2.getPriority())
			return s1.getPriority()-s2.getPriority();
		else
		{
			long dif=dt1-dt2;
			if(dif<0)
				return -1;
			else if(dif>0)
				return 1;
			else 
				return 0;
		}

	}

}

