package com.skplanet.rake.application;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.rake.android.rkmetrics.RakeAPI;
import com.rake.android.rkmetrics.RakeAPI.AutoFlush;
import com.rake.android.rkmetrics.network.Endpoint;
import com.skplanet.pdp.sentinel.shuttle.RakeClientTestSentinelShuttle;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MainActivity extends AppCompatActivity {

    private static RakeAPI devRake;
    private static RakeAPI liveRake;
    private static AtomicLong counter = new AtomicLong();
    private static final String TAG = "RAKE_TESTAPP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    private void initialize() {
        Button btnInstallDevRake = (Button) findViewById(R.id.btnInstallDevRake);
        btnInstallDevRake.setOnClickListener((View v) -> {
            install(RakeAPI.Env.DEV);
        });

        Button btnTrackDevRake = (Button) findViewById(R.id.btnTrackDevRake);
        btnTrackDevRake.setOnClickListener((View v) -> {
           track(RakeAPI.Env.DEV);
        });

        Button btnFlushDevRake = (Button) findViewById(R.id.btnFlushDevRake);
        btnFlushDevRake.setOnClickListener((View v) -> {
            flush(RakeAPI.Env.DEV);
        });

        Button btnSetFreeEndpointDevRake =
                (Button) findViewById(R.id.btnSetFreeEndpointDevRake);
        btnSetFreeEndpointDevRake.setOnClickListener((View v) -> {
            setFreeEndpoint(RakeAPI.Env.DEV);
        });

        /** LIVE */
        Button btnInstallLiveRake = (Button) findViewById(R.id.btnInstallLiveRake);
        btnInstallLiveRake.setOnClickListener((View v) -> {
            install(RakeAPI.Env.LIVE);
        });

        Button btnTrackLiveRake = (Button) findViewById(R.id.btnTrackLiveRake);
        btnTrackLiveRake.setOnClickListener((View v) -> {
            track(RakeAPI.Env.LIVE);
        });

        Button btnFlushLiveRake = (Button) findViewById(R.id.btnFlushLiveRake);
        btnFlushLiveRake.setOnClickListener((View v) -> {
            flush(RakeAPI.Env.LIVE);
        });

        Button btnSetFreeEndpointLiveRake =
                (Button) findViewById(R.id.btnSetFreeEndpointLiveRake);
        btnSetFreeEndpointLiveRake.setOnClickListener((View v) -> {
            setFreeEndpoint(RakeAPI.Env.LIVE);
        });

        /** GLOBAL */
        Button btnSetAutoFlushON =
                (Button) findViewById(R.id.btnSetAutoFlushON);
        btnSetAutoFlushON.setOnClickListener((View v) -> {
            RakeAPI.setAutoFlush(AutoFlush.ON);
        });

        Button btnSetAutoFlushOFF =
                (Button) findViewById(R.id.btnSetAutoFlushOFF);
        btnSetAutoFlushOFF.setOnClickListener((View v) -> {
            RakeAPI.setAutoFlush(AutoFlush.OFF);
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
