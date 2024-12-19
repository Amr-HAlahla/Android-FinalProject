package com.example.finalproject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskGroup {
    private String groupName; // Group name, e.g., the day
    private List<Task> tasks; // Tasks belonging to this group

    // Constructor
    public TaskGroup(String groupName, List<Task> tasks) {
        this.groupName = groupName;
        this.tasks = tasks;
    }

    // Getters and Setters
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * Static method for grouping tasks by day and sorting them by priority within each group.
     *
     * @param tasks List of tasks to be grouped and sorted.
     * @return List of TaskGroup objects grouped by day, with tasks sorted by priority.
     */
    public static List<TaskGroup> groupByDayAndSortByPriority(List<Task> tasks) {
        // Map to store tasks grouped by their due date (day)
        Map<String, List<Task>> groupedMap = new HashMap<>();

        // Group tasks by due date
        for (Task task : tasks) {
            String dueDate = task.getDueDate(); // Assume task.getDueDate() returns a string representing the day
            if (dueDate == null || dueDate.isEmpty()) {
                dueDate = "No Due Date"; // Handle tasks with no due date
            }

            // Add the task to the appropriate group in the map
            groupedMap.putIfAbsent(dueDate, new ArrayList<>());
            groupedMap.get(dueDate).add(task);
        }

        // Convert the map into a list of TaskGroup objects
        List<TaskGroup> taskGroups = new ArrayList<>();
        for (Map.Entry<String, List<Task>> entry : groupedMap.entrySet()) {
            // Sort tasks within each group by priority
            List<Task> tasksForDay = entry.getValue();
            Collections.sort(tasksForDay, new Comparator<Task>() {
                @Override
                public int compare(Task t1, Task t2) {
                    return getPriorityLevel(t1.getPriority()) - getPriorityLevel(t2.getPriority());
                }
            });

            // Add the sorted group to the list
            taskGroups.add(new TaskGroup(entry.getKey(), tasksForDay));
        }

        return taskGroups;
    }

    /**
     * Helper method to convert priority string to numeric level for sorting.
     *
     * @param priority The priority string (e.g., "High", "Medium", "Low").
     * @return Numeric level: High = 1, Medium = 2, Low = 3, No Priority = 4.
     */
    private static int getPriorityLevel(String priority) {
        if (priority == null || priority.isEmpty()) {
            return 4; // No priority
        }
        switch (priority.toLowerCase()) {
            case "high":
                return 1;
            case "medium":
                return 2;
            case "low":
                return 3;
            default:
                return 4; // Default for unknown priorities
        }
    }
}
