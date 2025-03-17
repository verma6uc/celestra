package com.celestra.dao.impl;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.celestra.dao.AuditChangeLogDao;
import com.celestra.dao.BaseDaoTest;
import com.celestra.enums.AuditEventType;
import com.celestra.model.AuditChangeLog;

/**
 * Test class for AuditChangeLogDaoImpl.
 */
public class AuditChangeLogDaoImplTest extends BaseDaoTest {
    
    private AuditChangeLogDao auditChangeLogDao;
    
    /**
     * Initialize the DAO before each test.
     */
    @Before
    public void initialize() {
        auditChangeLogDao = new AuditChangeLogDaoImpl();
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // Audit change logs table should already exist in the database
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
        
        // Insert test audit logs (to satisfy foreign key constraints)
        executeSQL("INSERT INTO audit_logs (id, user_id, event_type, event_description, ip_address, table_name, record_id, created_at) " +
                   "VALUES (1, 1, 'CONFIGURATION_UPDATE'::audit_event_type, 'Updated company settings', '192.168.1.1', 'companies', '1', NOW())");
        
        executeSQL("INSERT INTO audit_logs (id, user_id, event_type, event_description, ip_address, table_name, record_id, created_at) " +
                   "VALUES (2, 1, 'DATA_EXPORT'::audit_event_type, 'Exported user data', '192.168.1.1', 'users', '1', NOW())");
        
        // Insert test audit change logs
        executeSQL("INSERT INTO audit_change_logs (id, audit_log_id, column_name, old_value, new_value, created_at) " +
                   "VALUES (nextval('audit_change_logs_id_seq'), 1, 'name', 'Old Company Name', 'New Company Name', NOW())");
        
        executeSQL("INSERT INTO audit_change_logs (id, audit_log_id, column_name, old_value, new_value, created_at) " +
                   "VALUES (nextval('audit_change_logs_id_seq'), 1, 'description', 'Old Description', 'New Description', NOW())");
        
        executeSQL("INSERT INTO audit_change_logs (id, audit_log_id, column_name, old_value, new_value, created_at) " +
                   "VALUES (nextval('audit_change_logs_id_seq'), 2, 'email', 'old@test.com', 'new@test.com', NOW())");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM audit_change_logs WHERE column_name LIKE 'Test%' OR column_name IN ('name', 'description', 'email')");
        executeSQL("DELETE FROM audit_logs WHERE id IN (1, 2)");
        executeSQL("DELETE FROM users WHERE id = 1");
        executeSQL("DELETE FROM companies WHERE id = 1");
    }
    
    /**
     * Test the create method.
     */
    @Test
    public void testCreate() throws SQLException {
        // Create a new audit change log
        AuditChangeLog auditChangeLog = new AuditChangeLog();
        auditChangeLog.setAuditLogId(1);
        auditChangeLog.setColumnName("Test Column");
        auditChangeLog.setOldValue("Old Value");
        auditChangeLog.setNewValue("New Value");
        
        AuditChangeLog createdAuditChangeLog = auditChangeLogDao.create(auditChangeLog);
        
        // Verify the audit change log was created
        assertNotNull("Created audit change log should not be null", createdAuditChangeLog);
        assertTrue("Created audit change log should have an ID", createdAuditChangeLog.getId() > 0);
        
        // Clean up
        boolean deleted = auditChangeLogDao.delete(createdAuditChangeLog.getId());
        assertTrue("Audit change log should be deleted successfully", deleted);
    }
    
    /**
     * Test the findById method.
     */
    @Test
    public void testFindById() throws SQLException {
        // Find all audit change logs
        List<AuditChangeLog> auditChangeLogs = auditChangeLogDao.findAll();
        
        // Verify there are audit change logs
        assertFalse("There should be audit change logs in the database", auditChangeLogs.isEmpty());
        
        // Get the first audit change log
        AuditChangeLog auditChangeLog = auditChangeLogs.get(0);
        
        // Find the audit change log by ID
        Optional<AuditChangeLog> foundAuditChangeLog = auditChangeLogDao.findById(auditChangeLog.getId());
        
        // Verify the audit change log was found
        assertTrue("Audit change log should be found by ID", foundAuditChangeLog.isPresent());
        assertEquals("Found audit change log ID should match", auditChangeLog.getId(), foundAuditChangeLog.get().getId());
        assertEquals("Found audit change log column name should match", auditChangeLog.getColumnName(), foundAuditChangeLog.get().getColumnName());
    }
    
    /**
     * Test the findAll method.
     */
    @Test
    public void testFindAll() throws SQLException {
        // Find all audit change logs
        List<AuditChangeLog> auditChangeLogs = auditChangeLogDao.findAll();
        
        // Verify there are audit change logs
        assertFalse("There should be audit change logs in the database", auditChangeLogs.isEmpty());
    }
    
