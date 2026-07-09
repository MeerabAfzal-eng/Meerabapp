package com.example.meerabapp;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Locale;
import android.util.Log;
import android.media.AudioManager;
import android.media.ToneGenerator;


public class VisualizationActivity extends AppCompatActivity {
    TextView txtAlgoName, txtSwapCounter, txtTimer, txtComplexity, txtExplanation;
    LinearLayout visualContainer;
    Button btnAsc, btnDesc, btnRestart, btnReset;
    AnimatedBarsView barsView;
    ArrayList<Integer> arr = new ArrayList<>();
    ArrayList<Integer> originalArr = new ArrayList<>();
    ArrayList<Step> steps = new ArrayList<>();
    Handler handler = new Handler(Looper.getMainLooper());
    ToneGenerator toneGen;
    int stepIndex = 0;
    int swaps = 0;
    boolean ascending = true;
    long startTime;
    String algorithm = "Bubble Sort";
    static final int STEP_DELAY = 400;

    static class Step {
        String type, message;
        int i, j, value, level;

        String[] labels;
        int[] labelIdx;

        Step(String type, int i, int j, int value, String message) {
            this.type = type;
            this.i = i;
            this.j = j;
            this.value = value;
            this.message = message;
        }

        Step withPointers(String[] labels, int[] labelIdx) {
            this.labels = labels;
            this.labelIdx = labelIdx;
            return this;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualization);

        try {
            toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        } catch (RuntimeException e) {
            toneGen = null;
        }

        try {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager != null) {
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
            }
        } catch (Exception ignored) {}

        txtAlgoName = findViewById(R.id.txtAlgoName);
        txtSwapCounter = findViewById(R.id.txtSwapCounter);
        txtTimer = findViewById(R.id.txtTimer);
        txtComplexity = findViewById(R.id.txtComplexity);
        txtExplanation = findViewById(R.id.txtExplanation);
        visualContainer = findViewById(R.id.visualContainer);
        btnAsc = findViewById(R.id.btnAsc);
        btnDesc = findViewById(R.id.btnDesc);
        btnRestart = findViewById(R.id.btnRestart);
        btnReset = findViewById(R.id.btnReset);
        String receivedAlgo = getIntent().getStringExtra("algorithm");
        if (receivedAlgo != null) {
            algorithm = receivedAlgo;
        }
        ArrayList<Integer> receivedValues = getIntent().getIntegerArrayListExtra("numbers");
        if (receivedValues != null && receivedValues.size() > 0) {
            originalArr.addAll(receivedValues);
        } else {
            addDefaultValues();
        }

        visualContainer.removeAllViews();

