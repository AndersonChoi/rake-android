package com.rake.android.rkmetrics;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

public class TestUtil {
    public static String genToken() {
        return UUID.randomUUID().toString();
    }

    public static void failWhenSuccess(Class<? extends Throwable> clazz, TestCallback callback) {
        try { callback.execute();
            failBecauseExceptionWasNotThrown(clazz);
        }
        catch (Exception e) {
            assertThat(e.getClass()).isEqualTo(clazz);
        }
    }
}
