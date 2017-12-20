package com.rake.android.rkmetrics.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.rake.android.rkmetrics.util.Logger;

/**
 * Created by 1000731 on 2017. 12. 13..
 */

public class Table {
    private static final String DATABASE_NAME = "rake";

    private static DatabaseOpenHelper dbOpenHelper;

    private Object lockObject = new Object();

    Table(Context context) {
        synchronized (lockObject) {
            if (dbOpenHelper == null) {
                dbOpenHelper = new DatabaseOpenHelper(context, DATABASE_NAME);
            }
        }
    }

    interface QueryExecCallback<T> {
        T execute(SQLiteDatabase db);
    }

    <T> T queryExecutor(QueryExecCallback<T> callback) {
        try {
            return callback.execute(dbOpenHelper.getWritableDatabase());
        } catch (SQLiteException e) {
            Logger.e("[SQLite] query execution error : " + e.getMessage());
            return null;
        } finally {
            dbOpenHelper.close();
        }
    }

    String getStringFromCursor(Cursor cursor, String columnIndex) {
        return cursor.getString(cursor.getColumnIndex(columnIndex));
    }
}
