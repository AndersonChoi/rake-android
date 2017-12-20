package com.rake.android.rkmetrics.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

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

    boolean insert(String tableName, ContentValues values) {
        long _id = -1;

        try {
            _id = dbOpenHelper.getWritableDatabase().insert(tableName, null, values);
        } catch (SQLiteException e) {
            Logger.e("[SQLite] insertion error : " + e.getMessage());
        } finally {
            dbOpenHelper.close();
        }

        return _id != -1;
    }

    boolean delete(String tableName, String whereClause, String... whereArgs) {
        long affectedRowCount = 0;

        try {
            affectedRowCount = dbOpenHelper.getWritableDatabase().delete(tableName, whereClause, whereArgs);
        } catch (SQLiteException e) {
            Logger.e("[SQLite] deletion error : " + e.getMessage());
        } finally {
            dbOpenHelper.close();
        }

        return affectedRowCount > 0;
    }

    int count(String tableName, String whereClause) {
        if (TextUtils.isEmpty(whereClause)) {
            whereClause = "";
        } else {
            whereClause = " " + whereClause;
        }

        String sql = "SELECT COUNT(*) FROM " + tableName + whereClause;

        Cursor cursor = null;
        int count = 0;

        try {
            cursor = dbOpenHelper.getReadableDatabase().rawQuery(sql, null);
            cursor.moveToFirst();
            count = cursor.getInt(0);
        } catch (SQLiteException e) {
            Logger.e("[SQLite] selection (count) error : " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbOpenHelper.close();
        }

        return count;
    }

    Cursor select(String rawQuery) {
        Cursor cursor = null;

        try {
            cursor = dbOpenHelper.getReadableDatabase().rawQuery(rawQuery, null);
        } catch (SQLiteException e) {
            Logger.e("[SQLite] selection error : " + e.getMessage());
        } finally {
            dbOpenHelper.close();
        }

        return cursor;
    }

    String getStringFromCursor(Cursor cursor, String columnIndex) {
        return cursor.getString(cursor.getColumnIndex(columnIndex));
    }

}
