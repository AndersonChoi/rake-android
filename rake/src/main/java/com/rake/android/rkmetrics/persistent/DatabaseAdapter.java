package com.rake.android.rkmetrics.persistent;

import static com.rake.android.rkmetrics.config.RakeConfig.LOG_TAG_PREFIX;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import com.rake.android.rkmetrics.util.RakeLogger;

import java.io.File;

/**
 * Not thread-safe.
 * Instances of this class should only be used by a single thread.
 */
public abstract class DatabaseAdapter {
    public enum Table {
        EVENTS("events"),
        LOG("log");

        Table(String name) { tableName = name; }
        public String getName() {
            return tableName;
        }
        private final String tableName;
    }

    protected static final String DATABASE_NAME = "rake";
    protected static final String TEXT_TYPE_NOT_NULL = " TEXT NOT NULL";
    protected static final String STRING_TYPE_NOT_NULL = " STRING NOT NULL";
    protected static final String INTEGER_TYPE_NOT_NULL = " INTEGER NOT NULL";
    protected static final String INTEGER_PK_AUTO_INCREMENT = " INTEGER PRIMARY KEY AUTOINCREMENT";

    private static final int DATABASE_VERSION = 5;
    public static boolean upgradedFrom4To5 = false;

    protected static final String COMMA_SEP = ", ";
    protected static final String QUERY_END = ");";

    private static DatabaseHelper dbHelper;
    private static final Object lock = new Object();

    protected DatabaseAdapter(Context context) {
        synchronized (lock) { /* prevent from creating multiple database helpers */
            if (null == dbHelper) dbHelper = new DatabaseHelper(context, DATABASE_NAME);
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private final File database;
        DatabaseHelper(Context context, String dbName) {
            super(context, dbName, null, DATABASE_VERSION);
            database = context.getDatabasePath(dbName);
        }

        public void dropDatabase() {
            close();
            database.delete(); // delete the DB file from the file system completely.
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            RakeLogger.d(LOG_TAG_PREFIX, "Create database: " + DATABASE_NAME);

            String createTableQuery = LogTableAdapter.LogContract.QUERY_CREATE_TABLE;
            String createIndexQuery = LogTableAdapter.LogContract.QUERY_CREATE_INDEX;

            RakeLogger.d(LOG_TAG_PREFIX, "Create table with query: \n" + createTableQuery);
            RakeLogger.d(LOG_TAG_PREFIX, "Create index with query: \n" + createIndexQuery);

            db.execSQL(createTableQuery);
            db.execSQL(createIndexQuery);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String message = String.format("Upgrade Database [%s] from version %d to %d",
                    DATABASE_NAME, oldVersion, newVersion);

            RakeLogger.d(LOG_TAG_PREFIX, message);

            if (oldVersion < 4) { /* DO NOT SUPPORT */
                db.execSQL(EventTableAdapter.EventContract.QUERY_DROP_TABLE);
                db.execSQL(EventTableAdapter.EventContract.QUERY_CREATE_TABLE);
                db.execSQL(EventTableAdapter.EventContract.QUERY_CREATE_INDEX);
            }

            /**
             * Version 4 -> 5: `Log` 테이블이 추가되었음
             */
            if (oldVersion < 5) {
                db.execSQL(LogTableAdapter.LogContract.QUERY_CREATE_TABLE);
                db.execSQL(LogTableAdapter.LogContract.QUERY_CREATE_INDEX);
            }

            /** Event 테이블을 바로 Log 테이블로 옮길 수 없기 때문에 라이브 로그라 가정하고
             * Event 테이블 플러시를 위한 Flag 값을 설정
             */
            if (4 == oldVersion && 5 == newVersion)
                upgradedFrom4To5 = true;
        }
    }

    public void deleteDatabase() {
        dbHelper.dropDatabase();
    }

    protected void execute(SQLiteCallback callback) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            callback.execute(db);
        } catch (SQLiteException e) {
            String message = String.format("execute failed with query: %s", callback.getQuery());
            RakeLogger.e(LOG_TAG_PREFIX, message, e);
            // We assume that in general, the results of a SQL exception are
            // unrecoverable, and could be associated with an oversized or
            // otherwise unusable DB. Better to bomb it and get back on track
            // than to leave it junked up (and maybe filling up the disk.)
            dbHelper.dropDatabase();
        } catch (Exception e) {
            RakeLogger.e(LOG_TAG_PREFIX, "Uncaught exception", e);
        } finally {
            dbHelper.close();
        }
    }

    // TODO column index to enum type
    protected String getStringFromCursor(Cursor cursor, String columnIndex) throws SQLiteException {
        return cursor.getString(cursor.getColumnIndex(columnIndex));
    }

    protected <T> T executeAndReturnT(SQLiteCallback<T> callback) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            return callback.execute(db);
        } catch (SQLiteException e) {
            String message = String.format("executeAndReturnT failed with query: %s", callback.getQuery());
            RakeLogger.e(LOG_TAG_PREFIX, message, e);
            // We assume that in general, the results of a SQL exception are
            // unrecoverable, and could be associated with an oversized or
            // otherwise unusable DB. Better to bomb it and get back on track
            // than to leave it junked up (and maybe filling up the disk.)
            dbHelper.dropDatabase();
            return null;
        } catch (Exception e) {
            RakeLogger.e(LOG_TAG_PREFIX, "Uncaught exception", e);
            return null;
        } finally {
            dbHelper.close();
        }
    }
}
