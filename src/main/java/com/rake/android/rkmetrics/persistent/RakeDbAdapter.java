package com.rake.android.rkmetrics.persistent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.rake.android.rkmetrics.RakeConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Not thread-safe.
 * Instances of this class should only be used by a single thread.
 */
public class RakeDbAdapter {
    private static final String TAG = "RakeAPI";

    public enum Table {
        EVENTS("events");

        Table(String name) {
            tableName = name;
        }

        public String getName() {
            return tableName;
        }

        private final String tableName;
    }

    private static final String DATABASE_NAME = "rake";
    private static final int DATABASE_VERSION = 4;

    public static final String KEY_DATA = "data";
    public static final String KEY_CREATED_AT = "created_at";

    private static final String CREATE_EVENTS_TABLE =
            "CREATE TABLE " + Table.EVENTS.getName() + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_DATA + " STRING NOT NULL, " +
                    KEY_CREATED_AT + " INTEGER NOT NULL);";
    private static final String EVENTS_TIME_INDEX =
            "CREATE INDEX IF NOT EXISTS time_idx ON " + Table.EVENTS.getName() +
                    " (" + KEY_CREATED_AT + ");";

    private final MPDatabaseHelper dbHelper;

    private static class MPDatabaseHelper extends SQLiteOpenHelper {

        private final File databaseFile;
        MPDatabaseHelper(Context context, String dbName) {
            super(context, dbName, null, DATABASE_VERSION);
            databaseFile = context.getDatabasePath(dbName);
        }

        /**
         * Completely deletes the DB file from the file system.
         */
        public void deleteDatabase() {
            close();
            databaseFile.delete();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if (RakeConfig.DEBUG) Log.d(TAG, "Creating a new Rake events DB");

            db.execSQL(CREATE_EVENTS_TABLE);
            db.execSQL(EVENTS_TIME_INDEX);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (RakeConfig.DEBUG) Log.d(TAG, "Upgrading app, replacing Rake events DB");

            db.execSQL("DROP TABLE IF EXISTS " + Table.EVENTS.getName());
            db.execSQL(CREATE_EVENTS_TABLE);
            db.execSQL(EVENTS_TIME_INDEX);
        }
    }

    public RakeDbAdapter(Context context) {
        this(context, DATABASE_NAME);
    }

    public RakeDbAdapter(Context context, String dbName) {
        if (RakeConfig.DEBUG)
            Log.d(TAG, "Rake Database (" + dbName + ") adapter constructed in context " + context);

        dbHelper = new MPDatabaseHelper(context, dbName);
    }

    /**
     * Adds a JSON string representing an event with properties or a person record
     * to the SQLiteDatabase.
     *
     * @param j     the JSON to record
     * @param table the table to insert into, either "events"
     * @return the number of rows in the table, or -1 on failure
     */
    public int addJSON(JSONObject j, Table table) {
        String tableName = table.getName();
        if (RakeConfig.DEBUG) {
            Log.d(TAG, "addJSON " + tableName);
        }

        Cursor c = null;
        int count = -1;

        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put(KEY_DATA, j.toString());
            cv.put(KEY_CREATED_AT, System.currentTimeMillis());
            db.insert(tableName, null, cv);

            c = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
            c.moveToFirst();
            count = c.getInt(0);
        } catch (SQLiteException e) {
            Log.e(TAG, "addJSON " + tableName + " FAILED. Deleting DB.", e);

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
     * @param table   the table to remove events from, either "events"
     */
    public void cleanupEvents(String last_id, Table table) {
        String tableName = table.getName();
        if (RakeConfig.DEBUG) {
            Log.d(TAG, "cleanupEvents _id " + last_id + " from table " + tableName);
        }

        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete(tableName, "_id <= " + last_id, null);
        } catch (SQLiteException e) {
            Log.e(TAG, "cleanupEvents " + tableName + " by id FAILED. Deleting DB.", e);

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
     * @param table the table to remove events from, either "events"
     */
    public void cleanupEvents(long time, Table table) {
        String tableName = table.getName();
        if (RakeConfig.DEBUG) {
            Log.d(TAG, "cleanupEvents time " + time + " from table " + tableName);
        }

        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete(tableName, KEY_CREATED_AT + " <= " + time, null);
        } catch (SQLiteException e) {
            Log.e(TAG, "cleanupEvents " + tableName + " by time FAILED. Deleting DB.", e);

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
     * @param table the table to read the JSON from, either "events"
     * @return String array containing the maximum ID and the data string
     * representing the events, or null if none could be successfully retrieved.
     */
    public String[] generateDataString(Table table) {
        Cursor c = null;
        String data = null;
        String last_id = null;
        String tableName = table.getName();

        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            c = db.rawQuery("SELECT * FROM " + tableName +
                    " ORDER BY " + KEY_CREATED_AT + " ASC LIMIT 50", null);
            JSONArray arr = new JSONArray();

            while (c.moveToNext()) {
                if (c.isLast()) {
                    last_id = c.getString(c.getColumnIndex("_id"));
                }
                try {
                    JSONObject j = new JSONObject(c.getString(c.getColumnIndex(KEY_DATA)));
                    arr.put(j);
                } catch (JSONException e) {
                    // Ignore this object
                }
            }

            if (arr.length() > 0) {
                data = arr.toString();
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "generateDataString " + tableName, e);

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
