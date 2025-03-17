package com.celestra.dao.impl;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.celestra.dao.BaseDaoTest;
import com.celestra.dao.UserLockoutDao;
import com.celestra.model.UserLockout;

/**
 * Test class for UserLockoutDaoImpl.
 */
public class UserLockoutDaoImplTest extends BaseDaoTest {
    
    private UserLockoutDao userLockoutDao;
    
    /**
     * Initialize the DAO before each test.
     */
    @Before
    public void initialize() {
        userLockoutDao = new UserLockoutDaoImpl();
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // User lockouts table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // First insert test company to satisfy foreign key constraints
        executeSQL("INSERT INTO companies (id, name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES (1, 'Test Company 1', 'Test Company Description 1', 'SMALL'::company_size, 'TECH'::company_vertical, 'ACTIVE'::company_status, NOW(), NOW())");
        
        // Insert test users
        executeSQL("INSERT INTO users (id, company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES (999, 1, 'REGULAR_USER'::user_role, 'testuser@test.com', 'Test User', 'hash123', 'ACTIVE'::user_status, NOW(), NOW())");
        
        executeSQL("INSERT INTO users (id, company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES (998, 1, 'REGULAR_USER'::user_role, 'testuser2@test.com', 'Test User 2', 'hash456', 'ACTIVE'::user_status, NOW(), NOW())");
        
        // Insert test lockouts
        executeSQL("INSERT INTO user_lockouts (user_id, lockout_start, lockout_end, failed_attempts, reason, created_at, updated_at) " +
                   "VALUES (999, NOW(), NOW() + INTERVAL '1 day', 3, 'Test temporary lockout', NOW(), NOW())");
        
        executeSQL("INSERT INTO user_lockouts (user_id, lockout_start, lockout_end, failed_attempts, reason, created_at, updated_at) " +
                   "VALUES (999, NOW(), NULL, 5, 'Test permanent lockout', NOW(), NOW())");
        
        executeSQL("INSERT INTO user_lockouts (user_id, lockout_start, lockout_end, failed_attempts, reason, created_at, updated_at) " +
                   "VALUES (999, NOW(), NOW() - INTERVAL '1 day', 2, 'Test expired lockout', NOW(), NOW())");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM user_lockouts WHERE reason LIKE 'Test%'");
        executeSQL("DELETE FROM users WHERE email IN ('testuser@test.com', 'testuser2@test.com')");
        executeSQL("DELETE FROM companies WHERE id = 1");
    }
    
    /**
     * Test the create method.
     */
    @Test
    public void testCreate() throws SQLException {
        // Create a new user lockout
        UserLockout userLockout = new UserLockout();
        userLockout.setUserId(999);
        userLockout.setLockoutStart(OffsetDateTime.now());
        userLockout.setLockoutEnd(OffsetDateTime.now().plusHours(1));
        userLockout.setFailedAttempts(3);
        userLockout.setReason("Test create lockout");
        
        UserLockout createdUserLockout = userLockoutDao.create(userLockout);
        
        // Verify the user lockout was created
        assertNotNull("Created user lockout should not be null", createdUserLockout);
        assertTrue("Created user lockout should have an ID", createdUserLockout.getId() > 0);
        
        // Clean up
        boolean deleted = userLockoutDao.delete(createdUserLockout.getId());
        assertTrue("User lockout should be deleted successfully", deleted);
    }
    
    /**
     * Test the findById method.
     */
    @Test
    public void testFindById() throws SQLException {
        // Find all user lockouts
        List<UserLockout> userLockouts = userLockoutDao.findAll();
        
        // Verify there are user lockouts
        assertFalse("There should be user lockouts in the database", userLockouts.isEmpty());
        
        // Get the first user lockout
        UserLockout userLockout = userLockouts.get(0);
        
        // Find the user lockout by ID
        Optional<UserLockout> foundUserLockout = userLockoutDao.findById(userLockout.getId());
        
        // Verify the user lockout was found
        assertTrue("User lockout should be found by ID", foundUserLockout.isPresent());
        assertEquals("Found user lockout ID should match", userLockout.getId(), foundUserLockout.get().getId());
        assertEquals("Found user lockout user ID should match", userLockout.getUserId(), foundUserLockout.get().getUserId());
    }
    
    /**
     * Test the findAll method.
     */
    @Test
    public void testFindAll() throws SQLException {
        // Find all user lockouts
        List<UserLockout> userLockouts = userLockoutDao.findAll();
        
        // Verify there are user lockouts
        assertFalse("There should be user lockouts in the database", userLockouts.isEmpty());
        assertTrue("There should be at least 3 user lockouts", userLockouts.size() >= 3);
    }
    
