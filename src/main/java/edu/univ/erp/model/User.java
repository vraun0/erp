package edu.univ.erp.model;

import java.time.LocalDateTime;

/**
 * User model for authentication system
 * Represents users in the auth_db database
 */
public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String role; // ADMIN, INSTRUCTOR, STUDENT
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int failedAttempts;
    private LocalDateTime lockoutTime;

    // Constructors
    public User() {
    }

    public User(String username, String passwordHash, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.failedAttempts = 0;
        this.lockoutTime = null;
    }

    public User(int userId, String username, String role, String passwordHash, boolean active) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.passwordHash = passwordHash;
        this.active = active;
        // Default values for new fields when using this constructor
        this.failedAttempts = 0;
        this.lockoutTime = null;
        this.createdAt = LocalDateTime.now(); // Assuming creation time is now if not provided
        this.updatedAt = LocalDateTime.now(); // Assuming update time is now if not provided
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public LocalDateTime getLockoutTime() {
        return lockoutTime;
    }

    public void setLockoutTime(LocalDateTime lockoutTime) {
        this.lockoutTime = lockoutTime;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isInstructor() {
        return "INSTRUCTOR".equals(role);
    }

    public boolean isStudent() {
        return "STUDENT".equals(role);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                '}';
    }
}
