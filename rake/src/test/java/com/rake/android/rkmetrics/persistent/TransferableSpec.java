package com.rake.android.rkmetrics.persistent;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class TransferableSpec {

    @Test
    public void create_lastId_또는_logs_가_null_또는_logs_size_가_0_일때() {

        assertNull("both null",
                Transferable.create(null, null));

        assertNull("lastId is null and logs is empty",
                Transferable.create(null, Collections.EMPTY_LIST));

        assertNull("logs is empty",
                Transferable.create("lastId", Collections.EMPTY_LIST));

    }

    @Test
    public void create() {
        Log l1 = Log.create("url", "token", new JSONObject());
        List<Log> logs = new ArrayList<Log>();
        logs.add(l1);

        assertNotNull("last id and logs are not null, and logs is not empty",
                Transferable.create("lastId", logs));
    }

    @Test
    public void create__token_과_url_이_여러개일때() {
        List<Log> logs = new ArrayList<Log>();
        logs.add(Log.create("url1", "token1", new JSONObject()));
        logs.add(Log.create("url2", "token2", new JSONObject()));
        logs.add(Log.create("url2", "token3", new JSONObject()));

        Transferable t = Transferable.create("lastId", logs);

        assertEquals(2, t.getUrls().size());
        assertEquals(2, t.getLogMap().keySet().size());

        assertEquals(1, t.getTokens("url1").size());
        assertEquals(2, t.getTokens("url2").size());
    }

    @Test(expected = IllegalAccessException.class)
    public void default_생성자를_리플렉션으로_직접호출시()
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class clazz = Class.forName(Transferable.class.getName());
        clazz.newInstance();
    }

    // TODO countMap test


}
