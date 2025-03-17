package com.celestra.seeding.seeders;

import com.celestra.dao.NotificationDao;
import com.celestra.dao.impl.NotificationDaoImpl;
import com.celestra.model.Notification;
import com.celestra.enums.NotificationDeliveryMethod;
import com.celestra.enums.NotificationPriority;
import com.celestra.enums.NotificationStatus;
import com.celestra.enums.NotificationType;
import com.celestra.seeding.util.EnumUtil;
import com.celestra.seeding.util.FakerUtil;
import com.celestra.seeding.util.TimestampUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Seeder class for the notifications table.
 * This class is responsible for generating and inserting test data for notifications.
 * It uses the NotificationDao to interact with the database.
 */
public class NotificationSeeder {
    
    private static final Logger LOGGER = Logger.getLogger(NotificationSeeder.class.getName());
    
    // Notification type distribution
    private static final double[] NOTIFICATION_TYPE_DISTRIBUTION = {0.3, 0.2, 0.15, 0.1, 0.1, 0.15};
    
    // Notification status distribution (UNREAD, READ, DISMISSED)
    private static final double[] NOTIFICATION_STATUS_DISTRIBUTION = {0.6, 0.3, 0.1}; // PENDING, DELIVERED, FAILED
    
    // Notification priority distribution (LOW, MEDIUM, HIGH, URGENT)
    private static final double[] NOTIFICATION_PRIORITY_DISTRIBUTION = {0.4, 0.4, 0.15, 0.05};
    
    // Notification delivery method distribution (EMAIL, IN_APP, SMS, PUSH)
    private static final double[] NOTIFICATION_DELIVERY_METHOD_DISTRIBUTION = {0.4, 0.4, 0.1, 0.1};
    
    private final Connection connection;
    private final NotificationDao notificationDao;
    private final int numNotifications;
    private final List<Integer> companyIds;
    private final List<Integer> userIds;
    
    /**
     * Constructor for NotificationSeeder.
     * 
     * @param connection Database connection
     * @param numNotifications Number of notifications to seed
     * @param companyIds List of company IDs to associate notifications with
     * @param userIds List of user IDs to associate notifications with
     */
    public NotificationSeeder(Connection connection, int numNotifications, List<Integer> companyIds, List<Integer> userIds) {
        this.connection = connection;
        this.notificationDao = new NotificationDaoImpl();
        this.companyIds = companyIds;
        this.numNotifications = numNotifications;
        this.userIds = userIds;
    }
    
    /**
     * Seed the notifications table with test data.
     * 
     * @return List of generated notification IDs
     * @throws SQLException If a database error occurs
     */
    public List<Integer> seed() throws SQLException {
        LOGGER.info("Seeding notifications table with " + numNotifications + " records...");

        if (userIds.isEmpty()) {
            LOGGER.warning("No user IDs provided. Cannot seed notifications.");
            return List.of();
        }
        
        List<Integer> notificationIds = new ArrayList<>();
        
        try {
            // Normalize the distribution weights
            double[] notificationTypeWeights = new double[NotificationType.values().length];
            for (int i = 0; i < Math.min(notificationTypeWeights.length, NOTIFICATION_TYPE_DISTRIBUTION.length); i++) {
                notificationTypeWeights[i] = NOTIFICATION_TYPE_DISTRIBUTION[i];
            }
            
            double[] notificationStatusWeights = EnumUtil.createNormalizedWeights(NotificationStatus.class, NOTIFICATION_STATUS_DISTRIBUTION);
            double[] notificationPriorityWeights = EnumUtil.createNormalizedWeights(NotificationPriority.class, NOTIFICATION_PRIORITY_DISTRIBUTION);
            double[] notificationDeliveryMethodWeights = EnumUtil.createNormalizedWeights(NotificationDeliveryMethod.class, NOTIFICATION_DELIVERY_METHOD_DISTRIBUTION);
            
            for (int i = 0; i < numNotifications; i++) {
                // Select a random user
                Integer userId = userIds.get(FakerUtil.generateRandomInt(0, userIds.size() - 1));
                // Select a random company
                Integer companyId = companyIds.isEmpty() ? null : 
                        companyIds.get(FakerUtil.generateRandomInt(0, companyIds.size() - 1));
                
                // Generate notification data
                NotificationType type = EnumUtil.getWeightedRandomEnumValue(NotificationType.class, notificationTypeWeights);
                String title = generateNotificationTitle(type);
                String message = generateNotificationContent(type);
                NotificationStatus status = EnumUtil.getWeightedRandomEnumValue(NotificationStatus.class, notificationStatusWeights);
                NotificationPriority priority = EnumUtil.getWeightedRandomEnumValue(NotificationPriority.class, notificationPriorityWeights);
                NotificationDeliveryMethod deliveryMethod = EnumUtil.getWeightedRandomEnumValue(NotificationDeliveryMethod.class, notificationDeliveryMethodWeights);
                
                // Generate timestamps
                Timestamp[] timestamps = TimestampUtil.getCreatedUpdatedTimestamps(30, 7, 1440);
                Timestamp createdAt = timestamps[0];
                Timestamp updatedAt = timestamps[1];
                
                // Generate expiration timestamp (30 days after creation)
                Timestamp expiresAt = new Timestamp(createdAt.getTime() + (30 * 24 * 60 * 60 * 1000));
                
                // Generate delivered timestamp for DELIVERED notifications
                Timestamp deliveredAt = null;
                if (status == NotificationStatus.DELIVERED) {
                    deliveredAt = new Timestamp(createdAt.getTime() + FakerUtil.generateRandomInt(1, 60) * 60 * 1000); // 1-60 minutes after creation
                }
                
                // Generate read timestamp for some delivered notifications
                Timestamp readAt = null;
                if (status == NotificationStatus.DELIVERED && FakerUtil.generateRandomInt(0, 1) == 1) {
                    readAt = new Timestamp(deliveredAt.getTime() + FakerUtil.generateRandomInt(1, 24 * 60) * 60 * 1000); // 1 minute to 24 hours after delivery
                }
                
                // Create the notification object
                Notification notification = new Notification();
                notification.setUserId(userId);
                notification.setCompanyId(companyId);
                notification.setNotificationType(type);
                notification.setTitle(title);
                notification.setMessage(message);
                notification.setStatus(status);
                notification.setPriority(priority);
                notification.setDeliveryMethod(deliveryMethod);
                notification.setExpiresAt(expiresAt);
                notification.setDeliveredAt(deliveredAt);
                notification.setReadAt(readAt);
                notification.setCreatedAt(createdAt);
                notification.setUpdatedAt(updatedAt);
                
                // Save the notification
                Notification createdNotification = notificationDao.create(notification);
                if (createdNotification != null && createdNotification.getId() > 0) {
                    notificationIds.add(createdNotification.getId());
                }
            }
            
            LOGGER.info("Successfully seeded " + notificationIds.size() + " notifications.");
            return notificationIds;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error seeding notifications table", e);
            throw e;
        }
    }
    
