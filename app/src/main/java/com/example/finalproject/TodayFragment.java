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

        return rootView;
    }

    private void loadTasks() {
        Calendar calendar = Calendar.getInstance();
        String todayDate = DateFormat.format("yyyy-MM-dd", calendar).toString();

        SharedPreferences preferences = requireContext().getSharedPreferences("TaskManagerPrefs", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("logged_in_user", "");

        tasks = db.getTasksForTodayAndUser(todayDate, userEmail);

        if (!tasks.isEmpty()) {
            adapter = new TaskAdapter(tasks, this);
            recyclerView.setAdapter(adapter);
            noTasksTextView.setVisibility(View.GONE);
        } else {
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
                    switch (priority) {
                        case "High":
                            return 3;
                        case "Medium":
                            return 2;
                        case "Low":
                            return 1;
                        default:
                            return 0;
                    }
                }
            });

            // Update the RecyclerView adapter with the new sorted list
            adapter = new TaskAdapter(tasks, this); // Reinitialize the adapter
            recyclerView.setAdapter(adapter); // Set the updated adapter
            adapter.notifyDataSetChanged(); // Notify adapter about changes

            Toast.makeText(requireContext(), "Tasks sorted by priority", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "No tasks to sort", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onTaskCompleted(Task task, int position) {
        boolean updated = db.updateTaskCompletionStatus(task.getId(), 1); // Mark task as completed

        if (updated) {
            tasks.get(position).setCompletionStatus(1);  // Update the task status locally
            adapter.notifyItemChanged(position);  // Notify RecyclerView about the change
            Toast.makeText(requireContext(), "Task marked as completed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTaskClick(Task task, int position) {
        // Implement task details view or other click behavior
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

}
