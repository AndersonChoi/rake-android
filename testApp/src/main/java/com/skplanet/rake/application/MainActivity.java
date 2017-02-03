package com.skplanet.rake.application;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.facebook.stetho.Stetho;
import com.rake.android.rkmetrics.RakeAPI;
import com.rake.android.rkmetrics.network.Endpoint;
import com.skplanet.pdp.sentinel.shuttle.RakeClientTestSentinelShuttle;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

public class MainActivity extends AppCompatActivity {

    private static RakeAPI devRake;
    private static RakeAPI liveRake;
    private static AtomicLong counter = new AtomicLong();
    private static final String TAG = "RAKE_TESTAPP";

    private static final String TEXT_SIZE_1M_FILE = "large_text";
    private String TEXT_SIZE_1M;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        RakeAPI.setLogging(RakeAPI.Logging.ENABLE);
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

        for(int i = 0; i <= 12000; i++) {
            sb.append("1234567890");
        }

        return sb.toString();
    }

    private void initialize() {
        Button btnInstallDevRake = (Button) findViewById(R.id.btnInstallDevRake);
        btnInstallDevRake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                install(RakeAPI.Env.DEV);
            }
        });

        Button btnTrackDevRake = (Button) findViewById(R.id.btnTrackDevRake);
        btnTrackDevRake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                track(RakeAPI.Env.DEV);
            }
        });

        Button btnFlushDevRake = (Button) findViewById(R.id.btnFlushDevRake);
        btnFlushDevRake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flush(RakeAPI.Env.DEV);
            }
        });

        Button btnSetFreeEndpointDevRake =
                (Button) findViewById(R.id.btnSetFreeEndpointDevRake);
        btnSetFreeEndpointDevRake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFreeEndpoint(RakeAPI.Env.DEV);
            }
        });

        /** LIVE */
        Button btnInstallLiveRake = (Button) findViewById(R.id.btnInstallLiveRake);
        btnInstallLiveRake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                install(RakeAPI.Env.LIVE);
            }
        });

        Button btnTrackLiveRake = (Button) findViewById(R.id.btnTrackLiveRake);
        btnTrackLiveRake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                track(RakeAPI.Env.LIVE);
            }
        });

        Button btnFlushLiveRake = (Button) findViewById(R.id.btnFlushLiveRake);
        btnFlushLiveRake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flush(RakeAPI.Env.LIVE);
            }});

        Button btnSetFreeEndpointLiveRake =
                (Button) findViewById(R.id.btnSetFreeEndpointLiveRake);
        btnSetFreeEndpointLiveRake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFreeEndpoint(RakeAPI.Env.LIVE);
            }
        });

        /** GLOBAL */
        Button btnSetAutoFlushON =
                (Button) findViewById(R.id.btnSetAutoFlushON);
        btnSetAutoFlushON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RakeAPI.setAutoFlush(RakeAPI.AutoFlush.ON);
            }
        });

        Button btnSetAutoFlushOFF =
                (Button) findViewById(R.id.btnSetAutoFlushOFF);
        btnSetAutoFlushOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RakeAPI.setAutoFlush(RakeAPI.AutoFlush.OFF);
            }
        });
    }

    private void install(RakeAPI.Env env) {
        String token = getToken(env);
        if (RakeAPI.Env.DEV == env) {
            devRake =  RakeAPI.getInstance(getApplicationContext(), token, env, RakeAPI.Logging.ENABLE);
        }
        else {
            liveRake =  RakeAPI.getInstance(getApplicationContext(), token, env, RakeAPI.Logging.ENABLE);
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
//        shuttle.code_text(TEXT_SIZE_1M);

        getRakeInstance(env).track(shuttle.toJSONObject());
        Log.d(TAG, String.format("Counter: %s", count));
    }

    private void setFreeEndpoint(RakeAPI.Env env) {
        getRakeInstance(env).setEndpoint(Endpoint.FREE);
    }

    private RakeAPI getRakeInstance(RakeAPI.Env env) {
        if (RakeAPI.Env.DEV == env) return devRake;
        else return liveRake;
    }

    private String getToken(RakeAPI.Env env) {
        if (RakeAPI.Env.DEV == env) return Token.DEV_TOKEN;
        else return Token.LIVE_TOKEN;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
