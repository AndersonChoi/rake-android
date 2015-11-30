package com.rake.android.rkmetrics.metric.model;

public class Header {

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

    public Header setAction(Action action) { this.action = action; return this; }
    public String getAction() { if (null == action) return null; return action.getValue(); }

    public Header setStatus(Status status) { this.status = status; return this; }
    public String getStatus() { if (null == action) return null; return action.getValue(); }

    public Header setAppPackage(String app_package) { this.app_package = app_package; return this; }
    public String getAppPackage() { return app_package; }

    public Header setTransactionId(String transaction_id) { this.transaction_id = transaction_id; return this; }
    public String getTransactionId() { return transaction_id; }

    public Header setServiceToken(String service_token) { this.service_token = service_token; return this; }
    public String getServiceToken() { return service_token; }
}
