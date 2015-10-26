package com.rake.android.rkmetrics.persistent;

import java.util.ArrayList;
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
                         Map<String, Map<String, List<String>>> logMap) {

        this.lastId = lastId;
        this.logMap = logMap;
    }

    /**
     * variables, getters
     */
    private String lastId; /* `rake` Database PK */
    private Map<String, Map<String, List<String>>> logMap; /* url to log */

    public String getLastId() { return lastId; }

    public Set<String> getUrls() {
        return new HashSet<String>(logMap.keySet());
    }

    public Set<String> getTokens(String url) {
        if (null == url || !logMap.containsKey(url)) return Collections.EMPTY_SET;

        return new HashSet(getLogMap().get(url).keySet());
    }

    public Map<String, Map<String, List<String>>> getLogMap() {
        return new HashMap<String, Map<String, List<String>>>(logMap);
    }

    /**
     * static functions
     */

    public static Transferable create(String lastId, List<Log> logList) {

        if (null == lastId || null == logList || 0 == logList.size()) return null;

        Map<String, Map<String, List<String>>> urlMap = new HashMap<String, Map<String, List<String>>>();

        for (Log l : logList) {
            if (!urlMap.containsKey(l.getUrl())) /* if urlMap doesn't have the url */
                urlMap.put(l.getUrl(), new HashMap<String, List<String>>());

            Map<String, List<String>> tokenMap = urlMap.get(l.getUrl());

            if (!tokenMap.containsKey(l.getToken()))
                tokenMap.put(l.getToken(), new ArrayList<String>());

            List<String> stringifiedLogList = tokenMap.get(l.getToken());

            /* stringify JSONObject and put it into the list */
            stringifiedLogList.add(l.getLog().toString());
        }

        return new Transferable(lastId, urlMap);
    }
}
