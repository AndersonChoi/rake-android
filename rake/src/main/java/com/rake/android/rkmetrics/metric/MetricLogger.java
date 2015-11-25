package com.rake.android.rkmetrics.metric;

import com.rake.android.rkmetrics.RakeAPI;

public final class MetricLogger { /* singleton */
    private MetricLogger() {}

    /**
     * 소스 코드에 토큰을 노출하지 않기 위해서 TOKEN 값을 빌드타임에 환경변수에서 읽어와 덮어쓴다.
     * `build.gradle` 과 `MetricLoggerTokenSpec.java` 를 참조할 것
     *
     * 후에 `release` 브랜치에서 LIVE TOKEN 이 기록되었는지 크로스 체크를 위해 BRANCH 값을 이용한다.
     *
     * 아래의 변수 이름, 스페이스바, 변수 값 어느 하나라도 변경시 build.gradle fillMetricToken 함수 내의
     * 정규식도 변경해야 함.
     */
    public static final String BRANCH = "feature/RAKE-383-metric";
    public static final String METRIC_TOKEN = "";

    /**
     * static members
     */
    private static MetricLogger instance;

    public static synchronized MetricLogger getInstance() {
        if (null == instance) instance = new MetricLogger();

        return instance;
    }

    /**
     * instance members
     */



}
