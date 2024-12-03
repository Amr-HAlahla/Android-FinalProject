package com.example.finalproject;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private AllTaskAdapter adapter;
    private DatabaseHelper db;
    private EditText etSearchKeyword;
    private Button btnStartDate, btnEndDate, btnSearch;
    private String startDate, endDate;
    private List<Task> tasks;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize views
        etSearchKeyword = rootView.findViewById(R.id.et_search_keyword);
        btnStartDate = rootView.findViewById(R.id.btn_start_date);
        btnEndDate = rootView.findViewById(R.id.btn_end_date);
        btnSearch = rootView.findViewById(R.id.btn_search);
        recyclerView = rootView.findViewById(R.id.recycler_search_results);

        db = new DatabaseHelper(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up listeners
        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));

        // Set up search button click listener
        btnSearch.setOnClickListener(v -> searchTasks());

        return rootView;
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth) -> {
            String date = (month1 + 1) + "/" + dayOfMonth + "/" + year1;

            if (isStartDate) {
                startDate = date;
                btnStartDate.setText("Start Date: " + date);
            } else {
                endDate = date;
                btnEndDate.setText("End Date: " + date);
            }

        }, year, month, day);
        datePickerDialog.show();
    }

    private void searchTasks() {
        String keyword = etSearchKeyword.getText().toString().trim();

        // Ensure that at least one of the fields (keyword or date range) is provided
        if (keyword.isEmpty() && (startDate == null || endDate == null)) {
            Toast.makeText(getContext(), "Please enter a search keyword or date range", Toast.LENGTH_SHORT).show();
            return;
        }

        String loggedInUserEmail = getLoggedInUserEmail();
        if (!loggedInUserEmail.isEmpty()) {
            // Fetch tasks based on keyword and date range
            tasks = db.searchTasks(loggedInUserEmail, keyword, startDate, endDate);
            adapter = new AllTaskAdapter(tasks, new AllTaskAdapter.OnTaskClickListener() {
                @Override
                public void onTaskClick(Task task, int position) {
                    // Handle task click (e.g., mark as completed or edit)
                    editTask(task, position);
                }

                @Override
                public void onEditClick(Task task, int position) {
                    // Handle edit action
                    editTask(task, position);
                }

                @Override
                public void onDeleteClick(Task task, int position) {
                    // Handle delete action
                    deleteTask(task, position);
                }

                @Override
                public void onTaskCompleted(Task task, int position) {
                    // Handle task completion
                    updateTaskCompletionStatus(task, position);
                }
            });
            recyclerView.setAdapter(adapter);
        }
    }

    private void editTask(Task task, int position) {
        // Open the EditTaskDialog to edit the task
        EditTaskDialog dialog = new EditTaskDialog(task, updatedTask -> {
            // After updating the task, refresh the list
            tasks.set(position, updatedTask);  // Update the task in the list
            adapter.notifyItemChanged(position);  // Refresh the specific item in RecyclerView
        });
        dialog.show(getChildFragmentManager(), "EditTaskDialog");
    }

    private void deleteTask(Task task, int position) {
        // Delete task from the database
        db.deleteTask(task.getId());

        // Remove the task from the list and update the RecyclerView
        tasks.remove(position);  // Remove the task from the list
        adapter.notifyItemRemoved(position);  // Notify RecyclerView about the removal
    }

    private void updateTaskCompletionStatus(Task task, int position) {
        // Update task completion status in the database
        boolean isUpdated = db.updateTask(task);
        if (isUpdated) {
            adapter.notifyDataSetChanged();  // Refresh the list after updating
        }
    }

    private String getLoggedInUserEmail() {
        SharedPreferences preferences = getContext().getSharedPreferences("TaskManagerPrefs", getContext().MODE_PRIVATE);
        return preferences.getString("logged_in_user", "");
    }
}
