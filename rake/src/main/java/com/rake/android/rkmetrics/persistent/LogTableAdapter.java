package com.rake.android.rkmetrics.persistent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.rake.android.rkmetrics.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public final class LogTableAdapter extends DatabaseAdapter {

    public static class LogContract implements BaseColumns {
        static final String TABLE_NAME = Table.LOG.getName();
        static final String COLUMN_CREATED_AT = "createdAt";

        static final String COLUMN_URL = "url";                 /* STRING not null */
        static final String COLUMN_TOKEN = "token";             /* STRING not null */
        static final String COLUMN_LOG = "log";

        static final String QUERY_CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + INTEGER_PK_AUTO_INCREMENT + COMMA_SEP +
                        COLUMN_URL + TEXT_TYPE_NOT_NULL + COMMA_SEP +
                        COLUMN_TOKEN + TEXT_TYPE_NOT_NULL + COMMA_SEP +
                        COLUMN_LOG + TEXT_TYPE_NOT_NULL + COMMA_SEP +
                        COLUMN_CREATED_AT + INTEGER_TYPE_NOT_NULL + QUERY_END;

        static final String QUERY_CREATE_INDEX =
                "CREATE INDEX IF NOT EXISTS craetedAt_idx ON " + TABLE_NAME +
                        " (" + COLUMN_CREATED_AT + QUERY_END;

        public static final String QUERY_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    private LogTableAdapter(Context appContext) {
        super(appContext);
    }

    private static LogTableAdapter instance;

    public static synchronized LogTableAdapter getInstance(Context appContext) {
        if (null == instance) {
            instance = new LogTableAdapter(appContext);
        }

        return instance;
    }

    /**
     * @param token
     * @return -1, if exception occurred or token is NULL
     */
    public synchronized int getCount(final String token) {
        if (null == token) return -1;

        Integer count = executeAndReturnT(new SQLiteUtil.Callback<Integer>() {
            @Override
            public Integer execute(SQLiteDatabase db) {
                Cursor c = null;

                try {
                    c = db.rawQuery(getQuery(), null);
                    c.moveToFirst();
                    return c.getInt(0);
                } finally { /* exception 은 Callback 에서 처리 */
                    if (null != c) c.close();
                }
            }

            @Override
            public String getQuery() {
                /* starts with ` ` (space) */
                String BEGIN_WHERE_CLAUSE_TOKEN = String.format(" WHERE %s = \"%s\"",
                        LogContract.COLUMN_TOKEN, token);

                return "SELECT COUNT(*) FROM " + LogContract.TABLE_NAME + BEGIN_WHERE_CLAUSE_TOKEN;
            }
        });

        return (null == count) ? -1 : count;
    }

    /**
     * @param chunk should not be null
     */
    public synchronized void removeLogChunk(final LogChunk chunk) {
        execute(new SQLiteUtil.Callback<Void>() {
            @Override
            public Void execute(SQLiteDatabase db) {
                db.delete(LogContract.TABLE_NAME, getQuery(), null);
                return null;
            }

            @Override
            public String getQuery() {
                String WHERE_CLAUSE_TOKEN = String.format("%s = \"%s\"",
                        LogContract.COLUMN_TOKEN, chunk.getToken());

                String WHERE_CLAUSE_URL = String.format("%s = \"%s\"",
                        LogContract.COLUMN_URL, chunk.getUrl());

                return LogContract._ID + " <= " + chunk.getLastId() +
                        AND + WHERE_CLAUSE_TOKEN +
                        AND + WHERE_CLAUSE_URL;
            }
        });
    }

    public synchronized void removeLogByTime(final Long time) {
        execute(new SQLiteUtil.Callback<Void>() {
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

    public synchronized int addLog(final Log log) {
        if (null == log) {
            Logger.e("Can't record NULL log");
            return -1;
        }

        Integer result = executeAndReturnT(new SQLiteUtil.Callback<Integer>() {
            @Override
            public Integer execute(SQLiteDatabase db) {
                Cursor c = null;

                try {
                    ContentValues values = new ContentValues();
                    values.put(LogContract.COLUMN_LOG, log.getJson().toString());
                    values.put(LogContract.COLUMN_CREATED_AT, System.currentTimeMillis());
                    values.put(LogContract.COLUMN_URL, log.getUrl());
                    values.put(LogContract.COLUMN_TOKEN, log.getToken());
                    db.insert(LogContract.TABLE_NAME, null, values);

                    c = db.rawQuery(getQuery(), null);
                    c.moveToFirst();

                    return c.getInt(0);
                } finally { /* exception 은 Callback 에서 처리 */
                    if (null != c) {
                        c.close();
                    }
                }
            }

            @Override
            public String getQuery() {
                return "SELECT COUNT(*) FROM " + LogContract.TABLE_NAME;
            }
        });

        return ((null == result) ? -1 : result);
    }

    /**
     * 개별 토큰마다 flush 하지 않고 아래 처럼 모든 토큰을 flush 하는 이유는,
     * <p>
     * - 단말 내에 Rake 를 사용한 복수개의 SDK 가 있을 수 있음
     * - 개별 SDK 는 자신을 Flush 하지 않을 수 있음
     */
    public synchronized List<LogChunk> getLogChunks(final int extractCount) {

        return executeAndReturnT(new SQLiteUtil.Callback<List<LogChunk>>() {
            @Override
            public List<LogChunk> execute(SQLiteDatabase db) {
                Cursor c = null;
                String lastId = null;
                List<Log> logList = new ArrayList<>();

                try {
                    c = db.rawQuery(getQuery(), null);

                    while (c.moveToNext()) {
                        if (c.isLast()) {
                            lastId = LogTableAdapter.this.getStringFromCursor(c, LogContract._ID);
                        }

                        Log log = LogTableAdapter.this.createLog(c);

                        if (null != log) {
                            logList.add(log);
                        }
                    }
                } finally { /* exception 은 Callback 에서 처리 */
                    if (null != c) {
                        c.close();
                    }
                }

                return LogChunk.create(lastId, logList);
            }

            @Override
            public String getQuery() {

                return String.format(Locale.US, "SELECT * FROM %s ORDER BY %s ASC LIMIT %d",
                        LogContract.TABLE_NAME,
                        LogContract.COLUMN_CREATED_AT,
                        extractCount);
            }
        });
    }

    /**
     * do not close Cursor in this method
     */
    private synchronized Log createLog(Cursor c) {
        try {
            String url = getStringFromCursor(c, LogContract.COLUMN_URL);
            String token = getStringFromCursor(c, LogContract.COLUMN_TOKEN);
            JSONObject log = new JSONObject(getStringFromCursor(c, LogContract.COLUMN_LOG));

            return Log.create(url, token, log);
        } catch (JSONException e) { /* ignore */ }

        return null;
    }
}
