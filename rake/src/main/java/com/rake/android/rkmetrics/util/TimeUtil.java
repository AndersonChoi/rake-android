package com.rake.android.rkmetrics.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {

    // TODO: thread-local, thread-unsafe shared object
    public static final DateFormat baseTimeFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US);
    public static final DateFormat localTimeFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US);

    static { baseTimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); }
}
