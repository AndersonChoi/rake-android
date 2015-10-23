package com.rake.android.rkmetrics.persistent;

public class ExtractedEvent {

    private String lastId; /* `rake` Database PK */
    private String log;

    public ExtractedEvent() {
        throw new RuntimeException("Can't create ExtractedEvent without args");
    }

    public ExtractedEvent(String lastId, String log) {
        this.lastId = lastId;
        this.log = log;
    }

    public String getLastId() { return lastId; }
    public String getLog() { return log; }
}
