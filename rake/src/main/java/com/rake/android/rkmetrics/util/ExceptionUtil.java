package com.rake.android.rkmetrics.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtil {

    public static String createExceptionType(Throwable e) {
        return e == null ? null : e.getClass().getCanonicalName();
    }

    public static String createStacktrace(Throwable e) {
        if (e == null) {
            return null;
        }

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));

        return sw.toString();
    }
}
