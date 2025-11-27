package edu.univ.erp.dao;

import edu.univ.erp.config.DatabaseManager;
import edu.univ.erp.model.Attendance;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDao {

    public void saveAttendance(Attendance attendance) throws SQLException {
        // Upsert: Insert or Update if exists for same enrollment and date
        String sql = "INSERT INTO attendance (enrollment_id, date, status) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE status = VALUES(status)";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, attendance.getEnrollmentId());
            stmt.setDate(2, Date.valueOf(attendance.getDate()));
            stmt.setString(3, attendance.getStatus());
            stmt.executeUpdate();

            // Note: getGeneratedKeys might not return correct ID on update, but we mostly
            // care about saving here
            if (attendance.getAttendanceId() == 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        attendance.setAttendanceId(generatedKeys.getInt(1));
                    }
                }
            }
        }
    }

    public List<Attendance> getAttendanceByEnrollment(int enrollmentId) throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE enrollment_id = ? ORDER BY date DESC";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttendance(rs));
                }
            }
        }
        return list;
    }

    public Attendance getAttendanceByEnrollmentAndDate(int enrollmentId, LocalDate date) throws SQLException {
        String sql = "SELECT * FROM attendance WHERE enrollment_id = ? AND date = ?";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            stmt.setDate(2, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttendance(rs);
                }
            }
        }
        return null;
    }

    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        return new Attendance(
                rs.getInt("attendance_id"),
                rs.getInt("enrollment_id"),
                rs.getDate("date").toLocalDate(),
                rs.getString("status"));
    }
}
