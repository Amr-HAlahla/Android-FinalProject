// NewTaskFragment.java
package com.example.finalproject;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewTaskFragment extends Fragment {

    private TextInputEditText etTitle, etDescription, etDueDate, etDueTime;
    private TextInputLayout tilTitle, tilDescription, tilDueDate, tilDueTime;
    private ArrayAdapter<CharSequence> priorityAdapter;
    private Spinner spPriority;
    private MaterialButton btnSave, btnCancel;
    private String selectedPriority = "Medium"; // Default priority
    private DatabaseHelper db;

    public NewTaskFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using View Binding
        View rootView = inflater.inflate(R.layout.fragment_new_task, container, false);

        // Initialize views
        tilTitle = rootView.findViewById(R.id.til_title);
        etTitle = rootView.findViewById(R.id.et_title);
        tilDescription = rootView.findViewById(R.id.til_description);
        etDescription = rootView.findViewById(R.id.et_description);
        tilDueDate = rootView.findViewById(R.id.til_due_date);
        etDueDate = rootView.findViewById(R.id.et_due_date);
        tilDueTime = rootView.findViewById(R.id.til_due_time);
        etDueTime = rootView.findViewById(R.id.et_due_time);
        spPriority = rootView.findViewById(R.id.sp_priority);
        btnSave = rootView.findViewById(R.id.btn_save);
        btnCancel = rootView.findViewById(R.id.btn_cancel);

        db = new DatabaseHelper(getContext());

        setupPrioritySpinner();

        etDueDate.setOnClickListener(v -> showMaterialDatePicker());

        etDueTime.setOnClickListener(v -> showTimePicker());

        btnSave.setOnClickListener(v -> saveTask());

        btnCancel.setOnClickListener(v -> clearFields());

        // Load dark mode preference and apply the theme
        loadDarkModePreference();

        return rootView;
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

    private void setupPrioritySpinner() {
        // Load priorities into the spinner from arrays.xml
        priorityAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.priority_levels, android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(priorityAdapter);

        spPriority.setSelection(priorityAdapter.getPosition(selectedPriority));

        spPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedPriority = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Default priority remains
            }
        });
    }

    private void showMaterialDatePicker() {
        Calendar calendar = Calendar.getInstance();
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_due_date))
                .setSelection(calendar.getTimeInMillis())
                .build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTimeInMillis(selection);

            // Use the yyyy-MM-dd date format
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            etDueDate.setText(dateFormat.format(selectedDate.getTime()));
        });

        materialDatePicker.show(getChildFragmentManager(), materialDatePicker.toString());
    }


    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minuteOfHour) -> {
                    // Use a user-friendly time format
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    etDueTime.setText(time);
                }, hour, minute, DateFormat.is24HourFormat(getContext()));

        timePickerDialog.show();
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim(); // Now in yyyy-MM-dd
        String dueTime = etDueTime.getText().toString().trim();

        // Validate inputs and show errors if necessary
        boolean valid = true;

        if (TextUtils.isEmpty(title)) {
            tilTitle.setError(getString(R.string.enter_title));
            valid = false;
        } else {
            tilTitle.setError(null);
        }

        if (TextUtils.isEmpty(description)) {
            tilDescription.setError(getString(R.string.enter_description));
            valid = false;
        } else {
            tilDescription.setError(null);
        }

        if (TextUtils.isEmpty(dueDate)) {
            tilDueDate.setError(getString(R.string.enter_due_date));
            valid = false;
        } else {
            tilDueDate.setError(null);
        }

        if (TextUtils.isEmpty(dueTime)) {
            tilDueTime.setError(getString(R.string.enter_due_time));
            valid = false;
        } else {
            tilDueTime.setError(null);
        }

        if (!valid) {
            Toast.makeText(getContext(), getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate future date/time
        try {
            SimpleDateFormat fullDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Calendar selectedDateTime = Calendar.getInstance();
            selectedDateTime.setTime(fullDateTimeFormat.parse(dueDate + " " + dueTime));

            if (selectedDateTime.before(Calendar.getInstance())) {
                Toast.makeText(getContext(), getString(R.string.select_future_datetime), Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), getString(R.string.invalid_date_time), Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve the logged-in user's email
        String userEmail = getLoggedInUserEmail();

        // Save the task in the database
        boolean isSaved = db.insertTask(title, description, dueDate, dueTime, selectedPriority, 0, "", userEmail);

        if (isSaved) {
            Toast.makeText(getContext(), getString(R.string.task_saved_successfully), Toast.LENGTH_SHORT).show();
            showAnimation(); // Show the animation on successful save
            clearFields();
        } else {
            Toast.makeText(getContext(), getString(R.string.failed_to_save_task), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAnimation() {
        ImageView animationGif = getView().findViewById(R.id.animationGif);
        if (animationGif != null) {
            Glide.with(requireContext())
                    .asGif()
                    .load(R.drawable.task_saved2) // Replace with your actual GIF resource
                    .into(animationGif);

            animationGif.setVisibility(View.VISIBLE);

            // Hide the animation after 2 seconds
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (animationGif != null) {
                    animationGif.setVisibility(View.GONE);
                }
            }, 3000);
        }
    }


    private void clearFields() {
        etTitle.setText("");
        etDescription.setText("");
        etDueDate.setText("");
        etDueTime.setText("");
        spPriority.setSelection(priorityAdapter.getPosition("Medium"));
        selectedPriority = "Medium";
    }

    private String getLoggedInUserEmail() {
        // Retrieve the logged-in user's email from SharedPreferences
        SharedPreferences preferences = getContext().getSharedPreferences("TaskManagerPrefs", getContext().MODE_PRIVATE);
        return preferences.getString("logged_in_user", "");
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
