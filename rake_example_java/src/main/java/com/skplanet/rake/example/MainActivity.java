package com.skplanet.rake.example;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.rake.android.rkmetrics.RakeAPI;
import com.skplanet.pdp.sentinel.shuttle.RakeClientTestSentinelShuttle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This example is made for the purpose of external release.
 * */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    // Define Rake Token. You can get it from Sentinel site. Check if it is for DEV or LIVE.
    private final static String rakeToken = "your_rake_token";
    private RakeAPI rake = null;

    Button buttonTrack;
    Button buttonFlush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create Rake Instance
        rake = RakeAPI.getInstance(getApplicationContext(),
                rakeToken,
                RakeAPI.Env.DEV /* or RakeAPI.Env.LIVE */,
                RakeAPI.Logging.ENABLE /* or RakeAPI.Logging.DISABLE */);


        buttonTrack = findViewById(R.id.button_track);
        buttonTrack.setOnClickListener(this);

        buttonFlush = findViewById(R.id.button_flush);
        buttonFlush.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_track: // buttonTrack click event

                // Create your Shuttle instance.
                RakeClientTestSentinelShuttle shuttle = new RakeClientTestSentinelShuttle();

                // Add data to the shuttle.
                shuttle.ab_test_group("A");

                // Call track() to store your data.
                rake.track(shuttle.toJSONObject());
                break;
            case R.id.button_flush: // buttonFlush click event
                // Call flush() to send stored data to a server.
                rake.flush();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        // Calling flush() is recommended before your app get into background.
        rake.flush();

        super.onPause();
    }
}
