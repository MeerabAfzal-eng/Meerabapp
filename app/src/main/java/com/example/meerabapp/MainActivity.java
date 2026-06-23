package com.example.meerabapp;

import android.content.Intent;
import android.graphics.Color;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private LinearLayout layoutBarsWrapper;
    private Button btnValueAdd, btnValueRemove, btnSortNow;
    private androidx.appcompat.widget.AppCompatButton btnCompareScreen, btnTakeQuiz, btnViewProgress;
    private Spinner spinnerAlgorithm;
    private EditText etInputNumber;

    // 👑 1 Arab tak ki badi values (+ aur - dono) handle karne ke liye Long array list
    private ArrayList<Long> numbersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ✅ UI Elements Mapping
        layoutBarsWrapper = findViewById(R.id.layoutBarsWrapper);
        btnValueAdd = findViewById(R.id.btnValueAdd);
        btnValueRemove = findViewById(R.id.btnValueRemove);
        btnSortNow = findViewById(R.id.btnSortNow);
        spinnerAlgorithm = findViewById(R.id.spinnerAlgorithm);
        etInputNumber = findViewById(R.id.etInputNumber);
        btnCompareScreen = findViewById(R.id.btnCompareScreen);
        btnTakeQuiz = findViewById(R.id.btnTakeQuiz);
        btnViewProgress = findViewById(R.id.btnViewProgress);

        // 📋 Dropdown List (Spinner) Setup
        String[] algorithms = {
                "Select Algorithm", "Bubble Sort", "Insertion Sort",
                "Selection Sort", "Merge Sort", "Quick Sort", "Heap Sort", "Shell Sort"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, algorithms) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @NonNull
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.parseColor("#94A3B8"));
                } else {
                    tv.setTextColor(Color.parseColor("#001F3F"));
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerAlgorithm.setAdapter(adapter);

        // 1. ➕ ADD VALUE BUTTON LOGIC (Fixed for Plus & Minus both)
        btnValueAdd.setOnClickListener(v -> {
            String valStr = etInputNumber.getText().toString().trim();
            if (!valStr.isEmpty()) {
                try {
                    // 🛑 Check 1: Max 30 items limit
                    if (numbersList.size() >= 30) {
                        Toast.makeText(this, "You can only add a maximum of 30 numbers!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Agar user ne galti se sirf '+' ya '-' likh kar add daba diya ho
                    if (valStr.equals("+") || valStr.equals("-")) {
                        Toast.makeText(this, "Please enter a valid number after the sign!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Value convert karein
                    long inputVal = Long.parseLong(valStr);

                    // 🛑 Check 2: Range limit (-1 Arab se +1 Arab tak allowed hai)
                    if (inputVal > 1000000000L || inputVal < -1000000000L) {
                        Toast.makeText(this, "Value must be between -1 Arab and +1 Arab!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ✅ Sub sahi hai toh list mein add karein (Ab positive check bilkul saaf kar di hai)
                    numbersList.add(inputVal);
                    updatePreview();
                    etInputNumber.setText("");

                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid number format!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 2. 🚀 SORT NOW BUTTON LOGIC
        btnSortNow.setOnClickListener(v -> {
            String selectedAlgo = spinnerAlgorithm.getSelectedItem().toString();
            if (selectedAlgo.equals("Select Algorithm")) {
                Toast.makeText(this, "Please select an algorithm first!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (numbersList.isEmpty()) {
                Toast.makeText(this, "Enter 1st number!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(MainActivity.this, VisualizationActivity.class);

            // Auto-conversion to Integer for next screens
            ArrayList<Integer> intList = new ArrayList<>();
            for (Long l : numbersList) {
                intList.add(l.intValue());
            }

            intent.putIntegerArrayListExtra("numbers", intList);
            intent.putExtra("algorithm", selectedAlgo);
            startActivity(intent);
        });

        // 3. ❌ REMOVE LAST BUTTON LOGIC
        btnValueRemove.setOnClickListener(v -> {
            if (!numbersList.isEmpty()) {
                numbersList.remove(numbersList.size() - 1);
                updatePreview();
            } else {
                Toast.makeText(this, "List is already empty!", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. 📊 COMPARE SCREEN BUTTON
        btnCompareScreen.setOnClickListener(v -> {
            if (numbersList.size() < 2) {
                Toast.makeText(this, "Enter at least 2 numbers for comparison!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MainActivity.this, ComparisonScreen.class);

                ArrayList<Integer> intList = new ArrayList<>();
                for (Long l : numbersList) {
                    intList.add(l.intValue());
                }

                intent.putIntegerArrayListExtra("numbers", intList);
                startActivity(intent);
            }
        });

        // 5. 📝 QUIZ & PROGRESS NAVIGATION
        btnTakeQuiz.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, activity_quiz.class)));
        btnViewProgress.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, activity_progress.class)));
    }

    // 🎨 UI PREVIEW GENERATOR (Plus aur Minus ke signs ke sath chamkega)
    private void updatePreview() {
        if (layoutBarsWrapper != null) {
            layoutBarsWrapper.removeAllViews();
            for (long n : numbersList) {
                TextView tv = new TextView(this);

                // Agar number 0 se bada hai toh "+" lagayein, minus khud ba khud show hoga
                if (n > 0) {
                    tv.setText("+" + n);
                } else {
                    tv.setText(String.valueOf(n));
                }

                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(11f);
                tv.setSingleLine(true);

                // 🔵 Blue Boxes Style
                GradientDrawable boxDesign = new GradientDrawable();
                boxDesign.setColor(Color.parseColor("#3B97E3"));
                boxDesign.setCornerRadius(6f);
                tv.setBackground(boxDesign);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, 80);
                params.setMargins(6, 0, 6, 0);
                tv.setLayoutParams(params);

                layoutBarsWrapper.addView(tv);
            }
        }
    }
}