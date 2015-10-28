package com.rake.android.rkmetrics.util;

public final class UriUtil {

    private UriUtil() {}

    // NOTE: some clients use 8553 port using `RakeAPI.setEndpoint` function
    public static final String LIVE_BASE_ENDPOINT = "https://rake.skplanet.com:8443/log";
    public static final String DEV_BASE_ENDPOINT = "https://pg.rake.skplanet.com:8443/log";
    public static final String ENDPOINT_TRACK_PATH = "/track";
}
