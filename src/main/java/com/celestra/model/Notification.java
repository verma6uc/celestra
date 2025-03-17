package com.celestra.model;

import java.time.OffsetDateTime;
import java.util.Objects;

import com.celestra.enums.NotificationDeliveryMethod;
import com.celestra.enums.NotificationPriority;
import com.celestra.enums.NotificationStatus;
import com.celestra.enums.NotificationType;

/**
 * Represents a system or user notification with delivery status.
 * Maps to the notifications table in the database.
 */
public class Notification {
    private Integer id;
    private Integer userId;
    private Integer companyId;
    private NotificationType notificationType;
    private String title;
    private String message;
    private NotificationPriority priority;
    private NotificationStatus status;
    private NotificationDeliveryMethod deliveryMethod;
    private OffsetDateTime readAt;
    private String actionUrl;
    private OffsetDateTime expiresAt;
    private OffsetDateTime deliveredAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    // References to associated entities (not stored in database)
    private User user;
    private Company company;
    
    /**
     * Default constructor
     */
    public Notification() {
        // Default constructor
    }
    
    /**
     * Parameterized constructor with required fields
     * 
     * @param userId The ID of the user receiving the notification
     * @param notificationType The category of notification
     * @param title The brief heading or subject of the notification
     * @param message The full notification content or body
     */
    public Notification(Integer userId, NotificationType notificationType, String title, String message) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.priority = NotificationPriority.MEDIUM; // Default priority
        this.status = NotificationStatus.PENDING; // Default status
        this.deliveryMethod = NotificationDeliveryMethod.IN_APP; // Default delivery method
    }
    
    /**
     * Full parameterized constructor
     * 
     * @param id The notification ID
     * @param userId The ID of the user receiving the notification
     * @param companyId The ID of the associated company (if applicable)
     * @param notificationType The category of notification
     * @param title The brief heading or subject of the notification
     * @param message The full notification content or body
     * @param priority The urgency level of the notification
     * @param status The current delivery status of the notification
     * @param deliveryMethod The channel used to deliver the notification
     * @param readAt The timestamp when user viewed the notification (null if unread)
     * @param actionUrl The URL or path for user to take action on notification
     * @param expiresAt The timestamp when notification expires and should be hidden
     * @param deliveredAt The timestamp when notification was successfully delivered
     * @param createdAt The creation timestamp
     * @param updatedAt The last update timestamp
     */
    public Notification(Integer id, Integer userId, Integer companyId, NotificationType notificationType, 
                       String title, String message, NotificationPriority priority, 
                       NotificationStatus status, NotificationDeliveryMethod deliveryMethod, 
                       OffsetDateTime readAt, String actionUrl, OffsetDateTime expiresAt, 
                       OffsetDateTime deliveredAt, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.companyId = companyId;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.priority = priority;
        this.status = status;
        this.deliveryMethod = deliveryMethod;
        this.readAt = readAt;
        this.actionUrl = actionUrl;
        this.expiresAt = expiresAt;
        this.deliveredAt = deliveredAt;
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

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public NotificationDeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(NotificationDeliveryMethod deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public OffsetDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(OffsetDateTime readAt) {
        this.readAt = readAt;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public OffsetDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(OffsetDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
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
    
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
        if (company != null) {
            this.companyId = company.getId();
        }
    }
    
    /**
     * Checks if the notification has been read
     * 
     * @return true if the notification has been read, false otherwise
     */
    public boolean isRead() {
        return readAt != null;
    }
    
    /**
     * Checks if the notification has been delivered
     * 
     * @return true if the notification has been delivered, false otherwise
     */
    public boolean isDelivered() {
        return status == NotificationStatus.DELIVERED;
    }
    
    /**
     * Checks if the notification has expired
     * 
     * @return true if the notification has expired, false otherwise
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(OffsetDateTime.now());
    }
    
    /**
     * Checks if the notification is critical
     * 
     * @return true if the notification is critical, false otherwise
     */
    public boolean isCritical() {
        return priority == NotificationPriority.CRITICAL;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(userId, that.userId) &&
               notificationType == that.notificationType &&
               Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, notificationType, createdAt);
    }

    @Override
    public String toString() {
        return "Notification{" +
               "id=" + id +
               ", userId=" + userId +
               ", companyId=" + companyId +
               ", notificationType=" + notificationType +
               ", title='" + title + '\'' +
               ", message='" + (message != null ? message.substring(0, Math.min(message.length(), 30)) + "..." : null) + '\'' +
               ", priority=" + priority +
               ", status=" + status +
               ", deliveryMethod=" + deliveryMethod +
               ", readAt=" + readAt +
               ", expiresAt=" + expiresAt +
               ", createdAt=" + createdAt +
               '}';
    }
}