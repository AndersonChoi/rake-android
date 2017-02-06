package com.rake.android.rkmetrics.persistent;

import android.database.sqlite.SQLiteDatabase;

public class SQLiteUtil {
    public interface Callback<T> {
        T execute(SQLiteDatabase db);
        String getQuery();
    }
}
