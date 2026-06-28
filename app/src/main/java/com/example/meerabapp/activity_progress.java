package com.example.meerabapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;

public class activity_progress extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        TextView tvHigh = findViewById(R.id.tv_high_score);
        TextView tvRecent = findViewById(R.id.tv_recent_score);
        LineChart lineChart = findViewById(R.id.progressChart);

        SharedPreferences pref = getSharedPreferences("UserProfile", MODE_PRIVATE);
        int high = pref.getInt("high_score", 0);
        int recent = pref.getInt("recent_score", 0);
        String history = pref.getString("quiz_history", "");

        tvHigh.setText("Highest Score: " + high + "/20");
        tvRecent.setText("Last Score: " + recent + "/20");

        ArrayList<Entry> entries = new ArrayList<>();

        if (history.isEmpty()) {
            entries.add(new Entry(0, 5));
            entries.add(new Entry(1, 10));
            entries.add(new Entry(2, 8));
            entries.add(new Entry(3, 15));
        } else {
            String[] scores = history.split(",");
            for (int i = 0; i < scores.length; i++) {
                if(!scores[i].isEmpty()) {
                    entries.add(new Entry((float) i, Float.parseFloat(scores[i])));
                }
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "Quiz Performance");

        // Professional Styling
        dataSet.setColor(Color.parseColor("#001F3F")); // Navy Blue Line
        dataSet.setCircleColor(Color.parseColor("#0040FF")); // Bright Blue Dots
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setCircleRadius(6f);
        dataSet.setLineWidth(3f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#E2E8F0")); // Light Navy Fill
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.parseColor("#001F3F"));

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Axis Configuration
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setGranularity(1f); // Sirf 1, 2, 3 dikhaye
        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.getAxisLeft().setAxisMaximum(20f); // Max limit 20 fix
        lineChart.getAxisRight().setEnabled(false); // Right side hide ki

        lineChart.animateX(1000);
        lineChart.invalidate();
    }
}