package com.rake.android.rkmetrics.network;

public interface HttpRequestProcedure {
    ServerResponseMetric execute(String url, String log, FlushMethod flushMethod) throws Throwable;
}


