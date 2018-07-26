package com.rake.android.rkmetrics.db.log;

import org.json.JSONObject;

public class Log {
    private String url;
    private String token;
    private JSONObject json;

    public Log(String url, String token, JSONObject json) {
        this.url = url;
        this.token = token;
        this.json = json;
    }

    public String getUrl() {
        return url;
    }

    public String getToken() {
        return token;
    }

    public JSONObject getJSON() {
        return json;
    }
}
