package com.example.meerabapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private LinearLayout visualContainer;
    private Button addButton, submitButton, removeLastButton;
    private Spinner algorithmSpinner;
    private EditText numberInput;
    private ArrayList<Integer> numbersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views ko initialize karna
        visualContainer = findViewById(R.id.visualContainer);
        addButton = findViewById(R.id.addButton);
        submitButton = findViewById(R.id.submitButton);
        removeLastButton = findViewById(R.id.removeLastButton);
        algorithmSpinner = findViewById(R.id.algorithmSpinner);
        numberInput = findViewById(R.id.numberInput);

        // Spinner Setup
        String[] algorithms = {"Bubble Sort", "Insertion Sort", "Selection Sort", "Merge Sort", "Quick Sort", "Heap Sort", "Shell Sort"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, algorithms);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        algorithmSpinner.setAdapter(adapter);

        // Add Button Logic
        addButton.setOnClickListener(v -> {
            String valStr = numberInput.getText().toString().trim();
            if (!valStr.isEmpty()) {
                try {
                    int num = Integer.parseInt(valStr);
                    if (numbersList.size() < 20) { // Limit taake screen se bahar na jaye
                        numbersList.add(num);
                        updatePreview();
                        numberInput.setText("");
                    } else {
                        Toast.makeText(this, "Maximum 20 numbers allowed", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Remove Last Logic
        removeLastButton.setOnClickListener(v -> {
            if (!numbersList.isEmpty()) {
                numbersList.remove(numbersList.size() - 1);
                updatePreview();
            }
        });

        // Submit to Visualization Logic
        submitButton.setOnClickListener(v -> {
            if (numbersList.size() < 2) {
                Toast.makeText(this, "Please add at least 2 numbers to sort", Toast.LENGTH_SHORT).show();
                return;
            }
            // Intent for VisualizationActivity
            Intent intent = new Intent(MainActivity.this, VisualizationActivity.class);
            intent.putIntegerArrayListExtra("numbers", numbersList);
            intent.putExtra("algorithm", algorithmSpinner.getSelectedItem().toString());
            startActivity(intent);
        });
    }

    private void updatePreview() {
        visualContainer.removeAllViews();
        for (int n : numbersList) {
            TextView tv = new TextView(this);
            tv.setText(String.valueOf(n));
            tv.setTextSize(18);
            tv.setTextColor(Color.BLACK);
            tv.setGravity(Gravity.CENTER);

            // Box styling
            tv.setBackgroundResource(android.R.drawable.editbox_background);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, 120);
            params.setMargins(10, 0, 10, 0);
            tv.setLayoutParams(params);

            visualContainer.addView(tv);
        }
    }
}