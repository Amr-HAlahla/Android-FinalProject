package com.example.finalproject;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SetNotificationDialog extends AppCompatDialogFragment {

    private Task task;
    private DatabaseHelper db;

    public SetNotificationDialog(Task task) {
        this.task = task;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // Inflate the layout for the dialog
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_set_notification, null);
        dialog.setContentView(view);

        // Initialize views
        TextView tvDeadline = view.findViewById(R.id.tv_deadline);
        TextView tvRemainingTime = view.findViewById(R.id.tv_remaining_time);
        TextView tvCurrentNotification = view.findViewById(R.id.tv_current_notification);
        EditText etDays = view.findViewById(R.id.et_days);
        EditText etHours = view.findViewById(R.id.et_hours);
        EditText etMinutes = view.findViewById(R.id.et_minutes); // New input for minutes
        Button btnSave = view.findViewById(R.id.btn_save_notification);
        Button btnCancel = view.findViewById(R.id.btn_cancel_notification);

        db = new DatabaseHelper(getContext());

        // Display the task deadline
        tvDeadline.setText("Task Deadline: " + task.getDueDate() + " " + task.getDueTime());

        // Calculate and display remaining time
        Calendar now = Calendar.getInstance();
        Calendar deadline = getDeadlineCalendar(task.getDueDate(), task.getDueTime());
        if (deadline != null) {
            long millisRemaining = deadline.getTimeInMillis() - now.getTimeInMillis();

            long daysRemaining = millisRemaining / (1000 * 60 * 60 * 24);
            long hoursRemaining = (millisRemaining / (1000 * 60 * 60)) % 24;
            long minutesRemaining = (millisRemaining / (1000 * 60)) % 60; // Calculate remaining minutes
            tvRemainingTime.setText("Remaining Time: " + daysRemaining + " days " + hoursRemaining + " hours " + minutesRemaining + " minutes");
        } else {
            tvRemainingTime.setText("Invalid deadline");
        }

        // Display current reminder time
        if (!TextUtils.isEmpty(task.getReminderTime())) {
            tvCurrentNotification.setText("Current Notification: " + task.getReminderTime());
        } else {
            tvCurrentNotification.setText("Current Notification: None");
        }

        // Save button logic
        btnSave.setOnClickListener(v -> {
            String daysStr = etDays.getText().toString().trim();
            String hoursStr = etHours.getText().toString().trim();
            String minutesStr = etMinutes.getText().toString().trim(); // Get minutes input

            // Validate input
            if (TextUtils.isEmpty(daysStr) || TextUtils.isEmpty(hoursStr) || TextUtils.isEmpty(minutesStr)) {
                Toast.makeText(getContext(), "Please enter days, hours, and minutes", Toast.LENGTH_SHORT).show();
                return;
            }

            int days = Integer.parseInt(daysStr);
            int hours = Integer.parseInt(hoursStr);
            int minutes = Integer.parseInt(minutesStr);

            if (days < 0 || hours < 0 || minutes < 0 || (days == 0 && hours == 0 && minutes == 0)) {
                Toast.makeText(getContext(), "Invalid notification time!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Calculate notification time based on user input
            Calendar notificationTime = (Calendar) deadline.clone();
            notificationTime.add(Calendar.DAY_OF_YEAR, -days);
            notificationTime.add(Calendar.HOUR_OF_DAY, -hours);
            notificationTime.add(Calendar.MINUTE, -minutes);

            if (notificationTime.before(now)) {
                Toast.makeText(getContext(), "Notification time must be in the future!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Format and save the reminder time
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String reminderTime = sdf.format(notificationTime.getTime());

            task.setReminderTime(reminderTime); // Update task object
            boolean isUpdated = db.updateTaskReminderTime(task.getId(), reminderTime); // Save to database

            if (isUpdated) {
                // Schedule the notification reminder
                NotificationScheduler.scheduleTaskReminder(getContext(), task);

                Toast.makeText(getContext(), "Notification set successfully!", Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                Toast.makeText(getContext(), "Failed to set notification!", Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel button logic
        btnCancel.setOnClickListener(v -> dismiss());

        return dialog;
    }

    private Calendar getDeadlineCalendar(String dueDate, String dueTime) {
        try {
            String datetime = dueDate + " " + dueTime;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(datetime));
            return calendar;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
