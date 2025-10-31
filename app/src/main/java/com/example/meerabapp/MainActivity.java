package com.example.meerabapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Spinner algorithmSpinner;
    private EditText valueInput;
    private Button addButton, submitButton;
    private TextView valuesLabel;

    private final ArrayList<Integer> valuesList = new ArrayList<>();
    private String selectedAlgorithm = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        algorithmSpinner = findViewById(R.id.algorithmSpinner);
        valueInput = findViewById(R.id.valueInput);
        addButton = findViewById(R.id.addButton);
        submitButton = findViewById(R.id.submitButton);
        valuesLabel = findViewById(R.id.valuesLabel);

        // Setup spinner
        String[] algorithms = {
                "Select Algorithm",
                "Insertion Sort",
                "Selection Sort",
                "Bubble Sort",
                "Merge Sort",
                "Quick Sort",
                "Heap Sort",
                "Shell Sort"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, algorithms);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        algorithmSpinner.setAdapter(adapter);

        algorithmSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedAlgorithm = algorithms[position];
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedAlgorithm = "";
            }
        });

        addButton.setOnClickListener(v -> {
            String s = valueInput.getText().toString().trim();
            if (s.isEmpty()) {
                Toast.makeText(MainActivity.this, "Enter a value first", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int val = Integer.parseInt(s);
                valuesList.add(val);
                valueInput.setText("");
                updateValuesLabel();
                Toast.makeText(MainActivity.this, "Added: " + val, Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Enter a valid integer", Toast.LENGTH_SHORT).show();
            }
        });

        submitButton.setOnClickListener(v -> {
            if (selectedAlgorithm.equals("Select Algorithm") || selectedAlgorithm.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please select an algorithm", Toast.LENGTH_SHORT).show();
                return;
            }
            if (valuesList.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please add values for sorting", Toast.LENGTH_SHORT).show();
                return;
            }
            // For now just show a Toast — later you can start visualization Activity for selectedAlgorithm
            Toast.makeText(MainActivity.this,
                    "Algorithm: " + selectedAlgorithm + "\nValues: " + valuesList.toString(),
                    Toast.LENGTH_LONG).show();
        });
    }

    private void updateValuesLabel() {
        valuesLabel.setText("Values: " + valuesList.toString());
    }
}
