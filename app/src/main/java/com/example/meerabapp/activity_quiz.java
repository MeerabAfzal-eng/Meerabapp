package com.example.meerabapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

// Match physical file name: activity_quiz.java
public class activity_quiz extends AppCompatActivity {

    private TextView txtQuestionCount, txtCurrentScore, txtQuestionCard;
    private RadioGroup optionsRadioGroup;
    private RadioButton optionA, optionB, optionC, optionD;
    private Button btnNextQuestion;

    private DatabaseHelper dbHelper;
    private int currentQuestionIndex = 0;
    private int userScore = 0;

    // Documentation Compliant 30 Questions Bank Matrix
    private final String[] questions = {
            "Which sorting algorithm has a best-case time complexity of O(n) when the array is already sorted?",
            "What is the average case time complexity of Quick Sort?",
            "Which of the following algorithms is NOT a stable sorting algorithm?",
            "In Selection Sort, how many maximum swaps are performed in the worst-case scenario?",
            "What auxiliary space complexity does Merge Sort require?",
            "Which algorithm utilizes a gap sequence to reduce shifts, standardizing into insertion sort at gap=1?",
            "What data structure is inherently built and structured within a Heap Sort algorithm execution?",
            "Which sorting technique divides the array into sub-arrays and combines them back in sorted order?",
            "What is the worst-case time complexity of Insertion Sort?",
            "Which pointer-based approach selects a 'pivot' component to break down partitions?",
            "What is the best-case time complexity of Bubble Sort when optimized with a swapped flag?",
            "Which sorting algorithm is in-place, stable, and runs in O(n²) worst-case time?",
            "What is the worst-case time complexity of Quick Sort?",
            "What is the space complexity of an optimized Bubble Sort algorithm?",
            "Which sorting algorithm is considered the fastest for small datasets or nearly sorted arrays?",
            "What is the average-case time complexity of Heap Sort?",
            "Which of the following algorithms is a non-comparison based sorting technique?",
            "What is the space complexity of Selection Sort?",
            "If an algorithm preserves the relative order of duplicate items, it is called:",
            "Which sorting algorithm uses the 'Divide and Conquer' paradigm?",
            "What is the worst-case time complexity of Merge Sort?",
            "In Heap Sort, what is the time complexity to heapify a single sub-tree element node?",
            "Which sorting algorithm mimics the layout of sorting a deck of cards hand-by-hand?",
            "What is the primary disadvantage of Merge Sort compared to Quick Sort or Heap Sort?",
            "What is the worst-case time complexity of Selection Sort?",
            "Which gap sequence was originally proposed by Donald Shell for Shell Sort?",
            "What is the space complexity of Quick Sort in the best-case scenario due to recursive stack calls?",
            "Can a comparison-based sorting algorithm have a worst-case time complexity better than O(n log n)?",
            "In Bubble Sort, after the first complete pass, which element is guaranteed to be at its final position?",
            "Which algorithm extracts the minimum element repeatedly and places it at the beginning?"
    };

