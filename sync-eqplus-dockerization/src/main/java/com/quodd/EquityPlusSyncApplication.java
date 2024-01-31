package com.quodd;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.quodd.cache.Qss4Cache;
import com.quodd.cache.QuoddSupportCache;
import com.quodd.common.logger.QuoddLogger;
import com.quodd.common.request.EmailSenderRequest;
// import com.quodd.common.request.ProcessMonitoringRequest;
import com.quodd.common.util.Constants;
// import com.quodd.common.util.QuoddProperty;
import com.quodd.controller.EntitlementSyncController;
import com.quodd.controller.UserSyncController;
import com.quodd.util.EquityUtility;

import io.github.cdimascio.dotenv.Dotenv;

public class EquityPlusSyncApplication {
	public static Logger logger = Logger.getLogger(EquityPlusSyncApplication.class.getName());
	public static final Dotenv environments = Dotenv.load();
	// public static final Logger logger =
	// QuoddLogger.getInstance("entitlementSync").getLogger();
	// public static final QuoddProperty environmentProperties = new
	// QuoddProperty("/environment.properties");
	public static final EquityUtility utility = new EquityUtility();
	public static final Gson gson = new Gson();

	public static final Qss4Cache qss4Cache = new Qss4Cache();
	public static final String QSS4_DOMAIN = environments.get("QSS4_DOMAIN", "http://172.16.192.127:8988/vor/quodd");
	// public static final String QSS4_DOMAIN =
	// environmentProperties.getStringProperty("QSS4_DOMAIN","http://172.16.192.127:8988/vor/quodd");
	public static final String qss4Authorization = "Basic ZXFwbHVzOm9uZGVtYW5kZXFwbHVz";

	public static final QuoddSupportCache quoddSupportCache = new QuoddSupportCache();
	public static final String QS_DOMAIN = environments.get("QS_DOMAIN", "https://quoddsupport.com/api");
	public static final String qsAuthorization = "Basic cXVvZGRzdXBwb3J0OnFzdXBwb3J0MjAxOSM=";

	// private static ProcessMonitoringRequest processMonitoringRequest = null;
	private static final String PROCESS_ID = "QS_Entitlement_sync_" + System.currentTimeMillis();
	private static final String PROCESS_NAME = "QS_Entitlement_sync";
	private static int recordsProcessed = 0;

	public static void main(String[] args) throws Exception {
		// Firstly add services, product and product-service link.
		logger.info(() -> "Sync started");
		try {
			// processMonitoringRequest = new ProcessMonitoringRequest();
			// processMonitoringRequest.insertProcessDetail(PROCESS_NAME, PROCESS_ID, Constants.GROUP_ID_SYNC);
			EmailSenderRequest emailSender = new EmailSenderRequest();
			String type = args[0];
			if ("entitlement".equalsIgnoreCase(type)) {
				logger.info(() -> "Entitlement Controller started");
				EntitlementSyncController entitlementSyncController = new EntitlementSyncController();
				entitlementSyncController.syncServices();
				entitlementSyncController.syncProducts();
				entitlementSyncController.syncProductServiceMapping();
				emailSender.sendEmail(utility.getEmailBody(),
						"QS-Qss4 Entitlement Daily Sync " + new SimpleDateFormat("yyyMMdd-hhmmss").format(new Date()));
				logger.info(() -> "Entitlement Controller ended");
			} else if ("equser".equalsIgnoreCase(type)) {
				logger.info(() -> "User Controller started");
				UserSyncController userSyncController = new UserSyncController();
				emailSender.sendEmail(utility.getEmailBody(),
						"QS-Qss4 User Daily Sync " + new SimpleDateFormat("yyyMMdd-hhmmss").format(new Date()));
				logger.info(() -> "User Controller ended");
			}
			// processMonitoringRequest.updateProcessDetail(PROCESS_ID, Constants.PROCESS_STATUS_SUCCESS, recordsProcessed,
					// "", "EquityPlus_Entitlement_sync", Constants.GROUP_ID_SYNC);
			logger.info(() -> "Sync ended");
		} catch (Exception e) {
			// processMonitoringRequest.updateProcessDetail(PROCESS_ID, Constants.PROCESS_STATUS_FAILURE, recordsProcessed,
			// 		e.getMessage(), "", Constants.GROUP_ID_SYNC);
			throw e;
		}
	}
}