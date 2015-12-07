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
import com.rake.android.rkmetrics.network.Endpoint;
import com.skplanet.pdp.sentinel.shuttle.RakeClientTestSentinelShuttle;

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
        btnInstallDevRake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                install(RakeAPI.Env.DEV);
            }
        });

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
        /**
        shuttle.code_text("\n" +
                "{\"_$schemaId\":\"5664fa9a1c0000d41ff4b779\",\"_$fieldOrder\":{\"base_time\":0,\"local_time\":1,\"recv_time\":2,\"device_id\":3,\"device_model\":4,\"manufacturer\":5,\"os_name\":6,\"os_version\":7,\"resolution\":8,\"screen_width\":9,\"screen_height\":10,\"carrier_name\":11,\"network_type\":12,\"language_code\":13,\"ip\":14,\"recv_host\":15,\"app_version\":16,\"rake_lib\":17,\"rake_lib_version\":18,\"token\":19,\"log_version\":20,\"browser_name\":21,\"browser_version\":22,\"referrer\":23,\"url\":24,\"document_title\":25,\"action\":26,\"status\":27,\"app_package\":28,\"transaction_id\":29,\"service_token\":30,\"reserved0\":31,\"reserved1\":32,\"reserved2\":33,\"_$body\":34},\"_$encryptionFields\":[\"device_id\"],\"_$projectId\":\"projectId\",\"properties\":{\"log_version\":\"15.12.07:1.5.54:35\",\"action\":\"flush\",\"status\":\"RETRY\",\"app_package\":\"TestApp\",\"transaction_id\":\"7915a75348954ceda6c184290b88ded9cc2eb20377354a50a53c191c505a55c7917ef0a5ee7b4360890a79cf9f285d52\",\"service_token\":\"d839ca7a5875c8ac304d465bfcfd78d95b4726eb\",\"_$body\":{\"exception_type\":\"UnknownHostException\",\"stacktrace\":\"java.net.UnknownHostException: Unable to resolve host \\\"rake.skplanet.com\\\": No address associated with hostname\\\\n\\\\tat java.net.InetAddress.lookupHostByName(InetAddress.java:457)\\\\n\\\\tat java.net.InetAddress.getAllByNameImpl(InetAddress.java:252)\\\\n\\\\tat java.net.InetAddress.getAllByName(InetAddress.java:215)\\\\n\\\\tat com.android.okhttp.HostResolver$1.getAllByName(HostResolver.java:29)\\\\n\\\\tat com.android.okhttp.internal.http.RouteSelector.resetNextInetSocketAddress(RouteSelector.java:232)\\\\n\\\\tat com.android.okhttp.internal.http.RouteSelector.next(RouteSelector.java:124)\\\\n\\\\tat com.android.okhttp.internal.http.HttpEngine.connect(HttpEngine.java:272)\\\\n\\\\tat com.android.okhttp.internal.http.HttpEngine.sendRequest(HttpEngine.java:211)\\\\n\\\\tat com.android.okhttp.internal.http.HttpURLConnectionImpl.execute(HttpURLConnectionImpl.java:382)\\\\n\\\\tat com.android.okhttp.internal.http.HttpURLConnectionImpl.connect(HttpURLConnectionImpl.java:106)\\\\n\\\\tat com.android.okhttp.internal.http.HttpURLConnectionImpl.getOutputStream(HttpURLConnectionImpl.java:217)\\\\n\\\\tat com.android.okhttp.internal.http.DelegatingHttpsURLConnection.getOutputStream(DelegatingHttpsURLConnection.java:218)\\\\n\\\\tat com.android.okhttp.internal.http.HttpsURLConnectionImpl.getOutputStream(HttpsURLConnectionImpl.java:25)\\\\n\\\\tat com.rake.android.rkmetrics.network.HttpRequestSender.sendHttpUrlStreamRequest(HttpRequestSender.java:89)\\\\n\\\\tat com.rake.android.rkmetrics.network.HttpRequestSender.sendRequest(HttpRequestSender.java:54)\\\\n\\\\tat com.rake.android.rkmetrics.MessageLoop$MessageHandler.send(MessageLoop.java:372)\\\\n\\\\tat com.rake.android.rkmetrics.MessageLoop$MessageHandler.flush(MessageLoop.java:304)\\\\n\\\\tat com.rake.android.rkmetrics.MessageLoop$MessageHandler.handleMessage(MessageLoop.java:437)\\\\n\\\\tat android.os.Handler.dispatchMessage(Handler.java:102)\\\\n\\\\tat android.os.Looper.loop(Looper.java:135)\\\\n\\\\tat com.rake.android.rkmetrics.MessageLoop$1.run(MessageLoop.java:222)\\\\nCaused by: android.system.GaiException: android_getaddrinfo failed: EAI_NODATA (No address associated with hostname)\\\\n\\\\tat libcore.io.Posix.android_getaddrinfo(Native Method)\\\\n\\\\tat libcore.io.ForwardingOs.android_getaddrinfo(ForwardingOs.java:55)\\\\n\\\\tat java.net.InetAddress.lookupHostByName(InetAddress.java:438)\\\\n\\\\t... 20 more\\\\n\",\"operation_time\":3,\"endpoint\":\"https://rake.skplanet.com:8443/log/track\",\"log_count\":1,\"log_size\":1637,\"flush_type\":\"MANUAL_FLUSH\",\"server_response_time\":0,\"server_response_code\":0},\"token\":\"d839ca7a5875c8ac304d465bfcfd78d95b4726eb\",\"base_time\":\"20151207170137207\",\"local_time\":\"20151207180137207\",\"rake_lib\":\"android\",\"rake_lib_version\":\"r0.5.0_c0.4.0\",\"os_name\":\"Android\",\"os_version\":\"5.1.1\",\"manufacturer\":\"LGE\",\"device_model\":\"Nexus 5\",\"device_id\":\"eySfO6c1koI1o6Rlmhl1UJJ6CnWXsCPwvykzf2HnAqxUAcgz27OH2HTKUfR4gQNUSUh8zuDER8FhDJ2zylBf5A==\",\"screen_height\":1080,\"screen_width\":1776,\"resolution\":\"1080*1776\",\"app_version\":\"1.0\",\"carrier_name\":\"UNKNOWN\",\"network_type\":\"NOT WIFI\",\"language_code\":\"KR\",\"recv_time\":\"20151207170137407\",\"recv_host\":\"RAKEc-was09\",\"ip\":\"124.66.180.54\"},\"recv_time\":\"20151207170137407\",\"recv_host\":\"RAKEc-was09\",\"token\":\"d839ca7a5875c8ac304d465bfcfd78d95b4726eb\",\"base_time\":\"20151207170137207\",\"local_time\":\"20151207180137207\"}\n");

        */

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
