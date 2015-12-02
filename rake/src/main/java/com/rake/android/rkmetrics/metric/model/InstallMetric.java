package com.rake.android.rkmetrics.metric.model;

import com.rake.android.rkmetrics.util.Logger;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import org.json.JSONObject;

public class InstallMetric extends Body {

    private Header header;
    private Long operation_time;
    private String mode;
    private String database_version;
    private Long persisted_log_count;
    private Long expired_log_count;

    public InstallMetric setHeader(Header header) {
        this.header = header; return this;
    }

    public InstallMetric setOperationTime(Long operation_time) {
        this.operation_time = operation_time; return this;
    }

    public InstallMetric setMode(String mode) {
        this.mode = mode; return this;
    }

    public InstallMetric setDatabaseVersion(String database_version) {
        this.database_version = database_version; return this;
    }

    public InstallMetric setPersistedLogCount(long persisted_log_count) {
        this.persisted_log_count = persisted_log_count; return this;
    }

    public InstallMetric setExpieredLogCount(long expired_log_count) {
        this.expired_log_count = expired_log_count; return this;
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
                .mode(mode)
                .database_version(database_version)
                .persisted_log_count(persisted_log_count)
                .expired_log_count(expired_log_count);

        return shuttle.toJSONObject();
    }
}
