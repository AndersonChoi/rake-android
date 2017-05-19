package com.rake.android.rkmetrics.util;

import java.io.Closeable;
import java.io.IOException;

public class StreamUtil {
    public static void closeQuietly(Closeable stream) {
        try {
            if (null != stream) {
                stream.close();
            }
        } catch (IOException e) {
            Logger.e("closeQuietly failed", e);
        }
    }
}
