package com.celestra.dao.impl;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import com.celestra.dao.BaseDaoTest;
import com.celestra.dao.UserLockoutDao;
import com.celestra.model.UserLockout;

/**
 * Test class for UserLockoutDaoImpl.
 */
public class UserLockoutDaoImplTest extends BaseDaoTest {
    
    private UserLockoutDao userLockoutDao;
    
    /**
     * Main method to run the tests.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        UserLockoutDaoImplTest test = new UserLockoutDaoImplTest();
        test.runTests();
    }
    
    /**
     * Constructor.
     */
    public UserLockoutDaoImplTest() {
        userLockoutDao = new UserLockoutDaoImpl();
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
            testFindActiveByUserId();
            testFindByUserId();
            testFindAllActive();
            testFindAllExpired();
            testFindAllPermanent();
            testFindAllTemporary();
            testUpdateLockoutEnd();
            testUpdateFailedAttempts();
            testDeleteExpired();
            testDeleteByUserId();
            
            tearDown();
            
            System.out.println("All tests completed.");
        } catch (Exception e) {
            System.err.println("Error running tests: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // User lockouts table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // Insert test user
        executeSQL("INSERT INTO users (id, company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES (999, 1, 'REGULAR_USER', 'testuser@test.com', 'Test User', 'hash123', 'ACTIVE', NOW(), NOW())");
        
        // Insert test lockouts
        executeSQL("INSERT INTO user_lockouts (user_id, lockout_start, lockout_end, failed_attempts, reason, created_at, updated_at) " +
                   "VALUES (999, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 3, 'Test temporary lockout', NOW(), NOW())");
        
        executeSQL("INSERT INTO user_lockouts (user_id, lockout_start, lockout_end, failed_attempts, reason, created_at, updated_at) " +
                   "VALUES (999, NOW(), NULL, 5, 'Test permanent lockout', NOW(), NOW())");
        
        executeSQL("INSERT INTO user_lockouts (user_id, lockout_start, lockout_end, failed_attempts, reason, created_at, updated_at) " +
                   "VALUES (999, NOW(), DATE_SUB(NOW(), INTERVAL 1 DAY), 2, 'Test expired lockout', NOW(), NOW())");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM user_lockouts WHERE reason LIKE 'Test%'");
        executeSQL("DELETE FROM users WHERE email = 'testuser@test.com'");
    }
    
    /**
     * Test the create method.
     */
    private void testCreate() {
        try {
            // Create a new user lockout
            UserLockout userLockout = new UserLockout();
            userLockout.setUserId(999);
            userLockout.setLockoutStart(OffsetDateTime.now());
            userLockout.setLockoutEnd(OffsetDateTime.now().plusHours(1));
            userLockout.setFailedAttempts(3);
            userLockout.setReason("Test create lockout");
            
            UserLockout createdUserLockout = userLockoutDao.create(userLockout);
            
            // Verify the user lockout was created
            boolean success = createdUserLockout.getId() > 0;
            printTestResult("testCreate", success);
            
            // Clean up
            if (success) {
                userLockoutDao.delete(createdUserLockout.getId());
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
            // Find all user lockouts
            List<UserLockout> userLockouts = userLockoutDao.findAll();
            
            // Verify there are user lockouts
            if (userLockouts.isEmpty()) {
                printTestResult("testFindById", false, "No user lockouts found");
                return;
            }
            
            // Get the first user lockout
            UserLockout userLockout = userLockouts.get(0);
            
            // Find the user lockout by ID
            Optional<UserLockout> foundUserLockout = userLockoutDao.findById(userLockout.getId());
            
            // Verify the user lockout was found
            boolean success = foundUserLockout.isPresent() && 
                              foundUserLockout.get().getId().equals(userLockout.getId()) &&
                              foundUserLockout.get().getUserId().equals(userLockout.getUserId());
            
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
            // Find all user lockouts
            List<UserLockout> userLockouts = userLockoutDao.findAll();
            
            // Verify there are user lockouts
            boolean success = !userLockouts.isEmpty();
            printTestResult("testFindAll", success, "Found " + userLockouts.size() + " user lockouts");
        } catch (Exception e) {
            printTestFailure("testFindAll", e);
        }
    }
    
    /**
     * Test the update method.
     */
    private void testUpdate() {
        try {
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
            boolean success = updatedUserLockout.getFailedAttempts() == 5 &&
                              updatedUserLockout.getReason().equals("Updated test lockout");
            
            printTestResult("testUpdate", success);
            
            // Clean up
            userLockoutDao.delete(createdUserLockout.getId());
        } catch (Exception e) {
            printTestFailure("testUpdate", e);
        }
    }
    
    /**
     * Test the delete method.
     */
    private void testDelete() {
        try {
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
            boolean success = deleted && !userLockoutDao.findById(createdUserLockout.getId()).isPresent();
            
            printTestResult("testDelete", success);
        } catch (Exception e) {
            printTestFailure("testDelete", e);
        }
    }
    
    /**
     * Test the findActiveByUserId method.
     */
    private void testFindActiveByUserId() {
        try {
            // Find active lockout by user ID
            Optional<UserLockout> userLockout = userLockoutDao.findActiveByUserId(999);
            
            // Verify the active lockout was found
            boolean success = userLockout.isPresent() && 
                              userLockout.get().getUserId() == 999 &&
                              userLockout.get().isActive();
            
            printTestResult("testFindActiveByUserId", success);
        } catch (Exception e) {
            printTestFailure("testFindActiveByUserId", e);
        }
    }
    
    /**
     * Test the findByUserId method.
     */
    private void testFindByUserId() {
        try {
            // Find lockouts by user ID
            List<UserLockout> userLockouts = userLockoutDao.findByUserId(999);
            
            // Verify there are lockouts
            boolean success = !userLockouts.isEmpty();
            printTestResult("testFindByUserId", success, 
                    "Found " + userLockouts.size() + " lockouts for user ID 999");
        } catch (Exception e) {
            printTestFailure("testFindByUserId", e);
        }
    }
    
    /**
     * Test the findAllActive method.
     */
    private void testFindAllActive() {
        try {
            // Find all active lockouts
            List<UserLockout> userLockouts = userLockoutDao.findAllActive();
            
            // Verify there are active lockouts
            boolean success = !userLockouts.isEmpty();
            printTestResult("testFindAllActive", success, 
                    "Found " + userLockouts.size() + " active lockouts");
        } catch (Exception e) {
            printTestFailure("testFindAllActive", e);
        }
    }
    
    /**
     * Test the findAllExpired method.
     */
    private void testFindAllExpired() {
        try {
            // Find all expired lockouts
            List<UserLockout> userLockouts = userLockoutDao.findAllExpired();
            
            // Verify there are expired lockouts
            boolean success = !userLockouts.isEmpty();
            printTestResult("testFindAllExpired", success, 
                    "Found " + userLockouts.size() + " expired lockouts");
        } catch (Exception e) {
            printTestFailure("testFindAllExpired", e);
        }
    }
    
    /**
     * Test the findAllPermanent method.
     */
    private void testFindAllPermanent() {
        try {
            // Find all permanent lockouts
            List<UserLockout> userLockouts = userLockoutDao.findAllPermanent();
            
            // Verify there are permanent lockouts
            boolean success = !userLockouts.isEmpty();
            printTestResult("testFindAllPermanent", success, 
                    "Found " + userLockouts.size() + " permanent lockouts");
        } catch (Exception e) {
            printTestFailure("testFindAllPermanent", e);
        }
    }
    
    /**
     * Test the findAllTemporary method.
     */
    private void testFindAllTemporary() {
        try {
            // Find all temporary lockouts
            List<UserLockout> userLockouts = userLockoutDao.findAllTemporary();
            
            // Verify there are temporary lockouts
            boolean success = !userLockouts.isEmpty();
            printTestResult("testFindAllTemporary", success, 
                    "Found " + userLockouts.size() + " temporary lockouts");
        } catch (Exception e) {
            printTestFailure("testFindAllTemporary", e);
        }
    }
    
    /**
     * Test the updateLockoutEnd method.
     */
    private void testUpdateLockoutEnd() {
        try {
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
            Optional<UserLockout> updatedUserLockout = userLockoutDao.findById(createdUserLockout.getId());
            boolean success = updated && 
                              updatedUserLockout.isPresent() && 
                              updatedUserLockout.get().getLockoutEnd().isAfter(OffsetDateTime.now().plusDays(6));
            
            printTestResult("testUpdateLockoutEnd", success);
            
            // Clean up
            userLockoutDao.delete(createdUserLockout.getId());
        } catch (Exception e) {
            printTestFailure("testUpdateLockoutEnd", e);
        }
    }
    
    /**
     * Test the updateFailedAttempts method.
     */
    private void testUpdateFailedAttempts() {
        try {
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
            Optional<UserLockout> updatedUserLockout = userLockoutDao.findById(createdUserLockout.getId());
            boolean success = updated && 
                              updatedUserLockout.isPresent() && 
                              updatedUserLockout.get().getFailedAttempts() == 10;
            
            printTestResult("testUpdateFailedAttempts", success);
            
            // Clean up
            userLockoutDao.delete(createdUserLockout.getId());
        } catch (Exception e) {
            printTestFailure("testUpdateFailedAttempts", e);
        }
    }
    
    /**
     * Test the deleteExpired method.
     */
    private void testDeleteExpired() {
        try {
            // Delete expired lockouts
            int deleted = userLockoutDao.deleteExpired();
            
            // Verify expired lockouts were deleted
            boolean success = deleted > 0;
            printTestResult("testDeleteExpired", success, "Deleted " + deleted + " expired lockouts");
        } catch (Exception e) {
            printTestFailure("testDeleteExpired", e);
        }
    }
    
    /**
     * Test the deleteByUserId method.
     */
    private void testDeleteByUserId() {
        try {
            // Create a new user lockout for a different user
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
            boolean success = deleted > 0 && !userLockoutDao.findById(createdUserLockout.getId()).isPresent();
            
            printTestResult("testDeleteByUserId", success, "Deleted " + deleted + " lockouts for user ID 998");
        } catch (Exception e) {
            printTestFailure("testDeleteByUserId", e);
        }
    }
}