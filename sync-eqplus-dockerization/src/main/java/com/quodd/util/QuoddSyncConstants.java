package com.quodd.util;

public final class QuoddSyncConstants {

	public static final String AUTHORIZATION = "Authorization";
	public static final String FIRM_CODE = "firm_code";
	public static final String TOKEN = "token";
	public static final String SERVICE_NAME = "service_name";
	public static final String SERVICE_ID = "service_id";
	public static final String QS_UPSTREAM_ID = "qs_upstream_id";
	public static final String QS_DB_CODE = "qs_db_code";
	public static final String PRODUCT_ID = "product_id"; 
	public static final String PRODUCT_NAME = "product_name"; 
	public static final String QS_ENTITLEMENT_ID = "qs_entitlement_id";
	public static final String feedQuality = "feed_quality";
	public static final String NO_RESPONSE_FOUND = "No response from server.";
	public static final String GET_REQUEST = "GET";
	public static final String POST_REQUEST = "POST";
	public static final String SENDING_GET_REQUEST = "Sending "+GET_REQUEST+" request to URL : ";
	public static final String SENDING_POST_REQUEST = "Sending "+POST_REQUEST+" request to URL : ";
	public static final String GETTING_RESPONSE_FROM_URL = "Getting Response from URL : ";
	public static final String FIRM_ID = "firm_id";
	public static final String FIRM_NAME = "firm_name";
	public static final String EXCHANGE_ID = "exchange_id";
	public static final String ADDRESS_LINE_1 = "address_line_1";
	public static final String ADDRESS_LINE_2 = "address_line_2";
	public static final String CITY = "city";
	public static final String STATE = "state";
	public static final String ZIP = "zip";
	public static final String COUNTRY = "country";
	public static final String PHONE = "phone";
	public static final String EMAIL = "email";
	public static final String QS_FIRM_ID = "qs_firm_id";
	public static final String DEVICE_TYPE = "device_type";
	public static final String DATAFEED_TYPE = "datafeed_type";
	public static final String FIRM_TYPE = "firm_type";
	public static final int USER_TYPE_SEAT = 1;
	public static final int USER_TYPE_TRIAL = 0;

	
	private QuoddSyncConstants() {
		throw new IllegalStateException("Utility class");
	}

}
