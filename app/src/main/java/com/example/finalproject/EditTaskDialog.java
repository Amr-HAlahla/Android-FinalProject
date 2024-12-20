// SetNotificationDialog.java
package com.example.finalproject;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditTaskDialog extends AppCompatDialogFragment {

    private TextInputEditText etTitle, etDescription, etDueDate, etDueTime;
    private TextInputLayout tilTitle, tilDescription, tilDueDate, tilDueTime;
    private Spinner spPriority;
    private MaterialButton btnSave, btnCancel;
    private Task task;
    private DatabaseHelper db;
    private EditTaskListener listener;

    public interface EditTaskListener {
        void onTaskUpdated(Task updatedTask);
    }

    public EditTaskDialog(Task task, EditTaskListener listener) {
        this.task = task;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // Inflate the layout for the dialog
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_task, null);
        dialog.setContentView(view);

        // Initialize views
        tilTitle = view.findViewById(R.id.til_title);
        etTitle = view.findViewById(R.id.et_title);
        tilDescription = view.findViewById(R.id.til_description);
        etDescription = view.findViewById(R.id.et_description);
        tilDueDate = view.findViewById(R.id.til_due_date);
        etDueDate = view.findViewById(R.id.et_due_date);
        tilDueTime = view.findViewById(R.id.til_due_time);
        etDueTime = view.findViewById(R.id.et_due_time);
        spPriority = view.findViewById(R.id.sp_priority);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);

        db = new DatabaseHelper(getContext());

        // Pre-populate the fields with the current task data
        etTitle.setText(task.getTitle());
        etDescription.setText(task.getDescription());
        etDueDate.setText(task.getDueDate());
        etDueTime.setText(task.getDueTime());

        // Set up the Priority Spinner
        setupPrioritySpinner();

        // Set up the Due Date picker
        etDueDate.setOnClickListener(v -> showMaterialDatePicker());

        // Set up the Due Time picker
        etDueTime.setOnClickListener(v -> showTimePicker());

        // Set up the Save button to save the updated task
        btnSave.setOnClickListener(v -> saveUpdatedTask());

        // Set up the Cancel button to dismiss the dialog without saving
        btnCancel.setOnClickListener(v -> dismiss());

        return dialog;
    }

    private void setupPrioritySpinner() {
        // Load priorities into the spinner
        String[] priorities = getResources().getStringArray(R.array.priority_levels);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(adapter);

        // Set the spinner to the current priority
        int priorityIndex = getPriorityIndex(task.getPriority(), priorities);
        spPriority.setSelection(priorityIndex);
    }

    private int getPriorityIndex(String priority, String[] priorities) {
        for (int i = 0; i < priorities.length; i++) {
            if (priorities[i].equals(priority)) {
                return i;
            }
        }
        return 0;
    }

    private void saveUpdatedTask() {
        // Get updated data from the input fields
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim();
        String dueTime = etDueTime.getText().toString().trim();
        String priority = spPriority.getSelectedItem().toString();

        // Validate the inputs (ensure they are not empty)
        boolean valid = true;

        if (title.isEmpty()) {
            tilTitle.setError(getString(R.string.enter_title));
            valid = false;
        } else {
            tilTitle.setError(null);
        }

        if (description.isEmpty()) {
            tilDescription.setError(getString(R.string.enter_description));
            valid = false;
        } else {
            tilDescription.setError(null);
        }

        if (dueDate.isEmpty()) {
            tilDueDate.setError(getString(R.string.enter_due_date));
            valid = false;
        } else {
            tilDueDate.setError(null);
        }

        if (dueTime.isEmpty()) {
            tilDueTime.setError(getString(R.string.enter_due_time));
            valid = false;
        } else {
            tilDueTime.setError(null);
        }

        if (!valid) {
            return;
        }

        // Update the task object
        task.setTitle(title);
        task.setDescription(description);
        task.setDueDate(dueDate);
        task.setDueTime(dueTime);
        task.setPriority(priority);

        // Save the updated task in the database
        boolean isUpdated = db.updateTask(task);
        if (isUpdated) {
            // Notify listener and dismiss dialog
            listener.onTaskUpdated(task);
            Toast.makeText(getContext(), getString(R.string.task_updated_successfully), Toast.LENGTH_SHORT).show();
            dismiss();
        } else {
            Toast.makeText(getContext(), getString(R.string.failed_to_update_task), Toast.LENGTH_SHORT).show();
        }
    }

    private void showMaterialDatePicker() {
        Calendar calendar = Calendar.getInstance();
        long today = calendar.getTimeInMillis();

        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_due_date))
                .setSelection(today) // Default to today
                .build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTimeInMillis(selection);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
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
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    etDueTime.setText(time);
                }, hour, minute, DateFormat.is24HourFormat(getContext()));

        timePickerDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Dialog dialog = getDialog();
        if (dialog != null) {
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(params);
        }
    }
}
