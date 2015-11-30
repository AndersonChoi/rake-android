package com.rake.android.rkmetrics.metric.model;

import org.json.JSONObject;

public abstract class Header {

    public static final String FIELD_NAME_ACTION = "action";
    public static final String FIELD_NAME_STATUS = "status";
    public static final String FIELD_NAME_APP_PACKAGE = "app_package";
    public static final String FIELD_NAME_TRANSACTION_ID = "transaction_id";
    public static final String FIELD_NAME_SERVICE_TOKEN = "service_token";

    private Action action;
    private Status status;

    private String app_package;
    private String transaction_id;
    private String service_token;

    protected Header(String app_package,
                     String transaction_id,
                     String service_token) {
        this.app_package = app_package;
        this.transaction_id = transaction_id;
        this.service_token = service_token;
    }

    public String getAction() {
        if (null == action) return null;
        return action.getValue();
    }

    public String getStatus() {
        if (null == action) return null;
        return action.getValue();
    }

    public abstract JSONObject toJSONObject();

    public String getAppPackage() { return app_package; }
    public String getTransactionId() { return transaction_id; }
    public String getServiceToken() { return service_token; }

}
