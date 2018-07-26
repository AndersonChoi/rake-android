package com.rake.android.rkmetrics.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.text.TextUtils;


import com.rake.android.rkmetrics.db.log.Log;
import com.rake.android.rkmetrics.db.log.LogBundle;
import com.rake.android.rkmetrics.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogTable extends Table {
    private static final String TABLE_NAME = "log";

    // log 테이블 columns
    static class Columns implements BaseColumns {
        static final String URL = "url";                 /* STRING not null */
        static final String TOKEN = "token";             /* STRING not null */
        static final String LOG = "log";
        static final String CREATED_AT = "createdAt";
    }

    // 테이블 생성/삭제 쿼리
    static final String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
            Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Columns.URL + " TEXT NOT NULL, " +
            Columns.TOKEN + " TEXT NOT NULL, " +
            Columns.LOG + " TEXT NOT NULL, " +
            Columns.CREATED_AT + " INTEGER NOT NULL);";

    static final String QUERY_CREATE_INDEX = "CREATE INDEX IF NOT EXISTS createdAt_idx ON " + TABLE_NAME +
            " (" + Columns.CREATED_AT + ");";

    static final String QUERY_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    // singleton
    private static LogTable instance;

    private LogTable(Context context) {
        super(context);
    }

    public static synchronized LogTable getInstance(Context context) {
        if (instance == null) {
            instance = new LogTable(context);
        }
        return instance;
    }

    /**
     * insert Log into the 'log' table in the database 'rake'
     *
     * @param log Log object
     * @return total data count
     */
    public synchronized long addLog(final Log log) {
        if (log == null
                || TextUtils.isEmpty(log.getUrl())
                || TextUtils.isEmpty(log.getToken())
                || log.getJSON() == null) {
            Logger.e("Cannot add log without args.");
            return getCount();
        }

        queryExecutor(new QueryExecCallback<Void>() {
            @Override
            public Void execute(SQLiteDatabase db) {
                ContentValues values = new ContentValues();
                values.put(Columns.URL, log.getUrl());
                values.put(Columns.TOKEN, log.getToken());
                values.put(Columns.LOG, log.getJSON().toString());
                values.put(Columns.CREATED_AT, System.currentTimeMillis());

                db.insert(TABLE_NAME, null, values);
                return null;
            }
        });

        return getCount();
    }

    private synchronized long getCount() {
        Object queryResult = queryExecutor(new QueryExecCallback<Long>() {
            @Override
            public Long execute(SQLiteDatabase db) {

                String query = "SELECT COUNT(*) FROM " + TABLE_NAME;

                long count = -1L;

                Cursor cursor = db.rawQuery(query, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    count = cursor.getLong(0);
                    cursor.close();
                }

                return count;
            }
        });

        return queryResult == null ? -1L : (Long) queryResult;
    }

    /**
     * get count of logs by rake token
     *
     * @param token rake token
     * @return -1, if exception occurred or token is empty
     */
    public synchronized long getCount(final String token) {
        if (TextUtils.isEmpty(token)) {
            Logger.e("Cannot count data without token");
            return -1L;
        }

        Object queryResult = queryExecutor(new QueryExecCallback<Long>() {
            @Override
            public Long execute(SQLiteDatabase db) {
                String query = "SELECT COUNT(*) FROM " + TABLE_NAME
                        + " WHERE " + Columns.TOKEN + " = \"" + token + "\"";

                long count = -1;

                Cursor cursor = db.rawQuery(query, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    count = cursor.getLong(0);
                    cursor.close();
                }

                return count;
            }
        });

        return queryResult == null ? -1L : (Long) queryResult;
    }

    /**
     * remove logs from 'log' table which were created before time
     *
     * @param time remove logs before time
     * @return true if Logs are successfully removed.
     */
    public synchronized boolean removeLogsBefore(final long time) {
        if (time <= 0) {
            Logger.e("Cannot count data without proper time millis");
            return false;
        }

        Object queryResult = queryExecutor(new QueryExecCallback<Boolean>() {
            @Override
            public Boolean execute(SQLiteDatabase db) {
                String whereClause = Columns.CREATED_AT + " <= " + time;
                long affectedRowCount = db.delete(TABLE_NAME, whereClause, null);

                return affectedRowCount > 0;
            }
        });

        return queryResult == null ? false : (Boolean) queryResult;
    }

    /**
     * get log bundles from database
     *
     * @param maxLogCount total max log count to get (regardless of token)
     * @return a list of LogBundle regarding token
     */
    public synchronized List<LogBundle> getLogBundles(final int maxLogCount) {
        if (maxLogCount <= 0) {
            Logger.e("Cannot count data without proper maxLogCount");
            return null;
        }

        return queryExecutor(new QueryExecCallback<List<LogBundle>>() {
            @Override
            public List<LogBundle> execute(SQLiteDatabase db) {
                // read logs from DB
                String query = "SELECT * FROM " + TABLE_NAME
                        + " ORDER BY " + Columns.CREATED_AT + " ASC "
                        + " LIMIT " + maxLogCount;

                Cursor cursor = db.rawQuery(query, null);

                if (cursor == null) {
                    return null;
                }

                // divide logs by token
                Map<String, LogBundle> logBundleMap = new HashMap<>();

                try {
                    while (cursor.moveToNext()) {
                        String token = getStringFromCursor(cursor, Columns.TOKEN);
                        JSONObject json = new JSONObject(getStringFromCursor(cursor, Columns.LOG));

                        LogBundle logBundle;
                        if (logBundleMap.containsKey(token)) {
                            logBundle = logBundleMap.get(token);
                        } else {
                            logBundle = new LogBundle();
                            String url = getStringFromCursor(cursor, Columns.URL);
                            logBundle.setUrl(url);
                            logBundle.setToken(token);
                        }

                        logBundle.addLog(json);

                        if (cursor.isLast()) {
                            logBundle.setLast_ID(getStringFromCursor(cursor, Columns._ID));
                        }

                        logBundleMap.put(token, logBundle);
                    }
                } catch (JSONException e) {
                    Logger.e("Failed to getting logs from DB. " + e.getMessage());
                } finally {
                    cursor.close();
                }

                return new ArrayList<>(logBundleMap.values());
            }
        });
    }

    /**
     * remove log bundles from database
     *
     * @param logBundle log data set to be removed from database
     * @return true if logs are successfully removed
     */
    public synchronized boolean removeLogBundle(final LogBundle logBundle) {
        if (logBundle == null) {
            Logger.e("Cannot count data without LogBundle");
            return false;
        }

        Object queryResult = queryExecutor(new QueryExecCallback<Boolean>() {
            @Override
            public Boolean execute(SQLiteDatabase db) {
                String whereClause = Columns._ID + " <= " + logBundle.getLast_ID()
                        + " AND " + Columns.TOKEN + " = \"" + logBundle.getToken() + "\""
                        + " AND " + Columns.URL + " = \"" + logBundle.getUrl() + "\"";

                long affectedRowCount = db.delete(TABLE_NAME, whereClause, null);

                return affectedRowCount > 0;
            }
        });

        return queryResult == null ? false : (Boolean) queryResult;
    }
}
