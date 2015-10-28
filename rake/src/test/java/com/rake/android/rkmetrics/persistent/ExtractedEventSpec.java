package com.rake.android.rkmetrics.persistent;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class ExtractedEventSpec {

    @Test
    public void create_lastId_가_null_일때() {
        JSONArray arr = new JSONArray();
        arr.put(new JSONObject());

        assertNull(ExtractedEvent.create(null, arr));
    }

    @Test
    public void create_jsonArr_가_null_일때() {
        String lastId = "20150810014955";

        assertNull(ExtractedEvent.create(lastId, null));
    }

    @Test
    public void create_jsonArr_size_가_0_일때() {
        String lastId = "20150810014955";
        JSONArray arr = new JSONArray();

        assertNull(ExtractedEvent.create(lastId, arr));
    }

    @Test(expected = RuntimeException.class)
    public void default_생성자를_리플렉션으로_직접호출시()
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class clazz = Class.forName(ExtractedEvent.class.getName());

        clazz.newInstance();
    }

    @Test
    public void create() {
        String lastId = "20150810014955";
        JSONArray arr = new JSONArray();
        arr.put(new JSONObject());

        assertNotNull(ExtractedEvent.create(lastId, arr));
    }
}
