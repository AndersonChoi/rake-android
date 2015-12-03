package com.rake.android.rkmetrics.metric.model;

import com.rake.android.rkmetrics.util.ExceptionUtil;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import org.json.JSONObject;

public abstract class Body {

    /** common */
    public static final String BODY_NAME_EXCEPTION_TYPE = "exception_type";
    public static final String BODY_NAME_STACKTRACE = "stacktrace";
    public static final String BODY_NAME_THREAD_INFO = "thread_info";

    /** instance members */
    protected String exception_type;
    protected String stacktrace;

    public Body setExceptionInfo(Throwable e) {
        if (null == e) return this;

        this.exception_type = ExceptionUtil.createExceptionType(e);
        this.stacktrace = ExceptionUtil.createStacktrace(e);
        return this;
    }

    protected JSONObject threadInfo;

    private static final ThreadLocal<RakeClientMetricSentinelShuttle> metricShuttles =
            new ThreadLocal<RakeClientMetricSentinelShuttle>() {
                @Override
                protected RakeClientMetricSentinelShuttle initialValue() {
                    return new RakeClientMetricSentinelShuttle();
                }
            };

    /**
     * @return null if the provided shuttle is NULL
     */
    public final boolean fillCommonBodyFields(RakeClientMetricSentinelShuttle shuttle) {
        if (null == shuttle) return false;

        shuttle.exception_type(exception_type);
        shuttle.stacktrace(stacktrace);
        // TODO thread INFO

        return true;
    }

    /** return initialized shuttle for this thread */
    public static final RakeClientMetricSentinelShuttle getEmptyShuttle() {

        RakeClientMetricSentinelShuttle shuttle = metricShuttles.get();
        shuttle = initializeShuttle(shuttle);

        return shuttle;
    }

    public static RakeClientMetricSentinelShuttle initializeShuttle(RakeClientMetricSentinelShuttle shuttle) {
        if (null == shuttle) return null;

        shuttle.action(null);
        shuttle.status(null);
        shuttle.clearBody();

        return shuttle;
    }

    /** abstract methods */
    public abstract JSONObject toJSONObject();
    public abstract String getMetricType();

}
