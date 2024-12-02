package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;

public class HomePageActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ImageView welcomeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);
        welcomeImage = findViewById(R.id.welcome_image);

        setSupportActionBar(toolbar);

        // Setup ActionBarDrawerToggle with the correct strings
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState(); // Synchronize the state of the drawer toggle

        // Set up the default fragment (TodayFragment)
        loadFragment(new TodayFragment());

        // Set up Navigation Drawer click listener
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_today:
                    loadFragment(new TodayFragment());
                    break;
                case R.id.nav_new_task:
                    loadFragment(new NewTaskFragment());
                    break;
                case R.id.nav_all_tasks:
                    loadFragment(new AllTasksFragment());
                    break;
                case R.id.nav_completed:
                    loadFragment(new CompletedTasksFragment());
                    break;
                case R.id.nav_profile:
                    loadFragment(new ProfileFragment());
                    break;
                case R.id.nav_logout:
                    // Handle logout logic
                    startActivity(new Intent(HomePageActivity.this, MainPageActivity.class));
                    finish();  // Close the HomeActivity
                    break;
                default:
                    return false;
            }
            drawerLayout.closeDrawers();  // Close the drawer after selecting an item
            return true;
        });

        // Show the welcome image with animation when activity starts
        welcomeImage.setVisibility(View.VISIBLE);
        welcomeImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        welcomeImage.setVisibility(View.INVISIBLE);
    }

    // Method to load a fragment into the FrameLayout
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.addToBackStack(null); // Add the fragment transaction to back stack
        transaction.commit();
    }
}
