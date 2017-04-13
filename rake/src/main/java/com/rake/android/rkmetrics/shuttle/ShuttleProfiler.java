package com.rake.android.rkmetrics.shuttle;

import com.rake.android.rkmetrics.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ShuttleProfiler {

    /* top-level fields */
    static final String FIELD_NAME_SENTINEL_META = "sentinel_meta";
    static final String FIELD_NAME_BODY = "_$body";
    static final String FIELD_NAME_PROPERTIES = "properties";

    /* in sentinel_meta */
    static final String META_FIELD_NAME_ENCRYPTION_FIELDS = "_$encryptionFields";
    static final String META_FIELD_NAME_FIELD_ORDER = "_$fieldOrder";
    static final String META_FIELD_NAME_SCHEMA_ID = "_$schemaId";
    static final String META_FIELD_NAME_PROJECT_ID = "_$projectId";

    /* in properties (name) */
    public static final String PROPERTY_NAME_TOKEN = "token";
    public static final String PROPERTY_NAME_BASE_TIME = "base_time";
    public static final String PROPERTY_NAME_LOCAL_TIME = "local_time";
    public static final String PROPERTY_NAME_RAKE_LIB = "rake_lib";
    public static final String PROPERTY_NAME_RAKE_LIB_VERSION = "rake_lib_version";
    public static final String PROPERTY_NAME_OS_NAME = "os_name";
    public static final String PROPERTY_NAME_OS_VERSION = "os_version";
    public static final String PROPERTY_NAME_MANUFACTURER = "manufacturer";
    public static final String PROPERTY_NAME_DEVICE_MODEL = "device_model";
    public static final String PROPERTY_NAME_DEVICE_ID = "device_id";
    public static final String PROPERTY_NAME_SCREEN_HEIGHT = "screen_height";
    public static final String PROPERTY_NAME_SCREEN_WIDTH = "screen_width";
    public static final String PROPERTY_NAME_SCREEN_RESOLUTION = "resolution";
    public static final String PROPERTY_NAME_APP_VERSION = "app_version";
    public static final String PROPERTY_NAME_CARRIER_NAME = "carrier_name";
    public static final String PROPERTY_NAME_NETWORK_TYPE = "network_type";
    public static final String PROPERTY_NAME_LANGUAGE_CODE = "language_code";
    public static final String PROPERTY_NAME_LOG_VERSION = "log_version";

    /* in properties (values) */
    public static final String PROPERTY_VALUE_UNKNOWN = "UNKNOWN";
    public static final String PROPERTY_VALUE_OS_NAME = "Android";
    public static final String PROPERTY_VALUE_RAKE_LIB = "android";
    public static final String PROPERTY_VALUE_NETWORK_TYPE_WIFI = "WIFI";
    public static final String PROPERTY_VALUE_NETWORK_TYPE_NOT_WIFI = "NOT WIFI";

    static final String EMPTY_FIELD_VALUE = "";

    /**
     * - null 이거나
     * <p>
     * - `sentinel_meta`
     * - `_$body`,
     * <p>
     * - `sentinel_meta._$encryptionFields`
     * - `sentinel_meta._$projectId`
     * - `sentinel_meta._$schemaId`
     * - `sentinel_meta._$fieldOrder`
     * <p>
     * 가 없을 경우 Shuttle 이 아닌 것으로 판단
     */
    static boolean isShuttle(JSONObject shuttle) {
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

    /**
     * 1. META 를 extract
     * 2. fieldOrder 를 이용해 userProps, superProps, defaultProps 를 머지해 props 생성
     * 3. props 에 META 를 추가하여 돌려줌
     */
    public static JSONObject createValidShuttle(JSONObject userProps,
                                                JSONObject superProps,
                                                JSONObject defaultProps) {

        /* superProps 는 null 일 경우 mergeProps() 내에서 빈 JSONObject 생성하여 실행하므로 검사하지 않음 */
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

    static JSONObject extractMeta(JSONObject userProps) throws JSONException, NullPointerException {
        if (!isShuttle(userProps)) {
            Logger.e("Passed JSONObject is not created by Shuttle.toJSONObject");
            return null;
        }

        JSONObject meta = new JSONObject();

        /* extract META_FIELDS and remove `sentinel_meta` FIELD */
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
     * <p>
     * - superProps 는 userProps 가 비어있지 않을 경우 덮어쓰지 않음
     * - defaultProps 는 항상 덮어 씀
     * <p>
     * 우선순위는,
     * <p>
     * superProps < userProps < defaultProps
     *
     * @throws JSONException
     * @throws NullPointerException
     */
    static JSONObject mergeProps(JSONObject fieldOrder,
                                 JSONObject userProps,
                                 JSONObject superProps,
                                 JSONObject defaultProps)
            throws JSONException, NullPointerException {

        JSONObject props = new JSONObject();

        /* 0. Insert super-props */
        if (null != superProps) {
            for (Iterator<?> keys = superProps.keys(); keys.hasNext(); ) {
                String key = (String) keys.next();
                Object value = superProps.get(key);

                if (fieldOrder.has(key)) {
                    props.put(key, value);
                }
            }
        }

        /* 1. Insert user-collected fields */
        for (Iterator<?> keys = userProps.keys(); keys.hasNext(); ) {
            String key = (String) keys.next();
            Object value = userProps.get(key);

            if (null == value) {
                continue; /* Usually, we can't insert null into JSON */
            }

            /* if userProps is not an empty string and it is in fieldOrder */
            if (fieldOrder.has(key)) {
                // TODO: JSON.null SEN-268, SEN-269, consider RAKE-389

                /* RAKE-389 do not overwrite the superProp as userProp is empty */
                if (!props.has(key) || (null == props.get(key)) || !EMPTY_FIELD_VALUE.equals(value.toString())) {
                    props.put(key, value);
                }
            }
        }

        /* 2. Insert auto-collected fields */
        for (Iterator<?> keys = defaultProps.keys(); keys.hasNext(); ) {
            String key = (String) keys.next();

            /* merge super props with default props */
            if (fieldOrder.has(key)) {
                props.put(key, defaultProps.get(key));
            }
        }

        return props;
    }
}
