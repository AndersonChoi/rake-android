package com.rake.android.rkmetrics.metric.model;

import android.content.Context;

import com.rake.android.rkmetrics.android.SystemInformation;
import com.rake.android.rkmetrics.metric.MetricUtil;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

public class Header {

    public Header() {}

    public static final String HEADER_NAME_ACTION = "action";
    public static final String HEADER_NAME_STATUS = "status";
    public static final String HEADER_NAME_APP_PACKAGE = "app_package";
    public static final String HEADER_NAME_TRANSACTION_ID = "transaction_id";
    public static final String HEADER_NAME_SERVICE_TOKEN = "service_token";

    private String action;
    private String status;

    private String app_package;
    private String transaction_id;
    private String service_token;

    public Header setAction(Action action) { this.action = (null == action) ? null : action.getValue(); return this; }
    public Header setStatus(Status status) { this.status = (null == status) ? null : status.getValue(); return this; }
    public Header setAppPackage(String app_package) { this.app_package = app_package; return this; }
    public Header setTransactionId(String transaction_id) { this.transaction_id = transaction_id; return this; }
    public Header setServiceToken(String service_token) { this.service_token = service_token; return this; }


    /**
     * @param shuttle
     * @return false if shuttle is null, otherwise true
     */
    public boolean fillShuttle(RakeClientMetricSentinelShuttle shuttle) {
       if (null == shuttle) return false;

        shuttle
                .action(action)
                .status(status)
                .app_package(app_package)
                .transaction_id(transaction_id)
                .service_token(service_token);

        return true;
    }

    /**
     * @return return NULL if action *and* status are null since we can't know which kind of metric
     */
    public static Header create(Context context, Action action, Status status, String serviceToken) {
        if (null == action && null == status) return null;

        Header header = new Header();
        header
                .setAction(action)
                .setStatus(status)
                .setAppPackage(SystemInformation.getPackageName(context))
                .setTransactionId(MetricUtil.TRANSACTION_ID)
                .setServiceToken(serviceToken);

        return header;
    }
}