    /**
     * Test the update method.
     */
    @Test
    public void testUpdate() throws SQLException {
        // Create a new audit change log
        AuditChangeLog auditChangeLog = new AuditChangeLog();
        auditChangeLog.setAuditLogId(1);
        auditChangeLog.setColumnName("Test Column Update");
        auditChangeLog.setOldValue("Old Value Update");
        auditChangeLog.setNewValue("New Value Update");
        
        AuditChangeLog createdAuditChangeLog = auditChangeLogDao.create(auditChangeLog);
        
        // Update the audit change log
        createdAuditChangeLog.setOldValue("Updated Old Value");
        createdAuditChangeLog.setNewValue("Updated New Value");
        
        AuditChangeLog updatedAuditChangeLog = auditChangeLogDao.update(createdAuditChangeLog);
        
        // Verify the audit change log was updated
        assertEquals("Audit change log old value should be updated", "Updated Old Value", updatedAuditChangeLog.getOldValue());
        assertEquals("Audit change log new value should be updated", "Updated New Value", updatedAuditChangeLog.getNewValue());
        
        // Clean up
        boolean deleted = auditChangeLogDao.delete(createdAuditChangeLog.getId());
        assertTrue("Audit change log should be deleted successfully", deleted);
    }
    
    /**
     * Test the delete method.
     */
    @Test
    public void testDelete() throws SQLException {
        // Create a new audit change log
        AuditChangeLog auditChangeLog = new AuditChangeLog();
        auditChangeLog.setAuditLogId(1);
        auditChangeLog.setColumnName("Test Column Delete");
        auditChangeLog.setOldValue("Old Value Delete");
        auditChangeLog.setNewValue("New Value Delete");
        
        AuditChangeLog createdAuditChangeLog = auditChangeLogDao.create(auditChangeLog);
        
        // Delete the audit change log
        boolean deleted = auditChangeLogDao.delete(createdAuditChangeLog.getId());
        
        // Verify the audit change log was deleted
        assertTrue("Audit change log should be deleted successfully", deleted);
        
        Optional<AuditChangeLog> foundAuditChangeLog = auditChangeLogDao.findById(createdAuditChangeLog.getId());
        assertFalse("Audit change log should not be found after deletion", foundAuditChangeLog.isPresent());
    }
    
    /**
     * Test the findByAuditLogId method.
     */
    @Test
    public void testFindByAuditLogId() throws SQLException {
        // Find audit change logs by audit log ID
        List<AuditChangeLog> auditChangeLogs = auditChangeLogDao.findByAuditLogId(1);
        
        // Verify there are audit change logs
        assertFalse("There should be audit change logs for audit log ID 1", auditChangeLogs.isEmpty());
        
        // Verify all entries have the correct audit log ID
        for (AuditChangeLog auditChangeLog : auditChangeLogs) {
            assertEquals("Audit change log audit log ID should be 1", Integer.valueOf(1), auditChangeLog.getAuditLogId());
        }
    }
    
    /**
     * Test the findByColumnName method.
     */
    @Test
    public void testFindByColumnName() throws SQLException {
        // Find audit change logs by column name
        List<AuditChangeLog> auditChangeLogs = auditChangeLogDao.findByColumnName("name");
        
        // Verify there are audit change logs
        assertFalse("There should be audit change logs for column name 'name'", auditChangeLogs.isEmpty());
        
        // Verify all entries have the correct column name
        for (AuditChangeLog auditChangeLog : auditChangeLogs) {
            assertEquals("Audit change log column name should be 'name'", "name", auditChangeLog.getColumnName());
        }
    }
    
    /**
     * Test the findByOldValueContaining method.
     */
    @Test
    public void testFindByOldValueContaining() throws SQLException {
        // Find audit change logs by old value containing
        List<AuditChangeLog> auditChangeLogs = auditChangeLogDao.findByOldValueContaining("Old");
        
        // Verify there are audit change logs
        assertFalse("There should be audit change logs with old values containing 'Old'", auditChangeLogs.isEmpty());
        
        // Verify all entries have old values containing the pattern
        for (AuditChangeLog auditChangeLog : auditChangeLogs) {
            assertTrue("Audit change log old value should contain 'Old'", 
                    auditChangeLog.getOldValue().contains("Old"));
        }
    }
    
    /**
     * Test the findByNewValueContaining method.
     */
    @Test
    public void testFindByNewValueContaining() throws SQLException {
        // Find audit change logs by new value containing
        List<AuditChangeLog> auditChangeLogs = auditChangeLogDao.findByNewValueContaining("New");
        
        // Verify there are audit change logs
        assertFalse("There should be audit change logs with new values containing 'New'", auditChangeLogs.isEmpty());
        
        // Verify all entries have new values containing the pattern
        for (AuditChangeLog auditChangeLog : auditChangeLogs) {
            assertTrue("Audit change log new value should contain 'New'", 
                    auditChangeLog.getNewValue().contains("New"));
        }
    }
    
    /**
     * Test the findByAuditLogIdAndColumnName method.
     */
    @Test
    public void testFindByAuditLogIdAndColumnName() throws SQLException {
        // Find audit change logs by audit log ID and column name
        List<AuditChangeLog> auditChangeLogs = auditChangeLogDao.findByAuditLogIdAndColumnName(1, "name");
        
        // Verify there are audit change logs
        assertFalse("There should be audit change logs for audit log ID 1 and column name 'name'", auditChangeLogs.isEmpty());
        
        // Verify all entries have the correct audit log ID and column name
        for (AuditChangeLog auditChangeLog : auditChangeLogs) {
            assertEquals("Audit change log audit log ID should be 1", Integer.valueOf(1), auditChangeLog.getAuditLogId());
            assertEquals("Audit change log column name should be 'name'", "name", auditChangeLog.getColumnName());
        }
    }
}