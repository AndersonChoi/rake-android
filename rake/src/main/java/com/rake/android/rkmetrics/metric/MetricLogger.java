package com.rake.android.rkmetrics.metric;

import static com.rake.android.rkmetrics.config.RakeConfig.LOG_TAG_PREFIX;

import android.app.Application;

import com.rake.android.rkmetrics.RakeAPI;
import com.rake.android.rkmetrics.util.RakeLogger;
import com.rake.android.rkmetrics.util.functional.Callback;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;


import java.io.PrintWriter;
import java.io.StringWriter;

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

    public RakeAPI rake;

    private MetricLogger(Application app) {

        if (null == app) {
            RakeLogger.e(LOG_TAG_PREFIX, "Can't initialize MetricLogger using null Application");
            return;
        }

        rake = RakeAPI.getInstance(
                app.getApplicationContext(),
                BUILD_CONSTANT_METRIC_TOKEN,
                BUILD_CONSTANT_ENV,
                (BUILD_CONSTANT_ENV == RakeAPI.Env.DEV) ? RakeAPI.Logging.ENABLE : RakeAPI.Logging.DISABLE);
    }
    public static final ThreadLocal<RakeClientMetricSentinelShuttle> metricShuttles =
            new ThreadLocal<RakeClientMetricSentinelShuttle>() {
                @Override
                protected RakeClientMetricSentinelShuttle initialValue() {
                    return new RakeClientMetricSentinelShuttle();
                }
            };

    /**
     * static members
     */
    private static MetricLogger instance;

    public static synchronized MetricLogger getInstance(Application app) {
        if (null == instance) instance = new MetricLogger(app);

        return instance;
    }

    /**
     * instance members
     */

    public void write(Callback<RakeClientMetricSentinelShuttle, Void> callback) {

        try {
            callback.execute(metricShuttles.get());
        } catch (Exception e) {
            RakeClientMetricSentinelShuttle shuttle = metricShuttles.get();

            StringWriter sw = new StringWriter();
            PrintWriter  pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            shuttle.setBodyOf__ERROR(
                    e.getClass().getSimpleName(), /* exception_type*/
                    sw.toString(), /* stacktrace */
                    null  /* thread_info */
            );

            // TODO send to rake
        }
    }
}
