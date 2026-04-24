package com.example.meerabapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // 4 Seconds delay ke baad ProfileActivity par jana
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Flow: Splash -> Welcome -> Profile
                Intent intent = new Intent(WelcomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish(); // Welcome screen ko close karna
            }
        }, 3000);
    }
}