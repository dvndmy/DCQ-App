package com.dcq.quotesapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    TextView developers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Reference to the TextView in the layout
        developers = findViewById(R.id.developers);

        // Create a Timer to delay the splash screen
        Timer myTimer = new Timer();

        // Set the developer's name in the TextView
        developers.setText("Divin Domy");

        // Schedule a TimerTask to navigate to the MainActivity after a delay
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Ensure UI modifications are done on the main (UI) thread
                SplashActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Start the MainActivity and finish the SplashActivity
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    }
                });
            }
        }, 3000); // Delay of 3000 milliseconds (3 seconds)
    }
}
