package com.rake.android.rkmetrics.shuttle;

import com.rake.android.rkmetrics.util.Logger;
import com.rake.android.rkmetrics.util.functional.Function1;
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

    public static final List<String> SENTINEL_META_FIELD_NAMES = Arrays.asList(
            META_FIELD_NAME_ENCRYPTION_FIELDS,
            META_FIELD_NAME_FIELD_ORDER,
            META_FIELD_NAME_SCHEMA_ID,
            META_FIELD_NAME_PROJECT_ID
    );

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
                                       Function1<Object, Boolean> comparator) {

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

    public static boolean hasValue(JSONObject props, String depth1Key,
                                   String depth2Key, String expected) {

        if (null == props || null == depth1Key || null == depth2Key || null == expected) {
            return false;
        }

        boolean hasValue = true;

        String found = null;
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

        if (null == found || !expected.equals(found)) return false;

        return hasValue;
    }


    public static boolean hasNoKey(JSONObject json, String depth1Key, String depth2Key) {
        return !hasKey(json, depth1Key, depth2Key);
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

    public static boolean hasMeta(JSONObject meta) {
        if (null == meta) return false;

        boolean isValid = true;

        isValid &= hasKey(meta, META_FIELD_NAME_FIELD_ORDER, null);
        isValid &= hasKey(meta, META_FIELD_NAME_FIELD_ORDER, null);
        isValid &= hasKey(meta, META_FIELD_NAME_FIELD_ORDER, null);
        isValid &= hasKey(meta, META_FIELD_NAME_FIELD_ORDER, null);
        isValid &= hasNoKey(meta, FIELD_NAME_SENTINEL_META, null);

        return isValid;
    }

    public static boolean hasProps(JSONObject validShuttle) {
        if (null == validShuttle) return false;

        boolean isValid = true;

        isValid &= hasKey(validShuttle, FIELD_NAME_PROPERTIES, null);
        isValid &= hasKey(validShuttle, FIELD_NAME_PROPERTIES, FIELD_NAME_BODY);

        return isValid;
    }

    public static boolean hasDefaultProps(JSONObject json, String depth1Key) {
        if (null == json) return false;

        boolean isValid = true;

        try {
            if (null != depth1Key) json = json.getJSONObject(depth1Key);
        } catch (Exception e) {
            return false;
        }

        for (String name : DEFAULT_PROPERTY_NAMES) {
           isValid &= hasKey(json, name, null);
        }

        return isValid;
    }

    public static boolean hasMetaFields(JSONObject json) {
        if (null == json) return false;

        boolean isValid = true;

        for (String name : SENTINEL_META_FIELD_NAMES) {
            isValid &= json.has(name);
        }

        isValid &= (!json.has(FIELD_NAME_SENTINEL_META));

        return isValid;
    }

    /**
     * 1. META 를 extract
     * 2. fieldOrder 를 이용해 userProps, superProps, defaultProps 를 머지해 props 생성
     * 3. props 에 META 를 추가하여 돌려줌
     */
    public static JSONObject createValidShuttle(JSONObject userProps,
                                                JSONObject superProps,
                                                JSONObject defaultProps) {

        /** superProps 는 null 일 경우 mergeProps() 내에서 빈 JSONObject 생성하여 실행하므로 검사하지 않음 */
        if (null == userProps || null == defaultProps) {
            Logger.e("Can't create valid shuttle using null userProps, defaultProps");
            return null;
        }

        JSONObject validShuttle = null;

        try {
            JSONObject meta = extractMeta(userProps);
            JSONObject fieldOrder = meta.getJSONObject(META_FIELD_NAME_FIELD_ORDER);
            JSONObject props = mergeProps(fieldOrder, userProps, superProps, defaultProps);

            if (null != props) {
                meta.put(FIELD_NAME_PROPERTIES, props);
                validShuttle = meta;
            }
        } catch (Exception e) { /* JSONException or NullPointerException */
            Logger.e("Failed to make valid shuttle", e);
        }

        return validShuttle;
    }

    /**
     * @throws JSONException
     * @throws NullPointerException
     */
    public static JSONObject extractMeta(JSONObject userProps)
            throws JSONException, NullPointerException {

        if (!isShuttle(userProps)) {
            Logger.e("Passed JSONObject is not created by Shuttle.toJSONObject");
            return null;
        }

        JSONObject meta = new JSONObject();

        /** extract META_FIELDS and remove `sentinel_meta` FIELD */
        JSONObject sentinel_meta = userProps.getJSONObject(FIELD_NAME_SENTINEL_META);
        for (Iterator<?> iter = sentinel_meta.keys(); iter.hasNext(); ) {
            String key = (String) iter.next();
            meta.put(key, sentinel_meta.get(key));
        }

        userProps.remove(FIELD_NAME_SENTINEL_META);

        return meta;
    }


    /**
     * userProps, superProps, defaultProps 가 merge 될 경우, 같은 Key 에 대해
     *
     * - superProps 는 userProps 가 비어있지 않을 경우 덮어쓰지 않음
     * - defaultProps 는 항상 덮어 씀
     *
     * 우선순위는,
     *
     * superProps < userProps < defaultProps
     *
     * @throws JSONException
     * @throws NullPointerException
     */
    public static JSONObject mergeProps(JSONObject fieldOrder,
                                        JSONObject userProps,
                                        JSONObject superProps,
                                        JSONObject defaultProps)
            throws JSONException, NullPointerException {

        JSONObject props = new JSONObject();

        /** 0. Insert super-props */
        if (null != superProps) {
            for (Iterator<?> keys = superProps.keys(); keys.hasNext(); ) {
                String key = (String) keys.next();
                Object value = superProps.get(key);

                if (fieldOrder.has(key)) props.put(key, value);
            }
        }

        /** 1. Insert user-collected fields */
        for (Iterator<?> keys = userProps.keys(); keys.hasNext(); ) {
            String key = (String) keys.next();
            Object value = userProps.get(key);

            // TODO: JSON.null SEN-268, SEN-269
            /** iff userProps is not an empty string and it is in fieldOrder */
            if (!(value.toString().length() == 0) && fieldOrder.has(key))
                props.put(key, value);
        }

        /** 2. Insert auto-collected fields */
        for (Iterator<?> keys = defaultProps.keys(); keys.hasNext(); ) {
            String key = (String) keys.next();
            boolean addToProperties = false;

            if (fieldOrder.has(key)) addToProperties = true;

            /** merge super props with default props */
            if (addToProperties) props.put(key, defaultProps.get(key));
        }

        return props;
    }
}
