package com.example.meerabapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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

    // ✅ List of Strings to handle '+' and '-' signs correctly
    private ArrayList<String> rawInputList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI Mapping
        layoutBarsWrapper = findViewById(R.id.layoutBarsWrapper);
        btnValueAdd = findViewById(R.id.btnValueAdd);
        btnValueRemove = findViewById(R.id.btnValueRemove);
        btnSortNow = findViewById(R.id.btnSortNow);
        spinnerAlgorithm = findViewById(R.id.spinnerAlgorithm);
        etInputNumber = findViewById(R.id.etInputNumber);
        btnCompareScreen = findViewById(R.id.btnCompareScreen);
        btnTakeQuiz = findViewById(R.id.btnTakeQuiz);
        btnViewProgress = findViewById(R.id.btnViewProgress);

        // Spinner Setup
        String[] algorithms = {"Select Algorithm", "Bubble Sort", "Insertion Sort", "Selection Sort", "Merge Sort", "Quick Sort", "Heap Sort", "Shell Sort"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, algorithms) {
            @Override
            public boolean isEnabled(int position) { return position != 0; }
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(position == 0 ? Color.parseColor("#94A3B8") : Color.parseColor("#001F3F"));
                return view;
            }
        };
        spinnerAlgorithm.setAdapter(adapter);

        // 1. Add Value Logic
        btnValueAdd.setOnClickListener(v -> {
            String valStr = etInputNumber.getText().toString().trim();
            if (valStr.isEmpty() || valStr.equals("+") || valStr.equals("-")) {
                Toast.makeText(this, "Enter a valid number!", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                long inputVal = Long.parseLong(valStr);
                if (inputVal > 1000000000L || inputVal < -1000000000L) {
                    Toast.makeText(this, "Value must be between -1 and +1 Arab!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (rawInputList.size() >= 30) {
                    Toast.makeText(this, "Max 30 numbers allowed!", Toast.LENGTH_SHORT).show();
                    return;
                }
                rawInputList.add(valStr);
                updatePreview();
                etInputNumber.setText("");
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number!", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Remove Last Logic
        btnValueRemove.setOnClickListener(v -> {
            if (!rawInputList.isEmpty()) {
                rawInputList.remove(rawInputList.size() - 1);
                updatePreview();
            }
        });

        // 3. Sort Now Logic
        btnSortNow.setOnClickListener(v -> {
            if (spinnerAlgorithm.getSelectedItemPosition() == 0 || rawInputList.isEmpty()) {
                Toast.makeText(this, "Select Algorithm and add numbers!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this, VisualizationActivity.class);
            ArrayList<Integer> intList = new ArrayList<>();
            for (String s : rawInputList) intList.add(Integer.parseInt(s));
            intent.putIntegerArrayListExtra("numbers", intList);
            intent.putExtra("algorithm", spinnerAlgorithm.getSelectedItem().toString());
            startActivity(intent);
        });

        // 4. Compare Screen Logic
        btnCompareScreen.setOnClickListener(v -> {
            if (rawInputList.size() < 2) {
                Toast.makeText(this, "Add at least 2 numbers!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MainActivity.this, ComparisonScreen.class);
                ArrayList<Integer> intList = new ArrayList<>();
                for (String s : rawInputList) intList.add(Integer.parseInt(s));
                intent.putIntegerArrayListExtra("numbers", intList);
                startActivity(intent);
            }
        });

        // 5. Navigation
        btnTakeQuiz.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, activity_quiz.class)));
        btnViewProgress.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, activity_progress.class)));
    }

    private void updatePreview() {
        if (layoutBarsWrapper != null) {
            layoutBarsWrapper.removeAllViews();
            for (String s : rawInputList) {
                TextView tv = new TextView(this);
                tv.setText(s);
                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(11f);
                tv.setSingleLine(true);
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