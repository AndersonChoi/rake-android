package com.rake.android.rkmetrics.metric.model;

import com.rake.android.rkmetrics.util.ExceptionUtil;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import org.json.JSONObject;

public abstract class Body {

    /** common */
    public static final String FIELD_NAME_EXCEPTION_TYPE = "exception_type";
    public static final String FIELD_NAME_STACKTRACE = "stacktrace";
    public static final String FIELD_NAME_THREAD_INFO = "thread_info";

    /** for Action.FLUSH */
    public static final String FIELD_NAME_OPERATION_TIME = "operation_time";

    /** for Action.TRACK */
    public static final String FIELD_NAME_TRACK_OPERATION_COUNT = "track_operation_count";
    public static final String FIELD_NAME_TRACK_OPERATION_TIME_LIST = "track_operation_time_list";
    public static final String FIELD_NAME_TRACKED_LOG_SIZE_LIST = "tracked_log_size_list";


    /** instance members */
    protected String exception_type;
    protected String stacktrace;

    /** getter, setter */
    public String getExceptionType() { return exception_type; }
    public String getStacktrace() { return stacktrace; }

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

    public static final RakeClientMetricSentinelShuttle getShuttle() {
        return metricShuttles.get();
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

}
