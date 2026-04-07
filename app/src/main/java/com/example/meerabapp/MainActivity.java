package com.example.meerabapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
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

    private LinearLayout inputContainer;
    private Button addButton, submitButton;
    private Spinner algorithmSpinner;
    private EditText firstInput;
    private final int MAX_INPUTS = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputContainer = findViewById(R.id.inputContainer);
        addButton = findViewById(R.id.addButton);
        submitButton = findViewById(R.id.submitButton);
        algorithmSpinner = findViewById(R.id.algorithmSpinner);
        firstInput = findViewById(R.id.firstInput);

        // Algorithms List
        String[] algorithms = {"Bubble Sort", "Insertion Sort", "Selection Sort", "Merge Sort", "Quick Sort", "Heap Sort", "Shell Sort"};

        // Custom Adapter taaki selection par color 'Gray' ho sake
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, algorithms) {
            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) v;
                // Jo select ho chuka hai usay gray dikhane ke liye
                if (position == algorithmSpinner.getSelectedItemPosition()) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return v;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        algorithmSpinner.setAdapter(adapter);

        // Button Clicks
        addButton.setOnClickListener(v -> addInputField());
        submitButton.setOnClickListener(v -> handleSubmit());
    }

    private void addInputField() {
        int currentCount = inputContainer.getChildCount() + 1;
        if (currentCount < MAX_INPUTS) {
            EditText newInput = new EditText(this);
            newInput.setHint("Enter value");
            newInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); // Sirf numbers allow karega
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
        ArrayList<Integer> numbers = new ArrayList<>();
        boolean hasError = false;

        // Saare fields se data collect karna
        ArrayList<EditText> allFields = new ArrayList<>();
        allFields.add(firstInput);
        for (int i = 0; i < inputContainer.getChildCount(); i++) {
            allFields.add((EditText) inputContainer.getChildAt(i));
        }

        for (EditText et : allFields) {
            String val = et.getText().toString().trim();
            if (!val.isEmpty()) {
                try {
                    numbers.add(Integer.parseInt(val));
                } catch (NumberFormatException e) {
                    et.setError("Only whole numbers allowed!"); // Character error message
                    hasError = true;
                }
            }
        }

        if (hasError) return;

        if (numbers.isEmpty()) {
            Toast.makeText(this, "Please enter some numbers!", Toast.LENGTH_SHORT).show();
        } else {
            String algo = algorithmSpinner.getSelectedItem().toString();
            // Submit press karne par total count ka message show karna
            Toast.makeText(this, "You entered " + numbers.size() + " numbers for " + algo, Toast.LENGTH_LONG).show();
        }
    }
}