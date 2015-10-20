package com.rake.android.rkmetrics.persistent;

import org.json.JSONArray;

import java.util.List;

public class Flushable<L> {

    public Flushable() { throw new RuntimeException("Can't create Flushable without args"); }

    public Flushable(String lastId, List<L> logList) {
        this.lastId = lastId;
        this.logList = logList;
    }

    private String lastId; /* Database _id */
    private List<L> logList;
    public String getLastId() { return lastId; }
    public List<L> getLogList() { return logList; }
}
