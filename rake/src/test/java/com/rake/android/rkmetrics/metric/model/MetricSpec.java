package com.rake.android.rkmetrics.metric.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.robolectric.annotation.Config.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19, manifest = NONE)
public class MetricSpec {
    @Test
    public void create_FlushMetric() {
        FlushMetric fm = new FlushMetric();
        fm.toJSONObject();
    }

    @Test
    public void create_InstallMetric() {
        InstallMetric im = new InstallMetric();
        im.toJSONObject();
    }

    @Test
    public void create_EmptyMetric() {
        EmptyMetric em = new EmptyMetric();
        em.toJSONObject();
    }
}
