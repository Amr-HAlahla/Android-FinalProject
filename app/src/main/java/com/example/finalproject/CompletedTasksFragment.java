package com.example.finalproject;

import android.content.Context;
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

public class CompletedTasksFragment extends Fragment implements AllTaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerView;
    private AllTaskAdapter adapter;
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

        // Fetch completed tasks for the current user
        tasks = db.getCompletedTasksForUser(loggedInUserEmail);

        if (tasks != null && !tasks.isEmpty()) {
            adapter = new AllTaskAdapter(tasks, this); // Pass `this` as the listener
            recyclerView.setAdapter(adapter);
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTaskClick(Task task, int position) {
        // Handle task click (e.g., show task details or other actions)
        Toast.makeText(requireContext(), "Task clicked: " + task.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditClick(Task task, int position) {
        // Open the EditTaskDialog to edit the task
        EditTaskDialog dialog = new EditTaskDialog(task, updatedTask -> {
            // After updating the task, refresh the list
            tasks.set(position, updatedTask); // Update the task in the list
            adapter.notifyItemChanged(position); // Notify RecyclerView about the change
        });
        dialog.show(getChildFragmentManager(), "EditTaskDialog");
    }

    @Override
    public void onDeleteClick(Task task, int position) {
        // Delete task from the database
        boolean deleted = db.deleteTask(task.getId());

        if (deleted) {
            tasks.remove(position); // Remove the task from the list
            adapter.notifyItemRemoved(position); // Notify RecyclerView about the removal
            Toast.makeText(requireContext(), "Task deleted successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Failed to delete task", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTaskCompleted(Task task, int position) {
        // Toggle the completion status of the task
        int newStatus = task.getCompletionStatus() == 1 ? 0 : 1; // Toggle completion status
        boolean isUpdated = db.updateTaskCompletionStatus(task.getId(), newStatus);

        if (isUpdated) {
            // Remove the task from the list if it is no longer completed
            if (newStatus == 0) {
                tasks.remove(position);
                adapter.notifyItemRemoved(position);
            }
            Toast.makeText(requireContext(), "Task status updated", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Failed to update task status", Toast.LENGTH_SHORT).show();
        }
    }

    private String getLoggedInUserEmail() {
        // Retrieve the logged-in user's email from SharedPreferences
        SharedPreferences preferences = requireContext().getSharedPreferences("TaskManagerPrefs", Context.MODE_PRIVATE);
        return preferences.getString("logged_in_user", "");
    }
}
