package com.example.finalproject;

public class Task {

    private int id;
    private String title;
    private String description;
    private String dueDate;
    private String dueTime;
    private String priority;
    private int completionStatus;  // 0: Not completed, 1: Completed
    private String reminderTime;
    private String userEmail;

    // Empty Constructor
    public Task() {
    }

    // Constructor
    public Task(int id, String title, String description, String dueDate, String dueTime,
                String priority, int completionStatus, String reminderTime, String userEmail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.priority = priority;
        this.completionStatus = completionStatus;
        this.reminderTime = reminderTime;
        this.userEmail = userEmail;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getDueTime() {
        return dueTime;
    }

    public void setDueTime(String dueTime) {
        this.dueTime = dueTime;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public int getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(int completionStatus) {
        this.completionStatus = completionStatus;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public boolean isCompleted() {
        return completionStatus == 1;
    }

    public void setCompleted(boolean completed) {
        completionStatus = completed ? 1 : 0;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dueDate='" + dueDate + '\'' +
                ", dueTime='" + dueTime + '\'' +
                ", priority='" + priority + '\'' +
                ", completionStatus=" + completionStatus +
                ", reminderTime='" + reminderTime + '\'' +
                ", userEmail='" + userEmail + '\'' +
                '}';
    }
}