    /**
     * Test the update method.
     */
    @Test
    public void testUpdate() throws SQLException {
        // Create a new user lockout
        UserLockout userLockout = new UserLockout();
        userLockout.setUserId(999);
        userLockout.setLockoutStart(OffsetDateTime.now());
        userLockout.setLockoutEnd(OffsetDateTime.now().plusHours(1));
        userLockout.setFailedAttempts(3);
        userLockout.setReason("Test update lockout");
        
        UserLockout createdUserLockout = userLockoutDao.create(userLockout);
        
        // Update the user lockout
        createdUserLockout.setFailedAttempts(5);
        createdUserLockout.setReason("Updated test lockout");
        
        UserLockout updatedUserLockout = userLockoutDao.update(createdUserLockout);
        
        // Verify the user lockout was updated
        assertEquals("User lockout failed attempts should be updated", Integer.valueOf(5), updatedUserLockout.getFailedAttempts());
        assertEquals("User lockout reason should be updated", "Updated test lockout", updatedUserLockout.getReason());
        
        // Clean up
        boolean deleted = userLockoutDao.delete(createdUserLockout.getId());
        assertTrue("User lockout should be deleted successfully", deleted);
    }
    
    /**
     * Test the delete method.
     */
    @Test
    public void testDelete() throws SQLException {
        // Create a new user lockout
        UserLockout userLockout = new UserLockout();
        userLockout.setUserId(999);
        userLockout.setLockoutStart(OffsetDateTime.now());
        userLockout.setLockoutEnd(OffsetDateTime.now().plusHours(1));
        userLockout.setFailedAttempts(3);
        userLockout.setReason("Test delete lockout");
        
        UserLockout createdUserLockout = userLockoutDao.create(userLockout);
        
        // Delete the user lockout
        boolean deleted = userLockoutDao.delete(createdUserLockout.getId());
        
        // Verify the user lockout was deleted
        assertTrue("User lockout should be deleted successfully", deleted);
        
        Optional<UserLockout> foundUserLockout = userLockoutDao.findById(createdUserLockout.getId());
        assertFalse("User lockout should not be found after deletion", foundUserLockout.isPresent());
    }
    
    /**
     * Test the findActiveByUserId method.
     */
    @Test
    public void testFindActiveByUserId() throws SQLException {
        // Find active lockout by user ID
        Optional<UserLockout> userLockout = userLockoutDao.findActiveByUserId(999);
        
        // Verify the active lockout was found
        assertTrue("Active lockout should be found for user ID 999", userLockout.isPresent());
        assertEquals("Found lockout user ID should be 999", Integer.valueOf(999), userLockout.get().getUserId());
        assertTrue("Found lockout should be active", userLockout.get().isActive());
    }
    
    /**
     * Test the findByUserId method.
     */
    @Test
    public void testFindByUserId() throws SQLException {
        // Find lockouts by user ID
        List<UserLockout> userLockouts = userLockoutDao.findByUserId(999);
        
        // Verify there are lockouts
        assertFalse("There should be lockouts for user ID 999", userLockouts.isEmpty());
        
        // Verify all lockouts have the correct user ID
        for (UserLockout userLockout : userLockouts) {
            assertEquals("Lockout user ID should be 999", Integer.valueOf(999), userLockout.getUserId());
        }
    }
    
    /**
     * Test the findAllActive method.
     */
    @Test
    public void testFindAllActive() throws SQLException {
        // Find all active lockouts
        List<UserLockout> userLockouts = userLockoutDao.findAllActive();
        
        // Verify there are active lockouts
        assertFalse("There should be active lockouts", userLockouts.isEmpty());
        
        // Verify all lockouts are active
        for (UserLockout userLockout : userLockouts) {
            assertTrue("Lockout should be active", userLockout.isActive());
        }
    }
    
    /**
     * Test the findAllExpired method.
     */
    @Test
    public void testFindAllExpired() throws SQLException {
        // Find all expired lockouts
        List<UserLockout> userLockouts = userLockoutDao.findAllExpired();
        
        // Verify there are expired lockouts
        assertFalse("There should be expired lockouts", userLockouts.isEmpty());
        
        // Verify all lockouts are expired
        for (UserLockout userLockout : userLockouts) {
            assertTrue("Lockout should be expired", userLockout.isExpired());
        }
    }
    
    /**
     * Test the findAllPermanent method.
     */
    @Test
    public void testFindAllPermanent() throws SQLException {
        // Find all permanent lockouts
        List<UserLockout> userLockouts = userLockoutDao.findAllPermanent();
        
        // Verify there are permanent lockouts
        assertFalse("There should be permanent lockouts", userLockouts.isEmpty());
        
        // Verify all lockouts are permanent
        for (UserLockout userLockout : userLockouts) {
            assertTrue("Lockout should be permanent", userLockout.isPermanent());
        }
    }
    
