package com.sis.app.service;

import com.sis.app.dao.CourseDao;
import com.sis.app.dao.InstructorDao;
import com.sis.app.dao.SectionDao;
import com.sis.app.dao.SettingsDao;
import com.sis.app.dao.StudentDao;
import com.sis.app.dao.UserDao;
import com.sis.app.model.Course;
import com.sis.app.model.Instructor;
import com.sis.app.model.Section;
import com.sis.app.model.Student;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;

public class AdminService {
    private final UserDao userDao;
    private final StudentDao studentDao;
    private final InstructorDao instructorDao;
    private final CourseDao courseDao;
    private final SectionDao sectionDao;
    private final SettingsDao settingsDao;

    public AdminService() {
        this.userDao = new UserDao();
        this.studentDao = new StudentDao();
        this.instructorDao = new InstructorDao();
        this.courseDao = new CourseDao();
        this.sectionDao = new SectionDao();
        this.settingsDao = new SettingsDao();
    }

    public void createStudentAccount(String username, String password, int rollNumber, String program, int year)
            throws SQLException, ServiceException {
        validateUsername(username);
        validatePassword(password);
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        try {
            userDao.insertUser(username, "STUDENT", passwordHash);
            Student student = new Student(username, rollNumber, program, year);
            studentDao.insertStudent(student);
        } catch (SQLException ex) {
            userDao.deleteByUsername(username);
            throw ex;
        }
    }

    public void createInstructorAccount(String username, String password, String department)
            throws SQLException, ServiceException {
        validateUsername(username);
        validatePassword(password);
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        try {
            userDao.insertUser(username, "INSTRUCTOR", passwordHash);
            Instructor instructor = new Instructor(username, department);
            instructorDao.insertInstructor(instructor);
        } catch (SQLException ex) {
            userDao.deleteByUsername(username);
            throw ex;
        }
    }

    public void createCourse(String code, String title, int credits) throws SQLException, ServiceException {
        if (code == null || code.isBlank()) {
            throw new ServiceException("Course code is required.");
        }
        if (title == null || title.isBlank()) {
            throw new ServiceException("Course title is required.");
        }
        if (credits <= 0) {
            throw new ServiceException("Credits must be positive.");
        }
        Course course = new Course(code.trim(), title.trim(), credits);
        courseDao.insertCourse(course);
    }

    public int createSection(String courseCode, String instructorId, String dayTime, String room,
                             int capacity, int semester, int year) throws SQLException, ServiceException {
        if (capacity <= 0) {
            throw new ServiceException("Capacity must be positive.");
        }
        Section section = new Section();
        section.setCourseId(courseCode);
        section.setInstructorId(instructorId);
        section.setDayTime(dayTime);
        section.setRoom(room);
        section.setCapacity(capacity);
        section.setSemester(semester);
        section.setYear(year);
        return sectionDao.insertSection(section);
    }

    public void assignInstructor(int sectionId, String instructorId) throws SQLException {
        sectionDao.updateInstructor(sectionId, instructorId);
    }

    public List<Course> getAllCourses() throws SQLException {
        return courseDao.findAllCourses();
    }

    public List<Instructor> getAllInstructors() throws SQLException {
        return instructorDao.findAll();
    }

    public List<Student> getAllStudents() throws SQLException {
        return studentDao.findAll();
    }

    public List<Section> getAllSections() throws SQLException {
        return sectionDao.findAll();
    }

    public boolean isMaintenanceMode() throws SQLException {
        return settingsDao.isMaintenanceMode();
    }

    public void setMaintenanceMode(boolean maintenance) throws SQLException {
        settingsDao.setMaintenanceMode(maintenance);
    }

    private void validateUsername(String username) throws SQLException, ServiceException {
        if (username == null || username.trim().isEmpty()) {
            throw new ServiceException("Username is required.");
        }
        if (userDao.usernameExists(username)) {
            throw new ServiceException("Username already exists.");
        }
    }

    private void validatePassword(String password) throws ServiceException {
        if (password == null || password.length() < 6) {
            throw new ServiceException("Password must be at least 6 characters long.");
        }
    }
}

