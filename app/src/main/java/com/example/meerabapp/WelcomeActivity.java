package com.example.meerabapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler; // Isay import karein
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // 3 seconds ka delay taaki user welcome message parh sakay
        new Handler().postDelayed(() -> {
                // Yahan MainActivity ki jagah ProfileActivity likhna hai
                Intent intent = new Intent(WelcomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }, 3000);
    }
}