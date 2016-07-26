package com.rake.android.rkmetrics.metric.model;

import com.rake.android.rkmetrics.network.FlushMethod;
import com.rake.android.rkmetrics.network.RakeProtocolV2;
import com.rake.android.rkmetrics.util.Logger;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import org.json.JSONObject;

public final class FlushMetric extends Body {

    public FlushMetric() {}

    private String endpoint;
    private Long operation_time;
    private Long log_count;
    private Long log_size;
    private String flush_type;
    private Long server_response_time;
    private Long server_response_code;
    private String server_response_body;
    private String flush_method;

    /** setter */

    public FlushMetric setOperationTime(Long operationTime) {
        this.operation_time = operationTime; return this;
    }
    public FlushMetric setLogSize(Long logSizeAsBytes) {
        this.log_size = logSizeAsBytes; return this;
    }
    public FlushMetric setLogCount(Long logCount) {
        this.log_count = logCount; return this;
    }
    public FlushMetric setFlushType(FlushType flushType) {
        if (null != flushType) this.flush_type = flushType.getValue(); return this;
    }
    public FlushMetric setEndpoint(String endpoint) {
        if (null != endpoint) this.endpoint = endpoint; return this;
    }
    public FlushMetric setServerResponseBody(String responseBody) {
        this.server_response_body = responseBody; return this;
    }
    public FlushMetric setServerResponseCode(Long responseCode) {
        this.server_response_code = responseCode; return this;
    }
    public FlushMetric setServerResponseTime(Long responseTime) {
        this.server_response_time = responseTime; return this;
    }
    public FlushMetric setFlushMethod(FlushMethod flushMethod) {
        if (null != flushMethod) this.flush_method = flushMethod.getValue(); return this;
    }


    @Override
    public JSONObject toJSONObject() {
        RakeClientMetricSentinelShuttle shuttle = getEmptyShuttle();

        if (null == shuttle) {
            Logger.e("NULL shuttle returned from getEmptyShuttle()");
            return null;
        }

        if (null != header) header.fillShuttle(shuttle);
        fillCommonBodyFields(shuttle);

        /* specific body */
        shuttle
                .operation_time(operation_time)
                .log_count(log_count)
                .log_size(log_size)
                .flush_type(flush_type)
                .endpoint(endpoint)
                .server_response_body(server_response_body)
                .server_response_code(server_response_code)
                .server_response_time(server_response_time)
                .flush_method(flush_method)
                .rake_protocol_version(RakeProtocolV2.RAKE_PROTOCOL_VERSION);

        return shuttle.toJSONObject();
    }

    @Override public String getMetricType() {
        String status = null;
        if (null != header) status = header.getStatus();

        status = (null == status) ? Status.UNKNOWN.getValue() : status;

        return Action.FLUSH.getValue() + ":" + status;
    }
}
