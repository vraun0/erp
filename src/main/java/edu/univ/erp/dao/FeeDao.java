package edu.univ.erp.dao;

import edu.univ.erp.config.DatabaseManager;
import edu.univ.erp.model.Fee;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeeDao {

    public void createFee(Fee fee) throws SQLException {
        String sql = "INSERT INTO fees (student_id, amount, description, due_date, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, fee.getStudentId());
            stmt.setBigDecimal(2, fee.getAmount());
            stmt.setString(3, fee.getDescription());
            stmt.setDate(4, Date.valueOf(fee.getDueDate()));
            stmt.setString(5, fee.getStatus());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    fee.setFeeId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<Fee> getFeesByStudent(String studentId) throws SQLException {
        List<Fee> fees = new ArrayList<>();
        String sql = "SELECT * FROM fees WHERE student_id = ? ORDER BY due_date DESC";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fees.add(mapResultSetToFee(rs));
                }
            }
        }
        return fees;
    }

    public List<Fee> getPendingFeesByStudent(String studentId) throws SQLException {
        List<Fee> fees = new ArrayList<>();
        String sql = "SELECT * FROM fees WHERE student_id = ? AND status = 'PENDING' ORDER BY due_date ASC";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fees.add(mapResultSetToFee(rs));
                }
            }
        }
        return fees;
    }

    public void updateFeeStatus(int feeId, String status) throws SQLException {
        String sql = "UPDATE fees SET status = ? WHERE fee_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, feeId);
            stmt.executeUpdate();
        }
    }

    private Fee mapResultSetToFee(ResultSet rs) throws SQLException {
        return new Fee(
                rs.getInt("fee_id"),
                rs.getString("student_id"),
                rs.getBigDecimal("amount"),
                rs.getString("description"),
                rs.getDate("due_date").toLocalDate(),
                rs.getString("status"));
    }
}
