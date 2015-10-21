package com.rake.android.rkmetrics.persistent;

import static com.rake.android.rkmetrics.config.RakeConfig.LOG_TAG_PREFIX;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.rake.android.rkmetrics.util.RakeLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class EventTableAdapter extends DatabaseAdapter {

    public static class EventContract implements BaseColumns {
        public static final String TABLE_NAME = Table.EVENTS.getName();

        public static final String COLUMN_CREATED_AT = "created_at";   /* INTEGER not null */
        public static final String COLUMN_DATA = "data";               /* STRING not null */

        public static final String QUERY_CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + _ID + INTEGER_PK_AUTO_INCREMENT + COMMA_SEP +
                        COLUMN_DATA + STRING_TYPE_NOT_NULL + COMMA_SEP +
                        COLUMN_CREATED_AT + INTEGER_TYPE_NOT_NULL + QUERY_END;

        public static final String QUERY_CREATE_INDEX =
                "CREATE INDEX IF NOT EXISTS time_idx ON " + TABLE_NAME +
                        " (" + COLUMN_CREATED_AT + ");";

        public static final String QUERY_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    private EventTableAdapter(Context appContext) {
        super(appContext);
    }

    private static EventTableAdapter instance;

    public static synchronized EventTableAdapter getInstance(Context appContext) {
        if (null == instance) instance = new EventTableAdapter(appContext);

        return instance;
    }

    /**
     * Adds a JSON string representing an event with properties or a person record
     * to the SQLiteDatabase.
     *
     * @param json     the JSON to record
     * @return the number of rows in the table, or -1 on failure
     */
    public int addEvent(final JSONObject json) {
        final String table = Table.EVENTS.getName();

        Integer result = executeAndReturnT(new SQLiteCallback<Integer>() {
            @Override
            public Integer execute(SQLiteDatabase db) {

                Cursor c = null;
                ContentValues cv = new ContentValues();
                cv.put(EventContract.COLUMN_DATA, json.toString());
                cv.put(EventContract.COLUMN_CREATED_AT, System.currentTimeMillis());
                db.insert(table, null, cv);

                c = db.rawQuery(getQuery(), null);
                c.moveToFirst();
                return c.getInt(0);
            }

            @Override
            public String getQuery() {
                return "SELECT COUNT(*) FROM " + table;
            }
        });

        return ((result == null) ? -1 : result);
    }

    /**
     * Removes events with an _id <= lastId from table
     *
     * @param lastId the last id to delete
     */
    public void removeEvent(final String lastId) {
        final String table = Table.EVENTS.getName();

        execute(new SQLiteCallback<Void>() {
            @Override
            public Void execute(SQLiteDatabase db) {
                db.delete(table, getQuery(), null);
                return null;
            }

            @Override
            public String getQuery() {
                return EventContract._ID + " <= " + lastId;
            }
        });
    }

    /**
     * Removes events before time.
     *
     * @param time  the unix epoch in milliseconds to remove events before
     */
    public void removeEvent(final long time) {
        final String table = Table.EVENTS.getName();

        execute(new SQLiteCallback<Void>() {
            @Override
            public Void execute(SQLiteDatabase db) {
                db.delete(table, getQuery(), null);
                return null;
            }

            @Override
            public String getQuery() {
                return EventContract.COLUMN_CREATED_AT + " <= " + time;
            }
        });
    }

    /**
     * Returns the data string to send to Rake and the maximum ID of the row that
     * we're sending, so we know what rows to delete when a track request was successful.
     *
     * @return String array containing the maximum ID and the data string
     * representing the events, or null if none could be successfully retrieved.
     */
    public String[] getEventList() {
        Cursor c = null;
        String data = null;
        final String table = Table.EVENTS.getName();

        Flushable<JSONObject> flushable = executeAndReturnT(new SQLiteCallback<Flushable<JSONObject>>() {
            @Override
            public Flushable<JSONObject> execute(SQLiteDatabase db) {
                Cursor c = null;
                String lastId = null;
                List<JSONObject> logList = new ArrayList<JSONObject>();

                try {
                    c = db.rawQuery(getQuery(), null);

                    while (c.moveToNext()) {
                        if (c.isLast()) lastId = c.getString(c.getColumnIndex(EventContract._ID));

                        String log = c.getString(c.getColumnIndex(EventContract.COLUMN_DATA));

                        try { logList.add(new JSONObject(log)); }
                        catch (JSONException e) { /* logging and ignore */
                            RakeLogger.t(LOG_TAG_PREFIX, "Failed to convert String to JsonObject", e); }
                    }

                /* if exception occurred, just throw out */
                } finally {
                    if (null != c) c.close();
                }

                return new Flushable<JSONObject>(lastId, logList);
            }

            @Override
            public String getQuery() {
                return "SELECT * FROM " + table + " ORDER BY " + EventContract.COLUMN_CREATED_AT + " ASC LIMIT 50";
            }
        });

        if (null == flushable || flushable.getLogList().size() == 0) return null;
        else { /* Flushable<JSONObject>.getLogList() to String */
            List<JSONObject> logList = flushable.getLogList();
            JSONArray jsonArray = new JSONArray(logList);

            String[] result = { flushable.getLastId(), new JSONArray(logList).toString() };
            return result;
        }
    }

}
