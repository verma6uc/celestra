package com.celestra.dao.impl;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.celestra.dao.BaseDaoTest;
import com.celestra.dao.PasswordHistoryDao;
import com.celestra.model.PasswordHistory;

/**
 * Test class for PasswordHistoryDaoImpl.
 */
public class PasswordHistoryDaoImplTest extends BaseDaoTest {
    
    private PasswordHistoryDao passwordHistoryDao;
    
    /**
     * Initialize the DAO before each test.
     */
    @Before
    public void initialize() {
        passwordHistoryDao = new PasswordHistoryDaoImpl();
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // Password history table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // Insert test user
        executeSQL("INSERT INTO users (id, company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES (999, 1, 'REGULAR_USER', 'testuser@test.com', 'Test User', 'hash123', 'ACTIVE', NOW(), NOW())");
        
        // Insert test password history entries
        executeSQL("INSERT INTO password_history (user_id, password_hash, created_at) " +
                   "VALUES (999, 'oldhash1', DATE_SUB(NOW(), INTERVAL 30 DAY))");
        
        executeSQL("INSERT INTO password_history (user_id, password_hash, created_at) " +
                   "VALUES (999, 'oldhash2', DATE_SUB(NOW(), INTERVAL 20 DAY))");
        
        executeSQL("INSERT INTO password_history (user_id, password_hash, created_at) " +
                   "VALUES (999, 'oldhash3', DATE_SUB(NOW(), INTERVAL 10 DAY))");
        
        executeSQL("INSERT INTO password_history (user_id, password_hash, created_at) " +
                   "VALUES (999, 'currenthash', NOW())");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM password_history WHERE user_id = 999 OR password_hash LIKE 'test%'");
        executeSQL("DELETE FROM users WHERE email = 'testuser@test.com'");
    }
    
    /**
     * Test the create method.
     */
    @Test
    public void testCreate() throws SQLException {
        // Create a new password history entry
        PasswordHistory passwordHistory = new PasswordHistory();
        passwordHistory.setUserId(999);
        passwordHistory.setPasswordHash("testhash1");
        passwordHistory.setCreatedAt(OffsetDateTime.now());
        
        PasswordHistory createdPasswordHistory = passwordHistoryDao.create(passwordHistory);
        
        // Verify the password history entry was created
        assertNotNull("Created password history should not be null", createdPasswordHistory);
        assertTrue("Created password history should have an ID", createdPasswordHistory.getId() > 0);
        
        // Clean up
        boolean deleted = passwordHistoryDao.delete(createdPasswordHistory.getId());
        assertTrue("Password history should be deleted successfully", deleted);
    }
    
    /**
     * Test the findById method.
     */
    @Test
    public void testFindById() throws SQLException {
        // Find all password history entries
        List<PasswordHistory> passwordHistories = passwordHistoryDao.findAll();
        
        // Verify there are password history entries
        assertFalse("There should be password history entries in the database", passwordHistories.isEmpty());
        
        // Get the first password history entry
        PasswordHistory passwordHistory = passwordHistories.get(0);
        
        // Find the password history entry by ID
        Optional<PasswordHistory> foundPasswordHistory = passwordHistoryDao.findById(passwordHistory.getId());
        
        // Verify the password history entry was found
        assertTrue("Password history should be found by ID", foundPasswordHistory.isPresent());
        assertEquals("Found password history ID should match", passwordHistory.getId(), foundPasswordHistory.get().getId());
        assertEquals("Found password history hash should match", passwordHistory.getPasswordHash(), foundPasswordHistory.get().getPasswordHash());
    }
    
    /**
     * Test the findAll method.
     */
    @Test
    public void testFindAll() throws SQLException {
        // Find all password history entries
        List<PasswordHistory> passwordHistories = passwordHistoryDao.findAll();
        
        // Verify there are password history entries
        assertFalse("There should be password history entries in the database", passwordHistories.isEmpty());
        assertTrue("There should be at least 4 password history entries", passwordHistories.size() >= 4);
    }
    
    /**
     * Test the update method.
     */
    @Test
    public void testUpdate() throws SQLException {
        // Create a new password history entry
        PasswordHistory passwordHistory = new PasswordHistory();
        passwordHistory.setUserId(999);
        passwordHistory.setPasswordHash("testhash2");
        passwordHistory.setCreatedAt(OffsetDateTime.now());
        
        PasswordHistory createdPasswordHistory = passwordHistoryDao.create(passwordHistory);
        
        // Update the password history entry
        createdPasswordHistory.setPasswordHash("updatedhash");
        
        PasswordHistory updatedPasswordHistory = passwordHistoryDao.update(createdPasswordHistory);
        
        // Verify the password history entry was updated
        assertEquals("Password history hash should be updated", "updatedhash", updatedPasswordHistory.getPasswordHash());
        
        // Clean up
        boolean deleted = passwordHistoryDao.delete(createdPasswordHistory.getId());
        assertTrue("Password history should be deleted successfully", deleted);
    }
    
