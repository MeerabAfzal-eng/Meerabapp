
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
    static final int STEP_DELAY = 450;

    static class Step {
        String type, message;
        int i, j, value, level;

        Step(String type, int i, int j, int value, String message) {
            this.type = type;
            this.i = i;
            this.j = j;
            this.value = value;
            this.level = level;
            this.message = message;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualization);
        toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
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
        barsView = new AnimatedBarsView(this);
        visualContainer.removeAllViews();
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
        // Yahan fix: Sorted status ko naye sire se clear karein

        if (barsView != null) {

            barsView.sortedStatus = new boolean[arr.size()];

            barsView.sortedMode = false;

            barsView.invalidate();

        }
        txtSwapCounter.setText("Swaps: 0");
        txtTimer.setText("00:00");
        txtExplanation.setText("Sorting steps will appear here...");

        updateHeader();
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
        txtSwapCounter.setText("Swaps: 0");
        txtTimer.setText("00:00");

        barsView.setData(arr);
        int[] copy = toIntArray(arr);
        if (algorithm.equals("Bubble Sort")) bubbleSort(copy);
        else if (algorithm.equals("Insertion Sort")) insertionSort(copy);
        else if (algorithm.equals("Selection Sort")) selectionSort(copy);
        else if (algorithm.equals("Quick Sort")) quickSort(copy, 0, copy.length - 1);
        else if (algorithm.equals("Merge Sort")) {
            mergeSort(copy, 0, copy.length - 1, 0); // 0 initial level hai
        }

        else if (algorithm.equals("Heap Sort")) heapSort(copy);
        else if (algorithm.equals("Shell Sort")) shellSort(copy);
        // Yahan check karein
        Log.d("DEBUG_SORT", "Steps generated: " + steps.size());
        playStep();

    }


    void playStep() {
        updateTimer();
        if (stepIndex >= steps.size()) {
            barsView.showSorted();
            if (toneGen != null) {
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 500);
            }
            txtExplanation.append("\nSorting completed.");
            return;
        }

        Step step = steps.get(stepIndex++);
        Log.d("SORT_DEBUG", "Processing step: " + step.type + " at Index: " + step.i + " Value: " + step.value);

        if (step.type.equals("compare")) {
            barsView.animateCompare(step.i, step.j, () -> {
                txtExplanation.append("\n" + step.message);
                handler.postDelayed(this::playStep, STEP_DELAY);
            });
        } else if (step.type.equals("swap")) {
            barsView.animateSwap(step.i, step.j, () -> {
                int temp = arr.get(step.i);
                arr.set(step.i, arr.get(step.j));
                arr.set(step.j, temp);
                swaps++;
                txtSwapCounter.setText("Swaps: " + swaps);
                txtExplanation.append("\n" + step.message);
                handler.postDelayed(this::playStep, STEP_DELAY);
            });
        } else if (step.type.equals("set")) {
            barsView.animateWrite(step.i, step.value, () -> {
                arr.set(step.i, step.value);
                barsView.updateData(step.i, step.value); // UI refresh (smooth)
                txtExplanation.append("\n" + step.message); // Explanation update
                handler.postDelayed(this::playStep, STEP_DELAY);
            });

        } else if (step.type.equals("shift")) {
            barsView.animateShift(step.i, step.j, () -> {
                int val = arr.get(step.i);
                arr.set(step.j, val);
                swaps++;
                txtSwapCounter.setText("Swaps:" + swaps);

                // Status ko shift karein
                if (barsView.sortedStatus != null) {
                    boolean tempStatus = barsView.sortedStatus[step.i];
                    barsView.sortedStatus[step.j] = tempStatus;
                    barsView.sortedStatus[step.i] = false;
                }

                barsView.updateData(step.j, val);
                txtExplanation.append("\n" + step.message);
                handler.postDelayed(this::playStep, STEP_DELAY);
            });
        }
        else if (step.type.equals("insert")) {
            barsView.animateWrite(step.i, step.value, () -> {
                arr.set(step.i, step.value);
                barsView.setData(arr);

                // Insert hone ke baad index ko true mark karne ke liye markSorted step aage aayega
                txtExplanation.append("\n" + step.message);
                handler.postDelayed(this::playStep, STEP_DELAY);
            });
        } else if (step.type.equals("mark")) {
            barsView.markSorted(step.i);
            toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 100);
            playStep();
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

    void updateTimer() {
        long seconds = (System.currentTimeMillis() - startTime) / 1000;
        txtTimer.setText(String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60));
    }


    boolean wrongOrder(int a, int b) {
        // Agar Ascending true hai: a > b wrong hai (e.g., 5 > 2)
        // Agar Ascending false (Descending) hai: a < b wrong hai (e.g., 2 < 5)
        return ascending ? (a > b) : (a < b);
    }

    boolean correctOrEqual(int a, int b) {
        // Ascending: a <= b sahi hai
        // Descending: a >= b sahi hai
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
                        "comparing " + a[j] + " and " + a[j + 1] +"."));
                if (wrongOrder(a[j], a[j + 1])) {
                    steps.add(new Step("swap", j, j + 1, 0,
                            "Items " + a[j] + " and " + a[j + 1] + " because they are out of order. Swapping them."));
                    swap(a, j, j + 1);
                }
            }
            // Yahan par ek naya step add karein jo bataye ki ye element fix ho gaya
            steps.add(new Step("mark", a.length - 1 - i, -1, 0, "Fixed position"));
        }
        // Aakhri element bach jata hai, usay bhi mark karein
        steps.add(new Step("mark", 0, -1, 0, " Array Sorted!"));

    }


    void insertionSort(int[] a) {
        for (int i = 1; i < a.length; i++) {
            int key = a[i];
            int j = i - 1;

            while (j >= 0 && (ascending ? a[j] > key : a[j] < key)) {
                steps.add(new Step("shift", j, j + 1, a[j], "Shifting " + a[j] + " to  the right"));
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
            steps.add(new Step("insert", j + 1, -1, key, "Inserting " + key + " into its correct sorted position."));

            // Yahan fix: i tak ka poora hissa sorted hai, isliye 0 se i tak mark karein
            for (int k = 0; k <= i; k++) {
                steps.add(new Step("mark", k, -1, 0, "Fixed position"));
            }
        }
    }

    void selectionSort(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            int selected = i;
            for (int j = i + 1; j < a.length; j++) {
                steps.add(new Step("compare", selected, j, 0, "Searching for the  minimum :comparing"));
                if (wrongOrder(a[selected], a[j])) selected = j;
            }
            if (selected != i) {
                steps.add(new Step("swap", i, selected, 0, "Swaping"+ " with the smallest element " + a[selected]));
                swap(a, i, selected);
            }
            // Yahan mark karein
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
    }

    int partition(int[] a, int low, int high) {
        int pivotValue = a[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            steps.add(new Step("compare", j, high, 0, "Compareing element at index " + j + " with pivot " + a[high]));
            boolean move = ascending ? a[j] < pivotValue : a[j] > pivotValue;
            if (move) {
                i++;
                steps.add(new Step("swap", i, j, 0,"Moving smaller element "+ a[j] + " to the left side of the pivot."));
                swap(a, i, j);
            }
        }
        steps.add(new Step("swap", i + 1, high, 0, "Placing pivot " + a[high] + " in its final sorted position."));
        swap(a, i + 1, high);
        // Pivot ko mark karein
        steps.add(new Step("mark", i + 1, -1, 0, "Pivot fixed"));
        return i + 1;

    }


    void mergeSort(int[] a, int left, int right, int  level) {
        if (left >= right) return;

        barsView.updateLevel(left, right, level);

        int mid = (left + right) / 2;
        mergeSort(a, left, mid, level + 1);
        mergeSort(a, mid + 1, right, level + 1);
        merge(a, left, mid, right, level);

        barsView.updateLevel(left, right, level);
        if (left == 0 && right == a.length - 1) {
            for (int k = left; k <= right; k++) {
                steps.add(new Step("mark", k, -1, 0, "Segment " + left + "-" + right + " is now sorted and fixed."));
            }
        }

    }

    void merge(int[] a, int left, int mid, int right,int level) {
        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;



        // 1. Comparison logic (Purple animation)
        while (i <= mid && j <= right) {
            steps.add(new Step("compare", i, j, 0, "Comparing" + a[i] + " and " + a[j]+ " to merge in  order."));
            if (correctOrEqual(a[i], a[j])) {
                temp[k++] = a[i++];
            } else {
                temp[k++] = a[j++];
            }
        }

        while (i <= mid) temp[k++] = a[i++];
        while (j <= right) temp[k++] = a[j++];

        // 2. Writing logic (Move/Update bars)
        for (int x = 0; x < temp.length; x++) {
            a[left + x] = temp[x];
            // "set" type trigger karega animateWrite()
            steps.add(new Step("set", left + x, -1, temp[x], "Merging" + temp[x] + " back into the main array." + (left + x) + "."));

        }
        barsView.updateLevel(left, right, level);
    }

    void heapSort(int[] a) {
        int n = a.length;
        for (int i = n / 2 - 1; i >= 0; i--) heapify(a, n, i);
        for (int i = n - 1; i > 0; i--) {
            steps.add(new Step("swap", 0, i, 0, "Swap root"));
            swap(a, 0, i);
            // Swap hone ke baad i index fix ho gaya
            steps.add(new Step("mark", i, -1, 0, "Fixed position"));
            heapify(a, i, 0);
        }
        steps.add(new Step("mark", 0, -1, 0, "Sorted!"));
    }

    void heapify(int[] a, int n, int root) {
        int best = root;
        int left = 2 * root + 1;
        int right = 2 * root + 2;
        if (left < n) {
            steps.add(new Step("compare", left, best, 0,
                    "Compare left child with root"));
            // Agar ascending hai to a[left] > a[best] check karo, warna a[left] < a[best]
            boolean shouldSwap = ascending ? (a[left] > a[best]) : (a[left] < a[best]);
            if (shouldSwap) {
                best = left;
            }
        }
        if (right < n) {
            steps.add(new Step("compare", right, best, 0,
                    "Compare right child with parent to maintain heap property. "));
            boolean shouldSwap = ascending ? (a[right] > a[best]) : (a[right] < a[best]);
            if (shouldSwap) {
                best = right;
            }
        }
        if (best != root) {
            steps.add(new Step("swap", root, best, 0,
                    "Swapping to maintain max_heap sructure."));
            swap(a, root, best);
            heapify(a, n, best);
        }
    }

    void shellSort(int[] a) {
        for (int gap = a.length / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < a.length; i++) {
                int temp = a[i];
                int j = i;
                // Yahan comparison ke liye compare step add karein (taake Purple ho)
                while (j >= gap && (ascending ? a[j - gap] > temp : a[j - gap] < temp)) {
                    steps.add(new Step("compare", j - gap, j, 0, "Comparing elements with a gap of" + gap));

                    // Yahan "set" ki jagah "shift" use karein
                    steps.add(new Step("shift", j - gap, j, a[j - gap], "Shifting element " + a[j - gap]+ " to fill the gap." ));

                    a[j] = a[j - gap];
                    j -= gap;
                }
                steps.add(new Step("set", j, -1, temp,
                        "Placing element " + temp + " after gap comparison"));
                a[j] = temp;
            }
        }
        // Shell Sort khatam hone ke baad saare elements ko Teal (mark) kar dein
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


        void markSorted(int index) {
            if (sortedStatus != null && index >= 0 && index < sortedStatus.length) {
                sortedStatus[index] = true;
                Log.d("DEBUG_COLOR", "Index " + index + " marked as sorted (Teal)");
                invalidate(); // Ye zaroori hai screen refresh karne ke liye
            }
        }

        // AnimatedBarsView ke variables mein ye update karein
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

            this.sortedStatus = new boolean[values.size()]; // Naya array yahan initialize hoga
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

        // AnimatedBarsView ke andar
        public void updateData(int index, int value) {
            if (index >= 0 && index < data.size()) {
                data.set(index, value);
                invalidate();
            }
        }
        // AnimatedBarsView ke andar
        public void updateLevel(int start, int end, int level) {
            for (int i = start; i <= end; i++) {
                if (i < barLevels.length) {
                    barLevels[i] = level;
                }
            }
            invalidate(); // UI refresh
        }
        // AnimatedBarsView.java mein
        public void animateBarMove(int from, int to, int value) {
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(500); // 500ms tak slide karega

            animator.addUpdateListener(animation -> {
                float progress = (float) animation.getAnimatedValue();
                // Yahan 'moveProgress' ko update karein aur invalidate() call karein
                this.moveProgress = progress;
                this.moveFrom = from;
                this.moveTo = to;
                invalidate(); // Har frame par UI redraw hoga, bar slide hota dikhega
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
            // This is similar to animateShift, but handles the transition smoothly
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(350); // Increased duration for visibility
            animator.addUpdateListener(a -> {
                // Here you would interpolate the X position of the bar
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

        // Add these variables
        int shiftFrom = -1;
        int shiftTo = -1;

        // Update animateShift
        void animateShift(int fromIndex, int toIndex, Runnable action) {
            shiftFrom = fromIndex;
            shiftTo = toIndex;
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(850);
            animator.addUpdateListener(a -> {
                swapProgress = (float) a.getAnimatedValue(); // You can keep using swapProgress
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

            // Dynamic height ke liye reference value
            float maxData = 100f;
            float availableHeight = getHeight() - dp(100);
            float baseY = getHeight() - dp(18);
            float barHeight = dp(62);



            for (int i = 0; i < data.size(); i++) {
                // Ab ye error nahi dega kyunki barLevels class member hai
                int currentLevel = barLevels[i];
                int shownValue = data.get(i);
                if (i == writeIndex && writeValue > 0) {
                    shownValue = writeValue;

                 barHeight = (shownValue / maxData) * availableHeight;
            }

                int color;

// 1. Priority: Fixed/Sorted elements (Teal)
                if (sortedStatus != null && i < sortedStatus.length && sortedStatus[i]) {
                    color = Color.rgb(0, 128, 128); // Teal (Sorted)
                }else if (i == writeIndex) {
                    color = Color.rgb(76, 175, 80); // Green (Value Update ho rahi hai)
                }
// 2. Priority: Currently Comparing/Shifting (Purple)
                else if (i == compareA || i == compareB || i == shiftFrom || i == shiftTo) {
                    color = Color.rgb(128, 0, 128); // Purple (comparing)
                }
// 3. Priority: Default (Blue)
                else {
                    color = Color.rgb(33, 150, 243); // Blue (deault)
                }


                float verticalShift = currentLevel * dp(60);
                float currentBaseY = baseY - verticalShift;
                float x = gap + i * (barWidth + gap);
                float top = currentBaseY - barHeight;
                if (i == swapA || i == swapB) {
                    // Agar swap ho raha hai
                    float targetIndex = (i == swapA) ? swapB : swapA;
                    x = gap + i * (barWidth + gap); // Start position
                    float targetX = gap + targetIndex * (barWidth + gap);

                    x = x + (targetX - x) * smoothMove(swapProgress);

                    if (i == swapA) top -= dp(45) * swapLift; // Swap A upar
                    else top += dp(22) * swapLift;            // Swap B niche
                }
                else if (i == shiftFrom || i == shiftTo) {
                    // Agar shift ho raha hai
                    x = gap + shiftFrom * (barWidth + gap);
                    float targetX = gap + shiftTo * (barWidth + gap);
                    x = x + (targetX - x) * smoothMove(swapProgress);
                }

                 else if (i == moveFrom) {
                    float startX = gap + moveFrom * (barWidth + gap);
                    float targetX = gap + moveTo * (barWidth + gap);
                    x = startX + (targetX - startX) * moveProgress; // Yahan bar slide hoga
                }
                // Animation logic (Swap aur Compare ka movement)
                // Inside onDraw loop, replace the animation logic with this:
                if (i == shiftFrom) {
                    float startX = gap + shiftFrom * (barWidth + gap);
                    float targetX = gap + shiftTo * (barWidth + gap);
                    x = startX + (targetX - startX) * smoothMove(swapProgress);
                    // Note: No "top" modification here, so it slides horizontally without jumping!
                } else if (i == swapA && swapB >= 0) {
                    float startX = gap + swapA * (barWidth + gap);
                    float targetX = gap + swapB * (barWidth + gap);
                    float move = smoothMove(swapProgress);
                    x = startX + (targetX - startX) * move;
                    top -= dp(45) * swapLift;
                } else if (i == swapB && swapA >= 0) {
                    float startX = gap + swapB * (barWidth + gap);
                    float targetX = gap + swapA * (barWidth + gap);
                    float move = smoothMove(swapProgress);
                    x = startX + (targetX - startX) * move;
                    top += dp(22) * swapLift;


                }

                // Agar move ho raha hai
                if (i == moveFrom) {
                    float startX = gap + moveFrom * (barWidth + gap);
                    float targetX = gap + moveTo * (barWidth + gap);
                    x = startX + (targetX - startX) * smoothMove(moveProgress);
                    top -= dp(30); // Thoda upar uthayein (lift effect)
                }

                // Drawing
                paint.setColor(color);
                // Shadow
                RectF shadowRect = new RectF(x + dp(3), top + dp(4), x + barWidth + dp(3), baseY + dp(4));
                canvas.drawRoundRect(shadowRect, dp(8), dp(8), paint);

                // Bar
                RectF rect = new RectF(x, top, x + barWidth, currentBaseY);
                canvas.drawRoundRect(rect, dp(8), dp(8), paint);

                // Text
                paint.setColor(Color.WHITE);
                paint.setTextSize(dp(13));
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setFakeBoldText(true);
                canvas.drawText(String.valueOf(shownValue), x + barWidth / 2, top + barHeight / 2 + dp(5), paint);
                paint.setFakeBoldText(false);

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