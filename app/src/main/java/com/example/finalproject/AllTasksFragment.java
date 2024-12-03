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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AllTasksFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private DatabaseHelper db;
    private List<Task> tasks;
    private TextView noTasksTextView;

    public AllTasksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_all_tasks, container, false);

        // Initialize views and database
        recyclerView = rootView.findViewById(R.id.recycler_all_tasks);
        noTasksTextView = rootView.findViewById(R.id.text_no_tasks);
        db = new DatabaseHelper(requireContext());

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Load tasks for the logged-in user
        loadTasks();

        return rootView;
    }

    private void loadTasks() {
        String loggedInUserEmail = getLoggedInUserEmail();

        if (loggedInUserEmail.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch tasks for the current user
        tasks = db.getAllTasksForUser(loggedInUserEmail);

        if (tasks != null && !tasks.isEmpty()) {
            adapter = new TaskAdapter(tasks, this);
            recyclerView.setAdapter(adapter);
            recyclerView.setVisibility(View.VISIBLE);
            noTasksTextView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            noTasksTextView.setVisibility(View.VISIBLE);
        }
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
        // Delete task from the database
        boolean deleted = db.deleteTask(task.getId());

        if (deleted) {
            // Remove the task from the list and notify RecyclerView
            tasks.remove(position);
            adapter.notifyItemRemoved(position);
            Toast.makeText(requireContext(), "Task deleted successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Failed to delete task", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTaskCompleted(Task task, int position) {
        boolean updated = db.updateTaskCompletionStatus(task.getId(), task.getCompletionStatus());

        if (updated) {
            adapter.notifyItemChanged(position);
            Toast.makeText(requireContext(), "Task completion status updated", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Failed to update task status", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTaskClick(Task task, int position) {
        // Handle task click (e.g., show task details or other actions)
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
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"3mro7ala7la@gmail.com"}); // Add recipient email addresses if needed
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

    private String getLoggedInUserEmail() {
        // Retrieve the logged-in user's email from SharedPreferences
        SharedPreferences preferences = requireContext().getSharedPreferences("TaskManagerPrefs", Context.MODE_PRIVATE);
        return preferences.getString("logged_in_user", "");
    }
}
