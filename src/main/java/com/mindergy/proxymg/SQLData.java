package com.mindergy.proxymg;

/**
 * This class handles all the sql strings used by the proxy mg 
 * application to interact with the database.
 * @author TIMwe
 *
 */
public class SQLData {

	/**
	 * This class handles all sql necessary to communicate with the xconn
	 * table in the database.
	 * @author TIMwe
	 *
	 */
	public static class Xconn
	{
		/**
		 * This sql string gets all the active xconns from
		 * the database.
		 */
		public static String SELN_ALL=
			" select "+
			" xconn_key ,"+          
			" xconn_charge_threads,"+    
			" xconn_send_threads      ,"+
			" xconn_notif_threads,"+     
			" xconn_send_class,"+        
			" xconn_charge_start_hour,"+ 
			" xconn_charge_end_hour,"+   
			" xconn_send_start_hour,"+   
			" xconn_send_end_hour,"+     
			" xconn_proxyof,"+           
			" xconn_notif_failed,"+      
			" xconn_charge_retries,"+    
			" xconn_send_retries,"+      
			" xconn_notif_retries,"+     
			" xconn_async_ack,"+
			" xconn_name, " +
			" xconn_props," +
			" xconn_retry_minutes," +
			" xconn_op_id," +
			" xconn_send_queue "+  
			" from xconn where xconn_status =1";
	}
	
	/**
	 * This handles all the sql used to communicate with the service table in
	 * the database.
	 * @author TIMwe
	 *
	 */
	public static class Service
	{
		/**
		 * This sql string selects all the active parameters of all 
		 * services in the database. 
		 */
		public static String SELN_ALL=
			" select service_id, service_charge_cl ,service_prepr_cl, service_postpr_cl from service" +
			" where service_status=1";
	}
	
	/**
	 * This class handles all the sql that interacts with the senq table.
	 * @author TIMwe
	 *
	 */
	public static class Senq
	{
		/**
		 * Selects all the requests from the database considering
		 * their status and with date/time < current date/time, ordered
		 * by priority and limited to a certain number.
		 */
		public static String SELN_MON=
			" select senq_id           ,origin , dest              ,message           ,"+
			" aditional_message , xconn_key  ,service_id        , charge            , ext_id            ,"+
			" retries           , priority          ,qtime             ,stime             ,status            ,"+
			"op_id             , ctype             , ref1              ,ref2              ,ref3, charge_status," +
			"purpose_id, sender "+
			" from senq### where status =? and stime < now() order by retries,priority limit ? ";
		
		/**
		 * This sql string updates the status of a certain request by id and 
		 * previous status.
		 */
		public static String UPD_MON_STATUS=
			" update senq### set status =? where senq_id=? ";
		
		/**
		 * This sql string deletes a request from the database.
		 */
		public static String DEL_1=
			"delete from senq### where senq_id = ?";
		
		/**
		 * This sql string inserts a request in the database.
		 */
		public static String INS_1=
			" insert into senq### "+
			" (senq_id, origin , dest, message, aditional_message, xconn_key,service_id, charge, ext_id, retries, priority, qtime, stime, status, op_id, ctype, ref1, ref2, ref3, charge_status, purpose_id, sender)"+
			" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		/**
		 * This sql string updates the number of retries for the 
		 * selected request.
		 */
		public static String UPD_RETRY=
			" update senq### set status =?, retries=?, stime=? where senq_id=?";
		

		/**
		 * This sql deletes scheduled messages for an specified MSISDN
		 */
		public static String DELETE_BY_MSISDN=
			" delete from  senq### where dest = ?";
		
		/**
		 * This sql deletes scheduled messages for an specified MSISDN, suboption_id
		 */
		public static String DELETE_BY_MSISDN_SUBOPTION =
			" delete from  senq### where dest = ? and ref2 = ? ";
		
		/**
		 * This SQL recovers all messages in invalid status
		 */
		public static String RECOVER_MTS =
			" update senq### set status = 0 where status = 90 ";

		/**
		 * This SQL removes all messages in invalid status
		 */
		public static String REMOVE_MTS =
			" delete from senq### where status = 90 ";

	}
	
