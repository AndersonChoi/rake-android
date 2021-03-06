package com.rake.android.rkmetrics.metric;

import android.content.Context;

import com.rake.android.rkmetrics.RakeAPI;
import com.rake.android.rkmetrics.android.SystemInformation;
import com.rake.android.rkmetrics.db.LogTable;
import com.rake.android.rkmetrics.db.log.Log;
import com.rake.android.rkmetrics.db.log.LogBundle;
import com.rake.android.rkmetrics.metric.model.Action;
import com.rake.android.rkmetrics.metric.model.Metric;
import com.rake.android.rkmetrics.metric.model.Status;
import com.rake.android.rkmetrics.network.Endpoint;
import com.rake.android.rkmetrics.network.HttpRequestSender;
import com.rake.android.rkmetrics.network.HttpResponse;
import com.rake.android.rkmetrics.shuttle.ShuttleProfiler;
import com.rake.android.rkmetrics.util.Logger;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


/**
 * 클래스 이름을 바꿀 경우 build.gradle 내에 빌드 변수 또한 변경해야 함
 */
public final class MetricUtil {

    private MetricUtil() {
    }

    /**
     * 소스 코드에 LIVE TOKEN 을 노출하지 않기 위해서 TOKEN 값을 빌드타임에 환경변수에서 읽어와 덮어쓴다
     * `build.gradle` 과 `MetricUtilTokenSpec.java` 를 참조할 것
     * <p>
     * 후에 `release` 브랜치에서 LIVE TOKEN 이 기록되었는지 크로스 체크를 위해 BUILD_CONSTANT_BRANCH 값을 이용한다.
     * `release` 브랜치일 경우에만 BUILD_CONSTANT_ENV 값이 Env.LIVE 이고 나머지의 경우에는 Env.DEV 여야 한다.
     * <p>
     * 아래의 변수 이름, *스페이스바*, 변수 값 어느 하나라도 변경시 build.gradle 상수와
     * updateMetricToken, getRakeEnv 함수 내의 정규식도 변경해야 함.
     */
    public static final String BUILD_CONSTANT_BRANCH = "release/0.6.0";
    public static final String BUILD_CONSTANT_METRIC_TOKEN = "d839ca7a5875c8ac304d465bfcfd78d95b4726eb";
    static final RakeAPI.Env BUILD_CONSTANT_ENV = RakeAPI.Env.LIVE;


    static final String TRANSACTION_ID = createTransactionId();
    public static final String EMPTY_TOKEN = null;

    static String getURI(Context context) {
        return new Endpoint(context, BUILD_CONSTANT_ENV).getURI();
    }

    static String createTransactionId() {
        StringBuilder sb = new StringBuilder();

        String u1 = java.util.UUID.randomUUID().toString();
        String u2 = java.util.UUID.randomUUID().toString();
        String u3 = java.util.UUID.randomUUID().toString();

        sb.append(u1);
        sb.append(u2);
        sb.append(u3);

        return sb.toString().replaceAll("-", "");
    }

    static boolean isMetricToken(String token) {
        return BUILD_CONSTANT_METRIC_TOKEN.equals(token);
    }

    /**
     * @return true if log was successfully persisted otherwise returns false
     */
    public static boolean recordErrorMetric(Context context, Action action, String token, Throwable e) {
        if (null == context) {
            Logger.e("Can't record ErrorStatusMetric using NULL context");
            return false;
        }

        Metric errorMetric = fillMetricHeaderValues(context, action, Status.ERROR, token)
                .setBodyExceptionInfo(e)
                .build();

        return trackMetric(context, errorMetric);
    }

    public static boolean recordInstallErrorMetric(Context context, RakeAPI.Env env, String endpoint,
                                                   String token, Throwable e) {
        if (null == context) {
            Logger.e("Can't record InstallErrorMetric using NULL context");
            return false;
        }

        long persistedLogCount = LogTable.getInstance(context).getCount(token);

        Metric installErrorMetric = fillMetricHeaderValues(context, Action.INSTALL, Status.ERROR, token)
                .setBodyExceptionInfo(e)
                .setBodyEnv(env)
                .setBodyEndpoint(endpoint)
                .setBodyPersistedLogCount(persistedLogCount)
                .build();

        return trackMetric(context, installErrorMetric);
    }

    /**
     * @return true if log was successfully persisted otherwise returns false
     */
    public static boolean recordFlushMetric(Context context,
                                            String flushType,
                                            long operationTime,
                                            LogBundle logBundle,
                                            HttpResponse response) {

        if (null == context
                || null == logBundle
                || null == response
                || null == response.getFlushStatus()) {
            Logger.e("Can't record FlushMetric using NULL args");
            return false;
        }

        /* 메트릭 토큰에 서비스 토큰에 의한 flush 메트릭은 기록하지 않음, MessageLoop 내부에서 필터링 하고 있으나 나중을 위해 방어로직을 추가 */
        if (MetricUtil.isMetricToken(logBundle.getToken())) {
            return false;
        }

        /* Error Response 일 경우에만 기록 RAKE-429 */
        if (!response.isErrorResponse()) {
            return false;
        }


        Metric flushMetric = fillMetricHeaderValues(context, Action.FLUSH, response.getFlushStatus(), logBundle.getToken())
                .setBodyExceptionInfo(response.getException())
                .setBodyFlushType(flushType)
                .setBodyEndpoint(logBundle.getUrl())
                .setBodyOperationTime(operationTime)
                .setBodyLogCount((long) logBundle.getCount())
                .setBodyLogSize((long) logBundle.getLogsByJSONString().getBytes().length)
                .setBodyServerResponseBody(response.getResponseBody())
                .setBodyServerResponseCode((long) response.getResponseCode())
                .setBodyServerResponseTime(response.getResponseTime())
                .setBodyRakeProtocolVersion(HttpRequestSender.RAKE_PROTOCOL_VERSION)

                // (2017.11) Rake-Android 사용 앱들의 ICS(Android 4.0, versionCode = 14) 미만 단말 지원 종료.
                .setBodyFlushMethod("HTTP_URL_CONNECTION")
                .build();


        return trackMetric(context, flushMetric);
    }

    /**
     * @return fill header column values
     */
    private static Metric.Builder fillMetricHeaderValues(Context context, Action action, Status status, String token) {
        return new Metric.Builder(new RakeClientMetricSentinelShuttle())
                .setHeaderAction(action)
                .setHeaderStatus(status)
                .setHeaderAppPackage(SystemInformation.getPackageName(context))
                .setHeaderTransactionId(MetricUtil.TRANSACTION_ID)
                .setHeaderServiceToken(token);
    }

    /**
     * @return true if log was successfully persisted otherwise returns false
     */
    private static boolean trackMetric(Context context, Metric metric) {
        if (metric == null) {
            return false;
        }

        try {
            JSONObject userProps = metric.toJSONObject();
            JSONObject autoProps = RakeAPI.getDefaultProps(context, null, BUILD_CONSTANT_METRIC_TOKEN, new Date());
            JSONObject validShuttle = ShuttleProfiler.createValidShuttle(userProps, null, autoProps);

            Log log = new Log(MetricUtil.getURI(context), MetricUtil.BUILD_CONSTANT_METRIC_TOKEN, validShuttle);

            long count = LogTable.getInstance(context).addLog(log);

            boolean recorded = count != -1;

            if (recorded) {
                Logger.t(String.format("[METRIC] Record ACTION:STATUS [%s]", metric.getMetricType()));
            }

            return recorded;
        } catch (JSONException e) {
            Logger.e("Can't create autoProps for metric");
        }

        return false;
    }
}
