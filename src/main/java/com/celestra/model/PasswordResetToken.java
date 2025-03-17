package com.celestra.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Represents a password reset token in the system.
 * Maps to the password_reset_tokens table in the database.
 */
public class PasswordResetToken {
    private Integer id;
    private Integer userId;
    private String token;
    private Timestamp createdAt;
    private Timestamp expiresAt;
    private Timestamp usedAt;
    
    // References to associated entities (not stored in database)
    private User user;
    
    /**
     * Default constructor
     */
    public PasswordResetToken() {
        // Default constructor
    }
    
    /**
     * Parameterized constructor with required fields
     * 
     * @param userId The ID of the user
     * @param token The reset token
     * @param createdAt The creation timestamp
     * @param expiresAt The expiration timestamp
     */
    public PasswordResetToken(Integer userId, String token, Timestamp createdAt, Timestamp expiresAt) {
        this.userId = userId;
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
    
    /**
     * Full parameterized constructor
     * 
     * @param id The token ID
     * @param userId The ID of the user
     * @param token The reset token
     * @param createdAt The creation timestamp
     * @param expiresAt The expiration timestamp
     * @param usedAt The timestamp when the token was used (null if not used)
     */
    public PasswordResetToken(Integer id, Integer userId, String token, Timestamp createdAt, 
                             Timestamp expiresAt, Timestamp usedAt) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
    }
    
    /**
     * Check if the token is expired.
     * 
     * @return true if the token is expired, false otherwise
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return expiresAt.before(new Timestamp(System.currentTimeMillis()));
    }
    
    /**
     * Check if the token has been used.
     * 
     * @return true if the token has been used, false otherwise
     */
    public boolean isUsed() {
        return usedAt != null;
    }
    
    /**
     * Check if the token is valid (not expired and not used).
     * 
     * @return true if the token is valid, false otherwise
     */
    public boolean isValid() {
        return !isExpired() && !isUsed();
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
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Timestamp getUsedAt() {
        return usedAt;
    }
    
    public void setUsedAt(Timestamp usedAt) {
        this.usedAt = usedAt;
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
        PasswordResetToken that = (PasswordResetToken) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(token, that.token);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, token);
    }
    
    @Override
    public String toString() {
        return "PasswordResetToken{" +
               "id=" + id +
               ", userId=" + userId +
               ", token='" + token + '\'' +
               ", createdAt=" + createdAt +
               ", expiresAt=" + expiresAt +
               ", usedAt=" + usedAt +
               '}';
    }
}