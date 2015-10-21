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
import java.util.ArrayList;
import java.util.List;

import static com.rake.android.rkmetrics.config.RakeConfig.LOG_TAG_PREFIX;

/**
 * Not thread-safe.
 * Instances of this class should only be used by a single thread.
 */
public class DatabaseAdapter {
    public enum Table {
        EVENTS("events"),
        LOG("log");

        Table(String name) { tableName = name; }
        public String getName() {
            return tableName;
        }
        private final String tableName;
    }

    private static final String TEXT_TYPE = "TEXT";

    private static final String DATABASE_NAME = "rake";
    /**
     * version 5: `token`, `url` columns added
     */
    private static final int DATABASE_VERSION = 4;

    public static final String QUERY_SEP = ", ";
    public static final String QUERY_END = ");";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_CREATED_AT = "created_at";   /* INTEGER not null */

    /* EVENT TABLE specific constants */
    private static final String COLUMN_DATA = "data";               /* STRING not null */
    private static final String QUERY_CREATE_EVENTS_TABLE =
            "CREATE TABLE " + Table.EVENTS.getName() + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DATA + " STRING NOT NULL" + QUERY_SEP +
                    COLUMN_CREATED_AT + " INTEGER NOT NULL" + QUERY_END;
    private static final String QUERY_EVENTS_TIME_INDEX =
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

        private final File database;
        DatabaseHelper(Context context, String dbName) {
            super(context, dbName, null, DATABASE_VERSION);
            database = context.getDatabasePath(dbName);
        }

        public void deleteDatabase() {
            close();
            database.delete(); // Completely deletes the DB file from the file system.
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            RakeLogger.d(LOG_TAG_PREFIX, "Creating Database: " + DATABASE_NAME);

            db.execSQL(QUERY_CREATE_EVENTS_TABLE);
            db.execSQL(QUERY_EVENTS_TIME_INDEX);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String message = String.format("Upgrading Database [%s] from version %d to %d",
                    DATABASE_NAME, oldVersion, newVersion);

            RakeLogger.d(LOG_TAG_PREFIX, message);

            db.execSQL("DROP TABLE IF EXISTS " + Table.EVENTS.getName());
            db.execSQL(QUERY_CREATE_EVENTS_TABLE);
            db.execSQL(QUERY_EVENTS_TIME_INDEX);

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
     * @param json     the JSON to record
     * @return the number of rows in the table, or -1 on failure
     */
    public int addEvent(final JSONObject json) {
        final String table = Table.EVENTS.getName();

        Integer result = executeAndReturnT(new DatabaseCallback<Integer>() {
            @Override
            public Integer execute(SQLiteDatabase db) {

                Cursor c = null;
                ContentValues cv = new ContentValues();
                cv.put(COLUMN_DATA, json.toString());
                cv.put(COLUMN_CREATED_AT, System.currentTimeMillis());
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

        execute(new DatabaseCallback<Void>() {
            @Override
            public Void execute(SQLiteDatabase db) {
                db.delete(table, getQuery(), null);
                return null;
            }

            @Override
            public String getQuery() {
                return COLUMN_ID + " <= " + lastId;
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

        execute(new DatabaseCallback<Void>() {
            @Override
            public Void execute(SQLiteDatabase db) {
                db.delete(table, getQuery(), null);
                return null;
            }

            @Override
            public String getQuery() {
                return COLUMN_CREATED_AT + " <= " + time;
            }
        });
    }

    public void deleteDatabase() {
        dbHelper.deleteDatabase();
    }

    public void execute(DatabaseCallback callback) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            callback.execute(db);
        } catch (SQLiteException e) {
            String message = String.format("execute failed %s", callback.getQuery());
            RakeLogger.e(LOG_TAG_PREFIX, message, e);
            // We assume that in general, the results of a SQL exception are
            // unrecoverable, and could be associated with an oversized or
            // otherwise unusable DB. Better to bomb it and get back on track
            // than to leave it junked up (and maybe filling up the disk.)
            dbHelper.deleteDatabase();
        } catch (Exception e) {
            RakeLogger.e(LOG_TAG_PREFIX, "Uncaught exception", e);
        } finally {
            dbHelper.close();
        }
    }

    public <T> T executeAndReturnT(DatabaseCallback<T> callback) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            return callback.execute(db);
        } catch (SQLiteException e) {
            String message = String.format("executeAndReturnT failed %s", callback.getQuery());
            RakeLogger.e(LOG_TAG_PREFIX, message, e);
            // We assume that in general, the results of a SQL exception are
            // unrecoverable, and could be associated with an oversized or
            // otherwise unusable DB. Better to bomb it and get back on track
            // than to leave it junked up (and maybe filling up the disk.)
            dbHelper.deleteDatabase();
            return null;
        } catch (Exception e) {
            RakeLogger.e(LOG_TAG_PREFIX, "Uncaught exception", e);
        } finally {
            dbHelper.close();
        }
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

        Flushable<JSONObject> flushable = executeAndReturnT(new DatabaseCallback<Flushable<JSONObject>>() {
            @Override
            public Flushable<JSONObject> execute(SQLiteDatabase db) {
                Cursor c = null;
                String lastId = null;
                List<JSONObject> logList = new ArrayList<JSONObject>();

                try {
                    c = db.rawQuery(getQuery(), null);

                    while (c.moveToNext()) {
                        if (c.isLast()) lastId = c.getString(c.getColumnIndex(COLUMN_ID));

                        String log = c.getString(c.getColumnIndex(COLUMN_DATA));

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
                return "SELECT * FROM " + table + " ORDER BY " + COLUMN_CREATED_AT + " ASC LIMIT 50";
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
