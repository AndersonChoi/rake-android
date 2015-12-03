package com.rake.android.rkmetrics.util.functional;

public interface Callback<T, R> {
    R execute(T arg);
}
