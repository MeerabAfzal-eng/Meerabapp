package com.example.meerabapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private EditText etName, etID;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etName = findViewById(R.id.etName);
        etID = findViewById(R.id.etID);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String id = etID.getText().toString().trim();

            if (!name.isEmpty() && !id.isEmpty()) {
                // User ka record save karna
                SharedPreferences pref = getSharedPreferences("UserRecords", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("userName", name);
                editor.putString("studentID", id);
                editor.apply();

                Toast.makeText(this, "Profile Saved: " + name, Toast.LENGTH_SHORT).show();

                // Profile ke baad Main Input Screen par jana
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Please enter both Name and ID", Toast.LENGTH_SHORT).show();
            }
        });
    }
}