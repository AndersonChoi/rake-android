package com.rake.android.rkmetrics.metric.model;

import com.rake.android.rkmetrics.RakeAPI;
import com.rake.android.rkmetrics.network.Endpoint;
import com.rake.android.rkmetrics.util.Logger;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import org.json.JSONObject;

public final class InstallMetric extends Body {

    private Header header;
    private Long operation_time;
    private String env;
    private Long database_version;
    private Long persisted_log_count;
    private Long expired_log_count;
    private String endpoint;

    public String getServiceToken() {
        if (null == header) return null;

        return header.getServiceToken();
    }

    public InstallMetric setHeader(Header header) {
        this.header = header; return this;
    }

    public InstallMetric setOperationTime(Long operation_time) {
        this.operation_time = operation_time; return this;
    }

    public InstallMetric setEnv(RakeAPI.Env env) {
        if (null != env) this.env = env.name(); return this;
    }

    public InstallMetric setEndpoint(String endpoint) {
        if (null != endpoint) this.endpoint = endpoint; return this;
    }

    public InstallMetric setDatabaseVersion(long database_version) {
        this.database_version = database_version; return this;
    }

    public InstallMetric setPersistedLogCount(long persisted_log_count) {
        this.persisted_log_count = persisted_log_count; return this;
    }

    public InstallMetric setExpiredLogCount(long expired_log_count) {
        this.expired_log_count = expired_log_count; return this;
    }

    public InstallMetric setStatus(Status status) {
        if (null != header) header.setStatus(status); return this;
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
                .env(env)
                .endpoint(endpoint)
                .database_version(database_version)
                .persisted_log_count(persisted_log_count)
                .expired_log_count(expired_log_count);

        return shuttle.toJSONObject();
    }

    @Override
    public String getMetricType() { return "INSTALL"; }
}
