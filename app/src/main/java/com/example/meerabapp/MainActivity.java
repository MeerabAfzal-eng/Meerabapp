package com.example.meerabapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private LinearLayout inputContainer;
    private Button addButton, submitButton;
    private Spinner algorithmSpinner;
    private EditText firstInput;
    private final int MAX_INPUTS = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ID setup
        inputContainer = findViewById(R.id.inputContainer);
        addButton = findViewById(R.id.addButton);
        submitButton = findViewById(R.id.submitButton);
        algorithmSpinner = findViewById(R.id.algorithmSpinner);
        firstInput = findViewById(R.id.firstInput);

        // First Input field ko fix karna (Plus/Minus ke liye)
        setupEditText(firstInput);

        // Algorithms List
        String[] algorithms = {"Bubble Sort", "Insertion Sort", "Selection Sort", "Merge Sort", "Quick Sort", "Heap Sort", "Shell Sort"};

        // Simple Adapter jo colors ko sahi rakhega
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, algorithms) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.BLACK); // Selected text ka color
                tv.setTextSize(18);
                return tv;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(Color.BLACK); // List items ka color
                tv.setPadding(20, 20, 20, 20);
                return tv;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        algorithmSpinner.setAdapter(adapter);

        addButton.setOnClickListener(v -> addInputField());
        submitButton.setOnClickListener(v -> handleSubmit());
    }

    // Is method se keyboard par minus aur decimal nazar aayega
    private void setupEditText(EditText et) {
        et.setTextColor(Color.BLACK);
        et.setHintTextColor(Color.GRAY);
        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        et.setKeyListener(DigitsKeyListener.getInstance("0123456789.-"));
    }

    private void addInputField() {
        int currentCount = inputContainer.getChildCount() + 1;
        if (currentCount < MAX_INPUTS) {
            EditText newInput = new EditText(this);
            newInput.setHint("Enter value");
            setupEditText(newInput); // Naye field par bhi keyboard aur color fix
            newInput.setBackgroundResource(android.R.drawable.edit_text);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 150);
            params.setMargins(0, 15, 0, 0);
            newInput.setLayoutParams(params);
            newInput.setPadding(35, 0, 35, 0);

            inputContainer.addView(newInput);
            newInput.requestFocus();
        } else {
            Toast.makeText(this, "Limit Reached: Max 30", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSubmit() {
        ArrayList<Double> numbers = new ArrayList<>(); // Double use kiya taake decimal values bhi aa saken
        boolean hasError = false;

        ArrayList<EditText> allFields = new ArrayList<>();
        allFields.add(firstInput);
        for (int i = 0; i < inputContainer.getChildCount(); i++) {
            allFields.add((EditText) inputContainer.getChildAt(i));
        }

        for (EditText et : allFields) {
            String val = et.getText().toString().trim();
            if (!val.isEmpty()) {
                try {
                    numbers.add(Double.parseDouble(val));
                } catch (NumberFormatException e) {
                    et.setError("Invalid number!");
                    hasError = true;
                }
            }
        }

        if (hasError) return;

        if (numbers.isEmpty()) {
            Toast.makeText(this, "Please enter some numbers!", Toast.LENGTH_SHORT).show();
        } else {
            String algo = algorithmSpinner.getSelectedItem().toString();
            Toast.makeText(this, "Ready to sort " + numbers.size() + " values using " + algo, Toast.LENGTH_LONG).show();
            // Yahan hum next activity par jayenge
        }
    }
}