package com.example.meerabapp;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class ComparisonScreen extends AppCompatActivity {

    private static class CompareStep {
        ArrayList<Integer> stateA;
        ArrayList<Integer> stateB;
        int activeA1, activeA2;
        int activeB1, activeB2;
        HashSet<Integer> sortedA;
        HashSet<Integer> sortedB;
        int swapsA, swapsB;
        boolean isFinishedA;
        boolean isFinishedB;

        CompareStep(ArrayList<Integer> sA, ArrayList<Integer> sB, int aA1, int aA2, int aB1, int aB2,
                    HashSet<Integer> sDA, HashSet<Integer> sDB, int swA, int swB, boolean fA, boolean fB) {
            this.stateA = new ArrayList<>(sA);
            this.stateB = new ArrayList<>(sB);
            this.activeA1 = aA1; this.activeA2 = aA2;
            this.activeB1 = aB1; this.activeB2 = aB2;
            this.sortedA = new HashSet<>(sDA);
            this.sortedB = new HashSet<>(sDB);
            this.swapsA = swA; this.swapsB = swB;
            this.isFinishedA = fA;
            this.isFinishedB = fB;
        }
    }

    private ArrayList<CompareStep> timelineSteps = new ArrayList<>();
    private ArrayList<Integer> initialNumbers;

    private Spinner spinnerAlgoA, spinnerAlgoB;
    private LinearLayout containerBarsA, containerBarsB;
    private TextView lblAlgoA, lblAlgoB, txtSwapsA, txtSwapsB, txtTimerA, txtTimerB;
    private Thread raceThread;

    private ToneGenerator processToneGenerator;
    private ToneGenerator successToneGenerator;

    private int finalTotalSwapsA = 0;
    private int finalTotalSwapsB = 0;

    private long finalDurationA = 0;
    private long finalDurationB = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparison_screen);

        processToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 85);
        successToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        spinnerAlgoA = findViewById(R.id.spinnerAlgoA);
        spinnerAlgoB = findViewById(R.id.spinnerAlgoB);
        lblAlgoA = findViewById(R.id.lblAlgoA);
        lblAlgoB = findViewById(R.id.lblAlgoB);
        txtSwapsA = findViewById(R.id.txtSwapsA);
        txtSwapsB = findViewById(R.id.txtSwapsB);
        txtTimerA = findViewById(R.id.txtTimerA);
        txtTimerB = findViewById(R.id.txtTimerB);
        containerBarsA = findViewById(R.id.containerBarsA);
        containerBarsB = findViewById(R.id.containerBarsB);

        String[] targetedAlgorithms = {
                "Bubble Sort", "Insertion Sort", "Selection Sort",
                "Merge Sort", "Quick Sort", "Heap Sort", "Shell Sort"
        };

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, targetedAlgorithms);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerAlgoA.setAdapter(spinnerAdapter);
        spinnerAlgoB.setAdapter(spinnerAdapter);

        spinnerAlgoA.setSelection(0);
        spinnerAlgoB.setSelection(1);

        ArrayList<Integer> incomingNumbers = getIntent().getIntegerArrayListExtra("numbers");
        if (incomingNumbers != null) {
            initialNumbers = new ArrayList<>(incomingNumbers);
            if (initialNumbers.size() > 30) {
                initialNumbers = new ArrayList<>(initialNumbers.subList(0, 30));
            }
        } else {
            initialNumbers = new ArrayList<>();
            for(int i = 1; i <= 12; i++) initialNumbers.add((int)(Math.random() * 90) + 10);
        }

        resetComparisonUI();

        findViewById(R.id.btnStartCompare).setOnClickListener(v -> executeTargetedRace());
        findViewById(R.id.btnResetCompare).setOnClickListener(v -> resetComparisonUI());
    }

    private void resetComparisonUI() {
        if (raceThread != null && raceThread.isAlive()) raceThread.interrupt();

        lblAlgoA.setText(spinnerAlgoA.getSelectedItem().toString());
        lblAlgoB.setText(spinnerAlgoB.getSelectedItem().toString());
        txtSwapsA.setText("Swaps: 0");
        txtSwapsB.setText("Swaps: 0");
        txtTimerA.setText("0.00s");
        txtTimerB.setText("0.00s");

        finalDurationA = 0;
        finalDurationB = 0;
        finalTotalSwapsA = 0;
        finalTotalSwapsB = 0;

        renderBaseState(containerBarsA, initialNumbers);
        renderBaseState(containerBarsB, initialNumbers);
    }

    private void renderBaseState(LinearLayout container, ArrayList<Integer> list) {
        container.removeAllViews();
        for (int item : list) {
            TextView bar = new TextView(this);
            bar.setText(String.valueOf(item));
            bar.setGravity(Gravity.CENTER);
            bar.setTextColor(Color.WHITE);
            bar.setTextSize(11f);
            bar.setSingleLine(true);

            GradientDrawable design = new GradientDrawable();
            design.setCornerRadius(10f);
            design.setColor(Color.parseColor("#0040FF")); // Bright Blue
            bar.setBackground(design);

            LinearLayout.LayoutParams space = new LinearLayout.LayoutParams(110, 100);
            space.setMargins(6, 6, 6, 6);
            container.addView(bar, space);
        }
    }

    private void executeTargetedRace() {
        if (raceThread != null && raceThread.isAlive()) raceThread.interrupt();

        String selectedA = spinnerAlgoA.getSelectedItem().toString();
        String selectedB = spinnerAlgoB.getSelectedItem().toString();

        lblAlgoA.setText(selectedA);
        lblAlgoB.setText(selectedB);

        finalDurationA = 0;
        finalDurationB = 0;

        raceThread = new Thread(() -> {
            timelineSteps.clear();

            ArrayList<Integer> workingA = new ArrayList<>(initialNumbers);
            ArrayList<Integer> workingB = new ArrayList<>(initialNumbers);

            int[] swapCounterA = new int[]{0};
            int[] swapCounterB = new int[]{0};

            ArrayList<CompareStep> algorithmASteps = generateStepsForAlgo(workingA, selectedA, true, swapCounterA);
            ArrayList<CompareStep> algorithmBSteps = generateStepsForAlgo(workingB, selectedB, false, swapCounterB);

            finalTotalSwapsA = swapCounterA[0];
            finalTotalSwapsB = swapCounterB[0];

            int totalFrames = Math.max(algorithmASteps.size(), algorithmBSteps.size());

            for (int f = 0; f < totalFrames; f++) {
                boolean isOverA = (f >= algorithmASteps.size() - 1);
                boolean isOverB = (f >= algorithmBSteps.size() - 1);

                CompareStep stepDataA = isOverA ? algorithmASteps.get(algorithmASteps.size() - 1) : algorithmASteps.get(f);
                CompareStep stepDataB = isOverB ? algorithmBSteps.get(algorithmBSteps.size() - 1) : algorithmBSteps.get(f);

                timelineSteps.add(new CompareStep(
                        stepDataA.stateA, stepDataB.stateB,
                        isOverA ? -1 : stepDataA.activeA1, isOverA ? -1 : stepDataA.activeA2,
                        isOverB ? -1 : stepDataB.activeB1, isOverB ? -1 : stepDataB.activeB2,
                        stepDataA.sortedA, stepDataB.sortedB,
                        isOverA ? finalTotalSwapsA : stepDataA.swapsA,
                        isOverB ? finalTotalSwapsB : stepDataB.swapsB,
                        isOverA, isOverB
                ));
            }

            long tickerStart = SystemClock.elapsedRealtime();
            for (int t = 0; t < timelineSteps.size(); t++) {
                if (Thread.interrupted()) return;
                CompareStep activeFrame = timelineSteps.get(t);
                long currentDuration = SystemClock.elapsedRealtime() - tickerStart;

                // 🎵 ✅ AWESOME CHANGE 1: Jab sorting chal rhi ho to pyari si crisp click/tinkle sound aye gi
                if ((!activeFrame.isFinishedA && (activeFrame.activeA1 != -1)) ||
                        (!activeFrame.isFinishedB && (activeFrame.activeB1 != -1))) {
                    try {
                        processToneGenerator.stopTone();
                        // TONE_DTMF_1 se ek short aur pyari high-pitch game sound baje gi
                        processToneGenerator.startTone(ToneGenerator.TONE_DTMF_1, 35);
                    } catch (Exception ignored) {}
                }

                runOnUiThread(() -> refreshDynamicDisplay(activeFrame, currentDuration));

                try {
                    Thread.sleep(350);
                } catch (InterruptedException e) { return; }
            }


            try {
                successToneGenerator.stopTone();
                successToneGenerator.startTone(ToneGenerator.TONE_DTMF_D, 150);
                SystemClock.sleep(100);
                successToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 200);
            } catch (Exception ignored) {}

            runOnUiThread(() -> {
                String finalResult;
                if (finalTotalSwapsA < finalTotalSwapsB) {
                    finalResult = "🏆 " + selectedA + " is more efficient! \n(Swaps: " + finalTotalSwapsA + " vs " + finalTotalSwapsB + ")";
                } else if (finalTotalSwapsB < finalTotalSwapsA) {
                    finalResult = "🏆 " + selectedB + " is more efficient! \n(Swaps: " + finalTotalSwapsB + " vs " + finalTotalSwapsA + ")";
                } else {
                    finalResult = "🤝 It's a structural tie! Both took " + finalTotalSwapsA + " operations.";
                }
                Toast.makeText(this, finalResult, Toast.LENGTH_LONG).show();
            });
        });

        raceThread.start();
    }

    private ArrayList<CompareStep> generateStepsForAlgo(ArrayList<Integer> array, String name, boolean isPanelA, int[] counter) {
        ArrayList<CompareStep> steps = new ArrayList<>();
        HashSet<Integer> sorted = new HashSet<>();
        int n = array.size();

        if (name.equalsIgnoreCase("Bubble Sort")) {
            for (int i = 0; i < n - 1; i++) {
                for (int j = 0; j < n - i - 1; j++) {
                    if (array.get(j) > array.get(j + 1)) {
                        Collections.swap(array, j, j + 1);
                        counter[0]++;
                    }
                    steps.add(createFrame(array, j, j + 1, sorted, counter[0], isPanelA));
                }
                sorted.add(n - i - 1);
            }
            for(int k=0; k<n; k++) sorted.add(k);
            steps.add(createFrame(array, -1, -1, sorted, counter[0], isPanelA));
        }
        else if (name.equalsIgnoreCase("Selection Sort")) {
            for (int i = 0; i < n; i++) {
                int minIdx = i;
                for (int j = i + 1; j < n; j++) {
                    steps.add(createFrame(array, minIdx, j, sorted, counter[0], isPanelA));
                    if (array.get(j) < array.get(minIdx)) minIdx = j;
                }
                if (minIdx != i) {
                    Collections.swap(array, i, minIdx);
                    counter[0]++;
                }
                sorted.add(i);
                steps.add(createFrame(array, i, minIdx, sorted, counter[0], isPanelA));
            }
        }
        else if (name.equalsIgnoreCase("Insertion Sort")) {
            sorted.add(0);
            for (int i = 1; i < n; i++) {
                int key = array.get(i);
                int j = i - 1;
                while (j >= 0 && array.get(j) > key) {
                    array.set(j + 1, array.get(j));
                    counter[0]++;
                    steps.add(createFrame(array, j, j + 1, sorted, counter[0], isPanelA));
                    j--;
                }
                array.set(j + 1, key);
                for(int k = 0; k <= i; k++) sorted.add(k);
                steps.add(createFrame(array, j + 1, i, sorted, counter[0], isPanelA));
            }
            for(int k=0; k<n; k++) sorted.add(k);
            steps.add(createFrame(array, -1, -1, sorted, counter[0], isPanelA));
        }
        else if (name.equalsIgnoreCase("Shell Sort")) {
            for (int gap = n / 2; gap > 0; gap /= 2) {
                for (int i = gap; i < n; i++) {
                    int temp = array.get(i);
                    int j;
                    for (j = i; j >= gap && array.get(j - gap) > temp; j -= gap) {
                        array.set(j, array.get(j - gap));
                        counter[0]++;
                        steps.add(createFrame(array, j, j - gap, sorted, counter[0], isPanelA));
                    }
                    array.set(j, temp);
                    if (gap == 1) {
                        for(int k = 0; k <= i; k++) sorted.add(k);
                    }
                    steps.add(createFrame(array, j, -1, sorted, counter[0], isPanelA));
                }
            }
            for(int k=0; k<n; k++) sorted.add(k);
            steps.add(createFrame(array, -1, -1, sorted, counter[0], isPanelA));
        }
        else if (name.equalsIgnoreCase("Heap Sort")) {
            for (int i = n / 2 - 1; i >= 0; i--) {
                runHeapifySimulation(array, n, i, steps, sorted, counter, isPanelA);
            }
            for (int i = n - 1; i > 0; i--) {
                Collections.swap(array, 0, i);
                counter[0]++;
                sorted.add(i);
                steps.add(createFrame(array, 0, i, sorted, counter[0], isPanelA));
                runHeapifySimulation(array, i, 0, steps, sorted, counter, isPanelA);
            }
            for(int k=0; k<n; k++) sorted.add(k);
            steps.add(createFrame(array, -1, -1, sorted, counter[0], isPanelA));
        }
        else if (name.equalsIgnoreCase("Quick Sort")) {
            runQuickSortSimulation(array, 0, n - 1, steps, sorted, counter, isPanelA);
            for(int k=0; k<n; k++) sorted.add(k);
            steps.add(createFrame(array, -1, -1, sorted, counter[0], isPanelA));
        }
        else if (name.equalsIgnoreCase("Merge Sort")) {
            runMergeSortSimulation(array, 0, n - 1, steps, sorted, counter, isPanelA);
            for(int k=0; k<n; k++) sorted.add(k);
            steps.add(createFrame(array, -1, -1, sorted, counter[0], isPanelA));
        }

        return steps;
    }

    private void runHeapifySimulation(ArrayList<Integer> arr, int size, int root, ArrayList<CompareStep> steps, HashSet<Integer> sorted, int[] counter, boolean isPanelA) {
        int largest = root;
        int l = 2 * root + 1;
        int r = 2 * root + 2;

        if (l < size && arr.get(l) > arr.get(largest)) largest = l;
        if (r < size && arr.get(r) > arr.get(largest)) largest = r;

        if (largest != root) {
            Collections.swap(arr, root, largest);
            counter[0]++;
            steps.add(createFrame(arr, root, largest, sorted, counter[0], isPanelA));
            runHeapifySimulation(arr, size, largest, steps, sorted, counter, isPanelA);
        }
    }

    private void runQuickSortSimulation(ArrayList<Integer> arr, int low, int high, ArrayList<CompareStep> steps, HashSet<Integer> sorted, int[] counter, boolean isPanelA) {
        if (low < high) {
            int pivot = arr.get(high);
            int i = (low - 1);
            for (int j = low; j < high; j++) {
                steps.add(createFrame(arr, j, high, sorted, counter[0], isPanelA));
                if (arr.get(j) < pivot) {
                    i++;
                    Collections.swap(arr, i, j);
                    counter[0]++;
                    steps.add(createFrame(arr, i, j, sorted, counter[0], isPanelA));
                }
            }
            Collections.swap(arr, i + 1, high);
            counter[0]++;
            sorted.add(i + 1);
            steps.add(createFrame(arr, i + 1, high, sorted, counter[0], isPanelA));

            runQuickSortSimulation(arr, low, i - 1, steps, sorted, counter, isPanelA);
            runQuickSortSimulation(arr, i + 1, high, steps, sorted, counter, isPanelA);
        } else if (low == high) {
            sorted.add(low);
        }
    }

    private void runMergeSortSimulation(ArrayList<Integer> arr, int l, int r, ArrayList<CompareStep> steps, HashSet<Integer> sorted, int[] counter, boolean isPanelA) {
        if (l < r) {
            int m = l + (r - l) / 2;
            runMergeSortSimulation(arr, l, m, steps, sorted, counter, isPanelA);
            runMergeSortSimulation(arr, m + 1, r, steps, sorted, counter, isPanelA);

            ArrayList<Integer> leftList = new ArrayList<>(arr.subList(l, m + 1));
            ArrayList<Integer> rightList = new ArrayList<>(arr.subList(m + 1, r + 1));
            int i = 0, j = 0, k = l;
            while (i < leftList.size() && j < rightList.size()) {
                if (leftList.get(i) <= rightList.get(j)) {
                    arr.set(k++, leftList.get(i++));
                } else {
                    arr.set(k++, rightList.get(j++));
                }
                counter[0]++;
                sorted.add(k - 1);
                steps.add(createFrame(arr, k - 1, -1, sorted, counter[0], isPanelA));
            }
            while (i < leftList.size()) {
                arr.set(k, leftList.get(i++));
                sorted.add(k);
                k++;
            }
            while (j < rightList.size()) {
                arr.set(k, rightList.get(j++));
                sorted.add(k);
                k++;
            }
        } else if (l == r) {
            sorted.add(l);
        }
    }

    private CompareStep createFrame(ArrayList<Integer> arr, int act1, int act2, HashSet<Integer> srtd, int ops, boolean isPanelA) {
        if (isPanelA) {
            return new CompareStep(arr, initialNumbers, act1, act2, -1, -1, new HashSet<>(srtd), new HashSet<>(), ops, 0, false, false);
        } else {
            return new CompareStep(initialNumbers, arr, -1, -1, act1, act2, new HashSet<>(), new HashSet<>(srtd), 0, ops, false, false);
        }
    }

    private void refreshDynamicDisplay(CompareStep frame, long durationMs) {
        if (!frame.isFinishedA && finalDurationA == 0) {
            txtTimerA.setText(String.format("%.2f s", durationMs / 1000.0));
        } else if (frame.isFinishedA && finalDurationA == 0) {
            finalDurationA = durationMs;
            txtTimerA.setText(String.format("%.2f s", finalDurationA / 1000.0));
        }

        if (!frame.isFinishedB && finalDurationB == 0) {
            txtTimerB.setText(String.format("%.2f s", durationMs / 1000.0));
        } else if (frame.isFinishedB && finalDurationB == 0) {
            finalDurationB = durationMs;
            txtTimerB.setText(String.format("%.2f s", finalDurationB / 1000.0));
        }

        txtSwapsA.setText("Swaps: " + frame.swapsA);
        txtSwapsB.setText("Swaps: " + frame.swapsB);

        drawActiveTimelineBars(containerBarsA, frame.stateA, frame.activeA1, frame.activeA2, frame.sortedA);
        drawActiveTimelineBars(containerBarsB, frame.stateB, frame.activeB1, frame.activeB2, frame.sortedB);
    }

    private void drawActiveTimelineBars(LinearLayout container, ArrayList<Integer> items, int a1, int a2, HashSet<Integer> sorted) {
        container.removeAllViews();
        for (int m = 0; m < items.size(); m++) {
            TextView cell = new TextView(this);
            cell.setText(String.valueOf(items.get(m)));
            cell.setGravity(Gravity.CENTER);
            cell.setTextColor(Color.WHITE);
            cell.setTextSize(11f);
            cell.setSingleLine(true);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(10f);

            if (m == a1 || m == a2) {
                drawable.setColor(Color.parseColor("#9B59B6"));
            } else if (sorted.contains(m)) {
                drawable.setColor(Color.parseColor("#008080"));
            } else {
                drawable.setColor(Color.parseColor("#0040FF"));
            }

            cell.setBackground(drawable);

            LinearLayout.LayoutParams rule = new LinearLayout.LayoutParams(110, 100);
            rule.setMargins(6, 6, 6, 6);
            container.addView(cell, rule);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (processToneGenerator != null) {
            processToneGenerator.release();
        }
        if (successToneGenerator != null) {
            successToneGenerator.release();
        }
    }
}