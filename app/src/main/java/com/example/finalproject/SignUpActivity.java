package com.example.finalproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SignUpActivity extends AppCompatActivity {

    private EditText etEmail, etFirstName, etLastName, etPassword, etConfirmPassword;
    private Button btnRegister, btnBack;
    private DatabaseHelper db;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = getSharedPreferences("TaskManagerPrefs", MODE_PRIVATE);
        if (preferences.getBoolean("dark_mode", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        btnBack = findViewById(R.id.btn_back);
        CheckBox cbShowPassword = findViewById(R.id.cb_show_password);

        // Initialize database helper
        db = new DatabaseHelper(this);

        // Handle registration
        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (validateInput(email, firstName, lastName, password, confirmPassword)) {
                if (db.insertUser(email, firstName, lastName, password)) {
                    Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, "Error: User already exists", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle back button
        btnBack.setOnClickListener(v -> finish());

        // Toggle password visibility
        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etPassword.setSelection(etPassword.getText().length());
        });
    }


    private boolean validateInput(String email, String firstName, String lastName, String password, String confirmPassword) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (firstName.length() < 3 || firstName.length() > 20) {
            Toast.makeText(this, "First name must be 3-20 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (lastName.length() < 5 || lastName.length() > 20) {
            Toast.makeText(this, "Last name must be 5-20 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6 || password.length() > 12 ||
                !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")) {
            Toast.makeText(this, "Password must be 6-12 characters and include an uppercase letter, a lowercase letter, and a number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
