package com.example.meerabapp;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
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

        LineChart lineChart = findViewById(R.id.progressChart);

        // 🛡️ Safety Check: Agar XML mein ID galat ho toh app crash na ho
        if (lineChart == null) {
            Toast.makeText(this, "Error: progressChart not found in XML!", Toast.LENGTH_LONG).show();
            return;
        }

        DatabaseHelper db = new DatabaseHelper(this);
        ArrayList<Entry> entries = new ArrayList<>();
        Cursor cursor = db.getAllQuizResults();

        // 📊 Database se Quiz ke data (Scores) ko nikalna
        int quizCount = 1;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                // Column name lowercase ya uppercase dono ko check karne ke liye safe approach
                int scoreIndex = cursor.getColumnIndex("score");
                if (scoreIndex == -1) {
                    scoreIndex = cursor.getColumnIndex("Score");
                }

                if (scoreIndex != -1) {
                    do {
                        float score = (float) cursor.getInt(scoreIndex);
                        // Entry(X-axis par quiz number, Y-axis par score)
                        entries.add(new Entry(quizCount++, score));
                    } while (cursor.moveToNext());
                } else {
                    Toast.makeText(this, "Database error: 'score' column missing!", Toast.LENGTH_LONG).show();
                }
            }
            cursor.close(); // Cursor ko close karna zaroori hai memory leak se bachne ke liye
        }

        // 📈 Agar data moojood hai toh graph plot karein
        if (!entries.isEmpty()) {
            LineDataSet dataSet = new LineDataSet(entries, "Quiz Score Progress");

            // 🎨 Premium Styling matching with your App Theme
            dataSet.setColor(Color.parseColor("#001F3F"));      // Dark Navy Blue Line
            dataSet.setCircleColor(Color.parseColor("#0040FF"));// Uniform Bright Blue Dots
            dataSet.setLineWidth(3f);                            // Thick premium line
            dataSet.setCircleRadius(5f);                         // Dot size
            dataSet.setValueTextSize(12f);                       // Font size of scores
            dataSet.setValueTextColor(Color.parseColor("#334155"));
            dataSet.setDrawFilled(true);                         // Graph ke neeche shadow fill karne ke liye
            dataSet.setFillColor(Color.parseColor("#E2E8F0"));   // Light grey/blue shadow fill

            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);

            // ⚙️ Chart Configuration for Clean Look
            lineChart.getDescription().setEnabled(false);        // Extra text clear kiya
            lineChart.getAxisRight().setEnabled(false);          // Right side wali border scale disable ki
            lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // X-Axis labels bottom par laye
            lineChart.getXAxis().setDrawGridLines(false);        // Vertical background grid lines band ki

            lineChart.animateX(1000);                            // 1 second ki smooth loading animation
            lineChart.invalidate();                              // Chart refresh completely
        } else {
            // Agar pehli dafa open kiya aur database khali hai
            Toast.makeText(this, "No quiz data available yet!", Toast.LENGTH_SHORT).show();
        }
    }
}