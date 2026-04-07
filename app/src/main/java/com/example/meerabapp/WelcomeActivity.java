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
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Taaki user back dabaye to dubara welcome screen na aaye
        }, 3000);
    }
}