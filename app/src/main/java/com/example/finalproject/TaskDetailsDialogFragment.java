package com.example.finalproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.fragment.app.DialogFragment;

public class TaskDetailsDialogFragment extends DialogFragment {

    private Task task;

    public TaskDetailsDialogFragment(Task task) {
        this.task = task;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the custom dialog layout
        View rootView = inflater.inflate(R.layout.dialog_task_details, container, false);

        rootView.setElevation(16f);

        TextView taskTitle = rootView.findViewById(R.id.task_title);
        TextView taskDescription = rootView.findViewById(R.id.task_description);
        TextView taskDueDate = rootView.findViewById(R.id.task_due_date);
        TextView taskDueTime = rootView.findViewById(R.id.task_due_time);
        TextView taskPriority = rootView.findViewById(R.id.task_priority);
        TextView taskStatus = rootView.findViewById(R.id.task_status);

        // Set the task details to the respective views
        taskTitle.setText(task.getTitle());
        taskDescription.setText(task.getDescription());
        taskDueDate.setText("Due Date: " + task.getDueDate());
        taskDueTime.setText("Due Time: " + task.getDueTime());
        taskPriority.setText("Priority: " + task.getPriority());
        taskStatus.setText("Status: " + (task.isCompleted() ? "Completed" : "Incomplete"));

        // Set the close button
        Button closeButton = rootView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> {
            // Apply scale-out animation when the dialog is closed
            Animation scaleOut = AnimationUtils.loadAnimation(getContext(), R.anim.dialog_scale_out);
            rootView.startAnimation(scaleOut);
            scaleOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    dismiss(); // Dismiss the dialog after animation ends
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        });

        // Apply scale-in animation when the dialog is shown
        applyDialogAnimation(rootView);

        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, // Width
                ViewGroup.LayoutParams.WRAP_CONTENT  // Height
        );

        return rootView;
    }

    private void applyDialogAnimation(View view) {
        // Load the scale-in animation
        Animation scaleIn = AnimationUtils.loadAnimation(getContext(), R.anim.dialog_scale_in);
        view.startAnimation(scaleIn);
    }

    @Override
    public int getTheme() {
        return R.style.CustomDialogTheme;
    }
}
