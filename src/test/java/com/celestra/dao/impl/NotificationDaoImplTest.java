package com.celestra.dao.impl;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.celestra.dao.BaseDaoTest;
import com.celestra.dao.NotificationDao;
import com.celestra.enums.NotificationDeliveryMethod;
import com.celestra.enums.NotificationPriority;
import com.celestra.enums.NotificationStatus;
import com.celestra.enums.NotificationType;
import com.celestra.model.Notification;

/**
 * Test class for NotificationDaoImpl.
 */
public class NotificationDaoImplTest extends BaseDaoTest {
    
    private NotificationDao notificationDao;
    
    /**
     * Initialize the DAO before each test.
     */
    @Before
    public void initialize() {
        notificationDao = new NotificationDaoImpl();
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // Notifications table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // Insert test companies first (to satisfy foreign key constraints)
        executeSQL("INSERT INTO companies (id, name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES (1, 'Test Company 1', 'Test Company Description 1', 'SMALL'::company_size, 'TECH'::company_vertical, 'ACTIVE'::company_status, NOW(), NOW())");
        
        // Insert test users (to satisfy foreign key constraints)
        executeSQL("INSERT INTO users (id, company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES (1, 1, 'COMPANY_ADMIN'::user_role, 'admin@test.com', 'Admin User', 'hash123', 'ACTIVE'::user_status, NOW(), NOW())");
        
        executeSQL("INSERT INTO users (id, company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES (2, 1, 'REGULAR_USER'::user_role, 'user@test.com', 'Regular User', 'hash456', 'ACTIVE'::user_status, NOW(), NOW())");
        
        // Insert test notifications with nextval for id to avoid conflicts
        executeSQL("INSERT INTO notifications (id, user_id, company_id, notification_type, title, message, priority, status, " +
                   "delivery_method, read_at, action_url, expires_at, delivered_at, created_at, updated_at) " +
                   "VALUES (nextval('notifications_id_seq'), 1, 1, 'FAILED_LOGIN_NOTIFICATION'::notification_type, 'Failed Login', 'There was a failed login attempt', " +
                   "'MEDIUM'::notification_priority, 'DELIVERED'::notification_status, 'IN_APP'::notification_delivery_method, " +
                   "NULL, '/login', NOW() + INTERVAL '7 days', NOW(), NOW(), NOW())");
        
        executeSQL("INSERT INTO notifications (id, user_id, company_id, notification_type, title, message, priority, status, " +
                   "delivery_method, read_at, action_url, expires_at, delivered_at, created_at, updated_at) " +
                   "VALUES (nextval('notifications_id_seq'), 2, 1, 'PASSWORD_RESET'::notification_type, 'Password Reset', 'Your password has been reset', " +
                   "'HIGH'::notification_priority, 'DELIVERED'::notification_status, 'EMAIL'::notification_delivery_method, " +
                   "NOW(), '/reset-password', NOW() + INTERVAL '7 days', NOW(), NOW(), NOW())");

        executeSQL("INSERT INTO notifications (id, user_id, company_id, notification_type, title, message, priority, status, " +
                   "delivery_method, read_at, action_url, expires_at, delivered_at, created_at, updated_at) " +
                   "VALUES (nextval('notifications_id_seq'), 1, 1, 'SECURITY_ALERT'::notification_type, 'Security Alert', 'Suspicious activity detected', " +
                   "'CRITICAL'::notification_priority, 'PENDING'::notification_status, 'SMS'::notification_delivery_method, " +
                   "NULL, '/security', NOW() - INTERVAL '7 days', NULL, NOW(), NOW())");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM notifications WHERE title LIKE 'Test%' OR title LIKE 'Failed Login' OR title LIKE 'Password Reset' OR title LIKE 'Security Alert'");
        executeSQL("DELETE FROM users WHERE id IN (1, 2)");
        executeSQL("DELETE FROM companies WHERE id = 1");
    }
    
