package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.metric.model.Status;

import static com.rake.android.rkmetrics.network.RakeProtocolV1.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19, manifest = Config.NONE)
public class RakeProtocolV1Spec {

    @Test
    public void isValidResponse_should_return_Status_DONE_when_responseBody_starts_with_1() {
        /** responseBody 가 1로 시작할 경우 DONE */

        String res1 = "1";
        String res2 = "1\n";
        String res3 = "1 ";

        /** code 에 상관 없이 */
        int statusCode1 = 0;
        int statusCode2 = 200;
        int statusCode3 = 500;

        assertThat(interpretResponse(res1, statusCode1)).isEqualTo(Status.DONE);
        assertThat(interpretResponse(res2, statusCode2)).isEqualTo(Status.DONE);
        assertThat(interpretResponse(res3, statusCode3)).isEqualTo(Status.DONE);
    }

    @Test
    public void isValidResponse_should_return_Status_RETRY_when_responseBody_is_NULL() {
        /** responseBody 가 null 일 경우 RETRY */

        String res1 = null;
        int statusCode1 = 200;

        assertThat(interpretResponse(res1, statusCode1)).isEqualTo(Status.RETRY);
    }

    @Test
    public void isValidResponse_should_return_Status_DROP_when_got_invalid_response() {
       /** responseBody 가 1 도 아니고 null 도 아닐 경우 DROP */

        String res1 = "invalid1";
        String res2 = "-1";
        String res3 = " 1";
        int statusCode1 = 500;
        int statusCode2 = 400;
        int statusCode3 = 200;

        assertThat(interpretResponse(res1, statusCode1)).isEqualTo(Status.DROP);
        assertThat(interpretResponse(res2, statusCode2)).isEqualTo(Status.DROP);
        assertThat(interpretResponse(res3, statusCode3)).isEqualTo(Status.DROP);
    }
}
