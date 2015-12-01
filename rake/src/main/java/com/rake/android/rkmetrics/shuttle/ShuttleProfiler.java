package com.rake.android.rkmetrics.shuttle;

import com.rake.android.rkmetrics.util.functional.Callback;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import org.json.JSONException;
import org.json.JSONObject;

public class ShuttleProfiler {

    /**
     * constants
     */

    public static final String FIELD_NAME_SENTINEL_META = "sentinel_meta";
    public static final String FIELD_NAME_FIELD_ORDER   = "_$fieldOrder";
    public static final String FIELD_NAME_PROPERTIES    = "properties";
    public static final String FIELD_NAME_BODY          = "_$body";

    public static final String PROPERTY_NAME_TOKEN      = "token";
    public static final String PROPERTY_NAME_BASE_TIME  = "base_time";
    public static final String PROPERTY_NAME_LOCAL_TIME = "local_time";

    public static final String EMPTY_BODY_STRING = new JSONObject().toString();
    public static final String EMPTY_HEADER_VALUE = "";

    public static boolean hasBodyValue(RakeClientMetricSentinelShuttle shuttle,
                                       String field,
                                       Callback<Long, Boolean> comparator) {

        if (null == shuttle || null == field || null == comparator) return false;

        Long value = null;

        try {
            JSONObject _$body = shuttle.toJSONObject().getJSONObject(FIELD_NAME_BODY);
            value = _$body.getLong(field);
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

}