    /**
     * Test the create method.
     */
    @Test
    public void testCreate() throws SQLException {
        // Create a new notification
        Notification notification = new Notification();
        notification.setUserId(1);
        notification.setCompanyId(1);
        notification.setNotificationType(NotificationType.GENERAL);
        notification.setTitle("Test Notification");
        notification.setMessage("This is a test notification");
        notification.setPriority(NotificationPriority.MEDIUM);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setDeliveryMethod(NotificationDeliveryMethod.IN_APP);
        
        Notification createdNotification = notificationDao.create(notification);
        
        // Verify the notification was created
        assertNotNull("Created notification should not be null", createdNotification);
        assertTrue("Created notification should have an ID", createdNotification.getId() > 0);
        
        // Clean up
        boolean deleted = notificationDao.delete(createdNotification.getId());
        assertTrue("Notification should be deleted successfully", deleted);
    }
    
    /**
     * Test the findById method.
     */
    @Test
    public void testFindById() throws SQLException {
        // Find all notifications
        List<Notification> notifications = notificationDao.findAll();
        
        // Verify there are notifications
        assertFalse("There should be notifications in the database", notifications.isEmpty());
        
        // Get the first notification
        Notification notification = notifications.get(0);
        
        // Find the notification by ID
        Optional<Notification> foundNotification = notificationDao.findById(notification.getId());
        
        // Verify the notification was found
        assertTrue("Notification should be found by ID", foundNotification.isPresent());
        assertEquals("Found notification ID should match", notification.getId(), foundNotification.get().getId());
        assertEquals("Found notification title should match", notification.getTitle(), foundNotification.get().getTitle());
    }
    
    /**
     * Test the findAll method.
     */
    @Test
    public void testFindAll() throws SQLException {
        // Find all notifications
        List<Notification> notifications = notificationDao.findAll();
        
        // Verify there are notifications
        assertFalse("There should be notifications in the database", notifications.isEmpty());
    }
    
    /**
     * Test the update method.
     */
    @Test
    public void testUpdate() throws SQLException {
        // Create a new notification
        Notification notification = new Notification();
        notification.setUserId(1);
        notification.setCompanyId(1);
        notification.setNotificationType(NotificationType.GENERAL);
        notification.setTitle("Test Notification Update");
        notification.setMessage("This is a test notification for update");
        notification.setPriority(NotificationPriority.MEDIUM);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setDeliveryMethod(NotificationDeliveryMethod.IN_APP);
        
        Notification createdNotification = notificationDao.create(notification);
        
        // Update the notification
        createdNotification.setTitle("Test Notification Updated");
        createdNotification.setMessage("This is an updated test notification");
        createdNotification.setStatus(NotificationStatus.DELIVERED);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        createdNotification.setDeliveredAt(now);
        
        Notification updatedNotification = notificationDao.update(createdNotification);
        
        // Verify the notification was updated
        assertEquals("Notification title should be updated", "Test Notification Updated", updatedNotification.getTitle());
        assertEquals("Notification message should be updated", "This is an updated test notification", updatedNotification.getMessage());
        assertEquals("Notification status should be updated", NotificationStatus.DELIVERED, updatedNotification.getStatus());
        assertNotNull("Notification delivered at should be set", updatedNotification.getDeliveredAt());
        
        // Clean up
        boolean deleted = notificationDao.delete(createdNotification.getId());
        assertTrue("Notification should be deleted successfully", deleted);
    }
    
    /**
     * Test the delete method.
     */
    @Test
    public void testDelete() throws SQLException {
        // Create a new notification
        Notification notification = new Notification();
        notification.setUserId(1);
        notification.setCompanyId(1);
        notification.setNotificationType(NotificationType.GENERAL);
        notification.setTitle("Test Notification Delete");
        notification.setMessage("This is a test notification for delete");
        notification.setPriority(NotificationPriority.MEDIUM);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setDeliveryMethod(NotificationDeliveryMethod.IN_APP);
        
        Notification createdNotification = notificationDao.create(notification);
        
        // Delete the notification
        boolean deleted = notificationDao.delete(createdNotification.getId());
        
        // Verify the notification was deleted
        assertTrue("Notification should be deleted successfully", deleted);
        
        Optional<Notification> foundNotification = notificationDao.findById(createdNotification.getId());
        assertFalse("Notification should not be found after deletion", foundNotification.isPresent());
    }
    
