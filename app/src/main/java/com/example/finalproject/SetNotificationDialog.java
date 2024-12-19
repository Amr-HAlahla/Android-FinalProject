// SetNotificationDialog.java
package com.example.finalproject;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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
        TextInputLayout tilDays = view.findViewById(R.id.til_days);
        TextInputEditText etDays = view.findViewById(R.id.et_days);
        TextInputLayout tilHours = view.findViewById(R.id.til_hours);
        TextInputEditText etHours = view.findViewById(R.id.et_hours);
        TextInputLayout tilMinutes = view.findViewById(R.id.til_minutes);
        TextInputEditText etMinutes = view.findViewById(R.id.et_minutes);
        MaterialButton btnSave = view.findViewById(R.id.btn_save_notification);
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel_notification);

        db = new DatabaseHelper(getContext());

        // Display the task deadline
        tvDeadline.setText(getString(R.string.task_deadline) + " " + task.getDueDate() + " " + task.getDueTime());

        // Calculate and display remaining time
        Calendar now = Calendar.getInstance();
        Calendar deadline = getDeadlineCalendar(task.getDueDate(), task.getDueTime());
        if (deadline != null) {
            long millisRemaining = deadline.getTimeInMillis() - now.getTimeInMillis();

            if (millisRemaining > 0) {
                long daysRemaining = millisRemaining / (1000 * 60 * 60 * 24);
                long hoursRemaining = (millisRemaining / (1000 * 60 * 60)) % 24;
                long minutesRemaining = (millisRemaining / (1000 * 60)) % 60;
                tvRemainingTime.setText(getString(R.string.remaining_time) + " " + daysRemaining + " days " + hoursRemaining + " hours " + minutesRemaining + " minutes");
            } else {
                tvRemainingTime.setText(getString(R.string.remaining_time) + " Deadline has passed.");
            }
        } else {
            tvRemainingTime.setText(getString(R.string.remaining_time) + " Invalid deadline");
        }

        // Check if reminder time has passed and display the appropriate message
        if (!TextUtils.isEmpty(task.getReminderTime())) {
            String reminderTime = task.getReminderTime();
            Calendar reminderCalendar = getReminderCalendar(reminderTime);
            if (reminderCalendar != null && reminderCalendar.before(now)) {
                // If the reminder time has passed
                tvCurrentNotification.setText(getString(R.string.current_notification) + " None");
            } else {
                tvCurrentNotification.setText(getString(R.string.current_notification) + " " + reminderTime);
            }
        } else {
            tvCurrentNotification.setText(getString(R.string.current_notification) + " None");
        }

        // Save button logic
        btnSave.setOnClickListener(v -> {
            String daysStr = etDays.getText().toString().trim();
            String hoursStr = etHours.getText().toString().trim();
            String minutesStr = etMinutes.getText().toString().trim(); // Get minutes input

            // Validate input
            boolean valid = true;

            if (TextUtils.isEmpty(daysStr)) {
                tilDays.setError(getString(R.string.enter_days));
                valid = false;
            } else {
                tilDays.setError(null);
            }

            if (TextUtils.isEmpty(hoursStr)) {
                tilHours.setError(getString(R.string.enter_hours));
                valid = false;
            } else {
                tilHours.setError(null);
            }

            if (TextUtils.isEmpty(minutesStr)) {
                tilMinutes.setError(getString(R.string.enter_minutes));
                valid = false;
            } else {
                tilMinutes.setError(null);
            }

            if (!valid) {
                return;
            }

            int days = Integer.parseInt(daysStr);
            int hours = Integer.parseInt(hoursStr);
            int minutes = Integer.parseInt(minutesStr);

            if (days < 0 || hours < 0 || minutes < 0 || (days == 0 && hours == 0 && minutes == 0)) {
                Toast.makeText(getContext(), getString(R.string.invalid_notification_time), Toast.LENGTH_SHORT).show();
                return;
            }

            // Calculate notification time based on user input
            Calendar notificationTime = (Calendar) deadline.clone();
            notificationTime.add(Calendar.DAY_OF_YEAR, -days);
            notificationTime.add(Calendar.HOUR_OF_DAY, -hours);
            notificationTime.add(Calendar.MINUTE, -minutes);

            if (notificationTime.before(now)) {
                Toast.makeText(getContext(), getString(R.string.notification_time_future), Toast.LENGTH_SHORT).show();
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

                Toast.makeText(getContext(), getString(R.string.notification_set_success), Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                Toast.makeText(getContext(), getString(R.string.notification_set_failed), Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel button logic
        btnCancel.setOnClickListener(v -> dismiss());

        return dialog;
    }

    private Calendar getReminderCalendar(String reminderTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(reminderTime));
            return calendar;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    @Override
    public void onResume() {
        super.onResume();
        // Set dialog width and height
        Dialog dialog = getDialog();
        if (dialog != null) {
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(params);
        }
    }
}
