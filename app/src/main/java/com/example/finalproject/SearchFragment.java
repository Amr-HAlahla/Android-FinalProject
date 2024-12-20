package com.example.finalproject;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private DatabaseHelper db;
    private EditText etSearchKeyword;
    private Button btnStartDate, btnEndDate, btnSearch;
    private String startDate, endDate;
    private List<Task> tasks;
    private TextView emptyStateText;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize views
        etSearchKeyword = rootView.findViewById(R.id.et_search_keyword);
        btnStartDate = rootView.findViewById(R.id.btn_start_date);
        btnEndDate = rootView.findViewById(R.id.btn_end_date);
        btnSearch = rootView.findViewById(R.id.btn_search);
        recyclerView = rootView.findViewById(R.id.recycler_search_results);
        emptyStateText = rootView.findViewById(R.id.text_empty_state);

        db = new DatabaseHelper(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set up listeners
        btnStartDate.setOnClickListener(v -> showMaterialDatePicker(true));
        btnEndDate.setOnClickListener(v -> showMaterialDatePicker(false));
        btnSearch.setOnClickListener(v -> searchTasks());

        loadDarkModePreference();

        return rootView;
    }

    private void showMaterialDatePicker(boolean isStartDate) {
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
            String formattedDate = dateFormat.format(selectedDate.getTime());

            if (isStartDate) {
                startDate = formattedDate;
                btnStartDate.setText("Start Date: " + formattedDate);
            } else {
                endDate = formattedDate;
                btnEndDate.setText("End Date: " + formattedDate);
            }
        });

        materialDatePicker.show(getChildFragmentManager(), materialDatePicker.toString());
    }

    private void searchTasks() {
        String keyword = etSearchKeyword.getText().toString().trim();

        // Ensure that at least one of the fields (keyword or date range) is provided
        if (keyword.isEmpty() && (startDate == null || endDate == null)) {
            Toast.makeText(requireContext(), "Please enter a search keyword or date range", Toast.LENGTH_SHORT).show();
            return;
        }

        String loggedInUserEmail = getLoggedInUserEmail();
        if (loggedInUserEmail.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        // Fetch tasks based on keyword and date range
        tasks = db.searchTasks(loggedInUserEmail, keyword, startDate, endDate);

        if (tasks != null && !tasks.isEmpty()) {
            if (adapter == null) {
                // Initialize the adapter with Context, tasks, and listener
                adapter = new TaskAdapter(requireContext(), tasks, this);
                recyclerView.setAdapter(adapter);
            } else {
                // Update the adapter's data set
                adapter.updateTasks(tasks);
            }
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        } else {
            if (adapter != null) {
                adapter.clearTasks();
            }
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            Toast.makeText(requireContext(), "No tasks found matching the criteria.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTaskClick(Task task, int position) {
        // Show the task details in a Dialog
        TaskDetailsDialogFragment dialog = new TaskDetailsDialogFragment(task);
        dialog.show(getParentFragmentManager(), "TaskDetailsDialog");
    }

    @Override
    public void onEditClick(Task task, int position) {
        // Open the EditTaskDialog to edit the task
        EditTaskDialog dialog = new EditTaskDialog(task, updatedTask -> {
            // After updating the task, refresh the list
            tasks.set(position, updatedTask);  // Update the task in the list
            adapter.notifyItemChanged(position);  // Notify RecyclerView about the change
            Toast.makeText(requireContext(), "Task updated successfully!", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getChildFragmentManager(), "EditTaskDialog");
    }

    @Override
    public void onDeleteClick(Task task, int position) {
        // Delete task from the database
        boolean deleted = db.deleteTask(task.getId());

        if (deleted) {
            // Remove the task from the list and notify RecyclerView
            tasks.remove(position);
            adapter.notifyItemRemoved(position);
            Toast.makeText(requireContext(), "Task deleted successfully!", Toast.LENGTH_SHORT).show();

            if (tasks.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyStateText.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(requireContext(), "Failed to delete task", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTaskCompleted(Task task, int position) {
        // Toggle completion status
        int newStatus = task.isCompleted() ? 1 : 0;
        boolean updated = db.updateTaskCompletionStatus(task.getId(), newStatus);

        if (updated) {
            tasks.get(position).setCompletionStatus(newStatus);
            recyclerView.post(() -> {
                if (adapter != null) {
                    adapter.notifyItemChanged(position);
                }
            });

            String message = newStatus == 1 ? "Task marked as completed" : "Task marked as incomplete";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Failed to update task status", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSetNotification(Task task, int position) {
        // Open the Notification Dialog for this task
        SetNotificationDialog dialog = new SetNotificationDialog(task);
        dialog.show(getChildFragmentManager(), "SetNotificationDialog");
    }

    @Override
    public void onShareEmailClick(Task task) {
        String taskTitle = task.getTitle();
        String taskDescription = task.getDescription();
        String taskDueDate = task.getDueDate();
        String taskDueTime = task.getDueTime();
        String taskPriority = task.getPriority();

        String emailBody = getString(R.string.share_email_body, taskTitle, taskDescription, taskDueDate, taskDueTime, taskPriority);

        // Create an Intent to share the task details via email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Task Reminder: " + taskTitle);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "No email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getLoggedInUserEmail() {
        SharedPreferences preferences = requireContext().getSharedPreferences("TaskManagerPrefs", Context.MODE_PRIVATE);
        return preferences.getString("logged_in_user", "");
    }

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
