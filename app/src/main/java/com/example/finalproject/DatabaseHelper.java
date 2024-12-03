package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TaskManager.db";
    private static final int DATABASE_VERSION = 2;

    // User table
    private static final String TABLE_USERS = "Users";
    private static final String COLUMN_EMAIL = "Email";
    static final String COLUMN_FIRST_NAME = "FirstName";
    static final String COLUMN_LAST_NAME = "LastName";
    private static final String COLUMN_PASSWORD = "Password";

    // Task table
    private static final String TABLE_TASKS = "Tasks";
    private static final String COLUMN_TASK_ID = "TaskID";
    private static final String COLUMN_TASK_TITLE = "TaskTitle";
    private static final String COLUMN_TASK_DESCRIPTION = "TaskDescription";
    private static final String COLUMN_DUE_DATE = "DueDate";
    private static final String COLUMN_DUE_TIME = "DueTime"; // New Field
    private static final String COLUMN_PRIORITY = "Priority";
    private static final String COLUMN_COMPLETION_STATUS = "CompletionStatus";
    private static final String COLUMN_REMINDER_TIME = "ReminderTime";
    private static final String COLUMN_USER_EMAIL = "UserEmail"; // Foreign key

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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

    // Add a new task
    public boolean insertTask(String title, String description, String dueDate, String dueTime,
                              String priority, int completionStatus, String reminderTime, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TASK_TITLE, title);
        contentValues.put(COLUMN_TASK_DESCRIPTION, description);
        contentValues.put(COLUMN_DUE_DATE, dueDate);
        contentValues.put(COLUMN_DUE_TIME, dueTime);
        contentValues.put(COLUMN_PRIORITY, priority);
        contentValues.put(COLUMN_COMPLETION_STATUS, completionStatus);
        contentValues.put(COLUMN_REMINDER_TIME, reminderTime);
        contentValues.put(COLUMN_USER_EMAIL, userEmail);
        long result = db.insert(TABLE_TASKS, null, contentValues);
        db.close();
        return result != -1;
    }

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
    public boolean updateTaskCompletionStatus(int taskId, int completionStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_COMPLETION_STATUS, completionStatus);  // Update only completion status

        int rowsUpdated = db.update(TABLE_TASKS, contentValues, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
        return rowsUpdated > 0;  // Return true if the update was successful
    }


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

    public List<Task> searchTasks(String email, String keyword, String startDate, String endDate) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Construct the base query
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_USER_EMAIL + " = ?");
        List<String> args = new ArrayList<>();
        args.add(email);

        // Add keyword filter if provided
        if (!keyword.isEmpty()) {
            queryBuilder.append(" AND (" + COLUMN_TASK_TITLE + " LIKE ? OR " + COLUMN_TASK_DESCRIPTION + " LIKE ?)");
            args.add("%" + keyword + "%");
            args.add("%" + keyword + "%");
        }

        // Add date range filter if provided
        if (startDate != null && !startDate.isEmpty()) {
            queryBuilder.append(" AND " + COLUMN_DUE_DATE + " >= ?");
            args.add(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            queryBuilder.append(" AND " + COLUMN_DUE_DATE + " <= ?");
            args.add(endDate);
        }

        Cursor cursor = db.rawQuery(queryBuilder.toString(), args.toArray(new String[0]));

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


    public List<Task> getTasksForDate(String date) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_DUE_DATE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{date});

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


}
