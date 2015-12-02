package com.rake.android.rkmetrics.metric;

import android.content.Context;

import com.rake.android.rkmetrics.RakeAPI;
import com.rake.android.rkmetrics.metric.model.Action;
import com.rake.android.rkmetrics.metric.model.FlushType;
import com.rake.android.rkmetrics.metric.model.Header;
import com.rake.android.rkmetrics.metric.model.Status;
import com.rake.android.rkmetrics.network.Endpoint;
import com.rake.android.rkmetrics.util.ExceptionUtil;
import com.rake.android.rkmetrics.util.Logger;
import com.rake.android.rkmetrics.util.functional.Callback;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;


/**
 * 클래스 이름을 바꿀 경우 build.gradle 내에 빌드 변수 또한 변경해야 함
 */
public final class MetricUtil {

    private MetricUtil() {}

    /**
     * 소스 코드에 LIVE TOKEN 을 노출하지 않기 위해서 TOKEN 값을 빌드타임에 환경변수에서 읽어와 덮어쓴다
     * `build.gradle` 과 `MetricUtilTokenSpec.java` 를 참조할 것
     *
     * 후에 `release` 브랜치에서 LIVE TOKEN 이 기록되었는지 크로스 체크를 위해 BUILD_CONSTANT_BRANCH 값을 이용한다.
     * `release` 브랜치일 경우에만 BUILD_CONSTANT_ENV 값이 Env.LIVE 이고 나머지의 경우에는 Env.DEV 여야 한다.
     *
     * 아래의 변수 이름, *스페이스바*, 변수 값 어느 하나라도 변경시 build.gradle 상수와
     * updateMetricToken, getRakeEnv 함수 내의 정규식도 변경해야 함.
     */
    public static final String BUILD_CONSTANT_BRANCH = "feature/RAKE-383-metric";
    // public static final String BUILD_CONSTANT_METRIC_TOKEN = "df234e764a5e4c3beaa7831d5b8ad353149495ac";
    public static final String BUILD_CONSTANT_METRIC_TOKEN = "df234e764a5e4c3beaa7831d5b8ad353149495ac";
    public static final RakeAPI.Env BUILD_CONSTANT_ENV = RakeAPI.Env.DEV;


    public static final String TRANSACTION_ID = createTransactionId();

    public static String getURI() {
        return Endpoint.CHARGED.getURI(BUILD_CONSTANT_ENV);
    }

    public static String createTransactionId() {
        StringBuilder sb = new StringBuilder();

        String u1 = java.util.UUID.randomUUID().toString();
        String u2 = java.util.UUID.randomUUID().toString();
        String u3 = java.util.UUID.randomUUID().toString();

        sb.append(u1);
        sb.append(u2);
        sb.append(u3);

        return sb.toString().replaceAll("-", "");
    }

}
