package com.mindergy.proxymg.reception;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mindergy.proxymg.ProxyMain;
import com.mindergy.proxymg.ProxyXconn;
import com.mindergy.proxymg.SQLData;
import com.mindergy.util.database.DBConnections;
import com.mindergy.util.log.Log;

/**
 * This servlet is responsible for removing queued messages in 
 * in the MySQL database located at the proxyMG server.
 * @author TIMwe
 *
 */
public class DeletionServlet extends HttpServlet {

    /**
	 * Universal serial number for servlet
	 */
	private static final long serialVersionUID = 2L;

	private static final String CONTENT_TYPE = "text/plain; charset=windows-1252";

	/**
	 * This method allows for the reception of the 
	 * requests and processes them.
	 * @param request -> incoming http header
	 * @param response -> http response lines and header
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * This method allows for the sending of data over the
	 * connection without changing the URL.
	 * It also processes the request.
	 * @param request -> incoming http header
	 * @param response -> http response lines and header
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * This is the method responsible for processing the message from
	 * the get and post methods.
	 * @param request -> incoming http header
	 * @param response -> http response lines and header
	 * @throws ServletException
	 * @throws IOException
	 */
	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Thread.currentThread().setName("__DeletionServlet");
		response.setContentType(CONTENT_TYPE);
		PrintWriter out = response.getWriter();
		try {
			ProxyMain main = ProxyMain.getProxyMain();

			String msisdn       = request.getParameter("msisdn");
			Integer op_id       = new Integer(request.getParameter("op_id"));
			String suboption_id = request.getParameter("suboption_id");

			Log.info("MSISDN: " + msisdn + " OP_ID: " + op_id + " SUBOPTION_ID: " + suboption_id);

			Iterator it = main.xConnsList.iterator();
			while(it.hasNext()){
				ProxyXconn xconn = (ProxyXconn) it.next();
				if(xconn.getOpId() != op_id.intValue())
					continue;

				this.deleteMessages(msisdn, suboption_id, xconn);
			}

			Log.info("OK");
			out.print("OK");

		} 
		catch (Exception e) {
			out.print(-1);
			Log.error("There was an unknown error!",e);
		}
		finally{
			out.flush();
			out.close();
		}        
	}

	private void deleteMessages(String msisdn, String suboptionId, ProxyXconn xconn){
		Connection db=null;
		PreparedStatement s=null;
		try	{
			Log.info("Deleting messages");

			String sql = null;
			if(suboptionId == null){
				if(xconn.getChargeThreads() > 0)
					sql = SQLData.Chargeq.DELETE_BY_MSISDN;
				else sql = SQLData.Senq.DELETE_BY_MSISDN;
			}
			else{
				if(xconn.getChargeThreads() > 0)
					sql = SQLData.Chargeq.DELETE_BY_MSISDN_SUBOPTION;
				else sql = SQLData.Senq.DELETE_BY_MSISDN_SUBOPTION;
			}

			db=DBConnections.GetConnection("proxymg");
			s = db.prepareStatement(ProxyXconn.getXconnReplacedSql(sql,xconn.getKey()));
			s.setString(1, msisdn);

			if(suboptionId != null)
				s.setInt(2, new Integer(suboptionId).intValue());

			int cant = s.executeUpdate();

			Log.info("Messages removed: " + cant);
		}
		catch(Exception e){
			Log.error(e);
		}
		finally{
			// Always make sure result sets and statements are closed,
			// and the connection is returned to the pool
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