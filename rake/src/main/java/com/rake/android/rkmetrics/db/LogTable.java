package com.rake.android.rkmetrics.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteQuery;
import android.provider.BaseColumns;

import com.rake.android.rkmetrics.android.SystemInformation;
import com.rake.android.rkmetrics.db.value.Log;
import com.rake.android.rkmetrics.util.Logger;

/**
 * Created by 1000731 on 2017. 12. 13..
 */

public class LogTable extends Table {
    public static final String TABLE_NAME = "log";

    // log 테이블 columns
    public static class Columns implements BaseColumns {
        static final String URL = "url";                 /* STRING not null */
        static final String TOKEN = "token";             /* STRING not null */
        static final String LOG = "log";
        static final String CREATED_AT = "createdAt";
    }

    // 테이블 생성/삭제 쿼리
    static final String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
            Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
            Columns.URL + " TEXT NOT NULL, " +
            Columns.TOKEN + " TEXT NOT NULL, " +
            Columns.LOG + " TEXT NOT NULL, " +
            Columns.CREATED_AT + " INTEGER NOT NULL);";

    static final String QUERY_CREATE_INDEX = "CREATE INDEX IF NOT EXISTS craetedAt_idx ON " + TABLE_NAME +
            " (" + Columns.CREATED_AT + ");";

    public static final String QUERY_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    // singleton
    private static LogTable instance;

    private LogTable(Context context) {
        super(context);
    }

    public static synchronized LogTable getInstance(Context context){
        if(instance == null){
            instance = new LogTable(context);
        }
        return instance;
    }

    /**
     * insert Log into the 'log' table in the database 'rake'
     *
     * @param log Log object
     * @return true if Log is successfully added.
     * */
    public synchronized boolean addLog(Log log){
        if(log == null || log.getUrl() == null || log.getToken() == null || log.getJson() == null) {
            Logger.e("Cannot add log without args.");
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(Columns.URL, log.getUrl());
        values.put(Columns.TOKEN, log.getToken());
        values.put(Columns.LOG, log.getJson().toString());
        values.put(Columns.CREATED_AT, System.currentTimeMillis());

        return insert(TABLE_NAME, values);
    }

    /**
     * get count of logs by rake token
     *
     * @param token rake token
     * @return -1, if exception occurred or token is NULL
     * */
    public synchronized int getCount(String token) {
        if(token == null) {
            return -1;
        }

        String whereClause = String.format("WHERE %s = \"%s\"", Columns.TOKEN, token);
        return count(TABLE_NAME, whereClause);
    }

    /**
     * remove logs from 'log' table which were created before time
     *
     * @param time remove logs before time
     * @return true if Logs are successfully added.
     * */
    public synchronized boolean removeLogsBefore(long time){
        if(time <= 0) {
            return false;
        }

        String whereClause = Columns.CREATED_AT + " <= " + time;
        return delete(TABLE_NAME, whereClause, null);
    }



}
