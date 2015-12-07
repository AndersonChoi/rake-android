package com.rake.android.rkmetrics.persistent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.rake.android.rkmetrics.config.RakeConfig;
import com.rake.android.rkmetrics.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    public synchronized int addEvent(final JSONObject json) {
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
    public synchronized void removeEventById(final String lastId) {
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
    public synchronized void removeEventByTime(final long time) {
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
    public synchronized ExtractedEvent getExtractEvent() {
        ExtractedEvent event = executeAndReturnT(new SQLiteCallback<ExtractedEvent>() {
            @Override
            public ExtractedEvent execute(SQLiteDatabase db) {
                Cursor c = null;
                String lastId = null;
                JSONArray jsonArr = new JSONArray();

                try {
                    c = db.rawQuery(getQuery(), null);

                    while (c.moveToNext()) {
                        // TODO c.getString getColumnIndex to helper function
                        if (c.isLast()) lastId = getStringFromCursor(c, EventContract._ID);

                        String log = getStringFromCursor(c, EventContract.COLUMN_DATA);

                        try { jsonArr.put(new JSONObject(log)); } /* if an exception occurred, ignore it */
                        catch (JSONException e) { Logger.t("Failed to convert String to JsonObject", e); }
                    }

                /* if JSONException occurred, just throw out eventually returning null. */
                } finally { if (null != c) c.close(); }

                // TODO static factory
                ExtractedEvent e = ExtractedEvent.create(lastId, jsonArr);

                if (null != e) {
                    String message = String.format("[SQLite] Extracting %d rows from the [%s] table",
                            jsonArr.length(), EventContract.TABLE_NAME);
                    Logger.d(message);
                }

                return e;
            }

            @Override
            public String getQuery() {
                return String.format("SELECT * FROM %s ORDER BY %s ASC LIMIT %d",
                        EventContract.TABLE_NAME, EventContract.COLUMN_CREATED_AT, RakeConfig.TRACK_MAX_LOG_COUNT);
            }
        });

        return event;
    }

}
