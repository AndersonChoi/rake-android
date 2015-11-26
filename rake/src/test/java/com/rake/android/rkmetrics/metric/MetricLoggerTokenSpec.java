package com.rake.android.rkmetrics.metric;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 소스 코드에 라이브 토큰을 노출하지 않기 위해서 BRANCH 값을 빌드타임에 build.gradle 에서
 * BRANCH 를 현재 브랜치와(`release`) 로 덮어쓰고, TOKEN 도 환경변수에서 읽어 덮어쓴다.
 * `build.gradle` 과 `MetricLoggerTokenSpec.java` 를 참조할 것
 */
public class MetricLoggerTokenSpec {

    /**
     * 아래의 환경변수 및 상수 이름들은 build.gradle 에 있는 이름과 동일해야 함
     */
    private final String ENV_METRIC_TOKEN_LIVE = "METRIC_TOKEN_LIVE";
    private final String ENV_METRIC_TOKEN_DEV = "METRIC_TOKEN_DEV";
    private final String releaseBranch = "release";

    String METRIC_TOKEN_LIVE = System.getenv(ENV_METRIC_TOKEN_LIVE);
    String METRIC_TOKEN_DEV = System.getenv(ENV_METRIC_TOKEN_DEV);

    @Test
    public void 토큰_관련_환경변수_검사() {
        if (null == METRIC_TOKEN_LIVE)
            throw new IllegalArgumentException("Environment variable required: " + ENV_METRIC_TOKEN_LIVE);

        if (null == METRIC_TOKEN_DEV)
            throw new IllegalArgumentException("Environment variable required: " + ENV_METRIC_TOKEN_DEV);
    }

    @Test
    public void 현재_Git_브랜치가_release_면_TOKEN_LIVE_를_리턴() throws IOException {
        String branch = executeCommand(GIT_CURRENT_BRANCH_CMD);

        if (releaseBranch == branch) {
            String TOKEN_LIVE = System.getenv(ENV_METRIC_TOKEN_LIVE);

            assertThat(MetricLogger.METRIC_TOKEN).isEqualTo(System.getenv(ENV_METRIC_TOKEN_LIVE));
        } else { /* DEV */
            String TOKEN_DEV  = System.getenv(ENV_METRIC_TOKEN_DEV);

            assertThat(MetricLogger.METRIC_TOKEN).isEqualTo(System.getenv(ENV_METRIC_TOKEN_DEV));
        }
    }

    private String GIT_CURRENT_BRANCH_CMD = "git rev-parse --abbrev-ref HEAD";

    private String executeCommand(String command) throws IOException {
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line = null;
        StringBuilder sb = new StringBuilder();

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }
}
