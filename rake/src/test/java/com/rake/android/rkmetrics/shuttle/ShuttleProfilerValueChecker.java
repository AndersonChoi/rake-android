package com.rake.android.rkmetrics.shuttle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.FIELD_NAME_BODY;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.FIELD_NAME_PROPERTIES;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.FIELD_NAME_SENTINEL_META;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.META_FIELD_NAME_ENCRYPTION_FIELDS;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.META_FIELD_NAME_FIELD_ORDER;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.META_FIELD_NAME_PROJECT_ID;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.META_FIELD_NAME_SCHEMA_ID;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_APP_BUILD_NUMBER;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_APP_RELEASE;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_APP_VERSION;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_BASE_TIME;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_CARRIER_NAME;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_DEVICE_ID;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_DEVICE_MODEL;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_LANGUAGE_CODE;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_LOCAL_TIME;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_MANUFACTURER;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_NETWORK_TYPE;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_OS_NAME;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_OS_VERSION;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_RAKE_LIB;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_RAKE_LIB_VERSION;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_SCREEN_HEIGHT;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_SCREEN_RESOLUTION;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_SCREEN_WIDTH;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_TOKEN;

public class ShuttleProfilerValueChecker {
    static final List<String> SENTINEL_META_FIELD_NAMES = Arrays.asList(
            META_FIELD_NAME_ENCRYPTION_FIELDS,
            META_FIELD_NAME_FIELD_ORDER,
            META_FIELD_NAME_SCHEMA_ID,
            META_FIELD_NAME_PROJECT_ID
    );

    public static final List<String> DEFAULT_PROPERTY_NAMES = Arrays.asList(
            PROPERTY_NAME_TOKEN,
            PROPERTY_NAME_BASE_TIME,
            PROPERTY_NAME_LOCAL_TIME,
            PROPERTY_NAME_RAKE_LIB,
            PROPERTY_NAME_RAKE_LIB_VERSION,
            PROPERTY_NAME_OS_NAME,
            PROPERTY_NAME_OS_VERSION,
            PROPERTY_NAME_MANUFACTURER,
            PROPERTY_NAME_DEVICE_MODEL,
            PROPERTY_NAME_DEVICE_ID,
            PROPERTY_NAME_SCREEN_HEIGHT,
            PROPERTY_NAME_SCREEN_WIDTH,
            PROPERTY_NAME_SCREEN_RESOLUTION,
            PROPERTY_NAME_APP_VERSION,
            PROPERTY_NAME_APP_RELEASE,
            PROPERTY_NAME_APP_BUILD_NUMBER,
            PROPERTY_NAME_CARRIER_NAME,
            PROPERTY_NAME_NETWORK_TYPE,
            PROPERTY_NAME_LANGUAGE_CODE
    );

    static boolean hasValue(JSONObject props, String depth1Key,
                            String depth2Key, String expected) {

        if (null == props || null == depth1Key || null == depth2Key || null == expected) {
            return false;
        }

        boolean hasValue = true;

        String found;
        String key = depth1Key;

        try {
            if (null != depth2Key) {
                props = props.getJSONObject(depth1Key);
                key = depth2Key;
            }

            found = props.getString(key);
        } catch (Exception e) {
            return false;
        }

        return !(null == found || !expected.equals(found));

    }

    private static boolean hasNoKey(JSONObject json, String depth1Key, String depth2Key) {
        return !hasKey(json, depth1Key, depth2Key);
    }

    static boolean hasKey(JSONObject json, String depth1Key, String depth2Key) {
        if (null == json || null == depth1Key) return false;

        boolean hasKey;

        try {
            Object value = json.get(depth1Key);

            if (null != depth2Key) {
                JSONObject depth1 = json.getJSONObject(depth1Key);
                Object depth2 = depth1.get(depth2Key);
            }

            hasKey = true;
        } catch (JSONException e) {
            return false;
        }

        return hasKey;
    }

    static boolean hasMeta(JSONObject meta) {
        if (null == meta) return false;

        boolean isValid = true;

        isValid &= hasKey(meta, META_FIELD_NAME_FIELD_ORDER, null);
        isValid &= hasKey(meta, META_FIELD_NAME_FIELD_ORDER, null);
        isValid &= hasKey(meta, META_FIELD_NAME_FIELD_ORDER, null);
        isValid &= hasKey(meta, META_FIELD_NAME_FIELD_ORDER, null);
        isValid &= hasNoKey(meta, FIELD_NAME_SENTINEL_META, null);

        return isValid;
    }

    static boolean hasProps(JSONObject validShuttle) {
        if (null == validShuttle) return false;

        boolean isValid = true;

        isValid &= hasKey(validShuttle, FIELD_NAME_PROPERTIES, null);
        isValid &= hasKey(validShuttle, FIELD_NAME_PROPERTIES, FIELD_NAME_BODY);

        return isValid;
    }

    static boolean hasDefaultProps(JSONObject json, String depth1Key) {
        if (null == json) return false;

        boolean isValid = true;

        try {
            if (null != depth1Key) json = json.getJSONObject(depth1Key);
        } catch (Exception e) {
            return false;
        }

        for (String name : DEFAULT_PROPERTY_NAMES) {
            // RAKE-485 : Unit Test용 shuttle의 fieldOrder에는 없는 field들.
            // TODO : Unit Test용 shuttle에 app_release, app_build_number field 추가되면 아래 if문 제거할 것 
            if (name.equals(PROPERTY_NAME_APP_RELEASE) || name.equals(PROPERTY_NAME_APP_BUILD_NUMBER)) {
                continue;
            }

            isValid &= hasKey(json, name, null);
        }

        return isValid;
    }

    static boolean hasMetaFields(JSONObject json) {
        if (null == json) return false;

        boolean isValid = true;

        for (String name : SENTINEL_META_FIELD_NAMES) {
            isValid &= json.has(name);
        }

        isValid &= (!json.has(FIELD_NAME_SENTINEL_META));

        return isValid;
    }
}
