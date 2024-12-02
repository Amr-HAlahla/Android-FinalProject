package com.example.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainPageActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private CheckBox cbRememberMe;
    private Button btnLogin, btnSignup;
    private DatabaseHelper db;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load theme based on saved preferences
        preferences = getSharedPreferences("TaskManagerPrefs", MODE_PRIVATE);
        if (preferences.getBoolean("dark_mode", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        cbRememberMe = findViewById(R.id.cb_remember_me);
        btnLogin = findViewById(R.id.btn_login);
        btnSignup = findViewById(R.id.btn_signup);
        CheckBox cbShowPassword = findViewById(R.id.cb_show_password);
        Switch switchDarkMode = findViewById(R.id.switch_dark_mode); // Dark mode switch

        // Initialize database helper
        db = new DatabaseHelper(this);

        // Load saved preferences
        String savedEmail = preferences.getString("email", "");
        if (!savedEmail.isEmpty()) {
            etEmail.setText(savedEmail);
            cbRememberMe.setChecked(true);
        }
        switchDarkMode.setChecked(preferences.getBoolean("dark_mode", false));

        // Handle login
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainPageActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            } else {
                // Authenticate user
                if (db.authenticateUser(email, password)) {
                    Toast.makeText(MainPageActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                    // Save email if "Remember Me" is checked
                    SharedPreferences.Editor editor = preferences.edit();
                    if (cbRememberMe.isChecked()) {
                        editor.putString("email", email);
                    } else {
                        editor.remove("email");
                    }

                    // Save current logged-in user
                    editor.putString("logged_in_user", email);
                    editor.apply();

                    // Navigate to Home Page
                    Intent intent = new Intent(MainPageActivity.this, HomePageActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainPageActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle Sign-Up
        btnSignup.setOnClickListener(v -> {
            Intent intent = new Intent(MainPageActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Toggle password visibility
        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        // Handle dark mode toggle
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }
}
