package com.rake.android.rkmetrics.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.rake.android.rkmetrics.BuildConfig;

import org.apache.commons.lang.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19)
public class DatabaseOpenHelperSpec {
    private DatabaseOpenHelper databaseOpenHelper;

    @Before
    public void setUp() {
        databaseOpenHelper = new DatabaseOpenHelper(RuntimeEnvironment.application, "rake");
    }

    @Test
    public void testGetProperDatabase() {
        assertThat(databaseOpenHelper.getReadableDatabase()).isNotNull();

        assertThat(databaseOpenHelper.getWritableDatabase()).isNotNull();
    }

    @Test
    public void testGetLogTableColumns() {
        SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();
        Cursor cursor = db.query("log", null, null, null, null, null, null);
        String[] columnNames = cursor.getColumnNames();
        cursor.close();

        String[] expectedColumnNames = {LogTable.Columns.URL, LogTable.Columns.TOKEN, LogTable.Columns.LOG, LogTable.Columns.CREATED_AT};

        for (String expectedColumn : expectedColumnNames) {
            assertThat(ArrayUtils.contains(columnNames, expectedColumn));
        }
    }

    @After
    public void tearDown() {
        databaseOpenHelper.close();
    }
}
