package com.rake.android.rkmetrics.persistent;

import org.json.JSONArray;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/* immutable object */
public class Transferable {

    /**
     * constructors
     */
    private Transferable() { throw new RuntimeException("Can't create Log without args"); }
    private Transferable(String lastId,
                         int logCount,
                         Map<String, Map<String, JSONArray>> logMap) {

        this.lastId = lastId;
        this.logCount = logCount;
        this.logMap = logMap;
    }

    /**
     * variables, getters
     */

    private int logCount;
    private String lastId; /* `rake` Database PK */
    private Map<String, Map<String, JSONArray>> logMap; /* url to log */

    public String getLastId() { return lastId; }
    public int getLogCount() { return logCount; }
    public Set<String> getUrls() { return new HashSet<String>(logMap.keySet()); }

    public Set<String> getTokens(String url) {
        if (null == url || !logMap.containsKey(url)) return Collections.EMPTY_SET;

        return new HashSet(getLogMap().get(url).keySet());
    }

    public Map<String, Map<String, JSONArray>> getLogMap() {
        return new HashMap<String, Map<String, JSONArray>>(logMap);
    }

    /**
     * static functions
     */

    public static Transferable create(String lastId, List<Log> logs) {

        if (null == lastId || null == logs || 0 == logs.size()) return null;

        Map<String, Map<String, JSONArray>> urlMap = new HashMap<String, Map<String, JSONArray>>();

        int logCount = 0;

        for (Log log : logs) {
            if (!urlMap.containsKey(log.getUrl())) /* if urlMap doesn't have the url */
                urlMap.put(log.getUrl(), new HashMap<String, JSONArray>());

            Map<String, JSONArray> tokenMap = urlMap.get(log.getUrl());

            if (!tokenMap.containsKey(log.getToken()))
                tokenMap.put(log.getToken(), new JSONArray());

            JSONArray jsonArr = tokenMap.get(log.getToken());
            jsonArr.put(log.getJson());

            logCount = logCount + 1;
        }

        return new Transferable(lastId, logCount, urlMap);
    }
}
