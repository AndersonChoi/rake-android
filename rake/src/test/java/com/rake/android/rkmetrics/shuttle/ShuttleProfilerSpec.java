package com.rake.android.rkmetrics.shuttle;

import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import static org.assertj.core.api.Assertions.*;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
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
