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

    private ArrayList<Long> numbersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        layoutBarsWrapper = findViewById(R.id.layoutBarsWrapper);
        btnValueAdd = findViewById(R.id.btnValueAdd);
        btnValueRemove = findViewById(R.id.btnValueRemove);
        btnSortNow = findViewById(R.id.btnSortNow);
        spinnerAlgorithm = findViewById(R.id.spinnerAlgorithm);
        etInputNumber = findViewById(R.id.etInputNumber);
        btnCompareScreen = findViewById(R.id.btnCompareScreen);
        btnTakeQuiz = findViewById(R.id.btnTakeQuiz);
        btnViewProgress = findViewById(R.id.btnViewProgress);


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


        btnValueAdd.setOnClickListener(v -> {
            String valStr = etInputNumber.getText().toString().trim();
            if (!valStr.isEmpty()) {
                try {

                    if (numbersList.size() >= 30) {
                        Toast.makeText(this, "You can only add a maximum of 30 numbers!", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    if (valStr.equals("+") || valStr.equals("-")) {
                        Toast.makeText(this, "Please enter a valid number after the sign!", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    long inputVal = Long.parseLong(valStr);


                    if (inputVal > 1000000000L || inputVal < -1000000000L) {
                        Toast.makeText(this, "Value must be between -1 Arab and +1 Arab!", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    numbersList.add(inputVal);
                    updatePreview();
                    etInputNumber.setText("");

                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid number format!", Toast.LENGTH_SHORT).show();
                }
            }
        });


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


            ArrayList<Integer> intList = new ArrayList<>();
            for (Long l : numbersList) {
                intList.add(l.intValue());
            }

            intent.putIntegerArrayListExtra("numbers", intList);
            intent.putExtra("algorithm", selectedAlgo);
            startActivity(intent);
        });


        btnValueRemove.setOnClickListener(v -> {
            if (!numbersList.isEmpty()) {
                numbersList.remove(numbersList.size() - 1);
                updatePreview();
            } else {
                Toast.makeText(this, "List is already empty!", Toast.LENGTH_SHORT).show();
            }
        });


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


        btnTakeQuiz.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, activity_quiz.class)));
        btnViewProgress.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, activity_progress.class)));
    }


    private void updatePreview() {
        if (layoutBarsWrapper != null) {
            layoutBarsWrapper.removeAllViews();
            for (long n : numbersList) {
                TextView tv = new TextView(this);


                if (n > 0) {
                    tv.setText("+" + n);
                } else {
                    tv.setText(String.valueOf(n));
                }

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