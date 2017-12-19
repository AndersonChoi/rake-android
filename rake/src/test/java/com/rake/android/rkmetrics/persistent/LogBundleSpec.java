package com.rake.android.rkmetrics.persistent;


import com.rake.android.rkmetrics.db.value.Log;
import com.rake.android.rkmetrics.db.value.LogBundle;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@RunWith(JUnit4.class)
public class LogBundleSpec {

    @Test
    public void create_lastId_또는_logs_가_null_또는_logs_size_가_0_일때() {

//        assertThat(LogBundle.create(null, null)).isNull();
//        assertThat(LogBundle.create(null, Collections.EMPTY_LIST)).isNull();
//        assertThat(LogBundle.create("lastId-1", Collections.EMPTY_LIST)).isNull();
    }

    @Test
    public void create() {
//        Log l1 = Log.create("url", "token", new JSONObject());
//        List<Log> logs = new ArrayList<>();
//        logs.add(l1);
//
//        assertThat(LogBundle.create("lastId-1", logs)).isNotNull();
    }

    @Test
    public void create__token_과_url_이_여러개일때() {
//        List<Log> logs = new ArrayList<>();
//        logs.add(Log.create("url1", "token1", new JSONObject()));
//        logs.add(Log.create("url1", "token1", new JSONObject()));
//        logs.add(Log.create("url2", "token2", new JSONObject()));
//        logs.add(Log.create("url2", "token3", new JSONObject()));
//
//        List<LogBundle> chunks = LogBundle.create("lastId", logs);
//
//        assertThat(chunks.size()).isEqualTo(3);
//
//        assertThat(chunks.get(0).getCount()).isEqualTo(2);
//        assertThat(chunks.get(1).getCount()).isEqualTo(1);
//        assertThat(chunks.get(2).getCount()).isEqualTo(1);
    }

//    @Test(expected = IllegalAccessException.class)
//    public void default_생성자를_리플렉션으로_직접호출시()
//            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
//        Class clazz = Class.forName(LogBundle.class.getName());
//        clazz.newInstance();
//    }
}
