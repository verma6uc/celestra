package com.celestra.dao.impl;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.celestra.dao.BaseDaoTest;
import com.celestra.dao.FailedLoginDao;
import com.celestra.model.FailedLogin;

/**
 * Test class for FailedLoginDaoImpl.
 */
public class FailedLoginDaoImplTest extends BaseDaoTest {
    
    private FailedLoginDao failedLoginDao;
    
    /**
     * Initialize the DAO before each test.
     */
    @Before
    public void initialize() {
        failedLoginDao = new FailedLoginDaoImpl();
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // Failed logins table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // First insert test company to satisfy foreign key constraints
        executeSQL("INSERT INTO companies (id, name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES (1, 'Test Company 1', 'Test Company Description 1', 'SMALL'::company_size, 'TECH'::company_vertical, 'ACTIVE'::company_status, NOW(), NOW())");
        
        // Insert test user
        executeSQL("INSERT INTO users (id, company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES (999, 1, 'REGULAR_USER'::user_role, 'testuser@test.com', 'Test User', 'hash123', 'ACTIVE'::user_status, NOW(), NOW())");
        
        executeSQL("INSERT INTO failed_logins (user_id, ip_address, attempted_at, failure_reason) " +
                   "VALUES (999, '192.168.1.1', NOW(), 'Invalid password')");
        
        executeSQL("INSERT INTO failed_logins (user_id, ip_address, attempted_at, failure_reason) " +
                   "VALUES (999, '192.168.1.2', NOW(), 'Account locked')");
        
        executeSQL("INSERT INTO failed_logins (ip_address, attempted_at, failure_reason) " +
                   "VALUES ('192.168.1.3', NOW(), 'User not found')");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM failed_logins WHERE ip_address LIKE '192.168.1.%'");
        executeSQL("DELETE FROM users WHERE email = 'testuser@test.com'");
        executeSQL("DELETE FROM companies WHERE id = 1");
    }
    
    /**
     * Test the create method.
     */
    @Test
    public void testCreate() throws SQLException {
        // Create a new failed login
        FailedLogin failedLogin = new FailedLogin();
        failedLogin.setIpAddress("192.168.1.100");
        failedLogin.setFailureReason("Test failure reason");
        failedLogin.setAttemptedAt(new Timestamp(System.currentTimeMillis()));
        
        FailedLogin createdFailedLogin = failedLoginDao.create(failedLogin);
        
        // Verify the failed login was created
        assertNotNull("Created failed login should not be null", createdFailedLogin);
        assertTrue("Created failed login should have an ID", createdFailedLogin.getId() > 0);
        
        // Clean up
        boolean deleted = failedLoginDao.delete(createdFailedLogin.getId());
        assertTrue("Failed login should be deleted successfully", deleted);
    }
    
    /**
     * Test the findById method.
     */
    @Test
    public void testFindById() throws SQLException {
        // Find all failed logins
        List<FailedLogin> failedLogins = failedLoginDao.findAll();
        
        // Verify there are failed logins
        assertFalse("There should be failed logins in the database", failedLogins.isEmpty());
        
        // Get the first failed login
        FailedLogin failedLogin = failedLogins.get(0);
        
        // Find the failed login by ID
        Optional<FailedLogin> foundFailedLogin = failedLoginDao.findById(failedLogin.getId());
        
        // Verify the failed login was found
        assertTrue("Failed login should be found by ID", foundFailedLogin.isPresent());
        assertEquals("Found failed login ID should match", failedLogin.getId(), foundFailedLogin.get().getId());
        assertEquals("Found failed login IP address should match", failedLogin.getIpAddress(), foundFailedLogin.get().getIpAddress());
    }
    
    /**
     * Test the findAll method.
     */
    @Test
    public void testFindAll() throws SQLException {
        // Find all failed logins
        List<FailedLogin> failedLogins = failedLoginDao.findAll();
        
        // Verify there are failed logins
        assertFalse("There should be failed logins in the database", failedLogins.isEmpty());
        assertTrue("There should be at least 3 failed logins", failedLogins.size() >= 3);
    }
    
    /**
     * Test the update method.
     */
    @Test
    public void testUpdate() throws SQLException {
        // Create a new failed login
        FailedLogin failedLogin = new FailedLogin();
        failedLogin.setIpAddress("192.168.1.101");
        failedLogin.setFailureReason("Test update reason");
        failedLogin.setAttemptedAt(new Timestamp(System.currentTimeMillis()));
        
        FailedLogin createdFailedLogin = failedLoginDao.create(failedLogin);
        
        // Update the failed login
        createdFailedLogin.setIpAddress("192.168.1.102");
        createdFailedLogin.setFailureReason("Updated test reason");
        
        FailedLogin updatedFailedLogin = failedLoginDao.update(createdFailedLogin);
        
        // Verify the failed login was updated
        assertEquals("Failed login IP address should be updated", "192.168.1.102", updatedFailedLogin.getIpAddress());
        assertEquals("Failed login reason should be updated", "Updated test reason", updatedFailedLogin.getFailureReason());
        
        // Clean up
        boolean deleted = failedLoginDao.delete(createdFailedLogin.getId());
        assertTrue("Failed login should be deleted successfully", deleted);
    }
    
