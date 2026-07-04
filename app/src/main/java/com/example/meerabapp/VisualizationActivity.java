
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
public class VisualizationActivity extends AppCompatActivity {
    TextView txtAlgoName, txtSwapCounter, txtTimer, txtComplexity, txtExplanation;
    LinearLayout visualContainer;
    Button btnAsc, btnDesc, btnRestart, btnReset;
    AnimatedBarsView barsView;
    ArrayList<Integer> arr = new ArrayList<>();
    ArrayList<Integer> originalArr = new ArrayList<>();
    ArrayList<Step> steps = new ArrayList<>();
    Handler handler = new Handler(Looper.getMainLooper());
    int stepIndex = 0;
    int swaps = 0;
    boolean ascending = true;
    long startTime;
    String algorithm = "Bubble Sort";
    static final int STEP_DELAY = 80;

    static class Step {
        String type, message;
        int i, j, value;

        Step(String type, int i, int j, int value, String message) {
            this.type = type;
            this.i = i;
            this.j = j;
            this.value = value;
            this.message = message;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualization);
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
        if (algorithm.equals("Quick Sort")) {
            txtComplexity.setText("Avg O(n log n)");

        } else if (algorithm.equals("Merge Sort") || algorithm.equals("Heap Sort") || algorithm.equals("Shell Sort")) {
            txtComplexity.setText("O(n log n)");
        } else {
            txtComplexity.setText("O(n²)");
        }
    }

