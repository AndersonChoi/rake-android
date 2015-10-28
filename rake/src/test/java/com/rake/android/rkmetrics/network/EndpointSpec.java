package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.RakeAPI.Env;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class EndpointSpec {

    @Test
    public void DEV_ENDPOINT_는_Env_DEV_를_가져야함() {
        for(Endpoint e : Endpoint.getDevEndpoints()) {
            assertEquals(Env.DEV, e.getEnv());
        }
    }

    @Test
    public void LIVE_ENDPOINT_는_Env_LIVE_를_가져야함() {
        for(Endpoint e : Endpoint.getLiveEndpoints()) {
            assertEquals(Env.LIVE, e.getEnv());
        }
    }

    @Test
    public void get_dev_live_endpoints_를_합치면_전체를_돌려줘야함() {
        List<Endpoint> all = new ArrayList<Endpoint>();
        all.addAll(Endpoint.getDevEndpoints());
        all.addAll(Endpoint.getLiveEndpoints());

        for(Endpoint e : Endpoint.values()) {
           assertTrue(all.contains(e));
        }
    }
}
