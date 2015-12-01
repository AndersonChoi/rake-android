package com.rake.android.rkmetrics.shuttle;

import com.rake.android.rkmetrics.util.Logger;
import com.rake.android.rkmetrics.util.functional.Callback;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ShuttleProfiler {

    /** constants */

    /** top-level fields */
    public static final String FIELD_NAME_SENTINEL_META = "sentinel_meta";
    public static final String FIELD_NAME_BODY          = "_$body";
    public static final String FIELD_NAME_PROPERTIES    = "properties";

    /** in sentinel_meta */
    public static final String META_FIELD_NAME_ENCRYPTION_FIELDS = "_$encryptionFields";
    public static final String META_FIELD_NAME_FIELD_ORDER       = "_$fieldOrder";
    public static final String META_FIELD_NAME_SCHEMA_ID         = "_$schemaId";
    public static final String META_FIELD_NAME_PROJECT_ID        = "_$projectId";

    /** in properties */
    public static final String PROPERTY_NAME_TOKEN             = "token";
    public static final String PROPERTY_NAME_BASE_TIME         = "base_time";
    public static final String PROPERTY_NAME_LOCAL_TIME        = "local_time";
    public static final String PROPERTY_NAME_RAKE_LIB          = "rake_lib";
    public static final String PROPERTY_NAME_RAKE_LIB_VERSION  = "rake_lib_version";
    public static final String PROPERTY_NAME_OS_NAME           = "os_name";
    public static final String PROPERTY_NAME_OS_VERSION        = "os_version";
    public static final String PROPERTY_NAME_MANUFACTURER      = "manufacturer";
    public static final String PROPERTY_NAME_DEVICE_MODEL      = "device_model";
    public static final String PROPERTY_NAME_DEVICE_ID         = "device_id";
    public static final String PROPERTY_NAME_SCREEN_HEIGHT     = "screen_height";
    public static final String PROPERTY_NAME_SCREEN_WIDTH      = "screen_width";
    public static final String PROPERTY_NAME_SCREEN_RESOLUTION = "resolution";
    public static final String PROPERTY_NAME_APP_VERSION       = "app_version";
    public static final String PROPERTY_NAME_CARRIER_NAME      = "carrier_name";
    public static final String PROPERTY_NAME_NETWORK_TYPE      = "network_type";
    public static final String PROPERTY_NAME_LANGUAGE_CODE     = "language_code";

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
            PROPERTY_NAME_CARRIER_NAME,
            PROPERTY_NAME_NETWORK_TYPE,
            PROPERTY_NAME_LANGUAGE_CODE
    );

    public static final String PROPERTY_VALUE_UNKNOWN               = "UNKNOWN";
    public static final String PROPERTY_VALUE_OS_NAME               = "Android";
    public static final String PROPERTY_VALUE_RAKE_LIB              = "android";
    public static final String PROPERTY_VALUE_NETWORK_TYPE_WIFI     = "WIFI";
    public static final String PROPERTY_VALUE_NETWORK_TYPE_NOT_WIFI = "NOT WIFI";

    public static final String EMPTY_BODY_STRING  = new JSONObject().toString();
    public static final String EMPTY_HEADER_VALUE = "";

    public static boolean hasBodyValue(RakeClientMetricSentinelShuttle shuttle,
                                       String field,
                                       Callback<Object, Boolean> comparator) {

        if (null == shuttle || null == field || null == comparator) return false;

        Object value = null;

        try {
            JSONObject _$body = shuttle.toJSONObject().getJSONObject(FIELD_NAME_BODY);
            value = _$body.get(field);
        } catch (Exception e) { /* JSONException, NullPointerException */
            return false;
        }

        if (null == value /* JSON.null */) return false;

        return comparator.execute(value);
    }

    public static boolean hasBodyValue(RakeClientMetricSentinelShuttle shuttle,
                                       String field,
                                       String expected) {

        if (null == shuttle || null == field || null == expected) return false;

        String value = null;

        try {
            JSONObject _$body = shuttle.toJSONObject().getJSONObject(FIELD_NAME_BODY);
            value = _$body.getString(field);
        } catch (Exception e) { /* JSONException, NullPointerException */
            return false;
        }

        if (null == value /* JSON.null */ || !expected.equals(value)) return false;

        return true;
    }

    public static boolean hasHeaderValue(RakeClientMetricSentinelShuttle shuttle,
                                         String field,
                                         String expected) {

        if (null == shuttle || null == field || null == expected) return false;

        String value = null;

        try {
            value = shuttle.toJSONObject().getString(field);
        } catch (Exception e) { /* JSONException, NullPointerException */
            return false;
        }

        if (null == value /* JSON.null */ || !expected.equals(value)) return false;

        return true;
    }

    public static boolean hasKey(JSONObject json, String depth1Key, String depth2Key) {
        if (null == json || null == depth1Key) return false;

        boolean hasKey = false;

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

    /**
     * - null 이거나
     *
     * - `sentinel_meta`
     * - `_$body`,
     *
     * - `seitinel_meta._$encryptionFields`
     * - `seitinel_meta._$projectId`
     * - `seitinel_meta._$schemaId`
     * - `seitinel_meta._$fieldOrder`
     *
     * 가 없을 경우 Shuttle 이 아닌 것으로 판단
     */
    public static boolean isShuttle(JSONObject shuttle) {
        if (null == shuttle) return false;

        boolean isShuttle = true;

        try {
            shuttle.get(FIELD_NAME_BODY);
            JSONObject sentinel_meta = shuttle.getJSONObject(FIELD_NAME_SENTINEL_META);

            sentinel_meta.get(META_FIELD_NAME_ENCRYPTION_FIELDS);
            sentinel_meta.get(META_FIELD_NAME_PROJECT_ID);
            sentinel_meta.get(META_FIELD_NAME_SCHEMA_ID);
            sentinel_meta.get(META_FIELD_NAME_FIELD_ORDER);

        } catch (JSONException e) {
            isShuttle = false;
        }

        return isShuttle;
    }

    public static boolean isTransformedShuttle(JSONObject transformed) {
        if (null == transformed) return false;

        boolean isValid = true;

        isValid &= hasKey(transformed, META_FIELD_NAME_FIELD_ORDER, null);
        isValid &= hasKey(transformed, META_FIELD_NAME_FIELD_ORDER, null);
        isValid &= hasKey(transformed, META_FIELD_NAME_FIELD_ORDER, null);
        isValid &= hasKey(transformed, META_FIELD_NAME_FIELD_ORDER, null);
        isValid &= hasKey(transformed, FIELD_NAME_PROPERTIES, null);
        isValid &= hasKey(transformed, FIELD_NAME_PROPERTIES, FIELD_NAME_BODY);

        return isValid;
    }

    public static boolean hasDefaultProps(JSONObject transformed) {
        if (null == transformed) return false;

        boolean isValid = true;

        for (String name : DEFAULT_PROPERTY_NAMES) {
           isValid &= transformed.has(name);
        }

        return isValid;
    }

    public static JSONObject transformShuttle(JSONObject shuttle,
                                              JSONObject superProps,
                                              JSONObject defaultProps) {

        if (null == shuttle || null == superProps || null == defaultProps) {
            Logger.e("Can't transform JSONObject with null args");
            return null;
        }

        if (!isShuttle(shuttle)) {
            Logger.e("Passed JSONObject is not created by Shuttle.toJSONObject");
            return null;
        }

        JSONObject transformed = new JSONObject();

        try {
            /** 1. extract META_FIELDS */
            JSONObject sentinel_meta = shuttle.getJSONObject(FIELD_NAME_SENTINEL_META);
            for (Iterator<?> iter = sentinel_meta.keys(); iter.hasNext(); ) {
                String key = (String) iter.next();
                transformed.put(key, sentinel_meta.get(key));
            }
            shuttle.remove(FIELD_NAME_SENTINEL_META);

            JSONObject fieldOrder = transformed.getJSONObject(META_FIELD_NAME_FIELD_ORDER);

            /** 2. Insert user-collected fields */
            for (Iterator<?> keys = shuttle.keys(); keys.hasNext(); ) {
                String key = (String) keys.next();
                Object value = shuttle.get(key);

                if (superProps.has(key) && value.toString().length() == 0) {
                    // DO NOT overwrite superProps if user inserted nothing
                } else superProps.put(key, value);
            }

            /** 3. Insert auto-collected fields */
            for (Iterator<?> keys = defaultProps.keys(); keys.hasNext(); ) {
                String key = (String) keys.next();
                boolean addToProperties = true;

                if (fieldOrder.has(key)) addToProperties = true;
                else addToProperties = false;

                /** merge super props with default props */
                if (addToProperties) { superProps.put(key, defaultProps.get(key)); }
            }

            /** 4. put properties */
            transformed.put(FIELD_NAME_PROPERTIES, superProps);

        } catch (Exception e) {
            Logger.e("Failed to track", e);
            return null;
        }

        return transformed;
    }
}
