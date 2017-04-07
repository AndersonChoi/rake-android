package com.rake.android.rkmetrics.network;

public interface HttpRequestProcedure {
    ServerResponse execute(String url,
                           String log,
                           String flushMethod) throws Throwable;
}


