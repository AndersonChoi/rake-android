package com.skplanet.rake.application;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.rake.android.rkmetrics.RakeAPI;
import com.skplanet.pdp.sentinel.shuttle.RakeClientTestSentinelShuttle;

import java.util.concurrent.atomic.AtomicLong;

/**
 * This example is made for the purpose of internal test.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static RakeAPI devRake;
    private static RakeAPI liveRake;
    private static AtomicLong counter = new AtomicLong();
    private static final String TAG = "RAKE_TESTAPP";

    private static final String TEXT_SIZE_1M_FILE = "large_text";
    private String TEXT_SIZE_1M;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 디버깅을 위한 stetho 연동(chrome://inspect url로 접속하여 DB, SharedPreference 조회)
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());

        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));


        // 디버깅을 위한 로그 출력 활성
        RakeAPI.setLogging(RakeAPI.Logging.ENABLE);

        // 버튼 UI 초기화
        initialize();

        TEXT_SIZE_1M = createLargeString();

        /**
         * set strict mode
         *
         * strict mode 모드는 API 9+, `detectLeakedCloableObjects()` 는 API 11+ 이므로
         * QA 용 앱 배포 등 상황에 따라 테스트앱에서 API 8 지원이 필요할 경우 아래 코드를 제거할 것
         */
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .permitDiskReads()
                .permitDiskWrites()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }

    private String createLargeString() {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i <= 12000; i++) {
            sb.append("1234567890");
        }

        return sb.toString();
    }

    private void initialize() {
        // Test용 버튼 Initialization

        /** DEV */
        Button btnInstallDevRake = findViewById(R.id.btnInstallDevRake);
        btnInstallDevRake.setOnClickListener(this);

        Button btnTrackDevRake = findViewById(R.id.btnTrackDevRake);
        btnTrackDevRake.setOnClickListener(this);

        Button btnFlushDevRake = findViewById(R.id.btnFlushDevRake);
        btnFlushDevRake.setOnClickListener(this);

        Button btnSetServerPortDevRake = findViewById(R.id.btnSetServerPortDevRake);
        btnSetServerPortDevRake.setOnClickListener(this);

        /** LIVE */
        Button btnInstallLiveRake = findViewById(R.id.btnInstallLiveRake);
        btnInstallLiveRake.setOnClickListener(this);

        Button btnTrackLiveRake = findViewById(R.id.btnTrackLiveRake);
        btnTrackLiveRake.setOnClickListener(this);

        Button btnFlushLiveRake = findViewById(R.id.btnFlushLiveRake);
        btnFlushLiveRake.setOnClickListener(this);

        Button btnSetServerPortLiveRake = findViewById(R.id.btnSetServerPortLiveRake);
        btnSetServerPortLiveRake.setOnClickListener(this);

        /** GLOBAL */
        Button btnSetAutoFlushON = findViewById(R.id.btnSetAutoFlushON);
        btnSetAutoFlushON.setOnClickListener(this);

        Button btnSetAutoFlushOFF = findViewById(R.id.btnSetAutoFlushOFF);
        btnSetAutoFlushOFF.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // onClick() operations for Dev Environment
            case R.id.btnInstallDevRake:
                install(RakeAPI.Env.DEV);
                break;
            case R.id.btnTrackDevRake:
                track(RakeAPI.Env.DEV);
                break;
            case R.id.btnFlushDevRake:
                flush(RakeAPI.Env.DEV);
                break;
            case R.id.btnSetServerPortDevRake:
                setServerPort(RakeAPI.Env.DEV);
                break;

            // onClick() operations for Live Environment
            case R.id.btnInstallLiveRake:
                install(RakeAPI.Env.LIVE);
                break;
            case R.id.btnTrackLiveRake:
                track(RakeAPI.Env.LIVE);
                break;
            case R.id.btnFlushLiveRake:
                flush(RakeAPI.Env.LIVE);
                break;
            case R.id.btnSetServerPortLiveRake:
                setServerPort(RakeAPI.Env.LIVE);
                break;

            // onClick() operations for GLOBAL
            case R.id.btnSetAutoFlushON:
                RakeAPI.setAutoFlush(RakeAPI.AutoFlush.ON);
                break;
            case R.id.btnSetAutoFlushOFF:
                RakeAPI.setAutoFlush(RakeAPI.AutoFlush.OFF);
                break;

            default:
                break;
        }
    }

    private void install(RakeAPI.Env env) {
        String token = getToken(env);
        if (RakeAPI.Env.DEV == env) {
            devRake = RakeAPI.getInstance(getApplicationContext(), token, env, RakeAPI.Logging.ENABLE);
        } else {
            liveRake = RakeAPI.getInstance(getApplicationContext(), token, env, RakeAPI.Logging.ENABLE);
        }
    }

    private void flush(RakeAPI.Env env) {
        getRakeInstance(env).flush();
    }

    private void track(RakeAPI.Env env) {
        RakeClientTestSentinelShuttle shuttle = new RakeClientTestSentinelShuttle();
        shuttle.action("flush");
        Long group = counter.getAndIncrement();
        String count = group.toString();

        shuttle.ab_test_group(count);
//        shuttle.code_text(TEXT_SIZE_1M);  // 대용량 사이즈 로그 전송 테스트용

        getRakeInstance(env).track(shuttle.toJSONObject());
        Log.d(TAG, String.format("Counter: %s", count));
    }

    private void setServerPort(final RakeAPI.Env env) {
        final EditText editPort = new EditText(this);
        editPort.setInputType(InputType.TYPE_CLASS_NUMBER);
        editPort.setHint("Put port number.");

        new AlertDialog.Builder(this)
                .setTitle("SET SERVER PORT NUMBER")
                .setView(editPort)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editPort.getText().toString().equals("")) {
                            Toast.makeText(MainActivity.this, "Port value is empty.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int port = Integer.parseInt(editPort.getText().toString());
                        getRakeInstance(env).setServerPort(port);
                    }
                })
                .create()
                .show();

    }

    private RakeAPI getRakeInstance(RakeAPI.Env env) {
        if (RakeAPI.Env.DEV == env) {
            return devRake;
        }

        return liveRake;
    }

    private String getToken(RakeAPI.Env env) {
        if (RakeAPI.Env.DEV == env) {
            return Token.DEV_TOKEN;
        }
        return Token.LIVE_TOKEN;
    }
}
