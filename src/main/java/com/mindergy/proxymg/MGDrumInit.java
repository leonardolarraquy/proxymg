package com.mindergy.proxymg;
import com.mindergy.util.log.Log;
import com.mindergy.drum.IDeamon;
/* 
 * $Source$
 * $Revision: 4169 $
 * $Author: carlos.silva $
 * $Date: 2007-11-16 12:09:35 -0300 (Fri, 16 Nov 2007) $
 * $Name$
 * $State$
 * $Log$
 * Revision 1.1  2007/04/13 10:27:31  pps
 * *** empty log message ***
 *
 * Revision 1.1  2006/08/18 20:00:03  pps
 * *** empty log message ***
 *
 * Revision 1.2  2004/04/27 12:40:45  pps
 * add cvs keyrwords
 * set req_qdate to systimestamp
 *
 *
 */

/**
 * 
 * @author TIMwe
 *
 */
public class MGDrumInit  extends Thread implements IDeamon
{

  /**
 * This establishes the relationship with the main application.
 */
private ProxyMain main=null;
  
  /**
 * Simple constuctor
 */
public MGDrumInit()
  {
  }

  /* (non-Javadoc)
 * @see java.lang.Thread#run()
 * This method starts the proxy mg from the drum container.
 */
public void run()
  {
    Thread.currentThread().setName("__MG");
    // create mgmain and start it
    main = new ProxyMain();
    main.startProxyMg();
    Log.info("Proxy MG Started from DRUM Container");
  }

  /**
 * This method stops the MG.
 */
public void stopRunning()
  { 
    Log.info("Stopping MG");
    main.endMg();
  }

  /**
 * Empty event handler
 */
public void handleEvent()
  {
  }
}