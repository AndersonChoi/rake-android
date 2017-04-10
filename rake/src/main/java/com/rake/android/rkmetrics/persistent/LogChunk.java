package com.rake.android.rkmetrics.persistent;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 전송 가능한 `Log` 덩어리
 */
public class LogChunk {
    private LogChunk() { throw new RuntimeException("Can't create Log without args"); }

    private LogChunk(String lastId, String url, String token, String chunk, int count) {
        this.lastId = lastId;
        this.url = url;
        this.token = token;
        this.chunk = chunk;
        this.count = count;
    }

    private String lastId;
    private String url;
    private String token;
    private String chunk;
    private int count;

    /**
     * 데이터베이스에 저장된 LOG(TOKEN, URL, DATA 로 구성) 여러개를 받아,
     * TOKEN 과 URL 별로 구분한 LogChunk 리스트를 생성함
     */
    public static List<LogChunk> create(String lastId, List<Log> logs) {

        if (null == lastId || null == logs || 0 == logs.size()) return null;

        Map<String, Map<String, JSONArray>> logMap = new HashMap<>();

        for (Log log : logs) {
            if (!logMap.containsKey(log.getUrl())) { /* if urlMap doesn't have the url */
                logMap.put(log.getUrl(), new HashMap<String, JSONArray>());
            }

            Map<String, JSONArray> tokenMap = logMap.get(log.getUrl());

            if (!tokenMap.containsKey(log.getToken()))
                tokenMap.put(log.getToken(), new JSONArray());

            JSONArray jsonArr = tokenMap.get(log.getToken());
            jsonArr.put(log.getJson());
        }

        List<LogChunk> chunks = new ArrayList<>();

        for (String url : logMap.keySet()) {
            for (String token : logMap.get(url).keySet()) {
                JSONArray jsons = logMap.get(url).get(token);
                int count = jsons.length();

                if (null == jsons || 0 == count) continue;

                LogChunk chunk = create(lastId, url, token, jsons.toString(), count);

                chunks.add(chunk);
            }
        }

        return (0 == chunks.size()) ? Collections.EMPTY_LIST : chunks;
    }

    public String getLastId() { return lastId; }
    public String getUrl() { return url; }
    public String getToken() { return token; }
    public String getChunk() { return chunk; }
    public int getCount() { return count; }

    public static LogChunk create(String lastId,
                                  String url,
                                  String token,
                                  String chunk,
                                  int count) {

        if (null == url || null == token || null == chunk) return null;

        return new LogChunk(lastId, url, token, chunk, count);
    }
}