    /**
     * Test the delete method.
     */
    @Test
    public void testDelete() throws SQLException {
        // Create a new failed login
        FailedLogin failedLogin = new FailedLogin();
        failedLogin.setIpAddress("192.168.1.103");
        failedLogin.setFailureReason("Test delete reason");
        failedLogin.setAttemptedAt(new Timestamp(System.currentTimeMillis()));
        
        FailedLogin createdFailedLogin = failedLoginDao.create(failedLogin);
        
        // Delete the failed login
        boolean deleted = failedLoginDao.delete(createdFailedLogin.getId());
        
        // Verify the failed login was deleted
        assertTrue("Failed login should be deleted successfully", deleted);
        
        Optional<FailedLogin> foundFailedLogin = failedLoginDao.findById(createdFailedLogin.getId());
        assertFalse("Failed login should not be found after deletion", foundFailedLogin.isPresent());
    }
    
    /**
     * Test the findByUsername method.
     */
    @Test
    public void testFindByUsername() throws SQLException {
        // Find failed logins by username
        List<FailedLogin> failedLogins = failedLoginDao.findByUsername("testuser@test.com");
        
        // Verify there are failed logins
        assertFalse("There should be failed logins for username 'testuser@test.com'", failedLogins.isEmpty());
    }
    
    /**
     * Test the findByIpAddress method.
     */
    @Test
    public void testFindByIpAddress() throws SQLException {
        // Find failed logins by IP address
        List<FailedLogin> failedLogins = failedLoginDao.findByIpAddress("192.168.1.1");
        
        // Verify there are failed logins
        assertFalse("There should be failed logins for IP address '192.168.1.1'", failedLogins.isEmpty());
    }
    
    /**
     * Test the findByUsernameAndIpAddress method.
     */
    @Test
    public void testFindByUsernameAndIpAddress() throws SQLException {
        // Find failed logins by username and IP address
        List<FailedLogin> failedLogins = failedLoginDao.findByUsernameAndIpAddress("testuser@test.com", "192.168.1.1");
        
        // Verify there are failed logins
        assertFalse("There should be failed logins for username 'testuser@test.com' and IP address '192.168.1.1'", failedLogins.isEmpty());
    }
    
    /**
     * Test the findRecentByUsername method.
     */
    @Test
    public void testFindRecentByUsername() throws SQLException {
        // Find recent failed logins by username
        List<FailedLogin> failedLogins = failedLoginDao.findRecentByUsername("testuser@test.com", 60);
        
        // Verify there are failed logins
        assertFalse("There should be recent failed logins for username 'testuser@test.com'", failedLogins.isEmpty());
    }
    
    /**
     * Test the countRecentByUsername method.
     */
    @Test
    public void testCountRecentByUsername() throws SQLException {
        // Count recent failed logins by username
        int count = failedLoginDao.countRecentByUsername("testuser@test.com", 60);
        
        // Verify there are failed logins
        assertTrue("There should be recent failed logins for username 'testuser@test.com'", count > 0);
    }
    
    /**
     * Test the countRecentByIpAddress method.
     */
    @Test
    public void testCountRecentByIpAddress() throws SQLException {
        // Count recent failed logins by IP address
        int count = failedLoginDao.countRecentByIpAddress("192.168.1.1", 60);
        
        // Verify there are failed logins
        assertTrue("There should be recent failed logins for IP address '192.168.1.1'", count > 0);
    }
    
    /**
     * Test the deleteOlderThan method.
     */
    @Test
    public void testDeleteOlderThan() throws SQLException {
        // Create a new failed login with a timestamp in the past
        FailedLogin failedLogin = new FailedLogin();
        failedLogin.setIpAddress("192.168.1.104");
        failedLogin.setFailureReason("Test old record");
        
        // Set timestamp to 10 days ago
        Timestamp oldTimestamp = new Timestamp(System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000L);
        failedLogin.setAttemptedAt(oldTimestamp);
        
        FailedLogin createdFailedLogin = failedLoginDao.create(failedLogin);
        
        // Delete failed logins older than 5 days
        int deleted = failedLoginDao.deleteOlderThan(5);
        
        // Verify the old failed login was deleted
        assertTrue("At least one old failed login should be deleted", deleted > 0);
        
        Optional<FailedLogin> foundFailedLogin = failedLoginDao.findById(createdFailedLogin.getId());
        assertFalse("Old failed login should not be found after deletion", foundFailedLogin.isPresent());
    }
}