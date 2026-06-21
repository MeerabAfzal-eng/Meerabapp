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

    private Button addButton, submitButton, removeLastButton, btnGoToCompare, btnGoToQuiz, btnGoToProgress;

    private Spinner algorithmSpinner;

    private EditText numberInput;

    private ArrayList<Integer> numbersList = new ArrayList<>();



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        visualContainer = findViewById(R.id.visualContainer);

        addButton = findViewById(R.id.addButton);

        submitButton = findViewById(R.id.submitButton);

        removeLastButton = findViewById(R.id.removeLastButton);

        algorithmSpinner = findViewById(R.id.algorithmSpinner);

        numberInput = findViewById(R.id.numberInput);

        btnGoToCompare = findViewById(R.id.btnGoToCompare);

        btnGoToQuiz = findViewById(R.id.btnGoToQuiz);

        btnGoToProgress = findViewById(R.id.btnGoToProgress);



        String[] algorithms = {"Bubble Sort", "Insertion Sort", "Selection Sort", "Merge Sort", "Quick Sort", "Heap Sort", "Shell Sort"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, algorithms);

        algorithmSpinner.setAdapter(adapter);

// 1. Add Button
        addButton.setOnClickListener(v -> {
            String valStr = numberInput.getText().toString().trim();
            if (!valStr.isEmpty()) {
                if (numbersList.size() >= 30) {
                    Toast.makeText(this, "Enter only 30 numbers!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    numbersList.add(Integer.parseInt(valStr));
                    updatePreview();
                    numberInput.setText("");
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Enter Valid number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 2. Submit Button
        submitButton.setOnClickListener(v -> {
            if (numbersList.isEmpty()) {
                Toast.makeText(this, "  Enter 1st number!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this, VisualizationActivity.class);
            intent.putIntegerArrayListExtra("numbers", numbersList);
            intent.putExtra("algorithm", algorithmSpinner.getSelectedItem().toString());
            startActivity(intent);
        });

        // 3. Remove Last Button (Ye submitButton ke bahar hai)
        removeLastButton.setOnClickListener(v -> {
            if (!numbersList.isEmpty()) {
                numbersList.remove(numbersList.size() - 1);
                updatePreview();
            } else {
                Toast.makeText(this, "List is already empty!", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Compare Button
        btnGoToCompare.setOnClickListener(v -> {
            if (numbersList.size() < 2) {
                Toast.makeText(this, "Enter atleast 2 numbers for comparison!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MainActivity.this, ComparisonScreen.class);
                intent.putIntegerArrayListExtra("numbers", new ArrayList<>(numbersList));
                startActivity(intent);
            }
        });

//



        btnGoToQuiz.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, activity_quiz.class)));



        btnGoToProgress.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, activity_progress.class)));

    }



    private void updatePreview() {

        visualContainer.removeAllViews();

        for (int n : numbersList) {

            TextView tv = new TextView(this);

            tv.setText(String.valueOf(n));

            tv.setBackgroundResource(android.R.drawable.editbox_background);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, 120);

            params.setMargins(10, 0, 10, 0);

            tv.setLayoutParams(params);

            visualContainer.addView(tv);

        }

    }

}