package edu.univ.erp.service;

import edu.univ.erp.dao.*;
import edu.univ.erp.model.Attendance;
import edu.univ.erp.model.Course;
import edu.univ.erp.model.Enrollment;
import edu.univ.erp.model.Fee;
import edu.univ.erp.model.Grade;
import edu.univ.erp.model.Section;
import edu.univ.erp.model.view.CourseSectionView;
import edu.univ.erp.model.view.EnrollmentView;
import edu.univ.erp.model.view.GradeView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentService {
    private final CourseDao courseDao;
    private final SectionDao sectionDao;
    private final EnrollmentDao enrollmentDao = new EnrollmentDao();
    private final GradeDao gradeDao = new GradeDao();
    private final FeeDao feeDao = new FeeDao();
    private final AttendanceDao attendanceDao = new AttendanceDao();
    private final UserDao userDao = new UserDao();

    private final SettingsDao settingsDao;
    private final AccessControlService accessControlService;

    public StudentService() {
        this.courseDao = new CourseDao();
        this.sectionDao = new SectionDao();
        // this.enrollmentDao = new EnrollmentDao(); // Initialized at declaration
        // this.gradeDao = new GradeDao(); // Initialized at declaration
        this.settingsDao = new SettingsDao();

        // this.feeDao = new FeeDao(); // Initialized at declaration

        this.accessControlService = new AccessControlService();
    }

    public List<CourseSectionView> getCourseCatalog() throws SQLException {
        List<CourseSectionView> catalog = new ArrayList<>();
        List<Section> sections = sectionDao.findAll();

        for (Section section : sections) {
            Optional<Course> courseOpt = courseDao.findByCode(section.getCourseId());
            if (courseOpt.isEmpty()) {
                continue;
            }
            Course course = courseOpt.get();

            int seatsTaken = enrollmentDao.countActiveEnrollmentsForSection(section.getSectionId());

            String instructorName = "TBD";
            if (section.getInstructorId() != null) {
                edu.univ.erp.model.User instructor = userDao.findById(section.getInstructorId());
                if (instructor != null) {
                    instructorName = instructor.getUsername();
                }
            }

            catalog.add(new CourseSectionView(
                    course.getCode(),
                    course.getTitle(),
                    course.getCredits(),
                    section.getSectionId(),
                    section.getDayTime(),
                    section.getRoom(),
                    section.getInstructorId(),
                    instructorName,
                    section.getCapacity(),
                    seatsTaken,
                    section.getSemester(),
                    section.getYear()));
        }

        return catalog;
    }

    public void registerForSection(int studentId, int sectionId) throws SQLException, ServiceException {
        if (!accessControlService.canAccessStudentData(studentId, studentId)) {
            throw new ServiceException("Access Denied.");
        }
        ensureSystemWritable();

        Optional<Section> sectionOpt = sectionDao.findById(sectionId);
        if (sectionOpt.isEmpty()) {
            throw new ServiceException("Section not found.");
        }
        Section section = sectionOpt.get();

        Optional<Enrollment> existingOpt = enrollmentDao.findActiveEnrollment(String.valueOf(studentId), sectionId);
        if (existingOpt.isPresent()) {
            throw new ServiceException("You are already enrolled in this section.");
        }

        int seatsTaken = enrollmentDao.countActiveEnrollmentsForSection(sectionId);
        if (seatsTaken >= section.getCapacity()) {
            throw new ServiceException("Section is full. No seats available.");
        }

        // Check prerequisites
        checkPrerequisites(studentId, section.getCourseId());

        enrollmentDao.insertEnrollment(studentId, sectionId);
    }

    private void checkPrerequisites(int studentId, String courseCode) throws SQLException, ServiceException {
        if (courseCode == null || courseCode.length() < 3) {
            return;
        }

        List<String> prereqs = new ArrayList<>();

        // Rule 1: Sequence rule (e.g. CS102 requires CS101)
        if (courseCode.endsWith("2")) {
            prereqs.add(courseCode.substring(0, courseCode.length() - 1) + "1");
        }

        // Rule 2: Level progression (e.g. CS201 requires CS101)
        // Find the first digit
        int firstDigitIdx = -1;
        for (int i = 0; i < courseCode.length(); i++) {
            if (Character.isDigit(courseCode.charAt(i))) {
                firstDigitIdx = i;
                break;
            }
        }

        if (firstDigitIdx != -1) {
            char levelChar = courseCode.charAt(firstDigitIdx);
            if (Character.isDigit(levelChar) && levelChar > '1') {
                char prevLevelChar = (char) (levelChar - 1);
                String prereq = courseCode.substring(0, firstDigitIdx) + prevLevelChar
                        + courseCode.substring(firstDigitIdx + 1);
                prereqs.add(prereq);
            }
        }

        for (String prereqCode : prereqs) {
            // Only enforce if the prerequisite course actually exists
            if (courseDao.findByCode(prereqCode).isEmpty()) {
                continue;
            }

            // Check if student has passed the prerequisite
            boolean hasPassed = false;
            List<GradeView> grades = getGrades(studentId);
            for (GradeView grade : grades) {
                if (grade.getCourseCode().equals(prereqCode) && grade.getFinalScore() != null
                        && grade.getFinalScore() >= 50) {
                    hasPassed = true;
                    break;
                }
            }

            if (!hasPassed) {
                throw new ServiceException(
                        "Prerequisite not met: You must pass " + prereqCode + " before registering for " + courseCode);
            }
        }
    }

    public List<GradeView> getGrades(int studentId) throws SQLException, ServiceException {
        if (!accessControlService.canAccessStudentData(studentId, studentId)) {
            throw new ServiceException("Access Denied.");
        }

        List<GradeView> gradeViews = new ArrayList<>();
        List<Grade> grades = gradeDao.findByStudent(String.valueOf(studentId));

        for (Grade grade : grades) {
            Optional<Enrollment> enrollmentOpt = enrollmentDao.findById(grade.getEnrollmentId());
            if (enrollmentOpt.isEmpty()) {
                continue;
            }

            Enrollment enrollment = enrollmentOpt.get();

            Optional<Section> sectionOpt = sectionDao.findById(enrollment.getSectionId());
            if (sectionOpt.isEmpty()) {
                continue;
            }

            Optional<Course> courseOpt = courseDao.findByCode(sectionOpt.get().getCourseId());
            if (courseOpt.isEmpty()) {
                continue;
            }

            Course course = courseOpt.get();

            Integer finalScore = grade.getFinalScore();

            gradeViews.add(new GradeView(course.getCode(), course.getTitle(), enrollment.getSectionId(),
                    grade.getComponent(), grade.getScore(), finalScore));
        }

        return gradeViews;
    }

    public double calculateGPA(int studentId) throws SQLException, ServiceException {
        if (!accessControlService.canAccessStudentData(studentId, studentId)) {
            throw new ServiceException("Access Denied.");
        }

        List<GradeView> grades = getGrades(studentId);
        double totalPoints = 0.0;
        int totalCredits = 0;

        for (GradeView grade : grades) {
            if (grade.getFinalScore() == null) {
                continue; // Skip courses without final grades
            }

            Optional<Course> courseOpt = courseDao.findByCode(grade.getCourseCode());
            if (courseOpt.isEmpty()) {
                continue;
            }
            int credits = courseOpt.get().getCredits();
            double points = convertScoreToPoints(grade.getFinalScore());

            totalPoints += points * credits;
            totalCredits += credits;
        }

        if (totalCredits == 0) {
            return 0.0;
        }

        return Math.round((totalPoints / totalCredits) * 100.0) / 100.0;
    }

    private double convertScoreToPoints(int score) {
        if (score >= 90)
            return 4.0;
        if (score >= 80)
            return 3.0;
        if (score >= 70)
            return 2.0;
        if (score >= 60)
            return 1.0;
        return 0.0;
    }

    public edu.univ.erp.model.Student getStudent(int studentId) throws SQLException, ServiceException {
        if (!accessControlService.canAccessStudentData(studentId, studentId)) {
            throw new ServiceException("Access Denied.");
        }
        Optional<edu.univ.erp.model.Student> studentOpt = new edu.univ.erp.dao.StudentDao().findByUserId(studentId);
        if (studentOpt.isEmpty()) {
            throw new ServiceException("Student not found.");
        }
        return studentOpt.get();
    }

    public String getStudentUsername(int studentId) throws SQLException, ServiceException {
        edu.univ.erp.model.Student student = getStudent(studentId);
        edu.univ.erp.model.User user = new edu.univ.erp.dao.UserDao().findById(student.getUserId());
        if (user == null) {
            throw new ServiceException("User account not found for student.");
        }
        return user.getUsername();
    }

    public List<Fee> getFees(int studentId) throws ServiceException {
        if (!accessControlService.canAccessStudentData(studentId, studentId)) {
            throw new ServiceException("Access Denied.");
        }
        try {
            return feeDao.getFeesByStudent(String.valueOf(studentId));
        } catch (SQLException e) {
            throw new ServiceException("Error fetching fees", e);
        }
    }

    public void payFee(int feeId) throws ServiceException {
        try {
            feeDao.updateFeeStatus(feeId, "PAID");
        } catch (SQLException e) {
            throw new ServiceException("Error paying fee", e);
        }
    }

    public List<Attendance> getAttendance(int studentId) throws ServiceException {
        if (!accessControlService.canAccessStudentData(studentId, studentId)) {
            throw new ServiceException("Access Denied.");
        }
        try {
            List<Attendance> allAttendance = new ArrayList<>();
            List<Enrollment> enrollments = enrollmentDao.findByStudent(String.valueOf(studentId));
            for (Enrollment e : enrollments) {
                allAttendance.addAll(attendanceDao.getAttendanceByEnrollment(e.getEnrollmentId()));
            }
            return allAttendance;
        } catch (SQLException e) {
            throw new ServiceException("Error fetching attendance", e);
        }
    }

    public List<EnrollmentView> getCurrentRegistrations(int studentId) throws SQLException, ServiceException {
        if (!accessControlService.canAccessStudentData(studentId, studentId)) {
            throw new ServiceException("Access Denied.");
        }
        List<EnrollmentView> views = new ArrayList<>();
        List<Enrollment> enrollments = enrollmentDao.findByStudent(String.valueOf(studentId));
        System.out.println("DEBUG: StudentService.getCurrentRegistrations - Found " + enrollments.size()
                + " enrollments for student " + studentId);

        for (Enrollment enrollment : enrollments) {
            Optional<Section> sectionOpt = sectionDao.findById(enrollment.getSectionId());
            if (sectionOpt.isEmpty()) {
                continue;
            }

            Optional<Course> courseOpt = courseDao.findByCode(sectionOpt.get().getCourseId());
            if (courseOpt.isEmpty()) {
                continue;
            }

            Course course = courseOpt.get();
            Section section = sectionOpt.get();

            String instructorName = "TBD";
            if (section.getInstructorId() != null) {
                edu.univ.erp.model.User instructor = userDao.findById(section.getInstructorId());
                if (instructor != null) {
                    instructorName = instructor.getUsername();
                }
            }

            views.add(new EnrollmentView(
                    enrollment.getEnrollmentId(),
                    enrollment.getSectionId(),
                    course.getCode(),
                    course.getTitle(),
                    section.getDayTime(),
                    section.getRoom(),
                    enrollment.getStatus(),
                    instructorName,
                    section.getDropDeadline()));
        }

        return views;
    }

    public void dropSection(int studentId, int sectionId) throws SQLException, ServiceException {
        if (!accessControlService.canAccessStudentData(studentId, studentId)) {
            throw new ServiceException("Access Denied.");
        }
        ensureSystemWritable();

        Optional<Enrollment> enrollmentOpt = enrollmentDao.findActiveEnrollment(String.valueOf(studentId), sectionId);
        if (enrollmentOpt.isEmpty()) {
            throw new ServiceException("You are not enrolled in this section.");
        }

        Enrollment enrollment = enrollmentOpt.get();
        if (!accessControlService.canModifyEnrollment(studentId, enrollment.getEnrollmentId())) {
            throw new ServiceException("Access Denied: You cannot drop this enrollment.");
        }

        Optional<Section> sectionOpt = sectionDao.findById(sectionId);
        if (sectionOpt.isPresent()) {
            Section section = sectionOpt.get();
            if (section.getDropDeadline() != null && java.time.LocalDate.now().isAfter(section.getDropDeadline())) {
                throw new ServiceException("Drop deadline has passed for this section.");
            }
        }

        enrollmentDao.updateStatus(enrollment.getEnrollmentId(), "DROPPED");
    }

    private void ensureSystemWritable() throws SQLException, ServiceException {
        if (settingsDao.isMaintenanceMode()) {
            throw new ServiceException("System is currently in maintenance mode. Please try again later.");
        }
    }
}
