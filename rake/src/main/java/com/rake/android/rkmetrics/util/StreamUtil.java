package com.rake.android.rkmetrics.util;

import com.rake.android.rkmetrics.config.RakeConfig;

import java.io.Closeable;
import java.io.IOException;

public class StreamUtil {
    public static void closeQuietly(Closeable stream) {
        try {
            if (null != stream) stream.close();
        } catch (IOException e) {
            Logger.e(RakeConfig.LOG_TAG_PREFIX, "closeQuietly failed", e);
        }
    }
}
