package com.celestra.model;

import java.time.OffsetDateTime;
import java.util.Objects;

import com.celestra.enums.InvitationStatus;

/**
 * Represents a system access invitation.
 * Maps to the invitations table in the database.
 */
public class Invitation {
    private Integer id;
    private Integer userId;
    private String token;
    private InvitationStatus status;
    private OffsetDateTime sentAt;
    private OffsetDateTime expiresAt;
    private Integer resendCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    // Reference to the associated user (not stored in database)
    private User user;
    
    /**
     * Default constructor
     */
    public Invitation() {
        // Default constructor
    }
    
    /**
     * Parameterized constructor with required fields
     * 
     * @param userId The ID of the invited user account
     * @param token The secure random token for invitation validation
     */
    public Invitation(Integer userId, String token) {
        this.userId = userId;
        this.token = token;
        this.status = InvitationStatus.PENDING; // Default status
        this.resendCount = 0; // Default resend count
    }
    
    /**
     * Full parameterized constructor
     * 
     * @param id The invitation ID
     * @param userId The ID of the invited user account
     * @param token The secure random token for invitation validation
     * @param status The current state of the invitation process
     * @param sentAt The timestamp when invitation was sent to user
     * @param expiresAt The timestamp when invitation becomes invalid
     * @param resendCount The number of times invitation has been resent
     * @param createdAt The creation timestamp
     * @param updatedAt The last update timestamp
     */
    public Invitation(Integer id, Integer userId, String token, InvitationStatus status, 
                     OffsetDateTime sentAt, OffsetDateTime expiresAt, Integer resendCount, 
                     OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.status = status;
        this.sentAt = sentAt;
        this.expiresAt = expiresAt;
        this.resendCount = resendCount;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
    }

    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(OffsetDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Integer getResendCount() {
        return resendCount;
    }

    public void setResendCount(Integer resendCount) {
        this.resendCount = resendCount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
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
     * Checks if the invitation is pending
     * 
     * @return true if the invitation is pending, false otherwise
     */
    public boolean isPending() {
        return status == InvitationStatus.PENDING;
    }
    
    /**
     * Checks if the invitation has been sent
     * 
     * @return true if the invitation has been sent, false otherwise
     */
    public boolean isSent() {
        return status == InvitationStatus.SENT;
    }
    
    /**
     * Checks if the invitation has expired
     * 
     * @return true if the invitation has expired, false otherwise
     */
    public boolean isExpired() {
        return status == InvitationStatus.EXPIRED || 
               (expiresAt != null && expiresAt.isBefore(OffsetDateTime.now()));
    }
    
    /**
     * Checks if the invitation has been accepted
     * 
     * @return true if the invitation has been accepted, false otherwise
     */
    public boolean isAccepted() {
        return status == InvitationStatus.ACCEPTED;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invitation that = (Invitation) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, token);
    }

    @Override
    public String toString() {
        return "Invitation{" +
               "id=" + id +
               ", userId=" + userId +
               ", token='" + token + '\'' +
               ", status=" + status +
               ", sentAt=" + sentAt +
               ", expiresAt=" + expiresAt +
               ", resendCount=" + resendCount +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}