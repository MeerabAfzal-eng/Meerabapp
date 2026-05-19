package com.example.meerabapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SortingScope.db";
    private static final int DATABASE_VERSION = 1;

    // Profile Table Parameters
    private static final String TABLE_PROFILE = "user_profile";
    private static final String COL_ID = "user_id";
    private static final String COL_NAME = "user_name";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creating Profile Table
        String createProfileTable = "CREATE TABLE " + TABLE_PROFILE + " (" +
                COL_ID + " TEXT PRIMARY KEY, " +
                COL_NAME + " TEXT)";
        db.execSQL(createProfileTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        onCreate(db);
    }

    // Insert or Update Profile Data
    public boolean saveProfile(String id, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ID, id);
        contentValues.put(COL_NAME, name);

        // database clean rakhne ke liye pehle purana data clear kar dete hain (Single user session logic)
        db.delete(TABLE_PROFILE, null, null);

        long result = db.insert(TABLE_PROFILE, null, contentValues);
        return result != -1; // returns true if inserted successfully
    }

    // Check if user profile already exists
    public boolean isProfileCreated() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PROFILE, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}