    void startSorting() {
        handler.removeCallbacksAndMessages(null);
        arr.clear();
        arr.addAll(originalArr);
        steps.clear();
        stepIndex = 0;
        swaps = 0;
        startTime = System.currentTimeMillis();
        txtSwapCounter.setText("Swaps: 0");
        txtTimer.setText("00:00");
        txtExplanation.setText("");
        barsView.setData(arr);
        int[] copy = toIntArray(arr);
        if (algorithm.equals("Bubble Sort")) bubbleSort(copy);
        else if (algorithm.equals("Insertion Sort")) insertionSort(copy);
        else if (algorithm.equals("Selection Sort")) selectionSort(copy);
        else if (algorithm.equals("Quick Sort")) quickSort(copy, 0, copy.length - 1);
        else if (algorithm.equals("Merge Sort")) mergeSort(copy, 0, copy.length - 1);
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
            txtExplanation.append("\nSorting completed.");
            return;
        }
        Step step = steps.get(stepIndex++);
        // Log add karein taaki pata chale kaunsa step chal raha hai
        Log.d("DEBUG_SORT", "Step Type: " + step.type + " Message: " + step.message);
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
             if (step.type.equals("set")) {
                barsView.animateWrite(step.i, step.value, () -> {
                    // Yahan view ka data update karein
                    barsView.updateData(step.i, step.value);
                    arr.set(step.i, step.value);
                    swaps++;
                    txtSwapCounter.setText("Moves: " + swaps);
                    handler.postDelayed(this::playStep, STEP_DELAY);
                });
            }
        } else if (step.type.equals("set")) {
            // Shift waala check
            if (step.message.contains("Shift")) {
                if (step.i + 1 < arr.size()) {
                    barsView.animateShift(step.i, step.i + 1, () -> {
                        arr.set(step.i + 1, step.value);
                        barsView.invalidate();
                        swaps++;
                        txtSwapCounter.setText("Moves: " + swaps);
                        txtExplanation.append("\n" + step.message);
                        handler.postDelayed(this::playStep, STEP_DELAY);
                    });
                } else {
                    handler.postDelayed(this::playStep, 0);
                }
            }
            // Yahan 'else' sahi jagah band ho raha hai
            else {
                barsView.animateWrite(step.i, step.value, () -> {
                    arr.set(step.i, step.value);
                    barsView.invalidate();
                    swaps++;
                    txtSwapCounter.setText("Moves: " + swaps);
                    txtExplanation.append("\n" + step.message);
                    handler.postDelayed(this::playStep, STEP_DELAY);
                });
            }
        } else if (step.type.equals("mark")) {
            barsView.markSorted(step.i);
            barsView.invalidate();
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
        return ascending ? a > b : a < b;
    }


    boolean correctOrEqual(int a, int b) {
        return ascending ? a <= b : a >= b;
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
                        "Compare " + a[j] + " and " + a[j + 1]));
                if (wrongOrder(a[j], a[j + 1])) {
                    steps.add(new Step("swap", j, j + 1, 0,
                            "Swap " + a[j] + " and " + a[j + 1]));
                    swap(a, j, j + 1);
                }
            }
            // Yahan par ek naya step add karein jo bataye ki ye element fix ho gaya
            steps.add(new Step("mark", a.length - 1 - i, -1, 0, "Fixed position"));
        }
        // Aakhri element bach jata hai, usay bhi mark karein
        steps.add(new Step("mark", 0, -1, 0, "Sorted!"));

    }


    void insertionSort(int[] a) {
        steps.add(new Step("mark", 0, -1, 0, "Initial element"));
        for (int i = 1; i < a.length; i++) {
            int key = a[i];
            int j = i - 1;

            // Jab tak wrong position par hai
            while (j >= 0 && (ascending ? a[j] > key : a[j] < key)) {
                // Shift animation ke liye "set" type
                steps.add(new Step("set", j + 1, -1, a[j], "Shifting " + a[j]));
                a[j + 1] = a[j];
                j--;
            }
            steps.add(new Step("set", j + 1, -1, key, "Inserting " + key));
            a[j + 1] = key;

            // Mark sorted section
            for(int k = 0; k <= i; k++) {
                steps.add(new Step("mark", k, -1, 0, "Sorted"));
            }
        }
    }
    void selectionSort(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            int selected = i;
            for (int j = i + 1; j < a.length; j++) {
                steps.add(new Step("compare", selected, j, 0, "Finding minimum"));
                if (wrongOrder(a[selected], a[j])) selected = j;
            }
            if (selected != i) {
                steps.add(new Step("swap", i, selected, 0, "Swap"));
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
            steps.add(new Step("compare", j, high, 0, "Compare"));
            boolean move = ascending ? a[j] < pivotValue : a[j] > pivotValue;
            if (move) {
                i++;
                steps.add(new Step("swap", i, j, 0, "Swap"));
                swap(a, i, j);
            }
        }
        steps.add(new Step("swap", i + 1, high, 0, "Pivot in place"));
        swap(a, i + 1, high);
        // Pivot ko mark karein
        steps.add(new Step("mark", i + 1, -1, 0, "Pivot fixed"));
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
        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;

        // 1. Comparison logic (Purple animation)
        while (i <= mid && j <= right) {
            steps.add(new Step("compare", i, j, 0, "Compare " + a[i] + " and " + a[j]));
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
            // "set" type trigger karega animateWrite()
            steps.add(new Step("set", left + x, -1, temp[x], "Place " + temp[x]));
            a[left + x] = temp[x];
        }

        // 3. Mark logic (Teal color)
        for (int x = left; x <= right; x++) {
            steps.add(new Step("mark", x, -1, 0, "Fixed"));
        }
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
            if (wrongOrder(a[best], a[left])) {
                best = left;
            }
        }
        if (right < n) {
            steps.add(new Step("compare", right, best, 0,
                    "Compare right child with current best"));
            if (wrongOrder(a[best], a[right])) {
                best = right;
            }
        }
        if (best != root) {
            steps.add(new Step("swap", root, best, 0,
                    "Restore heap order"));
            swap(a, root, best);
            heapify(a, n, best);
        }
    }

    void shellSort(int[] a) {
        for (int gap = a.length / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < a.length; i++) {
                int temp = a[i];
                int j = i;
                while (j >= gap) {
                    steps.add(new Step("compare", j - gap, j, 0,
                            "Compare gap " + gap));
                    if (correctOrEqual(a[j - gap], temp)) break;
                    steps.add(new Step("set", j, -1, a[j - gap],
                            "Shift " + a[j - gap] + " by gap " + gap));
                    a[j] = a[j - gap];
                    j -= gap;
                }
                steps.add(new Step("set", j, -1, temp,
                        "Place " + temp + " after gap comparison"));
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

        void markSorted(int index) {
            if (sortedStatus != null && index >= 0 && index < sortedStatus.length) {
                sortedStatus[index] = true;
                invalidate();
            }
        }

        // AnimatedBarsView ke variables mein ye update karein
        int unsortedColor = Color.rgb(33, 150, 243); // Blue
        int compareColor = Color.rgb(128, 0, 128);   // Purple
        int sortedColor = Color.rgb(0, 128, 128);    // Teal
        int writeColor = Color.rgb(255, 193, 7);     // Yellow (Write/Set ke liye)

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
        void updateData(int index, int value) {
            if (index >= 0 && index < data.size()) {
                data.set(index, value);
                invalidate();
            }
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
            animator.setDuration(850);
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

        void animateShift(int fromIndex, int toIndex, Runnable action) {
            swapA = fromIndex;
            swapB = toIndex;
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(300); // Shifting speed
            animator.addUpdateListener(a -> {
                swapProgress = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    swapA = -1;
                    swapB = -1;
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
            animator.setDuration(260);
            animator.addUpdateListener(a -> invalidate());
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    writeIndex = -1;
                    writeValue = -1;
                    invalidate();
                    action.run();
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
            float barHeight = dp(62);
            float baseY = getHeight() - dp(18);

            for (int i = 0; i < data.size(); i++) {
                int shownValue = data.get(i);
                if (i == writeIndex && writeValue > 0) {
                    shownValue = writeValue;
                }

                // Color Determine karein
                int color;
                if (sortedStatus != null && i < sortedStatus.length && sortedStatus[i]) {
                    color = Color.rgb(0, 128, 128); // Teal for sorted
                } else if (i == compareA || i == compareB) {
                    color = Color.rgb(128, 0, 128); // Purple for comparing
                } else {
                    color = Color.rgb(33, 150, 243); // Blue for normal
                }

                float x = gap + i * (barWidth + gap);
                float top = baseY - barHeight;

                // Animation logic (Swap aur Compare ka movement)
                if (i == swapA && swapB >= 0) {
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
                if (i == compareA || i == compareB) {
                    top -= dp(12) * (float) Math.sin(compareProgress * Math.PI);
                }

                // Drawing
                paint.setColor(color);
                // Shadow
                RectF shadowRect = new RectF(x + dp(3), top + dp(4), x + barWidth + dp(3), baseY + dp(4));
                canvas.drawRoundRect(shadowRect, dp(8), dp(8), paint);

                // Bar
                RectF rect = new RectF(x, top, x + barWidth, baseY);
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
}
