package com.example.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.finalproject.databinding.FragmentLogoutBinding;
import com.google.android.material.button.MaterialButton;

public class LogoutFragment extends Fragment implements ConfirmationDialog.ConfirmationListener {

    private FragmentLogoutBinding binding;
    private MaterialButton btnLogout;
    private DatabaseHelper db;

    public LogoutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout using View Binding
        binding = FragmentLogoutBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        // Initialize DatabaseHelper
        db = new DatabaseHelper(getContext());

        // Load dark mode preference and apply the theme
        loadDarkModePreference();

        // Initialize views
        btnLogout = binding.btnLogout;

        // Set up Logout button click listener
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        return rootView;
    }

    /**
     * Displays the confirmation dialog for logout.
     */
    private void showLogoutConfirmationDialog() {
        ConfirmationDialog dialog = ConfirmationDialog.newInstance(
                getString(R.string.logout_confirmation_title),
                getString(R.string.logout_confirmation_message)
        );
        dialog.show(getParentFragmentManager(), "ConfirmationDialog");
    }

    /**
     * Performs the logout operation by clearing user data and redirecting to the login screen.
     */
    private void performLogout() {
        // Clear user session or authentication data
        clearUserSession();

        // Redirect to the login screen
        Intent intent = new Intent(getActivity(), MainPageActivity.class);
        // Clear the activity stack to prevent the user from returning by pressing the back button
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();  // Close the current activity
        Toast.makeText(getContext(), getString(R.string.logout_successful), Toast.LENGTH_SHORT).show();
    }

    /**
     * Clears the user's session data from SharedPreferences.
     */
    private void clearUserSession() {
        SharedPreferences preferences = getContext().getSharedPreferences("TaskManagerPrefs", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();  // Remove all stored data
        editor.apply();
    }

    @Override
    public void onConfirm() {
        performLogout();
    }

    @Override
    public void onCancel() {
        Toast.makeText(getContext(), getString(R.string.logout_cancelled), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Nullify binding to prevent memory leaks
        binding = null;
    }

    // Load the dark mode preference and set the theme accordingly
    private void loadDarkModePreference() {
        SharedPreferences preferences = getContext().getSharedPreferences("TaskManagerPrefs", getContext().MODE_PRIVATE);
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
