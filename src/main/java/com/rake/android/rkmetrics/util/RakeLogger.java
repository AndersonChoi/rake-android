package com.rake.android.rkmetrics.util;

import android.util.Log;
import com.rake.android.rkmetrics.config.RakeLoggingMode;

final public class RakeLogger {

    public static RakeLoggingMode loggingMode = RakeLoggingMode.NO;

    public RakeLogger() throws InstantiationException { throw new InstantiationException("default constructor of RakeLogger is not supported"); }
    private static boolean isEnabled() { return (RakeLoggingMode.YES == loggingMode) ? true : false; }

    public static int v(String tag, String msg) {
        if (isEnabled()) return Log.v(tag, msg);
        else return -1;
    }

    public static int v(String tag, String msg, Throwable t) {
        if (isEnabled()) return Log.v(tag, msg, t);
        else return -1;
    }

    public static int d(String tag, String msg) {
        if (isEnabled()) return Log.d(tag, msg);
        else return -1;
    }

    public static int d(String tag, String msg, Throwable t) {
        if (isEnabled()) return Log.d(tag, msg, t);
        else return -1;
    }

    public static int i(String tag, String msg) {
        if (isEnabled()) return Log.i(tag, msg);
        else return -1;
    }

    public static int i(String tag, String msg, Throwable t) {
        if (isEnabled()) return Log.i(tag, msg, t);
        else return -1;
    }

    public static int w(String tag, String msg) {
        if (isEnabled()) return Log.w(tag, msg);
        else return -1;
    }

    public static int w(String tag, String msg, Throwable t) {
        if (isEnabled()) return Log.w(tag, msg, t);
        else return -1;
    }

    public static int w(String tag, Throwable t) {
        if (isEnabled()) return Log.w(tag, t);
        else return -1;
    }

    public static int e(String tag, String msg) {
        return Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable t) {
        return Log.e(tag, msg, t);
    }

    private static String getTagWithThreadId(String tag) {
        return String.format("Thread[%d] - %s",
                Thread.currentThread().getId(), tag);
    }

    public static int t(String tag, String msg) {
        if (isEnabled()) return Log.d(getTagWithThreadId(tag), msg);
        else return -1;
    }

    public static int t(String tag, String msg, Throwable t) {
        if (isEnabled()) return Log.d(getTagWithThreadId(tag), msg, t);
        else return -1;
    }

    public static int wtf(String tag, String msg) {
        return Log.wtf(tag, msg);
    }

    public static int wtf(String tag, Throwable t) {
        return Log.wtf(tag, t);
    }

    public static int wtf(String tag, String msg, Throwable t) {
        return Log.wtf(tag, msg, t);
    }

    public static String getStackTraceString(Throwable t) {
        return Log.getStackTraceString(t);
    }
}
