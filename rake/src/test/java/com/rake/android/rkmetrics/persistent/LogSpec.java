package com.rake.android.rkmetrics.persistent;

import com.rake.android.rkmetrics.db.value.Log;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class LogSpec {

    @Test
    public void create_url_또는_token_또는_json_이_null_일때() {
        assertNull("url is null", new Log(null, "token", new JSONObject()).getUrl());
        assertNull("token is null", new Log("url", null, new JSONObject()).getToken());
        assertNull("json is null", new Log("url", "token", null).getJSON());
    }

    @Test
    public void create() {
        Log log = new Log("url", "token", new JSONObject());
        assertNotNull(log.getUrl());
        assertNotNull(log.getToken());
        assertNotNull(log.getJSON());
    }
//
//    @Test(expected = IllegalAccessException.class)
//    public void default_생성자를_리플렉션으로_직접호출시()
//            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
//        Class clazz = Class.forName(Log.class.getName());
//        clazz.newInstance();
//    }
}
