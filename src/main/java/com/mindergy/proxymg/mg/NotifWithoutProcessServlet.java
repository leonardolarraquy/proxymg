package com.mindergy.proxymg.mg;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mindergy.mg.interfaces.ISMSReceiver;
import com.mindergy.util.log.Log;

/**
 * This class (servlet) is responsible for getting the notifications
 * sent by the proxyMG's but don't do anything with it.
 * @author TIMwe
 *
 */
public class NotifWithoutProcessServlet extends HttpServlet {

	/**
	 * Unique universal serial number. 
	 */
	private static final long serialVersionUID = 1L;
	private ISMSReceiver smsReceiver=null;
	/**
	 * Constructor of the object with reference to the superclass.
	 */
	public NotifWithoutProcessServlet() {
		super();
	}

	/**
	 * This method handles the destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		Thread.currentThread().setName("__PROXYMG_NOTIF");
		// parameters
		// id (mgid)
		// status (10|20, etc)
		
		
		 PrintWriter out = response.getWriter();
		    String mgId = request.getParameter("id");
		    String status =request.getParameter("status");
		    if (status == null ||status.length()==0){
		        Log.error("Received unknown status");
		        out.print("NOK");
		        return;
		    }
		    
		    try
		    {
		        if(smsReceiver==null)
		        {
		          out.print("Error!");
		          response.sendError(501);
		        }
		        else
		        {
		            Log.info("Successfully processed confirmation for mg id " + mgId + " status " + status);
		        }
		        
		            
		    }
		    catch(Exception e)
		    {
		      Log.error("ProxyMg Notif ERROR Processing message " + mgId + " status " + status,e);
		      out.print("Error!");
		      response.sendError(500);
		    }
		    out.flush();
		    out.close();
	}

	/**
	 *This method is the initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occure
	 */
	public void init(ServletConfig config) throws ServletException {
		 super.init(config);
		    this.getSMSReceiver();
		    Log.info("XConn ProxyMg Notif started");
	}

	/**
	   * This method gets the default receiver from servlet context
	   */
	  private  void getSMSReceiver()
	  {
	    this.smsReceiver = (ISMSReceiver) this.getServletContext().getAttribute("SMSReceiver");
	  }
}