	/**
	 * This class handles the sql for the notification queue table.
	 * @author TIMwe
	 *
	 */
	public static class Notifq
	{
		/**
		 * This sql string gets all the notifications with a certain status
		 * and limits the number of retrieved records.
		 */
		public static String SELN_MON=
			" select senq_id           ,origin , dest              ,message           ,"+
			" aditional_message , xconn_key  ,service_id        , charge            , ext_id            ,"+
			" retries           , priority          ,qtime             ,stime             ,status            ,"+
			"op_id             , ctype             , ref1              ,ref2              ,ref3, charge_status, send_status "+
			" from notifq where xconn_key = ? and status =? and stime < now() limit ? ";
		
		/**
		 * This sql query updates the notification queue status for a
		 * certein id an status.
		 */
		public static String UPD_MON_STATUS=
			" update notifq set status =? where senq_id=? ";
		
		/**
		 * This sql query deletes a record from the notify queue
		 * based on its senq_id.
		 */
		public static String DEL_1=
			"delete from notifq where senq_id = ?";
		
		/**
		 * This sql query inserts a notification in the queue.
		 */
		public static String INS_1=
			" insert into notifq "+
			" (senq_id           ,origin , dest              ,message           ,"+
			" aditional_message , xconn_key  ,service_id        , charge            , ext_id            ,"+
			" retries           , priority          ,qtime             ,stime             ,status            ,"+
			"op_id             , ctype             , ref1              ,ref2              ,ref3, charge_status, send_status )"+
			" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		/**
		 * This sql string updates the notification queue status for~
		 * a notification.
		 */
		public static String UPD_RETRY=
			" update notifq set status =?, retries=?, stime = ? where senq_id=?";
	}
	
	
	/**
	 * This class handles all the sql for the charge queue table.
	 * @author TIMwe
	 *
	 */
	public static class Chargeq
	{
		/**
		 * This sql string gets all the messages from the charge queue with a certain
		 * status and that date/time is < now, ordered by priority
		 * and limited in number of messages.
		 */
		public static String SELN_MON=
		" select senq_id           ,origin , dest              ,message           ,"+
		" aditional_message , xconn_key  ,service_id        , charge            , ext_id            ,"+
		" retries           , priority          ,qtime             ,stime             ,status            ,"+
		" op_id             , ctype             , ref1             ,ref2              ,ref3 , charge_status "+
		" from chargeq### where status =? and stime < now() order by retries,priority limit ? ";
		
		/**
		 * This sql string updates the status of a message in the charge queue
		 * using the senq_id an the old status.
		 */
		public static String UPD_MON_STATUS=
			" update chargeq### set status =? where senq_id=? ";
		
		/**
		 * This sql string deletes a message from the charge queue.
		 */
		public static String DEL_1=
			"delete from chargeq### where senq_id = ?";
		
		/**
		 * This sql string inserts a message in the charge queue. 
		 */
		public static String INS_1=
			" insert into chargeq### "+
			" (senq_id, origin , dest, message, aditional_message, xconn_key,service_id, charge, ext_id, retries, priority, qtime, stime, status, op_id, ctype, ref1, ref2, ref3, charge_status)"+
			" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		/**
		 * This sql string updates the number of retries in the charge queue
		 */
		public static String UPD_RETRY=
			" update chargeq### set status =?, retries=?, stime=?, charge_status=? where senq_id=?";
		
		/**
		 * This sql deletes scheduled messages for an specified MSISDN
		 */
		public static String DELETE_BY_MSISDN=
			" delete from  chargeq### where dest = ?";
		
		/**
		 * This sql deletes scheduled messages for an specified MSISDN, suboption_id
		 */
		public static String DELETE_BY_MSISDN_SUBOPTION =
			" delete from  chargeq### where dest = ? and ref2 = ? ";
		
		/**
		 * This SQL recovers all messages in invalid status
		 */
		public static String RECOVER_MTS =
			" update chargeq### set status = 0 where status = 90 ";
		
		/**
		 * This SQL removes all messages in invalid status
		 */
		public static String REMOVE_MTS =
			" delete from chargeq### where status = 90 ";
	}
	
	
	/**
	 * This class handles all the sql to interact with the smsfinal table.
	 * @author TIMwe
	 *
	 */
	public static class SmsFinal
	{
	
		public static String INS_1=
			" insert into smsfinal "+
			" (senq_id           ,origin , dest              ,message           ,"+
			" aditional_message , xconn_key  ,service_id        , charge            , ext_id            ,"+
			" retries           , priority          ,qtime             ,stime             ,status            ,"+
			"op_id             , ctype             , ref1              ,ref2              ,ref3, charge_status, send_status, notif_status )"+
			" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	}
	
}
