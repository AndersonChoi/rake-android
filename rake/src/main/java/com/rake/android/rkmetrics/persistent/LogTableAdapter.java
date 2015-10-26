package com.rake.android.rkmetrics.persistent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public final class LogTableAdapter extends DatabaseAdapter {

    public static class LogContract implements BaseColumns {
        public static final String TABLE_NAME = Table.LOG.getName();
        public static final String COLUMN_CREATED_AT = "createdAt";

        public static final String COLUMN_URL = "url";                 /* STRING not null */
        public static final String COLUMN_TOKEN = "token";             /* STRING not null */
        public static final String COLUMN_LOG = "log";

        public static final String QUERY_CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +  " (" +
                        _ID + INTEGER_PK_AUTO_INCREMENT + COMMA_SEP +
                        COLUMN_URL + TEXT_TYPE_NOT_NULL + COMMA_SEP +
                        COLUMN_TOKEN + TEXT_TYPE_NOT_NULL + COMMA_SEP +
                        COLUMN_LOG + TEXT_TYPE_NOT_NULL + COMMA_SEP +
                        COLUMN_CREATED_AT + INTEGER_TYPE_NOT_NULL + QUERY_END;

        public static final String QUERY_CREATE_INDEX =
                "CREATE INDEX IF NOT EXISTS craetedAt_idx ON " + TABLE_NAME +
                        " (" + COLUMN_CREATED_AT + QUERY_END;

        public static final String QUERY_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    private LogTableAdapter(Context appContext) {
        super(appContext);
    }

    private static LogTableAdapter instance;

    public static synchronized LogTableAdapter getInstance(Context appContext) {
        if (null == instance) instance = new LogTableAdapter(appContext);

        return instance;
    }

    public void removeLogById(final String lastId) {
        execute(new SQLiteCallback<Void>() {
            @Override
            public Void execute(SQLiteDatabase db) {
                db.delete(LogContract.TABLE_NAME, getQuery(), null);
                return null;
            }

            @Override
            public String getQuery() {
                return LogContract._ID + " <= " + lastId;
            }
        });
    }

    public void removeLogByTime(final Long time) {
       execute(new SQLiteCallback<Void>() {
           @Override
           public Void execute(SQLiteDatabase db) {
               db.delete(LogContract.TABLE_NAME, getQuery(), null);
               return null;
           }

           @Override
           public String getQuery() {
               return LogContract.COLUMN_CREATED_AT + " <= " + time;
           }
       });
    }

    public int addLog(final Log log) {
        Integer result = executeAndReturnT(new SQLiteCallback<Integer>() {
            @Override
            public Integer execute(SQLiteDatabase db) {
                Cursor c = null;
                ContentValues values = new ContentValues();
                values.put(LogContract.COLUMN_LOG, log.getLog().toString());
                values.put(LogContract.COLUMN_CREATED_AT, System.currentTimeMillis());
                values.put(LogContract.COLUMN_URL, log.getUrl());
                values.put(LogContract.COLUMN_TOKEN, log.getToken());
                db.insert(LogContract.TABLE_NAME, null, values);

                c = db.rawQuery(getQuery(), null);
                c.moveToFirst();

                return c.getInt(0);
            }

            @Override
            public String getQuery() { return "SELECT COUNT(*) FROM " + LogContract.TABLE_NAME; }
        });

        return ((null == result) ? -1 : result);
    }

    public Transferable getTransferable(final int extractCount) {

        Transferable t = executeAndReturnT(new SQLiteCallback<Transferable>() {
            @Override
            public Transferable execute(SQLiteDatabase db) {
                Cursor c = null;
                String lastId = null;
                List<Log> logList = new ArrayList<Log>();

                try {
                    c = db.rawQuery(getQuery(), null);

                    while(c.moveToNext()) {
                        if (c.isLast()) lastId = getStringFromCursor(c, LogContract._ID);

                        Log log = createLog(c);

                        if (null != log) logList.add(log);
                    }
                } finally { if (null != c) c.close(); }

                return Transferable.create(lastId, logList);
            }

            @Override
            public String getQuery() {
                String query = String.format("SELECT * FROM %s ORDER BY %s ASC LIMIT %d",
                        LogContract.TABLE_NAME,
                        LogContract.COLUMN_CREATED_AT,
                        extractCount);

                return query;
            }
        });

        return t; /* might be null */
    }

    private Log createLog(Cursor c) {
        Log l = null;

        try {
            String url = getStringFromCursor(c, LogContract.COLUMN_URL);
            String token = getStringFromCursor(c, LogContract.COLUMN_TOKEN);
            JSONObject log = new JSONObject(getStringFromCursor(c, LogContract.COLUMN_LOG));

            l = Log.create(url, token, log);
        } catch (JSONException e) { /* ignore */ }

        return l;
    }
}
