package com.rake.android.rkmetrics.shuttle;

import android.app.Application;

import com.rake.android.rkmetrics.RakeAPI;
import com.rake.android.rkmetrics.TestUtil;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import static org.assertj.core.api.Assertions.*;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Date;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19, manifest = Config.NONE)
public class ShuttleProfilerSpec {

    Application app = RuntimeEnvironment.application;

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
    public void defaultPropertyNames_length() {
        /** default properties 추가 혹은 삭제될 경우, 이 테스트 코드의 하드코딩된 숫자도 변화시켜야 함 */
        assertThat(DEFAULT_PROPERTY_NAMES.size()).isEqualTo(17);
    }

    @Test
    public void metaFieldNames_length() {
        /** sentinel meta fields 추가 혹은 삭제될 경우, 이 테스트 코드의 하드코딩된 숫자도 변화시켜야 함 */
        assertThat(SENTINEL_META_FIELD_NAMES.size()).isEqualTo(4);
    }

    @Test
    public void createValidShuttle_negative_case() {
        JSONObject j1 = createValidShuttle(new JSONObject(), null, null);
        JSONObject j2 = createValidShuttle(null, new JSONObject(), null);
        JSONObject j3 = createValidShuttle(null, null, new JSONObject());
        JSONObject j4 = createValidShuttle(null, null, null);

        assertThat(j1).isNull();
        assertThat(j2).isNull();
        assertThat(j3).isNull();
        assertThat(j4).isNull();
    }

    @Test /** IMPORTANT TEST: FULL test */
    public void createValidShuttle_should_return_validShuttle() throws JSONException {
        /**
         * validShuttle 은
         *
         * - 4개의 META 필드가 TOP-LEVEL 에 존재
         * - properties TOP-LEVEL 에 존재
         * - properties._$body 가 존재
         * - 자동수집필드 (default props) 가 properties 에 존재
         */

        JSONObject userProps = new RakeClientMetricSentinelShuttle().toJSONObject();
        JSONObject superProps = new JSONObject();
        JSONObject defaultProps = RakeAPI.getDefaultProps(app, RakeAPI.Env.DEV, TestUtil.genToken(), new Date());


        JSONObject validShuttle = createValidShuttle(userProps, superProps, defaultProps);

        hasMeta(validShuttle);
        hasProps(validShuttle);
        hasDefaultProps(validShuttle, FIELD_NAME_PROPERTIES);
    }

    @Test
    public void superProps_should_not_override_userProps() throws JSONException {
        /**
         * superProp 는 userProps 가 있을 경우에 덮어 쓰면 안됌
         */
        JSONObject userProps = new RakeClientMetricSentinelShuttle().toJSONObject();

        /** RakeClientMetricShuttle 을 샘플 Shuttle 로 사용하므로, 존재하는 임의 헤더를 superProps 테스트 대상으로 사용 */
        String sampleHeaderKey = "transaction_id";
        String sampleHeaderValue = "origin tx id";
        String overridedHeaderValue = "override tx id";
        assertThat(userProps.has(sampleHeaderKey)).isTrue();

        userProps.put(sampleHeaderKey, sampleHeaderValue);

        JSONObject defaultProps = RakeAPI.getDefaultProps(app, RakeAPI.Env.DEV, TestUtil.genToken(), new Date());

        JSONObject meta = extractMeta(userProps);
        JSONObject fieldOrder = meta.getJSONObject(META_FIELD_NAME_FIELD_ORDER);
        JSONObject superProps = new JSONObject();
        superProps.put(sampleHeaderKey, overridedHeaderValue);

        JSONObject props = mergeProps(fieldOrder, userProps, superProps, defaultProps);

        hasValue(props, FIELD_NAME_PROPERTIES, sampleHeaderKey, sampleHeaderKey);
    }

    @Test
    public void superProps_can_override_given_userProps_field_is_empty() throws JSONException {
        /**
         * superProp 는 userProps 가 없을 경우 덮어쓸 수 있음
         */
        JSONObject userProps = new RakeClientMetricSentinelShuttle().toJSONObject();

        /** RakeClientMetricShuttle 을 샘플 Shuttle 로 사용하므로, 존재하는 임의 헤더를 superProps 테스트 대상으로 사용 */
        String sampleHeaderKey = "transaction_id";
        String sampleHeaderValue = "example tx id";
        assertThat(userProps.has(sampleHeaderKey)).isTrue();

        JSONObject meta = extractMeta(userProps);
        JSONObject fieldOrder = meta.getJSONObject(META_FIELD_NAME_FIELD_ORDER);
        JSONObject superProps = new JSONObject();
        superProps.put(sampleHeaderKey, sampleHeaderValue);

        JSONObject defaultProps = RakeAPI.getDefaultProps(app, RakeAPI.Env.DEV, TestUtil.genToken(), new Date());

        JSONObject props = mergeProps(fieldOrder, userProps, superProps, defaultProps);

        hasValue(props, FIELD_NAME_PROPERTIES, sampleHeaderKey, sampleHeaderKey);
    }

