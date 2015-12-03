package com.rake.android.rkmetrics;

import java.util.UUID;

public class TestUtil {
    public static String genToken() {
        return UUID.randomUUID().toString();
    }
}
