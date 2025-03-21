package com.celestra.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Represents a temporary or permanent account access restriction.
 * Maps to the user_lockouts table in the database.
 */
public class UserLockout {
    private Integer id;
    private Integer userId;
    private Timestamp lockoutStart;
    private Timestamp lockoutEnd;
    private Integer failedAttempts;
    private String reason;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Reference to the associated user (not stored in database)
    private User user;
    
    /**
     * Default constructor
     */
    public UserLockout() {
        // Default constructor
    }
    
    /**
     * Parameterized constructor with required fields
     * 
     * @param userId The ID of the locked user account
     * @param failedAttempts The number of failed login attempts that triggered lockout
     */
    public UserLockout(Integer userId, Integer failedAttempts) {
        this.userId = userId;
        this.failedAttempts = failedAttempts;
        this.lockoutStart = new Timestamp(System.currentTimeMillis());
    }
    
    /**
     * Full parameterized constructor
     * 
     * @param id The lockout record ID
     * @param userId The ID of the locked user account
     * @param lockoutStart The timestamp when lockout began
     * @param lockoutEnd The timestamp when lockout expires (null if permanent)
     * @param failedAttempts The number of failed login attempts that triggered lockout
     * @param reason The description of why account was locked
     * @param createdAt The creation timestamp
     * @param updatedAt The last update timestamp
     */
    public UserLockout(Integer id, Integer userId, Timestamp lockoutStart, 
                      Timestamp lockoutEnd, Integer failedAttempts, String reason, 
                      Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.userId = userId;
        this.lockoutStart = lockoutStart;
        this.lockoutEnd = lockoutEnd;
        this.failedAttempts = failedAttempts;
        this.reason = reason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public Timestamp getLockoutStart() {
        return lockoutStart;
    }

    public void setLockoutStart(Timestamp lockoutStart) {
        this.lockoutStart = lockoutStart;
    }

    public Timestamp getLockoutEnd() {
        return lockoutEnd;
    }

    public void setLockoutEnd(Timestamp lockoutEnd) {
        this.lockoutEnd = lockoutEnd;
    }

    public Integer getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
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
    
    /**
     * Checks if the lockout is permanent
     * 
     * @return true if the lockout is permanent, false otherwise
     */
    public boolean isPermanent() {
        return lockoutEnd == null;
    }
    
    /**
     * Checks if the lockout is active
     * 
     * @return true if the lockout is currently active, false otherwise
     */
    public boolean isActive() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return lockoutStart.before(now) && 
               (lockoutEnd == null || lockoutEnd.after(now));
    }
    
    /**
     * Checks if the lockout has expired
     * 
     * @return true if the lockout has expired, false otherwise
     */
    public boolean isExpired() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return lockoutEnd != null && lockoutEnd.before(now);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserLockout that = (UserLockout) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(lockoutStart, that.lockoutStart);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, lockoutStart);
    }

    @Override
    public String toString() {
        return "UserLockout{" +
               "id=" + id +
               ", userId=" + userId +
               ", lockoutStart=" + lockoutStart +
               ", lockoutEnd=" + lockoutEnd +
               ", failedAttempts=" + failedAttempts +
               ", reason='" + reason + '\'' +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}