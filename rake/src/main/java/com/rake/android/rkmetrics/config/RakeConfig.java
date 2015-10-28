package com.rake.android.rkmetrics.config;

import com.rake.android.rkmetrics.util.TimeUtil;

import java.util.TimeZone;

/**
 * Stores global configuration options for the Rake library.
 * May be overridden to achieve custom behavior.
 */
final public class RakeConfig {

    private RakeConfig() {}

    public static final int TRACK_MAX_LOG_COUNT = 50;

    // TODO: remove r0.5.0_c. it requires to modify server dep.
    // version number will be replaced automatically before compiling.
    public static final String RAKE_LIB_VERSION = "r0.5.0_c0.3.20";

    // NOTE: some clients use 8553 port using `RakeAPI.setRakeServer` function
    public static final String EMPTY_BASE_ENDPOINT = "";
    public static final String LIVE_HOST = "https://rake.skplanet.com";
    public static final String DEV_HOST = "https://pg.rake.skplanet.com";
    public static final String DEV_BASE_ENDPOINT = DEV_HOST + ":8443/log";
    public static final String LIVE_BASE_ENDPOINT = LIVE_HOST + ":8443/log";

    public static final String LOG_TAG_PREFIX = "RakeAPI";
    public static final String ENDPOINT_TRACK_PATH = "/track";
}
