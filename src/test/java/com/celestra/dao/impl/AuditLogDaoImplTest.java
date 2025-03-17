package com.celestra.dao.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.celestra.dao.AuditLogDao;
import com.celestra.dao.BaseDaoTest;
import com.celestra.enums.AuditEventType;
import com.celestra.model.AuditLog;

/**
 * Test class for AuditLogDaoImpl.
 */
public class AuditLogDaoImplTest extends BaseDaoTest {
    
    private AuditLogDao auditLogDao;
    
    /**
     * Main method to run the tests.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        AuditLogDaoImplTest test = new AuditLogDaoImplTest();
        test.runTests();
    }
    
    /**
     * Constructor.
     */
    public AuditLogDaoImplTest() {
        auditLogDao = new AuditLogDaoImpl();
    }
    
    /**
     * Run all tests.
     */
    public void runTests() {
        try {
            setUp();
            
            testCreate();
            testFindById();
            testFindAll();
            testUpdate();
            testDelete();
            testFindByUserId();
            testFindByEventType();
            testFindByTableName();
            testFindByRecordId();
            testFindByGroupId();
            testFindByDateRange();
            testFindByUserIdAndEventType();
            testFindByTableNameAndRecordId();
            
            tearDown();
            
            System.out.println("All tests completed.");
        } catch (Exception e) {
            System.err.println("Error running tests: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // Audit logs table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // Generate a group ID for testing
        String groupId = UUID.randomUUID().toString();
        
        // Insert test data
        executeSQL("INSERT INTO audit_logs (user_id, event_type, event_description, ip_address, " +
                   "table_name, record_id, group_id, created_at) " +
                   "VALUES (1, 'SUCCESSFUL_LOGIN', 'User login', '127.0.0.1', 'users', '1', '" + groupId + "', NOW())");
        
        executeSQL("INSERT INTO audit_logs (user_id, event_type, event_description, ip_address, " +
                   "table_name, record_id, group_id, created_at) " +
                   "VALUES (1, 'CONFIGURATION_UPDATE', 'User profile updated', '127.0.0.1', 'users', '1', '" + groupId + "', NOW())");
        
        executeSQL("INSERT INTO audit_logs (user_id, event_type, event_description, ip_address, " +
                   "table_name, record_id, group_id, created_at) " +
                   "VALUES (2, 'OTHER', 'Knowledge base created', '192.168.1.1', 'knowledge_bases', '1', '" + 
                   UUID.randomUUID().toString() + "', NOW())");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM audit_logs WHERE event_description LIKE 'User login' OR " +
                   "event_description LIKE 'User profile updated' OR " +
                   "event_description LIKE 'Knowledge base created' OR " +
                   "event_description LIKE 'Test%'");
    }
    
    /**
     * Test the create method.
     */
    private void testCreate() {
        try {
            // Create a new audit log
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(1);
            auditLog.setEventType(AuditEventType.OTHER);
            auditLog.setEventDescription("Test audit log");
            auditLog.setIpAddress("127.0.0.1");
            auditLog.setTableName("test_table");
            auditLog.setRecordId("123");
            auditLog.setGroupId(UUID.randomUUID());
            
            AuditLog createdAuditLog = auditLogDao.create(auditLog);
            
            // Verify the audit log was created
            boolean success = createdAuditLog.getId() > 0;
            printTestResult("testCreate", success);
            
            // Clean up
            if (success) {
                auditLogDao.delete(createdAuditLog.getId());
            }
        } catch (Exception e) {
            printTestFailure("testCreate", e);
        }
    }
    
    /**
     * Test the findById method.
     */
    private void testFindById() {
        try {
            // Find all audit logs
            List<AuditLog> auditLogs = auditLogDao.findAll();
            
            // Verify there are audit logs
            if (auditLogs.isEmpty()) {
                printTestResult("testFindById", false, "No audit logs found");
                return;
            }
            
            // Get the first audit log
            AuditLog auditLog = auditLogs.get(0);
            
            // Find the audit log by ID
            Optional<AuditLog> foundAuditLog = auditLogDao.findById(auditLog.getId());
            
            // Verify the audit log was found
            boolean success = foundAuditLog.isPresent() && 
                              foundAuditLog.get().getId().equals(auditLog.getId()) &&
                              foundAuditLog.get().getEventDescription().equals(auditLog.getEventDescription());
            
            printTestResult("testFindById", success);
        } catch (Exception e) {
            printTestFailure("testFindById", e);
        }
    }
    
    /**
     * Test the findAll method.
     */
    private void testFindAll() {
        try {
            // Find all audit logs
            List<AuditLog> auditLogs = auditLogDao.findAll();
            
            // Verify there are audit logs
            boolean success = !auditLogs.isEmpty();
            printTestResult("testFindAll", success, "Found " + auditLogs.size() + " audit logs");
        } catch (Exception e) {
            printTestFailure("testFindAll", e);
        }
    }
    
    /**
     * Test the update method.
     */
    private void testUpdate() {
        try {
            // Create a new audit log
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(1);
            auditLog.setEventType(AuditEventType.OTHER);
            auditLog.setEventDescription("Test audit log update");
            auditLog.setIpAddress("127.0.0.1");
            auditLog.setTableName("test_table");
            auditLog.setRecordId("123");
            auditLog.setGroupId(UUID.randomUUID());
            
            AuditLog createdAuditLog = auditLogDao.create(auditLog);
            
            // Update the audit log
            createdAuditLog.setEventDescription("Test audit log updated");
            createdAuditLog.setIpAddress("192.168.1.1");
            
            AuditLog updatedAuditLog = auditLogDao.update(createdAuditLog);
            
            // Verify the audit log was updated
            boolean success = updatedAuditLog.getEventDescription().equals("Test audit log updated") &&
                              updatedAuditLog.getIpAddress().equals("192.168.1.1");
            
            printTestResult("testUpdate", success);
            
            // Clean up
            auditLogDao.delete(createdAuditLog.getId());
        } catch (Exception e) {
            printTestFailure("testUpdate", e);
        }
    }
    
    /**
     * Test the delete method.
     */
    private void testDelete() {
        try {
            // Create a new audit log
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(1);
            auditLog.setEventType(AuditEventType.OTHER);
            auditLog.setEventDescription("Test audit log delete");
            auditLog.setIpAddress("127.0.0.1");
            auditLog.setTableName("test_table");
            auditLog.setRecordId("123");
            auditLog.setGroupId(UUID.randomUUID());
            
            AuditLog createdAuditLog = auditLogDao.create(auditLog);
            
            // Delete the audit log
            boolean deleted = auditLogDao.delete(createdAuditLog.getId());
            
            // Verify the audit log was deleted
            boolean success = deleted && !auditLogDao.findById(createdAuditLog.getId()).isPresent();
            
            printTestResult("testDelete", success);
        } catch (Exception e) {
            printTestFailure("testDelete", e);
        }
    }
    
    /**
     * Test the findByUserId method.
     */
    private void testFindByUserId() {
        try {
            // Find audit logs by user ID
            List<AuditLog> auditLogs = auditLogDao.findByUserId(1);
            
            // Verify there are audit logs
            boolean success = !auditLogs.isEmpty();
            printTestResult("testFindByUserId", success, "Found " + auditLogs.size() + " audit logs for user ID 1");
        } catch (Exception e) {
            printTestFailure("testFindByUserId", e);
        }
    }
    
    /**
     * Test the findByEventType method.
     */
    private void testFindByEventType() {
        try {
            // Find audit logs by event type
            List<AuditLog> auditLogs = auditLogDao.findByEventType(AuditEventType.SUCCESSFUL_LOGIN);
            
            // Verify there are audit logs
            boolean success = !auditLogs.isEmpty();
            printTestResult("testFindByEventType", success, 
                    "Found " + auditLogs.size() + " audit logs with event type SUCCESSFUL_LOGIN");
        } catch (Exception e) {
            printTestFailure("testFindByEventType", e);
        }
    }
    
    /**
     * Test the findByTableName method.
     */
    private void testFindByTableName() {
        try {
            // Find audit logs by table name
            List<AuditLog> auditLogs = auditLogDao.findByTableName("users");
            
            // Verify there are audit logs
            boolean success = !auditLogs.isEmpty();
            printTestResult("testFindByTableName", success, 
                    "Found " + auditLogs.size() + " audit logs for table 'users'");
        } catch (Exception e) {
            printTestFailure("testFindByTableName", e);
        }
    }
    
    /**
     * Test the findByRecordId method.
     */
    private void testFindByRecordId() {
        try {
            // Find audit logs by record ID
            List<AuditLog> auditLogs = auditLogDao.findByRecordId("1");
            
            // Verify there are audit logs
            boolean success = !auditLogs.isEmpty();
            printTestResult("testFindByRecordId", success, 
                    "Found " + auditLogs.size() + " audit logs for record ID '1'");
        } catch (Exception e) {
            printTestFailure("testFindByRecordId", e);
        }
    }
    
    /**
     * Test the findByGroupId method.
     */
    private void testFindByGroupId() {
        try {
            // Find all audit logs
            List<AuditLog> allAuditLogs = auditLogDao.findAll();
            
            // Verify there are audit logs
            if (allAuditLogs.isEmpty()) {
                printTestResult("testFindByGroupId", false, "No audit logs found");
                return;
            }
            
            // Get the first audit log with a group ID
            Optional<AuditLog> auditLogWithGroupId = allAuditLogs.stream()
                    .filter(log -> log.getGroupId() != null)
                    .findFirst();
            
            if (!auditLogWithGroupId.isPresent()) {
                printTestResult("testFindByGroupId", false, "No audit logs with group ID found");
                return;
            }
            
            // Find audit logs by group ID
            List<AuditLog> auditLogs = auditLogDao.findByGroupId(auditLogWithGroupId.get().getGroupId());
            
            // Verify there are audit logs
            boolean success = !auditLogs.isEmpty();
            printTestResult("testFindByGroupId", success, 
                    "Found " + auditLogs.size() + " audit logs for group ID '" + 
                    auditLogWithGroupId.get().getGroupId() + "'");
        } catch (Exception e) {
            printTestFailure("testFindByGroupId", e);
        }
    }
    
    /**
     * Test the findByDateRange method.
     */
    private void testFindByDateRange() {
        try {
            // Find audit logs by date range (today)
            String today = java.time.LocalDate.now().toString();
            List<AuditLog> auditLogs = auditLogDao.findByDateRange(today, today);
            
            // Verify there are audit logs
            boolean success = !auditLogs.isEmpty();
            printTestResult("testFindByDateRange", success, 
                    "Found " + auditLogs.size() + " audit logs for date range '" + today + "' to '" + today + "'");
        } catch (Exception e) {
            printTestFailure("testFindByDateRange", e);
        }
    }
    
    /**
     * Test the findByUserIdAndEventType method.
     */
    private void testFindByUserIdAndEventType() {
        try {
            // Find audit logs by user ID and event type
            List<AuditLog> auditLogs = auditLogDao.findByUserIdAndEventType(1, AuditEventType.SUCCESSFUL_LOGIN);
            
            // Verify there are audit logs
            boolean success = !auditLogs.isEmpty();
            printTestResult("testFindByUserIdAndEventType", success, 
                    "Found " + auditLogs.size() + " audit logs for user ID 1 and event type SUCCESSFUL_LOGIN");
        } catch (Exception e) {
            printTestFailure("testFindByUserIdAndEventType", e);
        }
    }
    
    /**
     * Test the findByTableNameAndRecordId method.
     */
    private void testFindByTableNameAndRecordId() {
        try {
            // Find audit logs by table name and record ID
            List<AuditLog> auditLogs = auditLogDao.findByTableNameAndRecordId("users", "1");
            
            // Verify there are audit logs
            boolean success = !auditLogs.isEmpty();
            printTestResult("testFindByTableNameAndRecordId", success, 
                    "Found " + auditLogs.size() + " audit logs for table 'users' and record ID '1'");
        } catch (Exception e) {
            printTestFailure("testFindByTableNameAndRecordId", e);
        }
    }
}