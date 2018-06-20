package com.skplanet.rake.example.kt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.rake.android.rkmetrics.RakeAPI
import com.skplanet.pdp.sentinel.shuttle.RakeClientTestSentinelShuttle

/**
 * This example is made for the purpose of external release.
 * */
class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var rake: RakeAPI? = null

    // Define Rake Token. You can get it from Sentinel site. Check if it is for DEV or LIVE.
    private val rakeToken = "your_rake_token"

    private lateinit var buttonTrack: Button
    private lateinit var buttonFlush: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create Rake Instance
        rake = RakeAPI.getInstance(applicationContext,
                rakeToken,
                RakeAPI.Env.DEV, /* or RakeAPI.Env.LIVE */
                RakeAPI.Logging.ENABLE /* or RakeAPI.Logging.DISABLE */)

        buttonTrack = findViewById(R.id.button_track)
        buttonTrack.setOnClickListener(this)

        buttonFlush = findViewById(R.id.button_flush)
        buttonFlush.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
        // buttonTrack click event
            R.id.button_track -> {
                // Create your Shuttle instance.
                val shuttle = RakeClientTestSentinelShuttle()

                // Add data to the shuttle.
                shuttle.ab_test_group("A")

                // Call track() to store your data.
                rake?.track(shuttle.toJSONObject())
            }

        // buttonFlush click event
            R.id.button_flush -> {
                // Call flush() to send stored data to a server.
                rake?.flush()
            }

            else -> {
                return
            }
        }
    }

    override fun onPause() {
        // Calling flush() is recommended before your app get into background.
        rake?.flush()

        super.onPause()
    }
}
