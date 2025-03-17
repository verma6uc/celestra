package com.celestra.seeding.seeders;

import com.celestra.dao.AuditChangeLogDao;
import com.celestra.dao.impl.AuditChangeLogDaoImpl;
import com.celestra.model.AuditChangeLog;
import com.celestra.seeding.util.FakerUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Seeder class for the audit_change_logs table.
 * This class is responsible for generating and inserting test data for audit change logs.
 * It uses the AuditChangeLogDao to interact with the database.
 */
public class AuditChangeLogSeeder {
    
    private static final Logger LOGGER = Logger.getLogger(AuditChangeLogSeeder.class.getName());
    
    private final Connection connection;
    private final AuditChangeLogDao auditChangeLogDao;
    private final int numAuditChangeLogs;
    private final List<Integer> auditLogIds;
    
    /**
     * Constructor for AuditChangeLogSeeder.
     * 
     * @param connection Database connection
     * @param numAuditChangeLogs Number of audit change logs to seed
     * @param auditLogIds List of audit log IDs to associate change logs with
     */
    public AuditChangeLogSeeder(Connection connection, int numAuditChangeLogs, List<Integer> auditLogIds) {
        this.connection = connection;
        this.auditChangeLogDao = new AuditChangeLogDaoImpl();
        this.numAuditChangeLogs = numAuditChangeLogs;
        this.auditLogIds = auditLogIds;
    }
    
    /**
     * Seed the audit_change_logs table with test data.
     * 
     * @return List of generated audit change log IDs
     * @throws SQLException If a database error occurs
     */
    public List<Integer> seed() throws SQLException {
        LOGGER.info("Seeding audit_change_logs table with " + numAuditChangeLogs + " records...");
        
        if (auditLogIds.isEmpty()) {
            LOGGER.warning("No audit log IDs provided. Cannot seed audit change logs.");
            return List.of();
        }
        
        List<Integer> auditChangeLogIds = new ArrayList<>();
        
        try {
            // Distribute change logs across audit logs
            // Some audit logs will have multiple changes, others none
            int auditLogsWithChanges = Math.min(auditLogIds.size(), numAuditChangeLogs);
            int[] changesPerAuditLog = new int[auditLogsWithChanges];
            
            // Ensure each audit log with changes has at least one change
            for (int i = 0; i < auditLogsWithChanges; i++) {
                changesPerAuditLog[i] = 1;
            }
            
            // Distribute remaining changes
            int remainingChanges = numAuditChangeLogs - auditLogsWithChanges;
            for (int i = 0; i < remainingChanges; i++) {
                // Randomly select an audit log to add another change
                int auditLogIndex = FakerUtil.generateRandomInt(0, auditLogsWithChanges - 1);
                changesPerAuditLog[auditLogIndex]++;
            }
            
            // Create change logs for each audit log
            for (int i = 0; i < auditLogsWithChanges; i++) {
                Integer auditLogId = auditLogIds.get(i);
                int numChanges = changesPerAuditLog[i];
                
                // Generate changes for this audit log
                for (int j = 0; j < numChanges; j++) {
                    // Generate change data
                    String fieldName = generateFieldName();
                    String oldValue = generateFieldValue(fieldName);
                    String newValue = generateFieldValue(fieldName);
                    
                    // Create the audit change log object
                    AuditChangeLog auditChangeLog = new AuditChangeLog();
                    auditChangeLog.setAuditLogId(auditLogId);
                    auditChangeLog.setColumnName(fieldName);
                    auditChangeLog.setOldValue(oldValue);
                    auditChangeLog.setNewValue(newValue);
                    auditChangeLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                    
                    // Save the audit change log
                    AuditChangeLog createdAuditChangeLog = auditChangeLogDao.create(auditChangeLog);
                    if (createdAuditChangeLog != null && createdAuditChangeLog.getId() > 0) {
                        auditChangeLogIds.add(createdAuditChangeLog.getId());
                    }
                }
            }
            
            LOGGER.info("Successfully seeded " + auditChangeLogIds.size() + " audit change logs.");
            return auditChangeLogIds;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error seeding audit_change_logs table", e);
            throw e;
        }
    }
    
    /**
     * Generate a random field name for an audit change log.
     * 
     * @return A random field name
     */
    private String generateFieldName() {
        String[] fieldNames = {
            "name", "email", "status", "role", "company_id", "description", 
            "title", "content", "priority", "expires_at", "created_at", 
            "updated_at", "deleted_at", "is_active", "is_deleted", "is_verified",
            "password_hash", "last_login_at", "failed_attempts", "lockout_end",
            "session_token", "ip_address", "user_agent", "device_type"
        };
        
        return fieldNames[FakerUtil.generateRandomInt(0, fieldNames.length - 1)];
    }
    
    /**
     * Generate a random field value based on the field name.
     * 
     * @param fieldName The name of the field
     * @return A random value for the field
     */
    private String generateFieldValue(String fieldName) {
        switch (fieldName) {
            case "name":
                return FakerUtil.generatePersonName();
                
            case "email":
                return FakerUtil.generateEmail();
                
            case "status":
                String[] statuses = {"ACTIVE", "INACTIVE", "PENDING", "SUSPENDED", "DELETED"};
                return statuses[FakerUtil.generateRandomInt(0, statuses.length - 1)];
                
            case "role":
                String[] roles = {"ADMIN", "USER", "MANAGER", "GUEST", "SUPER_ADMIN"};
                return roles[FakerUtil.generateRandomInt(0, roles.length - 1)];
                
            case "company_id":
                return String.valueOf(FakerUtil.generateRandomInt(1, 100));
                
            case "description":
                return FakerUtil.generateSentence();
                
            case "title":
                return FakerUtil.getFaker().book().title();
                
            case "content":
                return FakerUtil.generateParagraph();
                
            case "priority":
                String[] priorities = {"LOW", "MEDIUM", "HIGH", "URGENT", "CRITICAL"};
                return priorities[FakerUtil.generateRandomInt(0, priorities.length - 1)];
                
            case "expires_at":
            case "created_at":
            case "updated_at":
            case "deleted_at":
            case "last_login_at":
            case "lockout_end":
                return new Timestamp(System.currentTimeMillis() + 
                        FakerUtil.generateRandomInt(-30, 30) * 24 * 60 * 60 * 1000).toString();
                
            case "is_active":
            case "is_deleted":
            case "is_verified":
                return String.valueOf(FakerUtil.generateRandomBoolean(0.5));
                
            case "password_hash":
                return FakerUtil.generatePasswordHash();
                
            case "failed_attempts":
                return String.valueOf(FakerUtil.generateRandomInt(0, 10));
                
            case "session_token":
                return FakerUtil.generateUuid();
                
            case "ip_address":
                return FakerUtil.generateIpAddress();
                
            case "user_agent":
                return FakerUtil.generateUserAgent();
                
            case "device_type":
                String[] deviceTypes = {"DESKTOP", "MOBILE", "TABLET", "OTHER"};
                return deviceTypes[FakerUtil.generateRandomInt(0, deviceTypes.length - 1)];
                
            default:
                return FakerUtil.generateSentence();
        }
    }
}