package com.example.meerabapp;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;

public class VisualizationActivity extends AppCompatActivity {

    private TextView txtAlgoName, txtSwaps, txtTimer, txtComplexity, txtStepLog;
    private LinearLayout containerBars, arrowContainer;
    private ScrollView logScroll;
    private Button btnAsc, btnDesc, btnRestart, btnReset;

    private ArrayList<Integer> originalNumbers = new ArrayList<>();
    private ArrayList<Integer> currentNumbers = new ArrayList<>();
    private Thread sortingThread;

    // 🎵 Audio components lagaye gae hain
    private ToneGenerator processToneGenerator;
    private ToneGenerator successToneGenerator;

    private String selectedAlgorithm = "Bubble Sort";
    private boolean isAscending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualization);

        // 🔊 Audio initialization for sorting screen
        processToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 85);
        successToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        txtAlgoName = findViewById(R.id.txtAlgoName);
        txtSwaps = findViewById(R.id.txtSwaps);
        txtTimer = findViewById(R.id.txtTimer);
        txtComplexity = findViewById(R.id.txtComplexity);
        txtStepLog = findViewById(R.id.txtStepLog);
        containerBars = findViewById(R.id.containerBars);
        arrowContainer = findViewById(R.id.arrowContainer);
        logScroll = findViewById(R.id.logScroll);

        btnAsc = findViewById(R.id.btnAsc);
        btnDesc = findViewById(R.id.btnDesc);
        btnRestart = findViewById(R.id.btnRestart);
        btnReset = findViewById(R.id.btnReset);

        ArrayList<Integer> incoming = getIntent().getIntegerArrayListExtra("numbers");
        if (incoming != null) {
            originalNumbers = new ArrayList<>(incoming);
        } else {
            for (int i = 0; i < 10; i++) originalNumbers.add((int) (Math.random() * 90) + 10);
        }

        String algo = getIntent().getStringExtra("algorithm");
        if (algo != null) selectedAlgorithm = algo;

        txtAlgoName.setText(selectedAlgorithm);
        updateComplexityText();
        resetToInitialState();

        btnAsc.setOnClickListener(v -> startSortingProcess(true));
        btnDesc.setOnClickListener(v -> startSortingProcess(false));
        btnRestart.setOnClickListener(v -> startSortingProcess(isAscending));
        btnReset.setOnClickListener(v -> resetToInitialState());
    }

    private void updateComplexityText() {
        if (selectedAlgorithm.contains("Merge") || selectedAlgorithm.contains("Quick") || selectedAlgorithm.contains("Heap")) {
            txtComplexity.setText("O(n log n)");
        } else if (selectedAlgorithm.contains("Shell")) {
            txtComplexity.setText("O(n log² n)");
        } else {
            txtComplexity.setText("O(n²)");
        }
    }

    private void resetToInitialState() {
        if (sortingThread != null && sortingThread.isAlive()) sortingThread.interrupt();
        currentNumbers = new ArrayList<>(originalNumbers);
        txtSwaps.setText("Swaps: 0");
        txtTimer.setText("0.00s");
        txtStepLog.setText("Ready for Sorting...");
        renderBars(-1, -1, new java.util.HashSet<>());
    }

    private void startSortingProcess(boolean ascending) {
        if (sortingThread != null && sortingThread.isAlive()) sortingThread.interrupt();
        this.isAscending = ascending;
        currentNumbers = new ArrayList<>(originalNumbers);

        sortingThread = new Thread(() -> {
            long startTime = SystemClock.elapsedRealtime();
            int[] swapCount = {0};
            java.util.HashSet<Integer> sortedIndices = new java.util.HashSet<>();

            runSortingAlgorithm(swapCount, sortedIndices, startTime);

            if (!Thread.interrupted()) {
                // 🎵 ✅ AWESOME CHANGE 2: Jab pooray numbers successfully sort ho jayein ge to victory chime bajega!
                try {
                    successToneGenerator.stopTone();
                    successToneGenerator.startTone(ToneGenerator.TONE_DTMF_D, 150);
                    SystemClock.sleep(100);
                    successToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 200);
                } catch (Exception ignored) {}

                runOnUiThread(() -> {
                    txtStepLog.append("\n✨ Sorting Completed Successfully!");
                    logScroll.fullScroll(ScrollView.FOCUS_DOWN);
                    Toast.makeText(this, "Sorting Done!", Toast.LENGTH_SHORT).show();
                });
            }
        });
        sortingThread.start();
    }

    private void runSortingAlgorithm(int[] swapCount, java.util.HashSet<Integer> sortedIndices, long startTime) {
        int n = currentNumbers.size();

        if (selectedAlgorithm.equalsIgnoreCase("Bubble Sort")) {
            for (int i = 0; i < n - 1; i++) {
                for (int j = 0; j < n - i - 1; j++) {
                    if (Thread.interrupted()) return;
                    boolean condition = isAscending ? (currentNumbers.get(j) > currentNumbers.get(j + 1)) : (currentNumbers.get(j) < currentNumbers.get(j + 1));

                    // 🎵 ✅ AWESOME CHANGE 1: Har ek compare aur swap step par clicky note play hoga
                    playStepSound();

                    if (condition) {
                        Collections.swap(currentNumbers, j, j + 1);
                        swapCount[0]++;
                    }
                    updateUI(j, j + 1, swapCount[0], startTime, sortedIndices, "Comparing/Swapping index " + j + " and " + (j + 1));
                    sleepDelay();
                }
                sortedIndices.add(n - i - 1);
            }
            for (int i = 0; i < n; i++) sortedIndices.add(i);
            updateUI(-1, -1, swapCount[0], startTime, sortedIndices, "All elements sorted.");
        }
        else if (selectedAlgorithm.equalsIgnoreCase("Selection Sort")) {
            for (int i = 0; i < n; i++) {
                int targetIdx = i;
                for (int j = i + 1; j < n; j++) {
                    if (Thread.interrupted()) return;
                    playStepSound();
                    boolean condition = isAscending ? (currentNumbers.get(j) < currentNumbers.get(targetIdx)) : (currentNumbers.get(j) > currentNumbers.get(targetIdx));
                    if (condition) targetIdx = j;
                    updateUI(targetIdx, j, swapCount[0], startTime, sortedIndices, "Scanning elements...");
                    sleepDelay();
                }
                if (targetIdx != i) {
                    Collections.swap(currentNumbers, i, targetIdx);
                    swapCount[0]++;
                }
                sortedIndices.add(i);
                updateUI(i, targetIdx, swapCount[0], startTime, sortedIndices, "Placed element at index " + i);
                sleepDelay();
            }
        }
        else if (selectedAlgorithm.equalsIgnoreCase("Insertion Sort")) {
            sortedIndices.add(0);
            for (int i = 1; i < n; i++) {
                int key = currentNumbers.get(i);
                int j = i - 1;
                while (j >= 0) {
                    if (Thread.interrupted()) return;
                    playStepSound();
                    boolean condition = isAscending ? (currentNumbers.get(j) > key) : (currentNumbers.get(j) < key);
                    if (!condition) break;

                    currentNumbers.set(j + 1, currentNumbers.get(j));
                    swapCount[0]++;
                    updateUI(j, j + 1, swapCount[0], startTime, sortedIndices, "Shifting element at " + j);
                    sleepDelay();
                    j--;
                }
                currentNumbers.set(j + 1, key);
                for (int k = 0; k <= i; k++) sortedIndices.add(k);
                updateUI(j + 1, i, swapCount[0], startTime, sortedIndices, "Inserted key at index " + (j + 1));
                sleepDelay();
            }
        }
        // Fallback for remaining algorithms to show uniform update
        else {
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (Thread.interrupted()) return;
                    playStepSound();
                    boolean cond = isAscending ? (currentNumbers.get(i) > currentNumbers.get(j)) : (currentNumbers.get(i) < currentNumbers.get(j));
                    if (cond) {
                        Collections.swap(currentNumbers, i, j);
                        swapCount[0]++;
                    }
                    updateUI(i, j, swapCount[0], startTime, sortedIndices, "Sorting in progress...");
                    sleepDelay();
                }
                sortedIndices.add(i);
            }
        }
    }

    // Helper method to play active click chime
    private void playStepSound() {
        try {
            processToneGenerator.stopTone();
            processToneGenerator.startTone(ToneGenerator.TONE_DTMF_1, 35);
        } catch (Exception ignored) {}
    }

    private void sleepDelay() {
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void updateUI(int a1, int a2, int swaps, long start, java.util.HashSet<Integer> sorted, String msg) {
        long duration = SystemClock.elapsedRealtime() - start;
        runOnUiThread(() -> {
            txtSwaps.setText("Swaps: " + swaps);
            txtTimer.setText(String.format("%.2f s", duration / 1000.0));
            txtStepLog.append("\n→ " + msg);
            logScroll.fullScroll(ScrollView.FOCUS_DOWN);
            renderBars(a1, a2, sorted);
        });
    }

    private void renderBars(int active1, int active2, java.util.HashSet<Integer> sorted) {
        containerBars.removeAllViews();
        arrowContainer.removeAllViews();

        for (int m = 0; m < currentNumbers.size(); m++) {
            TextView bar = new TextView(this);
            bar.setText(String.valueOf(currentNumbers.get(m)));
            bar.setGravity(Gravity.CENTER);
            bar.setTextColor(Color.WHITE);
            bar.setTextSize(11f);
            bar.setSingleLine(true);

            GradientDrawable design = new GradientDrawable();
            design.setCornerRadius(10f);

            if (m == active1 || m == active2) {
                design.setColor(Color.parseColor("#9B59B6")); // Purple for comparing
            } else if (sorted.contains(m)) {
                design.setColor(Color.parseColor("#008080")); // Teal for sorted
            } else {
                design.setColor(Color.parseColor("#0040FF")); // Bright Blue for unsorted
            }
            bar.setBackground(design);

            LinearLayout.LayoutParams space = new LinearLayout.LayoutParams(110, 100);
            space.setMargins(6, 6, 6, 6);
            containerBars.addView(bar, space);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (processToneGenerator != null) processToneGenerator.release();
        if (successToneGenerator != null) successToneGenerator.release();
    }
}