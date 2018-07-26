package com.rake.android.rkmetrics.metric;

import com.rake.android.rkmetrics.RakeAPI;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 소스 코드에 라이브 토큰을 노출하지 않기 위해서 BUILD_CONSTANT_BRANCH 값을 빌드타임에 build.gradle 에서
 * BUILD_CONSTANT_BRANCH 를 현재 브랜치와(`release`) 로 덮어쓰고, TOKEN 도 환경변수에서 읽어 덮어쓴다.
 * `build.gradle` 과 `MetricUtilBuildConstantSpec.java` 를 참조할 것
 */
@RunWith(JUnit4.class)
public class MetricUtilBuildConstantSpec {

    /**
     * 아래의 환경변수 및 상수 이름들은 build.gradle 에 있는 이름과 동일해야 함
     */
    private final String ENV_METRIC_TOKEN_LIVE = "METRIC_TOKEN_LIVE";
    private final String ENV_METRIC_TOKEN_DEV = "METRIC_TOKEN_DEV";
    private final String releaseBranch = "release";

    private String METRIC_TOKEN_LIVE = System.getenv(ENV_METRIC_TOKEN_LIVE);
    private String METRIC_TOKEN_DEV = System.getenv(ENV_METRIC_TOKEN_DEV);

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

        if (branch.startsWith(releaseBranch)) {
            assertThat(MetricUtil.BUILD_CONSTANT_METRIC_TOKEN).isEqualTo(System.getenv(ENV_METRIC_TOKEN_LIVE));
        } else { /* DEV */
            assertThat(MetricUtil.BUILD_CONSTANT_METRIC_TOKEN).isEqualTo(System.getenv(ENV_METRIC_TOKEN_DEV));
        }
    }

    @Test
    public void 현재_Git_브랜치가_release_면_Env_LIVE_를_리턴() throws IOException {
        String branch = executeCommand(GIT_CURRENT_BRANCH_CMD);

        if (branch.startsWith(releaseBranch)) { /* Env.LIVE */
           assertThat(MetricUtil.BUILD_CONSTANT_ENV).isEqualTo(RakeAPI.Env.LIVE);
        } else { /* Env.DEV */
            assertThat(MetricUtil.BUILD_CONSTANT_ENV).isEqualTo(RakeAPI.Env.DEV);
        }
    }

    private String GIT_CURRENT_BRANCH_CMD = "git rev-parse --abbrev-ref HEAD";

    private String executeCommand(String command) throws IOException {
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line;
        StringBuilder sb = new StringBuilder();

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }
}
