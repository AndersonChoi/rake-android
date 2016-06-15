package com.rake.android.rkmetrics.network;

public interface HttpRequestProcedure {
    ServerResponse execute(String url,
                           String log,
                           FlushMethod flushMethod) throws Throwable;
}


