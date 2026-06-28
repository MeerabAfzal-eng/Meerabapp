package com.example.meerabapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class activity_quiz extends AppCompatActivity {

    // UI Elements
    private TextView txtQuestionCount, txtCurrentScore, txtQuestionCard;
    private RadioGroup optionsRadioGroup;
    private RadioButton optionA, optionB, optionC, optionD;
    private Button btnNextQuestion;

    // Quiz Control Variables
    private List<JSONObject> finalQuestionsList;
    private int currentQuestionIndex = 0;
    private String correctAnswer;
    private int score = 0;
    private boolean isAnswered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // 1. UI Connect
        txtQuestionCount = findViewById(R.id.txtQuestionCount);
        txtCurrentScore = findViewById(R.id.txtCurrentScore);
        txtQuestionCard = findViewById(R.id.txtQuestionCard);
        optionsRadioGroup = findViewById(R.id.optionsRadioGroup);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);

        // 2. Data Load aur Random 20 Select karna
        String jsonString = loadJSONFromAsset();
        if (jsonString != null) {
            try {
                JSONArray originalArray = new JSONArray(jsonString);
                List<JSONObject> allQuestions = new ArrayList<>();

                for (int i = 0; i < originalArray.length(); i++) {
                    allQuestions.add(originalArray.getJSONObject(i));
                }

                // Questions shuffle karna
                Collections.shuffle(allQuestions);

                // Sirf pehle 20 random sawal uthana
                finalQuestionsList = new ArrayList<>();
                int limit = Math.min(allQuestions.size(), 20);
                for (int i = 0; i < limit; i++) {
                    finalQuestionsList.add(allQuestions.get(i));
                }

                showQuestion(currentQuestionIndex);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 3. Next Button Logic
        btnNextQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAnswered) {
                    int selectedId = optionsRadioGroup.getCheckedRadioButtonId();
                    if (selectedId == -1) {
                        Toast.makeText(activity_quiz.this, "Select One Option!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    isAnswered = true;
                    RadioButton selectedRadioButton = findViewById(selectedId);
                    String selectedAnswer = selectedRadioButton.getText().toString();

                    if (selectedAnswer.equals(correctAnswer)) {
                        selectedRadioButton.setBackgroundColor(Color.parseColor("#C8E6C9")); // Green
                        score++;
                        txtCurrentScore.setText("Score: " + score);
                        Toast.makeText(activity_quiz.this, "Right Answer! 🎉", Toast.LENGTH_SHORT).show();
                    } else {
                        selectedRadioButton.setBackgroundColor(Color.parseColor("#FFCDD2")); // Red
                        highlightCorrectAnswer();
                        Toast.makeText(activity_quiz.this, "Wrong Answer! ❌", Toast.LENGTH_SHORT).show();
                    }

                    btnNextQuestion.setText("Go to Next Question");

                } else {
                    currentQuestionIndex++;
                    if (currentQuestionIndex < finalQuestionsList.size()) {
                        resetOptionsTemplate();
                        showQuestion(currentQuestionIndex);
                    } else {
                        // Quiz Finished Interface
                        txtQuestionCard.setText("Your Quiz Is Completed.\nYour Total Score: " + score + "/" + finalQuestionsList.size());
                        optionsRadioGroup.setVisibility(View.GONE);
                        btnNextQuestion.setVisibility(View.GONE);
                        txtQuestionCount.setText("Completed!");

                        // 💾 LIVE PROGRESS SAVE LOGIC (SharedPreferences)
                        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        // Purana High Score check karna
                        int lastHighScore = sharedPreferences.getInt("high_score", 0);
                        if (score > lastHighScore) {
                            editor.putInt("high_score", score); // Naya record save
                        }

                        // Total attempts badhana (+1)
                        int totalQuizzes = sharedPreferences.getInt("total_quizzes", 0);
                        editor.putInt("total_quizzes", totalQuizzes + 1);

                        // Recent current score save karna
                        editor.putInt("recent_score", score);

                        editor.apply(); // Mobile internal memory mein permanently save ho gaya!
                    }
                }
            }
        });
    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("questions.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void showQuestion(int index) {
        try {
            JSONObject questionObject = finalQuestionsList.get(index);

            txtQuestionCount.setText("Question: " + (index + 1) + "/" + finalQuestionsList.size());
            txtQuestionCard.setText(questionObject.getString("question"));

            optionA.setText(questionObject.getString("optionA"));
            optionB.setText(questionObject.getString("optionB"));
            optionC.setText(questionObject.getString("optionC"));
            optionD.setText(questionObject.getString("optionD"));

            correctAnswer = questionObject.getString("correct");
            isAnswered = false;
            btnNextQuestion.setText("Submit Answer");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void highlightCorrectAnswer() {
        if (optionA.getText().toString().equals(correctAnswer)) optionA.setBackgroundColor(Color.parseColor("#C8E6C9"));
        else if (optionB.getText().toString().equals(correctAnswer)) optionB.setBackgroundColor(Color.parseColor("#C8E6C9"));
        else if (optionC.getText().toString().equals(correctAnswer)) optionC.setBackgroundColor(Color.parseColor("#C8E6C9"));
        else if (optionD.getText().toString().equals(correctAnswer)) optionD.setBackgroundColor(Color.parseColor("#C8E6C9"));
    }

    private void resetOptionsTemplate() {
        optionsRadioGroup.clearCheck();
        optionA.setBackgroundColor(Color.WHITE);
        optionB.setBackgroundColor(Color.WHITE);
        optionC.setBackgroundColor(Color.WHITE);
        optionD.setBackgroundColor(Color.WHITE);
    }
}