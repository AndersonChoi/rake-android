package com.rake.android.rkmetrics.shuttle;

import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import static org.assertj.core.api.Assertions.*;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19, manifest = Config.NONE)
public class ShuttleProfilerSpec {

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

    @Test
    public void isShuttle_negative_case_null() {
        assertThat(isShuttle(null)).isFalse();
    }

    @Test
    public void isShuttle_negative_case_no_body() throws JSONException {
        JSONObject shuttle = getShuttleWithMissingField(FIELD_NAME_BODY, null);
        assertThat(isShuttle(shuttle)).isFalse();
    }

    @Test
    public void isShuttle_negative_case_no_sentinel_meta() throws JSONException {
        JSONObject shuttle = getShuttleWithMissingField(FIELD_NAME_SENTINEL_META, null);
        assertThat(isShuttle(shuttle)).isFalse();
    }

    @Test
    public void isShuttle_negative_case_no_encryptionFields_in_sentinel_meta() throws JSONException {
        JSONObject shuttle = getShuttleWithMissingField(
                FIELD_NAME_SENTINEL_META, META_FIELD_NAME_ENCRYPTION_FIELDS);

        assertThat(isShuttle(shuttle)).isFalse();
    }

    @Test
    public void isShuttle_negative_case_no_schemaId_in_sentinel_meta() throws JSONException {
        JSONObject shuttle = getShuttleWithMissingField(
                FIELD_NAME_SENTINEL_META, META_FIELD_NAME_SCHEMA_ID);

        assertThat(isShuttle(shuttle)).isFalse();
    }

    @Test
    public void isShuttle_negative_case_no_projectId_in_sentinel_meta() throws JSONException {
        JSONObject shuttle = getShuttleWithMissingField(
                FIELD_NAME_SENTINEL_META, META_FIELD_NAME_PROJECT_ID);

        assertThat(isShuttle(shuttle)).isFalse();
    }

    @Test
    public void isShuttle_negative_case_no_fieldOrder_in_sentinel_meta() throws JSONException {
        JSONObject shuttle = getShuttleWithMissingField(
                FIELD_NAME_SENTINEL_META, META_FIELD_NAME_FIELD_ORDER);

        assertThat(isShuttle(shuttle)).isFalse();
    }

    @Test
    public void isShuttle_positive_case() {
        assertThat(isShuttle(new RakeClientMetricSentinelShuttle().toJSONObject())).isTrue();
    }

    @Test
    public void transformShuttleFormat_negative_case_null() {
        /** 3개의 인자중 하나라도 null 일 경우 리턴값은 null 이어야 함 */

        JSONObject j1 = transformShuttle(null, new JSONObject(), new JSONObject());
        JSONObject j2 = transformShuttle(new JSONObject(), null, new JSONObject());
        JSONObject j3 = transformShuttle(new JSONObject(), new JSONObject(), null);

        JSONObject j4 = transformShuttle(new JSONObject(), null, null);
        JSONObject j5 = transformShuttle(null, new JSONObject(), null);
        JSONObject j6 = transformShuttle(null, null, new JSONObject());

        JSONObject j7 = transformShuttle(null, null, null);

        assertThat(j1).isNull();
        assertThat(j2).isNull();
        assertThat(j3).isNull();
        assertThat(j4).isNull();
        assertThat(j5).isNull();
        assertThat(j6).isNull();
        assertThat(j7).isNull();
    }

    @Test
    public void transformShuttleFormat_positive_case() {
        RakeClientMetricSentinelShuttle shuttle = new RakeClientMetricSentinelShuttle();
        JSONObject defaultProps = new JSONObject();
        JSONObject superProps   = new JSONObject();
        JSONObject validShuttle = transformShuttle(
                shuttle.toJSONObject(), superProps, defaultProps);

        // isTransformedShuttle 테스트와의 교차 검증을 위해 필드를 아래와 같이 직접 나열
        assertThat(hasKey(validShuttle, META_FIELD_NAME_ENCRYPTION_FIELDS, null)).isTrue();
        assertThat(hasKey(validShuttle, META_FIELD_NAME_SCHEMA_ID, null)).isTrue();
        assertThat(hasKey(validShuttle, META_FIELD_NAME_PROJECT_ID, null)).isTrue();
        assertThat(hasKey(validShuttle, META_FIELD_NAME_FIELD_ORDER, null)).isTrue();
        assertThat(hasKey(validShuttle, FIELD_NAME_PROPERTIES, null)).isTrue();
        assertThat(hasKey(validShuttle, FIELD_NAME_PROPERTIES, FIELD_NAME_BODY)).isTrue();
    }

    @Test
    public void test_isTransformedShuttle() {
        RakeClientMetricSentinelShuttle shuttle = new RakeClientMetricSentinelShuttle();
        JSONObject defaultProps = new JSONObject();
        JSONObject superProps   = new JSONObject();
        JSONObject transformed = transformShuttle(
                shuttle.toJSONObject(), superProps, defaultProps);

        JSONObject invalid = new JSONObject();

        assertThat(isTransformedShuttle(transformed)).isTrue();
        assertThat(isTransformedShuttle(invalid)).isFalse();
    }

    @Test
    public void test_hasKey() throws JSONException {
        JSONObject depth1 = new JSONObject();
        JSONObject depth2 = new JSONObject();

        depth1.put("key1", 1);
        depth1.put("key2", 2);
        depth1.put("key3", JSONObject.NULL);
        depth1.put("key4", null); /* remove key 4 */

        depth2.put("nested_key1", 1);
        depth2.put("nested_key2", 2);
        depth2.put("nested_key3", JSONObject.NULL);
        depth2.put("nested_key4", null); /* remove nested_key4 */

        String depth2Key = "depth2";
        depth1.put(depth2Key, depth2);

        assertThat(hasKey(depth1, "key1", null)).isTrue();
        assertThat(hasKey(depth1, "key2", null)).isTrue();
        assertThat(hasKey(depth1, "key3", null)).isTrue();
        assertThat(hasKey(depth1, "key4", null)).isFalse();
        assertThat(hasKey(depth1, depth2Key, null)).isTrue();

        assertThat(hasKey(depth2, "nested_key1", null)).isTrue();
        assertThat(hasKey(depth2, "nested_key2", null)).isTrue();
        assertThat(hasKey(depth2, "nested_key3", null)).isTrue();
        assertThat(hasKey(depth2, "nested_key4", null)).isFalse();
    }

    private static JSONObject getShuttleWithMissingField(String depth1, String depth2)
            throws JSONException {

        JSONObject shuttle = new RakeClientMetricSentinelShuttle().toJSONObject();

        if (null == depth2) {
            shuttle.remove(depth1);
            return shuttle;
        }

        JSONObject container = shuttle.getJSONObject(depth1);
        container.remove(depth2);

        return shuttle;
    }

}
