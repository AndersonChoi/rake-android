package com.rake.android.rkmetrics.persistent;

import org.json.JSONArray;

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
