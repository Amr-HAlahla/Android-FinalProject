package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

public class HomePageActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        ConfirmationDialog.ConfirmationListener {

    private DrawerLayout drawer;
    private DatabaseHelper db;
    private SharedPreferences preferences;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Initialize database helper and shared preferences
        db = new DatabaseHelper(this);
        preferences = getSharedPreferences("TaskManagerPrefs", MODE_PRIVATE);

        // Load dark mode preference and apply the theme
        loadDarkModePreference();

        // Set up toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up drawer layout and toggle
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Load user info in navigation drawer
        loadUserInfo(navigationView);

        // Show "Today" fragment by default
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new TodayFragment())
                    .commit();
            // Optionally, set the first item as checked
            navigationView.setCheckedItem(R.id.nav_today);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        switch (item.getItemId()) {
            case R.id.nav_today:
                selectedFragment = new TodayFragment();
                break;
            case R.id.nav_new_task:
                selectedFragment = new NewTaskFragment();
                break;
            case R.id.nav_all_tasks:
                selectedFragment = new AllTasksFragment();
                break;
            case R.id.nav_completed:
                selectedFragment = new CompletedTasksFragment();
                break;
            case R.id.nav_search:
                selectedFragment = new SearchFragment();
                break;
            case R.id.nav_profile:
                selectedFragment = new ProfileFragment();
                break;
            case R.id.nav_logout:
                showLogoutConfirmationDialog();
                break;
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLogoutConfirmationDialog() {
        ConfirmationDialog dialog = ConfirmationDialog.newInstance(
                getString(R.string.logout_confirmation_title),
                getString(R.string.logout_confirmation_message)
        );
        dialog.show(getSupportFragmentManager(), "ConfirmationDialog");
    }

    private void performLogout() {
        // Clear logged-in user and other session data
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("logged_in_user"); // Remove specific keys if necessary
        editor.apply();

        // Redirect to MainPageActivity (assumed to be the login screen)
        Intent intent = new Intent(HomePageActivity.this, MainPageActivity.class);
        // Clear the activity stack to prevent the user from returning by pressing the back button
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, getString(R.string.logout_successful), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConfirm() {
        performLogout();
    }

    @Override
    public void onCancel() {
        // Optionally, provide feedback that logout was canceled
        Toast.makeText(this, getString(R.string.logout_cancelled), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Load user info (name and email) in the navigation drawer
    @SuppressLint("Range")
    private void loadUserInfo(NavigationView navigationView) {
        // Get the logged-in user's email from SharedPreferences
        String userEmail = preferences.getString("logged_in_user", "");
        if (!userEmail.isEmpty()) {
            // Fetch user info from database using email
            String firstName = "";
            String lastName = "";
            Cursor cursor = db.getUserInfo(userEmail);
            if (cursor != null && cursor.moveToFirst()) {
                firstName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_FIRST_NAME));
                lastName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LAST_NAME));
            }
            if (cursor != null) {
                cursor.close();
            }

            // Find the TextViews in the navigation header and set user info
            View headerView = navigationView.getHeaderView(0);
            TextView navHeaderName = headerView.findViewById(R.id.nav_header_name);
            TextView navHeaderEmail = headerView.findViewById(R.id.nav_header_email);

            navHeaderName.setText(firstName + " " + lastName);
            navHeaderEmail.setText(userEmail);
        }
    }

    // Load the dark mode preference and set the theme accordingly
    private void loadDarkModePreference() {
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
