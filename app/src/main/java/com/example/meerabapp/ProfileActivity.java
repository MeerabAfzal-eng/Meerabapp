package com.example.meerabapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

public class ProfileActivity extends AppCompatActivity {
    private EditText etName, etID;
    private Button btnSubmit, btnSwitchProfile;


    private AlertDialog profileListDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etName = findViewById(R.id.etName);
        etID = findViewById(R.id.etID);
        btnSubmit = findViewById(R.id.btnSubmitProfile);
        btnSwitchProfile = findViewById(R.id.btnSwitchProfile);

        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String id = etID.getText().toString().trim();

            if (name.isEmpty() || id.isEmpty()) {
                Toast.makeText(this, "Please enter your Name and ID", Toast.LENGTH_SHORT).show();
                return;
            }

            if (id.contains(",")) {
                Toast.makeText(this, "ID mein comma (,) allowed nahi hai", Toast.LENGTH_SHORT).show();
                return;
            }

            // Duplicate profile check
            if (checkDuplicateAndWarn(name, id)) {
                return; // duplicate mila, dialog dikha diya gaya
            }

            saveActiveProfile(name, id);
            Toast.makeText(this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show();
            goToMain();
        });


        btnSwitchProfile.setOnClickListener(v -> showProfileSwitcher());
    }


    private String normalizeId(String id) {
        String t = id.trim();
        if (t.matches("\\d+")) {
            return t.replaceFirst("^0+(?!$)", "");
        }
        return t.toLowerCase();
    }

    private String findMatchingId(ArrayList<String> ids, String id, String skipId) {
        String target = normalizeId(id);
        for (String existing : ids) {
            if (existing.equals(skipId)) continue;
            if (normalizeId(existing).equals(target)) return existing;
        }
        return null;
    }


    private boolean checkDuplicateAndWarn(String name, String id) {
        SharedPreferences pref = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String existingIdsRaw = pref.getString("all_user_ids", "");
        if (existingIdsRaw.isEmpty()) return false;

        ArrayList<String> ids = new ArrayList<>(Arrays.asList(existingIdsRaw.split(",")));


        String matchedId = findMatchingId(ids, id, null);
        if (matchedId != null) {
            String existingName = pref.getString("name_for_" + matchedId, "User");

            if (existingName.equalsIgnoreCase(name)) {

                new AlertDialog.Builder(this)
                        .setTitle("User Already Exists")
                        .setMessage(existingName + " (ID: " + matchedId + ") is already registered.\n\n"
                                + "Do you want to continue with this profile?")
                        .setPositiveButton("Use This Profile", (d, w) -> {

                            setActiveProfile(matchedId, existingName);
                            Toast.makeText(this, "Switched to " + existingName, Toast.LENGTH_SHORT).show();
                            goToMain();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {

                String message = matchedId.equals(id)
                        ? "ID " + id + " already belongs to " + existingName + "."
                        : "ID " + id + " is the same as ID " + matchedId
                        + ", which already belongs to " + existingName + ".";
                new AlertDialog.Builder(this)
                        .setTitle("ID Already Used")
                        .setMessage(message + "\n\nPlease enter a different ID.")
                        .setPositiveButton("OK", null)
                        .show();
            }
            return true;
        }


        for (String existingId : ids) {
            String existingName = pref.getString("name_for_" + existingId, "");
            if (existingName.equalsIgnoreCase(name)) {
                new AlertDialog.Builder(this)
                        .setTitle("User Already Exists")
                        .setMessage("A user named " + existingName + " (ID: " + existingId + ") already exists.\n\n"
                                + "Use \"Use Existing Profile\" to continue with it, or enter a different name.")
                        .setPositiveButton("OK", null)
                        .show();
                return true;
            }
        }

        return false;
    }


    private void setActiveProfile(String id, String name) {
        getSharedPreferences("UserProfile", MODE_PRIVATE).edit()
                .putString("user_id", id)
                .putString("user_name", name)
                .putBoolean("is_profile_set", true)
                .apply();
    }


    private void saveActiveProfile(String name, String id) {
        SharedPreferences pref = getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("user_name", name);
        editor.putString("user_id", id);
        editor.putBoolean("is_profile_set", true);


        editor.putString("name_for_" + id, name);


        String existingIdsRaw = pref.getString("all_user_ids", "");
        LinkedHashSet<String> idSet = new LinkedHashSet<>();
        if (!existingIdsRaw.isEmpty()) {
            idSet.addAll(Arrays.asList(existingIdsRaw.split(",")));
        }
        idSet.add(id);
        editor.putString("all_user_ids", TextUtils.join(",", idSet));

        editor.apply();
    }


    private void showProfileSwitcher() {
        SharedPreferences pref = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String existingIdsRaw = pref.getString("all_user_ids", "");

        if (existingIdsRaw.isEmpty()) {
            Toast.makeText(this, "No saved profiles yet. Please create one first.", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> ids = new ArrayList<>(Arrays.asList(existingIdsRaw.split(",")));
        String[] displayNames = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            String storedName = pref.getString("name_for_" + ids.get(i), "User");
            displayNames[i] = storedName + " (" + ids.get(i) + ")";
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, R.layout.item_profile, R.id.tvProfileName, displayNames) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                TextView tvEdit = row.findViewById(R.id.tvEditProfile);
                tvEdit.setOnClickListener(v -> {
                    if (profileListDialog != null) profileListDialog.dismiss();
                    showEditProfileDialog(ids.get(position));
                });
                return row;
            }
        };

        profileListDialog = new AlertDialog.Builder(this)
                .setTitle("Switch Profile")
                .setAdapter(adapter, (dialog, which) -> {
                    String selectedId = ids.get(which);
                    String selectedName = pref.getString("name_for_" + selectedId, "User");

                    setActiveProfile(selectedId, selectedName);

                    Toast.makeText(this, "Switched to " + selectedName, Toast.LENGTH_SHORT).show();
                    goToMain();
                })
                .setNegativeButton("Cancel", null)
                .create();
        profileListDialog.show();
    }


    private void showEditProfileDialog(String oldId) {
        SharedPreferences pref = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String currentName = pref.getString("name_for_" + oldId, "User");

        EditText nameInput = new EditText(this);
        nameInput.setHint("Name");
        nameInput.setSingleLine(true);
        nameInput.setText(currentName);
        nameInput.setSelection(currentName.length());
        nameInput.setTextColor(Color.parseColor("#001F3F"));

        EditText idInput = new EditText(this);
        idInput.setHint("Student ID");
        idInput.setSingleLine(true);
        idInput.setText(oldId);
        idInput.setTextColor(Color.parseColor("#001F3F"));

        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(pad, pad / 2, pad, 0);
        container.addView(nameInput);
        container.addView(idInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(container)
                .setPositiveButton("Save", null) // neeche override karte hain, taky galat input par dialog band na ho
                .setNegativeButton("Cancel", (d, w) -> showProfileSwitcher())
                .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String newName = nameInput.getText().toString().trim();
                    String newId = idInput.getText().toString().trim();

                    if (newName.isEmpty()) {
                        nameInput.setError("Name cannot be empty");
                        return;
                    }
                    if (newId.isEmpty()) {
                        idInput.setError("ID cannot be empty");
                        return;
                    }
                    if (newId.contains(",")) {
                        idInput.setError("ID mein comma (,) allowed nahi hai");
                        return;
                    }


                    if (newName.equals(currentName) && newId.equals(oldId)) {
                        dialog.dismiss();
                        showProfileSwitcher();
                        return;
                    }

                    ArrayList<String> ids = new ArrayList<>(
                            Arrays.asList(pref.getString("all_user_ids", "").split(",")));


                    String matchedId = findMatchingId(ids, newId, oldId);
                    if (matchedId != null) {
                        String matchedName = pref.getString("name_for_" + matchedId, "another user");
                        String extra = matchedId.equals(newId) ? "" : " (as ID " + matchedId + ")";
                        idInput.setError("This ID is already used by " + matchedName + extra);
                        return;
                    }

                    for (String otherId : ids) {
                        if (otherId.equals(oldId)) continue;
                        String otherName = pref.getString("name_for_" + otherId, "");
                        if (otherName.equalsIgnoreCase(newName)) {
                            nameInput.setError("This name is already used by another user");
                            return;
                        }
                    }

                    applyProfileEdit(oldId, newId, newName, ids);

                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    showProfileSwitcher(); // list dobara khol do taky naya naam/ID nazar aaye
                }));

        dialog.show();
    }


    private void applyProfileEdit(String oldId, String newId, String newName, ArrayList<String> ids) {
        SharedPreferences pref = getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        if (!newId.equals(oldId)) {

            editor.remove("high_score_" + newId);
            editor.remove("recent_score_" + newId);
            editor.remove("quiz_history_" + newId);


            if (pref.contains("high_score_" + oldId)) {
                editor.putInt("high_score_" + newId, pref.getInt("high_score_" + oldId, 0));
            }
            if (pref.contains("recent_score_" + oldId)) {
                editor.putInt("recent_score_" + newId, pref.getInt("recent_score_" + oldId, 0));
            }
            if (pref.contains("quiz_history_" + oldId)) {
                editor.putString("quiz_history_" + newId, pref.getString("quiz_history_" + oldId, ""));
            }


            editor.remove("high_score_" + oldId);
            editor.remove("recent_score_" + oldId);
            editor.remove("quiz_history_" + oldId);
            editor.remove("name_for_" + oldId);


            int pos = ids.indexOf(oldId);
            if (pos >= 0) ids.set(pos, newId);
            editor.putString("all_user_ids", TextUtils.join(",", ids));
        }

        editor.putString("name_for_" + newId, newName);


        if (oldId.equals(pref.getString("user_id", ""))) {
            editor.putString("user_id", newId);
            editor.putString("user_name", newName);
        }

        editor.apply();
    }


    private void goToMain() {
        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
    }
}