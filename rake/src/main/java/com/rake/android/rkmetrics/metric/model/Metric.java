package com.rake.android.rkmetrics.metric.model;

import android.text.TextUtils;

import com.rake.android.rkmetrics.RakeAPI;
import com.rake.android.rkmetrics.util.ExceptionUtil;
import com.rake.android.rkmetrics.util.Logger;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import org.json.JSONObject;

public class Metric {
    private RakeClientMetricSentinelShuttle shuttle;

    private Metric(Builder builder) {
        this.shuttle = builder.shuttle;
    }

    public String getMetricType() {
        String action;
        String status;

        if (shuttle == null) {
            action = Action.EMPTY.getValue();
            status = Status.UNKNOWN.getValue();
            return action + ":" + status;
        }

        action = shuttle.toJSONObject().optString("action");
        status = shuttle.toJSONObject().optString("status");


        if (TextUtils.isEmpty(action)) {
            action = Action.EMPTY.getValue();
        }

        if (TextUtils.isEmpty(status)) {
            status = Status.UNKNOWN.getValue();
        }

        return action + ":" + status;
    }

    public JSONObject toJSONObject() {
        if (shuttle == null) {
            Logger.e("Cannot return JSONObject. Metric shuttle is null");
            return null;
        }

        return shuttle.toJSONObject();
    }

    public static class Builder {
        private RakeClientMetricSentinelShuttle shuttle;

        public Builder(RakeClientMetricSentinelShuttle shuttle) {
            this.shuttle = shuttle;
        }

        // header values
        public Builder setHeaderAction(Action action) {
            if (shuttle != null) {
                shuttle.action(action.getValue());
            }
            return this;
        }

        public Builder setHeaderStatus(Status status) {
            if (shuttle != null) {
                shuttle.status(status.getValue());
            }
            return this;
        }

        public Builder setHeaderAppPackage(String appPackage) {
            if (shuttle != null) {
                shuttle.app_package(appPackage);
            }
            return this;
        }

        public Builder setHeaderTransactionId(String transactionId) {
            if (shuttle != null) {
                shuttle.transaction_id(transactionId);
            }
            return this;
        }

        public Builder setHeaderServiceToken(String serviceToken) {
            if (shuttle != null) {
                shuttle.service_token(serviceToken);
            }
            return this;
        }

        // body values
        public Builder setBodyExceptionInfo(Throwable e) {
            if (shuttle != null) {
                shuttle.exception_type(ExceptionUtil.createExceptionType(e));
                shuttle.stacktrace(ExceptionUtil.createStacktrace(e));
            }
            return this;
        }

        public Builder setBodyOperationTime(long operationTime) {
            if (shuttle != null) {
                shuttle.operation_time(operationTime);
            }
            return this;
        }

        public Builder setBodyLogSize(long logSizeAsBytes) {
            if (shuttle != null) {
                shuttle.log_size(logSizeAsBytes);
            }
            return this;
        }

        public Builder setBodyLogCount(long logCount) {
            if (shuttle != null) {
                shuttle.log_count(logCount);
            }
            return this;
        }

        public Builder setBodyFlushType(String flushType) {
            if (shuttle != null) {
                shuttle.flush_type(flushType);
            }
            return this;
        }

        public Builder setBodyEndpoint(String endpoint) {
            if (shuttle != null) {
                shuttle.endpoint(endpoint);
            }
            return this;
        }

        public Builder setBodyServerResponseBody(String responseBody) {
            if (shuttle != null) {
                shuttle.server_response_body(responseBody);
            }
            return this;
        }

        public Builder setBodyServerResponseCode(long responseCode) {
            if (shuttle != null) {
                shuttle.server_response_code(responseCode);
            }
            return this;
        }

        public Builder setBodyServerResponseTime(long responseTime) {
            if (shuttle != null) {
                shuttle.server_response_time(responseTime);
            }
            return this;
        }

        public Builder setBodyFlushMethod(String flushMethod) {
            if (shuttle != null) {
                shuttle.flush_method(flushMethod);
            }
            return this;
        }

        public Builder setBodyEnv(RakeAPI.Env env) {
            if (shuttle != null) {
                shuttle.env(env.name());
            }
            return this;
        }

        public Builder setBodyLogging(RakeAPI.Logging logging) {
            if (shuttle != null) {
                shuttle.logging(logging.name());
            }
            return this;
        }

        public Builder setBodyDatabaseVersion(long databaseVersion) {
            if (shuttle != null) {
                shuttle.database_version(databaseVersion);
            }
            return this;
        }

        public Builder setBodyPersistedLogCount(long persistedLogCount) {
            if (shuttle != null) {
                shuttle.persisted_log_count(persistedLogCount);
            }
            return this;
        }

        public Builder setBodyExpiredLogCount(long expiredLogCount) {
            if (shuttle != null) {
                shuttle.expired_log_count(expiredLogCount);
            }
            return this;
        }

        public Builder setBodyMaxTrackCount(long maxTrackCount) {
            if (shuttle != null) {
                shuttle.max_track_count(maxTrackCount);
            }
            return this;
        }

        public Builder setBodyAutoFlushOnOff(RakeAPI.AutoFlush autoFlushOnOff) {
            if (shuttle != null) {
                shuttle.auto_flush_onoff(autoFlushOnOff.name());
            }
            return this;
        }

        public Builder setBodyAutoFlushInterval(long autoFlushInterval) {
            if (shuttle != null) {
                shuttle.auto_flush_interval(autoFlushInterval);
            }
            return this;
        }

        public Metric build() {
            return new Metric(this);
        }
    }
}
