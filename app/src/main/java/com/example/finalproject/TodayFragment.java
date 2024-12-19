package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TodayFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private DatabaseHelper db;
    private TaskAdapter adapter;
    private List<Task> tasks;
    private RecyclerView recyclerView;
    private TextView noTasksTextView;
    private Button btnSortByPriority;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_today, container, false);

        db = new DatabaseHelper(requireContext());
        recyclerView = rootView.findViewById(R.id.recycler_today);
        noTasksTextView = rootView.findViewById(R.id.text_no_tasks);
        btnSortByPriority = rootView.findViewById(R.id.button_sort);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadTasks();

        // Set up the sort button to sort by priority
        btnSortByPriority.setOnClickListener(v -> sortTasksByPriority());

        // Load the user's dark mode preference
        loadDarkModePreference();

        return rootView;
    }

    private void loadTasks() {
        Calendar calendar = Calendar.getInstance();
        String todayDate = DateFormat.format("yyyy-MM-dd", calendar).toString();

        SharedPreferences preferences = requireContext().getSharedPreferences("TaskManagerPrefs", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("logged_in_user", "");

        tasks = db.getTasksForTodayAndUser(todayDate, userEmail);

        if (tasks != null && !tasks.isEmpty()) {
            if (adapter == null) {
                // Initialize the adapter with Context, tasks, and listener
                adapter = new TaskAdapter(requireContext(), tasks, this);
                recyclerView.setAdapter(adapter);
            } else {
                // Update the adapter's data set
                adapter.updateTasks(tasks);
            }
            noTasksTextView.setVisibility(View.GONE);
        } else {
            if (adapter != null) {
                adapter.clearTasks();
            }
            noTasksTextView.setVisibility(View.VISIBLE);
        }
    }

    private void sortTasksByPriority() {
        // Fetch the latest tasks from the database
        Calendar calendar = Calendar.getInstance();
        String todayDate = DateFormat.format("yyyy-MM-dd", calendar).toString();

        SharedPreferences preferences = requireContext().getSharedPreferences("TaskManagerPrefs", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("logged_in_user", "");

        // Get the latest tasks for today
        tasks = db.getTasksForTodayAndUser(todayDate, userEmail);

        if (tasks != null && !tasks.isEmpty()) {
            // Define the priority order: High > Medium > Low
            Collections.sort(tasks, new Comparator<Task>() {
                @Override
                public int compare(Task t1, Task t2) {
                    return getPriorityValue(t2.getPriority()) - getPriorityValue(t1.getPriority());
                }

                private int getPriorityValue(String priority) {
                    switch (priority.toLowerCase()) {
                        case "high":
                            return 3;
                        case "medium":
                            return 2;
                        case "low":
                            return 1;
                        default:
                            return 0;
                    }
                }
            });

            // Update the adapter's data set
            if (adapter != null) {
                adapter.updateTasks(tasks);
                adapter.notifyDataSetChanged();
            }

            Toast.makeText(requireContext(), "Tasks sorted by priority", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "No tasks to sort", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTaskCompleted(Task task, int position) {
        // Toggle completion status
        int newStatus = task.isCompleted() ? 1 : 0;
        boolean updated = db.updateTaskCompletionStatus(task.getId(), newStatus);

        if (updated) {
            tasks.get(position).setCompletionStatus(newStatus);
            // Safely notify the adapter to prevent IllegalStateException
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
        });
        dialog.show(getChildFragmentManager(), "EditTaskDialog");
    }

    @Override
    public void onDeleteClick(Task task, int position) {
        boolean deleted = db.deleteTask(task.getId());
        if (deleted) {
            tasks.remove(position);
            adapter.notifyItemRemoved(position);
            Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show();
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

        // Create an Intent to share the task details via email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{}); // Add recipient email addresses if needed
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Task Reminder: " + taskTitle);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Task Title: " + taskTitle + "\n" +
                "Description: " + taskDescription + "\n" +
                "Due Date: " + taskDueDate + " " + taskDueTime + "\n" +
                "Priority: " + task.getPriority());

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "No email client installed.", Toast.LENGTH_SHORT).show();
        }
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
}
