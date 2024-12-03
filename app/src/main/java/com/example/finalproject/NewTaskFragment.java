package com.example.finalproject;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewTaskFragment extends Fragment {

    private TextInputEditText etDueDate, etDueTime;
    private Spinner spPriority;
    private Button btnSave;
    private TextInputEditText etTitle, etDescription;
    private String selectedPriority = "Medium"; // Default priority
    private DatabaseHelper db;

    public NewTaskFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_new_task, container, false);

        // Initialize views
        etTitle = rootView.findViewById(R.id.et_title);
        etDescription = rootView.findViewById(R.id.et_description);
        etDueDate = rootView.findViewById(R.id.et_due_date);
        etDueTime = rootView.findViewById(R.id.et_due_time);
        spPriority = rootView.findViewById(R.id.sp_priority);
        btnSave = rootView.findViewById(R.id.btn_save);

        db = new DatabaseHelper(getContext());

        // Set up the Due Date picker (MaterialDatePicker)
        etDueDate.setOnClickListener(v -> showMaterialDatePicker());

        // Set up the Due Time picker
        etDueTime.setOnClickListener(v -> showTimePicker());

        // Set up the Priority Spinner (Drop-down menu)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.priority_levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(adapter);

        spPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedPriority = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        // Save Button functionality
        btnSave.setOnClickListener(v -> saveTask());

        return rootView;
    }

    private void showMaterialDatePicker() {
        Calendar calendar = Calendar.getInstance();
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Due Date")
                .setSelection(calendar.getTimeInMillis())
                .build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTimeInMillis(selection);

            // Use ISO 8601 format for consistent parsing
            SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            etDueDate.setText(isoDateFormat.format(selectedDate.getTime()));
        });

        materialDatePicker.show(getChildFragmentManager(), materialDatePicker.toString());
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minuteOfHour) -> {
                    // Use ISO 8601 time format with seconds
                    String time = String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minuteOfHour);
                    etDueTime.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);

        timePickerDialog.show();
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim();
        String dueTime = etDueTime.getText().toString().trim();

        // Validate future date/time
        try {
            SimpleDateFormat fullDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Calendar selectedDateTime = Calendar.getInstance();
            selectedDateTime.setTime(fullDateTimeFormat.parse(dueDate + " " + dueTime));

            if (selectedDateTime.before(Calendar.getInstance())) {
                Toast.makeText(getContext(), "Please select a future date and time", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Invalid date or time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Rest of the save task logic remains the same
        boolean isSaved = db.insertTask(title, description, dueDate, dueTime, selectedPriority, 0, "", getLoggedInUserEmail());

        if (isSaved) {
            Toast.makeText(getContext(), "Task saved successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to save task", Toast.LENGTH_SHORT).show();
        }
    }

    private String getLoggedInUserEmail() {
        // Retrieve the logged-in user's email from SharedPreferences
        SharedPreferences preferences = getContext().getSharedPreferences("TaskManagerPrefs", getContext().MODE_PRIVATE);
        return preferences.getString("logged_in_user", "");
    }
}
