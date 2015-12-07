package com.rake.android.rkmetrics.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtil {
    /** static methods */
    public static String createExceptionType(Throwable e) {
        if (null == e) return null;

        return e.getClass().getSimpleName();
    }

    public static String createStacktrace(Throwable e) {
        if (null == e) return null;

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));

        return sw.toString();
    }
}
