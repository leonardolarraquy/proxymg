package com.mindergy.proxymg;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.collections.FastArrayList;
import org.apache.commons.collections.FastHashMap;

import com.mindergy.util.database.DBConnections;
import com.mindergy.util.log.Log;

/* 
 * $Source$
 * $Revision: 91838 $
 * $Author: leonardo.larraquy $
 * $Date: 2009-11-09 17:23:56 -0300 (Mon, 09 Nov 2009) $
 * $Name$
 * $State$
 * $Log$
 * Revision 1.1  2007/04/13 10:27:31  pps
 * *** empty log message ***
 *
 * Revision 1.4  2006/12/20 08:35:04  pps
 * *** empty log message ***
 *
 * Revision 1.3  2006/08/20 20:15:12  pps
 * *** empty log message ***
 *
 * Revision 1.2  2006/08/19 14:10:59  pps
 * *** empty log message ***
 *
 * Revision 1.1  2006/08/18 20:00:03  pps
 * *** empty log message ***
 *
 * Revision 1.11  2006/06/13 16:46:34  pps
 * *** empty log message ***
 *
 * Revision 1.10  2006/06/03 13:51:01  ics
 * Batch Processor Implementation
 *
 * Revision 1.9  2006/06/01 18:49:28  pps
 * fix bugs in async notif
 *
 * Revision 1.8  2006/05/21 19:23:21  pps
 * complete notification system
 *
 * Revision 1.7  2006/05/19 17:18:08  pps
 * add control of repeated gw ids on SMSReceiver. Block them, if repeated
 *
 * Revision 1.6  2006/04/20 10:04:14  pps
 * *** empty log message ***
 *
 * Revision 1.5  2004/11/29 15:50:13  pps
 * OperatorId support in all rec and send classes
 *
 * Revision 1.4  2004/11/14 17:06:55  pps
 * fix error nullpointerex in wrapper.log in smsreceiver
 *
 * Revision 1.3  2004/07/07 00:34:00  pps
 * implement can_sync auto control for xconn
 *
 * Revision 1.2  2004/04/27 12:40:45  pps
 * add cvs keyrwords
 * set req_qdate to systimestamp
 *
 *
 */

/**
 * This class loads all the active connections from the database 
 * and puts them on the connections hashmap of mg
 * @author TIMwe
 *
 */
public class ServiceLoader extends Thread
{
  /**
 * The father of the class 
 */
private ProxyMain main;
  
  /**
   * This method establishes the relationship between this thread and
   * the main class of the application. 
 * @param main -> Main class of the application.
 */
public ServiceLoader(ProxyMain main)
  {
    this.main = main;
  }
  

  /**
   * This method loads all connections from the database and establishes 
   * them as services of the Main application.
 * @param cacheOnly
 */
public void load(boolean cacheOnly)
  {
   
    FastHashMap conns = new FastHashMap();
    FastArrayList clist = new FastArrayList();
    Connection db=null;
    PreparedStatement s=null;
    ResultSet rs=null;
    try
    {
      Log.debug("Service Loader init");

      db=DBConnections.GetConnection("proxymg");
      s=db.prepareStatement(SQLData.Service.SELN_ALL);
      rs=s.executeQuery();
      ProxyService sr=null;
	  while(rs.next())
      {
        sr=new ProxyService();
        /* service_id, service_charge_cl ,service_prepr_cl,service_postpr_cl from service */
        
         // put on hash map
        sr.setServiceId(rs.getInt(1));
        sr.setChargeClass(rs.getString(2));
        sr.setPrePrClass(rs.getString(3));
        sr.setPostPrClass(rs.getString(4));
        conns.put(String.valueOf(sr.getServiceId()),sr);
        clist.add(sr);
        sr.initialize();
        Log.debug("Service Loader Added Service" + sr.getServiceId());
        
      }
      // now replace the hasmap
      synchronized(main.xConns)
      {
        main.services=conns;
        main.services.setFast(true);
        main.servicesList= clist;
        main.servicesList.setFast(true);
        
        Log.debug("Service Loader Replaced On Main");
      }
      Log.info("Service Loader End");
    }
    catch(Exception e)
    {
      Log.error(e);
    }
    finally
    {
      // Always make sure result sets and statements are closed,
      // and the connection is returned to the pool
//      Always make sure result sets and statements are closed,
        // and the connection is returned to the pool
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { ; }
            rs = null;
          }
        if (s != null) {
          try { s.close(); } catch (SQLException e) { ; }
          s = null;
        }
        if (db != null) {
          try { db.close(); } catch (SQLException e) { ; }
          db = null;
        }
      
    }
  }
  
}