package com.celestra.dao.impl;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.celestra.dao.AuditLogDao;
import com.celestra.dao.BaseDaoTest;
import com.celestra.enums.AuditEventType;
import com.celestra.model.AuditLog;
import com.celestra.db.DatabaseUtil;

/**
 * Test class for AuditLogDaoImpl.
 */
public class AuditLogDaoImplTest extends BaseDaoTest {
    
    private AuditLogDao auditLogDao;
    
    /**
     * Initialize the DAO before each test.
     */
    @Before
    public void initialize() {
        auditLogDao = new AuditLogDaoImpl();
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // Audit logs table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // Insert test companies first (to satisfy foreign key constraints)
        executeSQL("INSERT INTO companies (name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES ('Test Company 1', 'Test Company Description 1', 'SMALL'::company_size, 'TECH'::company_vertical, 'ACTIVE'::company_status, NOW(), NOW()) RETURNING id");
        
        // Insert test users (to satisfy foreign key constraints)
        executeSQL("INSERT INTO users (company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES ((SELECT id FROM companies WHERE name = 'Test Company 1'), 'COMPANY_ADMIN'::user_role, 'admin@test.com', 'Admin User', 'hash123', 'ACTIVE'::user_status, NOW(), NOW()) RETURNING id");
        
        executeSQL("INSERT INTO users (company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES ((SELECT id FROM companies WHERE name = 'Test Company 1'), 'REGULAR_USER'::user_role, 'user@test.com', 'Regular User', 'hash456', 'ACTIVE'::user_status, NOW(), NOW()) RETURNING id");
        
        // Generate a group ID for testing
        String groupId = UUID.randomUUID().toString();
        
        // Insert test audit logs
        executeSQL("INSERT INTO audit_logs (user_id, event_type, event_description, ip_address, " +
                   "table_name, record_id, group_id, created_at) " +
                   "VALUES ((SELECT id FROM users WHERE email = 'admin@test.com'), 'SUCCESSFUL_LOGIN'::audit_event_type, 'User login', '127.0.0.1', 'users', '1', '" + groupId + "', NOW())");
        
        executeSQL("INSERT INTO audit_logs (user_id, event_type, event_description, ip_address, " +
                   "table_name, record_id, group_id, created_at) " +
                   "VALUES ((SELECT id FROM users WHERE email = 'admin@test.com'), 'CONFIGURATION_UPDATE'::audit_event_type, 'User profile updated', '127.0.0.1', 'users', '1', '" + groupId + "', NOW())");
        
        executeSQL("INSERT INTO audit_logs (user_id, event_type, event_description, ip_address, " +
                   "table_name, record_id, group_id, created_at) " +
                   "VALUES ((SELECT id FROM users WHERE email = 'user@test.com'), 'OTHER'::audit_event_type, 'Knowledge base created', '192.168.1.1', 'knowledge_bases', '1', '" + 
                   UUID.randomUUID().toString() + "', NOW())");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM audit_logs WHERE event_description LIKE 'User login' OR " +
                   "event_description LIKE 'User profile updated' OR " +
                   "event_description LIKE 'Knowledge base created' OR " +
                   "event_description LIKE 'Test%'");
        executeSQL("DELETE FROM users WHERE email IN ('admin@test.com', 'user@test.com')");
        executeSQL("DELETE FROM companies WHERE name = 'Test Company 1'");
    }
    
    /**
     * Test the create method.
     */
    @Test
    public void testCreate() throws SQLException {
        // Create a new audit log
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(getUserId("admin@test.com"));
        auditLog.setEventType(AuditEventType.OTHER);
        auditLog.setEventDescription("Test audit log");
        auditLog.setIpAddress("127.0.0.1");
        auditLog.setTableName("test_table");
        auditLog.setRecordId("123");
        auditLog.setGroupId(UUID.randomUUID());
        
        AuditLog createdAuditLog = auditLogDao.create(auditLog);
        
        // Verify the audit log was created
        assertNotNull("Created audit log should not be null", createdAuditLog);
        assertTrue("Created audit log should have an ID", createdAuditLog.getId() > 0);
        
        // Clean up
        boolean deleted = auditLogDao.delete(createdAuditLog.getId());
        assertTrue("Audit log should be deleted successfully", deleted);
    }
    
    /**
     * Test the findById method.
     */
    @Test
    public void testFindById() throws SQLException {
        // Find all audit logs
        List<AuditLog> auditLogs = auditLogDao.findAll();
        
        // Verify there are audit logs
        assertFalse("There should be audit logs in the database", auditLogs.isEmpty());
        
        // Get the first audit log
        AuditLog auditLog = auditLogs.get(0);
        
        // Find the audit log by ID
        Optional<AuditLog> foundAuditLog = auditLogDao.findById(auditLog.getId());
        
        // Verify the audit log was found
        assertTrue("Audit log should be found by ID", foundAuditLog.isPresent());
        assertEquals("Found audit log ID should match", auditLog.getId(), foundAuditLog.get().getId());
        assertEquals("Found audit log description should match", auditLog.getEventDescription(), foundAuditLog.get().getEventDescription());
    }
    
    /**
     * Test the findAll method.
     */
    @Test
    public void testFindAll() throws SQLException {
        // Find all audit logs
        List<AuditLog> auditLogs = auditLogDao.findAll();
        
        // Verify there are audit logs
        assertFalse("There should be audit logs in the database", auditLogs.isEmpty());
    }
    
    /**
     * Test the update method.
     */
    @Test
    public void testUpdate() throws SQLException {
        // Create a new audit log
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(getUserId("admin@test.com"));
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
        assertEquals("Audit log description should be updated", "Test audit log updated", updatedAuditLog.getEventDescription());
        assertEquals("Audit log IP address should be updated", "192.168.1.1", updatedAuditLog.getIpAddress());
        
        // Clean up
        boolean deleted = auditLogDao.delete(createdAuditLog.getId());
        assertTrue("Audit log should be deleted successfully", deleted);
    }
    
    /**
     * Test the delete method.
     */
    @Test
    public void testDelete() throws SQLException {
        // Create a new audit log
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(getUserId("admin@test.com"));
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
        assertTrue("Audit log should be deleted successfully", deleted);
        
        Optional<AuditLog> foundAuditLog = auditLogDao.findById(createdAuditLog.getId());
        assertFalse("Audit log should not be found after deletion", foundAuditLog.isPresent());
    }
    
    /**
     * Test the findByUserId method.
     */
    @Test
    public void testFindByUserId() throws SQLException {
        // Find audit logs by user ID
        List<AuditLog> auditLogs = auditLogDao.findByUserId(getUserId("admin@test.com"));
        
        // Verify there are audit logs
        assertFalse("There should be audit logs for admin user", auditLogs.isEmpty());
        
        // Verify all entries have the correct user ID
        for (AuditLog auditLog : auditLogs) {
            assertEquals("Audit log user ID should match admin user", getUserId("admin@test.com"), auditLog.getUserId());
        }
    }
    
    /**
     * Test the findByEventType method.
     */
    @Test
    public void testFindByEventType() throws SQLException {
        // Find audit logs by event type
        List<AuditLog> auditLogs = auditLogDao.findByEventType(AuditEventType.SUCCESSFUL_LOGIN);
        
        // Verify there are audit logs
        assertFalse("There should be audit logs with event type SUCCESSFUL_LOGIN", auditLogs.isEmpty());
        
        // Verify all entries have the correct event type
        for (AuditLog auditLog : auditLogs) {
            assertEquals("Audit log event type should be SUCCESSFUL_LOGIN", AuditEventType.SUCCESSFUL_LOGIN, auditLog.getEventType());
        }
    }
    
    /**
     * Test the findByTableName method.
     */
    @Test
    public void testFindByTableName() throws SQLException {
        // Find audit logs by table name
        List<AuditLog> auditLogs = auditLogDao.findByTableName("users");
        
        // Verify there are audit logs
        assertFalse("There should be audit logs for table 'users'", auditLogs.isEmpty());
        
        // Verify all entries have the correct table name
        for (AuditLog auditLog : auditLogs) {
            assertEquals("Audit log table name should be 'users'", "users", auditLog.getTableName());
        }
    }
    
    /**
     * Test the findByRecordId method.
     */
    @Test
    public void testFindByRecordId() throws SQLException {
        // Find audit logs by record ID
        List<AuditLog> auditLogs = auditLogDao.findByRecordId("1");
        
        // Verify there are audit logs
        assertFalse("There should be audit logs for record ID '1'", auditLogs.isEmpty());
        
        // Verify all entries have the correct record ID
        for (AuditLog auditLog : auditLogs) {
            assertEquals("Audit log record ID should be '1'", "1", auditLog.getRecordId());
        }
    }
    
    /**
     * Test the findByGroupId method.
     */
    @Test
    public void testFindByGroupId() throws SQLException {
        // Find all audit logs
        List<AuditLog> allAuditLogs = auditLogDao.findAll();
        
        // Verify there are audit logs
        assertFalse("There should be audit logs in the database", allAuditLogs.isEmpty());
        
        // Get the first audit log with a group ID
        Optional<AuditLog> auditLogWithGroupId = allAuditLogs.stream()
                .filter(log -> log.getGroupId() != null)
                .findFirst();
        
        assertTrue("There should be at least one audit log with a group ID", auditLogWithGroupId.isPresent());
        
        // Find audit logs by group ID
        List<AuditLog> auditLogs = auditLogDao.findByGroupId(auditLogWithGroupId.get().getGroupId());
        
        // Verify there are audit logs
        assertFalse("There should be audit logs for the group ID", auditLogs.isEmpty());
        
        // Verify all entries have the correct group ID
        for (AuditLog auditLog : auditLogs) {
            assertEquals("Audit log group ID should match", auditLogWithGroupId.get().getGroupId(), auditLog.getGroupId());
        }
    }
    
    /**
     * Test the findByDateRange method.
     */
    @Test
    public void testFindByDateRange() throws SQLException {
        // Find audit logs by date range (today)
        String today = java.time.LocalDate.now().toString();
        List<AuditLog> auditLogs = auditLogDao.findByDateRange(today, today);
        
        // Verify there are audit logs
        assertFalse("There should be audit logs for today's date", auditLogs.isEmpty());
    }
    
    /**
     * Test the findByUserIdAndEventType method.
     */
    @Test
    public void testFindByUserIdAndEventType() throws SQLException {
        // Find audit logs by user ID and event type
        List<AuditLog> auditLogs = auditLogDao.findByUserIdAndEventType(getUserId("admin@test.com"), AuditEventType.SUCCESSFUL_LOGIN);
        
        // Verify there are audit logs
        assertFalse("There should be audit logs for admin user and event type SUCCESSFUL_LOGIN", auditLogs.isEmpty());
        
        // Verify all entries have the correct user ID and event type
        for (AuditLog auditLog : auditLogs) {
            assertEquals("Audit log user ID should match admin user", getUserId("admin@test.com"), auditLog.getUserId());
            assertEquals("Audit log event type should be SUCCESSFUL_LOGIN", AuditEventType.SUCCESSFUL_LOGIN, auditLog.getEventType());
        }
    }
    
    /**
     * Test the findByTableNameAndRecordId method.
     */
    @Test
    public void testFindByTableNameAndRecordId() throws SQLException {
        // Find audit logs by table name and record ID
        List<AuditLog> auditLogs = auditLogDao.findByTableNameAndRecordId("users", "1");
        
        // Verify there are audit logs
        assertFalse("There should be audit logs for table 'users' and record ID '1'", auditLogs.isEmpty());
        
        // Verify all entries have the correct table name and record ID
        for (AuditLog auditLog : auditLogs) {
            assertEquals("Audit log table name should be 'users'", "users", auditLog.getTableName());
            assertEquals("Audit log record ID should be '1'", "1", auditLog.getRecordId());
        }
    }
    
    /**
     * Test the findByEventDescription method.
     */
    @Test
    public void testFindByEventDescription() throws SQLException {
        // Find audit log by event description
        Optional<AuditLog> auditLog = auditLogDao.findByEventDescription("User login");
        
        // Verify the audit log was found
        assertTrue("Audit log should be found by event description", auditLog.isPresent());
        assertEquals("Found audit log event description should match", "User login", auditLog.get().getEventDescription());
        
        // Test with a non-existent event description
        Optional<AuditLog> nonExistentAuditLog = auditLogDao.findByEventDescription("Non-existent event");
        
        // Verify the audit log was not found
        assertFalse("Non-existent audit log should not be found", nonExistentAuditLog.isPresent());
    }
    
    /**
     * Helper method to get the ID of a user by email.
     * 
     * @param email The email of the user
     * @return The ID of the user
     * @throws SQLException if a database error occurs
     */
    private Integer getUserId(String email) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
            ps.setString(1, email);
            var rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : null;
        }
    }
}