package com.example.finalproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AllTasksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_DATE_HEADER = 0;
    private static final int VIEW_TYPE_TASK = 1;

    private List<Object> items; // Mixed list of tasks and date headers
    private AllTasksListener listener;
    private Context context;

    public AllTasksAdapter(Context context, List<Object> items, AllTasksListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return VIEW_TYPE_DATE_HEADER;
        } else {
            return VIEW_TYPE_TASK;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
            return new TaskViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_DATE_HEADER) {
            String dateHeader = (String) items.get(position);
            DateHeaderViewHolder dateHolder = (DateHeaderViewHolder) holder;
            dateHolder.dateHeader.setText(dateHeader);
        } else {
            Task task = (Task) items.get(position);
            TaskViewHolder taskHolder = (TaskViewHolder) holder;

            // Set task details with labels using string resources
            taskHolder.title.setText(context.getString(R.string.title_label, task.getTitle()));
            taskHolder.description.setText(context.getString(R.string.description_label, task.getDescription()));
            taskHolder.dueDate.setText(context.getString(R.string.due_date_label, task.getDueDate()));
            taskHolder.dueTime.setText(context.getString(R.string.due_time_label, task.getDueTime()));
            taskHolder.priority.setText(context.getString(R.string.priority_label, task.getPriority()));
            taskHolder.completedCheckbox.setChecked(task.isCompleted());

            // Set priority text color based on level
            switch (task.getPriority().toLowerCase()) {
                case "high":
                    taskHolder.priority.setTextColor(context.getResources().getColor(R.color.priority_high));
                    break;
                case "medium":
                    taskHolder.priority.setTextColor(context.getResources().getColor(R.color.priority_medium));
                    break;
                case "low":
                    taskHolder.priority.setTextColor(context.getResources().getColor(R.color.priority_low));
                    break;
                default:
                    taskHolder.priority.setTextColor(context.getResources().getColor(R.color.darker_gray)); // Default color
            }

            // Handle checkbox change
            taskHolder.completedCheckbox.setOnCheckedChangeListener(null);
            taskHolder.completedCheckbox.setChecked(task.isCompleted());
            taskHolder.completedCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int bindingAdapterPosition = holder.getBindingAdapterPosition();
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    Task updatedTask = (Task) items.get(bindingAdapterPosition);
                    updatedTask.setCompleted(isChecked);
                    listener.onTaskCompleted(updatedTask, bindingAdapterPosition);
                }
            });

            // Click listeners for actions
            taskHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });

            taskHolder.btnEdit.setOnClickListener(v -> {
                int bindingAdapterPosition = holder.getBindingAdapterPosition();
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    listener.onEditClick(task, bindingAdapterPosition);
                }
            });

            taskHolder.btnDelete.setOnClickListener(v -> {
                int bindingAdapterPosition = holder.getBindingAdapterPosition();
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(task, bindingAdapterPosition);
                }
            });

            taskHolder.btnSetNotification.setOnClickListener(v -> {
                int bindingAdapterPosition = holder.getBindingAdapterPosition();
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    listener.onSetNotification(task, bindingAdapterPosition);
                }
            });

            taskHolder.btnShareEmail.setOnClickListener(v -> listener.onShareEmailClick(task));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateTasks(List<Object> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void updateGroupedTasks(Map<String, List<Task>> groupedTasks) {
        List<Object> newItems = new ArrayList<>();
        for (Map.Entry<String, List<Task>> entry : groupedTasks.entrySet()) {
            newItems.add(entry.getKey()); // Add date header
            newItems.addAll(entry.getValue()); // Add tasks for the date
        }
        this.items = newItems;
        notifyDataSetChanged();
    }

    // ViewHolder for task items
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, dueDate, priority, dueTime;
        CheckBox completedCheckbox;
        ImageButton btnEdit, btnDelete, btnSetNotification, btnShareEmail;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.task_title);
            description = itemView.findViewById(R.id.task_description);
            dueDate = itemView.findViewById(R.id.task_due_date);
            priority = itemView.findViewById(R.id.task_priority);
            dueTime = itemView.findViewById(R.id.task_due_time);
            completedCheckbox = itemView.findViewById(R.id.task_completed_checkbox);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnSetNotification = itemView.findViewById(R.id.btn_set_notification);
            btnShareEmail = itemView.findViewById(R.id.btn_share_email);
        }
    }

    // ViewHolder for date headers
    public static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView dateHeader;

        public DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            dateHeader = itemView.findViewById(R.id.date_header);
        }
    }

    public interface AllTasksListener {
        void onTaskClick(Task task);

        void onTaskCompleted(Task task, int position);

        void onEditClick(Task task, int position);

        void onDeleteClick(Task task, int position);

        void onSetNotification(Task task, int position);

        void onShareEmailClick(Task task);
    }

    public List<Object> getItems() {
        return items;
    }

}
