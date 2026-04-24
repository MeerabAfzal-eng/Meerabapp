package com.example.meerabapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    private EditText etName, etID;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etName = findViewById(R.id.etName);
        etID = findViewById(R.id.etID);
        btnSubmit = findViewById(R.id.btnSubmitProfile);

        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String id = etID.getText().toString().trim();

            if (name.isEmpty() || id.isEmpty()) {
                Toast.makeText(this, "Please enter Name and ID", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}