package com.rake.android.rkmetrics.shuttle;

import android.app.Application;

import static org.assertj.core.api.Assertions.*;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.*;

import com.rake.android.rkmetrics.RakeAPI;
import com.rake.android.rkmetrics.TestUtil;
import com.skplanet.pdp.sentinel.shuttle.RakeClientTestSentinelShuttle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ShuttleProfilerSpecHelper {
    // RakeClientTest Header Fields
    public static String HEADER_NAME_SESSION_ID = "session_id";
    public static String HEADER_NAME_LOG_SOURCE = "log_source";
    public static String HEADER_NAME_TRANSACTION_ID = "transaction_id";
    public static String HEADER_NAME_APP_PACKAGE = "app_package";

    public static Set<String> requiredHeaderFieldsForTestShuttle = new HashSet<>(Arrays.asList(
            HEADER_NAME_SESSION_ID,
            HEADER_NAME_LOG_SOURCE,
            HEADER_NAME_TRANSACTION_ID,
            HEADER_NAME_APP_PACKAGE
    ));

    public static String BODY_NAME_REPOSITORY = "repository";
    public static String BODY_NAME_BRANCH = "branch";
    public static String BODY_NAME_CODE_TEXT = "code_text";
    public static String BODY_NAME_ISSUE_ID = "issue_id";

    public static Set<String> requiredBodyFieldsForTestShuttle = new HashSet<>(Arrays.asList(
            BODY_NAME_REPOSITORY,
            BODY_NAME_BRANCH,
            BODY_NAME_CODE_TEXT,
            BODY_NAME_ISSUE_ID
    ));

    public static JSONObject getShuttleWithMissingField(String depth1, String depth2)
            throws JSONException {

        JSONObject shuttle = getTestShuttle().toJSONObject();

        if (null == depth2) {
            shuttle.remove(depth1);
            return shuttle;
        }

        JSONObject container = shuttle.getJSONObject(depth1);
        container.remove(depth2);

        return shuttle;
    }

    public static JSONObject getDefaultPropsForTest(Application app) throws JSONException {
        return RakeAPI.getDefaultProps(app, RakeAPI.Env.DEV, TestUtil.genToken(), new Date());
    }

    public static JSONObject getMergedPropsWithEmptySuperPropsForTest(Application app,
                                                                      JSONObject userProps)
            throws JSONException {

        JSONObject superProps = new JSONObject();
        return getMergedPropsWithSuperPropsForTest(app, superProps, userProps);
    }

    public static JSONObject getMergedPropsWithSuperPropsForTest(Application app,
                                                                 JSONObject superProps,
                                                                 JSONObject userProps)
            throws JSONException {

        assertThat(superProps).isNotNull();
        assertThat(userProps).isNotNull();
        assertThat(isShuttle(userProps)).isTrue();

        JSONObject meta = extractMeta(userProps);
        JSONObject fieldOrder = meta.getJSONObject(META_FIELD_NAME_FIELD_ORDER);
        JSONObject defaultProps = getDefaultPropsForTest(app);

        return mergeProps(fieldOrder, userProps, superProps, defaultProps);
    }

    public static void assertRequiredHeaderAndBodyFieldsForTestShuttle() throws JSONException {

        JSONObject userProps = getTestShuttle().toJSONObject();

        for(String header : requiredHeaderFieldsForTestShuttle) {
            assertThat(userProps.has(header));
            userProps.get(header);
        }
    }

    public static RakeClientTestSentinelShuttle getTestShuttle() throws JSONException {

        RakeClientTestSentinelShuttle shuttle = new RakeClientTestSentinelShuttle();

        return shuttle;
    }

    /** assertion for generator specific test */
    public static void assertShuttleGeneratorVersion() throws JSONException {
        String logVersion = getTestShuttle().toJSONObject().getString(PROPERTY_NAME_LOG_VERSION);
        String generatorVersion = Arrays.asList(logVersion.split(":")).get(1);

        assertThat(generatorVersion).isEqualTo("1.6.5");
    }

    /** assertion for schema specific test */
    public static void assertShuttleSchemaVersion() throws JSONException {

        assertRequiredHeaderAndBodyFieldsForTestShuttle();

        String logVersion = getTestShuttle().toJSONObject().getString(PROPERTY_NAME_LOG_VERSION);
        String schemaVersion = Arrays.asList(logVersion.split(":")).get(2);

        assertThat(schemaVersion).isEqualTo("1");
    }
}
