package edu.univ.erp.service;

import edu.univ.erp.dao.*;
import edu.univ.erp.model.*;
import edu.univ.erp.util.DatabaseSetup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class InstructorServiceTest {

    private static InstructorService instructorService;
    private static UserDao userDao;
    private static StudentDao studentDao;
    private static InstructorDao instructorDao;
    private static CourseDao courseDao;
    private static SectionDao sectionDao;
    private static EnrollmentDao enrollmentDao;
    private static GradeDao gradeDao;

    @BeforeAll
    public static void setUp() throws Exception {
        DatabaseSetup.init();
        instructorService = new InstructorService();
        userDao = new UserDao();
        studentDao = new StudentDao();
        instructorDao = new InstructorDao();
        courseDao = new CourseDao();
        sectionDao = new SectionDao();
        enrollmentDao = new EnrollmentDao();
        gradeDao = new GradeDao();
    }

    @Test
    public void testClassStatistics() throws SQLException, ServiceException {
        // Setup Instructor
        String instUsername = "inst_" + UUID.randomUUID().toString().substring(0, 8);
        int instUserId = (int) userDao.insertUser(instUsername, "INSTRUCTOR", "hash");
        Instructor instructor = new Instructor(instUserId, "CS");
        instructorDao.insertInstructor(instructor);

        // Setup Course & Section
        String courseCode = "CS" + (100 + (int) (Math.random() * 900));
        Course course = new Course(courseCode, "Stats Course", 3);
        courseDao.insertCourse(course);

        Section section = new Section();
        section.setCourseId(courseCode);
        section.setInstructorId(instUserId);
        section.setDayTime("Mon 10:00");
        section.setRoom("101");
        section.setCapacity(30);
        section.setSemester(1);
        section.setYear(2023);
        int sectionId = sectionDao.insertSection(section);

        // Enroll Students
        int[] scores = { 95, 85, 75, 65, 55 }; // A, B, C, D, F
        for (int score : scores) {
            String studUsername = "stud_" + UUID.randomUUID().toString().substring(0, 8);
            int studUserId = (int) userDao.insertUser(studUsername, "STUDENT", "hash");
            Student student = new Student(studUserId, (int) (Math.random() * 10000), "CS", 1);
            studentDao.insertStudent(student);

            Enrollment enrollment = enrollmentDao.insertEnrollment(studUserId, sectionId);
            // Insert initial grade record first
            gradeDao.upsertGrade(enrollment.getEnrollmentId(), 1, score, score);
        }

        // Test Statistics
        ClassStatistics stats = instructorService.getClassStatistics(instUserId, sectionId);

        assertEquals(5, stats.getStudentCount());
        assertEquals(95, stats.getMaxScore());
        assertEquals(55, stats.getMinScore());
        assertEquals(75.0, stats.getAverageScore(), 0.01);

        // Std Dev of 95, 85, 75, 65, 55
        // Mean = 75
        // Variance = ((20^2 + 10^2 + 0^2 + 10^2 + 20^2) / 5) = (400+100+0+100+400)/5 =
        // 1000/5 = 200
        // StdDev = sqrt(200) = 14.14
        assertEquals(14.14, stats.getStdDev(), 0.01);

        // Distribution
        assertEquals(1, stats.getGradeDistribution().get("A"));
        assertEquals(1, stats.getGradeDistribution().get("B"));
        assertEquals(1, stats.getGradeDistribution().get("C"));
        assertEquals(1, stats.getGradeDistribution().get("D"));
        assertEquals(1, stats.getGradeDistribution().get("F"));
    }
}
