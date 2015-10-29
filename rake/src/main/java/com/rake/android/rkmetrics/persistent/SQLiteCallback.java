package com.rake.android.rkmetrics.persistent;

import android.database.sqlite.SQLiteDatabase;

interface SQLiteCallback<T> {
    T execute(SQLiteDatabase db);
    String getQuery();
}
