package com.rake.android.rkmetrics.metric.model;

import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import static com.rake.android.rkmetrics.metric.model.Header.*;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class HeaderSpec {

    @Test
    public void fillHeader_should_return_false_given_shuttle_is_null() {

        RakeClientMetricSentinelShuttle shuttle = new RakeClientMetricSentinelShuttle();
        Header h = new Header();
        assertThat(h.fillShuttle(null)).isFalse();
    }

    @Test
    public void fillHeader_should_set_existing_header_fields_to_shuttle() {
        Header h = new Header();

        Action action = Action.CONFIGURE;
        Status status = Status.DONE;
        String appPackage = "example package";
        String transactionId = "example transactinoId";
        String serviceToken = "example serviceToken";

        h.setAction(Action.CONFIGURE);
        h.setStatus(status);
        h.setAppPackage(appPackage);
        h.setTransactionId(transactionId);
        h.setServiceToken(serviceToken);

        RakeClientMetricSentinelShuttle shuttle = new RakeClientMetricSentinelShuttle();
        h.fillShuttle(shuttle);

        hasHeaderValue(shuttle, HEADER_NAME_ACTION, action.getValue());
        hasHeaderValue(shuttle, HEADER_NAME_STATUS, status.getValue());
        hasHeaderValue(shuttle, HEADER_NAME_APP_PACKAGE, appPackage);
        hasHeaderValue(shuttle, HEADER_NAME_TRANSACTION_ID, transactionId);
        hasHeaderValue(shuttle, HEADER_NAME_SERVICE_TOKEN, serviceToken);
    }
}
