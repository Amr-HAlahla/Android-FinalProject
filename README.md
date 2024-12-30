# Task Management App

## Overview

This is a Task Management Android application developed as part of the ENCS5150 course project. The app is designed to help multiple users manage their daily to-do tasks efficiently. Users can sign up, log in, and manage tasks with features like reminders, task prioritization, and search functionality.

## Features

### Authentication
- **Signup, Sign in, and Logout**:
  - Secure authentication with email and password.
  - "Remember me" functionality to save email credentials using shared preferences.

### Task Management
- **Create, View, and Manage Tasks**:
  - Add tasks with details: title, description, due date & time, priority level, and completion status.
  - Edit, delete, or mark tasks as completed.
  - View tasks for today, all tasks, or completed tasks.

- **Search and Filter**:
  - Search tasks by keywords in the title or description.
  - Filter tasks by date range.

- **Task Prioritization**:
  - Assign priority levels (High, Medium, Low) to tasks.
  - Sort tasks by priority.

- **Task Sharing and Import**:
  - Share tasks via email.
  - Import tasks using a dummy REST API.

### Notifications
- Set notification alerts for tasks.
- Customize notification times (e.g., a day or a few hours before the task).

### User Profile
- View and edit email and password.

### Additional Features
- Dark and light mode support.
- Congratulatory message with animation when all tasks for the day are completed.

### UI Design
The UI is designed to be clear, visually appealing, and intuitive, with a focus on user experience.

## Technical Details
- **Framework**: Android Studio
- **Languages**: Java/Kotlin
- **Database**: SQLite
- **Other Technologies**:
  - Shared Preferences
  - Fragments
  - Toast Messages
  - Animations (Frame/Tween)

## Requirements
- **Target Device**: Pixel 3a XL
- **API Level**: 26 (Android Oreo)

## Installation
1. Download the APK file from the repository.
2. Install the APK on your device.
3. Launch the app and sign up to start managing tasks.


## License
This project is for educational purposes as part of the ENCS5150 course and is not licensed for commercial use.
