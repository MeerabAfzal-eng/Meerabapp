package com.example.meerabapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    private EditText etName, etID;
    private Button btnSubmit, btnSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // UI Initialization
        etName = findViewById(R.id.etName);
        etID = findViewById(R.id.etID);
        btnSubmit = findViewById(R.id.btnSubmitProfile);
        btnSkip = findViewById(R.id.btnSkip);

        // 1. Submit Button Logic
        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String id = etID.getText().toString().trim();

            if (name.isEmpty() || id.isEmpty()) {
                Toast.makeText(this, "Please enter your Name and ID", Toast.LENGTH_SHORT).show();
            } else {
                // Save to SharedPreferences
                SharedPreferences pref = getSharedPreferences("UserProfile", MODE_PRIVATE);
                pref.edit()
                        .putString("user_name", name)
                        .putString("user_id", id)
                        .putBoolean("is_profile_set", true)
                        .apply();

                Toast.makeText(this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show();

                // Redirect to Main
                startActivity(new Intent(this, MainActivity.class));
                finish(); // Activity khatam kar dein taake user wapas na aa sake
            }
        });

        // 2. Skip Button Logic with Validation
        btnSkip.setOnClickListener(v -> {
            SharedPreferences pref = getSharedPreferences("UserProfile", MODE_PRIVATE);
            boolean isSaved = pref.getBoolean("is_profile_set", false);

            if (isSaved) {
                // Agar data pehle se save hai, to skip allow karein
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                // Force user to fill data for the first time
                new AlertDialog.Builder(this)
                        .setTitle("Profile Incomplete")
                        .setMessage("Please enter your Name and ID to save your data before proceeding.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }
}