package com.rake.android.rkmetrics.persistent;

import org.json.JSONObject;

/* not immutable */
public class Log {
    private Log() { throw new RuntimeException("Can't create Log without args"); }

    private Log(String url, String token, JSONObject log) {
        this.url = url;
        this.token = token;
        this.log = log;
    }

    private String url;
    private String token;
    private JSONObject log;

    public String getUrl() { return url; }
    public String getToken() { return token; }
    public JSONObject getLog() { return log; }

    public static Log create(String url, String token, JSONObject log) {
        if (null == url || null == token || null == log) return null;

        return new Log(url, token, log);
    }
}
