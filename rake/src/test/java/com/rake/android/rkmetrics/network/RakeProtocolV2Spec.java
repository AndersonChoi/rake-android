package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.metric.model.Status;

import static com.rake.android.rkmetrics.network.RakeProtocolV2.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19, manifest = Config.NONE)
public class RakeProtocolV2Spec {

    @Test
    public void isValidResponse_should_return_DONE_when_responseBody_starts_with_1_and_responseCode_is_200() {
        /** responseBody 가 1로 시작하고 responseCode 가 경우만 DONE */

        int validStatusCode = 200;

        assertThat(interpretResponse(validStatusCode)).isEqualTo(Status.DONE);
    }

    @Test
    public void interpretResponse_should_return_Status_DROP_when_got_invalid_response() {

        int statusCode1 = 304;
        int statusCode2 = 401;
        int statusCode3 = 201;

        assertThat(interpretResponse(statusCode1)).isEqualTo(Status.DROP);
        assertThat(interpretResponse(statusCode2)).isEqualTo(Status.DROP);
        assertThat(interpretResponse(statusCode3)).isEqualTo(Status.DROP);
    }

    @Test
    public void interpretResponse_should_return_Status_DROP_when_got_status_code_413() {
        assertThat(interpretResponse(HTTP_STATUS_CODE_REQUEST_TOO_LONG)).isEqualTo(Status.DROP);
    }

    @Test
    public void interpretResponse_should_return_Status_DROP_when_got_status_code_500_502_503() {
        List<Integer> retryCodes = Arrays.asList(
                HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR,
                HTTP_STATUS_CODE_BAD_GATEWAY,
                HTTP_STATUS_CODE_SERVICE_UNAVAILABLE);

        for (int code : retryCodes) {
            assertThat(interpretResponse(code)).isEqualTo(Status.RETRY);
            assertThat(interpretResponse(code)).isEqualTo(Status.RETRY);
            assertThat(interpretResponse(code)).isEqualTo(Status.RETRY);
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

    @Test
    public void assert_RAKE_PROTOCOL_constants() {
        /** 변경되면 안되므로 하드코딩으로 검증 */
        assertThat(CHAR_ENCODING).isEqualTo("UTF-8");
        assertThat(RAKE_PROTOCOL_VERSION).isEqualTo("V2");
    }
}
