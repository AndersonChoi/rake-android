package com.rake.android.rkmetrics.config;

import com.rake.android.rkmetrics.util.TimeUtil;

import java.util.TimeZone;

/**
 * Stores global configuration options for the Rake library.
 * May be overridden to achieve custom behavior.
 */
public final class RakeConfig {

    private RakeConfig() {}

    public static final int TRACK_MAX_LOG_COUNT = 50;

    // TODO: remove r0.5.0_c. it requires to modify server dep.
    // version number will be replaced automatically before compiling.
    public static final String RAKE_LIB_VERSION = "r0.5.0_c0.4.0";

    public static final String LOG_TAG_PREFIX = "Rake";
}
