package com.rake.android.rkmetrics.persistent;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class LogSpec {

    @Test
    public void create_url_또는_token_또는_json_이_null_일때() {
        assertNull("url is null", Log.create(null, "token", new JSONObject()));
        assertNull("token is null", Log.create("url", null, new JSONObject()));
        assertNull("json is null", Log.create("url", "token", null));
    }

    @Test
    public void create() {
        assertNotNull(Log.create("url", "token", new JSONObject()));
    }

    @Test(expected = IllegalAccessException.class)
    public void default_생성자를_리플렉션으로_직접호출시()
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class clazz = Class.forName(Log.class.getName());
        clazz.newInstance();
    }
}