    @Test /** IMPORTANT TEST */
    public void mergeProps_should_preserve_defaultProps() throws JSONException {
        /**
         * 사용자가 입력한 필드 중 defaultProps 에 해당하는 키가 있을 경우에, 무조건 덮어 씀
         */
        JSONObject userProps = new RakeClientMetricSentinelShuttle().toJSONObject();
        userProps.put(PROPERTY_NAME_RAKE_LIB, "invalid rake_lib");
        JSONObject superProps = new JSONObject();
        superProps.put(PROPERTY_NAME_TOKEN, "invalid token");

        JSONObject meta = extractMeta(userProps);
        JSONObject fieldOrder = meta.getJSONObject(META_FIELD_NAME_FIELD_ORDER);

        String token = TestUtil.genToken();
        JSONObject defaultProps = RakeAPI.getDefaultProps(app, RakeAPI.Env.DEV, token, new Date());

        JSONObject props = mergeProps(fieldOrder, userProps, superProps, defaultProps);

        hasValue(props, FIELD_NAME_PROPERTIES, PROPERTY_NAME_RAKE_LIB, PROPERTY_VALUE_RAKE_LIB);
        hasValue(props, FIELD_NAME_PROPERTIES, PROPERTY_NAME_TOKEN, token);
    }

    @Test /** IMPORTANT TEST */
    public void mergeProps_should_return_props_which_has_body_and_defaultProps() throws JSONException {
        /**
         * mergeProps() 의 결과는,
         *
         * defaultProps 를 가지고 있어야 하고,
         * _$body 키도 가지고 있어야 함
         */
        JSONObject userProps = new RakeClientMetricSentinelShuttle().toJSONObject();
        JSONObject meta = extractMeta(userProps);
        JSONObject fieldOrder = meta.getJSONObject(META_FIELD_NAME_FIELD_ORDER);
        JSONObject defaultProps = RakeAPI.getDefaultProps(app, RakeAPI.Env.DEV, TestUtil.genToken(), new Date());

        JSONObject props = mergeProps(fieldOrder, userProps, null /* null 일 수 있음 */, defaultProps);

        assertThat(hasKey(props, FIELD_NAME_BODY, null)).isTrue();
        assertThat(hasDefaultProps(props, null)).isTrue();
    }

    @Test /** IMPORTANT TEST */
    public void mergeProps_should_not_merge_super_props_if_fieldOrder_does_not_have_it() throws JSONException {
        JSONObject superProps = new JSONObject();
        JSONObject userProps = new RakeClientMetricSentinelShuttle().toJSONObject();
        JSONObject meta = extractMeta(userProps);
        JSONObject fieldOrder = meta.getJSONObject(META_FIELD_NAME_FIELD_ORDER);
        JSONObject defaultProps = RakeAPI.getDefaultProps(app, RakeAPI.Env.DEV, TestUtil.genToken(), new Date());

        String invalidPropName = PROPERTY_NAME_TOKEN + "_invalid003";
        try { /* 존재하지 않는 필드 이름을 확인하기 위해 */
            fieldOrder.get(invalidPropName+ "_invalid"); /* not existing prop name */
            failBecauseExceptionWasNotThrown(JSONException.class);
        } catch (JSONException e) { /* ignore, success case */ }

        superProps.put(invalidPropName, "value");

        JSONObject props = mergeProps(fieldOrder, userProps, superProps, defaultProps);

        assertThat(props.has(invalidPropName)).isFalse();
    }

    @Test /** IMPORTANT TEST */
    public void mergeProps_should_not_merge_default_props_if_fieldOrder_does_not_have_it() throws JSONException {
        JSONObject superProps = new JSONObject();
        JSONObject userProps = new RakeClientMetricSentinelShuttle().toJSONObject();
        JSONObject meta = extractMeta(userProps);
        JSONObject fieldOrder = meta.getJSONObject(META_FIELD_NAME_FIELD_ORDER);
        JSONObject defaultProps = RakeAPI.getDefaultProps(app, RakeAPI.Env.DEV, TestUtil.genToken(), new Date());

        String invalidPropName = PROPERTY_NAME_TOKEN + "_invalid003";
        try { /* 존재하지 않는 필드 이름을 확인하기 위해 */
            fieldOrder.get(invalidPropName+ "_invalid"); /* not existing prop name */
            failBecauseExceptionWasNotThrown(JSONException.class);
        } catch (JSONException e) { /* ignore, success case */ }

        defaultProps.put(invalidPropName, "value");

        JSONObject props = mergeProps(fieldOrder, userProps, superProps, defaultProps);

        assertThat(props.has(invalidPropName)).isFalse();
    }

