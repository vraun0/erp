package edu.univ.erp.dao;

import edu.univ.erp.config.DatabaseManager;
import edu.univ.erp.model.Enrollment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnrollmentDao {
    private static final String SELECT_FOR_STUDENT = "SELECT enrollment_id, student_id, section_id, status FROM enrollments WHERE student_id = ?";
    private static final String SELECT_ACTIVE_FOR_STUDENT_SECTION = "SELECT enrollment_id, student_id, section_id, status FROM enrollments WHERE student_id = ? AND section_id = ? AND status = 'ENROLLED'";
    private static final String COUNT_ENROLLMENTS_FOR_SECTION = "SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND status = 'ENROLLED'";
    private static final String INSERT_ENROLLMENT = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'ENROLLED')";
    private static final String UPDATE_STATUS = "UPDATE enrollments SET status = ? WHERE enrollment_id = ?";
    private static final String SELECT_FOR_SECTION = "SELECT enrollment_id, student_id, section_id, status FROM enrollments WHERE section_id = ?";
    private static final String SELECT_BY_ID = "SELECT enrollment_id, student_id, section_id, status FROM enrollments WHERE enrollment_id = ?";

    private final DatabaseManager databaseManager;

    public EnrollmentDao() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public List<Enrollment> findByStudent(String studentId) throws SQLException {
        List<Enrollment> enrollments = new ArrayList<>();
        try (Connection conn = databaseManager.getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_FOR_STUDENT)) {

            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapEnrollment(rs));
                }
            }
        }
        return enrollments;
    }

    public Optional<Enrollment> findActiveEnrollment(String studentId, int sectionId) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_ACTIVE_FOR_STUDENT_SECTION)) {

            stmt.setString(1, studentId);
            stmt.setInt(2, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapEnrollment(rs));
                }
            }
        }
        return Optional.empty();
    }

    public int countActiveEnrollmentsForSection(int sectionId) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(COUNT_ENROLLMENTS_FOR_SECTION)) {

            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public Enrollment insertEnrollment(int studentId, int sectionId) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(INSERT_ENROLLMENT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    Enrollment enrollment = new Enrollment();
                    enrollment.setEnrollmentId(id);
                    enrollment.setStudentId(studentId);
                    enrollment.setSectionId(sectionId);
                    enrollment.setStatus("ENROLLED");
                    return enrollment;
                }
            }
        }
        throw new SQLException("Failed to insert enrollment");
    }

    public void updateStatus(int enrollmentId, String status) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_STATUS)) {

            stmt.setString(1, status);
            stmt.setInt(2, enrollmentId);
            stmt.executeUpdate();
        }
    }

    public List<Enrollment> findBySection(int sectionId) throws SQLException {
        List<Enrollment> enrollments = new ArrayList<>();
        try (Connection conn = databaseManager.getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_FOR_SECTION)) {

            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapEnrollment(rs));
                }
            }
        }
        return enrollments;
    }

    public Optional<Enrollment> findById(int enrollmentId) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, enrollmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapEnrollment(rs));
                }
            }
        }
        return Optional.empty();
    }

    private Enrollment mapEnrollment(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
        enrollment.setStudentId(rs.getInt("student_id"));
        enrollment.setSectionId(rs.getInt("section_id"));
        enrollment.setStatus(rs.getString("status"));
        return enrollment;
    }
}
