package com.rake.android.rkmetrics.db;

import android.content.Context;

import com.rake.android.rkmetrics.BuildConfig;
import com.rake.android.rkmetrics.config.RakeConfig;
import com.rake.android.rkmetrics.db.log.Log;
import com.rake.android.rkmetrics.db.log.LogBundle;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = Config.NONE)
public class LogTableSpec {
    private Context context;

    @Before
    public void setUp(){
        context = RuntimeEnvironment.application;
    }

    @Test
    public void test_addLog(){
        Log log = new Log("url", "token", new JSONObject());

        long dataCount = LogTable.getInstance(context).addLog(log);

        assertThat(dataCount != -1);
        assertThat(dataCount == 1);
    }

    @Test
    public void test_removeLogsBefore(){
        long time = System.currentTimeMillis();
        boolean removed = LogTable.getInstance(context).removeLogsBefore(time);

        assertThat(removed);
    }

    @Test
    public void test_getCount(){
        String token1 = "token1";
        String token2 = "token2";

        // add logs which token is token1
        LogTable.getInstance(context).addLog(new Log("url", token1, new JSONObject()));
        LogTable.getInstance(context).addLog(new Log("url", token1, new JSONObject()));
        LogTable.getInstance(context).addLog(new Log("url", token1, new JSONObject()));

        // add logs which token is token2
        LogTable.getInstance(context).addLog(new Log("url", token2, new JSONObject()));
        LogTable.getInstance(context).addLog(new Log("url", token2, new JSONObject()));

        assertThat(LogTable.getInstance(context).getCount(token1) == 3);
        assertThat(LogTable.getInstance(context).getCount(token2) == 2);
    }

    @Test
    public void test_getLogBundles(){
        int maxLogCount = RakeConfig.TRACK_MAX_LOG_COUNT;

        String token1 = "token1";
        String token2 = "token2";

        // add logs which token is token1
        LogTable.getInstance(context).addLog(new Log("url", token1, new JSONObject()));
        LogTable.getInstance(context).addLog(new Log("url", token1, new JSONObject()));
        LogTable.getInstance(context).addLog(new Log("url", token1, new JSONObject()));

        // add logs which token is token2
        LogTable.getInstance(context).addLog(new Log("url", token2, new JSONObject()));
        LogTable.getInstance(context).addLog(new Log("url", token2, new JSONObject()));

        List<LogBundle> logBundleList = LogTable.getInstance(context).getLogBundles(maxLogCount);

        assertThat(logBundleList != null);
        assertThat((logBundleList != null ? logBundleList.size() : 0) != 2);
    }

    @Test
    public void test_removeLogBundle(){
        String token1 = "token1";
        String token2 = "token2";

        // add logs which token is token1
        LogTable.getInstance(context).addLog(new Log("url", token1, new JSONObject()));
        LogTable.getInstance(context).addLog(new Log("url", token1, new JSONObject()));
        LogTable.getInstance(context).addLog(new Log("url", token1, new JSONObject()));

        // add logs which token is token2
        LogTable.getInstance(context).addLog(new Log("url", token2, new JSONObject()));
        LogTable.getInstance(context).addLog(new Log("url", token2, new JSONObject()));

        List<LogBundle> logBundleList = LogTable.getInstance(context).getLogBundles(RakeConfig.TRACK_MAX_LOG_COUNT);

        boolean removed = LogTable.getInstance(context).removeLogBundle(logBundleList.get(0));

        assertThat(removed);
    }
}
