package com.celestra.dao.impl;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import com.celestra.dao.BaseDaoTest;
import com.celestra.dao.FailedLoginDao;
import com.celestra.model.FailedLogin;

/**
 * Test class for FailedLoginDaoImpl.
 */
public class FailedLoginDaoImplTest extends BaseDaoTest {
    
    private FailedLoginDao failedLoginDao;
    
    /**
     * Main method to run the tests.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        FailedLoginDaoImplTest test = new FailedLoginDaoImplTest();
        test.runTests();
    }
    
    /**
     * Constructor.
     */
    public FailedLoginDaoImplTest() {
        failedLoginDao = new FailedLoginDaoImpl();
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
            testFindByUsername();
            testFindByIpAddress();
            testFindByUsernameAndIpAddress();
            testFindRecentByUsername();
            testCountRecentByUsername();
            testCountRecentByIpAddress();
            testDeleteOlderThan();
            
            tearDown();
            
            System.out.println("All tests completed.");
        } catch (Exception e) {
            System.err.println("Error running tests: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // Failed logins table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // Insert test data
        executeSQL("INSERT INTO users (id, company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES (999, 1, 'REGULAR_USER', 'testuser@test.com', 'Test User', 'hash123', 'ACTIVE', NOW(), NOW())");
        
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
    }
    
    /**
     * Test the create method.
     */
    private void testCreate() {
        try {
            // Create a new failed login
            FailedLogin failedLogin = new FailedLogin();
            failedLogin.setIpAddress("192.168.1.100");
            failedLogin.setFailureReason("Test failure reason");
            failedLogin.setAttemptedAt(OffsetDateTime.now());
            
            FailedLogin createdFailedLogin = failedLoginDao.create(failedLogin);
            
            // Verify the failed login was created
            boolean success = createdFailedLogin.getId() > 0;
            printTestResult("testCreate", success);
            
            // Clean up
            if (success) {
                failedLoginDao.delete(createdFailedLogin.getId());
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
            // Find all failed logins
            List<FailedLogin> failedLogins = failedLoginDao.findAll();
            
            // Verify there are failed logins
            if (failedLogins.isEmpty()) {
                printTestResult("testFindById", false, "No failed logins found");
                return;
            }
            
            // Get the first failed login
            FailedLogin failedLogin = failedLogins.get(0);
            
            // Find the failed login by ID
            Optional<FailedLogin> foundFailedLogin = failedLoginDao.findById(failedLogin.getId());
            
            // Verify the failed login was found
            boolean success = foundFailedLogin.isPresent() && 
                              foundFailedLogin.get().getId().equals(failedLogin.getId()) &&
                              foundFailedLogin.get().getIpAddress().equals(failedLogin.getIpAddress());
            
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
            // Find all failed logins
            List<FailedLogin> failedLogins = failedLoginDao.findAll();
            
            // Verify there are failed logins
            boolean success = !failedLogins.isEmpty();
            printTestResult("testFindAll", success, "Found " + failedLogins.size() + " failed logins");
        } catch (Exception e) {
            printTestFailure("testFindAll", e);
        }
    }
    
    /**
     * Test the update method.
     */
    private void testUpdate() {
        try {
            // Create a new failed login
            FailedLogin failedLogin = new FailedLogin();
            failedLogin.setIpAddress("192.168.1.101");
            failedLogin.setFailureReason("Test update reason");
            failedLogin.setAttemptedAt(OffsetDateTime.now());
            
            FailedLogin createdFailedLogin = failedLoginDao.create(failedLogin);
            
            // Update the failed login
            createdFailedLogin.setIpAddress("192.168.1.102");
            createdFailedLogin.setFailureReason("Updated test reason");
            
            FailedLogin updatedFailedLogin = failedLoginDao.update(createdFailedLogin);
            
            // Verify the failed login was updated
            boolean success = updatedFailedLogin.getIpAddress().equals("192.168.1.102") &&
                              updatedFailedLogin.getFailureReason().equals("Updated test reason");
            
            printTestResult("testUpdate", success);
            
            // Clean up
            failedLoginDao.delete(createdFailedLogin.getId());
        } catch (Exception e) {
            printTestFailure("testUpdate", e);
        }
    }
    
    /**
     * Test the delete method.
     */
    private void testDelete() {
        try {
            // Create a new failed login
            FailedLogin failedLogin = new FailedLogin();
            failedLogin.setIpAddress("192.168.1.103");
            failedLogin.setFailureReason("Test delete reason");
            failedLogin.setAttemptedAt(OffsetDateTime.now());
            
            FailedLogin createdFailedLogin = failedLoginDao.create(failedLogin);
            
            // Delete the failed login
            boolean deleted = failedLoginDao.delete(createdFailedLogin.getId());
            
            // Verify the failed login was deleted
            boolean success = deleted && !failedLoginDao.findById(createdFailedLogin.getId()).isPresent();
            
            printTestResult("testDelete", success);
        } catch (Exception e) {
            printTestFailure("testDelete", e);
        }
    }
    
    /**
     * Test the findByUsername method.
     */
    private void testFindByUsername() {
        try {
            // Find failed logins by username
            List<FailedLogin> failedLogins = failedLoginDao.findByUsername("testuser@test.com");
            
            // Verify there are failed logins
            boolean success = !failedLogins.isEmpty();
            printTestResult("testFindByUsername", success, 
                    "Found " + failedLogins.size() + " failed logins for username 'testuser@test.com'");
        } catch (Exception e) {
            printTestFailure("testFindByUsername", e);
        }
    }
    
    /**
     * Test the findByIpAddress method.
     */
    private void testFindByIpAddress() {
        try {
            // Find failed logins by IP address
            List<FailedLogin> failedLogins = failedLoginDao.findByIpAddress("192.168.1.1");
            
            // Verify there are failed logins
            boolean success = !failedLogins.isEmpty();
            printTestResult("testFindByIpAddress", success, 
                    "Found " + failedLogins.size() + " failed logins for IP address '192.168.1.1'");
        } catch (Exception e) {
            printTestFailure("testFindByIpAddress", e);
        }
    }
    
    /**
     * Test the findByUsernameAndIpAddress method.
     */
    private void testFindByUsernameAndIpAddress() {
        try {
            // Find failed logins by username and IP address
            List<FailedLogin> failedLogins = failedLoginDao.findByUsernameAndIpAddress("testuser@test.com", "192.168.1.1");
            
            // Verify there are failed logins
            boolean success = !failedLogins.isEmpty();
            printTestResult("testFindByUsernameAndIpAddress", success, 
                    "Found " + failedLogins.size() + " failed logins for username 'testuser@test.com' and IP address '192.168.1.1'");
        } catch (Exception e) {
            printTestFailure("testFindByUsernameAndIpAddress", e);
        }
    }
    
    /**
     * Test the findRecentByUsername method.
     */
    private void testFindRecentByUsername() {
        try {
            // Find recent failed logins by username
            List<FailedLogin> failedLogins = failedLoginDao.findRecentByUsername("testuser@test.com", 60);
            
            // Verify there are failed logins
            boolean success = !failedLogins.isEmpty();
            printTestResult("testFindRecentByUsername", success, 
                    "Found " + failedLogins.size() + " recent failed logins for username 'testuser@test.com'");
        } catch (Exception e) {
            printTestFailure("testFindRecentByUsername", e);
        }
    }
    
    /**
     * Test the countRecentByUsername method.
     */
    private void testCountRecentByUsername() {
        try {
            // Count recent failed logins by username
            int count = failedLoginDao.countRecentByUsername("testuser@test.com", 60);
            
            // Verify there are failed logins
            boolean success = count > 0;
            printTestResult("testCountRecentByUsername", success, 
                    "Found " + count + " recent failed logins for username 'testuser@test.com'");
        } catch (Exception e) {
            printTestFailure("testCountRecentByUsername", e);
        }
    }
    
    /**
     * Test the countRecentByIpAddress method.
     */
    private void testCountRecentByIpAddress() {
        try {
            // Count recent failed logins by IP address
            int count = failedLoginDao.countRecentByIpAddress("192.168.1.1", 60);
            
            // Verify there are failed logins
            boolean success = count > 0;
            printTestResult("testCountRecentByIpAddress", success, 
                    "Found " + count + " recent failed logins for IP address '192.168.1.1'");
        } catch (Exception e) {
            printTestFailure("testCountRecentByIpAddress", e);
        }
    }
    
    /**
     * Test the deleteOlderThan method.
     */
    private void testDeleteOlderThan() {
        try {
            // Create a new failed login with a timestamp in the past
            FailedLogin failedLogin = new FailedLogin();
            failedLogin.setIpAddress("192.168.1.104");
            failedLogin.setFailureReason("Test old record");
            
            // Set timestamp to 10 days ago
            OffsetDateTime oldTimestamp = OffsetDateTime.now().minusDays(10);
            failedLogin.setAttemptedAt(oldTimestamp);
            
            FailedLogin createdFailedLogin = failedLoginDao.create(failedLogin);
            
            // Delete failed logins older than 5 days
            int deleted = failedLoginDao.deleteOlderThan(5);
            
            // Verify the old failed login was deleted
            boolean success = deleted > 0 && !failedLoginDao.findById(createdFailedLogin.getId()).isPresent();
            
            printTestResult("testDeleteOlderThan", success, "Deleted " + deleted + " old failed logins");
        } catch (Exception e) {
            printTestFailure("testDeleteOlderThan", e);
        }
    }
}