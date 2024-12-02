package com.example.finalproject;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewTaskFragment extends Fragment {

    private TextInputEditText etDueDate;
    private Spinner spPriority;
    private Button btnSave;
    private TextInputEditText etTitle, etDescription;
    private String selectedPriority = "Low"; // Default priority

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
        spPriority = rootView.findViewById(R.id.sp_priority);
        btnSave = rootView.findViewById(R.id.btn_save);

        // Set up the Due Date picker (MaterialDatePicker)
        etDueDate.setOnClickListener(v -> showMaterialDatePicker());

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
        // Initialize Calendar instance
        Calendar calendar = Calendar.getInstance();
        long today = calendar.getTimeInMillis();

        // Set up the Material Date Picker
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Due Date")
                .setSelection(today)  // Default to today
                .setCalendarConstraints(new CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointForward.from(today))  // Only allow future dates
                        .build())
                .build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTimeInMillis(selection);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            etDueDate.setText(dateFormat.format(selectedDate.getTime()));
        });

        materialDatePicker.show(getChildFragmentManager(), materialDatePicker.toString());
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim();

        // Save the task (You can implement saving to a database or list here)
        // For now, we can log or display the task details
        System.out.println("Title: " + title);
        System.out.println("Description: " + description);
        System.out.println("Due Date: " + dueDate);
        System.out.println("Priority: " + selectedPriority);

        // Optionally, show a Toast or notification to confirm the task is saved
        Toast.makeText(getContext(), "Task saved successfully!", Toast.LENGTH_SHORT).show();
    }
}