    /**
     * Test the findAllTemporary method.
     */
    @Test
    public void testFindAllTemporary() throws SQLException {
        // Find all temporary lockouts
        List<UserLockout> userLockouts = userLockoutDao.findAllTemporary();
        
        // Verify there are temporary lockouts
        assertFalse("There should be temporary lockouts", userLockouts.isEmpty());
        
        // Verify all lockouts are temporary
        for (UserLockout userLockout : userLockouts) {
            assertFalse("Lockout should be temporary", userLockout.isPermanent());
        }
    }
    
    /**
     * Test the updateLockoutEnd method.
     */
    @Test
    public void testUpdateLockoutEnd() throws SQLException {
        // Create a new user lockout
        UserLockout userLockout = new UserLockout();
        userLockout.setUserId(999);
        userLockout.setLockoutStart(OffsetDateTime.now());
        userLockout.setLockoutEnd(OffsetDateTime.now().plusHours(1));
        userLockout.setFailedAttempts(3);
        userLockout.setReason("Test update lockout end");
        
        UserLockout createdUserLockout = userLockoutDao.create(userLockout);
        
        // Update the lockout end time
        OffsetDateTime newLockoutEnd = OffsetDateTime.now().plusDays(7);
        boolean updated = userLockoutDao.updateLockoutEnd(createdUserLockout.getId(), newLockoutEnd);
        
        // Verify the lockout end time was updated
        assertTrue("Lockout end time should be updated successfully", updated);
        
        Optional<UserLockout> updatedUserLockout = userLockoutDao.findById(createdUserLockout.getId());
        assertTrue("Lockout should be found after end time update", updatedUserLockout.isPresent());
        assertTrue("Lockout end time should be updated", 
                   updatedUserLockout.get().getLockoutEnd().isAfter(OffsetDateTime.now().plusDays(6)));
        
        // Clean up
        boolean deleted = userLockoutDao.delete(createdUserLockout.getId());
        assertTrue("User lockout should be deleted successfully", deleted);
    }
    
    /**
     * Test the updateFailedAttempts method.
     */
    @Test
    public void testUpdateFailedAttempts() throws SQLException {
        // Create a new user lockout
        UserLockout userLockout = new UserLockout();
        userLockout.setUserId(999);
        userLockout.setLockoutStart(OffsetDateTime.now());
        userLockout.setLockoutEnd(OffsetDateTime.now().plusHours(1));
        userLockout.setFailedAttempts(3);
        userLockout.setReason("Test update failed attempts");
        
        UserLockout createdUserLockout = userLockoutDao.create(userLockout);
        
        // Update the failed attempts
        boolean updated = userLockoutDao.updateFailedAttempts(createdUserLockout.getId(), 10);
        
        // Verify the failed attempts were updated
        assertTrue("Failed attempts should be updated successfully", updated);
        
        Optional<UserLockout> updatedUserLockout = userLockoutDao.findById(createdUserLockout.getId());
        assertTrue("Lockout should be found after failed attempts update", updatedUserLockout.isPresent());
        assertEquals("Lockout failed attempts should be updated", Integer.valueOf(10), updatedUserLockout.get().getFailedAttempts());
        
        // Clean up
        boolean deleted = userLockoutDao.delete(createdUserLockout.getId());
        assertTrue("User lockout should be deleted successfully", deleted);
    }
    
    /**
     * Test the deleteExpired method.
     */
    @Test
    public void testDeleteExpired() throws SQLException {
        // Delete expired lockouts
        int deleted = userLockoutDao.deleteExpired();
        
        // Verify expired lockouts were deleted
        assertTrue("At least one expired lockout should be deleted", deleted > 0);
        
        // Verify no expired lockouts remain
        List<UserLockout> expiredLockouts = userLockoutDao.findAllExpired();
        assertTrue("No expired lockouts should remain", expiredLockouts.isEmpty());
    }
    
    /**
     * Test the deleteByUserId method.
     */
    @Test
    public void testDeleteByUserId() throws SQLException {
        // Create a new user lockout for user 998
        UserLockout userLockout = new UserLockout();
        userLockout.setUserId(998);
        userLockout.setLockoutStart(OffsetDateTime.now());
        userLockout.setLockoutEnd(OffsetDateTime.now().plusHours(1));
        userLockout.setFailedAttempts(3);
        userLockout.setReason("Test delete by user ID");
        
        UserLockout createdUserLockout = userLockoutDao.create(userLockout);
        
        // Delete lockouts by user ID
        int deleted = userLockoutDao.deleteByUserId(998);
        
        // Verify lockouts were deleted
        assertTrue("At least one lockout should be deleted", deleted > 0);
        
        Optional<UserLockout> foundUserLockout = userLockoutDao.findById(createdUserLockout.getId());
        assertFalse("Lockout should not be found after deletion", foundUserLockout.isPresent());
    }
}