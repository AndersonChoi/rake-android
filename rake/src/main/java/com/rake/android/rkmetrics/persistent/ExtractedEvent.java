package com.rake.android.rkmetrics.persistent;

import org.json.JSONArray;

@Deprecated // 0.4.0 버전부터 사용하지 않음. 0.4.0 미만 버전에서 업그레이드시 하위 호환을 위해 남김
public final class ExtractedEvent {

    private String lastId; /* `rake` Database PK */
    private String log;
    private int logCount;

    public ExtractedEvent() {
        throw new RuntimeException("Can't create ExtractedEvent without args");
    }

    private ExtractedEvent(String lastId, String log, int logCount) {
        this.lastId = lastId;
        this.log = log;
        this.logCount = logCount;
    }

    public String getLastId() { return lastId; }
    public String getLog() { return log; }
    public int getLogCount() { return logCount; }

    public static ExtractedEvent create(String lastId, JSONArray jsonArr) {
        if (null == lastId || null == jsonArr || 0 == jsonArr.length()) return null;

        return new ExtractedEvent(lastId, jsonArr.toString(), jsonArr.length());
    }
}
