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

    private TextView txtQuestionCount, txtCurrentScore, txtQuestionCard;
    private RadioGroup optionsRadioGroup;
    private RadioButton optionA, optionB, optionC, optionD;
    private Button btnNextQuestion;

    private List<JSONObject> finalQuestionsList;
    private int currentQuestionIndex = 0;
    private String correctAnswer;
    private int score = 0;
    private boolean isAnswered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        txtQuestionCount = findViewById(R.id.txtQuestionCount);
        txtCurrentScore = findViewById(R.id.txtCurrentScore);
        txtQuestionCard = findViewById(R.id.txtQuestionCard);
        optionsRadioGroup = findViewById(R.id.optionsRadioGroup);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);

        String jsonString = loadJSONFromAsset();
        if (jsonString != null) {
            try {
                JSONArray originalArray = new JSONArray(jsonString);
                List<JSONObject> allQuestions = new ArrayList<>();
                for (int i = 0; i < originalArray.length(); i++) {
                    allQuestions.add(originalArray.getJSONObject(i));
                }
                Collections.shuffle(allQuestions);
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

        btnNextQuestion.setOnClickListener(v -> {
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
                    selectedRadioButton.setBackgroundColor(Color.parseColor("#C8E6C9"));
                    score++;
                    txtCurrentScore.setText("Score: " + score);
                } else {
                    selectedRadioButton.setBackgroundColor(Color.parseColor("#FFCDD2"));
                    highlightCorrectAnswer();
                }
                btnNextQuestion.setText("Go to Next Question");
            } else {
                currentQuestionIndex++;
                if (currentQuestionIndex < finalQuestionsList.size()) {
                    resetOptionsTemplate();
                    showQuestion(currentQuestionIndex);
                } else {
                    txtQuestionCard.setText("Quiz Completed!\nYour Score: " + score + "/" + finalQuestionsList.size());
                    optionsRadioGroup.setVisibility(View.GONE);
                    btnNextQuestion.setVisibility(View.GONE);
                    txtQuestionCount.setText("Finished");

                    // Saving data
                    saveQuizResults();
                }
            }
        });
    }

    private void saveQuizResults() {
        SharedPreferences pref = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        int lastHighScore = pref.getInt("high_score", 0);
        if (score > lastHighScore) editor.putInt("high_score", score);

        editor.putInt("recent_score", score);

        String history = pref.getString("quiz_history", "");
        String newHistory = history.isEmpty() ? String.valueOf(score) : history + "," + score;
        editor.putString("quiz_history", newHistory);

        editor.apply();
        Toast.makeText(this, "Progress Saved!", Toast.LENGTH_SHORT).show();
    }

    private String loadJSONFromAsset() {
        try {
            InputStream is = getAssets().open("questions.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void showQuestion(int index) {
        try {
            JSONObject q = finalQuestionsList.get(index);
            txtQuestionCount.setText("Question: " + (index + 1) + "/" + finalQuestionsList.size());
            txtQuestionCard.setText(q.getString("question"));
            optionA.setText(q.getString("optionA"));
            optionB.setText(q.getString("optionB"));
            optionC.setText(q.getString("optionC"));
            optionD.setText(q.getString("optionD"));
            correctAnswer = q.getString("correct");
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