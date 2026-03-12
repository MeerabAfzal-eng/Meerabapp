package com.example.meerabapp;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private LinearLayout inputContainer;
    private Button addButton, submitButton;
    private Spinner algorithmSpinner;
    private EditText firstInput;
    private final int MAX_INPUTS = 30; // 30 values ki limit

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views ko initialize karna
        inputContainer = findViewById(R.id.inputContainer);
        addButton = findViewById(R.id.addButton);
        submitButton = findViewById(R.id.submitButton);
        algorithmSpinner = findViewById(R.id.algorithmSpinner);
        firstInput = findViewById(R.id.firstInput);

        // Spinner Setup (Shell Sort ke sath)
        String[] algorithms = {
                "Bubble Sort",
                "Insertion Sort",
                "Selection Sort",
                "Merge Sort",
                "Quick Sort",
                "Heap Sort",
                "Shell Sort"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, algorithms);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        algorithmSpinner.setAdapter(adapter);

        // '+' Button par click karne ka logic
        addButton.setOnClickListener(v -> addInputField());

        // Pehle input field par keyboard ka 'Next' dabane se naya field aaye
        firstInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                addInputField();
                return true;
            }
            return false;
        });

        // Submit button ka logic
        submitButton.setOnClickListener(v -> handleSubmit());
    }

    // Naya input field add karne ka function
    private void addInputField() {
        // Total fields check karna (XML wala 1 + container ke bachay huay)
        int currentCount = inputContainer.getChildCount() + 1;

        if (currentCount < MAX_INPUTS) {
            EditText newInput = new EditText(this);
            newInput.setHint("Value " + (currentCount + 1));
            newInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            newInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            newInput.setBackgroundResource(android.R.drawable.edit_text);

            // Layout styling (Height aur Margin)
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 150);
            params.setMargins(0, 15, 0, 0);
            newInput.setLayoutParams(params);
            newInput.setPadding(35, 0, 35, 0);

            // Naye field par bhi 'Next' dabane se mazeed field add ho sakein
            newInput.setOnEditorActionListener((v, actionId, event) -> {
                addInputField();
                return true;
            });

            inputContainer.addView(newInput);

            // Cursor automatically naye field par shift ho jaye
            newInput.requestFocus();
        } else {
            // Agar 30 se zyada ho jayein
            Toast.makeText(this, "Limit reached! Sirf 30 values allowed hain.", Toast.LENGTH_SHORT).show();
        }
    }

    // Values jama karne aur sorting process dikhane ka function
    private void handleSubmit() {
        ArrayList<Integer> numbers = new ArrayList<>();

        // Pehle field ki value uthana
        String val1 = firstInput.getText().toString().trim();
        if (!val1.isEmpty()) numbers.add(Integer.parseInt(val1));

        // Baqi dynamic fields ki values uthana
        for (int i = 0; i < inputContainer.getChildCount(); i++) {
            EditText et = (EditText) inputContainer.getChildAt(i);
            String val = et.getText().toString().trim();
            if (!val.isEmpty()) {
                numbers.add(Integer.parseInt(val));
            }
        }

        // Validation check
        if (numbers.isEmpty()) {
            Toast.makeText(this, "Kam az kam ek number enter karein", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedAlgo = algorithmSpinner.getSelectedItem().toString();

        // Final Output Result Toast mein dikhana
        Toast.makeText(this, "Algorithm: " + selectedAlgo + "\nTotal Values: " + numbers.size(), Toast.LENGTH_LONG).show();

        // Note: Yahan aap apna sorting logic (like shellSort(numbers)) call kar sakte hain.
    }
}