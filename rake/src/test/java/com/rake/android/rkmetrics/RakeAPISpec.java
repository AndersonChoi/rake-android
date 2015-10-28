package com.rake.android.rkmetrics;

import android.app.Application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class RakeAPISpec {

    Application app = RuntimeEnvironment.application;
    RakeAPI rake = RakeAPI.getInstance(
            app,
            "exampleToken",
            RakeAPI.Env.DEV,
            RakeAPI.Logging.ENABLE
            );

    @Test
    public void setFlushInterval() {
        long defaultInterval = MessageLoop.DEFAULT_FLUSH_INTERVAL;
        long newInterval = 5 * 1000L;

        assertEquals(MessageLoop.DEFAULT_FLUSH_INTERVAL,
                RakeAPI.getFlushInterval());

        RakeAPI.setFlushInterval(newInterval);

        assertEquals(newInterval,
                RakeAPI.getFlushInterval());
    }
}
