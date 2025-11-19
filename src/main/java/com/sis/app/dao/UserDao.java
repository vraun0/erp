package com.sis.app.dao;

import com.sis.app.config.DatabaseManager;
import com.sis.app.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * DAO for interacting with the users_auth table in AuthDB.
 */
public class UserDao {
    private static final String SELECT_BY_USERNAME =
            "SELECT user_id, username, role, password_hash, status, last_login FROM users_auth WHERE username = ?";
    private static final String UPDATE_LAST_LOGIN =
            "UPDATE users_auth SET last_login = ? WHERE user_id = ?";
    private static final String COUNT_USERNAME = "SELECT COUNT(*) FROM users_auth WHERE username = ?";
    private static final String NEXT_USER_ID = "SELECT COALESCE(MAX(user_id), 0) + 1 AS next_id FROM users_auth";
    private static final String INSERT_USER = "INSERT INTO users_auth (user_id, username, role, password_hash, status) VALUES (?, ?, ?, ?, 'ACTIVE')";
    private static final String DELETE_BY_USERNAME = "DELETE FROM users_auth WHERE username = ?";

    private final DatabaseManager dbManager;

    public UserDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public User findByUsername(String username) throws SQLException {
        try (Connection conn = dbManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USERNAME)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setRole(rs.getString("role"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setActive(!"INACTIVE".equalsIgnoreCase(rs.getString("status")));

                    Timestamp lastLogin = rs.getTimestamp("last_login");
                    if (lastLogin != null) {
                        user.setUpdatedAt(lastLogin.toLocalDateTime());
                    }

                    return user;
                }
            }
        }
        return null;
    }

    public void updateLastLogin(int userId) throws SQLException {
        try (Connection conn = dbManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_LAST_LOGIN)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public boolean usernameExists(String username) throws SQLException {
        try (Connection conn = dbManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_USERNAME)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public long insertUser(String username, String role, String passwordHash) throws SQLException {
        long userId = nextUserId();
        try (Connection conn = dbManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_USER)) {
            stmt.setLong(1, userId);
            stmt.setString(2, username);
            stmt.setString(3, role);
            stmt.setString(4, passwordHash);
            stmt.executeUpdate();
        }
        return userId;
    }

    public void deleteByUsername(String username) throws SQLException {
        try (Connection conn = dbManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_USERNAME)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }

    private long nextUserId() throws SQLException {
        try (Connection conn = dbManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(NEXT_USER_ID);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("next_id");
            }
        }
        return 1L;
    }
}
