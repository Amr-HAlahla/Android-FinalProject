package com.example.finalproject;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private OnTaskClickListener onTaskClickListener;

    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.onTaskClickListener = listener;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.title.setText(task.getTitle());
        holder.description.setText(task.getDescription());
        holder.dueDate.setText(task.getDueDate() + " " + task.getDueTime());
        holder.priority.setText(task.getPriority());
        holder.completedCheckbox.setChecked(task.getCompletionStatus() == 1);

        // Handle task checkbox click
        holder.completedCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // find and Update the task completion status
            int status = isChecked ? 1 : 0;
            task.setCompletionStatus(status);
            onTaskClickListener.onTaskCompleted(task, position);
        });

        holder.btnEdit.setOnClickListener(v -> onTaskClickListener.onEditClick(task, position));
        holder.btnDelete.setOnClickListener(v -> onTaskClickListener.onDeleteClick(task, position));

        // New button for sharing via email
        holder.btnShareEmail.setOnClickListener(v -> onTaskClickListener.onShareEmailClick(task));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView title, description, dueDate, priority;
        CheckBox completedCheckbox;
        Button btnEdit, btnDelete;
        ImageButton btnShareEmail;  // New email share button

        public TaskViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.task_title);
            description = itemView.findViewById(R.id.task_description);
            dueDate = itemView.findViewById(R.id.task_due_date);
            priority = itemView.findViewById(R.id.task_priority);
            completedCheckbox = itemView.findViewById(R.id.task_completed_checkbox);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnShareEmail = itemView.findViewById(R.id.btn_share_email);  // Initialize the email button
        }
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task, int position);

        void onEditClick(Task task, int position);

        void onDeleteClick(Task task, int position);

        void onTaskCompleted(Task task, int position);  // New method for handling task completion

        // New method for notifications
        void onSetNotification(Task task, int position);

        // New method for sharing task via email
        void onShareEmailClick(Task task);
    }
}
