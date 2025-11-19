# Student Information System (SIS)

A desktop Student Information System implemented in Java Swing with a MariaDB backend. The application provides dedicated portals for students, instructors, and administrators, covering course registration, grading, schedule management, and system administration.

## Highlights

- **Role-based login** with password hashing (jBCrypt) and pooled connections (HikariCP)
- **Student portal** for course catalog, registrations, timetable, grades, and CSV export
- **Instructor portal** for viewing assigned sections, managing gradebooks, and exporting grade sheets
- **Admin console** to create users/courses/sections and toggle maintenance mode
- **Maintenance safeguards** – student and instructor actions respect the maintenance toggle

## Technology Stack

- Java 17+
- Swing + MigLayout + FlatLaf
- MariaDB 10+
- Maven for build/dependency management
- OpenCSV for exports

## Prerequisites

| Component | Version | Notes |
|-----------|---------|-------|
| Java      | 17 or higher | Swing UI |
| Maven     | 3.6+   | Build and run |
| MariaDB   | 10+    | Databases `AuthDB` and `ERPDB` |

## Database Setup

1. Ensure MariaDB is running and you can connect as root.
2. Create the databases and tables (sample script snippet):
   ```sql
   CREATE DATABASE IF NOT EXISTS AuthDB;
   CREATE DATABASE IF NOT EXISTS ERPDB;

   USE AuthDB;
   CREATE TABLE users_auth (
       user_id BIGINT PRIMARY KEY,
       username VARCHAR(512) UNIQUE NOT NULL,
       role VARCHAR(100) NOT NULL,
       password_hash VARCHAR(512) NOT NULL,
       status VARCHAR(100) DEFAULT 'ACTIVE',
       last_login DATETIME NULL
   );

   USE ERPDB;
   CREATE TABLE students (
       user_id VARCHAR(512) PRIMARY KEY,
       roll_no INT NOT NULL,
       program VARCHAR(100),
       year INT
   );
   CREATE TABLE instructors (
       user_id VARCHAR(512) PRIMARY KEY,
       department VARCHAR(512)
   );
   CREATE TABLE courses (
       code VARCHAR(100) PRIMARY KEY,
       title VARCHAR(100),
       credits INT
   );
   CREATE TABLE sections (
       section_id INT AUTO_INCREMENT PRIMARY KEY,
       course_id VARCHAR(100) NOT NULL,
       instructor_id VARCHAR(512) NOT NULL,
       day_time VARCHAR(100),
       room VARCHAR(100),
       capacity INT,
       semester INT,
       year INT
   );
   CREATE TABLE enrollments (
       enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
       student_id VARCHAR(512) NOT NULL,
       section_id INT,
       status VARCHAR(100)
   );
   CREATE TABLE grades (
       enrollment_id INT NOT NULL,
       component INT NOT NULL,
       score INT,
       final_score INT,
       PRIMARY KEY (enrollment_id, component)
   );
   CREATE TABLE settings (
       maintenance TINYINT(1) NOT NULL
   );
   TRUNCATE TABLE settings;
   INSERT INTO settings VALUES (0);
   ```
3. Insert the default data set (hashed passwords included below):
   ```sql
   USE AuthDB;
   INSERT INTO users_auth VALUES
     (1,'admin','ADMIN','$2a$10$fEs5Z8d/A72htuXe/hvsEecJOoMbcKVkm686d0ltpETgZLJ5dZNo.', 'ACTIVE', NULL),
     (2,'instructor1','INSTRUCTOR','$2a$10$MaBi2EyUrLDruMXJPK5Auu5IHblM2XjQThWyFtPIMjF2Fdx3kJXRq', 'ACTIVE', NULL),
     (3,'student1','STUDENT','$2a$10$NXpp4V2A16RovBc.8tDNV.tpaWurwhEdtVkoncoCJIUZ2JerLMI.G', 'ACTIVE', NULL),
     (4,'student2','STUDENT','$2a$10$NXpp4V2A16RovBc.8tDNV.tpaWurwhEdtVkoncoCJIUZ2JerLMI.G', 'ACTIVE', NULL);

   USE ERPDB;
   INSERT INTO students VALUES ('student1',10001,'Computer Science',2),
                                ('student2',10002,'Information Technology',1);
   INSERT INTO instructors VALUES ('instructor1','Computer Science');
   INSERT INTO courses VALUES ('CS101','Introduction to Programming',4),
                              ('CS201','Data Structures',3);
   INSERT INTO sections VALUES (1,'CS101','instructor1','Mon/Wed 10:00-11:30','Room 101',30,1,2025),
                               (2,'CS201','instructor1','Tue/Thu 14:00-15:30','Room 202',25,1,2025);
   INSERT INTO enrollments VALUES (1,'student1',1,'ENROLLED'),
                                  (2,'student2',1,'ENROLLED');
   INSERT INTO grades VALUES (1,1,85,0), (1,2,90,0);
   ```

> Tip: the application reads database credentials from environment variables `SIS_DB_USER`, `SIS_DB_PASSWORD`, `SIS_DB_HOST`, `SIS_DB_PORT`. If they are not provided it defaults to `root` / `yeahboi123` on `localhost:3306`.

## Building & Running

```bash
# compile
mvn clean compile

# launch the Swing client
mvn exec:java -Dexec.mainClass="com.sis.app.SISApplication"
```

## Default Accounts

| Role       | Username     | Password   |
|------------|--------------|------------|
| Admin      | `admin`      | `admin123` |
| Instructor | `instructor1`| `teach123` |
| Student    | `student1`   | `learn123` |
| Student    | `student2`   | `learn123` |

## Portals & Features

### Student Portal
- Browse course catalog with seat availability
- Register for sections (capacity checks & maintenance guard)
- Drop sections
- View timetable and current registrations
- Review grades and export personal transcript as CSV

### Instructor Portal
- View assigned sections
- Maintain gradebook with weighted final grade calculation (40% midterm, 50% final, 10% project)
- Save grades to the database
- Export section gradebooks to CSV

### Admin Console
- Create student and instructor accounts (writes to AuthDB & ERPDB)
- Create courses and sections, assign instructors
- Toggle maintenance mode (blocks student/instructor write actions)

## Project Structure (selected)

```
src/main/java/com/sis/app/
├── SISApplication.java
├── config/DatabaseManager.java
├── dao/*.java
├── model/*.java
├── model/view/*.java
├── service/*.java
└── ui/
    ├── student/*
    ├── instructor/*
    └── admin/*
```

## Testing

Legacy sample tests are included. Run `mvn test` to execute the suite.

## Troubleshooting

- **Database errors**: ensure AuthDB/ERPDB exist and credentials match environment variables.
- **UI launch issues**: verify Java 17+ is on PATH.
- **Exports**: choose a writable directory when saving CSV files.

## License

Created for educational purposes. Adapt and extend as needed for coursework or internal demos.


