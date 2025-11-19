package com.sis.app.service;

import com.sis.app.dao.UserDao;
import com.sis.app.model.User;
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
        
        String passwordHash = user.getPasswordHash();
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            System.err.println("Warning: Password hash is null or empty for user: " + username);
            return false;
        }
        
        // Check if hash looks like a valid BCrypt hash
        if (!passwordHash.startsWith("$2a$") && !passwordHash.startsWith("$2b$") && !passwordHash.startsWith("$2y$")) {
            System.err.println("Warning: Invalid password hash format for user: " + username + ". Hash should start with $2a$, $2b$, or $2y$");
            System.err.println("Current hash value: " + passwordHash);
            return false;
        }
        
        try {
            if (!BCrypt.checkpw(password, passwordHash)) {
                return false;
            }
        } catch (IllegalArgumentException e) {
            // Invalid hash format - treat as authentication failure
            System.err.println("Warning: BCrypt validation failed for user: " + username + ". Error: " + e.getMessage());
            System.err.println("Hash value: " + passwordHash);
            return false;
        }

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
}