    private final String[][] options = {
            {"Bubble Sort", "Selection Sort", "Quick Sort", "Heap Sort"},
            {"O(n²)", "O(n log n)", "O(log n)", "O(n)"},
            {"Bubble Sort", "Insertion Sort", "Merge Sort", "Quick Sort"},
            {"O(n²)", "O(n)", "O(n log n)", "O(1)"},
            {"O(1)", "O(log n)", "O(n)", "O(n²)"},
            {"Bubble Sort", "Shell Sort", "Merge Sort", "Selection Sort"},
            {"Binary Tree", "Complete Binary Heap", "Graph Array", "Linked Stack"},
            {"Merge Sort", "Quick Sort", "Selection Sort", "Insertion Sort"},
            {"O(n)", "O(n log n)", "O(n²)", "O(1)"},
            {"Heap Sort", "Shell Sort", "Bubble Sort", "Quick Sort"},
            {"O(n)", "O(n log n)", "O(n²)", "O(1)"},
            {"Quick Sort", "Insertion Sort", "Selection Sort", "Heap Sort"},
            {"O(n log n)", "O(n²)", "O(n³)", "O(n)"},
            {"O(1)", "O(n)", "O(log n)", "O(n²)"},
            {"Selection Sort", "Quick Sort", "Merge Sort", "Insertion Sort"},
            {"O(n)", "O(n²)", "O(n log n)", "O(log n)"},
            {"Radix Sort", "Bubble Sort", "Quick Sort", "Insertion Sort"},
            {"O(n)", "O(n log n)", "O(1)", "O(n²)"},
            {"Secure", "Stable", "Static", "Balanced"},
            {"Insertion Sort", "Bubble Sort", "Merge Sort", "Selection Sort"},
            {"O(n log n)", "O(n²)", "O(n)", "O(log n)"},
            {"O(1)", "O(log n)", "O(n)", "O(n log n)"},
            {"Insertion Sort", "Bubble Sort", "Selection Sort", "Merge Sort"},
            {"It is unstable", "High space requirement", "Poor worst-case time", "Hard to program"},
            {"O(n)", "O(n log n)", "O(log n)", "O(n²)"},
            {"n/2, n/4, ...", "1, 3, 7, 15, ...", "1, 4, 13, 40, ...", "Powers of 2"},
            {"O(1)", "O(log n)", "O(n)", "O(n²)"},
            {"Yes, O(n)", "Yes, O(log n)", "No, mathematically proven", "Yes, O(n¹⁵)"},
            {"The smallest element", "The largest element", "The median element", "No element is guaranteed"},
            {"Bubble Sort", "Insertion Sort", "Selection Sort", "Merge Sort"}
    };

    private final int[] correctAnswers = {
            0, 1, 3, 1, 2, 1, 1, 0, 2, 3,
            0, 1, 1, 0, 3, 2, 0, 2, 1, 2,
            0, 1, 0, 1, 3, 0, 1, 2, 1, 2
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        dbHelper = new DatabaseHelper(this);

        txtQuestionCount = findViewById(R.id.txtQuestionCount);
        txtCurrentScore = findViewById(R.id.txtCurrentScore);
        txtQuestionCard = findViewById(R.id.txtQuestionCard);
        optionsRadioGroup = findViewById(R.id.optionsRadioGroup);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);

        displayQuestion();

        btnNextQuestion.setOnClickListener(v -> {
            int selectedId = optionsRadioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select an answer to proceed!", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedAnswerIndex = -1;
            if (selectedId == R.id.optionA) selectedAnswerIndex = 0;
            else if (selectedId == R.id.optionB) selectedAnswerIndex = 1;
            else if (selectedId == R.id.optionC) selectedAnswerIndex = 2;
            else if (selectedId == R.id.optionD) selectedAnswerIndex = 3;

            if (selectedAnswerIndex == correctAnswers[currentQuestionIndex]) {
                userScore++;
            }

            currentQuestionIndex++;

            if (currentQuestionIndex < questions.length) {
                displayQuestion();
            } else {
                finalizeQuizSession();
            }
        });
    }

    private void displayQuestion() {
        optionsRadioGroup.clearCheck();

        txtQuestionCount.setText("Question: " + (currentQuestionIndex + 1) + "/" + questions.length);
        txtCurrentScore.setText("Score: " + userScore);
        txtQuestionCard.setText(questions[currentQuestionIndex]);

        optionA.setText(options[currentQuestionIndex][0]);
        optionB.setText(options[currentQuestionIndex][1]);
        optionC.setText(options[currentQuestionIndex][2]);
        optionD.setText(options[currentQuestionIndex][3]);
    }
private void finalizeQuizSession() {
    boolean isSaved = dbHelper.saveQuizResult(userScore, questions.length);

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Quiz Completed! 🎉");

    String feedbackMessage = isSaved ?
            "Score recorded! Let's see your progress.\n\n" :
            "Score recorded (Offline mode).\n\n";

    builder.setMessage(feedbackMessage + "Total Score: " + userScore + "/" + questions.length);
    builder.setCancelable(false);

    // Yahan wo purana button remove ho gaya aur naya performance button lag gaya
    builder.setPositiveButton("View Performance Chart", (dialog, which) -> {
        Intent intent = new Intent(activity_quiz.this, activity_progress.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    });

    builder.show();
}

}