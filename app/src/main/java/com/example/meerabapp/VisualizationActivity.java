
package com.example.meerabapp;



import android.graphics.Color;

import android.graphics.Typeface;

import android.graphics.drawable.GradientDrawable;

import android.media.AudioAttributes;

import android.media.AudioManager;

import android.media.ToneGenerator;

import android.os.Build;

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



// --- Sound Initialization Fixed ---

        try {

// Using a standard stream type (STREAM_MUSIC is common for apps)

            toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        } catch (Exception e) {

            e.printStackTrace();

// Fallback or disable sound if initialization fails

        }



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

            if (originalNumbers.size() > 30) originalNumbers = new ArrayList<>(originalNumbers.subList(0, 30));

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



            String algo = (selectedAlgo != null) ? selectedAlgo.toLowerCase() : "";



            if (algo.contains("bubble")) bubbleSort();

            else if (algo.contains("selection")) selectionSort();

            else if (algo.contains("insertion")) insertionSort();

            else if (algo.contains("quick")) quickSort(0, numbers.size() - 1);

            else if (algo.contains("merge")) mergeSort(0, numbers.size() - 1);

            else if (algo.contains("heap")) heapSort();

            else if (algo.contains("shell")) shellSort();



// Final Step: Mark all Teal

            for(int i=0; i<numbers.size(); i++) currentSortedIndices.add(i);

            record(-1, -1, "Sorting Complete!");



// Playback

            long uiStartTime = SystemClock.elapsedRealtime();

            for (int i = 0; i < recordedSteps.size(); i++) {

                if (Thread.interrupted()) return;

                final SortingStep step = recordedSteps.get(i);

                final long currentTime = SystemClock.elapsedRealtime() - uiStartTime;



// --- Updated UI Playback (Beep is played in updateUI now) ---

                runOnUiThread(() -> {

                    updateUI(step, currentTime);

// Standard Beep on comparison/swap (except start/end)

                    if (step.active1 != -1 && step.active2 != -1) {

                        playTone(ToneGenerator.TONE_PROP_BEEP, 70); // Slightly longer beep

                    }

                });



// --- Dynamic Speed: Faster updates, slower swaps ---

                try {

                    if (step.logEntry.startsWith("Swapping") || step.logEntry.startsWith("Inserted")) {

                        Thread.sleep(600); // Wait longer on swaps

                    } else {

                        Thread.sleep(300); // Normal speed

                    }

                } catch (InterruptedException e) { return; }

            }

        });

        sortingThread.start();

    }



