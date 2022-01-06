package com.example.dm.services;

public class Constants {
    public static final String TAG_LOG = "datamule";
    public static final String TAG_REQUEST = "dm_nw_request";

    public static final String EXTRA_STRING_DELIVERY_TYPE = "inputExtraDeliveryType";
    public static final String EXTRA_STRING_MESSAGE_TYPE = "inputExtraMsgType";
    public static final String EXTRA_STRING_SERVER_IP = "inputExtraServerIP";

    // Notification channel ID
    public static final String NOTIFICATION_CHANNEL_ID = "ForegroundServiceChannelDataMule";

    // ForegroundServce.java
    public static final String FOREGROUND_SERVICE_CHANNEL_ID = "foregroundservicechannel";

    // TIPPERS APIs
    public static final String API_CHECK_CONNECTION = "/observationhandler/observations/testconnection";
    public static final String API_ADD_DATA = "/observationhandler/observations/add";

//    http://128.195.52.69:8080/add/data
    public static final String TEST_API_CHECK_CONNECTION = "/testconnection";
    public static final String TEST_API_ADD_DATA = "/add/data";
    public static final String TEST_API_ADD_DATA_LIST = "/add/datalist";
}
