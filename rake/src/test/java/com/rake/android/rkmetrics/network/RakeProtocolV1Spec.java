package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.metric.model.Status;

import static com.rake.android.rkmetrics.network.RakeProtocolV1.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19, manifest = Config.NONE)
public class RakeProtocolV1Spec {

    @Test
    public void isValidResponse_should_return_DONE_when_responseBody_starts_with_1_and_responseCode_is_200() {
        /** responseBody 가 1로 시작하고 responseCode 가 경우만 DONE */

        String validResponseBody1 = "1";
        String validResponseBody2 = "1\n";
        String validResponseBody3 = "1 ";


        int validStatusCode = 200;

        assertThat(interpretResponse(validResponseBody1, validStatusCode)).isEqualTo(Status.DONE);
        assertThat(interpretResponse(validResponseBody2, validStatusCode)).isEqualTo(Status.DONE);
        assertThat(interpretResponse(validResponseBody3, validStatusCode)).isEqualTo(Status.DONE);

        /** negative cases */

        String invalidResponseBody1 = null;
        String invalidResponseBody2 = "";
        String invalidResponseBody3 = "-1";
        int invalidStatusCode1 = 201;
        int invalidStatusCode2 = 401;
        int invalidStatusCode3 = 500;

        assertThat(interpretResponse(validResponseBody1, invalidStatusCode1)).isNotEqualTo(Status.DONE);
        assertThat(interpretResponse(validResponseBody1, invalidStatusCode2)).isNotEqualTo(Status.DONE);
        assertThat(interpretResponse(validResponseBody1, invalidStatusCode3)).isNotEqualTo(Status.DONE);

        assertThat(interpretResponse(invalidResponseBody1, validStatusCode)).isNotEqualTo(Status.DONE);
        assertThat(interpretResponse(invalidResponseBody2, validStatusCode)).isNotEqualTo(Status.DONE);
        assertThat(interpretResponse(invalidResponseBody3, validStatusCode)).isNotEqualTo(Status.DONE);
    }

    @Test
    public void interpretResponse_should_return_Status_RETRY_when_responseBody_is_NULL() {
        /** responseBody 가 null 일 경우 DROP */

        String res1 = null;
        int statusCode1 = 200;

        assertThat(interpretResponse(res1, statusCode1)).isEqualTo(Status.DROP);
    }

    @Test
    public void interpretResponse_should_return_Status_DROP_when_got_invalid_response() {
       /** responseBody 가 1 도 아니고 null 도 아닐 경우 DROP */

        String res1 = "invalid1";
        String res2 = "-1";
        String res3 = " 1";
        int statusCode1 = 304;
        int statusCode2 = 401;
        int statusCode3 = 200;

        assertThat(interpretResponse(res1, statusCode1)).isEqualTo(Status.DROP);
        assertThat(interpretResponse(res2, statusCode2)).isEqualTo(Status.DROP);
        assertThat(interpretResponse(res3, statusCode3)).isEqualTo(Status.DROP);
    }

    @Test
    public void interpretResponse_should_return_Status_DROP_when_got_status_code_413() {

        assertThat(interpretResponse("" , HTTP_STATUS_CODE_REQUEST_TOO_LONG)).isEqualTo(Status.DROP);
        assertThat(interpretResponse("1" , HTTP_STATUS_CODE_REQUEST_TOO_LONG)).isEqualTo(Status.DROP);
        assertThat(interpretResponse(null , HTTP_STATUS_CODE_REQUEST_TOO_LONG)).isEqualTo(Status.DROP);
    }

    @Test
    public void interpretResponse_should_return_Status_DROP_when_got_status_code_500_502_503() {
        List<Integer> retryCodes = Arrays.asList(
                HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR,
                HTTP_STATUS_CODE_BAD_GATEWAY,
                HTTP_STATUS_CODE_SERVICE_UNAVAILABLE);

        for (int code : retryCodes) {
            assertThat(interpretResponse("" , code)).isEqualTo(Status.RETRY);
            assertThat(interpretResponse("1" , code)).isEqualTo(Status.RETRY);
            assertThat(interpretResponse(null , code)).isEqualTo(Status.RETRY);
        }
    }

    @Test
    public void assert_HTTP_STATUS_CODE() {
        /** 변경되면 안되므로 하드코딩으로 검증 */
        assertThat(HTTP_STATUS_CODE_OK).isEqualTo(200);
        assertThat(HTTP_STATUS_CODE_REQUEST_TOO_LONG).isEqualTo(413);
        assertThat(HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR).isEqualTo(500);
        assertThat(HTTP_STATUS_CODE_BAD_GATEWAY).isEqualTo(502);
        assertThat(HTTP_STATUS_CODE_SERVICE_UNAVAILABLE).isEqualTo(503);
    }
}