// (Sorting Algorithm logic - same as before, no changes needed here)

    private void bubbleSort() {

        int n = numbers.size();

        for (int i = 0; i < n - 1; i++) {

            for (int j = 0; j < n - i - 1; j++) {

                record(j, j + 1, "Comparing neighbors: " + numbers.get(j) + " & " + numbers.get(j+1));

                if (isAscending ? (numbers.get(j) > numbers.get(j + 1)) : (numbers.get(j) < numbers.get(j + 1))) {

                    Collections.swap(numbers, j, j + 1);

                    swapCount++;

                    record(j, j + 1, "Swapping " + numbers.get(j+1) + " and " + numbers.get(j));

                }

            }

            currentSortedIndices.add(n - i - 1);

        }

    }



    private void selectionSort() {

        int n = numbers.size();

        for (int i = 0; i < n; i++) {

            int minIdx = i;

            for (int j = i + 1; j < n; j++) {

                record(minIdx, j, "Searching for minimum element...");

                if (isAscending ? (numbers.get(j) < numbers.get(minIdx)) : (numbers.get(j) > numbers.get(minIdx))) minIdx = j;

            }

            Collections.swap(numbers, i, minIdx);

            if(i != minIdx) swapCount++;

            currentSortedIndices.add(i);

            record(i, minIdx, "Found minimum. Swapping to index " + i);

        }

    }



    private void insertionSort() {

        int n = numbers.size();

        for (int i = 1; i < n; i++) {

            int key = numbers.get(i);

            int j = i - 1;

            record(i, j, "Picking " + key + " and comparing with " + numbers.get(j));

            while (j >= 0 && (isAscending ? (numbers.get(j) > key) : (numbers.get(j) < key))) {

                numbers.set(j + 1, numbers.get(j));

                record(j, j + 1, "Shifting " + numbers.get(j) + " to the right");

                j--;

                swapCount++;

            }

            numbers.set(j + 1, key);

            for(int k=0; k<=i; k++) currentSortedIndices.add(k);

            record(j + 1, -1, "Inserted " + key + " in sorted portion");

        }

    }



    private void quickSort(int low, int high) {

        if (low < high) {

            int pi = partition(low, high);

            quickSort(low, pi - 1);

            quickSort(pi + 1, high);

        }

    }



    private int partition(int low, int high) {

        int pivot = numbers.get(high);

        int i = (low - 1);

        record(-1, high, "Pivot selected: " + pivot);

        for (int j = low; j < high; j++) {

            record(j, high, "Comparing " + numbers.get(j) + " with Pivot");

            if (isAscending ? (numbers.get(j) < pivot) : (numbers.get(j) > pivot)) {

                i++;

                Collections.swap(numbers, i, j);

                swapCount++;

                record(i, j, "Swapping around pivot");

            }

        }

        Collections.swap(numbers, i + 1, high);

        record(i + 1, high, "Placing Pivot in final position");

        currentSortedIndices.add(i + 1);

        return i + 1;

    }



    private void mergeSort(int l, int r) {

        if (l < r) {

            int m = l + (r - l) / 2;

            mergeSort(l, m);

            mergeSort(m + 1, r);

            merge(l, m, r);

        }

    }



    private void merge(int l, int m, int r) {

        record(l, r, "Merging segments [" + l + " to " + m + "] and [" + (m+1) + " to " + r + "]");

        ArrayList<Integer> left = new ArrayList<>(numbers.subList(l, m + 1));

        ArrayList<Integer> right = new ArrayList<>(numbers.subList(m + 1, r + 1));

        int i = 0, j = 0, k = l;

        while (i < left.size() && j < right.size()) {

            if (isAscending ? (left.get(i) <= right.get(j)) : (left.get(i) >= right.get(j))) numbers.set(k++, left.get(i++));

            else numbers.set(k++, right.get(j++));

            record(k - 1, -1, "Combining sub-arrays...");

        }

        while (i < left.size()) numbers.set(k++, left.get(i++));

        while (j < right.size()) numbers.set(k++, right.get(j++));

    }



    private void heapSort() {

        int n = numbers.size();

        for (int i = n / 2 - 1; i >= 0; i--) heapify(n, i);

        for (int i = n - 1; i > 0; i--) {

            Collections.swap(numbers, 0, i);

            record(0, i, "Swapping root with last element");

            currentSortedIndices.add(i);

            heapify(i, 0);

        }

    }



    private void heapify(int n, int i) {

        int largest = i;

        int l = 2 * i + 1;

        int r = 2 * i + 2;

        record(i, -1, "Heapifying subtree at root " + numbers.get(i));

        if (l < n && (isAscending ? (numbers.get(l) > numbers.get(largest)) : (numbers.get(l) < numbers.get(largest)))) largest = l;

        if (r < n && (isAscending ? (numbers.get(r) > numbers.get(largest)) : (numbers.get(r) < numbers.get(largest)))) largest = r;

        if (largest != i) {

            Collections.swap(numbers, i, largest);

            swapCount++;

            record(i, largest, "Adjusting Heap structure...");

            heapify(n, largest);

        }

    }



    private void shellSort() {

        int n = numbers.size();

        for (int gap = n / 2; gap > 0; gap /= 2) {

            record(-1, -1, "Current Gap: " + gap);

            for (int i = gap; i < n; i++) {

                int temp = numbers.get(i);

                int j;

                for (j = i; j >= gap && (isAscending ? (numbers.get(j - gap) > temp) : (numbers.get(j - gap) < temp)); j -= gap) {

                    record(j, j - gap, "Comparing elements with gap " + gap);

                    numbers.set(j, numbers.get(j - gap));

                    swapCount++;

                }

                numbers.set(j, temp);

                record(j, -1, "Placing element after gap comparisons");

            }

        }

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

// --- Updated Arrows: Bigger, Bold, and Clear Pointer ---

            TextView arrow = new TextView(this);

// Use '⬇' for standard look, adding "i" or "j" labels for clarity.

            if (k == step.active1) {

                arrow.setText("i\n⬇");

                arrow.setTextColor(Color.parseColor("#7B1FA2")); // Purple matching the box

            } else if (k == step.active2) {

                arrow.setText("j\n⬇");

                arrow.setTextColor(Color.parseColor("#7B1FA2")); // Purple matching the box

            } else {

                arrow.setText("");

            }

            arrow.setGravity(Gravity.CENTER);

            arrow.setTextSize(16f); // --- Increased from 12f ---

            arrow.setTypeface(null, Typeface.BOLD); // --- Made Bold ---



// Layout params (Adjust width slightly if labels make them tight)

            LinearLayout.LayoutParams aP = new LinearLayout.LayoutParams(70, 100); // More height for labels

            arrowContainer.addView(arrow, aP);



// Boxes

            TextView box = new TextView(this);

            box.setText(String.valueOf(step.state.get(k)));

            box.setGravity(Gravity.CENTER); box.setTextColor(Color.WHITE); box.setTextSize(11f);



            GradientDrawable gd = new GradientDrawable();

            gd.setCornerRadius(8f);



            if (k == step.active1 || k == step.active2) {

                gd.setColor(Color.parseColor("#7B1FA2")); // Purple for comparing

            } else if (step.sortedIndices.contains(k)) {

                gd.setColor(Color.parseColor("#008080")); // Teal for sorted

            } else {

                gd.setColor(Color.parseColor("#1E88E5")); // Blue for unsorted

            }



            box.setBackground(gd);

            LinearLayout.LayoutParams bP = new LinearLayout.LayoutParams(70, 75);

            bP.setMargins(4, 4, 4, 4);

            containerBars.addView(box, bP);

        }

    }



// --- Enhanced playTone function ---

    private void playTone(int type, int dur) {

        if (toneGen != null) {



            try {

// Ensure music volume isn't muted

                toneGen.startTone(type, dur);

            } catch (Exception ignored) {}

        }

    }



    private String getComplexity(String algo) {

        if (algo == null) return "Complexity: N/A";

        String a = algo.toLowerCase();

        if (a.contains("bubble") || a.contains("selection") || a.contains("insertion")) return "Complexity: O(n²)";

        if (a.contains("shell")) return "Complexity: O(n log² n)";

        return "Complexity: O(n log n)";

    }



    @Override

    protected void onDestroy() {

        super.onDestroy();

// --- Release ToneGenerator properly ---

        if (toneGen != null) {

            toneGen.release();

            toneGen = null;

        }

    }

}