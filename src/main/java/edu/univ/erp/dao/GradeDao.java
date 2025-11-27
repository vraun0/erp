package edu.univ.erp.dao;

import edu.univ.erp.config.DatabaseManager;
import edu.univ.erp.model.Grade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GradeDao {
    private static final String SELECT_BY_ENROLLMENT = "SELECT enrollment_id, component, score, final_score FROM grades WHERE enrollment_id = ? ORDER BY component";
    private static final String SELECT_BY_STUDENT = "SELECT g.enrollment_id, g.component, g.score, g.final_score FROM grades g INNER JOIN enrollments e ON g.enrollment_id = e.enrollment_id WHERE e.student_id = ? ORDER BY g.enrollment_id, g.component";
    private static final String UPSERT_GRADE = "INSERT INTO grades (enrollment_id, component, score, final_score) VALUES (?, ?, ?, ?) "
            +
            "ON DUPLICATE KEY UPDATE score = VALUES(score), final_score = VALUES(final_score)";
    private static final String UPSERT_GRADE_NO_FINAL = "INSERT INTO grades (enrollment_id, component, score, final_score) VALUES (?, ?, ?, ?) "
            +
            "ON DUPLICATE KEY UPDATE score = VALUES(score), final_score = COALESCE(VALUES(final_score), final_score)";
    private static final String UPDATE_FINAL_SCORE = "UPDATE grades SET final_score = ? WHERE enrollment_id = ?";

    private final DatabaseManager databaseManager;

    public GradeDao() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public List<Grade> findByEnrollment(int enrollmentId) throws SQLException {
        System.out.println("[DEBUG] findByEnrollment called for enrollment: " + enrollmentId);
        List<Grade> grades = queryGrades(SELECT_BY_ENROLLMENT, ps -> ps.setInt(1, enrollmentId));
        System.out.println("[DEBUG] findByEnrollment returning " + grades.size() + " grades");
        return grades;
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

    public void upsertGradeWithConnection(Connection conn, int enrollmentId, int component, Integer score,
            Integer finalScore) throws SQLException {
        // Always use UPSERT_GRADE which properly handles ON DUPLICATE KEY UPDATE
        // This ensures we update existing rows instead of creating duplicates
        try (PreparedStatement stmt = conn.prepareStatement(UPSERT_GRADE)) {
            stmt.setInt(1, enrollmentId);
            stmt.setInt(2, component);
            if (score == null) {
                stmt.setNull(3, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(3, score);
            }
            if (finalScore == null) {
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(4, finalScore);
            }
            int rowsAffected = stmt.executeUpdate();
            System.out.println("[DEBUG] UPSERT_GRADE executed: enrollment=" + enrollmentId +
                    ", component=" + component + ", score=" + score +
                    ", finalScore=" + finalScore + ", rowsAffected=" + rowsAffected);

            // Verify the row was inserted/updated
            if (rowsAffected == 0) {
                System.err.println("[WARNING] UPSERT_GRADE affected 0 rows for enrollment=" + enrollmentId +
                        ", component=" + component);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] UPSERT_GRADE failed for enrollment=" + enrollmentId +
                    ", component=" + component + ": " + e.getMessage());
            throw e;
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

    public int updateFinalScoreWithConnection(Connection conn, int enrollmentId, int finalScore) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_FINAL_SCORE)) {
            stmt.setInt(1, finalScore);
            stmt.setInt(2, enrollmentId);
            int rowsUpdated = stmt.executeUpdate();
            System.out.println("[DEBUG] UPDATE_FINAL_SCORE executed: " + rowsUpdated + " rows updated for enrollment "
                    + enrollmentId);
            return rowsUpdated;
        }
    }

    public Connection getConnection() throws SQLException {
        Connection conn = databaseManager.getErpConnection();
        // Ensure we have proper transaction isolation for consistency
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        return conn;
    }

    public List<Grade> findByEnrollmentWithConnection(Connection conn, int enrollmentId) throws SQLException {
        List<Grade> grades = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ENROLLMENT)) {
            stmt.setInt(1, enrollmentId);
            System.out.println(
                    "[DEBUG] Verifying grades with query: " + SELECT_BY_ENROLLMENT + " for enrollment " + enrollmentId);
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

    private List<Grade> queryGrades(String sql, StatementPreparer preparer) throws SQLException {
        List<Grade> grades = new ArrayList<>();
        try (Connection conn = databaseManager.getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            preparer.prepare(stmt);
            System.out.println("[DEBUG] Executing query: " + sql);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Grade grade = new Grade();
                    grade.setEnrollmentId(rs.getInt("enrollment_id"));
                    grade.setComponent(rs.getInt("component"));
                    grade.setScore(rs.getInt("score"));
                    int finalScore = rs.getInt("final_score");
                    grade.setFinalScore(rs.wasNull() ? null : finalScore);
                    System.out.println("[DEBUG] Loaded grade: enrollment=" + grade.getEnrollmentId() +
                            ", component=" + grade.getComponent() + ", score=" + grade.getScore() +
                            ", finalScore=" + grade.getFinalScore());
                    grades.add(grade);
                }
                System.out.println("[DEBUG] Total grades loaded: " + grades.size());
            }
        }
        return grades;
    }

    @FunctionalInterface
    private interface StatementPreparer {
        void prepare(PreparedStatement ps) throws SQLException;
    }
}
