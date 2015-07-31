package com.rake.android.rkmetrics.config;

/**
 * Stores global configuration options for the Rake library.
 * May be overridden to achieve custom behavior.
 */
public class RakeConfig {
    // When we've reached this many track calls, flush immediately
    public static final int BULK_UPLOAD_LIMIT = 40;

    public static final long DEFAULT_FLUSH_RAKE = 60 * 1000; /* 60 seconds */

    // Remove events that have sat around for this many milliseconds
    // on first initialization of the library. Default is 48 hours.
    // Must be reconfigured before the library is initialized for the first time.
    public static final int DATA_EXPIRATION = 1000 * 60 * 60 * 48;

    public static String BASE_ENDPOINT = "https://rake.skplanet.com:8443/log";
    public static String DEV_BASE_ENDPOINT = "https://pg.rake.skplanet.com:8443/log";

    public static boolean USE_HTTPS = true;

    public static final String LOG_TAG_PREFIX = "RakeAPI";
    public static final String TRACK_PATH = "/track";
}
