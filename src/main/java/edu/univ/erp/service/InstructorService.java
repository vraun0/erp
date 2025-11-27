package edu.univ.erp.service;

import edu.univ.erp.dao.AttendanceDao;
import edu.univ.erp.dao.CourseDao;
import edu.univ.erp.dao.EnrollmentDao;
import edu.univ.erp.dao.GradeDao;
import edu.univ.erp.dao.SectionDao;
import edu.univ.erp.dao.SettingsDao;
import edu.univ.erp.dao.StudentDao;
import edu.univ.erp.model.Attendance;
import edu.univ.erp.model.Course;
import edu.univ.erp.model.Enrollment;
import edu.univ.erp.model.Grade;
import edu.univ.erp.model.Section;
import edu.univ.erp.model.Student;
import edu.univ.erp.model.view.GradebookRow;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class InstructorService {
    private static final Map<Integer, Double> COMPONENT_WEIGHTS = Map.of(
            1, 0.4, // Midterm 40%
            2, 0.5, // Final 50%
            3, 0.1 // Project 10%
    );

    private final SectionDao sectionDao;
    private final EnrollmentDao enrollmentDao;
    private final GradeDao gradeDao;
    private final CourseDao courseDao;
    private final StudentDao studentDao;
    private final edu.univ.erp.dao.InstructorDao instructorDao;
    private final edu.univ.erp.dao.UserDao userDao;
    private final SettingsDao settingsDao;

    private final AttendanceDao attendanceDao;
    private final AccessControlService accessControlService;

    public InstructorService() {
        this.sectionDao = new SectionDao();
        this.enrollmentDao = new EnrollmentDao();
        this.gradeDao = new GradeDao();
        this.courseDao = new CourseDao();
        this.studentDao = new StudentDao();

        this.instructorDao = new edu.univ.erp.dao.InstructorDao();
        this.userDao = new edu.univ.erp.dao.UserDao();
        this.settingsDao = new SettingsDao();

        this.attendanceDao = new AttendanceDao();
        this.accessControlService = new AccessControlService();
    }

    public List<Section> getSectionsForInstructor(int instructorId) throws SQLException {
        List<Section> assigned = new ArrayList<>();
        for (Section section : sectionDao.findAll()) {
            if (section.getInstructorId() != null && section.getInstructorId().equals(instructorId)) {
                assigned.add(section);
            }
        }
        return assigned;
    }

    public List<GradebookRow> getGradebookForSection(int instructorId, int sectionId)
            throws SQLException, ServiceException {
        if (!accessControlService.canEditSection(instructorId, sectionId)) {
            throw new ServiceException("Access Denied: You do not have permission to view this section.");
        }
        System.out.println("[DEBUG] getGradebookForSection called for section: " + sectionId);
        List<GradebookRow> rows = new ArrayList<>();
        List<Enrollment> enrollments = enrollmentDao.findBySection(sectionId);
        System.out.println("[DEBUG] Found " + enrollments.size() + " enrollments for section " + sectionId);

        for (Enrollment enrollment : enrollments) {
            System.out.println("[DEBUG] Processing enrollment: " + enrollment.getEnrollmentId() +
                    " for student: " + enrollment.getStudentId());

            Optional<Student> studentOpt = studentDao.findByUserId(enrollment.getStudentId());
            int rollNumber = studentOpt.map(Student::getRollNumber).orElse(0);

            Integer midterm = null;
            Integer fin = null;
            Integer project = null;
            Integer finalScore = null;

            // Always read fresh from database - no caching
            var grades = gradeDao.findByEnrollment(enrollment.getEnrollmentId());
            System.out.println("[DEBUG] Loaded " + grades.size() + " grade records for enrollment "
                    + enrollment.getEnrollmentId());

            // Process grades - use a Set to handle duplicates (shouldn't happen with
            // PRIMARY KEY, but just in case)
            java.util.Set<Integer> seenComponents = new java.util.HashSet<>();
            for (var grade : grades) {
                System.out.println("[DEBUG] Processing grade: component=" + grade.getComponent() +
                        ", score=" + grade.getScore() + ", finalScore=" + grade.getFinalScore());

                // Skip if we've already seen this component (handle duplicates)
                if (seenComponents.contains(grade.getComponent())) {
                    System.out.println("[WARNING] Duplicate component " + grade.getComponent() +
                            " found for enrollment " + enrollment.getEnrollmentId() + ", skipping");
                    continue;
                }
                seenComponents.add(grade.getComponent());

                switch (grade.getComponent()) {
                    case 1 -> midterm = grade.getScore();
                    case 2 -> fin = grade.getScore();
                    case 3 -> project = grade.getScore();
                }
                if (grade.getFinalScore() != null) {
                    finalScore = grade.getFinalScore();
                }
            }

            System.out.println("[DEBUG] Created GradebookRow: midterm=" + midterm +
                    ", final=" + fin + ", project=" + project + ", finalScore=" + finalScore);

            String studentName = "Unknown";
            edu.univ.erp.model.User user = userDao.findById(enrollment.getStudentId());
            if (user != null) {
                studentName = user.getUsername();
            }

            rows.add(new GradebookRow(
                    enrollment.getEnrollmentId(),
                    enrollment.getStudentId(),
                    studentName,
                    rollNumber,
                    midterm,
                    fin,
                    project,
                    finalScore));
        }
        System.out.println("[DEBUG] getGradebookForSection returning " + rows.size() + " rows");
        return rows;
    }

    public void updateGrades(int instructorId, int sectionId, List<GradebookRow> rows)
            throws SQLException, ServiceException {
        if (!accessControlService.canEditSection(instructorId, sectionId)) {
            throw new ServiceException("Access Denied: You do not have permission to edit this section.");
        }
        ensureSystemWritable();
        java.sql.Connection conn = null;
        boolean autoCommitRestored = false;
        try {
            // Get a single connection for the transaction
            conn = gradeDao.getConnection();
            boolean wasAutoCommit = conn.getAutoCommit();
            autoCommitRestored = wasAutoCommit;
            conn.setAutoCommit(false); // Start transaction

            System.out.println("[DEBUG] Starting grade update transaction for section: " + sectionId);

            for (GradebookRow row : rows) {
                System.out.println("[DEBUG] Processing enrollment: " + row.getEnrollmentId() +
                        ", Student: " + row.getStudentId());

                // Calculate final score first
                int finalScore = calculateFinalScore(row);
                System.out.println(
                        "[DEBUG] Calculated final score: " + finalScore + " for enrollment: " + row.getEnrollmentId());

                // Upsert individual component grades WITH final_score set directly
                // We pass the Integer score directly, even if null, to allow clearing grades
                System.out.println("[DEBUG] Upserting midterm score: " + row.getMidtermScore()
                        + " with final_score: " + finalScore);
                gradeDao.upsertGradeWithConnection(conn, row.getEnrollmentId(), 1, row.getMidtermScore(),
                        finalScore);

                System.out.println("[DEBUG] Upserting final exam score: " + row.getFinalExamScore()
                        + " with final_score: " + finalScore);
                gradeDao.upsertGradeWithConnection(conn, row.getEnrollmentId(), 2, row.getFinalExamScore(),
                        finalScore);

                System.out.println("[DEBUG] Upserting project score: " + row.getProjectScore()
                        + " with final_score: " + finalScore);
                gradeDao.upsertGradeWithConnection(conn, row.getEnrollmentId(), 3, row.getProjectScore(),
                        finalScore);

                // Also update final_score on any existing grade records that might not have
                // been updated above
                // This handles the case where we're only updating one component but others
                // already exist
                int rowsUpdated = gradeDao.updateFinalScoreWithConnection(conn, row.getEnrollmentId(), finalScore);
                System.out.println("[DEBUG] Updated final_score on " + rowsUpdated
                        + " additional grade records for enrollment: " + row.getEnrollmentId());
            }

            System.out.println("[DEBUG] Committing transaction...");
            conn.commit(); // Commit the transaction
            System.out.println("[DEBUG] Transaction committed successfully");

            // Verify the commit by reading back using the SAME connection (should see
            // committed data)
            if (!rows.isEmpty()) {
                GradebookRow firstRow = rows.get(0);
                var verifyGrades = gradeDao.findByEnrollmentWithConnection(conn, firstRow.getEnrollmentId());
                System.out.println("[DEBUG] Verification (same connection): Found " + verifyGrades.size()
                        + " grade records after commit for enrollment " + firstRow.getEnrollmentId());
                for (var g : verifyGrades) {
                    System.out.println("[DEBUG] Verification: Grade - component=" + g.getComponent() +
                            ", score=" + g.getScore() + ", finalScore=" + g.getFinalScore());
                }
            }

            // CRITICAL: Do NOT restore auto-commit before closing - let the connection
            // close handle it
            // Restoring auto-commit here might interfere with the commit

        } catch (SQLException e) {
            System.err.println("[ERROR] SQLException during grade update: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    System.err.println("[ERROR] Rolling back transaction...");
                    conn.rollback(); // Rollback on error
                    System.err.println("[ERROR] Transaction rolled back");
                } catch (SQLException rollbackEx) {
                    System.err.println("[ERROR] Failed to rollback: " + rollbackEx.getMessage());
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    // Restore auto-commit mode before closing
                    if (autoCommitRestored) {
                        conn.setAutoCommit(true);
                        System.out.println("[DEBUG] Auto-commit restored to true before closing");
                    }

                    // Close connection - this returns it to the pool
                    conn.close();
                    System.out.println("[DEBUG] Connection closed and returned to pool");

                } catch (SQLException e) {
                    System.err.println("[ERROR] Failed to close connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        // Verify persistence with a FRESH connection AFTER the transaction connection
        // is closed
        // This ensures we're reading from the database, not from a transaction cache
        if (!rows.isEmpty()) {
            System.out.println("[DEBUG] Starting persistence verification with fresh connection...");
            try (Connection verifyConn = gradeDao.getConnection()) {
                // Use a fresh connection to verify the data is actually persisted
                verifyConn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                GradebookRow firstRow = rows.get(0);
                var verifyGrades = gradeDao.findByEnrollmentWithConnection(verifyConn, firstRow.getEnrollmentId());
                System.out.println("[DEBUG] Verification (fresh connection): Found " + verifyGrades.size() +
                        " grade records in database for enrollment " + firstRow.getEnrollmentId());
                if (verifyGrades.isEmpty()) {
                    System.err.println(
                            "[ERROR] CRITICAL: Grades not found in database after commit! Data may not be persisted.");
                } else {
                    for (var g : verifyGrades) {
                        System.out.println(
                                "[DEBUG] Fresh connection verification: Grade - component=" + g.getComponent() +
                                        ", score=" + g.getScore() + ", finalScore=" + g.getFinalScore());
                    }
                    System.out.println("[DEBUG] SUCCESS: Grades verified to be persisted in database!");
                }
            } catch (SQLException verifyEx) {
                System.err.println("[ERROR] Failed to verify persistence: " + verifyEx.getMessage());
                verifyEx.printStackTrace();
            }
        }
    }

    private int calculateFinalScore(GradebookRow row) {
        double score = 0.0;
        double totalWeight = 0.0;
        if (row.getMidtermScore() != null) {
            score += row.getMidtermScore() * COMPONENT_WEIGHTS.getOrDefault(1, 0.0);
            totalWeight += COMPONENT_WEIGHTS.getOrDefault(1, 0.0);
        }
        if (row.getFinalExamScore() != null) {
            score += row.getFinalExamScore() * COMPONENT_WEIGHTS.getOrDefault(2, 0.0);
            totalWeight += COMPONENT_WEIGHTS.getOrDefault(2, 0.0);
        }
        if (row.getProjectScore() != null) {
            score += row.getProjectScore() * COMPONENT_WEIGHTS.getOrDefault(3, 0.0);
            totalWeight += COMPONENT_WEIGHTS.getOrDefault(3, 0.0);
        }
        if (totalWeight == 0) {
            return 0;
        }
        return (int) Math.round(score / totalWeight);
    }

    public Course getCourseForSection(int sectionId) throws SQLException {
        Section section = sectionDao.findById(sectionId).orElse(null);
        if (section == null) {
            return null;
        }
        return courseDao.findByCode(section.getCourseId()).orElse(null);
    }

    private void ensureSystemWritable() throws SQLException, ServiceException {
        if (settingsDao.isMaintenanceMode()) {
            throw new ServiceException("System is currently in maintenance mode. Please try again later.");
        }
    }

    // Alias for getGradebookForSection to match UI usage
    public List<GradebookRow> getGradebook(int instructorId, int sectionId) throws SQLException, ServiceException {
        return getGradebookForSection(instructorId, sectionId);
    }

    public void markAttendance(Attendance attendance) throws ServiceException {
        try {
            attendanceDao.saveAttendance(attendance);
        } catch (SQLException e) {
            throw new ServiceException("Error marking attendance", e);
        }
    }

    public Attendance getAttendance(int enrollmentId, java.time.LocalDate date) throws ServiceException {
        try {
            return attendanceDao.getAttendanceByEnrollmentAndDate(enrollmentId, date);
        } catch (SQLException e) {
            throw new ServiceException("Error fetching attendance", e);
        }
    }

    public String getInstructorUsername(int instructorId) throws SQLException, ServiceException {
        java.util.Optional<edu.univ.erp.model.Instructor> instructorOpt = instructorDao.findByUserId(instructorId);
        if (instructorOpt.isEmpty()) {
            throw new ServiceException("Instructor not found.");
        }
        edu.univ.erp.model.User user = new edu.univ.erp.dao.UserDao().findById(instructorOpt.get().getUserId());
        if (user == null) {
            throw new ServiceException("User account not found for instructor.");
        }
        return user.getUsername();
    }

    public edu.univ.erp.model.ClassStatistics getClassStatistics(int instructorId, int sectionId)
            throws SQLException, ServiceException {
        if (!accessControlService.canEditSection(instructorId, sectionId)) {
            throw new ServiceException(
                    "Access Denied: You do not have permission to view statistics for this section.");
        }

        List<Enrollment> enrollments = enrollmentDao.findBySection(sectionId);
        if (enrollments.isEmpty()) {
            return new edu.univ.erp.model.ClassStatistics(0, 0, 0, 0, 0, java.util.Collections.emptyMap());
        }

        List<Integer> finalScores = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            List<Grade> grades = gradeDao.findByEnrollment(enrollment.getEnrollmentId());
            // Find final score if exists, or calculate it
            Integer finalScore = null;
            for (Grade g : grades) {
                if (g.getFinalScore() != null) {
                    finalScore = g.getFinalScore();
                    break;
                }
            }
            if (finalScore != null) {
                finalScores.add(finalScore);
            }
        }

        if (finalScores.isEmpty()) {
            return new edu.univ.erp.model.ClassStatistics(0, 0, 0, 0, enrollments.size(),
                    java.util.Collections.emptyMap());
        }

        double sum = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        Map<String, Integer> distribution = new java.util.HashMap<>();
        distribution.put("A", 0);
        distribution.put("B", 0);
        distribution.put("C", 0);
        distribution.put("D", 0);
        distribution.put("F", 0);

        for (int score : finalScores) {
            sum += score;
            if (score < min)
                min = score;
            if (score > max)
                max = score;

            if (score >= 90)
                distribution.put("A", distribution.get("A") + 1);
            else if (score >= 80)
                distribution.put("B", distribution.get("B") + 1);
            else if (score >= 70)
                distribution.put("C", distribution.get("C") + 1);
            else if (score >= 60)
                distribution.put("D", distribution.get("D") + 1);
            else
                distribution.put("F", distribution.get("F") + 1);
        }

        double average = sum / finalScores.size();

        double varianceSum = 0;
        for (int score : finalScores) {
            varianceSum += Math.pow(score - average, 2);
        }
        double stdDev = Math.sqrt(varianceSum / finalScores.size());

        return new edu.univ.erp.model.ClassStatistics(average, min, max, stdDev, enrollments.size(), distribution);
    }

    public edu.univ.erp.api.types.InstructorDashboardStats getDashboardStats(int instructorId) throws SQLException {
        List<Section> sections = getSectionsForInstructor(instructorId);
        int totalSections = sections.size();

        java.util.Set<String> uniqueCourses = new java.util.HashSet<>();
        for (Section section : sections) {
            uniqueCourses.add(section.getCourseId());
        }
        int totalCourses = uniqueCourses.size();

        int totalStudents = 0;
        int totalEnrollments = 0;
        int sectionsWithPendingGrades = 0;

        for (Section section : sections) {
            List<Enrollment> enrollments = enrollmentDao.findBySection(section.getSectionId());

            int activeCount = 0;
            int studentsWithGrades = 0;

            for (Enrollment enrollment : enrollments) {
                if ("ENROLLED".equalsIgnoreCase(enrollment.getStatus())) {
                    activeCount++;
                    totalEnrollments++;

                    List<Grade> grades = gradeDao.findByEnrollment(enrollment.getEnrollmentId());
                    if (!grades.isEmpty()) {
                        studentsWithGrades++;
                    }
                }
            }

            totalStudents += activeCount;

            if (activeCount > 0 && studentsWithGrades < activeCount) {
                sectionsWithPendingGrades++;
            }
        }

        double avgClassSize = totalSections > 0 ? (double) totalStudents / totalSections : 0;

        return new edu.univ.erp.api.types.InstructorDashboardStats(
                totalSections, totalStudents, totalCourses, avgClassSize, totalEnrollments, sectionsWithPendingGrades);
    }

    public Course getCourse(String courseCode) throws SQLException {
        return courseDao.findByCode(courseCode).orElse(null);
    }
}
