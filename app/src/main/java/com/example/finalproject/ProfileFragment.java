package com.example.finalproject;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.example.finalproject.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private DatabaseHelper db;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout using View Binding
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        db = new DatabaseHelper(getContext());

        // Fetch logged in user email
        String loggedInUserEmail = getLoggedInUserEmail();

        // Fetch and display the user's current details (First Name, Last Name, Email)
        displayUserDetails(loggedInUserEmail);

        // Set up Save button click listener
        binding.btnSave.setOnClickListener(v -> {
            String currentPassword = binding.etCurrentPassword.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (validateInputs(currentPassword, email, password)) {
                // Check if the current password matches the stored password
                if (db.authenticateUser(loggedInUserEmail, currentPassword)) {
                    // Current password is correct, update user profile
                    boolean isUpdated = updateUserProfile(loggedInUserEmail, email, password);
                    if (isUpdated) {
                        // Update SharedPreferences if email changed
                        if (!email.equals(loggedInUserEmail)) {
                            updateLoggedInUserEmail(email);
                        }
                        showSuccessAnimation();
                        Toast.makeText(getContext(), getString(R.string.profile_updated_successfully), Toast.LENGTH_SHORT).show();
                        clearFields();
                    } else {
                        Toast.makeText(getContext(), getString(R.string.failed_to_update_profile), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    binding.tilCurrentPassword.setError(getString(R.string.incorrect_current_password));
                }
            }
        });

        // Load dark mode preference and apply the theme
        loadDarkModePreference();

        return rootView;
    }

    private void displayUserDetails(String loggedInUserEmail) {
        // Fetch user details from database
        Cursor cursor = db.getUserInfo(loggedInUserEmail);

        if (cursor != null && cursor.moveToFirst()) {
            // Display user details in the UI
            String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIRST_NAME));
            String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_NAME));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL));

            binding.etFirstName.setText(firstName);
            binding.etLastName.setText(lastName);
            binding.etEmail.setText(email);

            cursor.close();
        }
    }

    private boolean validateInputs(String currentPassword, String email, String password) {
        boolean isValid = true;

        if (currentPassword.isEmpty()) {
            binding.tilCurrentPassword.setError(getString(R.string.current_password_required));
            isValid = false;
        } else {
            binding.tilCurrentPassword.setError(null);
        }

        if (email.isEmpty() || !isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.enter_valid_email));
            isValid = false;
        } else {
            binding.tilEmail.setError(null);
        }

        if (password.isEmpty() || !isValidPassword(password)) {
            binding.tilNewPassword.setError(getString(R.string.password_requirements));
            isValid = false;
        } else {
            binding.tilNewPassword.setError(null);
        }

        return isValid;
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
        boolean isEmailUpdated = false;
        boolean isPasswordUpdated = false;

        // Update email if it has changed
        if (!email.equals(loggedInUserEmail)) {
            isEmailUpdated = db.updateUserEmail(loggedInUserEmail, email);
            if (isEmailUpdated) {
                // Update the email in the tasks for this user
                db.updateUserTasksEmail(loggedInUserEmail, email);
            }
        }
        // Update password if it has changed
        if (!password.isEmpty()) {
            isPasswordUpdated = db.updateUserPassword(email, password);
        }

        return isEmailUpdated || isPasswordUpdated;
    }

    private void showSuccessAnimation() {
        // Make the animation visible
        binding.successAnimationGif.setVisibility(View.VISIBLE);

        // Load GIF using Glide
        Glide.with(this)
                .asGif()
                .load(R.drawable.success_animation)
                .into(binding.successAnimationGif);

        // Hide the animation after 1.5 seconds
        new Handler().postDelayed(() -> binding.successAnimationGif.setVisibility(View.GONE), 1500);
    }

    private String getLoggedInUserEmail() {
        SharedPreferences preferences = getContext().getSharedPreferences("TaskManagerPrefs", getContext().MODE_PRIVATE);
        return preferences.getString("logged_in_user", "");
    }

    private void updateLoggedInUserEmail(String newEmail) {
        SharedPreferences preferences = getContext().getSharedPreferences("TaskManagerPrefs", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("logged_in_user", newEmail);
        editor.apply();
    }

    private void clearFields() {
        binding.etCurrentPassword.setText("");
        binding.etPassword.setText("");
    }

    /**
     * Load the dark mode preference and apply the theme accordingly.
     */
    private void loadDarkModePreference() {
        SharedPreferences preferences = getContext().getSharedPreferences("TaskManagerPrefs", getContext().MODE_PRIVATE);
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Nullify binding to prevent memory leaks
        binding = null;
    }
}
