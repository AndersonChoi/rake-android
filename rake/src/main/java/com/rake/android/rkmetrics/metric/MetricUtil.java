package com.rake.android.rkmetrics.metric;

import android.content.Context;

import com.rake.android.rkmetrics.RakeAPI;
import com.rake.android.rkmetrics.metric.model.Action;
import com.rake.android.rkmetrics.metric.model.Body;
import com.rake.android.rkmetrics.metric.model.EmptyMetric;
import com.rake.android.rkmetrics.metric.model.FlushMetric;
import com.rake.android.rkmetrics.metric.model.FlushType;
import com.rake.android.rkmetrics.metric.model.Header;
import com.rake.android.rkmetrics.metric.model.InstallMetric;
import com.rake.android.rkmetrics.metric.model.Status;
import com.rake.android.rkmetrics.network.Endpoint;
import com.rake.android.rkmetrics.network.ServerResponse;
import com.rake.android.rkmetrics.persistent.Log;
import com.rake.android.rkmetrics.persistent.LogChunk;
import com.rake.android.rkmetrics.persistent.LogTableAdapter;
import com.rake.android.rkmetrics.shuttle.ShuttleProfiler;
import com.rake.android.rkmetrics.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


/**
 * 클래스 이름을 바꿀 경우 build.gradle 내에 빌드 변수 또한 변경해야 함
 */
public final class MetricUtil {

    private MetricUtil() {}

    /**
     * 소스 코드에 LIVE TOKEN 을 노출하지 않기 위해서 TOKEN 값을 빌드타임에 환경변수에서 읽어와 덮어쓴다
     * `build.gradle` 과 `MetricUtilTokenSpec.java` 를 참조할 것
     *
     * 후에 `release` 브랜치에서 LIVE TOKEN 이 기록되었는지 크로스 체크를 위해 BUILD_CONSTANT_BRANCH 값을 이용한다.
     * `release` 브랜치일 경우에만 BUILD_CONSTANT_ENV 값이 Env.LIVE 이고 나머지의 경우에는 Env.DEV 여야 한다.
     *
     * 아래의 변수 이름, *스페이스바*, 변수 값 어느 하나라도 변경시 build.gradle 상수와
     * updateMetricToken, getRakeEnv 함수 내의 정규식도 변경해야 함.
     */
    public static final String BUILD_CONSTANT_BRANCH = "release/0.4.3";
    public static final String BUILD_CONSTANT_METRIC_TOKEN = "df234e764a5e4c3beaa7831d5b8ad353149495ac";
    public static final RakeAPI.Env BUILD_CONSTANT_ENV = RakeAPI.Env.DEV;


    public static final String TRANSACTION_ID = createTransactionId();
    public static final String EMPTY_TOKEN = null;

    public static String getURI() {
        return Endpoint.CHARGED.getURI(BUILD_CONSTANT_ENV);
    }

    public static String createTransactionId() {
        StringBuilder sb = new StringBuilder();

        String u1 = java.util.UUID.randomUUID().toString();
        String u2 = java.util.UUID.randomUUID().toString();
        String u3 = java.util.UUID.randomUUID().toString();

        sb.append(u1);
        sb.append(u2);
        sb.append(u3);

        return sb.toString().replaceAll("-", "");
    }

    public static boolean isNotMetricToken(String token) {
        return !isMetricToken(token);
    }

    public static boolean isMetricToken(String token) {
        return BUILD_CONSTANT_METRIC_TOKEN.equals(token);
    }

    /** @return true if log was successfully persisted otherwise returns false */
    public static boolean recordErrorMetric(Context context, Action action, String token, Throwable e) {
        if (null == context) {
            Logger.e("Can't record ErrorStatusMetric using NULL args");
            return false;
        }

        EmptyMetric metric = new EmptyMetric();
        metric.setHeader(Header.create(context, action, Status.ERROR, token));
        metric.setExceptionInfo(e);

        return recordMetric(context, metric);
    }

    public static boolean recordInstallErrorMetric(Context context, RakeAPI.Env env, String endpoint,
                                                   String token, Throwable e) {
        if (null == context) {
            Logger.e("Can't record InstallErrorMetric using NULL context");
            return false;
        }

        InstallMetric metric = new InstallMetric();

        Header h = Header.create(context, Action.INSTALL, Status.ERROR, token);
        int persistedLogCount = LogTableAdapter.getInstance(context).getCount(metric.getServiceToken());

        metric.setHeader(h).setExceptionInfo(e);
        metric.setEnv(env).setEndpoint(endpoint).setPersistedLogCount(persistedLogCount);

        return recordMetric(context, metric);
    }

    /** @return true if log was successfully persisted otherwise returns false */
    public static boolean recordFlushMetric(Context context,
                                            FlushType flushType,
                                            long operationTime,
                                            LogChunk chunk,
                                            ServerResponse response){

        if (null == context
                || null == chunk
                || null == response
                || null == response.getFlushStatus()) {
            Logger.e("Can't record FlushMetric using NULL args");
            return false;
        }

        /** 메트릭 토큰에 flush 메트릭은 기록하지 않음, MessageLoop 내부에서 필터링 하고 있으나 나중을 위해 방어로직을 추가 */
        if (MetricUtil.isMetricToken(chunk.getToken())) return false;

        /** Error Response 일 경우에만 기록 RAKE-429 */
        if (!response.isErrorResponse()) return false;

        FlushMetric metric = new FlushMetric();

        Header header = Header.create(context, Action.FLUSH, response.getFlushStatus(), chunk.getToken());

        /** set HEADER */
        metric.setHeader(header);

        /** set COMMON BODY */
        metric.setExceptionInfo(response.getExceptionInfo());

        metric.setFlushType(flushType)
                .setEndpoint(chunk.getUrl())
                .setOperationTime(operationTime)
                .setLogCount(Long.valueOf(chunk.getCount()))
                .setLogSize(Long.valueOf(chunk.getChunk().getBytes().length))
                .setServerResponseBody(response.getResponseBody())
                .setServerResponseCode(Long.valueOf(response.getResponseCode()))
                .setServerResponseTime(response.getServerResponseTime())
                .setFlushMethod(response.getFlushMethod());

        return recordMetric(context, metric);
    }

    /**
     * @return true if log was successfully persisted otherwise returns false
     */
    public static boolean recordMetric(Context context, Body metric) {
        JSONObject validShuttle = createValidShuttleForMetric(metric, context);

        Log log = Log.create(
                MetricUtil.getURI(), MetricUtil.BUILD_CONSTANT_METRIC_TOKEN, validShuttle);

        int count = LogTableAdapter.getInstance(context).addLog(log);

        boolean recorded = (count == -1) ? false : true;

        if (recorded && null != metric)
            Logger.t(String.format("[METRIC] Record ACTION:STATUS [%s]", metric.getMetricType()));

        return recorded;
    }

    public static JSONObject createValidShuttleForMetric(Body metric, Context context) {
        if (null == metric) return null;

        JSONObject userProps = metric.toJSONObject();
        JSONObject defaultProps = createDefaultPropsForMetric(context);
        JSONObject validShuttle = ShuttleProfiler.createValidShuttle(userProps, null, defaultProps);

        return validShuttle;
    }

    public static JSONObject createDefaultPropsForMetric(Context context) {
        JSONObject defaultProps = null;
        try {
            defaultProps = RakeAPI.getDefaultProps(
                    context, BUILD_CONSTANT_ENV, BUILD_CONSTANT_METRIC_TOKEN, new Date());
        } catch (JSONException e) {
            Logger.e("Can't create defaultProps for metric");
        }

        return defaultProps;
    }
}
