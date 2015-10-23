package com.rake.android.rkmetrics.persistent;

import org.json.JSONArray;

import java.util.Collections;
import java.util.List;

public class Flushable {

    public Flushable() { throw new RuntimeException("Can't create Flushable without args"); }

    public Flushable(String lastId, String log) {

        // TODO logging, if null
        this.lastId = lastId;
        this.log = log;
    }

    private String lastId; /* `rake` Database PK */
    private String log;
    public String getLastId() { return lastId; }
    public String getLog() { return log; }
}
