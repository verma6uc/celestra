package com.celestra.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Represents a previous user password to prevent reuse.
 * Maps to the password_history table in the database.
 */
public class PasswordHistory {
    private Integer id;
    private Integer userId;
    private String passwordHash;
    private Timestamp createdAt;
    
    // Reference to the associated user (not stored in database)
    private User user;
    
    /**
     * Default constructor
     */
    public PasswordHistory() {
        // Default constructor
    }
    
    /**
     * Parameterized constructor with required fields
     * 
     * @param userId The ID of the associated user account
     * @param passwordHash The hashed version of previous password
     */
    public PasswordHistory(Integer userId, String passwordHash) {
        this.userId = userId;
        this.passwordHash = passwordHash;
    }
    
    /**
     * Full parameterized constructor
     * 
     * @param id The password history record ID
     * @param userId The ID of the associated user account
     * @param passwordHash The hashed version of previous password
     * @param createdAt The timestamp when this password was initially set
     */
    public PasswordHistory(Integer id, Integer userId, String passwordHash, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordHistory that = (PasswordHistory) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(passwordHash, that.passwordHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, passwordHash);
    }

    @Override
    public String toString() {
        return "PasswordHistory{" +
               "id=" + id +
               ", userId=" + userId +
               ", passwordHash='" + "[REDACTED]" + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
}