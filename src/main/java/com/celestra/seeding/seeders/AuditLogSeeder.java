package com.celestra.seeding.seeders;

import com.celestra.dao.AuditLogDao;
import com.celestra.dao.impl.AuditLogDaoImpl;
import com.celestra.model.AuditLog;
import com.celestra.enums.AuditEventType;
import com.celestra.seeding.util.EnumUtil;
import com.celestra.seeding.util.FakerUtil;
import com.celestra.seeding.util.TimestampUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Seeder class for the audit_logs table.
 * This class is responsible for generating and inserting test data for audit logs.
 * It uses the AuditLogDao to interact with the database.
 */
public class AuditLogSeeder {
    
    private static final Logger LOGGER = Logger.getLogger(AuditLogSeeder.class.getName());
    
    // Audit event type distribution
    private static final Map<AuditEventType, Double> EVENT_TYPE_DISTRIBUTION = new HashMap<>();
    static {
        EVENT_TYPE_DISTRIBUTION.put(AuditEventType.FAILED_LOGIN, 0.15);
        EVENT_TYPE_DISTRIBUTION.put(AuditEventType.SUCCESSFUL_LOGIN, 0.25);
        EVENT_TYPE_DISTRIBUTION.put(AuditEventType.SESSION_STARTED, 0.20);
        EVENT_TYPE_DISTRIBUTION.put(AuditEventType.SESSION_ENDED, 0.20);
        EVENT_TYPE_DISTRIBUTION.put(AuditEventType.ROLE_ASSIGNMENT_CHANGE, 0.05);
        EVENT_TYPE_DISTRIBUTION.put(AuditEventType.CONFIGURATION_UPDATE, 0.05);
        EVENT_TYPE_DISTRIBUTION.put(AuditEventType.DATA_EXPORT, 0.05);
        EVENT_TYPE_DISTRIBUTION.put(AuditEventType.OTHER, 0.05);
    }
    
    private final Connection connection;
    private final AuditLogDao auditLogDao;
    private final int numAuditLogs;
    private final List<Integer> userIds;
    private final List<Integer> companyIds;
    
    /**
     * Constructor for AuditLogSeeder.
     * 
     * @param connection Database connection
     * @param numAuditLogs Number of audit logs to seed
     * @param userIds List of user IDs to associate audit logs with
     * @param companyIds List of company IDs to associate audit logs with
     */
    public AuditLogSeeder(Connection connection, int numAuditLogs, List<Integer> userIds, List<Integer> companyIds) {
        this.connection = connection;
        this.auditLogDao = new AuditLogDaoImpl();
        this.numAuditLogs = numAuditLogs;
        this.userIds = userIds;
        this.companyIds = companyIds;
    }
    
    /**
     * Seed the audit_logs table with test data.
     * 
     * @return List of generated audit log IDs
     * @throws SQLException If a database error occurs
     */
    public List<Integer> seed() throws SQLException {
        LOGGER.info("Seeding audit_logs table with " + numAuditLogs + " records...");
        
        if (userIds.isEmpty() || companyIds.isEmpty()) {
            LOGGER.warning("No user IDs or company IDs provided. Cannot seed audit logs.");
            return List.of();
        }
        
        List<Integer> auditLogIds = new ArrayList<>();
        
        try {
            // Convert the distribution map to arrays for EnumUtil
            AuditEventType[] eventTypes = EVENT_TYPE_DISTRIBUTION.keySet().toArray(new AuditEventType[0]);
            double[] weights = EVENT_TYPE_DISTRIBUTION.values().stream().mapToDouble(Double::doubleValue).toArray();
            
            // Normalize the weights
            double sum = 0;
            for (double weight : weights) {
                sum += weight;
            }
            for (int i = 0; i < weights.length; i++) {
                weights[i] /= sum;
            }
            
            for (int i = 0; i < numAuditLogs; i++) {
                // Select a random user
                Integer userId = userIds.get(FakerUtil.generateRandomInt(0, userIds.size() - 1));
                
                // Select a random company
                Integer companyId = companyIds.get(FakerUtil.generateRandomInt(0, companyIds.size() - 1));
                
                // Select a random event type based on the distribution
                AuditEventType eventType = eventTypes[FakerUtil.generateWeightedRandomIndex(weights)];
                
                // Generate IP address and user agent
                String ipAddress = FakerUtil.generateIpAddress();
                String userAgent = FakerUtil.generateUserAgent();
                
                // Generate details based on event type
                String details = generateDetailsForEventType(eventType, userId, companyId);
                
                // Generate timestamp
                Timestamp timestamp = TimestampUtil.getRandomTimestampInRange(-365, -1);
                
                // Create the audit log object
                AuditLog auditLog = new AuditLog();
                auditLog.setUserId(userId);
                auditLog.setEventType(eventType);
                auditLog.setEventDescription(details);
                auditLog.setIpAddress(ipAddress);
                
                // Set table name and record ID for database operations
                if (eventType == AuditEventType.CONFIGURATION_UPDATE || eventType == AuditEventType.OTHER) {
                    auditLog.setTableName(getRandomTableName());
                    auditLog.setRecordId(String.valueOf(FakerUtil.generateRandomInt(1, 1000)));
                }
                auditLog.setCreatedAt(timestamp);
                
                // Save the audit log
                AuditLog createdLog = auditLogDao.create(auditLog);
                if (createdLog != null && createdLog.getId() > 0) {
                    auditLogIds.add(createdLog.getId());
                }
            }
            
            LOGGER.info("Successfully seeded " + auditLogIds.size() + " audit logs.");
            return auditLogIds;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error seeding audit_logs table", e);
            throw e;
        }
    }
    
