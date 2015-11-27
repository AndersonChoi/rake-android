package com.rake.android.rkmetrics.metric;

import static com.rake.android.rkmetrics.config.RakeConfig.LOG_TAG_PREFIX;

import android.app.Application;

import com.rake.android.rkmetrics.RakeAPI;
import com.rake.android.rkmetrics.util.RakeLogger;
import com.rake.android.rkmetrics.util.functional.Callback;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.EmptyStackException;

public final class MetricLogger { /* singleton */
    /**
     * 소스 코드에 LIVE TOKEN 을 노출하지 않기 위해서 TOKEN 값을 빌드타임에 환경변수에서 읽어와 덮어쓴다
     * `build.gradle` 과 `MetricLoggerTokenSpec.java` 를 참조할 것
     *
     * 후에 `release` 브랜치에서 LIVE TOKEN 이 기록되었는지 크로스 체크를 위해 BUILD_CONSTANT_BRANCH 값을 이용한다.
     * `release` 브랜치일 경우에만 BUILD_CONSTANT_ENV 값이 Env.LIVE 이고 나머지의 경우에는 Env.DEV 여야 한다.
     *
     * 아래의 변수 이름, 스페이스바, 변수 값 어느 하나라도 변경시 build.gradle 상수와
     * updateMetricToken, getRakeEnv 함수 내의 정규식도 변경해야 함.
     */
    public static final String BUILD_CONSTANT_METRIC_TOKEN = "df234e764a5e4c3beaa7831d5b8ad353149495ac";
    public static final String BUILD_CONSTANT_BRANCH = "feature/RAKE-383-metric";
    public static final RakeAPI.Env BUILD_CONSTANT_ENV = RakeAPI.Env.DEV;

    /**
     * constants
     */
    public static final String FIELD_NAME_EXCEPTION_TYPE = "exception_type";
    public static final String FIELD_NAME_STACKTRACE_STACKTRACE = "stacktrace";

    private RakeAPI rake; /* 테스트를 위해 패키지 범위 */

    private MetricLogger(Application app) {
        rake = RakeAPI.getInstance(
                app.getApplicationContext(),
                BUILD_CONSTANT_METRIC_TOKEN,
                BUILD_CONSTANT_ENV,
                (BUILD_CONSTANT_ENV == RakeAPI.Env.DEV) ? RakeAPI.Logging.ENABLE : RakeAPI.Logging.DISABLE);
    }

    /**
     * static members
     */

    private static MetricLogger instance;

    public static synchronized MetricLogger getInstance(Application app) {
        if (null == app) {
            RakeLogger.e(LOG_TAG_PREFIX, "Can't initialize MetricLogger using null Application");
            return null;
        }

        if (null == instance) instance = new MetricLogger(app);

        return instance;
    }

    private static final ThreadLocal<RakeClientMetricSentinelShuttle> metricShuttles =
            new ThreadLocal<RakeClientMetricSentinelShuttle>() {
                @Override
                protected RakeClientMetricSentinelShuttle initialValue() {
                    return new RakeClientMetricSentinelShuttle();
                }
            };


    /**
     * instance members
     */

    public RakeClientMetricSentinelShuttle write(
            Callback<RakeClientMetricSentinelShuttle, Action> callback) {

        RakeClientMetricSentinelShuttle shuttle = metricShuttles.get();
        shuttle = initializeShuttle(shuttle);

        Action action = Action.EMPTY;

        try {
            /**
             * action 필드는 callback 내부에서 기록하게 되어있으나 다음의 경우를 위해 리턴하도록 함
             * - 못잡은 예외를 Action.Empty 값으로 기록하기 위해
             * - 리턴된 Action 값을 활용해 Shuttle 에 추가적인 값을 기록하기 위해
             */
            action = callback.execute(shuttle);
        } catch (Exception e) {

            shuttle.setBodyOf__ERROR(
                    getExceptionType(e), /* exception_type*/
                    getStacktraceString(e), /* stacktrace */
                    null  /* thread_info */
            );

            shuttle.action(action.getValue());

            RakeLogger.e(LOG_TAG_PREFIX, "Uncaught exception", e);
        }

        return shuttle;
    }

    public static RakeClientMetricSentinelShuttle initializeShuttle(RakeClientMetricSentinelShuttle shuttle) {
        shuttle.action(null);
        shuttle.status(null);
        shuttle.clearBody();

        return shuttle;
    }

    public static String getExceptionType(Throwable e) {
        if (null == e) return null;

        return e.getClass().getSimpleName();
    }

    public static String getStacktraceString(Throwable e) {
        if (null == e) return null;

        StringWriter sw = new StringWriter();
        PrintWriter  pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        return pw.toString();
    }

    /**
     * package functions to support test
     */

    /* package */ static void initialize() {
        warning();

        if (null != instance)
            if (null != instance.rake) instance.rake = null;

        instance = null;
    }

    /* package */ RakeAPI getRake() {
        warning();
        return rake;
    }

    private static void warning() {
        RakeLogger.e(LOG_TAG_PREFIX, "DO NOT USE THIS FUNCTION IN PRODUCTION");
    }
}
