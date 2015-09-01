package com.rake.android.rkmetrics.android;

import android.os.Build;
import com.rake.android.rkmetrics.util.RakeLogger;

import java.lang.reflect.Field;

import static com.rake.android.rkmetrics.config.RakeConfig.LOG_TAG_PREFIX;

public final class Compatibility {
    public static int getAPILevel() {
        int apiLevel = 1;
        try {
            // This field has been added in Android 1.6
            final Field SDK_INT = Build.VERSION.class.getField("SDK_INT");
            apiLevel = SDK_INT.getInt(null);
        } catch (Exception e) {
            RakeLogger.e(LOG_TAG_PREFIX, "can not retrieve API level", e);
            apiLevel = Build.VERSION.SDK_INT;
        }

        return apiLevel;
    }

}
