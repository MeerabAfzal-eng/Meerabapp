package com.example.meerabapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logoImage = findViewById(R.id.logoImage);
        TextView appName = findViewById(R.id.appName);

        // Animation load karna (Pop-up effect)
        Animation popupAnim = AnimationUtils.loadAnimation(this, R.anim.popup);
        if (logoImage != null) logoImage.startAnimation(popupAnim);
        if (appName != null) appName.startAnimation(popupAnim);

        // 3 Seconds delay ke baad WelcomeActivity par jana
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish(); // Taake user back kare to splash dobara na aaye
            }
        }, 2000);
    }
}