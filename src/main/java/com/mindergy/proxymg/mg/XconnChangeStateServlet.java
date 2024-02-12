package com.mindergy.proxymg.mg;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mindergy.mg.engine.XConnController;
import com.mindergy.util.log.Log;

/**
 * This class is responsible for changing states of servlets
 * @author TIMwe
 *
 */
public class XconnChangeStateServlet extends HttpServlet {


	/**
	 * Universal serial number for the servlet
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * This method is the constructor of the object.
	 */
	public XconnChangeStateServlet() {
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
	 * @param request -> the request send by the client to the server
	 * @param response -> the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		Thread.currentThread().setName("__PROXYMG_CHANGE_STATE");
		// parameters
		// id (mgid)
		// status (1|0)
		
		String sxcon=request.getParameter("xc");
		String procid=request.getParameter("procid");
		String state=request.getParameter("state");
		
		 PrintWriter out = response.getWriter();
		    
		    try
		    {
		   	        
		            XConnController xcc= new XConnController(null);
		            xcc.changeState(Integer.parseInt(sxcon),
		            		Integer.parseInt(procid),state.equals("1"));
		            Log.info("PROXYMG PROXY CHANGE STATE xc:"+sxcon + " to: "+state);
		            
		    }
		    catch(Exception e)
		    {
		      Log.error("PROXYMG PROXY CHANGE STATE ERROR",e);
		      out.print("Error!");
		      response.sendError(500);
		    }
		    out.flush();
		    out.close();
	}

	/**
	 * This method initializes the servlet. <br>
	 *
	 * @throws ServletException if an error occure
	 */
	public void init(ServletConfig config) throws ServletException {
		 super.init(config);
		 Log.info("XConn Minimg Notif started");
	}

}
