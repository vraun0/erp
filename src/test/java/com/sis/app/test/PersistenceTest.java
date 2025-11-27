package edu.univ.erp.test;

import edu.univ.erp.dao.*;
import edu.univ.erp.model.*;
import edu.univ.erp.util.DatabaseSetup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PersistenceTest {

    @BeforeAll
    public static void setUp() throws Exception {
        DatabaseSetup.init();
    }

    @Test
    public void testUserPersistence() throws SQLException {
        UserDao userDao = new UserDao();
        String username = "testuser_" + UUID.randomUUID().toString().substring(0, 8);

        // Create
        long userId = userDao.insertUser(username, "STUDENT", "hash123");
        assertTrue(userId > 0);

        // Retrieve
        User user = userDao.findByUsername(username);
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals("STUDENT", user.getRole());
    }

    @Test
    public void testSettingsSingleton() throws SQLException {
        SettingsDao settingsDao = new SettingsDao();

        // Ensure we can read settings
        boolean maintenance = settingsDao.isMaintenanceMode();
        assertFalse(maintenance); // Default is false

        // Update settings
        settingsDao.setMaintenanceMode(true);
        assertTrue(settingsDao.isMaintenanceMode());

        // Reset
        settingsDao.setMaintenanceMode(false);
        assertFalse(settingsDao.isMaintenanceMode());
    }

    @Test
    public void testCourseAndSectionPersistence() throws SQLException {
        CourseDao courseDao = new CourseDao();
        SectionDao sectionDao = new SectionDao();

        String courseCode = "CS" + (100 + (int) (Math.random() * 900));

        // Create Course
        Course course = new Course();
        course.setCode(courseCode);
        course.setTitle("Test Course " + courseCode);
        course.setCredits(3);
        courseDao.insertCourse(course);

        // Verify Course
        Optional<Course> retrievedCourse = courseDao.findByCode(courseCode);
        assertTrue(retrievedCourse.isPresent());
        assertEquals(course.getTitle(), retrievedCourse.get().getTitle());

        // Create Instructor (Dependency for Section)
        InstructorDao instructorDao = new InstructorDao();
        String instructorUsername = "inst_" + UUID.randomUUID().toString().substring(0, 8);
        // Need to create user first for foreign key
        UserDao userDao = new UserDao();
        long userIdLong = userDao.insertUser(instructorUsername, "INSTRUCTOR", "hash");
        int instructorId = (int) userIdLong;

        Instructor instructor = new Instructor();
        instructor.setUserId(instructorId);
        instructor.setDepartment("CS");
        instructorDao.insertInstructor(instructor);

        // Create Section
        Section section = new Section();
        section.setCourseId(courseCode);
        section.setInstructorId(instructorId);
        section.setDayTime("Mon 10:00");
        section.setRoom("101");
        section.setCapacity(30);
        section.setSemester(1);
        section.setYear(2023);

        int sectionId = sectionDao.insertSection(section);
        assertTrue(sectionId > 0);

        // Verify Section
        Optional<Section> retrievedSection = sectionDao.findById(sectionId);
        assertTrue(retrievedSection.isPresent());
        assertEquals(courseCode, retrievedSection.get().getCourseId());
        assertEquals(Integer.valueOf(instructorId), retrievedSection.get().getInstructorId());
    }

    @Test
    public void testEnrollmentAndGrades() throws SQLException {
        // Setup dependencies
        UserDao userDao = new UserDao();
        StudentDao studentDao = new StudentDao();
        CourseDao courseDao = new CourseDao();
        SectionDao sectionDao = new SectionDao();
        EnrollmentDao enrollmentDao = new EnrollmentDao();
        GradeDao gradeDao = new GradeDao();
        InstructorDao instructorDao = new InstructorDao();

        // 1. Create Student
        String studentUsername = "stud_" + UUID.randomUUID().toString().substring(0, 8);
        long studentUserIdLong = userDao.insertUser(studentUsername, "STUDENT", "hash");
        int studentId = (int) studentUserIdLong;

        Student student = new Student();
        student.setUserId(studentId);
        student.setRollNumber((int) (Math.random() * 10000));
        student.setProgram("CS");
        student.setYear(1);
        studentDao.insertStudent(student);

        // 2. Create Instructor & Course & Section
        String instructorUsername = "inst_" + UUID.randomUUID().toString().substring(0, 8);
        long instructorUserIdLong = userDao.insertUser(instructorUsername, "INSTRUCTOR", "hash");
        int instructorId = (int) instructorUserIdLong;

        Instructor instructor = new Instructor();
        instructor.setUserId(instructorId);
        instructor.setDepartment("CS");
        instructorDao.insertInstructor(instructor);

        String courseCode = "CS" + (100 + (int) (Math.random() * 900));
        Course course = new Course();
        course.setCode(courseCode);
        course.setTitle("Test Course");
        course.setCredits(3);
        courseDao.insertCourse(course);

        Section section = new Section();
        section.setCourseId(courseCode);
        section.setInstructorId(instructorId);
        section.setDayTime("Fri 10:00");
        section.setRoom("102");
        section.setCapacity(30);
        section.setSemester(1);
        section.setYear(2023);
        int sectionId = sectionDao.insertSection(section);

        // 3. Enroll
        Enrollment enrollment = enrollmentDao.insertEnrollment(studentId, sectionId);
        assertNotNull(enrollment);
        assertTrue(enrollment.getEnrollmentId() > 0);

        // 4. Add Grades
        gradeDao.upsertGrade(enrollment.getEnrollmentId(), 1, 85, null);
        gradeDao.upsertGrade(enrollment.getEnrollmentId(), 2, 90, null);

        // Update existing grade (should update, not insert)
        gradeDao.upsertGrade(enrollment.getEnrollmentId(), 1, 95, null);

        gradeDao.updateFinalScore(enrollment.getEnrollmentId(), 88);

        // 5. Verify Grades
        List<Grade> grades = gradeDao.findByEnrollment(enrollment.getEnrollmentId());
        assertFalse(grades.isEmpty());
        // We expect 2 components (not 3)
        assertEquals(2, grades.size());

        // Check values
        boolean foundComp1 = false;
        for (Grade g : grades) {
            if (g.getComponent() == 1) {
                assertEquals(95, g.getScore()); // Should be the updated score
                assertEquals(Integer.valueOf(88), g.getFinalScore());
                foundComp1 = true;
            }
        }
        assertTrue(foundComp1);
    }
}
