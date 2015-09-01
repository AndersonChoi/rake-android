package com.rake.android.rkmetrics.android;

import android.os.Build;
import com.rake.android.rkmetrics.util.RakeLogger;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static com.rake.android.rkmetrics.config.RakeConfig.LOG_TAG_PREFIX;

public final class Compatibility {

    public enum APILevel {
        DEFAULT(9),
        GINGERBREAD(9),
        ICE_CREAM_SANDWICH(14),
        JELLY_BEAN(16),
        KITKAT(19),
        LOLLIPOP(21),
        M(22);

        private static final Map<Integer, APILevel> lookup = new HashMap<Integer, APILevel>();

        static {
            for(APILevel apiLevel : APILevel.values()) {
                lookup.put(apiLevel.getLevel(), apiLevel);
            }
        }

        private final int level;
        APILevel(int level) { this.level = level; }
        public int getLevel() { return this.level; }
        public static APILevel fromInt(Integer level) {
            APILevel apiLevel = lookup.get(level);
            return (null == apiLevel) ? APILevel.DEFAULT : apiLevel;
        }
    }

    public static int getIntAPILevel() {
        int apiLevel = 1;
        try {
            // This field has been added in Android 1.6
            final Field SDK_INT = Build.VERSION.class.getField("SDK_INT");
            apiLevel = SDK_INT.getInt(null);
        } catch (Exception e) {
            RakeLogger.e(LOG_TAG_PREFIX, "can not retrieve API level", e);
            apiLevel = (Build.VERSION.SDK_INT);
        }

        return apiLevel;
    }

}
