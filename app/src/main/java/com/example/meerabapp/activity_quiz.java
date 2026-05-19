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

// 1. Pehle line number 13 par public class ka naam badlein:
public class activity_quiz extends AppCompatActivity {

    private TextView txtQuestionCount, txtCurrentScore, txtQuestionCard;
    private RadioGroup optionsRadioGroup;
    private RadioButton optionA, optionB, optionC, optionD;
    private Button btnNextQuestion;

    private DatabaseHelper dbHelper;
    private int currentQuestionIndex = 0;
    private int userScore = 0;

    // SRS Framework Compliant Question Bank Matrix
    private final String[] questions = {
            "Which sorting algorithm has a best-case time complexity of O(n) when the array is already sorted?",
            "What is the average case time complexity of Quick Sort?",
            "Which of the following algorithms is NOT a stable sorting algorithm?",
            "In Selection Sort, how many swaps are performed in the worst case scenario?",
            "What auxiliary space complexity does Merge Sort require?",
            "Which algorithm utilizes a gap sequence to reduce shifts, standardizing into insertion sort at gap=1?",
            "What data structure is inherently built and structured within a Heap Sort algorithm execution?",
            "Which sorting technique divides the array into sub-arrays and combines them back in sorted order?",
            "What is the worst-case time complexity of Insertion Sort?",
            "Which pointer-based approach selects a 'pivot' component to break down partitions?"
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
            {"Heap Sort", "Shell Sort", "Bubble Sort", "Quick Sort"}
    };

    private final int[] correctAnswers = {0, 1, 3, 1, 2, 1, 1, 0, 2, 3}; // Store precise index allocations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        dbHelper = new DatabaseHelper(this);

        // UI References Allocation
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

            // Evaluation Mapping Check
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
        // Save score to local database via our helper class helper
        boolean isSaved = dbHelper.saveQuizResult(userScore, questions.length);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quiz Completed! 🎉");

        String feedbackMessage = isSaved ?
                "Your score was successfully recorded to performance logs.\n\n" :
                "Error processing metrics log routing storage.\n\n";

        builder.setMessage(feedbackMessage + "Total Score Secured: " + userScore + " out of " + questions.length);
        builder.setCancelable(false);
        builder.setPositiveButton("Return to Workspace Dashboard", (dialog, which) -> {
            // 2. Line number 128 ke aas paas Intent ka naam bhi yeh kar dein:
            Intent intent = new Intent(activity_quiz.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clears the back stack trace runtime
            startActivity(intent);
            finish();
        });

        builder.show();
    }
}