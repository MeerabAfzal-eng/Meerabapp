package com.example.meerabapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import java.util.ArrayList;

public class activity_progress extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        // UI Mapping
        TextView tvUserInfo = findViewById(R.id.tv_user_info);
        TextView tvHigh = findViewById(R.id.tvHighest);
        TextView tvRecent = findViewById(R.id.tvLast);
        LineChart lineChart = findViewById(R.id.lineChart);

        SharedPreferences pref = getSharedPreferences("UserProfile", MODE_PRIVATE);
        tvUserInfo.setText(pref.getString("user_name", "User") + " | ID: " + pref.getString("user_id", "000"));

        int high = pref.getInt("high_score", 0);
        int recent = pref.getInt("recent_score", 0);
        tvHigh.setText("Highest Score: " + high + "/20");
        tvRecent.setText("Last Score: " + recent + "/20");

        // History Logic
        ArrayList<Entry> entries = new ArrayList<>();
        String history = pref.getString("quiz_history", "");
        if (!history.isEmpty()) {
            String[] scores = history.split(",");
            for (int i = 0; i < scores.length; i++) {
                entries.add(new Entry((float) i, Float.parseFloat(scores[i])));
            }
        } else {
            entries.add(new Entry(0, 0));
        }

        float sum = 0;
        for (Entry e : entries) sum += e.getY();
        float average = entries.size() > 0 ? sum / entries.size() : 0;

        // Dataset Styling
        LineDataSet dataSet = new LineDataSet(entries, "Quiz Performance");
        dataSet.setColor(Color.parseColor("#001F3F"));
        dataSet.setCircleColor(Color.parseColor("#0040FF"));
        dataSet.setLineWidth(3f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#E2E8F0"));
        dataSet.setValueTextSize(12f);

        lineChart.setData(new LineData(dataSet));

        // Smart Marker Logic (Left/Right edge clipping fix)
        MarkerView mv = new MarkerView(this, R.layout.marker_layout) {
            private final TextView tvContent = findViewById(R.id.tvContent);
            @Override
            public void refreshContent(Entry e, Highlight highlight) {
                tvContent.setText("Score: " + (int)e.getY());
                super.refreshContent(e, highlight);
            }
            @Override
            public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {
                float width = lineChart.getWidth();
                // Left side check
                if (posX < width * 0.2f) return new MPPointF(10, -getHeight());
                // Right side check
                if (posX > width * 0.8f) return new MPPointF(-(getWidth() + 10), -getHeight());
                // Center
                return new MPPointF(-(getWidth() / 2), -getHeight());
            }
        };
        lineChart.setMarker(mv);
        lineChart.setTouchEnabled(true);
        lineChart.setHighlightPerTapEnabled(true);

        // Average Line
        LimitLine avgLine = new LimitLine(average, "Average: " + String.format("%.1f", average));
        avgLine.setLineColor(Color.RED);
        avgLine.setLineWidth(2f);
        avgLine.enableDashedLine(10f, 10f, 0f);
        avgLine.setTextColor(Color.RED);
        lineChart.getAxisLeft().addLimitLine(avgLine);

        // Final view settings
        lineChart.setExtraOffsets(20f, 10f, 20f, 10f); // Charon taraf extra space
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.getAxisLeft().setAxisMaximum(20f);
        lineChart.getAxisRight().setEnabled(false);

        lineChart.animateX(1500);
        lineChart.invalidate();
    }
}