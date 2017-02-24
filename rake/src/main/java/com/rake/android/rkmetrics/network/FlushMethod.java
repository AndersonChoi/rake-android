package com.rake.android.rkmetrics.network;


import android.os.Build;

public enum FlushMethod {
    HTTP_URL_CONNECTION("HTTP_URL_CONNECTION"),
    HTTP_CLIENT("HTTP_CLIENT");

    private String value;

    public String getValue() {
        return value;
    }

    /**
     * 4.0 이상일 경우 HttpUrlConnection 이용
     */
    public static FlushMethod getProperFlushMethod() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            return HTTP_URL_CONNECTION;
        return HTTP_CLIENT;
    }

    FlushMethod(String value) {
        this.value = value;
    }
}
