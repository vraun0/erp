package edu.univ.erp.service;

import edu.univ.erp.dao.CourseDao;
import edu.univ.erp.dao.EnrollmentDao;
import edu.univ.erp.dao.FeeDao;
import edu.univ.erp.dao.InstructorDao;
import edu.univ.erp.dao.SectionDao;
import edu.univ.erp.dao.SettingsDao;
import edu.univ.erp.dao.StudentDao;
import edu.univ.erp.dao.UserDao;
import edu.univ.erp.model.Course;
import edu.univ.erp.model.Fee;
import edu.univ.erp.model.Instructor;
import edu.univ.erp.model.Section;
import edu.univ.erp.model.Student;
import edu.univ.erp.model.User;
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
    private final FeeDao feeDao;

    public AdminService() {
        this.userDao = new UserDao();
        this.studentDao = new StudentDao();
        this.instructorDao = new InstructorDao();
        this.courseDao = new CourseDao();
        this.sectionDao = new SectionDao();
        this.settingsDao = new SettingsDao();
        this.feeDao = new FeeDao();
    }

    public void createStudentAccount(String username, String password, int rollNumber, String program, int year)
            throws SQLException, ServiceException {
        validateUsername(username);
        validatePassword(password);
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        try {
            int userId = (int) userDao.insertUser(username, "STUDENT", passwordHash);
            Student student = new Student(userId, rollNumber, program, year);
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
            int userId = (int) userDao.insertUser(username, "INSTRUCTOR", passwordHash);
            Instructor instructor = new Instructor(userId, department);
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
        if (credits < 1 || credits > 12) {
            throw new ServiceException("Credits must be between 1 and 12.");
        }
        Course course = new Course(code.trim(), title.trim(), credits);
        courseDao.insertCourse(course);
    }

    public int createSection(String courseCode, Integer instructorId, String dayTime, String room,
            int capacity, int semester, int year, java.time.LocalDate dropDeadline)
            throws SQLException, ServiceException {
        if (capacity < 1 || capacity > 500) {
            throw new ServiceException("Capacity must be between 1 and 500.");
        }
        if (dropDeadline != null && dropDeadline.isBefore(java.time.LocalDate.now())) {
            throw new ServiceException("Drop deadline cannot be in the past.");
        }
        Section section = new Section();
        section.setCourseId(courseCode);
        section.setInstructorId(instructorId);
        section.setDayTime(dayTime);
        section.setRoom(room);
        section.setCapacity(capacity);
        section.setSemester(semester);
        section.setYear(year);
        section.setDropDeadline(dropDeadline);
        return sectionDao.insertSection(section);
    }

    public void assignInstructor(int sectionId, Integer instructorId) throws SQLException {
        sectionDao.updateInstructor(sectionId, instructorId);
    }

    public void deleteSection(int sectionId) throws SQLException, ServiceException {
        EnrollmentDao enrollmentDao = new EnrollmentDao();
        int activeEnrollments = enrollmentDao.countActiveEnrollmentsForSection(sectionId);
        if (activeEnrollments > 0) {
            throw new ServiceException("Cannot delete section with " + activeEnrollments + " active enrollments.");
        }
        sectionDao.delete(sectionId);
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

    public void toggleMaintenanceMode(boolean enabled) throws ServiceException {
        try {
            settingsDao.setMaintenanceMode(enabled);
        } catch (SQLException e) {
            throw new ServiceException("Error toggling maintenance mode", e);
        }
    }

    public void assignFee(Fee fee) throws ServiceException {
        try {
            feeDao.createFee(fee);
        } catch (SQLException e) {
            throw new ServiceException("Error assigning fee", e);
        }
    }

    public int countActiveEnrollments() throws SQLException {
        EnrollmentDao enrollmentDao = new EnrollmentDao();
        int total = 0;
        List<Section> sections = getAllSections();
        for (Section section : sections) {
            total += enrollmentDao.countActiveEnrollmentsForSection(section.getSectionId());
        }
        return total;
    }

    private void validateUsername(String username) throws SQLException, ServiceException {
        if (username == null || username.trim().isEmpty()) {
            throw new ServiceException("Username is required.");
        }
        if (!username.matches("^[a-zA-Z0-9]{3,50}$")) {
            throw new ServiceException("Username must be alphanumeric and between 3 and 50 characters.");
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

    public User getUser(int userId) throws SQLException {
        return userDao.findById(userId);
    }

    public int getEnrollmentCount(int sectionId) throws SQLException {
        EnrollmentDao enrollmentDao = new EnrollmentDao();
        return enrollmentDao.countActiveEnrollmentsForSection(sectionId);
    }
}
