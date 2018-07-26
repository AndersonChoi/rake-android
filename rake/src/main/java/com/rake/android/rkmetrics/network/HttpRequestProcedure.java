package com.rake.android.rkmetrics.network;

public interface HttpRequestProcedure {
    HttpResponse execute(String url,
                           String log) throws Throwable;
}


