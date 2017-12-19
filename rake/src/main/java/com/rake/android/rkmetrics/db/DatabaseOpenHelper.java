package com.rake.android.rkmetrics.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 1000731 on 2017. 12. 13..
 */

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 5;


    DatabaseOpenHelper(Context context, String name) {
        super(context, name, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LogTable.QUERY_CREATE_TABLE);
        db.execSQL(LogTable.QUERY_CREATE_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < DATABASE_VERSION) {
            db.execSQL("DROP TABLE IF EXISTS events");  // Database version 4에서 쓰던 테이블 삭제

            // DB 버전 업그레이드시 구버전 테이블은 드롭 후 새로 만든다.
            db.execSQL(LogTable.QUERY_DROP_TABLE);

            db.execSQL(LogTable.QUERY_CREATE_TABLE);
            db.execSQL(LogTable.QUERY_CREATE_INDEX);
        }
    }
}