    /**
     * Test the delete method.
     */
    @Test
    public void testDelete() throws SQLException {
        // Create a new password history entry
        PasswordHistory passwordHistory = new PasswordHistory();
        passwordHistory.setUserId(999);
        passwordHistory.setPasswordHash("testhash3");
        passwordHistory.setCreatedAt(OffsetDateTime.now());
        
        PasswordHistory createdPasswordHistory = passwordHistoryDao.create(passwordHistory);
        
        // Delete the password history entry
        boolean deleted = passwordHistoryDao.delete(createdPasswordHistory.getId());
        
        // Verify the password history entry was deleted
        assertTrue("Password history should be deleted successfully", deleted);
        
        Optional<PasswordHistory> foundPasswordHistory = passwordHistoryDao.findById(createdPasswordHistory.getId());
        assertFalse("Password history should not be found after deletion", foundPasswordHistory.isPresent());
    }
    
    /**
     * Test the findByUserId method.
     */
    @Test
    public void testFindByUserId() throws SQLException {
        // Find password history entries by user ID
        List<PasswordHistory> passwordHistories = passwordHistoryDao.findByUserId(999);
        
        // Verify there are password history entries
        assertFalse("There should be password history entries for user ID 999", passwordHistories.isEmpty());
        
        // Verify all entries have the correct user ID
        for (PasswordHistory passwordHistory : passwordHistories) {
            assertEquals("Password history user ID should be 999", Integer.valueOf(999), passwordHistory.getUserId());
        }
    }
    
    /**
     * Test the findRecentByUserId method.
     */
    @Test
    public void testFindRecentByUserId() throws SQLException {
        // Find recent password history entries by user ID
        List<PasswordHistory> passwordHistories = passwordHistoryDao.findRecentByUserId(999, 2);
        
        // Verify there are password history entries and the limit is respected
        assertFalse("There should be recent password history entries for user ID 999", passwordHistories.isEmpty());
        assertTrue("There should be at most 2 recent password history entries", passwordHistories.size() <= 2);
        
        // Verify all entries have the correct user ID
        for (PasswordHistory passwordHistory : passwordHistories) {
            assertEquals("Password history user ID should be 999", Integer.valueOf(999), passwordHistory.getUserId());
        }
    }
    
    /**
     * Test the existsByUserIdAndPasswordHash method.
     */
    @Test
    public void testExistsByUserIdAndPasswordHash() throws SQLException {
        // Check if a password hash exists for a user
        boolean exists = passwordHistoryDao.existsByUserIdAndPasswordHash(999, "oldhash1");
        
        // Verify the password hash exists
        assertTrue("Password hash 'oldhash1' should exist for user ID 999", exists);
        
        // Check if a non-existent password hash exists
        boolean notExists = passwordHistoryDao.existsByUserIdAndPasswordHash(999, "nonexistenthash");
        
        // Verify the password hash does not exist
        assertFalse("Password hash 'nonexistenthash' should not exist for user ID 999", notExists);
    }
    
    /**
     * Test the deleteByUserId method.
     */
    @Test
    public void testDeleteByUserId() throws SQLException {
        // Create a new password history entry for a different user
        PasswordHistory passwordHistory = new PasswordHistory();
        passwordHistory.setUserId(998);
        passwordHistory.setPasswordHash("testhash4");
        passwordHistory.setCreatedAt(OffsetDateTime.now());
        
        PasswordHistory createdPasswordHistory = passwordHistoryDao.create(passwordHistory);
        
        // Delete password history entries by user ID
        int deleted = passwordHistoryDao.deleteByUserId(998);
        
        // Verify password history entries were deleted
        assertTrue("At least one password history entry should be deleted", deleted > 0);
        
        Optional<PasswordHistory> foundPasswordHistory = passwordHistoryDao.findById(createdPasswordHistory.getId());
        assertFalse("Password history should not be found after deletion", foundPasswordHistory.isPresent());
    }
    
    /**
     * Test the deleteOlderThan method.
     */
    @Test
    public void testDeleteOlderThan() throws SQLException {
        // Delete password history entries older than 15 days
        OffsetDateTime cutoffDate = OffsetDateTime.now().minusDays(15);
        int deleted = passwordHistoryDao.deleteOlderThan(cutoffDate);
        
        // Verify old password history entries were deleted
        assertTrue("At least one old password history entry should be deleted", deleted > 0);
        
        // Verify no entries older than 15 days remain
        List<PasswordHistory> allEntries = passwordHistoryDao.findAll();
        for (PasswordHistory entry : allEntries) {
            if (entry.getUserId() == 999) {
                assertTrue("Remaining entries should be newer than cutoff date", 
                           entry.getCreatedAt().isAfter(cutoffDate));
            }
        }
    }
    
    /**
     * Test the deleteOldestByUserId method.
     */
    @Test
    public void testDeleteOldestByUserId() throws SQLException {
        // Count initial entries
        List<PasswordHistory> initialEntries = passwordHistoryDao.findByUserId(999);
        int initialCount = initialEntries.size();
        
        // Delete oldest password history entries, keeping only the most recent 2
        int deleted = passwordHistoryDao.deleteOldestByUserId(999, 2);
        
        // Verify old password history entries were deleted
        assertTrue("At least one old password history entry should be deleted", deleted > 0);
        
        // Verify only 2 entries remain
        List<PasswordHistory> remainingEntries = passwordHistoryDao.findByUserId(999);
        assertEquals("There should be 2 password history entries remaining", 2, remainingEntries.size());
        
        // Verify the total number of deleted entries is correct
        assertEquals("The number of deleted entries should be correct", 
                     initialCount - 2, deleted);
    }
}