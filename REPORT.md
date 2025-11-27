# Project Report: University ERP System

## 1. Introduction
The University ERP System is a Java Swing-based desktop application designed to manage core academic processes. It serves three primary user roles: Students, Instructors, and Administrators, providing a secure and efficient environment for course registration, grading, and system management.

## 2. Key Features & Implementation

### 2.1 Authentication & Security
-   **Dual Database Architecture**: `AuthDB` stores user credentials and security data, while `ERPDB` stores academic records. This separation enhances security.
-   **Password Hashing**: All passwords are hashed using BCrypt before storage. Plain text passwords are never stored.
-   **Account Lockout**: To prevent brute-force attacks, accounts are locked for 15 minutes after 5 consecutive failed login attempts.
-   **Password History**: Users cannot reuse their last 3 passwords when changing credentials.

### 2.2 Student Module
-   **Course Catalog**: Students can browse available course sections, viewing details like schedule, room, and instructor.
-   **Registration**: The system enforces business rules such as:
    -   **Capacity Checks**: Prevents registration if a section is full.
    -   **Prerequisites**: Enforces sequence rules (e.g., CS201 requires passing CS101).
    -   **Duplicate Prevention**: Prevents double booking.
-   **Timetable**: A visual schedule of registered classes.

### 2.3 Instructor Module
-   **Gradebook**: Instructors can view enrolled students and manage grades (Midterm, Final, Project).
-   **CSV Import/Export**: Grades can be bulk imported or exported via CSV files for offline processing.
-   **Statistics**: The system calculates class averages, standard deviations, and grade distributions.

### 2.4 Admin Module
-   **User & Course Management**: Admins can create users, courses, and sections.
-   **Maintenance Mode**: A global switch that restricts write access for non-admins, displaying a system-wide banner.
-   **Backup & Restore**: Integrated tools to backup the `ERPDB` database to a SQL file and restore it.

## 3. Database Schema

### AuthDB
-   `users_auth`: `user_id` (PK), `username`, `password_hash`, `role`, `failed_attempts`, `lockout_time`.
-   `password_history`: `history_id` (PK), `user_id` (FK), `password_hash`, `changed_at`.

### ERPDB
-   `students`: `user_id` (PK, FK), `roll_no`, `program`, `year`.
-   `instructors`: `user_id` (PK, FK), `department`.
-   `courses`: `code` (PK), `title`, `credits`.
-   `sections`: `section_id` (PK), `course_id` (FK), `instructor_id` (FK), `day_time`, `room`, `capacity`.
-   `enrollments`: `enrollment_id` (PK), `student_id` (FK), `section_id` (FK), `status`.
-   `grades`: `enrollment_id` (FK), `component`, `score`, `final_score`.
-   `settings`: `id` (PK), `maintenance`.

## 4. Grading Rule
Final grades are computed based on a weighted sum of components. The default strategy sums the available scores (Midterm, Final, Project) to determine the total score out of 100.

## 5. Conclusion
The system meets all functional requirements and includes several bonus features like CSV handling, advanced security (lockout/history), and database backup tools, ensuring a robust and user-friendly experience.