    /**
     * case 1 - superProps 가 비었을 경우 userProps 가 무조건 덮어 씀,
     *          userProps 가 "" (EMPTY STRING) 여도 키가 보존되야 함
     *
     * case 2 - superProps 가 있고 userProps 가 "" 라면 superProps 가 유지 됨
     **/
    @Test /* RAKE-389 */
    public void mergeProp_should_preserve_empty_header() throws JSONException {
        // TODO: header 에 JSON.null 삽입시 테스트 변경 요구

        String HEADER_NAME_SERVICE_TOKEN = "service_token";
        String HEADER_NAME_APP_PACKAGE = "app_package";
        String HEADER_NAME_TRANSACTION_ID = "transaction_id";

        String userPropsServiceToken = "example service token";
        String userPropsAppPackage = "";    /* case 1 */
        String userPropsTransactionId = ""; /* case 2 */
        String superPropsTransactionId = "tx-id";

        JSONObject userProps = new RakeClientMetricSentinelShuttle()
                .service_token(userPropsServiceToken).transaction_id(userPropsTransactionId).toJSONObject();
        JSONObject meta = extractMeta(userProps);
        JSONObject fieldOrder = meta.getJSONObject(META_FIELD_NAME_FIELD_ORDER);
        JSONObject superProps = new JSONObject();
        superProps.put(HEADER_NAME_TRANSACTION_ID, superPropsTransactionId);
        JSONObject defaultProps = RakeAPI.getDefaultProps(app, RakeAPI.Env.DEV, TestUtil.genToken(), new Date());

        // validate if extra headers exist
        fieldOrder.get(HEADER_NAME_SERVICE_TOKEN);
        fieldOrder.get(HEADER_NAME_APP_PACKAGE);
        fieldOrder.get(HEADER_NAME_TRANSACTION_ID);

        JSONObject props = mergeProps(fieldOrder, userProps, superProps, defaultProps);

        /* normal case */
        assertThat(props.get(HEADER_NAME_SERVICE_TOKEN)).isEqualTo(userPropsServiceToken);
        /* case 1 */
        assertThat(props.get(HEADER_NAME_APP_PACKAGE)).isEqualTo(userPropsAppPackage);
        /* case 2 */
        assertThat(props.get(HEADER_NAME_TRANSACTION_ID)).isEqualTo(superPropsTransactionId);
    }

    @Test /* RAKE-390 */
    public void test_all_possible_header_values() {
        /**
         * HEADER 에
         *
         * A. 값을 넣었을 경우 -> 그대로 나와야 함
         * B. 빈 문자열을 넣었을 경우 ("") -> 그대로 나와야 함
         * C. NULL 을 넣었을 경우 -> 그대로 나와야함
         * D. JSON.NULL 을 넣었을 경우 -> ?
         */
    }

    @Test /* RAKE-390 */
    public void test_all_possible_body_values() {
        /**
         * BODY 에
         *
         * A. 값을 넣었을 경우 -> 그대로 나와야함
         * B. 빈 문자열을 넣었을 경우 ("") -> 그대로 나와야함
         * C. NULL 을 넣었을 경우 -> 키가 삭제되야 함
         * D. JSON.NULL 을 넣었을 경우 -> ?
         */
    }

    @Test
    public void extractMeta_negative_case_null() throws JSONException {
        JSONObject j1 = extractMeta(null);
        assertThat(j1).isNull();
    }

    @Test
    public void extractMeta_should_return_meta() throws JSONException {
        RakeClientMetricSentinelShuttle shuttle = new RakeClientMetricSentinelShuttle();
        JSONObject transformed = extractMeta(shuttle.toJSONObject());

        assertThat(hasMetaFields(transformed)).isTrue();
    }

    @Test
    public void test_hasMeta() throws JSONException {
        JSONObject shuttle = new RakeClientMetricSentinelShuttle().toJSONObject();
        JSONObject transformed = extractMeta(shuttle);
        JSONObject invalid = new JSONObject();

        assertThat(hasMeta(transformed)).isTrue();
        assertThat(hasMeta(invalid)).isFalse();
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
