package com.example.meerabapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SortingScope.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_PROFILE = "user_profile";
    private static final String COL_ID = "user_id";
    private static final String COL_NAME = "user_name";

    // Quiz Table Parameters (Naya Addition SRS ke mutabik)
    private static final String TABLE_QUIZ = "quiz_results";
    private static final String COL_QUIZ_ID = "quiz_id";
    private static final String COL_SCORE = "score";
    private static final String COL_TOTAL = "total_questions";
    private static final String COL_DATE = "test_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table 1: Profile Table
        String createProfileTable = "CREATE TABLE " + TABLE_PROFILE + " (" +
                COL_ID + " TEXT PRIMARY KEY, " +
                COL_NAME + " TEXT)";
        db.execSQL(createProfileTable);

        String createQuizTable = "CREATE TABLE " + TABLE_QUIZ + " (" +
                COL_QUIZ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SCORE + " INTEGER, " +
                COL_TOTAL + " INTEGER, " +
                COL_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(createQuizTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZ);
        onCreate(db);
    }

    public boolean saveProfile(String id, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ID, id);
        contentValues.put(COL_NAME, name);

        db.delete(TABLE_PROFILE, null, null);
        long result = db.insert(TABLE_PROFILE, null, contentValues);
        return result != -1;
    }

    public boolean isProfileCreated() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PROFILE, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean saveQuizResult(int score, int total) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_SCORE, score);
        contentValues.put(COL_TOTAL, total);

        long result = db.insert(TABLE_QUIZ, null, contentValues);
        return result != -1;
    }

    public Cursor getAllQuizResults() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_QUIZ + " ORDER BY " + COL_QUIZ_ID + " ASC", null);
    }
}