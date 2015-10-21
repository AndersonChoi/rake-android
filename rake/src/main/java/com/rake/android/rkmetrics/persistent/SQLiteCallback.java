package com.rake.android.rkmetrics.persistent;

import android.database.sqlite.SQLiteDatabase;

public interface SQLiteCallback<T> {
    <T> T execute(SQLiteDatabase db);
    String getQuery();
}
