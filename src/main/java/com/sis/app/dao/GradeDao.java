package com.sis.app.dao;

import com.sis.app.config.DatabaseManager;
import com.sis.app.model.Grade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GradeDao {
    private static final String SELECT_BY_ENROLLMENT = "SELECT enrollment_id, component, score, final_score FROM grades WHERE enrollment_id = ? ORDER BY component";
    private static final String SELECT_BY_STUDENT = "SELECT g.enrollment_id, g.component, g.score, g.final_score FROM grades g INNER JOIN enrollments e ON g.enrollment_id = e.enrollment_id WHERE e.student_id = ? ORDER BY g.enrollment_id, g.component";
    private static final String UPSERT_GRADE = "INSERT INTO grades (enrollment_id, component, score, final_score) VALUES (?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE score = VALUES(score), final_score = VALUES(final_score)";
    private static final String UPDATE_FINAL_SCORE = "UPDATE grades SET final_score = ? WHERE enrollment_id = ?";

    private final DatabaseManager databaseManager;

    public GradeDao() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public List<Grade> findByEnrollment(int enrollmentId) throws SQLException {
        return queryGrades(SELECT_BY_ENROLLMENT, ps -> ps.setInt(1, enrollmentId));
    }

    public List<Grade> findByStudent(String studentId) throws SQLException {
        return queryGrades(SELECT_BY_STUDENT, ps -> ps.setString(1, studentId));
    }

    public void upsertGrade(int enrollmentId, int component, int score, Integer finalScore) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(UPSERT_GRADE)) {

            stmt.setInt(1, enrollmentId);
            stmt.setInt(2, component);
            stmt.setInt(3, score);
            if (finalScore == null) {
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(4, finalScore);
            }
            stmt.executeUpdate();
        }
    }

    public void updateFinalScore(int enrollmentId, int finalScore) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_FINAL_SCORE)) {

            stmt.setInt(1, finalScore);
            stmt.setInt(2, enrollmentId);
            stmt.executeUpdate();
        }
    }

    private List<Grade> queryGrades(String sql, StatementPreparer preparer) throws SQLException {
        List<Grade> grades = new ArrayList<>();
        try (Connection conn = databaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            preparer.prepare(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Grade grade = new Grade();
                    grade.setEnrollmentId(rs.getInt("enrollment_id"));
                    grade.setComponent(rs.getInt("component"));
                    grade.setScore(rs.getInt("score"));
                    int finalScore = rs.getInt("final_score");
                    grade.setFinalScore(rs.wasNull() ? null : finalScore);
                    grades.add(grade);
                }
            }
        }
        return grades;
    }

    @FunctionalInterface
    private interface StatementPreparer {
        void prepare(PreparedStatement ps) throws SQLException;
    }
}
