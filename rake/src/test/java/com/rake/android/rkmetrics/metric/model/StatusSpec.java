package com.rake.android.rkmetrics.metric.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class StatusSpec {

    @Test
    public void assertStatusEnumCount() {
        /** Status 는 최소한 1개 이어야 함 */
        assertThat(Status.values().length).isGreaterThan(0);
    }
}
