package com.rake.android.rkmetrics.metric.model;

import com.rake.android.rkmetrics.util.Logger;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import org.json.JSONObject;

public final class FlushMetric extends Body {

    private Header header;
    private Long operation_time;
    private Long log_count;
    private Long log_size;
    private String flush_type;
    private Long server_response_time;
    private Long server_response_code;
    private String server_response_body;

    public FlushMetric(Header header) {
        this.header = header;
    }

    public void setOperation_time(Long operationTime) { this.operation_time = operationTime; }
    public void setLogSize(Long logSizeAsBytes) { this.log_size = logSizeAsBytes; }
    public void setLogCount(Long logCount) { this.log_count = logCount; }
    public void setFlushType(FlushType flushType) {
        if (null == flushType) return;

        this.flush_type = flushType.getValue();
    }
    public void setServerReponseBody(String responseBody) { this.server_response_body = responseBody; }
    public void setServerResponseCode(Long responseCode) { this.server_response_code = responseCode; }
    public void setServerResponseTime(Long responseTime) { this.server_response_time = responseTime; }


    @Override
    public JSONObject toJSONObject() {
        RakeClientMetricSentinelShuttle shuttle = getShuttle();
        initializeShuttle(shuttle);

        if (null == shuttle) {
            Logger.e("Null shuttle returned from getShuttle()");
            return null;
        }

        if (null != header) {
            /* header */
            shuttle
                    .action(header.getAction())
                    .status(header.getStatus())
                    .app_package(header.getAppPackage())
                    .transaction_id(header.getTransactionId())
                    .service_token((header.getServiceToken()));

            /* common body */
            shuttle
                    .exception_type(getExceptionType())
                    .stacktrace(getStacktrace()); // TODO getThreadInfo

            /* specific body */
            shuttle
                    .operation_time(operation_time)
                    .log_count(log_count)
                    .log_size(log_size)
                    .flush_type(flush_type)
                    .server_response_body(server_response_body)
                    .server_response_code(server_response_code)
                    .server_response_time(server_response_time);
        }

        return shuttle.toJSONObject();
    }
}
