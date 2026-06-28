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

public class activity_progress extends AppCompatActivity {

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

        loadQuizData();

        btnNextQuestion.setOnClickListener(v -> {
            if (!isAnswered) {
                int selectedId = optionsRadioGroup.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(this, "Select an option!", Toast.LENGTH_SHORT).show();
                    return;
                }
                isAnswered = true;
                RadioButton selected = findViewById(selectedId);
                if (selected.getText().toString().equals(correctAnswer)) {
                    score++;
                    txtCurrentScore.setText("Score: " + score);
                }
                btnNextQuestion.setText("Next Question");
            } else {
                currentQuestionIndex++;
                if (currentQuestionIndex < finalQuestionsList.size()) {
                    showQuestion(currentQuestionIndex);
                } else {
                    saveProgress();
                    txtQuestionCard.setText("Quiz Finished! Score: " + score + "/20");
                    btnNextQuestion.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadQuizData() {
        try {
            InputStream is = getAssets().open("questions.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            JSONArray array = new JSONArray(new String(buffer, "UTF-8"));
            List<JSONObject> all = new ArrayList<>();
            for(int i=0; i<array.length(); i++) all.add(array.getJSONObject(i));
            Collections.shuffle(all);
            finalQuestionsList = all.subList(0, Math.min(all.size(), 20));
            showQuestion(0);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void saveProgress() {
        SharedPreferences pref = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        int high = pref.getInt("high_score", 0);
        if (score > high) pref.edit().putInt("high_score", score).apply();
        pref.edit().putInt("recent_score", score).apply();
    }

    private void showQuestion(int index) {
        try {
            JSONObject q = finalQuestionsList.get(index);
            txtQuestionCount.setText("Q: " + (index+1) + "/20");
            txtQuestionCard.setText(q.getString("question"));
            optionA.setText(q.getString("optionA"));
            optionB.setText(q.getString("optionB"));
            optionC.setText(q.getString("optionC"));
            optionD.setText(q.getString("optionD"));
            correctAnswer = q.getString("correct");
            isAnswered = false;
            optionsRadioGroup.clearCheck();
            btnNextQuestion.setText("Submit");
        } catch (Exception e) { e.printStackTrace(); }
    }
}