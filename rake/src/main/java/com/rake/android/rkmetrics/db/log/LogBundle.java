package com.rake.android.rkmetrics.db.log;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by 1000731 on 2017. 12. 19..
 */

public class LogBundle {

    private String token;
    private String url;
    private String last_ID;
    private JSONArray jsonLogArray;

    public LogBundle(){
        jsonLogArray = new JSONArray();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLast_ID() {
        return last_ID;
    }

    public void setLast_ID(String last_ID) {
        this.last_ID = last_ID;
    }

    public void addLog(JSONObject jsonLog){
        jsonLogArray.put(jsonLog);
    }

    public String getLogsByJSONString(){
        return jsonLogArray.toString();
    }

    public int getCount() {
        return jsonLogArray.length();
    }
}
