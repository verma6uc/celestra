package com.celestra.dao;

import java.sql.SQLException;
import java.util.List;

import com.celestra.enums.NotificationDeliveryMethod;
import com.celestra.enums.NotificationPriority;
import com.celestra.enums.NotificationStatus;
import com.celestra.enums.NotificationType;
import com.celestra.model.Notification;

/**
 * Data Access Object interface for Notification entities.
 */
public interface NotificationDao extends BaseDao<Notification, Integer> {
    
    /**
     * Find notifications by user ID.
     * 
     * @param userId The user ID
     * @return A list of notifications for the user
     * @throws SQLException if a database access error occurs
     */
    List<Notification> findByUserId(Integer userId) throws SQLException;
    
    /**
     * Find notifications by company ID.
     * 
     * @param companyId The company ID
     * @return A list of notifications for the company
     * @throws SQLException if a database access error occurs
     */
    List<Notification> findByCompanyId(Integer companyId) throws SQLException;
    
    /**
     * Find notifications by type.
     * 
     * @param type The notification type
     * @return A list of notifications of the specified type
     * @throws SQLException if a database access error occurs
     */
    List<Notification> findByType(NotificationType type) throws SQLException;
    
    /**
     * Find notifications by status.
     * 
     * @param status The notification status
     * @return A list of notifications with the specified status
     * @throws SQLException if a database access error occurs
     */
    List<Notification> findByStatus(NotificationStatus status) throws SQLException;
    
    /**
     * Find notifications by priority.
     * 
     * @param priority The notification priority
     * @return A list of notifications with the specified priority
     * @throws SQLException if a database access error occurs
     */
    List<Notification> findByPriority(NotificationPriority priority) throws SQLException;
    
    /**
     * Find notifications by delivery method.
     * 
     * @param deliveryMethod The notification delivery method
     * @return A list of notifications with the specified delivery method
     * @throws SQLException if a database access error occurs
     */
    List<Notification> findByDeliveryMethod(NotificationDeliveryMethod deliveryMethod) throws SQLException;
    
    /**
     * Find unread notifications for a user.
     * 
     * @param userId The user ID
     * @return A list of unread notifications for the user
     * @throws SQLException if a database access error occurs
     */
    List<Notification> findUnreadByUserId(Integer userId) throws SQLException;
    
    /**
     * Mark a notification as read.
     * 
     * @param id The notification ID
     * @return true if the update was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean markAsRead(Integer id) throws SQLException;
    
    /**
     * Update the status of a notification.
     * 
     * @param id The notification ID
     * @param status The new status
     * @return true if the update was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean updateStatus(Integer id, NotificationStatus status) throws SQLException;
    
    /**
     * Find expired notifications.
     * 
     * @return A list of expired notifications
     * @throws SQLException if a database access error occurs
     */
    List<Notification> findExpired() throws SQLException;
}