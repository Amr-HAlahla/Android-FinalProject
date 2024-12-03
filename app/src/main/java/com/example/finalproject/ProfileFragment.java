package com.example.finalproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import android.util.Patterns;

public class ProfileFragment extends Fragment {

    private EditText etCurrentPassword, etEmail, etPassword;
    private Button btnSave;
    private DatabaseHelper db;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        etCurrentPassword = rootView.findViewById(R.id.et_current_password);
        etEmail = rootView.findViewById(R.id.et_email);
        etPassword = rootView.findViewById(R.id.et_password);
        btnSave = rootView.findViewById(R.id.btn_save);

        db = new DatabaseHelper(getContext());

        // Set up Save button click listener
        btnSave.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInputs(currentPassword, email, password)) {
                // Check if the current password matches the stored password
                String loggedInUserEmail = getLoggedInUserEmail();
                if (db.authenticateUser(loggedInUserEmail, currentPassword)) {
                    // Current password is correct, update user profile
                    boolean isUpdated = updateUserProfile(loggedInUserEmail, email, password);
                    if (isUpdated) {
                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Incorrect current password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    private boolean validateInputs(String currentPassword, String email, String password) {
        if (currentPassword.isEmpty()) {
            Toast.makeText(getContext(), "Current password is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (email.isEmpty() || !isValidEmail(email)) {
            Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.isEmpty() || !isValidPassword(password)) {
            Toast.makeText(getContext(), "Password must be 6-12 characters and include at least one number, one lowercase letter, and one uppercase letter", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();  // Check if email is valid
    }

    private boolean isValidPassword(String password) {
        // Ensure password is between 6 and 12 characters and contains at least one uppercase letter, one lowercase letter, and one digit
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{6,12}$";
        return password.matches(passwordPattern);
    }

    private boolean updateUserProfile(String loggedInUserEmail, String email, String password) {
        // If email and/or password are updated, update them in the database
        boolean isEmailUpdated = false;
        boolean isPasswordUpdated = false;

        // Update email if it has changed
        if (!email.equals(loggedInUserEmail)) {
            isEmailUpdated = db.updateUserEmail(loggedInUserEmail, email);
        }

        // Update password if it has changed
        if (!password.isEmpty()) {
            isPasswordUpdated = db.updateUserPassword(loggedInUserEmail, password);
        }

        return isEmailUpdated || isPasswordUpdated;
    }

    private String getLoggedInUserEmail() {
        SharedPreferences preferences = getContext().getSharedPreferences("TaskManagerPrefs", getContext().MODE_PRIVATE);
        return preferences.getString("logged_in_user", "");
    }
}