    /**
     * Generate a notification title based on the notification type.
     * 
     * @param type The notification type
     * @return A notification title
     */
    private String generateNotificationTitle(NotificationType type) {
        switch (type) {
            case GENERAL:
                return "System Notification: " + FakerUtil.getFaker().company().catchPhrase();
                
            case SECURITY_ALERT:
                String[] securityTitles = {
                    "Security Alert: Unusual Login Detected",
                    "Security Update: Password Change Required",
                    "Security Notice: Account Access Attempt",
                    "Security Warning: Suspicious Activity",
                    "Security Information: New Device Login"
                };
                return securityTitles[FakerUtil.generateRandomInt(0, securityTitles.length - 1)];
                
            case FAILED_LOGIN_NOTIFICATION:
                String[] loginTitles = {
                    "Failed Login Attempt Detected",
                    "Security Alert: Login Failure",
                    "Unsuccessful Login Attempt",
                    "Account Security: Failed Login",
                    "Login Attempt from Unknown Device"
                };
                return loginTitles[FakerUtil.generateRandomInt(0, loginTitles.length - 1)];
                
            case SYSTEM_MAINTENANCE:
                String[] maintenanceTitles = {
                    "Scheduled Maintenance: System Downtime",
                    "Maintenance Notice: Performance Improvements",
                    "Maintenance Alert: Database Upgrades",
                    "Maintenance Information: Server Updates",
                    "Maintenance Reminder: Upcoming Changes"
                };
                return maintenanceTitles[FakerUtil.generateRandomInt(0, maintenanceTitles.length - 1)];
                
            case INVITATION:
                String[] invitationTitles = {
                    "You've Been Invited",
                    "New System Access Invitation",
                    "Invitation to Join",
                    "Access Invitation from Administrator",
                    "System Invitation Awaiting Response"
                };
                return invitationTitles[FakerUtil.generateRandomInt(0, invitationTitles.length - 1)];
                
            case BILLING_EVENT:
                return "Billing Update: " + FakerUtil.getFaker().commerce().productName();
                
            default:
                return "Notification: " + FakerUtil.getFaker().lorem().sentence();
        }
    }
    
    /**
     * Generate notification content based on the notification type.
     * 
     * @param type The notification type
     * @return Notification content
     */
    private String generateNotificationContent(NotificationType type) {
        switch (type) {
            case GENERAL:
                return "Important system information: " + FakerUtil.getFaker().lorem().paragraph(2);
                
            case SECURITY_ALERT:
                return "Security information for your account: " + FakerUtil.getFaker().lorem().paragraph(2);
                
            case FAILED_LOGIN_NOTIFICATION:
                return "We detected a failed login attempt on your account from IP " + FakerUtil.generateIpAddress() + ". " + FakerUtil.getFaker().lorem().paragraph(1);
                
            case SYSTEM_MAINTENANCE:
                return "Upcoming maintenance information: " + FakerUtil.getFaker().lorem().paragraph(2);
                
            case INVITATION:
                return "You've been invited to join our platform. " + FakerUtil.getFaker().lorem().paragraph(2);
                
            case BILLING_EVENT:
                return "Your billing information has been updated: " + FakerUtil.getFaker().lorem().paragraph(2);
                
            default:
                return FakerUtil.getFaker().lorem().paragraph(2);
        }
    }
}