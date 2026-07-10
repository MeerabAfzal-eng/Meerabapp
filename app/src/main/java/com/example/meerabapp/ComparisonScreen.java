package com.example.meerabapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class ComparisonScreen extends AppCompatActivity {

    private static class PointerLabel {
        int index;
        String text;
        PointerLabel(int index, String text) {
            this.index = index;
            this.text = text;
        }
    }

    private static class CompareStep {
        ArrayList<Integer> stateA;
        ArrayList<Integer> stateB;
        int activeA1, activeA2;
        int activeB1, activeB2;
        HashSet<Integer> sortedA;
        HashSet<Integer> sortedB;
        int swapsA, swapsB;
        int comparisonsA, comparisonsB;
        boolean isFinishedA;
        boolean isFinishedB;
        ArrayList<PointerLabel> labelsA;
        ArrayList<PointerLabel> labelsB;

        CompareStep(ArrayList<Integer> sA, ArrayList<Integer> sB, int aA1, int aA2, int aB1, int aB2,
                    HashSet<Integer> sDA, HashSet<Integer> sDB, int swA, int swB,
                    int cmpA, int cmpB, boolean fA, boolean fB,
                    ArrayList<PointerLabel> lblA, ArrayList<PointerLabel> lblB) {
            this.stateA = new ArrayList<>(sA);
            this.stateB = new ArrayList<>(sB);
            this.activeA1 = aA1; this.activeA2 = aA2;
            this.activeB1 = aB1; this.activeB2 = aB2;
            this.sortedA = new HashSet<>(sDA);
            this.sortedB = new HashSet<>(sDB);
            this.swapsA = swA; this.swapsB = swB;
            this.comparisonsA = cmpA; this.comparisonsB = cmpB;
            this.isFinishedA = fA;
            this.isFinishedB = fB;
            this.labelsA = lblA != null ? new ArrayList<>(lblA) : new ArrayList<>();
            this.labelsB = lblB != null ? new ArrayList<>(lblB) : new ArrayList<>();
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
    private int finalTotalComparisonsA = 0;
    private int finalTotalComparisonsB = 0;

    private long finalDurationA = 0;
    private long finalDurationB = 0;

    private AnimatedBarsView barsViewA, barsViewB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparison_screen);

        try {
            processToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 85);
        } catch (RuntimeException e) {
            processToneGenerator = null;
        }
        try {
            successToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        } catch (RuntimeException e) {
            successToneGenerator = null;
        }

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

        ViewGroup.LayoutParams containerParamsA = containerBarsA.getLayoutParams();
        containerParamsA.height = ViewGroup.LayoutParams.MATCH_PARENT;
        containerBarsA.setLayoutParams(containerParamsA);

        ViewGroup.LayoutParams containerParamsB = containerBarsB.getLayoutParams();
        containerParamsB.height = ViewGroup.LayoutParams.MATCH_PARENT;
        containerBarsB.setLayoutParams(containerParamsB);

        barsViewA = new AnimatedBarsView(this);
        containerBarsA.removeAllViews();
        containerBarsA.addView(barsViewA, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));

        barsViewB = new AnimatedBarsView(this);
        containerBarsB.removeAllViews();
        containerBarsB.addView(barsViewB, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));

        String[] targetedAlgorithms = {
                "-- Select Algorithm --",
                "Bubble Sort", "Insertion Sort", "Selection Sort",
                "Merge Sort", "Quick Sort", "Heap Sort", "Shell Sort"
        };

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, targetedAlgorithms);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerAlgoA.setAdapter(spinnerAdapter);
        spinnerAlgoB.setAdapter(spinnerAdapter);

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

    private boolean isPlaceholderSelected(Spinner spinner) {
        return spinner.getSelectedItemPosition() == 0;
    }

    private void resetComparisonUI() {
        if (raceThread != null && raceThread.isAlive()) raceThread.interrupt();

        lblAlgoA.setText(isPlaceholderSelected(spinnerAlgoA) ? "Algorithm A" : spinnerAlgoA.getSelectedItem().toString());
        lblAlgoB.setText(isPlaceholderSelected(spinnerAlgoB) ? "Algorithm B" : spinnerAlgoB.getSelectedItem().toString());
        txtSwapsA.setText("Swaps: 0   Comparisons: 0");
        txtSwapsB.setText("Swaps: 0   Comparisons: 0");
        txtTimerA.setText("0.00s");
        txtTimerB.setText("0.00s");

        finalDurationA = 0;
        finalDurationB = 0;
        finalTotalSwapsA = 0;
        finalTotalSwapsB = 0;
        finalTotalComparisonsA = 0;
        finalTotalComparisonsB = 0;

        renderBaseState(barsViewA, initialNumbers);
        renderBaseState(barsViewB, initialNumbers);
    }

    private void renderBaseState(AnimatedBarsView view, ArrayList<Integer> list) {
        view.setData(list);
    }

    private void executeTargetedRace() {
        if (isPlaceholderSelected(spinnerAlgoA) || isPlaceholderSelected(spinnerAlgoB)) {
            Toast.makeText(this, "Please select an algorithm for both A and B first.", Toast.LENGTH_SHORT).show();
            return;
        }

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
            int[] comparisonCounterA = new int[]{0};
            int[] comparisonCounterB = new int[]{0};

            ArrayList<CompareStep> algorithmASteps = generateStepsForAlgo(workingA, selectedA, true, swapCounterA, comparisonCounterA);
            ArrayList<CompareStep> algorithmBSteps = generateStepsForAlgo(workingB, selectedB, false, swapCounterB, comparisonCounterB);

            finalTotalSwapsA = swapCounterA[0];
            finalTotalSwapsB = swapCounterB[0];
            finalTotalComparisonsA = comparisonCounterA[0];
            finalTotalComparisonsB = comparisonCounterB[0];

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
                        isOverA ? finalTotalComparisonsA : stepDataA.comparisonsA,
                        isOverB ? finalTotalComparisonsB : stepDataB.comparisonsB,
                        isOverA, isOverB,
                        isOverA ? new ArrayList<>() : stepDataA.labelsA,
                        isOverB ? new ArrayList<>() : stepDataB.labelsB
                ));
            }

            long tickerStart = SystemClock.elapsedRealtime();

            // Sound sirf tab bajta hai jab koi value VAQAI teal (sorted/fixed) hoti hai -
            // ek chhota "ping" jab sirf ek/kuch values teal hon, aur ek alag/distinct
            // "success" tone jab us panel ki POORI array teal ho jati hai. Purane code
            // mein har purple/compare/swap step par sound bajta tha, jo bohat frequent
            // aur annoying tha - ab wo hata diya gaya hai.
            int prevSortedCountA = 0;
            int prevSortedCountB = 0;
            int totalN = initialNumbers.size();

            for (int t = 0; t < timelineSteps.size(); t++) {
                if (Thread.interrupted()) return;
                CompareStep activeFrame = timelineSteps.get(t);
                long currentDuration = SystemClock.elapsedRealtime() - tickerStart;

                int newSortedCountA = activeFrame.sortedA.size();
                if (newSortedCountA > prevSortedCountA) {
                    playTealSound(newSortedCountA == totalN);
                }
                prevSortedCountA = newSortedCountA;

                int newSortedCountB = activeFrame.sortedB.size();
                if (newSortedCountB > prevSortedCountB) {
                    playTealSound(newSortedCountB == totalN);
                }
                prevSortedCountB = newSortedCountB;

                runOnUiThread(() -> refreshDynamicDisplay(activeFrame, currentDuration));

                try {
                    Thread.sleep(350);
                } catch (InterruptedException e) { return; }
            }

            if (successToneGenerator != null) {
                try {
                    successToneGenerator.stopTone();
                    successToneGenerator.startTone(ToneGenerator.TONE_DTMF_D, 150);
                    SystemClock.sleep(100);
                    successToneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 200);
                } catch (Exception ignored) {}
            }

            runOnUiThread(() -> {
                long totalOpsA = (long) finalTotalSwapsA + finalTotalComparisonsA;
                long totalOpsB = (long) finalTotalSwapsB + finalTotalComparisonsB;

                String statsLine = "\nComparisons: " + finalTotalComparisonsA + " vs " + finalTotalComparisonsB +
                        "\nSwaps: " + finalTotalSwapsA + " vs " + finalTotalSwapsB +
                        "\nTime: " + String.format("%.2f", finalDurationA / 1000.0) + "s vs " +
                        String.format("%.2f", finalDurationB / 1000.0) + "s";

                String finalResult;
                String titleText;
                if (totalOpsA < totalOpsB) {
                    titleText = "🏆 " + selectedA + " Wins!";
                    finalResult = selectedA + " is more efficient." + statsLine;
                } else if (totalOpsB < totalOpsA) {
                    titleText = "🏆 " + selectedB + " Wins!";
                    finalResult = selectedB + " is more efficient." + statsLine;
                } else if (finalDurationA < finalDurationB) {
                    titleText = "🏆 " + selectedA + " Wins!";
                    finalResult = selectedA + " is faster (tied on operations)." + statsLine;
                } else if (finalDurationB < finalDurationA) {
                    titleText = "🏆 " + selectedB + " Wins!";
                    finalResult = selectedB + " is faster (tied on operations)." + statsLine;
                } else {
                    titleText = "🤝 It's a Tie!";
                    finalResult = "Both algorithms performed identically." + statsLine;
                }
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(titleText)
                        .setMessage(finalResult)
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        });

        raceThread.start();
    }

    // Ek chhota "ping" bajata hai jab sirf ek (ya kuch) value(s) teal hoti hain, aur ek
    // alag/lambi "success" tone jab us panel ki POORI array teal (mukammal sorted) ho
    // jati hai. isFullArraySorted decide karta hai konsi tone chalani hai.
    private void playTealSound(boolean isFullArraySorted) {
        if (isFullArraySorted) {
            if (successToneGenerator != null) {
                try {
                    successToneGenerator.stopTone();
                    successToneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 220);
                } catch (Exception ignored) {}
            }
        } else {
            if (processToneGenerator != null) {
                try {
                    processToneGenerator.stopTone();
                    processToneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 60);
                } catch (Exception ignored) {}
            }
        }
    }

    // ------------- Algorithm step generation -------------

    private ArrayList<CompareStep> generateStepsForAlgo(ArrayList<Integer> array, String name, boolean isPanelA, int[] counter, int[] compCounter) {
        ArrayList<CompareStep> steps = new ArrayList<>();
        HashSet<Integer> sorted = new HashSet<>();
        int n = array.size();

        if (name.equalsIgnoreCase("Bubble Sort")) {
            for (int i = 0; i < n - 1; i++) {
                for (int j = 0; j < n - i - 1; j++) {
                    compCounter[0]++;
                    if (array.get(j) > array.get(j + 1)) {
                        Collections.swap(array, j, j + 1);
                        counter[0]++;
                    }
                    steps.add(createFrame(array, j, j + 1, sorted, counter[0], compCounter[0], isPanelA,
                            new PointerLabel(n - i - 1, "i"), new PointerLabel(j, "j"), new PointerLabel(j + 1, "j+1")));
                }
                sorted.add(n - i - 1);
            }
            for (int k = 0; k < n; k++) sorted.add(k);
            steps.add(createFrame(array, -1, -1, sorted, counter[0], compCounter[0], isPanelA));
        }
        else if (name.equalsIgnoreCase("Selection Sort")) {
            for (int i = 0; i < n; i++) {
                int minIdx = i;
                for (int j = i + 1; j < n; j++) {
                    compCounter[0]++;
                    steps.add(createFrame(array, minIdx, j, sorted, counter[0], compCounter[0], isPanelA,
                            new PointerLabel(i, "i"), new PointerLabel(minIdx, "min"), new PointerLabel(j, "j")));
                    if (array.get(j) < array.get(minIdx)) minIdx = j;
                }
                if (minIdx != i) {
                    Collections.swap(array, i, minIdx);
                    counter[0]++;
                }
                sorted.add(i);
                steps.add(createFrame(array, i, minIdx, sorted, counter[0], compCounter[0], isPanelA,
                        new PointerLabel(i, "i"), new PointerLabel(minIdx, "min")));
            }
        }
        else if (name.equalsIgnoreCase("Insertion Sort")) {
            for (int i = 1; i < n; i++) {
                int key = array.get(i);
                int j = i - 1;
                while (j >= 0) {
                    compCounter[0]++;
                    if (array.get(j) > key) {
                        array.set(j + 1, array.get(j));
                        counter[0]++;
                        steps.add(createFrame(array, j, j + 1, sorted, counter[0], compCounter[0], isPanelA,
                                new PointerLabel(i, "i"), new PointerLabel(j, "j"), new PointerLabel(j + 1, "j+1")));
                        j--;
                    } else break;
                }
                array.set(j + 1, key);
                steps.add(createFrame(array, j + 1, i, sorted, counter[0], compCounter[0], isPanelA,
                        new PointerLabel(i, "i"), new PointerLabel(j + 1, "j+1")));
            }
            for (int k = 0; k < n; k++) sorted.add(k);
            steps.add(createFrame(array, -1, -1, sorted, counter[0], compCounter[0], isPanelA));
        }
        else if (name.equalsIgnoreCase("Shell Sort")) {
            for (int gap = n / 2; gap > 0; gap /= 2) {
                for (int i = gap; i < n; i++) {
                    int temp = array.get(i);
                    int j = i;
                    while (j >= gap) {
                        compCounter[0]++;
                        if (array.get(j - gap) > temp) {
                            array.set(j, array.get(j - gap));
                            counter[0]++;
                            steps.add(createFrame(array, j, j - gap, sorted, counter[0], compCounter[0], isPanelA,
                                    new PointerLabel(i, "i"), new PointerLabel(j, "j"), new PointerLabel(j - gap, "j-gap")));
                            j -= gap;
                        } else break;
                    }
                    array.set(j, temp);
                    steps.add(createFrame(array, j, -1, sorted, counter[0], compCounter[0], isPanelA,
                            new PointerLabel(i, "i"), new PointerLabel(j, "j")));
                }
            }
            for (int k = 0; k < n; k++) sorted.add(k);
            steps.add(createFrame(array, -1, -1, sorted, counter[0], compCounter[0], isPanelA));
        }
        else if (name.equalsIgnoreCase("Heap Sort")) {
            for (int i = n / 2 - 1; i >= 0; i--) {
                runHeapifySimulation(array, n, i, steps, sorted, counter, compCounter, isPanelA);
            }
            for (int i = n - 1; i > 0; i--) {
                Collections.swap(array, 0, i);
                counter[0]++;
                sorted.add(i);
                steps.add(createFrame(array, 0, i, sorted, counter[0], compCounter[0], isPanelA,
                        new PointerLabel(0, "max"), new PointerLabel(i, "i")));
                runHeapifySimulation(array, i, 0, steps, sorted, counter, compCounter, isPanelA);
            }
            for (int k = 0; k < n; k++) sorted.add(k);
            steps.add(createFrame(array, -1, -1, sorted, counter[0], compCounter[0], isPanelA));
        }
        else if (name.equalsIgnoreCase("Quick Sort")) {
            runQuickSortSimulation(array, 0, n - 1, steps, sorted, counter, compCounter, isPanelA);
            for (int k = 0; k < n; k++) sorted.add(k);
            steps.add(createFrame(array, -1, -1, sorted, counter[0], compCounter[0], isPanelA));
        }
        else if (name.equalsIgnoreCase("Merge Sort")) {
            runMergeSortSimulation(array, 0, n - 1, steps, sorted, counter, compCounter, isPanelA);
            for (int k = 0; k < n; k++) sorted.add(k);
            steps.add(createFrame(array, -1, -1, sorted, counter[0], compCounter[0], isPanelA));
        }

        return steps;
    }

    private void runHeapifySimulation(ArrayList<Integer> arr, int size, int root, ArrayList<CompareStep> steps, HashSet<Integer> sorted, int[] counter, int[] compCounter, boolean isPanelA) {
        int largest = root;
        int l = 2 * root + 1;
        int r = 2 * root + 2;

        if (l < size) {
            compCounter[0]++;
            if (arr.get(l) > arr.get(largest)) largest = l;
        }
        if (r < size) {
            compCounter[0]++;
            if (arr.get(r) > arr.get(largest)) largest = r;
        }

        if (largest != root) {
            Collections.swap(arr, root, largest);
            counter[0]++;
            steps.add(createFrame(arr, root, largest, sorted, counter[0], compCounter[0], isPanelA,
                    new PointerLabel(root, "root"), new PointerLabel(largest, "largest")));
            runHeapifySimulation(arr, size, largest, steps, sorted, counter, compCounter, isPanelA);
        }
    }

    private void runQuickSortSimulation(ArrayList<Integer> arr, int low, int high, ArrayList<CompareStep> steps, HashSet<Integer> sorted, int[] counter, int[] compCounter, boolean isPanelA) {
        if (low < high) {
            int pivot = arr.get(high);
            int i = (low - 1);
            for (int j = low; j < high; j++) {
                compCounter[0]++;
                steps.add(createFrame(arr, j, high, sorted, counter[0], compCounter[0], isPanelA,
                        new PointerLabel(low, "low"), new PointerLabel(high, "pivot"), new PointerLabel(j, "j")));
                if (arr.get(j) < pivot) {
                    i++;
                    Collections.swap(arr, i, j);
                    counter[0]++;
                    steps.add(createFrame(arr, i, j, sorted, counter[0], compCounter[0], isPanelA,
                            new PointerLabel(i, "i"), new PointerLabel(j, "j")));
                }
            }
            Collections.swap(arr, i + 1, high);
            counter[0]++;
            sorted.add(i + 1);
            steps.add(createFrame(arr, i + 1, high, sorted, counter[0], compCounter[0], isPanelA,
                    new PointerLabel(i + 1, "pivot")));

            runQuickSortSimulation(arr, low, i - 1, steps, sorted, counter, compCounter, isPanelA);
            runQuickSortSimulation(arr, i + 1, high, steps, sorted, counter, compCounter, isPanelA);
        } else if (low == high) {
            sorted.add(low);
        }
    }

    private void runMergeSortSimulation(ArrayList<Integer> arr, int l, int r, ArrayList<CompareStep> steps, HashSet<Integer> sorted, int[] counter, int[] compCounter, boolean isPanelA) {
        if (l < r) {
            int m = l + (r - l) / 2;
            runMergeSortSimulation(arr, l, m, steps, sorted, counter, compCounter, isPanelA);
            runMergeSortSimulation(arr, m + 1, r, steps, sorted, counter, compCounter, isPanelA);

            ArrayList<Integer> leftList = new ArrayList<>(arr.subList(l, m + 1));
            ArrayList<Integer> rightList = new ArrayList<>(arr.subList(m + 1, r + 1));
            int i = 0, j = 0, k = l;
            while (i < leftList.size() && j < rightList.size()) {
                compCounter[0]++;
                int fromIdx;
                if (leftList.get(i) <= rightList.get(j)) {
                    fromIdx = l + i;
                    arr.set(k, leftList.get(i));
                    i++;
                } else {
                    fromIdx = m + 1 + j;
                    arr.set(k, rightList.get(j));
                    j++;
                }
                counter[0]++;
                int safeFromIdx = (fromIdx < k) ? -1 : fromIdx;
                steps.add(createFrame(arr, k, safeFromIdx, sorted, counter[0], compCounter[0], isPanelA,
                        new PointerLabel(k, "k")));
                k++;
            }
            while (i < leftList.size()) {
                int fromIdx = l + i;
                arr.set(k, leftList.get(i));
                int safeFromIdx = (fromIdx < k) ? -1 : fromIdx;
                steps.add(createFrame(arr, k, safeFromIdx, sorted, counter[0], compCounter[0], isPanelA,
                        new PointerLabel(k, "k")));
                i++;
                k++;
            }
            while (j < rightList.size()) {
                int fromIdx = m + 1 + j;
                arr.set(k, rightList.get(j));
                int safeFromIdx = (fromIdx < k) ? -1 : fromIdx;
                steps.add(createFrame(arr, k, safeFromIdx, sorted, counter[0], compCounter[0], isPanelA,
                        new PointerLabel(k, "k")));
                j++;
                k++;
            }
        } else if (l == r) {
            // NOTE: no sorted.add(l) here either -- this single-element "subarray" can
            // still be relocated when merged with its sibling at a higher recursion level.
        }
    }

    private CompareStep createFrame(ArrayList<Integer> arr, int act1, int act2, HashSet<Integer> srtd, int ops, int compOps, boolean isPanelA, PointerLabel... labels) {
        ArrayList<PointerLabel> labelList = new ArrayList<>();
        if (labels != null) {
            for (PointerLabel p : labels) {
                if (p != null && p.index >= 0) labelList.add(p);
            }
        }
        if (isPanelA) {
            return new CompareStep(arr, initialNumbers, act1, act2, -1, -1, new HashSet<>(srtd), new HashSet<>(),
                    ops, 0, compOps, 0, false, false, labelList, new ArrayList<>());
        } else {
            return new CompareStep(initialNumbers, arr, -1, -1, act1, act2, new HashSet<>(), new HashSet<>(srtd),
                    0, ops, 0, compOps, false, false, new ArrayList<>(), labelList);
        }
    }

    // ------------- Playback / rendering -------------

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

        txtSwapsA.setText("Swaps: " + frame.swapsA + "   Comparisons: " + frame.comparisonsA);
        txtSwapsB.setText("Swaps: " + frame.swapsB + "   Comparisons: " + frame.comparisonsB);

        barsViewA.updateFrame(frame.stateA, frame.activeA1, frame.activeA2, frame.sortedA, frame.labelsA);
        barsViewB.updateFrame(frame.stateB, frame.activeB1, frame.activeB2, frame.sortedB, frame.labelsB);
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
        if (raceThread != null && raceThread.isAlive()) {
            raceThread.interrupt();
        }
    }

    private class AnimatedBarsView extends View {
        private static final int ANIM_NONE = 0;
        private static final int ANIM_SWAP = 1;
        private static final int ANIM_SHIFT = 2;

        ArrayList<Integer> data = new ArrayList<>();
        int active1 = -1, active2 = -1;
        HashSet<Integer> sortedSet = new HashSet<>();
        ArrayList<PointerLabel> labels = new ArrayList<>();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private android.animation.ValueAnimator swapAnimator;
        private int animMode = ANIM_NONE;
        private float animProgress = 1f;
        private int animIdxA = -1, animIdxB = -1;
        private int animValAtA = 0, animValAtB = 0;
        private int animFromIdx = -1, animToIdx = -1;
        private int animShiftValue = 0;

        AnimatedBarsView(Context context) {
            super(context);
            setMinimumWidth(dp(650));
        }

        int dp(int value) {
            return (int) (value * getResources().getDisplayMetrics().density);
        }

        void setData(ArrayList<Integer> values) {
            if (swapAnimator != null) swapAnimator.cancel();
            animMode = ANIM_NONE;
            animProgress = 1f;
            data = new ArrayList<>(values);
            active1 = -1;
            active2 = -1;
            sortedSet.clear();
            labels.clear();
            requestLayout();
            invalidate();
        }

        void updateFrame(ArrayList<Integer> values, int a1, int a2, HashSet<Integer> sorted, ArrayList<PointerLabel> newLabels) {
            ArrayList<Integer> oldData = data;
            active1 = a1;
            active2 = a2;
            sortedSet = new HashSet<>(sorted);
            labels = newLabels != null ? new ArrayList<>(newLabels) : new ArrayList<>();

            boolean validPair = a1 != -1 && a2 != -1 && a1 != a2
                    && oldData.size() == values.size()
                    && a1 < oldData.size() && a2 < oldData.size()
                    && a1 >= 0 && a2 >= 0;

            data = new ArrayList<>(values);

            if (swapAnimator != null) swapAnimator.cancel();
            animMode = ANIM_NONE;

            if (validPair) {
                boolean changedA = !oldData.get(a1).equals(values.get(a1));
                boolean changedB = !oldData.get(a2).equals(values.get(a2));

                if (changedA && changedB) {
                    animMode = ANIM_SWAP;
                    animIdxA = a1;
                    animIdxB = a2;
                    animValAtA = oldData.get(a1);
                    animValAtB = oldData.get(a2);
                } else if (changedB) {
                    animMode = ANIM_SHIFT;
                    animFromIdx = a1;
                    animToIdx = a2;
                    animShiftValue = oldData.get(a1);
                } else if (changedA) {
                    animMode = ANIM_SHIFT;
                    animFromIdx = a2;
                    animToIdx = a1;
                    animShiftValue = oldData.get(a2);
                }
            }

            if (animMode != ANIM_NONE) {
                animProgress = 0f;
                swapAnimator = android.animation.ValueAnimator.ofFloat(0f, 1f);
                swapAnimator.setDuration(280);
                swapAnimator.addUpdateListener(anim -> {
                    animProgress = (float) anim.getAnimatedValue();
                    invalidate();
                });
                swapAnimator.start();
            } else {
                animProgress = 1f;
                invalidate();
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = Math.max(dp(64 * Math.max(data.size(), 8)), MeasureSpec.getSize(widthMeasureSpec));
            int height = MeasureSpec.getSize(heightMeasureSpec);
            if (height <= 0) {
                height = dp(220);
            }
            setMeasuredDimension(width, height);
        }

        private float gap, barWidth, baseY, barHeight, barTop;

        private float xAt(int index) {
            return gap + index * (barWidth + gap);
        }

        private int colorForIndex(int i) {
            if (sortedSet.contains(i)) {
                return Color.rgb(0, 128, 128);
            } else if (i == active1 || i == active2) {
                return Color.rgb(128, 0, 128);
            } else {
                return Color.rgb(33, 150, 243);
            }
        }

        private void drawBar(Canvas canvas, float x, int value, int color) {
            paint.setColor(color);
            RectF shadowRect = new RectF(x + dp(3), barTop + dp(4), x + barWidth + dp(3), baseY + dp(4));
            canvas.drawRoundRect(shadowRect, dp(10), dp(10), paint);

            RectF rect = new RectF(x, barTop, x + barWidth, baseY);
            canvas.drawRoundRect(rect, dp(10), dp(10), paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(dp(15));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setFakeBoldText(true);
            canvas.drawText(String.valueOf(value), x + barWidth / 2, barTop + barHeight / 2 + dp(6), paint);
            paint.setFakeBoldText(false);
        }

        private int colorForLabel(String text) {
            if ("i".equals(text)) return Color.BLACK;
            return Color.rgb(255, 152, 0);
        }

        private void drawPointerLabels(Canvas canvas) {
            if (labels.isEmpty()) return;
            HashMap<Integer, Integer> stack = new HashMap<>();
            paint.setTextSize(dp(12));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setFakeBoldText(true);
            for (PointerLabel lbl : labels) {
                if (lbl.index < 0 || lbl.index >= data.size()) continue;
                int stackLevel = stack.getOrDefault(lbl.index, 0);
                stack.put(lbl.index, stackLevel + 1);
                float x = xAt(lbl.index) + barWidth / 2;
                float y = barTop - dp(10) - stackLevel * dp(16);
                paint.setColor(colorForLabel(lbl.text));
                canvas.drawText(lbl.text + " \u2193", x, y, paint);
            }
            paint.setFakeBoldText(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (data.size() == 0) return;

            gap = dp(12);
            barWidth = dp(50);
            float availableHeight = getHeight() - dp(60);
            baseY = getHeight() - dp(14);

            barHeight = availableHeight * 0.78f;
            barTop = baseY - barHeight;

            boolean animating = animProgress < 1f && animMode != ANIM_NONE;

            for (int i = 0; i < data.size(); i++) {
                if (animating) {
                    if (animMode == ANIM_SWAP && (i == animIdxA || i == animIdxB)) continue;
                    if (animMode == ANIM_SHIFT && i == animToIdx) continue;
                }
                drawBar(canvas, xAt(i), data.get(i), colorForIndex(i));
            }

            if (animating) {
                if (animMode == ANIM_SWAP) {
                    float xA = lerp(xAt(animIdxA), xAt(animIdxB), animProgress);
                    float xB = lerp(xAt(animIdxB), xAt(animIdxA), animProgress);
                    drawBar(canvas, xA, animValAtA, Color.rgb(128, 0, 128));
                    drawBar(canvas, xB, animValAtB, Color.rgb(128, 0, 128));
                } else if (animMode == ANIM_SHIFT) {
                    float x = lerp(xAt(animFromIdx), xAt(animToIdx), animProgress);
                    drawBar(canvas, x, animShiftValue, Color.rgb(128, 0, 128));
                }
            }

            drawPointerLabels(canvas);
        }

        private float lerp(float from, float to, float t) {
            return from + (to - from) * t;
        }
    }
}