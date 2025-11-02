package com.example.meerabapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout inputContainer;
    private Button addButton, submitButton;
    private Spinner algorithmSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputContainer = findViewById(R.id.inputContainer);
        addButton = findViewById(R.id.addButton);
        submitButton = findViewById(R.id.submitButton);
        algorithmSpinner = findViewById(R.id.algorithmSpinner);

        // Spinner ke liye sorting algorithms
        String[] algorithms = {
                "Bubble Sort",
                "Insertion Sort",
                "Selection Sort",
                "Merge Sort",
                "Quick Sort",
                "Heap Sort",
                "Shell Sort"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, algorithms);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        algorithmSpinner.setAdapter(adapter);

        // Pehla input field add automatically
        addInputField();

        // + button click → new input field add
        addButton.setOnClickListener(v -> addInputField());

        // Submit button click
        submitButton.setOnClickListener(v -> handleSubmit());
    }

    private void addInputField() {
        EditText newInput = new EditText(this);
        newInput.setHint("Enter value");
        newInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        newInput.setPadding(20, 20, 20, 20);
        inputContainer.addView(newInput);
    }

    private void handleSubmit() {
        int count = inputContainer.getChildCount();
        if (count == 0) {
            Toast.makeText(this, "Please add some values first", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder values = new StringBuilder();
        for (int i = 0; i < count; i++) {
            EditText editText = (EditText) inputContainer.getChildAt(i);
            String value = editText.getText().toString().trim();
            if (!value.isEmpty()) {
                values.append(value).append(" ");
            }
        }

        String selectedAlgorithm = algorithmSpinner.getSelectedItem().toString();
        Toast.makeText(this, "Algorithm: " + selectedAlgorithm + "\nValues: " + values, Toast.LENGTH_LONG).show();
    }
}
