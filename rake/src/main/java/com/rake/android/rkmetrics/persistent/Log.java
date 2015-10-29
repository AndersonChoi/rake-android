package com.rake.android.rkmetrics.persistent;

import org.json.JSONObject;

/* not immutable */
public class Log {
    private Log() { throw new RuntimeException("Can't create Log without args"); }

    private Log(String url, String token, JSONObject json) {
        this.url = url;
        this.token = token;
        this.json = json;
    }

    private String url;
    private String token;
    private JSONObject json;

    public String getUrl() { return url; }
    public String getToken() { return token; }
    public JSONObject getJson() { return json; }

    public static Log create(String url, String token, JSONObject json) {
        if (null == url || null == token || null == json) return null;

        return new Log(url, token, json);
    }
}
