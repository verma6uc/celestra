package com.celestra.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Represents an active authenticated user session.
 * Maps to the user_sessions table in the database.
 */
public class UserSession {
    private Integer id;
    private Integer userId;
    private String sessionToken;
    private String ipAddress;
    private String userAgent;
    private Timestamp createdAt;
    private Timestamp expiresAt;
    
    // Reference to the associated user (not stored in database)
    private User user;
    
    /**
     * Default constructor
     */
    public UserSession() {
        // Default constructor
    }
    
    /**
     * Parameterized constructor with required fields
     * 
     * @param userId The ID of the user account for this session
     * @param sessionToken The secure cryptographic token for session validation
     * @param expiresAt The timestamp when session will automatically terminate
     */
    public UserSession(Integer userId, String sessionToken, Timestamp expiresAt) {
        this.userId = userId;
        this.sessionToken = sessionToken;
        this.expiresAt = expiresAt;
    }
    
    /**
     * Full parameterized constructor
     * 
     * @param id The session ID
     * @param userId The ID of the user account for this session
     * @param sessionToken The secure cryptographic token for session validation
     * @param ipAddress The IP address of client that created session
     * @param userAgent The browser/client information
     * @param createdAt The timestamp when session was established
     * @param expiresAt The timestamp when session will automatically terminate
     */
    public UserSession(Integer id, Integer userId, String sessionToken, String ipAddress, 
                      String userAgent, Timestamp createdAt, Timestamp expiresAt) {
        this.id = id;
        this.userId = userId;
        this.sessionToken = sessionToken;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
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

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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
     * Checks if the session is expired
     * 
     * @return true if the session is expired, false otherwise
     */
    public boolean isExpired() {
        return expiresAt.before(new Timestamp(System.currentTimeMillis()));
    }
    
    /**
     * Checks if the session is active
     * 
     * @return true if the session is active, false otherwise
     */
    public boolean isActive() {
        return !isExpired();
    }
    
    /**
     * Calculates the remaining time until session expiration
     * 
     * @return The number of seconds until session expiration, or 0 if already expired
     */
    public long getSecondsUntilExpiration() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (expiresAt.before(now)) {
            return 0;
        }
        return (expiresAt.getTime() - now.getTime()) / 1000;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSession that = (UserSession) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(sessionToken, that.sessionToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, sessionToken);
    }

    @Override
    public String toString() {
        return "UserSession{" +
               "id=" + id +
               ", userId=" + userId +
               ", sessionToken='" + "[REDACTED]" + '\'' +
               ", ipAddress='" + ipAddress + '\'' +
               ", userAgent='" + (userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 30)) + "..." : null) + '\'' +
               ", createdAt=" + createdAt +
               ", expiresAt=" + expiresAt +
               '}';
    }
}