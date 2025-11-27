package edu.univ.erp.dao;

import edu.univ.erp.config.DatabaseManager;
import edu.univ.erp.model.Instructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InstructorDao {
    private static final String INSERT_INSTRUCTOR = "INSERT INTO instructors (user_id, department) VALUES (?, ?)";
    private static final String SELECT_BY_USER = "SELECT user_id, department FROM instructors WHERE user_id = ?";
    private static final String SELECT_ALL = "SELECT user_id, department FROM instructors ORDER BY user_id";

    private final DatabaseManager databaseManager;

    public InstructorDao() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public void insertInstructor(Instructor instructor) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(INSERT_INSTRUCTOR)) {
            stmt.setInt(1, instructor.getUserId());
            stmt.setString(2, instructor.getDepartment());
            stmt.executeUpdate();
        }
    }

    public Optional<Instructor> findByUserId(int userId) throws SQLException {
        try (Connection conn = databaseManager.getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USER)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Instructor instructor = new Instructor();
                    instructor.setUserId(rs.getInt("user_id"));
                    instructor.setDepartment(rs.getString("department"));
                    return Optional.of(instructor);
                }
            }
        }
        return Optional.empty();
    }

    public List<Instructor> findAll() throws SQLException {
        List<Instructor> instructors = new ArrayList<>();
        try (Connection conn = databaseManager.getErpConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Instructor instructor = new Instructor();
                instructor.setUserId(rs.getInt("user_id"));
                instructor.setDepartment(rs.getString("department"));
                instructors.add(instructor);
            }
        }
        return instructors;
    }
}