    /**
     * Test the findByUserId method.
     */
    @Test
    public void testFindByUserId() throws SQLException {
        // Find notifications by user ID
        List<Notification> notifications = notificationDao.findByUserId(1);
        
        // Verify there are notifications
        assertFalse("There should be notifications for user ID 1", notifications.isEmpty());
        
        // Verify all entries have the correct user ID
        for (Notification notification : notifications) {
            assertEquals("Notification user ID should be 1", Integer.valueOf(1), notification.getUserId());
        }
    }
    
    /**
     * Test the findByCompanyId method.
     */
    @Test
    public void testFindByCompanyId() throws SQLException {
        // Find notifications by company ID
        List<Notification> notifications = notificationDao.findByCompanyId(1);
        
        // Verify there are notifications
        assertFalse("There should be notifications for company ID 1", notifications.isEmpty());
        
        // Verify all entries have the correct company ID
        for (Notification notification : notifications) {
            assertEquals("Notification company ID should be 1", Integer.valueOf(1), notification.getCompanyId());
        }
    }
    
    /**
     * Test the findByType method.
     */
    @Test
    public void testFindByType() throws SQLException {
        // Find notifications by type
        List<Notification> notifications = notificationDao.findByType(NotificationType.FAILED_LOGIN_NOTIFICATION);
        
        // Verify there are notifications
        assertFalse("There should be notifications of type FAILED_LOGIN_NOTIFICATION", notifications.isEmpty());
        
        // Verify all entries have the correct type
        for (Notification notification : notifications) {
            assertEquals("Notification type should be FAILED_LOGIN_NOTIFICATION", NotificationType.FAILED_LOGIN_NOTIFICATION, notification.getNotificationType());
        }
    }
    
    /**
     * Test the findByStatus method.
     */
    @Test
    public void testFindByStatus() throws SQLException {
        // Find notifications by status
        List<Notification> notifications = notificationDao.findByStatus(NotificationStatus.DELIVERED);
        
        // Verify there are notifications
        assertFalse("There should be notifications with status DELIVERED", notifications.isEmpty());
        
        // Verify all entries have the correct status
        for (Notification notification : notifications) {
            assertEquals("Notification status should be DELIVERED", NotificationStatus.DELIVERED, notification.getStatus());
        }
    }
    
    /**
     * Test the findByPriority method.
     */
    @Test
    public void testFindByPriority() throws SQLException {
        // Find notifications by priority
        List<Notification> notifications = notificationDao.findByPriority(NotificationPriority.HIGH);
        
        // Verify there are notifications
        assertFalse("There should be notifications with priority HIGH", notifications.isEmpty());
        
        // Verify all entries have the correct priority
        for (Notification notification : notifications) {
            assertEquals("Notification priority should be HIGH", NotificationPriority.HIGH, notification.getPriority());
        }
    }
    
    /**
     * Test the findByDeliveryMethod method.
     */
    @Test
    public void testFindByDeliveryMethod() throws SQLException {
        // Find notifications by delivery method
        List<Notification> notifications = notificationDao.findByDeliveryMethod(NotificationDeliveryMethod.IN_APP);
        
        // Verify there are notifications
        assertFalse("There should be notifications with delivery method IN_APP", notifications.isEmpty());
        
        // Verify all entries have the correct delivery method
        for (Notification notification : notifications) {
            assertEquals("Notification delivery method should be IN_APP", NotificationDeliveryMethod.IN_APP, notification.getDeliveryMethod());
        }
    }
    
    /**
     * Test the findUnreadByUserId method.
     */
    @Test
    public void testFindUnreadByUserId() throws SQLException {
        // Find unread notifications by user ID
        List<Notification> notifications = notificationDao.findUnreadByUserId(1);
        
        // Verify there are notifications
        assertFalse("There should be unread notifications for user ID 1", notifications.isEmpty());
        
        // Verify all entries have the correct user ID and are unread
        for (Notification notification : notifications) {
            assertEquals("Notification user ID should be 1", Integer.valueOf(1), notification.getUserId());
            assertNull("Notification read at should be null", notification.getReadAt());
        }
    }
    
