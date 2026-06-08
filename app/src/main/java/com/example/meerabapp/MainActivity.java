package com.example.meerabapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

        addButton.setOnClickListener(v -> {
            String valStr = numberInput.getText().toString().trim();
            if (!valStr.isEmpty()) {
                try {
                    numbersList.add(Integer.parseInt(valStr));
                    updatePreview();
                    numberInput.setText("");
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Valid number likhein", Toast.LENGTH_SHORT).show();
                }
            }
        });

        removeLastButton.setOnClickListener(v -> {
            if (!numbersList.isEmpty()) {
                numbersList.remove(numbersList.size() - 1);
                updatePreview();
            } else {
                Toast.makeText(this, "List pehle se khali hai", Toast.LENGTH_SHORT).show();
            }
        });

        submitButton.setOnClickListener(v -> {
            if (numbersList.isEmpty()) {
                Toast.makeText(this, "Pehle kuch numbers add karein", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this, VisualizationActivity.class);
            intent.putIntegerArrayListExtra("numbers", numbersList);
            intent.putExtra("algorithm", algorithmSpinner.getSelectedItem().toString());
            startActivity(intent);
        });
        btnGoToCompare.setOnClickListener(v -> {
            if (numbersList.size() < 2) {
                Toast.makeText(this, "Comparison ke liye kam se kam 2 numbers add karein!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this, ComparisonScreen.class);
            intent.putIntegerArrayListExtra("numbers", new ArrayList<>(numbersList));
            startActivity(intent);
        });

        btnGoToQuiz.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, activity_quiz.class)));

        btnGoToProgress.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, activity_progress.class)));
    }

    private void updatePreview() {
        visualContainer.removeAllViews();
        for (int n : numbersList) {
            TextView tv = new TextView(this);
            tv.setText(String.valueOf(n));
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(11f);
            tv.setSingleLine(true);

            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(10f);
            shape.setColor(Color.parseColor("#0040FF"));
            tv.setBackground(shape);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(110, 100);
            params.setMargins(6, 6, 6, 6);
            tv.setLayoutParams(params);

            visualContainer.addView(tv);
        }
    }
}