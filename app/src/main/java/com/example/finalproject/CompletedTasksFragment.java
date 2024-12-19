package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CompletedTasksFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private DatabaseHelper db;
    private List<Task> tasks;
    private TextView emptyStateText;

    public CompletedTasksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_completed_tasks, container, false);

        // Initialize views and database
        recyclerView = rootView.findViewById(R.id.recycler_completed_tasks);
        emptyStateText = rootView.findViewById(R.id.text_empty_state);
        db = new DatabaseHelper(requireContext());

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Check dark mode preference and apply it
        loadDarkModePreference();

        // Load tasks for the logged-in user
        loadTasks();

        return rootView;
    }

    private void loadDarkModePreference() {
        SharedPreferences preferences = requireContext().getSharedPreferences("TaskManagerPrefs", Context.MODE_PRIVATE);
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);

        // Set the theme based on the saved preference
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void loadTasks() {
        String loggedInUserEmail = getLoggedInUserEmail();

        if (loggedInUserEmail.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        // Fetch completed tasks for the current user
        tasks = db.getCompletedTasksForUser(loggedInUserEmail);

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
        }
    }

    @Override
    public void onTaskCompleted(Task task, int position) {
        // Toggle completion status
        int newStatus = task.isCompleted() ? 1 : 0;
        boolean updated = db.updateTaskCompletionStatus(task.getId(), newStatus);

        if (updated) {
            tasks.remove(position);
            recyclerView.post(() -> {
                if (adapter != null) {
                    adapter.notifyItemRemoved(position);
                }
            });
            Toast.makeText(requireContext(), "Task marked as incomplete", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Failed to update task status", Toast.LENGTH_SHORT).show();
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

            // If no tasks left, show the "No Tasks" message
            if (tasks.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyStateText.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(requireContext(), "Failed to delete task", Toast.LENGTH_SHORT).show();
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
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{}); // Add recipient email addresses if needed
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Task Reminder: " + taskTitle);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "No email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getLoggedInUserEmail() {
        // Retrieve the logged-in user's email from SharedPreferences
        SharedPreferences preferences = requireContext().getSharedPreferences("TaskManagerPrefs", Context.MODE_PRIVATE);
        return preferences.getString("logged_in_user", "");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
