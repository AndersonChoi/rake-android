package com.rake.android.rkmetrics.util;

import static com.rake.android.rkmetrics.config.RakeConfig.LOG_TAG_PREFIX;

import android.util.Log;
import com.rake.android.rkmetrics.RakeAPI;

final public class Logger {

    public static RakeAPI.Logging loggingMode = RakeAPI.Logging.DISABLE;

    public Logger() throws InstantiationException { throw new InstantiationException("default constructor of Logger is not supported"); }
    private static boolean isEnabled() { return (RakeAPI.Logging.ENABLE == loggingMode); }

    public static int v(String msg) {
        return v(LOG_TAG_PREFIX, msg);
    }

    public static int v(String msg, Throwable t) {
        return v(LOG_TAG_PREFIX, msg, t);
    }

    public static int v(String tag, String msg) {
        if (isEnabled()) return Log.v(tag, msg);
        else return -1;
    }

    public static int v(String tag, String msg, Throwable t) {
        if (isEnabled()) return Log.v(tag, msg, t);
        else return -1;
    }

    public static int d(String msg) {
        return d(LOG_TAG_PREFIX, msg);
    }

    public static int d(String msg, Throwable t) {
        return d(LOG_TAG_PREFIX, msg, t);
    }

    public static int d(String tag, String msg) {
        if (isEnabled()) return Log.d(tag, msg);
        else return -1;
    }

    public static int d(String tag, String msg, Throwable t) {
        if (isEnabled()) return Log.d(tag, msg, t);
        else return -1;
    }

    public static int i(String msg) {
        return i(LOG_TAG_PREFIX, msg);
    }

    public static int i(String msg, Throwable t) {
        return i(LOG_TAG_PREFIX, msg, t);
    }

    public static int i(String tag, String msg) {
        if (isEnabled()) return Log.i(tag, msg);
        else return -1;
    }

    public static int i(String tag, String msg, Throwable t) {
        if (isEnabled()) return Log.i(tag, msg, t);
        else return -1;
    }

    public static int w(String msg) {
        return w(LOG_TAG_PREFIX, msg);
    }

    public static int w(String msg, Throwable t) {
        return w(LOG_TAG_PREFIX, msg, t);
    }

    public static int w(String tag, String msg) {
        if (isEnabled()) return Log.w(tag, msg);
        else return -1;
    }

    public static int w(String tag, String msg, Throwable t) {
        if (isEnabled()) return Log.w(tag, msg, t);
        else return -1;
    }

    public static int e(String msg) {
        return e(LOG_TAG_PREFIX, msg);
    }

    public static int e(String msg, Throwable t) {
        return e(LOG_TAG_PREFIX, msg, t);
    }

    public static int e(String tag, String msg) {
        return Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable t) {
        return Log.e(tag, msg, t);
    }

    private static String getTagWithThreadId(String tag) {
        return String.format("%s [Thread %d]", tag, Thread.currentThread().getId());
    }

    public static int t(String msg) {
        return t(LOG_TAG_PREFIX, msg);
    }

    public static int t(String msg, Throwable t) {
        return t(LOG_TAG_PREFIX, msg, t);
    }

    public static int t(String tag, String msg) {
        if (isEnabled()) return Log.d(getTagWithThreadId(tag), msg);
        else return -1;
    }

    public static int t(String tag, String msg, Throwable t) {
        if (isEnabled()) return Log.d(getTagWithThreadId(tag), msg, t);
        else return -1;
    }

    public static int wtf(String msg) {
        return wtf(LOG_TAG_PREFIX, msg);
    }

    public static int wtf(String msg, Throwable t) {
        return wtf(LOG_TAG_PREFIX, msg, t);
    }

    public static int wtf(String tag, String msg) {
        return Log.wtf(tag, msg);
    }

    public static int wtf(String tag, String msg, Throwable t) {
        return Log.wtf(tag, msg, t);
    }
}
