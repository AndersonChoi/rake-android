package com.rake.android.rkmetrics.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {

    private static final ThreadLocal<SimpleDateFormat> baseTimeFormatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

            return format;
        }
    };

    private static final ThreadLocal<SimpleDateFormat> localTimeFormatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US);
        }
    };

    public static DateFormat getBaseFormatter() {
        return baseTimeFormatter.get();
    }

    public static DateFormat getLocalFormatter() {
        return localTimeFormatter.get();
    }

    public static String getCurrentTime() {
        return baseTimeFormatter.get().format(new Date());
    }

    public static long convertNanoTimeDurationToMillis(long start, long end) {
        return (end - start) / 1000000; /* drop nano second accuracy */
    }
}
