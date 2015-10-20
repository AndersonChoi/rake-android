package com.rake.android.rkmetrics.persistent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import com.rake.android.rkmetrics.util.RakeLogger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static com.rake.android.rkmetrics.config.RakeConfig.LOG_TAG_PREFIX;

/**
 * Not thread-safe.
 * Instances of this class should only be used by a single thread.
 */
final public class DatabaseAdapter {
    public enum Table {
        EVENTS("events");
        Table(String name) { tableName = name; }
        public String getName() {
            return tableName;
        }
        private final String tableName;
    }

    private static final String DATABASE_NAME = "rake";
    /**
     * version 5: `token`, `url` columns added
     */
    private static final int DATABASE_VERSION = 5;

    private static final String QUERY_SEP = ", ";
    private static final String QUERY_END = ");";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_URL = "url";                 /* STRING not null */
    public static final String COLUMN_TOKEN = "token";             /* STRING not null */
    public static final String COLUMN_DATA = "data";               /* STRING not null */
    public static final String COLUMN_CREATED_AT = "created_at";   /* INTEGER not null */
    private static final String CREATE_EVENTS_TABLE =
            "CREATE TABLE " + Table.EVENTS.getName() + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DATA + " STRING NOT NULL" + QUERY_SEP +
                    COLUMN_CREATED_AT + " INTEGER NOT NULL" + QUERY_END;

    private static final String EVENTS_TIME_INDEX =
            "CREATE INDEX IF NOT EXISTS time_idx ON " + Table.EVENTS.getName() +
                    " (" + COLUMN_CREATED_AT + ");";

    private static DatabaseAdapter instance;
    private final DatabaseHelper dbHelper;

    private DatabaseAdapter(Context context) {
        dbHelper = new DatabaseHelper(context, DATABASE_NAME);
    }

    public static synchronized DatabaseAdapter getInstance(Context appContext) {
        if (null == instance) {
            instance = new DatabaseAdapter(appContext);
        }

        return instance;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private final File databaseFile;
        DatabaseHelper(Context context, String dbName) {
            super(context, dbName, null, DATABASE_VERSION);
            databaseFile = context.getDatabasePath(dbName);
        }

        public void deleteDatabase() {
            close();
            databaseFile.delete(); // Completely deletes the DB file from the file system.
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            RakeLogger.d(LOG_TAG_PREFIX, "Creating Database: " + DATABASE_NAME);

            db.execSQL(CREATE_EVENTS_TABLE);
            db.execSQL(EVENTS_TIME_INDEX);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String message = String.format("Upgrading Database [%s] from version %d to %d",
                    DATABASE_NAME, oldVersion, newVersion);

            RakeLogger.d(LOG_TAG_PREFIX, message);

            db.execSQL("DROP TABLE IF EXISTS " + Table.EVENTS.getName());
            db.execSQL(CREATE_EVENTS_TABLE);
            db.execSQL(EVENTS_TIME_INDEX);

            if (oldVersion < 4) { /* DO NOT SUPPORT */
            }

            if (oldVersion < 5) { /* 4 -> 5 */

            }

        }
    }

    /**
     * Adds a JSON string representing an event with properties or a person record
     * to the SQLiteDatabase.
     *
     * @param j     the JSON to record
     * @return the number of rows in the table, or -1 on failure
     */
    public int addEvent(JSONObject j) {
        String tableName = Table.EVENTS.getName();
        Cursor c = null;
        int count = -1;

        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_DATA, j.toString());
            cv.put(COLUMN_CREATED_AT, System.currentTimeMillis());
            db.insert(tableName, null, cv);

            c = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
            c.moveToFirst();
            count = c.getInt(0);
        } catch (SQLiteException e) {
            RakeLogger.e(LOG_TAG_PREFIX, "addEvent " + tableName + " FAILED. Deleting DB.", e);

            // We assume that in general, the results of a SQL exception are
            // unrecoverable, and could be associated with an oversized or
            // otherwise unusable DB. Better to bomb it and get back on track
            // than to leave it junked up (and maybe filling up the disk.)
            dbHelper.deleteDatabase();
        } finally {
            dbHelper.close();
            if (c != null) {
                c.close();
            }
        }
        return count;
    }

    /**
     * Removes events with an _id <= last_id from table
     *
     * @param last_id the last id to delete
     */
    public void removeEvent(String last_id) {
        String tableName = Table.EVENTS.getName();
        // RakeLogger.t(LOG_TAG_PREFIX, "removeEvent _id " + last_id + " from table " + tableName);

        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete(tableName, "_id <= " + last_id, null);
        } catch (SQLiteException e) {
            RakeLogger.e(LOG_TAG_PREFIX, "removeEvent " + tableName + " by id FAILED. Deleting DB.", e);

            // We assume that in general, the results of a SQL exception are
            // unrecoverable, and could be associated with an oversized or
            // otherwise unusable DB. Better to bomb it and get back on track
            // than to leave it junked up (and maybe filling up the disk.)
            dbHelper.deleteDatabase();
        } finally {
            dbHelper.close();
        }
    }

    /**
     * Removes events before time.
     *
     * @param time  the unix epoch in milliseconds to remove events before
     */
    public void removeEvent(long time) {
        String tableName = Table.EVENTS.getName();
        // RakeLogger.d(LOG_TAG_PREFIX, "removeEvent time " + time + " from table " + tableName);

        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete(tableName, COLUMN_CREATED_AT + " <= " + time, null);
        } catch (SQLiteException e) {
            RakeLogger.e(LOG_TAG_PREFIX, "removeEvent " + tableName + " by time FAILED. Deleting DB.", e);

            // We assume that in general, the results of a SQL exception are
            // unrecoverable, and could be associated with an oversized or
            // otherwise unusable DB. Better to bomb it and get back on track
            // than to leave it junked up (and maybe filling up the disk.)
            dbHelper.deleteDatabase();
        } finally {
            dbHelper.close();
        }
    }

    public void deleteDB() {
        dbHelper.deleteDatabase();
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
        String last_id = null;
        String tableName = Table.EVENTS.getName();

        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            c = db.rawQuery("SELECT * FROM " + tableName +
                    " ORDER BY " + COLUMN_CREATED_AT + " ASC LIMIT 50", null);
            JSONArray arr = new JSONArray();

            RakeLogger.t(LOG_TAG_PREFIX, "sending log count: " + c.getCount());

            while (c.moveToNext()) {
                if (c.isLast()) {
                    last_id = c.getString(c.getColumnIndex("_id"));
                }
                try {
                    JSONObject j = new JSONObject(c.getString(c.getColumnIndex(COLUMN_DATA)));
                    arr.put(j);
                } catch (JSONException e) {
                    // Ignore this object
                }
            }

            if (arr.length() > 0) {
                data = arr.toString();
            }
        } catch (SQLiteException e) {
            RakeLogger.e(LOG_TAG_PREFIX, "getEventList " + tableName, e);

            // We'll dump the DB on write failures, but with reads we can
            // let things ride in hopes the issue clears up.
            // (A bit more likely, since we're opening the DB for read and not write.)
            // A corrupted or disk-full DB will be cleaned up on the next write or clear call.
            last_id = null;
            data = null;
        } finally {
            dbHelper.close();
            if (c != null) {
                c.close();
            }
        }

        if (last_id != null && data != null) {
            String[] ret = {last_id, data};
            return ret;
        }
        return null;
    }
}
