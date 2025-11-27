package edu.univ.erp.dao;

import edu.univ.erp.config.DatabaseManager;
import edu.univ.erp.model.User;

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
    private static final String SELECT_BY_USERNAME = "SELECT user_id, username, role, password_hash, status, last_login, failed_attempts, lockout_time FROM users_auth WHERE username = ?";
    private static final String SELECT_BY_ID = "SELECT user_id, username, role, password_hash, status, last_login, failed_attempts, lockout_time FROM users_auth WHERE user_id = ?";
    private static final String UPDATE_LAST_LOGIN = "UPDATE users_auth SET last_login = ? WHERE user_id = ?";
    private static final String COUNT_USERNAME = "SELECT COUNT(*) FROM users_auth WHERE username = ?";
    private static final String NEXT_USER_ID = "SELECT COALESCE(MAX(user_id), 0) + 1 AS next_id FROM users_auth";
    private static final String INSERT_USER = "INSERT INTO users_auth (username, role, password_hash, status) VALUES (?, ?, ?, 'ACTIVE')";
    private static final String DELETE_BY_USERNAME = "DELETE FROM users_auth WHERE username = ?";
    private static final String UPDATE_PASSWORD = "UPDATE users_auth SET password_hash = ? WHERE username = ?";
    private static final String UPDATE_FAILED_ATTEMPTS = "UPDATE users_auth SET failed_attempts = ?, lockout_time = ? WHERE user_id = ?";
    private static final String RESET_FAILED_ATTEMPTS = "UPDATE users_auth SET failed_attempts = 0, lockout_time = NULL WHERE user_id = ?";
    private static final String INSERT_PASSWORD_HISTORY = "INSERT INTO password_history (user_id, password_hash) VALUES (?, ?)";
    private static final String CHECK_PASSWORD_HISTORY = "SELECT password_hash FROM password_history WHERE user_id = ? ORDER BY changed_at DESC LIMIT 3";

    private final DatabaseManager dbManager;

    public UserDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public User findByUsername(String username) throws SQLException {
        try (Connection conn = dbManager.getAuthConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USERNAME)) {

            stmt.setString(1, username);
            return getUser(stmt);
        }
    }

    public User findById(int userId) throws SQLException {
        try (Connection conn = dbManager.getAuthConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, userId);
            return getUser(stmt);
        }
    }

    private User getUser(PreparedStatement stmt) throws SQLException {
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

                user.setFailedAttempts(rs.getInt("failed_attempts"));
                Timestamp lockoutTime = rs.getTimestamp("lockout_time");
                if (lockoutTime != null) {
                    user.setLockoutTime(lockoutTime.toLocalDateTime());
                }

                return user;
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
        try (Connection conn = dbManager.getAuthConnection();
                PreparedStatement stmt = conn.prepareStatement(INSERT_USER, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, role);
            stmt.setString(3, passwordHash);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to insert user");
    }

    public void deleteByUsername(String username) throws SQLException {
        try (Connection conn = dbManager.getAuthConnection();
                PreparedStatement stmt = conn.prepareStatement(DELETE_BY_USERNAME)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }

    public void updatePassword(String username, String newPasswordHash) throws SQLException {
        User user = findByUsername(username);
        if (user == null)
            return;

        try (Connection conn = dbManager.getAuthConnection()) {
            // Save old password to history
            try (PreparedStatement histStmt = conn.prepareStatement(INSERT_PASSWORD_HISTORY)) {
                histStmt.setInt(1, user.getUserId());
                histStmt.setString(2, user.getPasswordHash());
                histStmt.executeUpdate();
            }

            // Update new password
            try (PreparedStatement stmt = conn.prepareStatement(UPDATE_PASSWORD)) {
                stmt.setString(1, newPasswordHash);
                stmt.setString(2, username);
                stmt.executeUpdate();
            }
        }
    }

    public void updateFailedAttempts(int userId, int attempts, LocalDateTime lockoutTime) throws SQLException {
        try (Connection conn = dbManager.getAuthConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_FAILED_ATTEMPTS)) {
            stmt.setInt(1, attempts);
            stmt.setTimestamp(2, lockoutTime != null ? Timestamp.valueOf(lockoutTime) : null);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
        }
    }

    public void resetFailedAttempts(int userId) throws SQLException {
        try (Connection conn = dbManager.getAuthConnection();
                PreparedStatement stmt = conn.prepareStatement(RESET_FAILED_ATTEMPTS)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public boolean isPasswordInHistory(int userId, String newHash) throws SQLException {
        // Note: We cannot directly compare hashes because of salt.
        // We need to return the list of old hashes and let the service check them.
        // But for simplicity in DAO, let's just return the list of hashes.
        return false;
    }

    public java.util.List<String> getRecentPasswordHashes(int userId) throws SQLException {
        java.util.List<String> hashes = new java.util.ArrayList<>();
        try (Connection conn = dbManager.getAuthConnection();
                PreparedStatement stmt = conn.prepareStatement(CHECK_PASSWORD_HISTORY)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    hashes.add(rs.getString("password_hash"));
                }
            }
        }
        return hashes;
    }
}
