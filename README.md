# University ERP System

A comprehensive desktop application for managing university courses, enrollments, and grades.

## Prerequisites
- Java 17 or higher
- Maven 3.6+
- MariaDB/MySQL Database

## Setup & Running

1.  **Database Setup**:
    - Ensure your MariaDB server is running.
    - Create two databases: `AuthDB` and `ERPDB`.
    - The application will automatically initialize tables on first run using `auth_schema.sql` and `erp_schema.sql`.

2.  **Configuration**:
    - Set the following environment variables (or rely on defaults in `DatabaseManager.java`):
        - `SIS_DB_USER`: Database username (default: `root`)
        - `SIS_DB_PASSWORD`: Database password (default: `password`)
        - `SIS_DB_HOST`: Database host (default: `localhost`)
        - `SIS_DB_PORT`: Database port (default: `3306`)

3.  **Build**:
    ```bash
    mvn clean package
    ```

4.  **Run**:
    ```bash
    java -jar target/student-information-system-1.0-SNAPSHOT-jar-with-dependencies.jar
    ```
    *Note: If the shaded jar is not configured, run via Maven:*
    ```bash
    mvn exec:java -Dexec.mainClass="edu.univ.erp.SISApplication"
    ```

## Default Credentials

| Role | Username | Password |
| :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` |
| **Instructor** | `dr.smith` | `pass123` |
| **Student** | `alice.doe` | `pass123` |
| **Student** | `bob.lee` | `pass123` |

## Features
- **Role-Based Access**: Distinct dashboards for Students, Instructors, and Admins.
- **Course Management**: Create courses, sections, and assign instructors.
- **Registration**: Students can register/drop courses with prerequisite and capacity checks.
- **Grading**: Instructors can manage grades, import/export CSVs, and view statistics.
- **Security**: Account lockout after 5 failed attempts, password history enforcement.
- **Maintenance Mode**: Admins can lock the system for maintenance (backup/restore included).
