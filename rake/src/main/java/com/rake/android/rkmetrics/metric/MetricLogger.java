package com.rake.android.rkmetrics.metric;

import android.content.Context;

import com.rake.android.rkmetrics.RakeAPI;
import com.rake.android.rkmetrics.metric.model.Action;
import com.rake.android.rkmetrics.metric.model.Body;
import com.rake.android.rkmetrics.metric.model.FlushType;
import com.rake.android.rkmetrics.util.Logger;
import com.rake.android.rkmetrics.util.functional.Callback;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;


/**
 * 클래스 이름을 바꿀 경우 build.gradle 내에 빌드 변수 또한 변경해야 함
 */
public final class MetricLogger { /* singleton */
    /**
     * 소스 코드에 LIVE TOKEN 을 노출하지 않기 위해서 TOKEN 값을 빌드타임에 환경변수에서 읽어와 덮어쓴다
     * `build.gradle` 과 `MetricLoggerTokenSpec.java` 를 참조할 것
     *
     * 후에 `release` 브랜치에서 LIVE TOKEN 이 기록되었는지 크로스 체크를 위해 BUILD_CONSTANT_BRANCH 값을 이용한다.
     * `release` 브랜치일 경우에만 BUILD_CONSTANT_ENV 값이 Env.LIVE 이고 나머지의 경우에는 Env.DEV 여야 한다.
     *
     * 아래의 변수 이름, *스페이스바*, 변수 값 어느 하나라도 변경시 build.gradle 상수와
     * updateMetricToken, getRakeEnv 함수 내의 정규식도 변경해야 함.
     */
    public static final String BUILD_CONSTANT_BRANCH = "feature/RAKE-383-metric";
    public static final String BUILD_CONSTANT_METRIC_TOKEN = "df234e764a5e4c3beaa7831d5b8ad353149495ac";
    public static final RakeAPI.Env BUILD_CONSTANT_ENV = RakeAPI.Env.DEV;

    /**
     * constants
     *
     * FIELD_NAME_* 들은 스키마에서 가져온 변수 이름으로, 스키마 변경시 업데이트 해 주어야 함.
     */

    private RakeAPI rake;

    private MetricLogger(Context context) {
        rake = RakeAPI.getInstance(
                context,
                BUILD_CONSTANT_METRIC_TOKEN,
                BUILD_CONSTANT_ENV,
                (BUILD_CONSTANT_ENV == RakeAPI.Env.DEV) ? RakeAPI.Logging.ENABLE : RakeAPI.Logging.DISABLE);
    }

    /**
     * static members
     */

    private static MetricLogger instance;

    public static synchronized MetricLogger getInstance(Context context) {
        if (null == context) {
            Logger.e("Can't initialize MetricLogger using null Application");
            return null;
        }

        if (null == instance) instance = new MetricLogger(context);

        return instance;
    }

    private static final ThreadLocal<RakeClientMetricSentinelShuttle> metricShuttles =
            new ThreadLocal<RakeClientMetricSentinelShuttle>() {
                @Override
                protected RakeClientMetricSentinelShuttle initialValue() {
                    return new RakeClientMetricSentinelShuttle();
                }
            };

    public static RakeClientMetricSentinelShuttle initializeShuttle(RakeClientMetricSentinelShuttle shuttle) {
        shuttle.action(null);
        shuttle.status(null);
        shuttle.clearBody();

        return shuttle;
    }

    /**
     * instance members
     */

    public RakeClientMetricSentinelShuttle measureFlush(
        Callback<RakeClientMetricSentinelShuttle, Void> callback,
        FlushType flushType) {
        RakeClientMetricSentinelShuttle shuttle = metricShuttles.get();

        initializeShuttle(shuttle);

        shuttle.action(Action.FLUSH.getValue());
        shuttle.flush_type(flushType.getValue());

        return measureOperationTime(shuttle, callback);
    }

    public static RakeClientMetricSentinelShuttle measureOperationTime(
            RakeClientMetricSentinelShuttle shuttle,
            Callback<RakeClientMetricSentinelShuttle, Void> callback) {

        try {
            long startAt = System.currentTimeMillis();
            callback.execute(shuttle);
            long endAt = System.currentTimeMillis();

            shuttle.operation_time(endAt - startAt);

        } catch (Exception e) {

            shuttle.setBodyOf__ERROR(
                    Body.createExceptionType(e), /* exception_type*/
                    Body.createStacktrace(e), /* stacktrace */
                    null  /* thread_info */
            );

            Logger.e("Uncaught exception", e);
        }

        return shuttle;
    }

    public RakeAPI getRakeAPI() { return rake; }

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
        Logger.e("[CRITICAL] DO NOT USE THIS FUNCTION IN PRODUCTION");
    }
}
