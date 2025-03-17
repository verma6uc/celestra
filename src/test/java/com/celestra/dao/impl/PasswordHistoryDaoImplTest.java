package com.celestra.dao.impl;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

import com.celestra.dao.BaseDaoTest;
import com.celestra.dao.PasswordHistoryDao;
import com.celestra.model.PasswordHistory;

/**
 * Test class for PasswordHistoryDaoImpl.
 */
public class PasswordHistoryDaoImplTest extends BaseDaoTest {
    
    private PasswordHistoryDao passwordHistoryDao;
    
    /**
     * Main method to run the tests.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        PasswordHistoryDaoImplTest test = new PasswordHistoryDaoImplTest();
        test.runTests();
    }
    
    /**
     * Constructor.
     */
    public PasswordHistoryDaoImplTest() {
        passwordHistoryDao = new PasswordHistoryDaoImpl();
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
            testFindRecentByUserId();
            testExistsByUserIdAndPasswordHash();
            testDeleteByUserId();
            testDeleteOlderThan();
            testDeleteOldestByUserId();
            
            tearDown();
            
            System.out.println("All tests completed.");
        } catch (Exception e) {
            System.err.println("Error running tests: " + e.getMessage());
            e.printStackTrace();
        }
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
    private void testCreate() {
        try {
            // Create a new password history entry
            PasswordHistory passwordHistory = new PasswordHistory();
            passwordHistory.setUserId(999);
            passwordHistory.setPasswordHash("testhash1");
            passwordHistory.setCreatedAt(OffsetDateTime.now());
            
            PasswordHistory createdPasswordHistory = passwordHistoryDao.create(passwordHistory);
            
            // Verify the password history entry was created
            boolean success = createdPasswordHistory.getId() > 0;
            printTestResult("testCreate", success);
            
            // Clean up
            if (success) {
                passwordHistoryDao.delete(createdPasswordHistory.getId());
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
            // Find all password history entries
            List<PasswordHistory> passwordHistories = passwordHistoryDao.findAll();
            
            // Verify there are password history entries
            if (passwordHistories.isEmpty()) {
                printTestResult("testFindById", false, "No password history entries found");
                return;
            }
            
            // Get the first password history entry
            PasswordHistory passwordHistory = passwordHistories.get(0);
            
            // Find the password history entry by ID
            java.util.Optional<PasswordHistory> foundPasswordHistory = passwordHistoryDao.findById(passwordHistory.getId());
            
            // Verify the password history entry was found
            boolean success = foundPasswordHistory.isPresent() && 
                              foundPasswordHistory.get().getId().equals(passwordHistory.getId()) &&
                              foundPasswordHistory.get().getPasswordHash().equals(passwordHistory.getPasswordHash());
            
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
            // Find all password history entries
            List<PasswordHistory> passwordHistories = passwordHistoryDao.findAll();
            
            // Verify there are password history entries
            boolean success = !passwordHistories.isEmpty();
            printTestResult("testFindAll", success, "Found " + passwordHistories.size() + " password history entries");
        } catch (Exception e) {
            printTestFailure("testFindAll", e);
        }
    }
    
    /**
     * Test the update method.
     */
    private void testUpdate() {
        try {
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
            boolean success = updatedPasswordHistory.getPasswordHash().equals("updatedhash");
            
            printTestResult("testUpdate", success);
            
            // Clean up
            passwordHistoryDao.delete(createdPasswordHistory.getId());
        } catch (Exception e) {
            printTestFailure("testUpdate", e);
        }
    }
    
    /**
     * Test the delete method.
     */
    private void testDelete() {
        try {
            // Create a new password history entry
            PasswordHistory passwordHistory = new PasswordHistory();
            passwordHistory.setUserId(999);
            passwordHistory.setPasswordHash("testhash3");
            passwordHistory.setCreatedAt(OffsetDateTime.now());
            
            PasswordHistory createdPasswordHistory = passwordHistoryDao.create(passwordHistory);
            
            // Delete the password history entry
            boolean deleted = passwordHistoryDao.delete(createdPasswordHistory.getId());
            
            // Verify the password history entry was deleted
            boolean success = deleted && !passwordHistoryDao.findById(createdPasswordHistory.getId()).isPresent();
            
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
            // Find password history entries by user ID
            List<PasswordHistory> passwordHistories = passwordHistoryDao.findByUserId(999);
            
            // Verify there are password history entries
            boolean success = !passwordHistories.isEmpty();
            printTestResult("testFindByUserId", success, 
                    "Found " + passwordHistories.size() + " password history entries for user ID 999");
        } catch (Exception e) {
            printTestFailure("testFindByUserId", e);
        }
    }
    
    /**
     * Test the findRecentByUserId method.
     */
    private void testFindRecentByUserId() {
        try {
            // Find recent password history entries by user ID
            List<PasswordHistory> passwordHistories = passwordHistoryDao.findRecentByUserId(999, 2);
            
            // Verify there are password history entries and the limit is respected
            boolean success = !passwordHistories.isEmpty() && passwordHistories.size() <= 2;
            printTestResult("testFindRecentByUserId", success, 
                    "Found " + passwordHistories.size() + " recent password history entries for user ID 999");
        } catch (Exception e) {
            printTestFailure("testFindRecentByUserId", e);
        }
    }
    
    /**
     * Test the existsByUserIdAndPasswordHash method.
     */
    private void testExistsByUserIdAndPasswordHash() {
        try {
            // Check if a password hash exists for a user
            boolean exists = passwordHistoryDao.existsByUserIdAndPasswordHash(999, "oldhash1");
            
            // Verify the password hash exists
            printTestResult("testExistsByUserIdAndPasswordHash", exists, 
                    "Password hash 'oldhash1' " + (exists ? "exists" : "does not exist") + " for user ID 999");
        } catch (Exception e) {
            printTestFailure("testExistsByUserIdAndPasswordHash", e);
        }
    }
    
    /**
     * Test the deleteByUserId method.
     */
    private void testDeleteByUserId() {
        try {
            // Create a new password history entry for a different user
            PasswordHistory passwordHistory = new PasswordHistory();
            passwordHistory.setUserId(998);
            passwordHistory.setPasswordHash("testhash4");
            passwordHistory.setCreatedAt(OffsetDateTime.now());
            
            PasswordHistory createdPasswordHistory = passwordHistoryDao.create(passwordHistory);
            
            // Delete password history entries by user ID
            int deleted = passwordHistoryDao.deleteByUserId(998);
            
            // Verify the password history entries were deleted
            boolean success = deleted > 0 && !passwordHistoryDao.findById(createdPasswordHistory.getId()).isPresent();
            
            printTestResult("testDeleteByUserId", success, 
                    "Deleted " + deleted + " password history entries for user ID 998");
        } catch (Exception e) {
            printTestFailure("testDeleteByUserId", e);
        }
    }
    
    /**
     * Test the deleteOlderThan method.
     */
    private void testDeleteOlderThan() {
        try {
            // Delete password history entries older than 15 days
            OffsetDateTime cutoffDate = OffsetDateTime.now().minusDays(15);
            int deleted = passwordHistoryDao.deleteOlderThan(cutoffDate);
            
            // Verify old password history entries were deleted
            boolean success = deleted > 0;
            printTestResult("testDeleteOlderThan", success, 
                    "Deleted " + deleted + " password history entries older than 15 days");
        } catch (Exception e) {
            printTestFailure("testDeleteOlderThan", e);
        }
    }
    
    /**
     * Test the deleteOldestByUserId method.
     */
    private void testDeleteOldestByUserId() {
        try {
            // Delete oldest password history entries, keeping only the most recent 2
            int deleted = passwordHistoryDao.deleteOldestByUserId(999, 2);
            
            // Verify old password history entries were deleted
            List<PasswordHistory> remainingEntries = passwordHistoryDao.findByUserId(999);
            boolean success = deleted > 0 && remainingEntries.size() <= 2;
            
            printTestResult("testDeleteOldestByUserId", success, 
                    "Deleted " + deleted + " oldest password history entries for user ID 999, " + 
                    remainingEntries.size() + " entries remaining");
        } catch (Exception e) {
            printTestFailure("testDeleteOldestByUserId", e);
        }
    }
}