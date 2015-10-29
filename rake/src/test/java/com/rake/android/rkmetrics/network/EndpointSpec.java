package com.rake.android.rkmetrics.network;

import static com.rake.android.rkmetrics.RakeAPI.Env.*;
import static com.rake.android.rkmetrics.network.Endpoint.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class EndpointSpec {

    @Test
    public void ENDPOINT_검증() {
       /** ENDPOINT 추가 또는 변화시에는 이 테스트 코드를 반드시 변경하도록 하드코딩으로 URI 검증 */

        String CHARGED_ENDPOINT_DEV  = "https://pg.rake.skplanet.com:8443/log/track";
        String FREE_ENDPOINT_DEV     = "https://pg.rake.skplanet.com:8553/log/track";
        String CHARGED_ENDPOINT_LIVE = "https://rake.skplanet.com:8443/log/track";
        String FREE_ENDPOINT_LIVE    = "https://rake.skplanet.com:8553/log/track";

        assertEquals(CHARGED_ENDPOINT_DEV, CHARGED.getURI(DEV));
        assertEquals(FREE_ENDPOINT_DEV, FREE.getURI(DEV));
        assertEquals(CHARGED_ENDPOINT_LIVE, CHARGED.getURI(LIVE));
        assertEquals(FREE_ENDPOINT_LIVE, FREE.getURI(LIVE));
    }
}
