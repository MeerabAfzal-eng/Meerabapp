package com.example.meerabapp;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
    private EditText numberInput;
    private Button addButton, removeLastButton, submitButton;
    private LinearLayout visualContainer;
    private Spinner algorithmSpinner;
    private ArrayList<Double> numberList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numberInput = findViewById(R.id.numberInput);
        addButton = findViewById(R.id.addButton);
        removeLastButton = findViewById(R.id.removeLastButton);
        submitButton = findViewById(R.id.submitButton);
        visualContainer = findViewById(R.id.visualContainer);
        algorithmSpinner = findViewById(R.id.algorithmSpinner);

        // Spinner with Light Gray List
        String[] algorithms = {"Bubble Sort", "Insertion Sort", "Selection Sort", "Merge Sort", "Quick Sort", "Heap Sort", "Shell Sort"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, algorithms) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.BLACK);
                tv.setTextSize(18);
                return tv;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setBackgroundColor(Color.parseColor("#EEEEEE"));
                tv.setTextColor(Color.BLACK);
                tv.setPadding(35, 35, 35, 35);
                tv.setTextSize(18);
                return tv;
            }
        };
        algorithmSpinner.setAdapter(adapter);

        // Add Logic
        addButton.setOnClickListener(v -> {
            String val = numberInput.getText().toString().trim();
            if (!val.isEmpty() && !val.equals("-") && !val.equals("+") && !val.equals(".")) {
                addNumberToDisplay(val);
                numberInput.setText("");
            } else {
                Toast.makeText(this, "Type a number first! ✨", Toast.LENGTH_SHORT).show();
            }
        });

        // Remove Last Logic
        removeLastButton.setOnClickListener(v -> {
            int childCount = visualContainer.getChildCount();
            if (childCount > 0) {
                visualContainer.removeViewAt(childCount - 1);
                numberList.remove(numberList.size() - 1);
            } else {
                Toast.makeText(this, "Nothing left to remove! 🌸", Toast.LENGTH_SHORT).show();
            }
        });

        // Sort Button (Popup removed)
        submitButton.setOnClickListener(v -> {
            if (numberList.size() > 0) {
                // Yahan aap apni sorting ka logic likh sakti hain
                Toast.makeText(this, "Starting " + algorithmSpinner.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Add numbers to start! 🪄", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNumberToDisplay(String displayVal) {
        numberList.add(Double.parseDouble(displayVal));
        TextView tv = new TextView(this);
        tv.setText(displayVal);
        tv.setTextColor(Color.WHITE);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(18);

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(12f);
        shape.setColor(Color.parseColor("#42A5F5")); // Boxes Light Blue
        tv.setBackground(shape);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, 120);
        params.setMargins(10, 0, 10, 0);
        tv.setLayoutParams(params);
        visualContainer.addView(tv);
    }
}