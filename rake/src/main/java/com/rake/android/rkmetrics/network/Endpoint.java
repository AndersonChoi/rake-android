package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.RakeAPI.Env;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Endpoint {

    DEV_ENDPOINT_CHARGED("https://pg.rake.skplanet.com:8443/log/track", Env.DEV),
    DEV_ENDPOINT_FREE("https://pg.rake.skplanet.com:8553/log/track", Env.DEV),
    LIVE_ENDPOINT_CHARGED("https://rake.skplanet.com:8443/log/track", Env.LIVE),
    LIVE_ENDPOINT_FREE("https://rake.skplanet.com:8553/log/track", Env.LIVE);

    private final String uri;
    private final Env env;

    Endpoint(String uri, Env env) {
        this.uri = uri;
        this.env = env;
    }

    public String getUri() { return uri; }
    public Env getEnv() { return env; }

    private final static List<Endpoint> devEndpoints =
            Arrays.asList(DEV_ENDPOINT_FREE, DEV_ENDPOINT_CHARGED);

    private final static List<Endpoint> liveEndpoints =
            Arrays.asList(LIVE_ENDPOINT_FREE, LIVE_ENDPOINT_CHARGED);

    public static List<Endpoint> getDevEndpoints() {
        return new ArrayList<Endpoint>(devEndpoints); /* return immutable */
    }

    public static List<Endpoint> getLiveEndpoints() {
        return new ArrayList<Endpoint>(liveEndpoints); /* return immutable */
    }
}
