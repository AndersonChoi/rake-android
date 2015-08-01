package com.rake.android.rkmetrics.config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Stores global configuration options for the Rake library.
 * May be overridden to achieve custom behavior.
 */
final public class RakeConfig {

    private RakeConfig() {}

    public static final int TRACK_MAX_LOG_COUNT = 40;
    public static final long DEFAULT_FLUSH_INTERVAL = 60 * 1000; /* 60 seconds */
    public static final int DATA_EXPIRATION_TIME = 1000 * 60 * 60 * 48; /* 48 hours */

    public static final DateFormat baseTimeFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    public static final DateFormat localTimeFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    static { baseTimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); }

    // TODO: remove r0.5.0_c. it requires to modify server dep.
    // version number will be replaced automatically when building.
    public static final String RAKE_LIB_VERSION = "r0.5.0_c0.3.17";

    public static final String LIVE_BASE_ENDPOINT = "https://rake.skplanet.com:8443/log";
    public static final String DEV_BASE_ENDPOINT = "https://pg.rake.skplanet.com:8443/log";

    public static final boolean USE_HTTPS = true;

    public static final String LOG_TAG_PREFIX = "RakeAPI";
    public static final String ENDPOINT_TRACK_PATH = "/track";
}
