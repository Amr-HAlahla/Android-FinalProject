package com.example.finalproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private OnTaskClickListener onTaskClickListener;
    private Context context;

    public TaskAdapter(Context context, List<Task> tasks, OnTaskClickListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.onTaskClickListener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the updated layout without hardcoded texts
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        holder.title.setText(context.getString(R.string.title_label, task.getTitle()));
        holder.description.setText(context.getString(R.string.description_label, task.getDescription()));
        holder.dueDate.setText(context.getString(R.string.due_date_label, task.getDueDate()));
        holder.dueTime.setText(context.getString(R.string.due_time_label, task.getDueTime()));
        holder.priority.setText(context.getString(R.string.priority_label, getPriorityString(task.getPriority())));
        holder.completedCheckbox.setChecked(task.isCompleted());

        // Set the color of the priority text based on the priority level
        String priorityLevel = task.getPriority().toLowerCase();
        int priorityColor = getPriorityColor(priorityLevel);
        holder.priority.setTextColor(priorityColor);

        holder.completedCheckbox.setOnCheckedChangeListener(null);
        holder.completedCheckbox.setChecked(task.isCompleted());
        holder.completedCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int bindingAdapterPosition = holder.getBindingAdapterPosition();
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                Task updatedTask = tasks.get(bindingAdapterPosition);
                updatedTask.setCompleted(isChecked);
                onTaskClickListener.onTaskCompleted(updatedTask, bindingAdapterPosition);

                // Play animation if the task is marked as completed
                if (isChecked) {
                    playCompletionAnimation(holder.taskCompletionAnimation);
                } else {
                    holder.taskCompletionAnimation.setVisibility(View.GONE); // Hide animation if unchecked
                }
            }
        });

        // Handle button clicks for edit, delete, set notification, and share email
        holder.btnEdit.setOnClickListener(v -> {
            int bindingAdapterPosition = holder.getBindingAdapterPosition();
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                onTaskClickListener.onEditClick(task, bindingAdapterPosition);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            int bindingAdapterPosition = holder.getBindingAdapterPosition();
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                onTaskClickListener.onDeleteClick(task, bindingAdapterPosition);
            }
        });

        holder.btnSetNotification.setOnClickListener(v -> {
            int bindingAdapterPosition = holder.getBindingAdapterPosition();
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                onTaskClickListener.onSetNotification(task, bindingAdapterPosition);
            }
        });

        holder.btnShareEmail.setOnClickListener(v -> onTaskClickListener.onShareEmailClick(task));

        if (task.getDueDate() == null || task.getDueDate().isEmpty()) {
            holder.dueDate.setText(context.getString(R.string.due_date_label, "N/A"));
        }

        if (task.getPriority() == null || task.getPriority().isEmpty()) {
            holder.priority.setText(context.getString(R.string.priority_label, "N/A"));
            holder.priority.setTextColor(context.getResources().getColor(R.color.darker_gray)); // Neutral color
        }

        if (task.getDueTime() == null || task.getDueTime().isEmpty()) {
            holder.dueTime.setText(context.getString(R.string.due_time_label, "N/A"));
        }

        if (task.getDescription() == null || task.getDescription().isEmpty()) {
            holder.description.setText(context.getString(R.string.description_label, "No description available"));
        }

        holder.itemView.setOnClickListener(v -> {
            int bindingAdapterPosition = holder.getBindingAdapterPosition();
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                Task clickedTask = tasks.get(bindingAdapterPosition);
                onTaskClickListener.onTaskClick(clickedTask, bindingAdapterPosition);
            }
        });
    }

    // Helper method to play GIF animation
    private void playCompletionAnimation(ImageView animationView) {
        Glide.with(context)
                .asGif()
                .load(R.drawable.task_completed_gif)
                .into(animationView);

        animationView.setVisibility(View.VISIBLE);

        // Hide animation after 3 seconds
        animationView.postDelayed(() -> animationView.setVisibility(View.GONE), 3000);
    }

    // Helper method to get priority string
    private String getPriorityString(String priority) {
        if (priority == null) {
            return "N/A";
        }
        switch (priority.toLowerCase()) {
            case "low":
                return context.getString(R.string.priority_low);
            case "medium":
                return context.getString(R.string.priority_medium);
            case "high":
                return context.getString(R.string.priority_high);
            default:
                return priority;
        }
    }

    // Helper method to get the color based on priority level
    private int getPriorityColor(String priority) {
        switch (priority) {
            case "high":
                return context.getResources().getColor(R.color.priority_high);
            case "medium":
                return context.getResources().getColor(R.color.priority_medium);
            case "low":
                return context.getResources().getColor(R.color.priority_low);
            default:
                return context.getResources().getColor(R.color.darker_gray); // Neutral color for unknown priority
        }
    }


    @Override
    public int getItemCount() {
        return tasks.size();
    }


    // Method to update the adapter's task list
    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    // Method to clear the adapter's task list
    public void clearTasks() {
        if (this.tasks != null) {
            this.tasks.clear();
            notifyDataSetChanged();
        }
    }

    // ViewHolder Class
    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        // Task details
        TextView title, description, dueDate, priority, dueTime;
        CheckBox completedCheckbox;
        ImageView taskCompletionAnimation;

        // Action Buttons
        ImageButton btnEdit, btnDelete, btnSetNotification, btnShareEmail;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            title = itemView.findViewById(R.id.task_title);
            description = itemView.findViewById(R.id.task_description);
            dueDate = itemView.findViewById(R.id.task_due_date);
            dueTime = itemView.findViewById(R.id.task_due_time);
            priority = itemView.findViewById(R.id.task_priority);
            completedCheckbox = itemView.findViewById(R.id.task_completed_checkbox);
            taskCompletionAnimation = itemView.findViewById(R.id.task_completion_animation);

            // Initialize buttons
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnSetNotification = itemView.findViewById(R.id.btn_set_notification);
            btnShareEmail = itemView.findViewById(R.id.btn_share_email);
        }
    }

    // Listener Interface
    public interface OnTaskClickListener {
        void onTaskClick(Task task, int position);

        void onEditClick(Task task, int position);

        void onDeleteClick(Task task, int position);

        void onTaskCompleted(Task task, int position);

        void onSetNotification(Task task, int position);

        void onShareEmailClick(Task task);
    }

}
