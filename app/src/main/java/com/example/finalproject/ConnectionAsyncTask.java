package com.example.finalproject;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

public class ConnectionAsyncTask extends AsyncTask<String, String, String> {

    private AllTasksFragment fragment;

    public ConnectionAsyncTask(AllTasksFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Show progress bar and disable the button
        fragment.getProgressBar().setVisibility(View.VISIBLE);
        fragment.getFetchButton().setEnabled(false);
        fragment.getFetchButton().setText("Fetching..."); // Change button text to "Fetching..."
    }

    @Override
    protected String doInBackground(String... params) {
        return HttpManager.getData(params[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        // Hide progress bar and re-enable the fetch button
        if (fragment.getProgressBar() != null) {
            fragment.getProgressBar().setVisibility(View.GONE);
        }
        if (fragment.getFetchButton() != null) {
            fragment.getFetchButton().setEnabled(true);
            fragment.getFetchButton().setText("Fetch Data"); // Reset button text
        }

        if (result != null) {
            try {
                // Parse the JSON response and store tasks
                List<Task> tasks = ObjectJsonParser.getTaskFromJson(result);
                // print all the tasks in the log
                for (Task task : tasks) {
                    System.out.println(task);
                }
                DatabaseHelper db = new DatabaseHelper(fragment.getContext());

                // Insert tasks into the database
                for (Task task : tasks) {
                    String userEmail = task.getUserEmail();
                    if (db.isEmailExists(userEmail)) {
                        // Ensure the task data is formatted correctly before insertion
                        String dueDate = task.getDueDate();  // Ensure this is in the "yyyy-MM-dd" format
                        String dueTime = task.getDueTime();  // Ensure this is in the "hh:mm a" format
                        String reminderTime = task.getReminderTime();  // Ensure it's in the expected format

                        // Insert the task into the database
                        db.insertTask(task.getTitle(), task.getDescription(), dueDate,
                                dueTime, task.getPriority(), task.getCompletionStatus(),
                                reminderTime, task.getUserEmail());
                    } else {
                        System.out.println("Task ignored: User email not found in database - " + userEmail);
                    }
                }

                // Refresh the fragment with new data
                fragment.loadTasks(); // Refresh tasks in fragment

            } catch (Exception e) {
                // Handle any exceptions that might occur during parsing or database insertion
                e.printStackTrace();
                Toast.makeText(fragment.getContext(), "Error parsing or inserting data", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(fragment.getContext(), "No data received from server", Toast.LENGTH_SHORT).show();
        }
    }

}