    /**
     * Generate details for a specific event type.
     * 
     * @param eventType The event type
     * @param userId The user ID
     * @param companyId The company ID
     * @return Details for the event
     */
    private String generateDetailsForEventType(AuditEventType eventType, Integer userId, Integer companyId) {
        switch (eventType) {
            case FAILED_LOGIN:
                return "Failed login attempt for user " + userId + " from IP " + FakerUtil.generateIpAddress();
                
            case SUCCESSFUL_LOGIN:
                return "User " + userId + " successfully authenticated";
                
            case SESSION_STARTED:
                return "New session started for user " + userId + " in company " + companyId;
                
            case SESSION_ENDED:
                return "Session terminated for user " + userId + " after " + 
                        FakerUtil.generateRandomInt(5, 120) + " minutes";
                
            case ROLE_ASSIGNMENT_CHANGE:
                String[] roles = {"SUPER_ADMIN", "COMPANY_ADMIN", "SPACE_ADMIN", "REGULAR_USER"};
                String oldRole = roles[FakerUtil.generateRandomInt(0, roles.length - 1)];
                String newRole = roles[FakerUtil.generateRandomInt(0, roles.length - 1)];
                while (oldRole.equals(newRole)) {
                    newRole = roles[FakerUtil.generateRandomInt(0, roles.length - 1)];
                }
                return "User " + userId + " role changed from " + oldRole + " to " + newRole;
                
            case CONFIGURATION_UPDATE:
                String[] configTypes = {"System Settings", "Security Policy", "Email Templates", "API Configuration"};
                String configType = configTypes[FakerUtil.generateRandomInt(0, configTypes.length - 1)];
                return configType + " updated by user " + userId;
                
            case DATA_EXPORT:
                String[] exportTypes = {"User Data", "Company Data", "Agent Data", "Knowledge Base Data"};
                String exportType = exportTypes[FakerUtil.generateRandomInt(0, exportTypes.length - 1)];
                return exportType + " exported by user " + userId + " for company " + companyId;
                
            case OTHER:
                String[] actions = {"viewed", "accessed", "requested", "modified", "approved", "rejected"};
                String action = actions[FakerUtil.generateRandomInt(0, actions.length - 1)];
                String[] resources = {"dashboard", "report", "profile", "settings", "document", "knowledge base"};
                String resource = resources[FakerUtil.generateRandomInt(0, resources.length - 1)];
                return "User " + userId + " " + action + " " + resource;
                
            default:
                return "Unknown event occurred";
        }
    }
    
    /**
     * Get a random table name for database operations.
     * 
     * @return A random table name
     */
    private String getRandomTableName() {
        String[] tables = {
            "users", "companies", "agents", "knowledge_bases", "knowledge_sources", 
            "knowledge_types", "agent_knowledge_bases", "notifications", "invitations"
        };
        return tables[FakerUtil.generateRandomInt(0, tables.length - 1)];
    }
}