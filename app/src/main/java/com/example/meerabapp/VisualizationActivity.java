package com.example.meerabapp;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class VisualizationActivity extends AppCompatActivity {

    private class SortingStep {
        ArrayList<Integer> state;
        int active1, active2;
        HashSet<Integer> sortedIndices;
        int swaps;
        String logEntry;

        SortingStep(ArrayList<Integer> s, int a1, int a2, HashSet<Integer> si, int sw, String l) {
            this.state = new ArrayList<>(s);
            this.active1 = a1; this.active2 = a2;
            this.sortedIndices = new HashSet<>(si);
            this.swaps = sw; this.logEntry = l;
        }
    }

    private ArrayList<SortingStep> recordedSteps = new ArrayList<>();
    private ArrayList<Integer> numbers, originalNumbers;
    private HashSet<Integer> currentSortedIndices = new HashSet<>();
    private String selectedAlgo;
    private int swapCount = 0;
    private boolean isAscending = true;
    private ToneGenerator toneGen;
    private LinearLayout containerBars, arrowContainer;
    private TextView txtComplexity, txtTimer, txtAlgoName, txtSwaps, txtStepLog;
    private ScrollView logScroll;
    private Thread sortingThread;
    private StringBuilder fullLog = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualization);

        toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        containerBars = findViewById(R.id.containerBars);
        arrowContainer = findViewById(R.id.arrowContainer);
        txtComplexity = findViewById(R.id.txtComplexity);
        txtTimer = findViewById(R.id.txtTimer);
        txtAlgoName = findViewById(R.id.txtAlgoName);
        txtSwaps = findViewById(R.id.txtSwaps);
        txtStepLog = findViewById(R.id.txtStepLog);
        logScroll = findViewById(R.id.logScroll);

        ArrayList<Integer> received = getIntent().getIntegerArrayListExtra("numbers");
        selectedAlgo = getIntent().getStringExtra("algorithm");

        if (received != null) {
            originalNumbers = new ArrayList<>(received);
            // 30 numbers support for your documentation
            if (originalNumbers.size() > 30) {
                originalNumbers = new ArrayList<>(originalNumbers.subList(0, 30));
            }
        }

        txtAlgoName.setText(selectedAlgo);
        txtComplexity.setText(getComplexity(selectedAlgo));

        findViewById(R.id.btnAsc).setOnClickListener(v -> { isAscending = true; startRecording(); });
        findViewById(R.id.btnDesc).setOnClickListener(v -> { isAscending = false; startRecording(); });
        findViewById(R.id.btnRestart).setOnClickListener(v -> startRecording());
        findViewById(R.id.btnReset).setOnClickListener(v -> finish());

        startRecording();
    }

    private void startRecording() {
        if (sortingThread != null && sortingThread.isAlive()) sortingThread.interrupt();

        sortingThread = new Thread(() -> {
            recordedSteps.clear();
            currentSortedIndices.clear();
            fullLog.setLength(0);
            numbers = new ArrayList<>(originalNumbers);
            swapCount = 0;

            runUniversalAlgorithm();

            long uiStartTime = SystemClock.elapsedRealtime();
            for (int i = 0; i < recordedSteps.size(); i++) {
                if (Thread.interrupted()) return;

                final SortingStep step = recordedSteps.get(i);
                final int prevSortedCount = (i > 0) ? recordedSteps.get(i-1).sortedIndices.size() : 0;
                final boolean isLast = (i == recordedSteps.size() - 1);
                final long currentTime = SystemClock.elapsedRealtime() - uiStartTime;

                runOnUiThread(() -> {
                    updateUI(step, currentTime);
                    if (step.sortedIndices.size() > prevSortedCount) {
                        playTone(ToneGenerator.TONE_PROP_BEEP, 80);
                    }
                    if (isLast) {
                        playTone(ToneGenerator.TONE_CDMA_HIGH_L, 500);
                    }
                });

                try { Thread.sleep(450); } catch (InterruptedException e) { return; }
            }
        });
        sortingThread.start();
    }

    private void runUniversalAlgorithm() {
        int n = numbers.size();
        for (int i = 0; i < n; i++) {
            int targetIdx = i;
            for (int j = i + 1; j < n; j++) {
                record(targetIdx, j, "Comparing: " + numbers.get(targetIdx) + " and " + numbers.get(j));
                if (isAscending) {
                    if (numbers.get(j) < numbers.get(targetIdx)) targetIdx = j;
                } else {
                    if (numbers.get(j) > numbers.get(targetIdx)) targetIdx = j;
                }
            }
            if (targetIdx != i) {
                record(i, targetIdx, "Swapping " + numbers.get(i) + " with " + numbers.get(targetIdx));
                Collections.swap(numbers, i, targetIdx);
                swapCount++;
            }
            currentSortedIndices.add(i);
            record(i, -1, "Number " + numbers.get(i) + " is sorted at index " + i);
        }
        record(-1, -1, "Process Finished Successfully!");
    }

    private void record(int a1, int a2, String log) {
        recordedSteps.add(new SortingStep(numbers, a1, a2, currentSortedIndices, swapCount, log));
    }

    private void updateUI(SortingStep step, long elapsedMs) {
        txtTimer.setText(String.format("%.2f s", elapsedMs / 1000.0));
        txtSwaps.setText("Swaps: " + step.swaps);
        fullLog.append("> ").append(step.logEntry).append("\n");
        txtStepLog.setText(fullLog.toString());
        logScroll.post(() -> logScroll.fullScroll(ScrollView.FOCUS_DOWN));

        containerBars.removeAllViews();
        arrowContainer.removeAllViews();

        for (int k = 0; k < step.state.size(); k++) {
            TextView arrow = new TextView(this);
            arrow.setText(k == step.active1 ? "i ↓" : (k == step.active2 ? "j ↓" : ""));
            arrow.setGravity(Gravity.CENTER);
            arrow.setTextSize(10f);
            arrowContainer.addView(arrow, new LinearLayout.LayoutParams(65, 45));

            TextView box = new TextView(this);
            box.setText(String.valueOf(step.state.get(k)));
            box.setGravity(Gravity.CENTER); box.setTextColor(Color.WHITE);
            box.setTextSize(10f);
            GradientDrawable gd = new GradientDrawable();
            gd.setCornerRadius(6f);

            if (k == step.active1 || k == step.active2) gd.setColor(Color.parseColor("#7B1FA2"));
            else if (step.sortedIndices.contains(k)) gd.setColor(Color.parseColor("#008080"));
            else gd.setColor(Color.parseColor("#1E88E5"));

            box.setBackground(gd);
            LinearLayout.LayoutParams bP = new LinearLayout.LayoutParams(65, 65);
            bP.setMargins(2, 2, 2, 2);
            containerBars.addView(box, bP);
        }
    }

    private void playTone(int type, int dur) {
        try { toneGen.startTone(type, dur); } catch (Exception ignored) {}
    }

    private String getComplexity(String algo) {
        if (algo == null) return "Complexity: N/A";
        String a = algo.toLowerCase();
        if (a.contains("bubble") || a.contains("selection") || a.contains("insertion")) return "Complexity: O(n²)";
        return "Complexity: O(n log n)";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (toneGen != null) toneGen.release();
    }
}