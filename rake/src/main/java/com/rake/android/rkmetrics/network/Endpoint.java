package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.RakeAPI.Env;

import java.util.HashMap;
import java.util.Map;

public enum Endpoint {
    CHARGED("CHANGED"),
    FREE("FREE");

    public final static String CHARGED_ENDPOINT_DEV  = "https://pg.rake.skplanet.com:8443/log/track";
    public final static String FREE_ENDPOINT_DEV     = "https://pg.rake.skplanet.com:8553/log/track";
    public final static String CHARGED_ENDPOINT_LIVE = "https://rake.skplanet.com:8443/log/track";
    public final static String FREE_ENDPOINT_LIVE    = "https://rake.skplanet.com:8553/log/track";

    public final static Endpoint DEFAULT = CHARGED;

    private String value;

    Endpoint(String value) { this.value = value;}
    /**
     * 사용자에게 노출시키지 않기 위해 인스턴스 변수로 만들기 static block 에서 초기화
     */

    private Map<Env, String> uriPerEnv;

    static {
        CHARGED.uriPerEnv = new HashMap<Env, String>();
        CHARGED.uriPerEnv.put(Env.DEV, CHARGED_ENDPOINT_DEV);
        CHARGED.uriPerEnv.put(Env.LIVE, CHARGED_ENDPOINT_LIVE);

        FREE.uriPerEnv = new HashMap<Env, String>();
        FREE.uriPerEnv.put(Env.DEV, FREE_ENDPOINT_DEV);
        FREE.uriPerEnv.put(Env.LIVE, FREE_ENDPOINT_LIVE);
    }

    public String getURI(Env env) {
        return uriPerEnv.get(env);
    }
}