        barsView = new AnimatedBarsView(this);
        visualContainer.addView(
                barsView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                )
        );

        resetArray();
        btnAsc.setOnClickListener(v -> {
            ascending = true;
            startSorting();
        });
        btnDesc.setOnClickListener(v -> {
            ascending = false;
            startSorting();
        });
        btnRestart.setOnClickListener(v -> {
            handler.removeCallbacksAndMessages(null);
            resetArray();
            startSorting();
        });
        btnReset.setOnClickListener(v -> {
            handler.removeCallbacksAndMessages(null);
            finish();
        });
    }

    void addDefaultValues() {
        originalArr.add(45);
        originalArr.add(20);
        originalArr.add(70);
        originalArr.add(10);
        originalArr.add(90);
        originalArr.add(35);
        originalArr.add(60);
        originalArr.add(15);
        originalArr.add(80);
        originalArr.add(50);
    }

    void resetArray() {
        arr.clear();
        arr.addAll(originalArr);
        steps.clear();
        stepIndex = 0;
        swaps = 0;

        txtSwapCounter.setText(algorithm.equals("Merge Sort") ? "Merges: 0" : "Swaps: 0");
        txtTimer.setText("00:00");
        txtExplanation.setText("Sorting steps will appear here...");

        updateHeader();

        if (barsView != null) {
            barsView.sortedStatus = new boolean[arr.size()];
            barsView.sortedMode = false;
            barsView.invalidate();
        }
        barsView.setData(arr);
    }

    void updateHeader() {
        txtAlgoName.setText("Algorithm: " + algorithm);
        txtExplanation.setText(getExplanation(algorithm));

        if (algorithm.equals("Quick Sort")) {
            txtComplexity.setText("Avg O(n log n)");

        } else if (algorithm.equals("Merge Sort") || algorithm.equals("Heap Sort") || algorithm.equals("Shell Sort")) {
            txtComplexity.setText("O(n log n)");
        } else {
            txtComplexity.setText("O(n²)");
        }
    }

    String getExplanation(String algo) {
        switch (algo) {
            case "Bubble Sort":
                return "Bubble Sort: This algorithm repeatedly steps through the list, compares adjacent elements, and swaps them if they are in the wrong order.";
            case "Merge Sort":
                return "Merge Sort: A divide-and-conquer algorithm that divides the array into smaller sub-arrays, sorts them, and then merges them back together.";
            case "Insertion Sort":
                return "Insertion Sort: Builds the final sorted array one item at a time by taking elements from the unsorted part and inserting them into their correct position.";
            case "Selection Sort":
                return "Selection Sort: Repeatedly finds the minimum element from the unsorted part and puts it at the beginning of the list.";
            case "Quick Sort":
                return "Quick Sort: A highly efficient divide-and-conquer algorithm that picks a 'pivot' element and partitions the array around it.";
            case "Heap Sort":
                return "Heap Sort: A comparison-based sorting technique based on a Binary Heap data structure. It builds a heap and then repeatedly extracts the maximum/minimum element.";
            case "Shell Sort":
                return "Shell Sort: An optimization of Insertion Sort that allows the exchange of items that are far apart, gradually reducing the gap between them.";
            default:
                return "Algorithm selected: " + algo + ". Click 'Ascending' or 'Descending' to start the visualization.";
        }
    }

    void startSorting() {
        Log.d("ORDER_DEBUG", "Sorting in Ascending mode: " + ascending);
        handler.removeCallbacksAndMessages(null);
        arr.clear();
        arr.addAll(originalArr);
        steps.clear();
        stepIndex = 0;
        swaps = 0;

        txtExplanation.setText(getExplanation(algorithm) + "\n\n--- Sorting Steps ---");
        startTime = System.currentTimeMillis();
        txtSwapCounter.setText(algorithm.equals("Merge Sort") ? "Merges: 0" : "Swaps: 0");
        txtTimer.setText("00:00");

        barsView.setData(arr);
        barsView.setPointers(null, null);
        int[] copy = toIntArray(arr);
        if (algorithm.equals("Bubble Sort")) bubbleSort(copy);
        else if (algorithm.equals("Merge Sort")) mergeSort(copy, 0, copy.length - 1);
        else if (algorithm.equals("Insertion Sort")) insertionSort(copy);
        else if (algorithm.equals("Selection Sort")) selectionSort(copy);
        else if (algorithm.equals("Quick Sort")) quickSort(copy, 0, copy.length - 1);
        else if (algorithm.equals("Heap Sort")) heapSort(copy);
        else if (algorithm.equals("Shell Sort")) shellSort(copy);

        Log.d("DEBUG_SORT", "Steps generated: " + steps.size());
        playStep();

    }


    void playStep() {
        updateTimer();
        if (stepIndex >= steps.size()) {
            barsView.setPointers(null, null);
            barsView.showSorted();
            playSuccessChime();
            appendExplanationLog("Sorting completed.");
            return;
        }

        Step step = steps.get(stepIndex++);
        Log.d("SORT_DEBUG", "Processing step: " + step.type);

        if (step.labels != null) {
            barsView.setPointers(step.labels, step.labelIdx);
        }

        if (step.type.equals("compare")) {
            barsView.animateCompare(step.i, step.j, () -> {
                appendExplanationLog(step.message);
                handler.postDelayed(this::playStep, STEP_DELAY);
            });
        } else if (step.type.equals("swap")) {
            barsView.animateSwap(step.i, step.j, () -> {
                int temp = arr.get(step.i);
                arr.set(step.i, arr.get(step.j));
                arr.set(step.j, temp);
                swaps++;
                txtSwapCounter.setText("Swaps: " + swaps);
                appendExplanationLog(step.message);
                handler.postDelayed(this::playStep, STEP_DELAY);
            });
        } else if (step.type.equals("set")) {
            barsView.animateWrite(step.i, step.value, () -> {
                arr.set(step.i, step.value);
                barsView.updateData(step.i, step.value);
                appendExplanationLog(step.message);
                handler.postDelayed(this::playStep, STEP_DELAY);
            });

        } else if (step.type.equals("mergeWrite")) {
            barsView.animateMergeSlide(step.j, step.i, step.value, () -> {
                arr.set(step.i, step.value);
                barsView.updateData(step.i, step.value);
                swaps++;
                txtSwapCounter.setText("Merges: " + swaps);
                appendExplanationLog(step.message);
                handler.postDelayed(this::playStep, STEP_DELAY);
            });

        } else if (step.type.equals("shift")) {
            barsView.animateShift(step.i, step.j, () -> {
                int val = arr.get(step.i);
                arr.set(step.j, val);
                swaps++;
                txtSwapCounter.setText("Swaps:" + swaps);

                if (barsView.sortedStatus != null) {
                    boolean tempStatus = barsView.sortedStatus[step.i];
                    barsView.sortedStatus[step.j] = tempStatus;
                    barsView.sortedStatus[step.i] = false;
                }

                barsView.updateData(step.j, val);
                appendExplanationLog(step.message);
                handler.postDelayed(this::playStep, STEP_DELAY);
            });
        } else if (step.type.equals("insert")) {
            barsView.animateWrite(step.i, step.value, () -> {
                arr.set(step.i, step.value);
                barsView.updateData(step.i, step.value);
                appendExplanationLog(step.message);
                handler.postDelayed(this::playStep, STEP_DELAY);
            });
        } else if (step.type.equals("mark")) {
            barsView.markSorted(step.i);
            if (toneGen != null) {
                try {
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 80);
                } catch (Exception ignored) {}
            }
            // FIX: pehle ye gap sirf 100ms tha - itni tez ke jab poori array ek saath
            // (Insertion/Shell/Quick/Merge/Heap Sort ke end mein) teal hoti thi to "ek
            // dum" jesi lagti thi. Ab har bar ke teal hone ke darmiyan zyada visible
            // gap hai, taake ek left-se-right "wave" jesi dikhe, na ke instant flash.
            handler.postDelayed(this::playStep, 220);
        }
    }


    int[] toIntArray(ArrayList<Integer> list) {
        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);

        }
        return result;

    }

    int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    void playSuccessChime() {
        if (toneGen == null) return;
        try {
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 180);
        } catch (Exception ignored) {}
        handler.postDelayed(() -> {
            if (toneGen == null) return;
            try {
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 180);
            } catch (Exception ignored) {}
        }, 250);
        handler.postDelayed(() -> {
            if (toneGen == null) return;
            try {
                toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 450);
            } catch (Exception ignored) {}
        }, 550);
    }

    void updateTimer() {
        long seconds = (System.currentTimeMillis() - startTime) / 1000;
        txtTimer.setText(String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60));
    }

    void appendExplanationLog(String text) {
        txtExplanation.append("\n" + text);
        txtExplanation.post(() -> {
            if (txtExplanation.getLayout() == null) return;
            int scrollAmount = txtExplanation.getLayout().getLineTop(txtExplanation.getLineCount())
                    - txtExplanation.getHeight();
            txtExplanation.scrollTo(0, Math.max(scrollAmount, 0));
        });
    }


    boolean wrongOrder(int a, int b) {
        return ascending ? (a > b) : (a < b);
    }

    boolean correctOrEqual(int a, int b) {
        return ascending ? (a <= b) : (a >= b);
    }


    void swap(int[] a, int i, int j) {
        if (i == j) return;
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    void bubbleSort(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            for (int j = 0; j < a.length - i - 1; j++) {
                steps.add(new Step("compare", j, j + 1, 0,
                        "comparing " + a[j] + " and " + a[j + 1] + ".")
                        .withPointers(new String[]{"i", "j", "j+1"}, new int[]{i, j, j + 1}));
                if (wrongOrder(a[j], a[j + 1])) {
                    steps.add(new Step("swap", j, j + 1, 0,
                            "Items " + a[j] + " and " + a[j + 1] + " because they are out of order. Swapping them.")
                            .withPointers(new String[]{"i", "j", "j+1"}, new int[]{i, j, j + 1}));
                    swap(a, j, j + 1);
                }
            }
            steps.add(new Step("mark", a.length - 1 - i, -1, 0, "Fixed position"));
        }
        steps.add(new Step("mark", 0, -1, 0, " Array Sorted!"));

    }


    void insertionSort(int[] a) {
        for (int i = 1; i < a.length; i++) {
            int key = a[i];
            int j = i - 1;

            while (j >= 0 && (ascending ? a[j] > key : a[j] < key)) {
                steps.add(new Step("shift", j, j + 1, a[j], "Shifting " + a[j] + " to  the right")
                        .withPointers(new String[]{"i", "j", "j+1"}, new int[]{i, j, j + 1}));
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
            steps.add(new Step("insert", j + 1, -1, key, "Inserting " + key + " into its correct sorted position.")
                    .withPointers(new String[]{"i", "j+1"}, new int[]{i, j + 1}));
        }

        // FIX: insertion sort ke ek element ki "final" jagah tab tak guaranteed nahi hoti
        // jab tak POORA array process na ho jaye - kyunke koi bhi baad ki insertion is
        // element ko aage khiska sakti hai (uski relative order sahi rehti hai, lekin
        // uska INDEX badal sakta hai). Isi liye ab hum har pass ke baad 0..i ko turant
        // teal nahi karte (pehle ye "ek shift ke baad hi teal ho jana" wala bug tha) -
        // balke poori sorting mukammal hone ke baad, ek hi baar mein saari array ko
        // teal karte hain - jaisa Shell/Quick/Merge Sort mein already hota hai.
        for (int k = 0; k < a.length; k++) {
            steps.add(new Step("mark", k, -1, 0, "Sorted!"));
        }
    }

    void selectionSort(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            int selected = i;
            for (int j = i + 1; j < a.length; j++) {
                steps.add(new Step("compare", selected, j, 0, "Searching for the  minimum :comparing")
                        .withPointers(new String[]{"i", "min", "j"}, new int[]{i, selected, j}));
                if (wrongOrder(a[selected], a[j])) selected = j;
            }
            if (selected != i) {
                steps.add(new Step("swap", i, selected, 0, "Swaping" + " with the smallest element " + a[selected])
                        .withPointers(new String[]{"i", "min"}, new int[]{i, selected}));
                swap(a, i, selected);
            }
            steps.add(new Step("mark", i, -1, 0, "Fixed position"));
        }
        steps.add(new Step("mark", a.length - 1, -1, 0, "Sorted!"));
    }

    void quickSort(int[] a, int low, int high) {
        if (low < high) {
            int pivot = partition(a, low, high);
            quickSort(a, low, pivot - 1);
            quickSort(a, pivot + 1, high);
        }
        if (low == 0 && high == a.length - 1) {
            for (int k = 0; k < a.length; k++) {
                steps.add(new Step("mark", k, -1, 0, "Sorted!"));
            }
        }
    }

    int partition(int[] a, int low, int high) {
        int pivotValue = a[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            steps.add(new Step("compare", j, high, 0, "Compareing element at index " + j + " with pivot " + a[high])
                    .withPointers(new String[]{"low", "pivot", "i", "j"}, new int[]{low, high, i, j}));
            boolean move = ascending ? a[j] < pivotValue : a[j] > pivotValue;
            if (move) {
                i++;
                steps.add(new Step("swap", i, j, 0, "Moving smaller element " + a[j] + " to the left side of the pivot.")
                        .withPointers(new String[]{"low", "pivot", "i", "j"}, new int[]{low, high, i, j}));
                swap(a, i, j);
            }
        }
        steps.add(new Step("swap", i + 1, high, 0, "Placing pivot " + a[high] + " in its correct partition position.")
                .withPointers(new String[]{"low", "pivot", "i"}, new int[]{low, high, i + 1}));
        swap(a, i + 1, high);
        return i + 1;

    }

    void mergeSort(int[] a, int left, int right) {
        if (left >= right) return;
        int mid = (left + right) / 2;
        mergeSort(a, left, mid);
        mergeSort(a, mid + 1, right);
        merge(a, left, mid, right);
    }

    void merge(int[] a, int left, int mid, int right) {
        int leftLen = mid - left + 1;
        int rightLen = right - mid;

        int[] leftArr = new int[leftLen];
        int[] rightArr = new int[rightLen];
        for (int x = 0; x < leftLen; x++) leftArr[x] = a[left + x];
        for (int x = 0; x < rightLen; x++) rightArr[x] = a[mid + 1 + x];

        int i = 0, j = 0, k = left;

        while (i < leftLen && j < rightLen) {
            steps.add(new Step("compare", left + i, mid + 1 + j, 0,
                    "Comparing " + leftArr[i] + " and " + rightArr[j] + " to merge in order.")
                    .withPointers(new String[]{"k"}, new int[]{k}));
            int sourceIndex;
            if (correctOrEqual(leftArr[i], rightArr[j])) {
                a[k] = leftArr[i];
                sourceIndex = left + i;
                i++;
            } else {
                a[k] = rightArr[j];
                sourceIndex = mid + 1 + j;
                j++;
            }
            steps.add(new Step("mergeWrite", k, sourceIndex, a[k], "Placing " + a[k] + " into the merged result.")
                    .withPointers(new String[]{"k"}, new int[]{k}));
            k++;
        }
        while (i < leftLen) {
            int sourceIndex = left + i;
            a[k] = leftArr[i++];
            steps.add(new Step("mergeWrite", k, sourceIndex, a[k], "Copying remaining " + a[k] + " into the merged result.")
                    .withPointers(new String[]{"k"}, new int[]{k}));
            k++;
        }
        while (j < rightLen) {
            int sourceIndex = mid + 1 + j;
            a[k] = rightArr[j++];
            steps.add(new Step("mergeWrite", k, sourceIndex, a[k], "Copying remaining " + a[k] + " into the merged result.")
                    .withPointers(new String[]{"k"}, new int[]{k}));
            k++;
        }

        if (left == 0 && right == arr.size() - 1) {
            for (int idx = left; idx <= right; idx++) {
                steps.add(new Step("mark", idx, -1, 0, "Sorted!"));
            }
        }
    }

    void heapSort(int[] a) {
        int n = a.length;
        for (int i = n / 2 - 1; i >= 0; i--) heapify(a, n, i, "root");
        for (int i = n - 1; i > 0; i--) {
            steps.add(new Step("swap", 0, i, 0, "Swap root")
                    .withPointers(new String[]{"max", "i"}, new int[]{0, i}));
            swap(a, 0, i);
            steps.add(new Step("mark", i, -1, 0, "Fixed position"));
            heapify(a, i, 0, "max");
        }
        steps.add(new Step("mark", 0, -1, 0, "Sorted!"));
    }

    void heapify(int[] a, int n, int root, String rootLabel) {
        int best = root;
        int left = 2 * root + 1;
        int right = 2 * root + 2;
        if (left < n) {
            steps.add(new Step("compare", left, best, 0,
                    "Compare left child with root")
                    .withPointers(new String[]{rootLabel, "largest"}, new int[]{root, best}));
            boolean shouldSwap = ascending ? (a[left] > a[best]) : (a[left] < a[best]);
            if (shouldSwap) {
                best = left;
            }
        }
        if (right < n) {
            steps.add(new Step("compare", right, best, 0,
                    "Compare right child with parent to maintain heap property. ")
                    .withPointers(new String[]{rootLabel, "largest"}, new int[]{root, best}));
            boolean shouldSwap = ascending ? (a[right] > a[best]) : (a[right] < a[best]);
            if (shouldSwap) {
                best = right;
            }
        }
        if (best != root) {
            steps.add(new Step("swap", root, best, 0,
                    "Swapping to maintain max_heap sructure.")
                    .withPointers(new String[]{rootLabel, "largest"}, new int[]{root, best}));
            swap(a, root, best);
            heapify(a, n, best, rootLabel);
        }
    }

    void shellSort(int[] a) {
        for (int gap = a.length / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < a.length; i++) {
                int temp = a[i];
                int j = i;
                while (j >= gap && (ascending ? a[j - gap] > temp : a[j - gap] < temp)) {
                    steps.add(new Step("compare", j - gap, j, 0, "Comparing elements with a gap of" + gap)
                            .withPointers(new String[]{"i", "j", "j-gap"}, new int[]{i, j, j - gap}));

                    steps.add(new Step("shift", j - gap, j, a[j - gap], "Shifting element " + a[j - gap] + " to fill the gap.")
                            .withPointers(new String[]{"i", "j", "j-gap"}, new int[]{i, j, j - gap}));

                    a[j] = a[j - gap];
                    j -= gap;
                }
                steps.add(new Step("set", j, -1, temp,
                        "Placing element " + temp + " after gap comparison")
                        .withPointers(new String[]{"i", "j"}, new int[]{i, j}));
                a[j] = temp;
            }
        }
        for (int i = 0; i < a.length; i++) {
            steps.add(new Step("mark", i, -1, 0, "Sorted position"));
        }
    }


    public class AnimatedBarsView extends android.view.View {
        ArrayList<Integer> data = new ArrayList<>();
        boolean[] isSorted;

        boolean[] sortedStatus;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int compareA = -1;
        int compareB = -1;
        int writeIndex = -1;
        int writeValue = -1;
        boolean sortedMode = false;
        float compareProgress = 0f;
        float swapProgress = 0f;
        int swapA = -1;
        int swapB = -1;
        float swapLift = 0f;
        int moveFrom = -1;
        int moveTo = -1;
        float moveProgress = 0f;
        int[] barLevels;

        String[] activeLabels = new String[0];
        int[] activeLabelIdx = new int[0];
        Paint pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        float[] barTops;

        void setPointers(String[] labels, int[] idx) {
            this.activeLabels = labels != null ? labels : new String[0];
            this.activeLabelIdx = idx != null ? idx : new int[0];
            invalidate();
        }

        void markSorted(int index) {
            if (sortedStatus != null && index >= 0 && index < sortedStatus.length) {
                sortedStatus[index] = true;
                invalidate();
            }
        }

        int unsortedColor = Color.rgb(33, 150, 243); // Blue
        int compareColor = Color.rgb(128, 0, 128);   // Purple
        int sortedColor = Color.rgb(0, 128, 128);    // Teal


        public AnimatedBarsView(android.content.Context context) {
            super(context);
            setMinimumWidth(dp(650));
        }

        float smoothMove(float t) {
            return (float) (t * t * (3 - 2 * t));
        }


        void setData(ArrayList<Integer> values) {
            data.clear();
            data.addAll(values);
            this.barLevels = new int[values.size()];
            this.barTops = new float[values.size()];

            this.sortedStatus = new boolean[values.size()];
            compareA = -1;
            compareB = -1;
            writeIndex = -1;
            writeValue = -1;
            swapA = -1;
            swapB = -1;
            compareProgress = 0f;
            swapProgress = 0f;
            sortedMode = false;
            requestLayout();
            invalidate();
        }

        void showSorted() {
            sortedMode = true;
            compareA = -1;
            compareB = -1;
            writeIndex = -1;
            invalidate();
        }

        public void updateData(int index, int value) {
            if (index >= 0 && index < data.size()) {
                data.set(index, value);
                invalidate();
            }
        }

        public void updateLevel(int start, int end, int level) {
            for (int i = start; i <= end; i++) {
                if (i < barLevels.length) {
                    barLevels[i] = level;
                }
            }
            invalidate();
        }

        public void animateBarMove(int from, int to, int value) {
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(500);

            animator.addUpdateListener(animation -> {
                float progress = (float) animation.getAnimatedValue();
                this.moveProgress = progress;
                this.moveFrom = from;
                this.moveTo = to;
                invalidate();
            });

            animator.start();
        }

        void animateCompare(int i, int j, Runnable action) {
            compareA = i;
            compareB = j;
            writeIndex = -1;
            sortedMode = false;
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(220);
            animator.addUpdateListener(a -> {
                compareProgress = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    compareProgress = 0f;
                    compareA = -1;
                    compareB = -1;
                    invalidate();
                    action.run();
                }
            });
            animator.start();
        }

        void animateSwap(int i, int j, Runnable action) {

            swapA = i;
            swapB = j;
            compareA = i;
            compareB = j;
            writeIndex = -1;
            sortedMode = false;
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(650);
            animator.addUpdateListener(a -> {
                swapProgress = (float) a.getAnimatedValue();
                swapLift = (float) Math.sin(swapProgress * Math.PI);
                invalidate();
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    int temp = data.get(i);
                    data.set(i, data.get(j));
                    data.set(j, temp);
                    swapA = -1;
                    swapB = -1;
                    compareA = -1;
                    compareB = -1;
                    swapProgress = 0f;
                    swapLift = 0f;
                    invalidate();
                    action.run();
                }

            });
            animator.start();
        }


        void animateMove(int fromIndex, int toIndex, Runnable action) {
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(350);
            animator.addUpdateListener(a -> {
                invalidate();
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    moveFrom = -1;
                    moveTo = -1;
                    moveProgress = 0f;
                    action.run();
                }
            });
            animator.start();
        }

        int shiftFrom = -1;
        int shiftTo = -1;

        void animateShift(int fromIndex, int toIndex, Runnable action) {
            shiftFrom = fromIndex;
            shiftTo = toIndex;
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(850);
            animator.addUpdateListener(a -> {
                swapProgress = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    shiftFrom = -1;
                    shiftTo = -1;
                    swapProgress = 0f;
                    action.run();
                }
            });
            animator.start();
        }

        void animateWrite(int index, int value, Runnable action) {
            writeIndex = index;
            writeValue = value;
            compareA = -1;
            compareB = -1;
            sortedMode = false;
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(650);
            animator.addUpdateListener(a -> invalidate());
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    writeIndex = -1;
                    writeValue = -1;
                    invalidate();
                    if (action != null) action.run();
                }
            });
            animator.start();
        }

        void animateMergeSlide(int fromIndex, int toIndex, int value, Runnable action) {
            shiftFrom = fromIndex;
            shiftTo = toIndex;
            writeIndex = toIndex;
            writeValue = value;
            compareA = -1;
            compareB = -1;
            sortedMode = false;
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(550);
            animator.addUpdateListener(a -> {
                swapProgress = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    shiftFrom = -1;
                    shiftTo = -1;
                    swapProgress = 0f;
                    writeIndex = -1;
                    writeValue = -1;
                    invalidate();
                    if (action != null) action.run();
                }
            });
            animator.start();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = Math.max(dp(58 * Math.max(data.size(), 8)), MeasureSpec.getSize(widthMeasureSpec));
            int height = MeasureSpec.getSize(heightMeasureSpec);
            setMeasuredDimension(width, height);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (data.size() == 0) return;

            float gap = dp(10);
            float barWidth = dp(42);

            float fixedBarHeight = dp(65);
            float baseY = getHeight() - dp(18);

            if (barTops == null || barTops.length != data.size()) {
                barTops = new float[data.size()];
            }

            for (int i = 0; i < data.size(); i++) {
                int currentLevel = barLevels != null && i < barLevels.length ? barLevels[i] : 0;
                int shownValue = data.get(i);
                if (i == writeIndex && writeValue > 0) {
                    shownValue = writeValue;
                }
                float barHeight = fixedBarHeight;

                int color;

                if (sortedStatus != null && i < sortedStatus.length && sortedStatus[i]) {
                    color = sortedColor;
                }
                else if (i == compareA || i == compareB || i == shiftFrom || i == shiftTo || i == writeIndex) {
                    color = compareColor;
                }
                else {
                    color = unsortedColor;
                }

                float verticalShift = currentLevel * dp(60);
                float currentBaseY = baseY - verticalShift;
                float x = gap + i * (barWidth + gap);
                float top = currentBaseY - barHeight;

                if (i == swapA || i == swapB) {
                    float targetIndex = (i == swapA) ? swapB : swapA;
                    x = gap + i * (barWidth + gap);
                    float targetX = gap + targetIndex * (barWidth + gap);
                    x = x + (targetX - x) * smoothMove(swapProgress);
                    if (i == swapA) top -= dp(45) * swapLift;
                    else top += dp(22) * swapLift;
                } else if (i == shiftFrom) {
                    float startX = gap + shiftFrom * (barWidth + gap);
                    float targetX = gap + shiftTo * (barWidth + gap);
                    x = startX + (targetX - startX) * smoothMove(swapProgress);
                } else if (i == moveFrom) {
                    float startX = gap + moveFrom * (barWidth + gap);
                    float targetX = gap + moveTo * (barWidth + gap);
                    x = startX + (targetX - startX) * smoothMove(moveProgress);
                    top -= dp(30);
                }

                barTops[i] = top;

                paint.setColor(color);
                RectF shadowRect = new RectF(x + dp(3), top + dp(4), x + barWidth + dp(3), baseY + dp(4));
                canvas.drawRoundRect(shadowRect, dp(8), dp(8), paint);

                RectF rect = new RectF(x, top, x + barWidth, currentBaseY);
                canvas.drawRoundRect(rect, dp(8), dp(8), paint);

                paint.setColor(Color.WHITE);
                paint.setTextSize(dp(13));
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setFakeBoldText(true);
                canvas.drawText(String.valueOf(shownValue), x + barWidth / 2, top + barHeight / 2 + dp(5), paint);
                paint.setFakeBoldText(false);

            }

            drawPointers(canvas, gap, barWidth);
        }

        void drawPointers(Canvas canvas, float gap, float barWidth) {
            if (activeLabels == null || activeLabels.length == 0 || barTops == null) return;

            java.util.LinkedHashMap<Integer, java.util.ArrayList<String>> byIndex = new java.util.LinkedHashMap<>();
            for (int n = 0; n < activeLabels.length; n++) {
                if (activeLabelIdx == null || n >= activeLabelIdx.length) continue;
                int idx = activeLabelIdx[n];
                if (idx < 0 || idx >= data.size()) continue;
                java.util.ArrayList<String> list = byIndex.get(idx);
                if (list == null) {
                    list = new java.util.ArrayList<>();
                    byIndex.put(idx, list);
                }
                list.add(activeLabels[n]);
            }

            pointerPaint.setTextAlign(Paint.Align.CENTER);
            pointerPaint.setTextSize(dp(13));
            pointerPaint.setFakeBoldText(true);

            float labelGap = dp(6);
            float rowSpacing = dp(18);

            for (java.util.Map.Entry<Integer, java.util.ArrayList<String>> entry : byIndex.entrySet()) {
                int idx = entry.getKey();
                java.util.ArrayList<String> labelsHere = entry.getValue();
                float centerX = gap + idx * (barWidth + gap) + barWidth / 2f;
                float barTop = idx < barTops.length ? barTops[idx] : dp(24);

                for (int r = 0; r < labelsHere.size(); r++) {
                    String label = labelsHere.get(r);
                    float y = barTop - labelGap - (labelsHere.size() - 1 - r) * rowSpacing;
                    pointerPaint.setColor(label.equals("i") ? Color.BLACK : Color.rgb(255, 152, 0));
                    canvas.drawText(label + " \u2193", centerX, y, pointerPaint);
                }
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (toneGen != null) {
            toneGen.release();
            toneGen = null;
        }

        handler.removeCallbacksAndMessages(null);
    }
}