package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AllTasksFragment extends Fragment implements AllTasksAdapter.AllTasksListener {

    private RecyclerView recyclerView;
    private AllTasksAdapter adapter;
    private DatabaseHelper db;
    private List<Task> tasks;
    private TextView noTasksTextView;
    private Button fetchButton, toggleViewButton;
    private ProgressBar progressBar;
    private boolean isGroupedView = false;

    public AllTasksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_all_tasks, container, false);

        // Initialize views and database
        recyclerView = rootView.findViewById(R.id.recycler_all_tasks);
        noTasksTextView = rootView.findViewById(R.id.text_no_tasks);
        fetchButton = rootView.findViewById(R.id.button_fetch_data);
        toggleViewButton = rootView.findViewById(R.id.button_toggle_view);
        progressBar = rootView.findViewById(R.id.progress_bar);
        db = new DatabaseHelper(requireContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadDarkModePreference();

        fetchButton.setOnClickListener(v -> {
            String apiUrl = "https://mocki.io/v1/f02d98c4-e32c-4626-87b1-6912fd4bea2b";
            new ConnectionAsyncTask(AllTasksFragment.this).execute(apiUrl);
        });

        toggleViewButton.setOnClickListener(v -> {
            isGroupedView = !isGroupedView;
            if (isGroupedView) {
                loadGroupedTasks();
                toggleViewButton.setText("Show Normal View");
            } else {
                loadTasks();
                toggleViewButton.setText("Show Grouped View");
            }
        });

        loadTasks();

        return rootView;
    }

    private void loadDarkModePreference() {
        SharedPreferences preferences = requireContext().getSharedPreferences("TaskManagerPrefs", Context.MODE_PRIVATE);
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public void loadTasks() {
        String loggedInUserEmail = getLoggedInUserEmail();

        if (loggedInUserEmail.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            noTasksTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        // Fetch tasks for the current user
        tasks = db.getAllTasksForUser(loggedInUserEmail);

        if (tasks != null && !tasks.isEmpty()) {
            List<Object> items = new ArrayList<>(tasks); // Convert List<Task> to List<Object>

            if (adapter == null) {
                adapter = new AllTasksAdapter(requireContext(), items, this);
                recyclerView.setAdapter(adapter);
            } else {
                // Update the adapter's data set
                adapter.updateTasks(items);
            }
            noTasksTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            if (adapter != null) {
                adapter.updateTasks(new ArrayList<>());
            }
            noTasksTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void loadGroupedTasks() {
        String loggedInUserEmail = getLoggedInUserEmail();

        if (loggedInUserEmail.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            noTasksTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        // Fetch grouped tasks from the database
        Map<String, List<Task>> groupedTasks = db.getTasksGroupedByDay(loggedInUserEmail);

        // Sort tasks within each group by priority
        for (Map.Entry<String, List<Task>> entry : groupedTasks.entrySet()) {
            entry.getValue().sort((task1, task2) -> {
                int priority1 = getPriorityValue(task1.getPriority());
                int priority2 = getPriorityValue(task2.getPriority());
                return Integer.compare(priority1, priority2);
            });
        }

        // Update the adapter with the grouped and sorted tasks
        if (!groupedTasks.isEmpty()) {
            adapter.updateGroupedTasks(groupedTasks);
            noTasksTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            adapter.updateTasks(new ArrayList<>());
            noTasksTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    // Helper method to map priority to numeric values
    private int getPriorityValue(String priority) {
        if (priority == null) return 2;
        switch (priority.toLowerCase()) {
            case "high":
                return 1;
            case "medium":
                return 2;
            case "low":
                return 3;
            default:
                return 2;
        }
    }


    private String getLoggedInUserEmail() {
        // Retrieve the logged-in user's email from SharedPreferences
        SharedPreferences preferences = requireContext().getSharedPreferences("TaskManagerPrefs", Context.MODE_PRIVATE);
        return preferences.getString("logged_in_user", "");
    }

    public Button getFetchButton() {
        return fetchButton;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    @Override
    public void onTaskCompleted(Task task, int position) {
        int taskIndex = -1;
        int currentIndex = 0;

        for (Object item : adapter.getItems()) {
            if (item instanceof Task) {
                if (currentIndex == position) {
                    taskIndex = tasks.indexOf(item);
                    break;
                }
            }
            currentIndex++;
        }

        // If the task index is valid, update the task
        if (taskIndex != -1) {
            int newStatus = task.isCompleted() ? 1 : 0;
            boolean updated = db.updateTaskCompletionStatus(task.getId(), newStatus);

            if (updated) {
                tasks.get(taskIndex).setCompletionStatus(newStatus);
                recyclerView.post(() -> adapter.notifyItemChanged(position));

                String message = newStatus == 1 ? "Task marked as completed" : "Task marked as incomplete";
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Failed to update task status", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Task not found in the list", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onTaskClick(Task task) {
        // Show the task details in a Dialog
        TaskDetailsDialogFragment dialog = new TaskDetailsDialogFragment(task);
        dialog.show(getParentFragmentManager(), "TaskDetailsDialog");
    }

    @Override
    public void onEditClick(Task task, int position) {
        // Open the EditTaskDialog to edit the task
        EditTaskDialog dialog = new EditTaskDialog(task, updatedTask -> {
            tasks.set(position, updatedTask);
            adapter.notifyItemChanged(position);  // Notify RecyclerView about the change
            Toast.makeText(requireContext(), "Task updated successfully!", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getChildFragmentManager(), "EditTaskDialog");
    }

    @Override
    public void onDeleteClick(Task task, int position) {
        boolean deleted = db.deleteTask(task.getId());

        if (deleted) {
            tasks.remove(position);
            adapter.notifyItemRemoved(position);
            Toast.makeText(requireContext(), "Task deleted successfully!", Toast.LENGTH_SHORT).show();

            if (tasks.isEmpty()) {
                noTasksTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
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

        // Create an Intent to share the task details via email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{});
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
}
