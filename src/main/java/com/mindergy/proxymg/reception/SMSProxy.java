/*
 * Created on May 23, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.mindergy.proxymg.reception;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mindergy.proxymg.ProxyMain;
import com.mindergy.proxymg.ProxyMt;
import com.mindergy.proxymg.ProxyService;
import com.mindergy.proxymg.ProxyXconn;
import com.mindergy.proxymg.queues.Chargeq;
import com.mindergy.proxymg.queues.Senq;
import com.mindergy.util.commons.MiscUtil;
import com.mindergy.util.log.Log;

/**
 * This servlet is responsible for handling the reception of the http
 * requests from the main MG application and registering them 
 * in the MySQL database located at the proxyMG server.
 * @author TIMwe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SMSProxy extends HttpServlet
{
    
    /**
	 * Universal serial number for servlet
	 */
	private static final long serialVersionUID = 1L;






	/**
	 * Sring with communication content properties
	 */
	private static final String CONTENT_TYPE = "text/plain; charset=windows-1252";
    

    
    
   
    
    /**
     * Origin parameter input code
     */
    private static final String ORIGIN_PARAM_INPUT = "o";
    /**
     * Destination parameter input code
     */
    private static final String DEST_PARAM_INPUT = "d";
    /**
     * Text parameter input code
     */
    private static final String TEXT_PARAM_INPUT = "t";
    /**
     * ID parameter input code
     */
    private static final String ID_PARAM_INPUT = "i";
    /**
     * TODO (Help)
     * Charge
     */
    private static final String CH_PARAM_INPUT = "ch";
    /**
     * Service id 
     */
    private static final String SID_PARAM_INPUT = "sid";
    /**
     * Xconn parameter input code
     */
    private static final String XC_PARAM_INPUT = "xc";
    /**
     * Operator parameter input code
     */
    private static final String OP_PARAM_INPUT = "op";
    /**
     * Content type parameter input code
     */
    private static final String CTYPE_PARAM_INPUT = "ct";
    /**
     * Additional text parameter input code
     */
    private static final String ADDITIONAL_TEXT_PARAM_INPUT = "adt";
    /**
     * TODO (Help)
     * priority
     */
    private static final String PRI_PARAM_INPUT = "pri";
    /**
     * Externl ID parameter input code
     */
    private static final String EXTID_PARAM_INPUT = "ext";
    /**
     * Reference 1 parameter input code 
     */
    private static final String REF1_PARAM_INPUT = "ref1";
    /**
     * Reference 2 parameter input code
     */
    private static final String REF2_PARAM_INPUT = "ref2";
    /**
     * Reference 3 parameter input code
     */
    private static final String REF3_PARAM_INPUT = "ref3";

    /**
     * Purpose parameter input code
     */
    private static final String PURPOSE_PARAM_INPUT = "pur";

    /**
     * Purpose parameter input code
     */
    private static final String SENDER_PARAM_INPUT = "sd";

    
    /**
     * Persistent set of properties declaration
     */
    java.util.Properties props;

    /**
     * This method initializes a channel for the reception of text SMS
     * @param config -> Servlet configuration
     * @throws ServletException 
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Thread.currentThread().setName("__KannelProxySms");
        this.props = MiscUtil.getProperties("proxymg.properties");
        Log.info("Kannel Proxy SMS In for Text SMS started");
    }
    

    /**
     * This method allows for the reception of the 
     * requests and processes them.
     * @param request -> incoming http header
     * @param response -> http response lines and header
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
        Thread.currentThread().setName("__KannelProxySmsIn");
        response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        try {
            ProxyMain main = ProxyMain.getProxyMain();
        	Log.info(request.getQueryString());
            // Get message parameters
            // Get number of messages
            
            int n = Integer.parseInt(request.getParameter("n"));

            StringBuffer result = new StringBuffer();
            for (int i = 0; i < n; i++)
            {
                // create Proxy Mt object            	
            	ProxyMt sms=new ProxyMt();
            	// filenapath||mgid||origin||dest||message||serviceid||operatorid||xconn||charge||ctype||adtional text||pri||ext id||ref1||ref2||ref3
            	sms.setSenqId(Long.parseLong(request.getParameter(ID_PARAM_INPUT+i)));
                sms.setOrigin(request.getParameter(ORIGIN_PARAM_INPUT+i));
                sms.setDest(request.getParameter(DEST_PARAM_INPUT+i));
              	sms.setMessage(request.getParameter(TEXT_PARAM_INPUT+i));
                sms.setServiceId(Integer.parseInt(request.getParameter(SID_PARAM_INPUT+i)));
                sms.setOpId(Integer.parseInt(request.getParameter(OP_PARAM_INPUT+i)));
                int xconnOriginal=(Integer.parseInt(request.getParameter(XC_PARAM_INPUT+i)));
                sms.setCharge(request.getParameter(CH_PARAM_INPUT+i).equals("1"));
                sms.setCtype(Integer.parseInt(request.getParameter(CTYPE_PARAM_INPUT+i)));
                sms.setAditionalMessage(request.getParameter(ADDITIONAL_TEXT_PARAM_INPUT+i));
                sms.setPriority(Integer.parseInt(request.getParameter(PRI_PARAM_INPUT+i)));
                sms.setExtId(request.getParameter(EXTID_PARAM_INPUT+i));
                sms.setRef1(Long.parseLong(request.getParameter(REF1_PARAM_INPUT+i)));
                sms.setRef2(Long.parseLong(request.getParameter(REF2_PARAM_INPUT+i)));
                sms.setRef3(Long.parseLong(request.getParameter(REF3_PARAM_INPUT+i)));
                
                if (request.getParameter(PURPOSE_PARAM_INPUT+i) != null && request.getParameter(PURPOSE_PARAM_INPUT+i).length() > 0) {
                sms.setPurposeId(Integer.parseInt(request.getParameter(PURPOSE_PARAM_INPUT+i)));
                }
                
                if (request.getParameter(SENDER_PARAM_INPUT+i) != null && request.getParameter(SENDER_PARAM_INPUT+i).length() > 0) {
                	sms.setSender(request.getParameter(SENDER_PARAM_INPUT+i));
                }

                sms.setQtime(new Date());
                sms.setStime(new Date());
                sms.setRetries(0);
                sms.setStatus(0);
                
                // calculate xconn
                ProxyXconn xc=(ProxyXconn) main.xConnsByProxyOf.get(""+xconnOriginal);
                if(xc!=null)
                	sms.setXconnKey(xc.getKey());
                else
                {
                	result.append("-1");
                	result.append("|");
                	Log.error("Xconn not found. Error On Charge Class for senq id: " +sms.getSenqId());
                }
                // get service
                ProxyService serv=(ProxyService) main.services.get(""+sms.getServiceId());
                
                if(serv!=null && serv.getChargeClass()!=null && sms.isCharge() && xc.getChargeThreads()>0)
                {
                	// insert on chargeq
                	Chargeq.insert(sms);
                	Log.info("Chargeq insert: " +sms.getSenqId());
                	result.append("1");
                	result.append("|");
                }
                else
                {
//                	 insert on senq
                	Senq.insert(sms);
                	Log.info("Senq insert: " +sms.getSenqId());
                	result.append("1");
                	result.append("|");
                }
            }
            
            Log.info(result);
             out.print(result.toString());
                
            
            
        } catch (Exception e) {
            out.print(-1);
            Log.error("There was an unknown error!",e);
        }finally{
            out.flush();
            out.close();
        }        
    }

}
