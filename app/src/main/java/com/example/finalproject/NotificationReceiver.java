package com.example.finalproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "TASK_REMINDER_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get task info from the intent
        String taskTitle = intent.getStringExtra("task_title");

        // Create a notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Task Reminders", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Create the notification
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Task Reminder")
                .setContentText("You have a task: " + taskTitle)
                .setSmallIcon(R.drawable.ic_task_notification) // Use an appropriate icon here
                .setAutoCancel(true)
                .build();

        // Show the notification
        notificationManager.notify(0, notification);
    }
}
