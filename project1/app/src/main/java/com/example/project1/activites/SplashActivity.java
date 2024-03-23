package com.example.project1.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.example.project1.R;
import com.example.project1.activites.IPv4Activity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        // Delayed execution to transition to another activity after 3 seconds
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Create an Intent to transition to the IPv4Activity
                Intent intent = new Intent(SplashActivity.this, IPv4Activity.class);
                startActivity(intent);
                // Finish the current activity to prevent going back to it when pressing back button
                finish();
            }
        }, 3000); // 3000 milliseconds delay
    }
}
