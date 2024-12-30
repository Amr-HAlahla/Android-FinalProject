package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TaskManager.db";
    private static final int DATABASE_VERSION = 2;

    // User table
    private static final String TABLE_USERS = "Users";
    static final String COLUMN_EMAIL = "Email";
    static final String COLUMN_FIRST_NAME = "FirstName";
    static final String COLUMN_LAST_NAME = "LastName";
    private static final String COLUMN_PASSWORD = "Password";

    // Task table
    private static final String TABLE_TASKS = "Tasks";
    private static final String COLUMN_TASK_ID = "TaskID";
    private static final String COLUMN_TASK_TITLE = "TaskTitle";
    private static final String COLUMN_TASK_DESCRIPTION = "TaskDescription";
    private static final String COLUMN_DUE_DATE = "DueDate";
    private static final String COLUMN_DUE_TIME = "DueTime";
    private static final String COLUMN_PRIORITY = "Priority";
    private static final String COLUMN_COMPLETION_STATUS = "CompletionStatus";
    private static final String COLUMN_REMINDER_TIME = "ReminderTime";
    private static final String COLUMN_USER_EMAIL = "UserEmail";

    private Context context;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_EMAIL + " TEXT PRIMARY KEY, "
                + COLUMN_FIRST_NAME + " TEXT NOT NULL, "
                + COLUMN_LAST_NAME + " TEXT NOT NULL, "
                + COLUMN_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(createUsersTable);

        // Create Tasks table
        String createTasksTable = "CREATE TABLE " + TABLE_TASKS + " ("
                + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TASK_TITLE + " TEXT NOT NULL, "
                + COLUMN_TASK_DESCRIPTION + " TEXT, "
                + COLUMN_DUE_DATE + " TEXT NOT NULL, "
                + COLUMN_DUE_TIME + " TEXT NOT NULL, "
                + COLUMN_PRIORITY + " TEXT NOT NULL, "
                + COLUMN_COMPLETION_STATUS + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_REMINDER_TIME + " TEXT, "
                + COLUMN_USER_EMAIL + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COLUMN_USER_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_EMAIL + "))";
        db.execSQL(createTasksTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop existing tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public Map<String, List<Task>> getTasksGroupedByDay(String userEmail) {
        Map<String, List<Task>> groupedTasks = new LinkedHashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM Tasks WHERE UserEmail = ? ORDER BY DueDate, " +
                "CASE Priority WHEN 'high' THEN 1 WHEN 'medium' THEN 2 WHEN 'low' THEN 3 END";
        Cursor cursor = db.rawQuery(query, new String[]{userEmail});

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") Task task = new Task(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_TASK_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DUE_DATE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DUE_TIME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_PRIORITY)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_COMPLETION_STATUS)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_REMINDER_TIME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_USER_EMAIL))
                );

                String dueDate = task.getDueDate();
                groupedTasks.putIfAbsent(dueDate, new ArrayList<>());
                groupedTasks.get(dueDate).add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return groupedTasks;
    }


    // Retrieve all tasks for a specific user
    public List<Task> getTasksForTodayAndUser(String date, String email) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_TASKS +
                " WHERE " + COLUMN_DUE_DATE + " = ? AND " +
                COLUMN_USER_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{date, email});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // Your existing task creation logic
                @SuppressLint("Range") Task task = new Task(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_TASK_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DUE_DATE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DUE_TIME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_PRIORITY)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_COMPLETION_STATUS)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_REMINDER_TIME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_USER_EMAIL))

                );
                taskList.add(task);
            }
            cursor.close();
        }
        return taskList;
    }

    // Search tasks based on keyword, start date, and end date
    public List<Task> searchTasks(String email, String keyword, String startDate, String endDate) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Base query
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_USER_EMAIL + " = ?");

        List<String> argsList = new ArrayList<>();
        argsList.add(email);

        // Add keyword condition
        if (keyword != null && !keyword.isEmpty()) {
            queryBuilder.append(" AND (").append(COLUMN_TASK_TITLE).append(" LIKE ? OR ").append(COLUMN_TASK_DESCRIPTION).append(" LIKE ?)");
            String keywordPattern = "%" + keyword + "%";
            argsList.add(keywordPattern);
            argsList.add(keywordPattern);
        }

        // Add date range condition
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            // Ensure that startDate <= endDate
            try {
                Date start = DATE_FORMAT.parse(startDate);
                Date end = DATE_FORMAT.parse(endDate);
                if (start != null && end != null && !start.after(end)) {
                    queryBuilder.append(" AND ").append(COLUMN_DUE_DATE).append(" BETWEEN ? AND ?");
                    argsList.add(startDate);
                    argsList.add(endDate);
                } else {
                    // Invalid date range
                    Toast.makeText(context, "Start date must be before or equal to end date.", Toast.LENGTH_SHORT).show();
                    return taskList;
                }
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(context, "Invalid date format.", Toast.LENGTH_SHORT).show();
                return taskList;
            }
        } else if ((startDate != null && !startDate.isEmpty()) || (endDate != null && !endDate.isEmpty())) {
            // Only one of the dates is provided
            Toast.makeText(context, "Please provide both start and end dates for a date range.", Toast.LENGTH_SHORT).show();
            return taskList;
        }

        String query = queryBuilder.toString();
        String[] args = argsList.toArray(new String[0]);

        // Debugging: Log the query and arguments
        Log.d("DatabaseHelper", "Query: " + query);
        Log.d("DatabaseHelper", "Args: " + java.util.Arrays.toString(args));

        Cursor cursor = db.rawQuery(query, args);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Task task = new Task(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETION_STATUS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL))
                );
                taskList.add(task);
            }
            cursor.close();
        }

        return taskList;
    }

    // Add a new task
    public boolean insertTask(String title, String description, String dueDate, String dueTime,
                              String priority, int completionStatus, String reminderTime, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TASK_TITLE, title);
        contentValues.put(COLUMN_TASK_DESCRIPTION, description);
        contentValues.put(COLUMN_DUE_DATE, dueDate); // Now in yyyy-MM-dd
        contentValues.put(COLUMN_DUE_TIME, dueTime);
        contentValues.put(COLUMN_PRIORITY, priority);
        contentValues.put(COLUMN_COMPLETION_STATUS, completionStatus);
        contentValues.put(COLUMN_REMINDER_TIME, reminderTime);
        contentValues.put(COLUMN_USER_EMAIL, userEmail);
        long result = db.insert(TABLE_TASKS, null, contentValues);
        db.close();
        return result != -1;
    }

    // Load all tasks for a user
    public List<Task> getAllTasksForUser(String email) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_USER_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // Your existing task creation logic
                @SuppressLint("Range") Task task = new Task(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_TASK_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DUE_DATE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DUE_TIME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_PRIORITY)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_COMPLETION_STATUS)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_REMINDER_TIME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_USER_EMAIL))

                );
                taskList.add(task);
            }
            cursor.close();
        }
        return taskList;
    }

    // Update a task
    public boolean updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TASK_TITLE, task.getTitle());
        contentValues.put(COLUMN_TASK_DESCRIPTION, task.getDescription());
        contentValues.put(COLUMN_DUE_DATE, task.getDueDate());
        contentValues.put(COLUMN_DUE_TIME, task.getDueTime());
        contentValues.put(COLUMN_PRIORITY, task.getPriority());

        int rowsUpdated = db.update(TABLE_TASKS, contentValues, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
        return rowsUpdated > 0;
    }

    // Update task reminder time
    public boolean updateTaskReminderTime(int taskId, String reminderTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REMINDER_TIME, reminderTime);

        int rowsUpdated = db.update(TABLE_TASKS, values, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
        return rowsUpdated > 0;
    }


    // Update task completion status
    public boolean updateTaskCompletionStatus(int taskId, int newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMPLETION_STATUS, newStatus);
        int rowsAffected = db.update(TABLE_TASKS, values, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
        return rowsAffected > 0;
    }

    // Retrieve Completed Tasks
    public List<Task> getCompletedTasksForUser(String email) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_USER_EMAIL + " = ? AND " + COLUMN_COMPLETION_STATUS + " = ? ORDER BY " + COLUMN_DUE_DATE + " ASC, " + COLUMN_DUE_TIME + " ASC";
        Cursor cursor = db.rawQuery(query, new String[]{email, "1"});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") Task task = new Task(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_TASK_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DUE_DATE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DUE_TIME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_PRIORITY)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_COMPLETION_STATUS)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_REMINDER_TIME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_USER_EMAIL))
                );
                taskList.add(task);
            }
            cursor.close();
        }
        return taskList;
    }


    // Delete a task
    public boolean deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_TASKS, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
        return rowsDeleted > 0;
    }


    // Add new user
    public boolean insertUser(String email, String firstName, String lastName, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_EMAIL, email);
        contentValues.put(COLUMN_FIRST_NAME, firstName);
        contentValues.put(COLUMN_LAST_NAME, lastName);
        contentValues.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, contentValues);
        db.close();
        return result != -1; // Return true if insertion was successful
    }

    // Authenticate user
    public boolean authenticateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE "
                + COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});
        boolean isAuthenticated = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return isAuthenticated;
    }

    public Cursor getUserInfo(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        return db.rawQuery(query, new String[]{email});
    }

    public boolean updateUserEmail(String oldEmail, String newEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_EMAIL, newEmail);

        int rowsUpdated = db.update(TABLE_USERS, contentValues, COLUMN_EMAIL + " = ?", new String[]{oldEmail});
        db.close();
        return rowsUpdated > 0;
    }

    public boolean updateUserPassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_PASSWORD, newPassword);

        int rowsUpdated = db.update(TABLE_USERS, contentValues, COLUMN_EMAIL + " = ?", new String[]{email});
        db.close();
        return rowsUpdated > 0;
    }

    public boolean updateUserTasksEmail(String oldEmail, String newEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, newEmail); // Update the UserEmail in Tasks table

        // Update the tasks where the UserEmail matches the old email
        int rowsAffected = db.update(TABLE_TASKS, values, COLUMN_USER_EMAIL + " = ?", new String[]{oldEmail});
        db.close();

        return rowsAffected > 0;
    }


    // Check if the email exists in the Users table
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        boolean exists = cursor.getCount() > 0;
        cursor.close();

        return exists;
    }
}
