package edu.univ.erp.service;

import edu.univ.erp.dao.InstructorDao;
import edu.univ.erp.dao.SectionDao;
import edu.univ.erp.dao.StudentDao;
import edu.univ.erp.dao.UserDao;
import edu.univ.erp.model.Instructor;
import edu.univ.erp.model.Section;
import edu.univ.erp.model.Student;
import edu.univ.erp.util.DatabaseSetup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AccessControlServiceTest {

    private static AccessControlService accessControlService;
    private static UserDao userDao;
    private static StudentDao studentDao;
    private static InstructorDao instructorDao;
    private static SectionDao sectionDao;

    @BeforeAll
    public static void setUp() throws Exception {
        DatabaseSetup.init();
        accessControlService = new AccessControlService();
        userDao = new UserDao();
        studentDao = new StudentDao();
        instructorDao = new InstructorDao();
        sectionDao = new SectionDao();
    }

    @Test
    public void testCanEditSection() throws SQLException {
        // Create Instructor
        String username = "inst_" + UUID.randomUUID().toString().substring(0, 8);
        int userId = (int) userDao.insertUser(username, "INSTRUCTOR", "hash");
        Instructor instructor = new Instructor(userId, "CS");
        instructorDao.insertInstructor(instructor);

        // Create Course
        edu.univ.erp.dao.CourseDao courseDao = new edu.univ.erp.dao.CourseDao();
        String courseCode = "TEST_CS101_" + UUID.randomUUID().toString().substring(0, 8);
        edu.univ.erp.model.Course course = new edu.univ.erp.model.Course(courseCode, "Intro to CS", 3);
        courseDao.insertCourse(course);

        // Create Section assigned to instructor
        Section section = new Section();
        section.setCourseId(courseCode);
        section.setInstructorId(userId);
        section.setDayTime("Mon 10:00");
        section.setRoom("101");
        section.setCapacity(30);
        section.setSemester(1);
        section.setYear(2023);
        int sectionId = sectionDao.insertSection(section);

        // Verify access
        assertTrue(accessControlService.canEditSection(userId, sectionId));

        // Create another instructor
        String otherUsername = "inst_" + UUID.randomUUID().toString().substring(0, 8);
        int otherUserId = (int) userDao.insertUser(otherUsername, "INSTRUCTOR", "hash");
        Instructor otherInstructor = new Instructor(otherUserId, "CS");
        instructorDao.insertInstructor(otherInstructor);

        // Verify denial
        assertFalse(accessControlService.canEditSection(otherUserId, sectionId));
    }

    @Test
    public void testCanAccessStudentData() throws SQLException {
        // Create Student
        String username = "stud_" + UUID.randomUUID().toString().substring(0, 8);
        int userId = (int) userDao.insertUser(username, "STUDENT", "hash");
        Student student = new Student(userId, 101, "CS", 1);
        studentDao.insertStudent(student);

        // Verify access to own data
        assertTrue(accessControlService.canAccessStudentData(userId, userId));

        // Create another student
        String otherUsername = "stud_" + UUID.randomUUID().toString().substring(0, 8);
        int otherUserId = (int) userDao.insertUser(otherUsername, "STUDENT", "hash");

        // Verify denial to access other's data
        assertFalse(accessControlService.canAccessStudentData(otherUserId, userId));
    }

    @Test
    public void testIsAdmin() throws SQLException {
        String username = "admin_" + UUID.randomUUID().toString().substring(0, 8);
        int userId = (int) userDao.insertUser(username, "ADMIN", "hash");

        assertTrue(accessControlService.isAdmin(userId));

        String studUsername = "stud_" + UUID.randomUUID().toString().substring(0, 8);
        int studUserId = (int) userDao.insertUser(studUsername, "STUDENT", "hash");

        assertFalse(accessControlService.isAdmin(studUserId));
    }
}
