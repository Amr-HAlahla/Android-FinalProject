package com.example.finalproject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NotificationScheduler {

    public static void scheduleTaskReminder(Context context, Task task) {
        try {
            // Convert the reminder time string to Calendar
            String reminderTime = task.getReminderTime();
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            calendar.setTime(sdf.parse(reminderTime));

            // Create a pending intent to send a broadcast when the alarm goes off
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra("task_title", task.getTitle());  // Send task info
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, task.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Get the AlarmManager system service
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            // Set the alarm to trigger at the chosen time
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

            Log.d("NotificationScheduler", "Notification set for: " + task.getTitle() + " at " + reminderTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
