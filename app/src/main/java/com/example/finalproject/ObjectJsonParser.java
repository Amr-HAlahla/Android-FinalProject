package com.example.finalproject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ObjectJsonParser {

    public static List<Task> getTaskFromJson(String json) {
        List<Task> taskList = new ArrayList<>();
        try {
            JSONArray tasksArray = new JSONArray(json);
            for (int i = 0; i < tasksArray.length(); i++) {
                JSONObject taskJson = tasksArray.getJSONObject(i);

                int id = taskJson.getInt("id");
                String title = taskJson.getString("title");
                String description = taskJson.getString("description");
                String dueDate = taskJson.getString("dueDate");
                String dueTime = taskJson.getString("dueTime");
                String priority = taskJson.getString("priority");
                int completionStatus = taskJson.getInt("completionStatus");
                String reminderTime = taskJson.getString("reminderTime");
                String userEmail = taskJson.getString("userEmail");

                Task task = new Task(id, title, description, dueDate, dueTime, priority,
                        completionStatus, reminderTime, userEmail);

                taskList.add(task);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return taskList;
    }
}
