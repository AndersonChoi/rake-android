package com.rake.android.rkmetrics.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.InvalidClassException;

import static org.assertj.core.api.Assertions.*;

@RunWith(JUnit4.class)
public class ExceptionUtilSpec {

    @Test
    public void 테스트_getExceptionType() {
        Exception e = new IllegalArgumentException("e");

        assertThat(ExceptionUtil.createExceptionType(null)).isNull();
        assertThat(ExceptionUtil.createExceptionType(e)).isNotNull();
    }

    @Test
    public void 테스트_getStackTraceString() {
        Exception e = new InvalidClassException("e");

        assertThat(ExceptionUtil.createStacktrace(null)).isNull();
        assertThat(ExceptionUtil.createStacktrace(e)).isNotNull();
    }
}
