package edu.univ.erp.service;

import edu.univ.erp.dao.*;
import edu.univ.erp.model.*;
import edu.univ.erp.model.view.GradeView;
import edu.univ.erp.util.DatabaseSetup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class StudentServiceTest {

    private static StudentService studentService;
    private static UserDao userDao;
    private static StudentDao studentDao;
    private static CourseDao courseDao;
    private static SectionDao sectionDao;
    private static EnrollmentDao enrollmentDao;
    private static GradeDao gradeDao;
    private static InstructorDao instructorDao;

    @BeforeAll
    public static void setUp() throws Exception {
        DatabaseSetup.init();
        studentService = new StudentService();
        userDao = new UserDao();
        studentDao = new StudentDao();
        courseDao = new CourseDao();
        sectionDao = new SectionDao();
        enrollmentDao = new EnrollmentDao();
        gradeDao = new GradeDao();
        instructorDao = new InstructorDao();
    }

    @Test
    public void testGetGradesAndGPA() throws SQLException, ServiceException {
        // Setup Data
        String username = "stud_" + UUID.randomUUID().toString().substring(0, 8);
        int userId = (int) userDao.insertUser(username, "STUDENT", "hash");
        Student student = new Student(userId, 102, "CS", 1);
        studentDao.insertStudent(student);

        String instUsername = "inst_" + UUID.randomUUID().toString().substring(0, 8);
        int instUserId = (int) userDao.insertUser(instUsername, "INSTRUCTOR", "hash");
        Instructor instructor = new Instructor(instUserId, "CS");
        instructorDao.insertInstructor(instructor);

        String courseCode1 = "CS" + (100 + (int) (Math.random() * 900));
        Course course1 = new Course(courseCode1, "Course 1", 3);
        courseDao.insertCourse(course1);

        String courseCode2 = "CS" + (100 + (int) (Math.random() * 900));
        Course course2 = new Course(courseCode2, "Course 2", 4);
        courseDao.insertCourse(course2);

        Section section1 = new Section();
        section1.setCourseId(courseCode1);
        section1.setInstructorId(instUserId);
        section1.setDayTime("Mon 10:00");
        section1.setRoom("101");
        section1.setCapacity(30);
        section1.setSemester(1);
        section1.setYear(2023);
        int sectionId1 = sectionDao.insertSection(section1);

        Section section2 = new Section();
        section2.setCourseId(courseCode2);
        section2.setInstructorId(instUserId);
        section2.setDayTime("Tue 10:00");
        section2.setRoom("102");
        section2.setCapacity(30);
        section2.setSemester(1);
        section2.setYear(2023);
        int sectionId2 = sectionDao.insertSection(section2);

        // Enroll
        Enrollment enrollment1 = enrollmentDao.insertEnrollment(userId, sectionId1);
        Enrollment enrollment2 = enrollmentDao.insertEnrollment(userId, sectionId2);

        // Add Grades
        // Course 1: 95 (A = 4.0)
        // Insert initial grade record first
        gradeDao.upsertGrade(enrollment1.getEnrollmentId(), 1, 95, 95);
        gradeDao.updateFinalScore(enrollment1.getEnrollmentId(), 95);

        // Course 2: 85 (B = 3.0)
        // Insert initial grade record first
        gradeDao.upsertGrade(enrollment2.getEnrollmentId(), 1, 85, 85);
        gradeDao.updateFinalScore(enrollment2.getEnrollmentId(), 85);

        // Test getGrades
        List<GradeView> grades = studentService.getGrades(userId);
        assertEquals(2, grades.size());

        // Test GPA
        // (3 * 4.0 + 4 * 3.0) / (3 + 4) = (12 + 12) / 7 = 24 / 7 = 3.428...
        double gpa = studentService.calculateGPA(userId);
        assertEquals(3.43, gpa, 0.01);
    }
}
