package com.rake.android.rkmetrics.persistent;

import org.json.JSONArray;

public final class ExtractedEvent {

    private String lastId; /* `rake` Database PK */
    private String log;

    public ExtractedEvent() {
        throw new RuntimeException("Can't create ExtractedEvent without args");
    }

    private ExtractedEvent(String lastId, String log) {
        this.lastId = lastId;
        this.log = log;
    }

    public String getLastId() { return lastId; }
    public String getLog() { return log; }

    public static ExtractedEvent create(String lastId, JSONArray jsonArr) {
        if (null == lastId || null == jsonArr || 0 == jsonArr.length()) return null;

        return new ExtractedEvent(lastId, jsonArr.toString());
    }
}
