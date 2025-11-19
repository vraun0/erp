package com.sis.app.service;

import com.sis.app.dao.CourseDao;
import com.sis.app.dao.EnrollmentDao;
import com.sis.app.dao.GradeDao;
import com.sis.app.dao.SectionDao;
import com.sis.app.dao.SettingsDao;
import com.sis.app.dao.StudentDao;
import com.sis.app.model.Course;
import com.sis.app.model.Enrollment;
import com.sis.app.model.Section;
import com.sis.app.model.Student;
import com.sis.app.model.view.GradebookRow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InstructorService {
    private static final Map<Integer, Double> COMPONENT_WEIGHTS = Map.of(
            1, 0.4,  // Midterm 40%
            2, 0.5,  // Final 50%
            3, 0.1   // Project 10%
    );

    private final SectionDao sectionDao;
    private final EnrollmentDao enrollmentDao;
    private final GradeDao gradeDao;
    private final CourseDao courseDao;
    private final StudentDao studentDao;
    private final SettingsDao settingsDao;

    public InstructorService() {
        this.sectionDao = new SectionDao();
        this.enrollmentDao = new EnrollmentDao();
        this.gradeDao = new GradeDao();
        this.courseDao = new CourseDao();
        this.studentDao = new StudentDao();
        this.settingsDao = new SettingsDao();
    }

    public List<Section> getSectionsForInstructor(String instructorId) throws SQLException {
        List<Section> assigned = new ArrayList<>();
        for (Section section : sectionDao.findAll()) {
            if (instructorId.equalsIgnoreCase(section.getInstructorId())) {
                assigned.add(section);
            }
        }
        return assigned;
    }

    public List<GradebookRow> getGradebookForSection(int sectionId) throws SQLException {
        List<GradebookRow> rows = new ArrayList<>();
        List<Enrollment> enrollments = enrollmentDao.findBySection(sectionId);
        for (Enrollment enrollment : enrollments) {
            Optional<Student> studentOpt = studentDao.findByUserId(enrollment.getStudentId());
            int rollNumber = studentOpt.map(Student::getRollNumber).orElse(0);

            Integer midterm = null;
            Integer fin = null;
            Integer project = null;
            Integer finalScore = null;
            var grades = gradeDao.findByEnrollment(enrollment.getEnrollmentId());
            for (var grade : grades) {
                switch (grade.getComponent()) {
                    case 1 -> midterm = grade.getScore();
                    case 2 -> fin = grade.getScore();
                    case 3 -> project = grade.getScore();
                }
                if (grade.getFinalScore() != null) {
                    finalScore = grade.getFinalScore();
                }
            }

            rows.add(new GradebookRow(
                    enrollment.getEnrollmentId(),
                    enrollment.getStudentId(),
                    rollNumber,
                    midterm,
                    fin,
                    project,
                    finalScore
            ));
        }
        return rows;
    }

    public void updateGrades(int sectionId, List<GradebookRow> rows) throws SQLException, ServiceException {
        ensureSystemWritable();
        for (GradebookRow row : rows) {
            if (row.getMidtermScore() != null) {
                gradeDao.upsertGrade(row.getEnrollmentId(), 1, row.getMidtermScore(), row.getFinalScore());
            }
            if (row.getFinalExamScore() != null) {
                gradeDao.upsertGrade(row.getEnrollmentId(), 2, row.getFinalExamScore(), row.getFinalScore());
            }
            if (row.getProjectScore() != null) {
                gradeDao.upsertGrade(row.getEnrollmentId(), 3, row.getProjectScore(), row.getFinalScore());
            }
            int finalScore = calculateFinalScore(row);
            gradeDao.updateFinalScore(row.getEnrollmentId(), finalScore);
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
}