    /**
     * Test the markAsRead method.
     */
    @Test
    public void testMarkAsRead() throws SQLException {
        // Create a new notification
        Notification notification = new Notification();
        notification.setUserId(1);
        notification.setCompanyId(1);
        notification.setNotificationType(NotificationType.GENERAL);
        notification.setTitle("Test Notification Read");
        notification.setMessage("This is a test notification for marking as read");
        notification.setPriority(NotificationPriority.MEDIUM);
        notification.setStatus(NotificationStatus.DELIVERED);
        notification.setDeliveryMethod(NotificationDeliveryMethod.IN_APP);
        
        Notification createdNotification = notificationDao.create(notification);
        
        // Mark the notification as read
        boolean marked = notificationDao.markAsRead(createdNotification.getId());
        
        // Verify the notification was marked as read
        assertTrue("Notification should be marked as read successfully", marked);
        
        Optional<Notification> updatedNotification = notificationDao.findById(createdNotification.getId());
        assertTrue("Notification should be found after marking as read", updatedNotification.isPresent());
        assertNotNull("Notification read at should be set", updatedNotification.get().getReadAt());
        
        // Clean up
        boolean deleted = notificationDao.delete(createdNotification.getId());
        assertTrue("Notification should be deleted successfully", deleted);
    }
    
    /**
     * Test the updateStatus method.
     */
    @Test
    public void testUpdateStatus() throws SQLException {
        // Create a new notification
        Notification notification = new Notification();
        notification.setUserId(1);
        notification.setCompanyId(1);
        notification.setNotificationType(NotificationType.GENERAL);
        notification.setTitle("Test Notification Status");
        notification.setMessage("This is a test notification for status update");
        notification.setPriority(NotificationPriority.MEDIUM);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setDeliveryMethod(NotificationDeliveryMethod.IN_APP);
        
        Notification createdNotification = notificationDao.create(notification);
        
        // Update the notification status
        boolean updated = notificationDao.updateStatus(createdNotification.getId(), NotificationStatus.DELIVERED);
        
        // Verify the notification status was updated
        assertTrue("Notification status should be updated successfully", updated);
        
        Optional<Notification> updatedNotification = notificationDao.findById(createdNotification.getId());
        assertTrue("Notification should be found after status update", updatedNotification.isPresent());
        assertEquals("Notification status should be updated to DELIVERED", NotificationStatus.DELIVERED, updatedNotification.get().getStatus());
        
        // Clean up
        boolean deleted = notificationDao.delete(createdNotification.getId());
        assertTrue("Notification should be deleted successfully", deleted);
    }
    
    /**
     * Test the findExpired method.
     */
    @Test
    public void testFindExpired() throws SQLException {
        // Create a new notification that is expired
        Notification notification = new Notification();
        notification.setUserId(1);
        notification.setCompanyId(1);
        notification.setNotificationType(NotificationType.GENERAL);
        notification.setTitle("Test Notification Expired");
        notification.setMessage("This is a test notification that is expired");
        notification.setPriority(NotificationPriority.MEDIUM);
        notification.setStatus(NotificationStatus.DELIVERED);
        notification.setDeliveryMethod(NotificationDeliveryMethod.IN_APP);
        // Set expiry date to 7 days ago
        notification.setExpiresAt(new Timestamp(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L));
        
        Notification createdNotification = notificationDao.create(notification);
        
        // Find expired notifications
        List<Notification> expiredNotifications = notificationDao.findExpired();
        
        // Verify there are expired notifications
        assertFalse("There should be expired notifications", expiredNotifications.isEmpty());
        
        // Clean up
        boolean deleted = notificationDao.delete(createdNotification.getId());
        assertTrue("Notification should be deleted successfully", deleted);
    }
}