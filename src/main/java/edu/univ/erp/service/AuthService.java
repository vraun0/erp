package edu.univ.erp.service;

import edu.univ.erp.dao.UserDao;
import edu.univ.erp.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

/**
 * Handles authentication related logic.
 */
public class AuthService {
    private final UserDao userDao;
    private User authenticatedUser;

    public AuthService() {
        this.userDao = new UserDao();
    }

    /**
     * Attempts to authenticate the given username and password.
     *
     * @return true if credentials are valid and account is active.
     */
    public boolean login(String username, String password) throws SQLException {
        User user = userDao.findByUsername(username);
        if (user == null) {
            return false;
        }
        if (!user.isActive()) {
            return false;
        }

        // Check for lockout
        if (user.getLockoutTime() != null) {
            if (user.getLockoutTime().isAfter(java.time.LocalDateTime.now())) {
                throw new SQLException("Account is locked. Please try again later.");
            } else {
                // Lockout expired
                userDao.resetFailedAttempts(user.getUserId());
            }
        }

        String passwordHash = user.getPasswordHash();
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            System.err.println("Warning: Password hash is null or empty for user: " + username);
            return false;
        }

        // Check if hash looks like a valid BCrypt hash
        if (!passwordHash.startsWith("$2a$") && !passwordHash.startsWith("$2b$") && !passwordHash.startsWith("$2y$")) {
            System.err.println("Warning: Invalid password hash format for user: " + username
                    + ". Hash should start with $2a$, $2b$, or $2y$");
            System.err.println("Current hash value: " + passwordHash);
            return false;
        }

        try {
            if (!BCrypt.checkpw(password, passwordHash)) {
                // Increment failed attempts
                int attempts = user.getFailedAttempts() + 1;
                java.time.LocalDateTime lockoutTime = null;
                if (attempts >= 5) {
                    lockoutTime = java.time.LocalDateTime.now().plusMinutes(15);
                }
                userDao.updateFailedAttempts(user.getUserId(), attempts, lockoutTime);
                return false;
            }
        } catch (IllegalArgumentException e) {
            // Invalid hash format - treat as authentication failure
            System.err
                    .println("Warning: BCrypt validation failed for user: " + username + ". Error: " + e.getMessage());
            System.err.println("Hash value: " + passwordHash);
            return false;
        }

        // Success
        userDao.resetFailedAttempts(user.getUserId());
        this.authenticatedUser = user;
        userDao.updateLastLogin(user.getUserId());
        return true;
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void logout() {
        authenticatedUser = null;
    }

    public boolean isAuthenticated() {
        return authenticatedUser != null;
    }

    public void changePassword(String username, String oldPassword, String newPassword)
            throws ServiceException, SQLException {
        User user = userDao.findByUsername(username);
        if (user == null) {
            throw new ServiceException("User not found.");
        }

        if (!BCrypt.checkpw(oldPassword, user.getPasswordHash())) {
            throw new ServiceException("Incorrect old password.");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new ServiceException("New password must be at least 6 characters long.");
        }

        // Check password history
        java.util.List<String> recentHashes = userDao.getRecentPasswordHashes(user.getUserId());
        for (String hash : recentHashes) {
            if (BCrypt.checkpw(newPassword, hash)) {
                throw new ServiceException("You cannot reuse a recent password.");
            }
        }

        String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        userDao.updatePassword(username, newHash);
    }
}
