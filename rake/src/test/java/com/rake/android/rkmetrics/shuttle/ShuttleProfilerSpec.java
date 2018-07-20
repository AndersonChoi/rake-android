package com.rake.android.rkmetrics.shuttle;

import android.app.Application;

import com.rake.android.rkmetrics.TestUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.EMPTY_FIELD_VALUE;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.FIELD_NAME_BODY;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.FIELD_NAME_PROPERTIES;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.FIELD_NAME_SENTINEL_META;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.META_FIELD_NAME_ENCRYPTION_FIELDS;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.META_FIELD_NAME_FIELD_ORDER;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.META_FIELD_NAME_PROJECT_ID;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.META_FIELD_NAME_SCHEMA_ID;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_RAKE_LIB;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_NAME_TOKEN;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.PROPERTY_VALUE_RAKE_LIB;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.createValidShuttle;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.extractMeta;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.isShuttle;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.mergeProps;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfilerSpecHelper.BODY_NAME_BRANCH;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfilerSpecHelper.BODY_NAME_CODE_TEXT;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfilerSpecHelper.BODY_NAME_ISSUE_ID;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfilerSpecHelper.BODY_NAME_REPOSITORY;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfilerSpecHelper.HEADER_NAME_APP_PACKAGE;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfilerSpecHelper.HEADER_NAME_LOG_SOURCE;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfilerSpecHelper.HEADER_NAME_SESSION_ID;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfilerSpecHelper.HEADER_NAME_TRANSACTION_ID;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfilerSpecHelper.assertShuttleGeneratorVersion;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfilerSpecHelper.assertShuttleSchemaVersion;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfilerSpecHelper.getAutoPropsForTest;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfilerSpecHelper.getMergedPropsWithEmptySuperPropsForTest;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfilerSpecHelper.getShuttleWithMissingField;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfilerSpecHelper.getTestShuttle;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfilerValueChecker.DEFAULT_PROPERTY_NAMES;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfilerValueChecker.SENTINEL_META_FIELD_NAMES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19, manifest = Config.NONE)
public class ShuttleProfilerSpec {

    Application app = RuntimeEnvironment.application;


