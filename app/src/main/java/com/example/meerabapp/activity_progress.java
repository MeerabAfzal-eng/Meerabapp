 package com.example.meerabapp;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;

public class activity_progress extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        LineChart lineChart = findViewById(R.id.progressChart);
        DatabaseHelper db = new DatabaseHelper(this);

        ArrayList<Entry> entries = new ArrayList<>();
        Cursor cursor = db.getAllQuizResults();

        // Database se data nikalna
        int quizCount = 1;
        if (cursor != null && cursor.moveToFirst()) {
            // column index get karna (score column)
            int scoreIndex = cursor.getColumnIndex("score");

            if (scoreIndex != -1) {
                do {
                    float score = (float) cursor.getInt(scoreIndex);
                    entries.add(new Entry(quizCount++, score));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        // Agar data hai toh graph plot karein
        if (!entries.isEmpty()) {
            LineDataSet dataSet = new LineDataSet(entries, "Quiz Score Progress");
            dataSet.setColor(Color.parseColor("#001F3F")); // Dark Blue Theme
            dataSet.setCircleColor(Color.RED);
            dataSet.setLineWidth(3f);
            dataSet.setCircleRadius(5f);
            dataSet.setValueTextSize(12f);

            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);
            lineChart.getDescription().setEnabled(false); // Text description hatane ke liye
            lineChart.animateX(1000); // Graph smoothly load hoga
            lineChart.invalidate();
        } else {
            Toast.makeText(this, "No quiz data available yet!", Toast.LENGTH_SHORT).show();
        }
    }
}