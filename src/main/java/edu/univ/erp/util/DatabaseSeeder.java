package edu.univ.erp.util;

import edu.univ.erp.config.DatabaseManager;
import edu.univ.erp.dao.CourseDao;
import edu.univ.erp.dao.EnrollmentDao;
import edu.univ.erp.dao.InstructorDao;
import edu.univ.erp.dao.SectionDao;
import edu.univ.erp.dao.StudentDao;
import edu.univ.erp.model.Course;
import edu.univ.erp.model.Instructor;
import edu.univ.erp.model.Section;
import edu.univ.erp.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseSeeder {

    public static void seedIfEmpty() {
        try {
            System.out.println("Checking database state...");

            // 1. Ensure Users, Instructors, and Students exist
            seedUsers();
            seedInstructors();
            seedStudents();

            // 2. Ensure Courses and Sections exist
            ensureCourses();
            ensureSections();

            // 3. Ensure Enrollments and Grades
            System.out.println("Checking/Seeding Enrollments and Grades...");
            seedEnrollments();
            seedGrades();

            System.out.println("Database seeding complete.");
        } catch (Exception e) {
            System.err.println("Failed to seed database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedUsers() throws SQLException {
        String sql = "INSERT IGNORE INTO users_auth (user_id, username, role, password_hash, status) VALUES (?, ?, ?, ?, 'ACTIVE')";
        // Password is 'password'
        String hash = "$2a$10$R0wxQgPgGNy9eknmBn5KDu1U.i41jnIiRS007aTMjDWXk0G5liPLO";

        try (Connection conn = DatabaseManager.getInstance().getAuthConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Admin
            stmt.setInt(1, 1000);
            stmt.setString(2, "admin");
            stmt.setString(3, "ADMIN");
            stmt.setString(4, hash);
            stmt.addBatch();

            // Instructor: Dr. John Smith
            stmt.setInt(1, 1002);
            stmt.setString(2, "dr.smith");
            stmt.setString(3, "INSTRUCTOR");
            stmt.setString(4, hash);
            stmt.addBatch();

            // Student 1: Alice Doe
            stmt.setInt(1, 1001);
            stmt.setString(2, "alice.doe");
            stmt.setString(3, "STUDENT");
            stmt.setString(4, hash);
            stmt.addBatch();

            // Student 2: Bob Lee
            stmt.setInt(1, 1003);
            stmt.setString(2, "bob.lee");
            stmt.setString(3, "STUDENT");
            stmt.setString(4, hash);
            stmt.addBatch();

            stmt.executeBatch();
        }
    }

    private static void seedInstructors() throws SQLException {
        InstructorDao dao = new InstructorDao();
        Instructor i = new Instructor();
        i.setUserId(1002);
        i.setDepartment("Computer Science");
        try {
            dao.insertInstructor(i);
        } catch (SQLException e) {
            // Ignore if exists
        }
    }

    private static void seedStudents() throws SQLException {
        StudentDao dao = new StudentDao();

        // Student 1: Alice Doe
        Student s1 = new Student();
        s1.setUserId(1001);
        s1.setRollNumber(2025001);
        s1.setProgram("B.Sc. Computer Science");
        s1.setYear(1);
        try {
            dao.insertStudent(s1);
        } catch (SQLException e) {
            // Ignore
        }

        // Student 2: Bob Lee
        Student s2 = new Student();
        s2.setUserId(1003);
        s2.setRollNumber(2025002);
        s2.setProgram("B.Sc. Data Science");
        s2.setYear(1);
        try {
            dao.insertStudent(s2);
        } catch (SQLException e) {
            // Ignore
        }
    }

    private static void ensureCourses() throws SQLException {
        CourseDao dao = new CourseDao();
        ensureCourse(dao, "CS101", "Intro to Computer Science", 4);
        ensureCourse(dao, "CS201", "Data Structures & Algorithms", 4);
        ensureCourse(dao, "MATH101", "Calculus I", 3);
        ensureCourse(dao, "ENG101", "Academic Writing", 3);
    }

    private static void ensureCourse(CourseDao dao, String code, String title, int credits) {
        try {
            if (!dao.findByCode(code).isPresent()) {
                Course c = new Course();
                c.setCode(code);
                c.setTitle(title);
                c.setCredits(credits);
                dao.insertCourse(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void ensureSections() throws SQLException {
        SectionDao dao = new SectionDao();

        // CS101: Mon/Wed 10:00 (Dr. Smith)
        ensureSection(dao, "CS101", 1002, "Mon/Wed 10:00-11:30", "Room 101");

        // CS201: Tue/Thu 14:00 (Dr. Smith)
        ensureSection(dao, "CS201", 1002, "Tue/Thu 14:00-15:30", "Room 102");

        // MATH101: Mon/Wed 12:00 (Dr. Smith - assuming he teaches math too for
        // simplicity, or we add another instructor)
        // For simplicity, let's keep Dr. Smith as the super-prof
        ensureSection(dao, "MATH101", 1002, "Mon/Wed 12:00-13:30", "Room 201");

        // ENG101: Fri 09:00 (Dr. Smith)
        ensureSection(dao, "ENG101", 1002, "Fri 09:00-12:00", "Room 305");
    }

    private static void ensureSection(SectionDao dao, String courseId, int instructorId, String dayTime, String room) {
        try (Connection conn = DatabaseManager.getInstance().getErpConnection()) {
            // Check if section exists for this course
            int sectionId = -1;
            try (PreparedStatement stmt = conn
                    .prepareStatement("SELECT section_id FROM sections WHERE course_id = ? LIMIT 1")) {
                stmt.setString(1, courseId);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        sectionId = rs.getInt(1);
                    }
                }
            }

            if (sectionId != -1) {
                // Update instructor
                try (PreparedStatement stmt = conn
                        .prepareStatement("UPDATE sections SET instructor_id = ? WHERE section_id = ?")) {
                    stmt.setInt(1, instructorId);
                    stmt.setInt(2, sectionId);
                    stmt.executeUpdate();
                }
            } else {
                // Insert
                Section s = new Section();
                s.setCourseId(courseId);
                s.setInstructorId(instructorId);
                s.setDayTime(dayTime);
                s.setRoom(room);
                s.setCapacity(40);
                s.setSemester(1);
                s.setYear(2025);
                dao.insertSection(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void seedEnrollments() throws SQLException {
        EnrollmentDao dao = new EnrollmentDao();

        int cs101Id = getSectionId("CS101", 1002);
        int cs201Id = getSectionId("CS201", 1002);
        int math101Id = getSectionId("MATH101", 1002);
        int eng101Id = getSectionId("ENG101", 1002);

        // Alice Doe (1001): CS101, MATH101, ENG101
        if (cs101Id != -1)
            tryInsertEnrollment(dao, 1001, cs101Id);
        if (math101Id != -1)
            tryInsertEnrollment(dao, 1001, math101Id);
        if (eng101Id != -1)
            tryInsertEnrollment(dao, 1001, eng101Id);

        // Bob Lee (1003): CS101, CS201
        if (cs101Id != -1)
            tryInsertEnrollment(dao, 1003, cs101Id);
        if (cs201Id != -1)
            tryInsertEnrollment(dao, 1003, cs201Id);
    }

    private static void tryInsertEnrollment(EnrollmentDao dao, int studentId, int sectionId) {
        try {
            dao.insertEnrollment(studentId, sectionId);
        } catch (Exception e) {
            // Ignore duplicates
        }
    }

    private static int getSectionId(String courseId, int instructorId) {
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT section_id FROM sections WHERE course_id = ? AND instructor_id = ? LIMIT 1")) {
            stmt.setString(1, courseId);
            stmt.setInt(2, instructorId);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static void seedGrades() throws SQLException {
        edu.univ.erp.dao.GradeDao gradeDao = new edu.univ.erp.dao.GradeDao();

        // Alice (1001) Grades
        // CS101: A
        int aliceCS101 = getEnrollmentId(1001, "CS101");
        if (aliceCS101 != -1) {
            gradeDao.upsertGrade(aliceCS101, 1, 92, null); // Midterm
            gradeDao.upsertGrade(aliceCS101, 2, 95, 94); // Final
        }
        // MATH101: B+
        int aliceMath = getEnrollmentId(1001, "MATH101");
        if (aliceMath != -1) {
            gradeDao.upsertGrade(aliceMath, 1, 85, null);
            gradeDao.upsertGrade(aliceMath, 2, 88, 87);
        }

        // Bob (1003) Grades
        // CS101: B
        int bobCS101 = getEnrollmentId(1003, "CS101");
        if (bobCS101 != -1) {
            gradeDao.upsertGrade(bobCS101, 1, 78, null);
            gradeDao.upsertGrade(bobCS101, 2, 82, 80);
        }
        // CS201: A-
        int bobCS201 = getEnrollmentId(1003, "CS201");
        if (bobCS201 != -1) {
            gradeDao.upsertGrade(bobCS201, 1, 88, null);
            gradeDao.upsertGrade(bobCS201, 2, 91, 90);
        }
    }

    private static int getEnrollmentId(int studentId, String courseId) {
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT e.enrollment_id FROM enrollments e " +
                                "JOIN sections s ON e.section_id = s.section_id " +
                                "WHERE e.student_id = ? AND s.course_id = ? LIMIT 1")) {
            stmt.setInt(1, studentId);
            stmt.setString(2, courseId);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
