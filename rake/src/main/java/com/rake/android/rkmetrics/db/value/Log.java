package com.rake.android.rkmetrics.db.value;

import org.json.JSONObject;

/**
 * Created by 1000731 on 2017. 12. 13..
 */

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
