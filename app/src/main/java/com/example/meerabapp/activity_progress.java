package com.example.meerabapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import java.util.ArrayList;
import java.util.Arrays;

public class activity_progress extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        LineChart lineChart = findViewById(R.id.lineChart);
        View statsCard = findViewById(R.id.statsCard);
        TextView tvUserInfo = findViewById(R.id.tv_user_info);
        TextView tvHigh = findViewById(R.id.tvHighest);
        TextView tvRecent = findViewById(R.id.tvLast);
        TextView tvTotalAttempts = findViewById(R.id.tvTotalAttempts);

        SharedPreferences pref = getSharedPreferences("UserProfile", MODE_PRIVATE);
        tvUserInfo.setText(pref.getString("user_name", "User") + " | ID: " + pref.getString("user_id", "000"));
        tvHigh.setText("Highest Score: " + pref.getInt("high_score", 0) + "/20");
        tvRecent.setText("Last Score: " + pref.getInt("recent_score", 0) + "/20");

        // History Logic
        ArrayList<Entry> entries = new ArrayList<>();
        String history = pref.getString("quiz_history", "");
        if (history != null && !history.isEmpty()) {
            String[] allScores = history.split(",");
            tvTotalAttempts.setText("Total Attempts: " + allScores.length);
            int start = Math.max(0, allScores.length - 25);
            for (int i = start; i < allScores.length; i++) {
                entries.add(new Entry((float) (i - start), Float.parseFloat(allScores[i].trim())));
            }
        } else {
            entries.add(new Entry(0, 0));
            tvTotalAttempts.setText("Total Attempts: 0");
        }

        LineDataSet dataSet = new LineDataSet(entries, "Quiz Performance (Last 25)");
        dataSet.setColor(Color.parseColor("#001F3F"));
        dataSet.setCircleColor(Color.parseColor("#0040FF"));
        dataSet.setLineWidth(3f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#E2E8F0"));
        lineChart.setData(new LineData(dataSet));

        // Fix: Marker with Left/Right Edge Detection
        MarkerView mv = new MarkerView(this, R.layout.marker_layout) {
            private final TextView tvContent = findViewById(R.id.tvContent);
            @Override
            public void refreshContent(Entry e, Highlight highlight) {
                tvContent.setText("Score: " + (int)e.getY());
                super.refreshContent(e, highlight);
            }
            @Override
            public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {
                float markerWidth = getWidth();
                float chartWidth = lineChart.getWidth();
                // Left edge detect
                if (posX < markerWidth) return new MPPointF(0, -getHeight());
                // Right edge detect
                if (posX > chartWidth - markerWidth) return new MPPointF(-markerWidth, -getHeight());
                // Center
                return new MPPointF(-(markerWidth / 2), -getHeight());
            }
        };
        lineChart.setMarker(mv);

        // Styling and Legend
        Legend legend = lineChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setDrawInside(false);
        lineChart.setExtraBottomOffset(30f);
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        // Animation
        lineChart.animateX(1500);
        statsCard.setAlpha(0f);
        statsCard.setTranslationY(100f);
        statsCard.animate().alpha(1f).translationY(0f).setDuration(1000).start();

        lineChart.invalidate();
    }
}