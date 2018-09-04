package com.skplanet.rake.develop;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.rake.android.rkmetrics.RakeAPI;
import com.skplanet.pdp.sentinel.shuttle.RakeClientTestSentinelShuttle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView textToken;
    Button buttonTrack;
    Button buttonFlush;
    Button buttonSetServerPort;
    Switch switchAutoFlush;

    private static final String TEXT_SIZE_1M_FILE = "large_text";
    private String TEXT_SIZE_1M;
    private int count = 0;

    private RakeAPI rakeAPI;
    private static final String RAKE_TOKEN = "your_rake_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 디버깅을 위한 stetho 연동(chrome://inspect url로 접속하여 DB, SharedPreference 조회)
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build()
        );

        // 개발을 위해 StrictMode 활성. (비효율 작업 감지)
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

        textToken = findViewById(R.id.text_token);

        if (RAKE_TOKEN == "your_rake_token") {
            textToken.setText("No token is set. Set your token first.");
            return;
        }

        textToken.setText(RAKE_TOKEN);

        rakeAPI = RakeAPI.getInstance(getApplicationContext(), RAKE_TOKEN, RakeAPI.Env.DEV, RakeAPI.Logging.ENABLE);

        // init ui
        buttonTrack = findViewById(R.id.button_track);
        buttonFlush = findViewById(R.id.button_flush);
        buttonSetServerPort = findViewById(R.id.button_set_server_port);
        switchAutoFlush = findViewById(R.id.switch_auto_flush);

        switchAutoFlush.setChecked(RakeAPI.getAutoFlush() == RakeAPI.AutoFlush.ON);

        buttonTrack.setOnClickListener(this);
        buttonFlush.setOnClickListener(this);
        buttonSetServerPort.setOnClickListener(this);
        switchAutoFlush.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_track:
//                TEXT_SIZE_1M = createLargeString();

                RakeClientTestSentinelShuttle shuttle = new RakeClientTestSentinelShuttle();
                shuttle.action("ACTION: " + count);
                rakeAPI.track(shuttle.toJSONObject());

                count++;
                break;
            case R.id.button_flush:
                rakeAPI.flush();
                break;
            case R.id.button_set_server_port:
                showSetServerPortDialog();
                break;
            case R.id.switch_auto_flush:
                boolean isOn = RakeAPI.getAutoFlush() == RakeAPI.AutoFlush.ON;

                RakeAPI.setAutoFlush(isOn ? RakeAPI.AutoFlush.OFF : RakeAPI.AutoFlush.ON);

                switchAutoFlush.setChecked(!isOn);
                break;
            default:
                break;
        }
    }

    private String createLargeString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i <= 12000; i++) {
            sb.append("1234567890");
        }

        return sb.toString();
    }

    private void showSetServerPortDialog() {
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
                        rakeAPI.setServerPort(port);
                    }
                })
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        rakeAPI.flush();

        super.onDestroy();
    }
}