    /**
     * - null 이거나
     * <p>
     * - `sentinel_meta`
     * - `_$body`,
     * <p>
     * - `seitinel_meta._$encryptionFields`
     * - `seitinel_meta._$projectId`
     * - `seitinel_meta._$schemaId`
     * - `seitinel_meta._$fieldOrder`
     * <p>
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
    public void isShuttle_positive_case() throws JSONException {
        assertThat(isShuttle(getTestShuttle().toJSONObject())).isTrue();
    }

    @Test
    public void defaultPropertyNames_length() {
        /* default properties 추가 혹은 삭제될 경우, 이 테스트 코드의 하드코딩된 숫자도 변화시켜야 함 */
        assertThat(DEFAULT_PROPERTY_NAMES.size()).isEqualTo(19);
    }

    @Test
    public void metaFieldNames_length() {
        /* sentinel meta fields 추가 혹은 삭제될 경우, 이 테스트 코드의 하드코딩된 숫자도 변화시켜야 함 */
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

    @Test
    /* IMPORTANT TEST: FULL test */
    public void createValidShuttle_should_return_validShuttle() throws JSONException {
        /*
          validShuttle 은

          - 4개의 META 필드가 TOP-LEVEL 에 존재
          - properties TOP-LEVEL 에 존재
          - properties._$body 가 존재
          - 자동수집필드 (default props) 가 properties 에 존재
         */

        JSONObject userProps = getTestShuttle().toJSONObject();
        JSONObject superProps = new JSONObject();
        JSONObject autoProps = getAutoPropsForTest(app);

        JSONObject validShuttle = createValidShuttle(userProps, superProps, autoProps);

        ShuttleProfilerValueChecker.hasMeta(validShuttle);
        ShuttleProfilerValueChecker.hasProps(validShuttle);
        ShuttleProfilerValueChecker.hasAutoProps(validShuttle, FIELD_NAME_PROPERTIES);
    }

    @Test
    public void superProps_should_not_override_userProps() throws JSONException {
        /*
          superProp 는 userProps 가 있을 경우에 덮어 쓰면 안됌
         */
        JSONObject userProps = getTestShuttle().toJSONObject();

        /* RakeClientMetricShuttle 을 샘플 Shuttle 로 사용하므로, 존재하는 임의 헤더를 superProps 테스트 대상으로 사용 */
        String sampleHeaderKey = "transaction_id";
        String sampleHeaderValue = "origin tx id";
        String overridedHeaderValue = "override tx id";
        assertThat(userProps.has(sampleHeaderKey)).isTrue();

        userProps.put(sampleHeaderKey, sampleHeaderValue);
        JSONObject autoProps = getAutoPropsForTest(app);

        JSONObject meta = extractMeta(userProps);
        JSONObject fieldOrder = meta.getJSONObject(META_FIELD_NAME_FIELD_ORDER);
        JSONObject superProps = new JSONObject();
        superProps.put(sampleHeaderKey, overridedHeaderValue);

        JSONObject props = mergeProps(fieldOrder, userProps, superProps, autoProps);

        ShuttleProfilerValueChecker.hasValue(props, FIELD_NAME_PROPERTIES, sampleHeaderKey, sampleHeaderKey);
    }

    @Test
    public void superProps_can_override_given_userProps_field_is_empty() throws JSONException {
        /*
          superProp 는 userProps 가 없을 경우 덮어쓸 수 있음
         */
        JSONObject userProps = getTestShuttle().toJSONObject();

        /* RakeClientMetricShuttle 을 샘플 Shuttle 로 사용하므로, 존재하는 임의 헤더를 superProps 테스트 대상으로 사용 */
        String sampleHeaderKey = "transaction_id";
        String sampleHeaderValue = "example tx id";
        assertThat(userProps.has(sampleHeaderKey)).isTrue();

        JSONObject meta = extractMeta(userProps);
        JSONObject fieldOrder = meta.getJSONObject(META_FIELD_NAME_FIELD_ORDER);
        JSONObject superProps = new JSONObject();
        superProps.put(sampleHeaderKey, sampleHeaderValue);

        JSONObject autoProps = getAutoPropsForTest(app);

        JSONObject props = mergeProps(fieldOrder, userProps, superProps, autoProps);

        ShuttleProfilerValueChecker.hasValue(props, FIELD_NAME_PROPERTIES, sampleHeaderKey, sampleHeaderKey);
    }

    @Test
    /* IMPORTANT TEST */
    public void mergeProps_should_preserve_autoProps() throws JSONException {
        /*
          사용자가 입력한 필드 중 autoProps 에 해당하는 키가 있을 경우에, 무조건 덮어 씀
         */
        JSONObject userProps = getTestShuttle().toJSONObject();
        userProps.put(PROPERTY_NAME_RAKE_LIB, "invalid rake_lib");
        JSONObject superProps = new JSONObject();
        superProps.put(PROPERTY_NAME_TOKEN, "invalid token");

        JSONObject meta = extractMeta(userProps);
        JSONObject fieldOrder = meta.getJSONObject(META_FIELD_NAME_FIELD_ORDER);

        String token = TestUtil.genToken();
        JSONObject autoProps = getAutoPropsForTest(app);

        JSONObject props = mergeProps(fieldOrder, userProps, superProps, autoProps);

        ShuttleProfilerValueChecker.hasValue(props, FIELD_NAME_PROPERTIES, PROPERTY_NAME_RAKE_LIB, PROPERTY_VALUE_RAKE_LIB);
        ShuttleProfilerValueChecker.hasValue(props, FIELD_NAME_PROPERTIES, PROPERTY_NAME_TOKEN, token);
    }

    @Test
    /* IMPORTANT TEST */
    public void mergeProps_should_return_props_which_has_body_and_autoProps() throws JSONException {
        /*
          mergeProps() 의 결과는,

          autoProps 를 가지고 있어야 하고,
          _$body 키도 가지고 있어야 함
         */
        JSONObject userProps = getTestShuttle().toJSONObject();
        JSONObject meta = extractMeta(userProps);
        JSONObject fieldOrder = meta.getJSONObject(META_FIELD_NAME_FIELD_ORDER);
        JSONObject autoProps = getAutoPropsForTest(app);

        JSONObject props = mergeProps(fieldOrder, userProps, null /* null 일 수 있음 */, autoProps);

        assertThat(ShuttleProfilerValueChecker.hasKey(props, FIELD_NAME_BODY, null)).isTrue();
        assertThat(ShuttleProfilerValueChecker.hasAutoProps(props, null)).isTrue();
    }

    @Test
    /* IMPORTANT TEST */
    public void mergeProps_should_not_merge_super_props_if_fieldOrder_does_not_have_it() throws JSONException {
        assertShuttleSchemaVersion();

        JSONObject superProps = new JSONObject();
        JSONObject userProps = getTestShuttle().toJSONObject();
        JSONObject meta = extractMeta(userProps);
        JSONObject fieldOrder = meta.getJSONObject(META_FIELD_NAME_FIELD_ORDER);
        JSONObject autoProps = getAutoPropsForTest(app);

        String invalidPropName = PROPERTY_NAME_TOKEN + "_invalid003";
        try { /* 존재하지 않는 필드 이름을 확인하기 위해 */
            fieldOrder.get(invalidPropName + "_invalid"); /* not existing prop name */
            failBecauseExceptionWasNotThrown(JSONException.class);
        } catch (JSONException e) { /* ignore, success case */ }

        superProps.put(invalidPropName, "value");

        JSONObject props = mergeProps(fieldOrder, userProps, superProps, autoProps);

        assertThat(props.has(invalidPropName)).isFalse();
    }

    @Test
    /* IMPORTANT TEST */
    public void mergeProps_should_not_merge_default_props_if_fieldOrder_does_not_have_it() throws JSONException {
        assertShuttleSchemaVersion();

        // mergeProps 를 테스트 하기 위함이므로 helper function 을 작성하지 않고 직접 low-level 테스트
        JSONObject superProps = new JSONObject();
        JSONObject userProps = getTestShuttle().toJSONObject();
        JSONObject meta = extractMeta(userProps);
        JSONObject fieldOrder = meta.getJSONObject(META_FIELD_NAME_FIELD_ORDER);
        JSONObject autoProps = getAutoPropsForTest(app);

        String invalidPropName = PROPERTY_NAME_TOKEN + "_invalid003";
        try { /* 존재하지 않는 필드 이름을 확인하기 위해 */
            fieldOrder.get(invalidPropName + "_invalid"); /* not existing prop name */
            failBecauseExceptionWasNotThrown(JSONException.class);
        } catch (JSONException e) { /* ignore, success case */ }

        autoProps.put(invalidPropName, "value");

        JSONObject props = mergeProps(fieldOrder, userProps, superProps, autoProps);

        assertThat(props.has(invalidPropName)).isFalse();
    }

    /**
     * case 1 - superProps 가 비었을 경우 userProps 가 무조건 덮어 씀,
     * userProps 가 "" (EMPTY STRING) 여도 키가 보존되야 함
     * <p>
     * case 2 - superProps 가 있고 userProps 가 "" 라면 superProps 가 유지 됨
     **/
    @Test /* RAKE-389 */
    public void mergeProp_should_preserve_empty_header() throws JSONException {
        // TODO: header 에 JSON.null 삽입시 테스트 변경 요구

        assertShuttleSchemaVersion();

        String userPropsLogSource = "example log source";
        String userPropsAppPackage = "";    /* case 1 */
        String userPropsTransactionId = ""; /* case 2 */
        String superPropsTransactionId = "tx-id";

        // mergeProps 를 테스트 하기 위함이므로 helper function 을 작성하지 않고 직접 low-level 테스트
        JSONObject userProps = getTestShuttle()
                .log_source(userPropsLogSource).transaction_id(userPropsTransactionId).toJSONObject();
        JSONObject meta = extractMeta(userProps);
        JSONObject fieldOrder = meta.getJSONObject(META_FIELD_NAME_FIELD_ORDER);
        JSONObject superProps = new JSONObject();
        superProps.put(HEADER_NAME_TRANSACTION_ID, superPropsTransactionId);
        JSONObject autoProps = getAutoPropsForTest(app);

        JSONObject props = mergeProps(fieldOrder, userProps, superProps, autoProps);

        /* normal case */
        assertThat(props.get(HEADER_NAME_LOG_SOURCE)).isEqualTo(userPropsLogSource);
        /* case 1 */
        assertThat(props.get(HEADER_NAME_APP_PACKAGE)).isEqualTo(userPropsAppPackage);
        /* case 2 */
        assertThat(props.get(HEADER_NAME_TRANSACTION_ID)).isEqualTo(superPropsTransactionId);
    }

    @Test /* RAKE-390 */
    public void test_all_possible_header_values() throws JSONException {
        /*
          HEADER 에

          A. 값을 안 넣었을 경우
          B. String 타입 바디에 빈 문자열을 넣었을 경우 ("") -> ""
          C. String 타입 바디에 null 을 넣었을 경우 -> ""
             추후 JSONObject.NULL 로 변경
          D. String 이 아닌 다른 타입의 헤더에 null 값을 넣었을 경우,
         */

        assertShuttleGeneratorVersion();
        assertShuttleSchemaVersion();

        assertThat(EMPTY_FIELD_VALUE).isEqualTo("");
        String exampleTransactionId;                     /* case A, empty value  */
        String exampleAppPackage = null;                 /* case B, String type */
        Long exampleSessionId = null;                 /* case C, Long type */

        JSONObject userProps = getTestShuttle()
                .log_source(EMPTY_FIELD_VALUE)
                .app_package(exampleAppPackage)
                .session_id(exampleSessionId)
                .toJSONObject();

        JSONObject props = getMergedPropsWithEmptySuperPropsForTest(app, userProps);

        assertThat(props.get(HEADER_NAME_TRANSACTION_ID)).isEqualTo(EMPTY_FIELD_VALUE); /* case A */
        assertThat(props.get(HEADER_NAME_LOG_SOURCE)).isEqualTo(EMPTY_FIELD_VALUE);     /* case B */
        assertThat(props.get(HEADER_NAME_APP_PACKAGE)).isEqualTo(EMPTY_FIELD_VALUE);    /* case C */
        assertThat(props.get(HEADER_NAME_SESSION_ID)).isEqualTo(EMPTY_FIELD_VALUE);     /* case D */
    }

    @Test /* RAKE-390 */
    public void test_all_possible_body_values() throws JSONException {
        /*
          BODY 에
          A. 아무 값도 넣지 않았을 경우 -> 키가 삭제되야 함, 추후 JSONObject.NULL 로 변경
          B. String 타입 바디에 빈 문자열을 넣었을 경우 ("") -> 그대로 나와야함
          C. String 타입 바디에 null 을 넣었을 경우 -> 키가 삭제되야 함, 추후 JSONObject.NULL 로 변경
             추후 JSONObject.NULL 로 변경 (키 존재)
          D. String 이 아닌 다른 타입의 바디에 null 값을 넣었을 경우 -> 키가 삭제되야 함
         */

        assertShuttleGeneratorVersion();
        assertShuttleSchemaVersion();

        String exampleRepository;       /* case A */
        String exampleBranch = "";   /* case B */
        String exampleCodeText = null; /* case C */
        Long exampleIssueId = null;    /* case D */

        JSONObject userProps = getTestShuttle()
                .branch(exampleBranch)
                .code_text(exampleCodeText)
                .issue_id(exampleIssueId)
                .toJSONObject();

        JSONObject _$body = userProps.getJSONObject(FIELD_NAME_BODY);


        assertThat(_$body.has(BODY_NAME_REPOSITORY)).isFalse();
        assertThat(_$body.get(BODY_NAME_BRANCH)).isEqualTo(EMPTY_FIELD_VALUE);
        assertThat(_$body.has(BODY_NAME_CODE_TEXT)).isFalse();
        assertThat(_$body.has(BODY_NAME_ISSUE_ID)).isFalse();
    }

    @Test
    public void extractMeta_negative_case_null() throws JSONException {
        JSONObject j1 = extractMeta(null);
        assertThat(j1).isNull();
    }

    @Test
    public void extractMeta_should_return_meta() throws JSONException {
        JSONObject transformed = extractMeta(getTestShuttle().toJSONObject());
        assertThat(ShuttleProfilerValueChecker.hasMetaFields(transformed)).isTrue();
    }

    @Test
    public void test_hasMeta() throws JSONException {
        JSONObject shuttle = getTestShuttle().toJSONObject();
        JSONObject transformed = extractMeta(shuttle);
        JSONObject invalid = new JSONObject();

        assertThat(ShuttleProfilerValueChecker.hasMeta(transformed)).isTrue();
        assertThat(ShuttleProfilerValueChecker.hasMeta(invalid)).isFalse();
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

        assertThat(ShuttleProfilerValueChecker.hasKey(depth1, "key1", null)).isTrue();
        assertThat(ShuttleProfilerValueChecker.hasKey(depth1, "key2", null)).isTrue();
        assertThat(ShuttleProfilerValueChecker.hasKey(depth1, "key3", null)).isTrue();
        assertThat(ShuttleProfilerValueChecker.hasKey(depth1, "key4", null)).isFalse();
        assertThat(ShuttleProfilerValueChecker.hasKey(depth1, depth2Key, null)).isTrue();

        assertThat(ShuttleProfilerValueChecker.hasKey(depth2, "nested_key1", null)).isTrue();
        assertThat(ShuttleProfilerValueChecker.hasKey(depth2, "nested_key2", null)).isTrue();
        assertThat(ShuttleProfilerValueChecker.hasKey(depth2, "nested_key3", null)).isTrue();
        assertThat(ShuttleProfilerValueChecker.hasKey(depth2, "nested_key4", null)).isFalse();
    }


}
