package com.rake.android.rkmetrics.network;


import com.rake.android.rkmetrics.android.Compatibility;

public enum FlushMethod {
    HTTP_URL_CONNECTION("HTTP_URL_CONNECTION"),
    HTTP_CLIENT("HTTP_CLIENT");

    private String value;
    public String getValue() { return value; }

    /** 4.0 이상일 경우 HttpUrlConnection 이용 */
    public static FlushMethod getProperFlushMethod() {
        if (Compatibility.getCurrentAPILevelAsInt() >= Compatibility.APILevel.ICE_CREAM_SANDWICH.getLevel())
            return HTTP_URL_CONNECTION;
        else
            return HTTP_CLIENT;
    }

    FlushMethod(String value) { this.value = value;}
}
