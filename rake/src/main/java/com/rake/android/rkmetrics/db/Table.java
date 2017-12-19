package com.rake.android.rkmetrics.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

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
        long _id = dbOpenHelper.getWritableDatabase().insert(tableName, null, values);

        return _id != -1;
    }

    boolean delete(String tableName, String whereClause, String... whereArgs) {
        long affectedRowCount = dbOpenHelper.getWritableDatabase().delete(tableName, whereClause, whereArgs);

        return affectedRowCount > 0;
    }

    int count(String tableName, String whereClause) {
        if (TextUtils.isEmpty(whereClause)) {
            whereClause = "";
        } else {
            whereClause = " " + whereClause;
        }

        String sql = "SELECT COUNT(*) FROM " + tableName + whereClause;

        Cursor cursor = dbOpenHelper.getReadableDatabase().rawQuery(sql, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    Cursor select(String rawQuery) {
        return dbOpenHelper.getReadableDatabase().rawQuery(rawQuery, null);
    }

    String getStringFromCursor(Cursor cursor, String columnIndex){
        return cursor.getString(cursor.getColumnIndex(columnIndex));
    }
}
