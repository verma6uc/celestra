package com.celestra.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Represents an unsuccessful authentication attempt.
 * Maps to the failed_logins table in the database.
 */
public class FailedLogin {
    private Integer id;
    private Integer userId;
    private String ipAddress;
    private OffsetDateTime attemptedAt;
    private String failureReason;
    
    // Reference to the associated user (not stored in database)
    private User user;
    
    /**
     * Default constructor
     */
    public FailedLogin() {
        // Default constructor
    }
    
    /**
     * Parameterized constructor with required fields
     * 
     * @param ipAddress The IP address where login attempt originated
     * @param failureReason The description of why authentication failed
     */
    public FailedLogin(String ipAddress, String failureReason) {
        this.ipAddress = ipAddress;
        this.failureReason = failureReason;
    }
    
    /**
     * Full parameterized constructor
     * 
     * @param id The failed login record ID
     * @param userId The ID of the user account (if known/valid)
     * @param ipAddress The IP address where login attempt originated
     * @param attemptedAt The timestamp when login attempt occurred
     * @param failureReason The description of why authentication failed
     */
    public FailedLogin(Integer id, Integer userId, String ipAddress, 
                      OffsetDateTime attemptedAt, String failureReason) {
        this.id = id;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.attemptedAt = attemptedAt;
        this.failureReason = failureReason;
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

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public OffsetDateTime getAttemptedAt() {
        return attemptedAt;
    }

    public void setAttemptedAt(OffsetDateTime attemptedAt) {
        this.attemptedAt = attemptedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
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
        FailedLogin that = (FailedLogin) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(ipAddress, that.ipAddress) &&
               Objects.equals(attemptedAt, that.attemptedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, ipAddress, attemptedAt);
    }

    @Override
    public String toString() {
        return "FailedLogin{" +
               "id=" + id +
               ", userId=" + userId +
               ", ipAddress='" + ipAddress + '\'' +
               ", attemptedAt=" + attemptedAt +
               ", failureReason='" + failureReason + '\'' +
               '}';
    }
}