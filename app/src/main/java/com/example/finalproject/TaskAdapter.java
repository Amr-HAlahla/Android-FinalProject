package com.example.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private OnTaskClickListener onTaskClickListener;

    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.onTaskClickListener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        holder.title.setText(task.getTitle());
        holder.description.setText(task.getDescription());
        holder.dueDate.setText(task.getDueDate() + " " + task.getDueTime());
        holder.priority.setText(task.getPriority());
        holder.completedCheckbox.setChecked(task.getCompletionStatus() == 1);

        // Handle task completion checkbox
        holder.completedCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int status = isChecked ? 1 : 0;
            task.setCompletionStatus(status);
            onTaskClickListener.onTaskCompleted(task, position);
        });

        // Edit button logic
        holder.btnEdit.setOnClickListener(v -> onTaskClickListener.onEditClick(task, position));

        // Delete button logic
        holder.btnDelete.setOnClickListener(v -> onTaskClickListener.onDeleteClick(task, position));

        // Set Notification button logic
        holder.btnSetNotification.setOnClickListener(v -> onTaskClickListener.onSetNotification(task, position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView title, description, dueDate, priority;
        CheckBox completedCheckbox;
        Button btnEdit, btnDelete, btnSetNotification;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.task_title);
            description = itemView.findViewById(R.id.task_description);
            dueDate = itemView.findViewById(R.id.task_due_date);
            priority = itemView.findViewById(R.id.task_priority);
            completedCheckbox = itemView.findViewById(R.id.task_completed_checkbox);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnSetNotification = itemView.findViewById(R.id.btn_set_notification); // New button
        }
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task, int position);

        void onEditClick(Task task, int position);

        void onDeleteClick(Task task, int position);

        void onTaskCompleted(Task task, int position);

        void onSetNotification(Task task, int position); // New method
    }
}
