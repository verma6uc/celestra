package com.celestra.dao.impl;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.celestra.dao.BaseDaoTest;
import com.celestra.dao.UserSessionDao;
import com.celestra.model.UserSession;

/**
 * Test class for UserSessionDaoImpl.
 */
public class UserSessionDaoImplTest extends BaseDaoTest {
    
    private UserSessionDao userSessionDao;
    
    /**
     * Main method to run the tests.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        UserSessionDaoImplTest test = new UserSessionDaoImplTest();
        test.runTests();
    }
    
    /**
     * Constructor.
     */
    public UserSessionDaoImplTest() {
        userSessionDao = new UserSessionDaoImpl();
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
            testFindBySessionToken();
            testFindByUserId();
            testFindActiveByUserId();
            testFindByIpAddress();
            testFindAllActive();
            testFindAllExpired();
            testUpdateExpiresAt();
            testDeleteExpired();
            testDeleteByUserId();
            testDeleteOtherSessionsForUser();
            
            tearDown();
            
            System.out.println("All tests completed.");
        } catch (Exception e) {
            System.err.println("Error running tests: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // User sessions table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // Insert test user
        executeSQL("INSERT INTO users (id, company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES (999, 1, 'REGULAR_USER', 'testuser@test.com', 'Test User', 'hash123', 'ACTIVE', NOW(), NOW())");
        
        // Insert test sessions
        executeSQL("INSERT INTO user_sessions (user_id, session_token, ip_address, user_agent, created_at, expires_at) " +
                   "VALUES (999, 'active-token-1', '192.168.1.1', 'Test User Agent 1', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY))");
        
        executeSQL("INSERT INTO user_sessions (user_id, session_token, ip_address, user_agent, created_at, expires_at) " +
                   "VALUES (999, 'active-token-2', '192.168.1.2', 'Test User Agent 2', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY))");
        
        executeSQL("INSERT INTO user_sessions (user_id, session_token, ip_address, user_agent, created_at, expires_at) " +
                   "VALUES (999, 'expired-token', '192.168.1.3', 'Test User Agent 3', NOW(), DATE_SUB(NOW(), INTERVAL 1 DAY))");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM user_sessions WHERE session_token LIKE 'test-%' OR " +
                   "session_token LIKE 'active-token-%' OR session_token = 'expired-token'");
        executeSQL("DELETE FROM users WHERE email = 'testuser@test.com'");
    }
    
    /**
     * Test the create method.
     */
    private void testCreate() {
        try {
            // Create a new user session
            UserSession userSession = new UserSession();
            userSession.setUserId(999);
            userSession.setSessionToken("test-token-" + UUID.randomUUID().toString());
            userSession.setIpAddress("192.168.1.100");
            userSession.setUserAgent("Test User Agent");
            userSession.setCreatedAt(OffsetDateTime.now());
            userSession.setExpiresAt(OffsetDateTime.now().plusDays(1));
            
            UserSession createdUserSession = userSessionDao.create(userSession);
            
            // Verify the user session was created
            boolean success = createdUserSession.getId() > 0;
            printTestResult("testCreate", success);
            
            // Clean up
            if (success) {
                userSessionDao.delete(createdUserSession.getId());
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
            // Find all user sessions
            List<UserSession> userSessions = userSessionDao.findAll();
            
            // Verify there are user sessions
            if (userSessions.isEmpty()) {
                printTestResult("testFindById", false, "No user sessions found");
                return;
            }
            
            // Get the first user session
            UserSession userSession = userSessions.get(0);
            
            // Find the user session by ID
            Optional<UserSession> foundUserSession = userSessionDao.findById(userSession.getId());
            
            // Verify the user session was found
            boolean success = foundUserSession.isPresent() && 
                              foundUserSession.get().getId().equals(userSession.getId()) &&
                              foundUserSession.get().getSessionToken().equals(userSession.getSessionToken());
            
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
            // Find all user sessions
            List<UserSession> userSessions = userSessionDao.findAll();
            
            // Verify there are user sessions
            boolean success = !userSessions.isEmpty();
            printTestResult("testFindAll", success, "Found " + userSessions.size() + " user sessions");
        } catch (Exception e) {
            printTestFailure("testFindAll", e);
        }
    }
    
    /**
     * Test the update method.
     */
    private void testUpdate() {
        try {
            // Create a new user session
            UserSession userSession = new UserSession();
            userSession.setUserId(999);
            userSession.setSessionToken("test-token-" + UUID.randomUUID().toString());
            userSession.setIpAddress("192.168.1.101");
            userSession.setUserAgent("Test User Agent Update");
            userSession.setCreatedAt(OffsetDateTime.now());
            userSession.setExpiresAt(OffsetDateTime.now().plusDays(1));
            
            UserSession createdUserSession = userSessionDao.create(userSession);
            
            // Update the user session
            createdUserSession.setIpAddress("192.168.1.102");
            createdUserSession.setUserAgent("Updated User Agent");
            
            UserSession updatedUserSession = userSessionDao.update(createdUserSession);
            
            // Verify the user session was updated
            boolean success = updatedUserSession.getIpAddress().equals("192.168.1.102") &&
                              updatedUserSession.getUserAgent().equals("Updated User Agent");
            
            printTestResult("testUpdate", success);
            
            // Clean up
            userSessionDao.delete(createdUserSession.getId());
        } catch (Exception e) {
            printTestFailure("testUpdate", e);
        }
    }
    
    /**
     * Test the delete method.
     */
    private void testDelete() {
        try {
            // Create a new user session
            UserSession userSession = new UserSession();
            userSession.setUserId(999);
            userSession.setSessionToken("test-token-" + UUID.randomUUID().toString());
            userSession.setIpAddress("192.168.1.103");
            userSession.setUserAgent("Test User Agent Delete");
            userSession.setCreatedAt(OffsetDateTime.now());
            userSession.setExpiresAt(OffsetDateTime.now().plusDays(1));
            
            UserSession createdUserSession = userSessionDao.create(userSession);
            
            // Delete the user session
            boolean deleted = userSessionDao.delete(createdUserSession.getId());
            
            // Verify the user session was deleted
            boolean success = deleted && !userSessionDao.findById(createdUserSession.getId()).isPresent();
            
            printTestResult("testDelete", success);
        } catch (Exception e) {
            printTestFailure("testDelete", e);
        }
    }
    
    /**
     * Test the findBySessionToken method.
     */
    private void testFindBySessionToken() {
        try {
            // Find user session by token
            Optional<UserSession> userSession = userSessionDao.findBySessionToken("active-token-1");
            
            // Verify the user session was found
            boolean success = userSession.isPresent() && 
                              userSession.get().getSessionToken().equals("active-token-1");
            
            printTestResult("testFindBySessionToken", success);
        } catch (Exception e) {
            printTestFailure("testFindBySessionToken", e);
        }
    }
    
    /**
     * Test the findByUserId method.
     */
    private void testFindByUserId() {
        try {
            // Find user sessions by user ID
            List<UserSession> userSessions = userSessionDao.findByUserId(999);
            
            // Verify there are user sessions
            boolean success = !userSessions.isEmpty();
            printTestResult("testFindByUserId", success, 
                    "Found " + userSessions.size() + " user sessions for user ID 999");
        } catch (Exception e) {
            printTestFailure("testFindByUserId", e);
        }
    }
    
    /**
     * Test the findActiveByUserId method.
     */
    private void testFindActiveByUserId() {
        try {
            // Find active user sessions by user ID
            List<UserSession> userSessions = userSessionDao.findActiveByUserId(999);
            
            // Verify there are active user sessions
            boolean success = !userSessions.isEmpty();
            printTestResult("testFindActiveByUserId", success, 
                    "Found " + userSessions.size() + " active user sessions for user ID 999");
        } catch (Exception e) {
            printTestFailure("testFindActiveByUserId", e);
        }
    }
    
    /**
     * Test the findByIpAddress method.
     */
    private void testFindByIpAddress() {
        try {
            // Find user sessions by IP address
            List<UserSession> userSessions = userSessionDao.findByIpAddress("192.168.1.1");
            
            // Verify there are user sessions
            boolean success = !userSessions.isEmpty();
            printTestResult("testFindByIpAddress", success, 
                    "Found " + userSessions.size() + " user sessions for IP address 192.168.1.1");
        } catch (Exception e) {
            printTestFailure("testFindByIpAddress", e);
        }
    }
    
    /**
     * Test the findAllActive method.
     */
    private void testFindAllActive() {
        try {
            // Find all active user sessions
            List<UserSession> userSessions = userSessionDao.findAllActive();
            
            // Verify there are active user sessions
            boolean success = !userSessions.isEmpty();
            printTestResult("testFindAllActive", success, 
                    "Found " + userSessions.size() + " active user sessions");
        } catch (Exception e) {
            printTestFailure("testFindAllActive", e);
        }
    }
    
    /**
     * Test the findAllExpired method.
     */
    private void testFindAllExpired() {
        try {
            // Find all expired user sessions
            List<UserSession> userSessions = userSessionDao.findAllExpired();
            
            // Verify there are expired user sessions
            boolean success = !userSessions.isEmpty();
            printTestResult("testFindAllExpired", success, 
                    "Found " + userSessions.size() + " expired user sessions");
        } catch (Exception e) {
            printTestFailure("testFindAllExpired", e);
        }
    }
    
    /**
     * Test the updateExpiresAt method.
     */
    private void testUpdateExpiresAt() {
        try {
            // Create a new user session
            UserSession userSession = new UserSession();
            userSession.setUserId(999);
            userSession.setSessionToken("test-token-" + UUID.randomUUID().toString());
            userSession.setIpAddress("192.168.1.104");
            userSession.setUserAgent("Test User Agent Expires");
            userSession.setCreatedAt(OffsetDateTime.now());
            userSession.setExpiresAt(OffsetDateTime.now().plusHours(1));
            
            UserSession createdUserSession = userSessionDao.create(userSession);
            
            // Update the expiration time
            OffsetDateTime newExpiresAt = OffsetDateTime.now().plusDays(7);
            boolean updated = userSessionDao.updateExpiresAt(createdUserSession.getId(), newExpiresAt);
            
            // Verify the expiration time was updated
            Optional<UserSession> updatedUserSession = userSessionDao.findById(createdUserSession.getId());
            boolean success = updated && 
                              updatedUserSession.isPresent() && 
                              updatedUserSession.get().getExpiresAt().isAfter(OffsetDateTime.now().plusDays(6));
            
            printTestResult("testUpdateExpiresAt", success);
            
            // Clean up
            userSessionDao.delete(createdUserSession.getId());
        } catch (Exception e) {
            printTestFailure("testUpdateExpiresAt", e);
        }
    }
    
    /**
     * Test the deleteExpired method.
     */
    private void testDeleteExpired() {
        try {
            // Delete expired user sessions
            int deleted = userSessionDao.deleteExpired();
            
            // Verify expired user sessions were deleted
            boolean success = deleted > 0;
            printTestResult("testDeleteExpired", success, "Deleted " + deleted + " expired user sessions");
        } catch (Exception e) {
            printTestFailure("testDeleteExpired", e);
        }
    }
    
    /**
     * Test the deleteByUserId method.
     */
    private void testDeleteByUserId() {
        try {
            // Create a new user session for a different user
            UserSession userSession = new UserSession();
            userSession.setUserId(998);
            userSession.setSessionToken("test-token-" + UUID.randomUUID().toString());
            userSession.setIpAddress("192.168.1.105");
            userSession.setUserAgent("Test User Agent Delete By User");
            userSession.setCreatedAt(OffsetDateTime.now());
            userSession.setExpiresAt(OffsetDateTime.now().plusDays(1));
            
            UserSession createdUserSession = userSessionDao.create(userSession);
            
            // Delete user sessions by user ID
            int deleted = userSessionDao.deleteByUserId(998);
            
            // Verify user sessions were deleted
            boolean success = deleted > 0 && !userSessionDao.findById(createdUserSession.getId()).isPresent();
            
            printTestResult("testDeleteByUserId", success, "Deleted " + deleted + " user sessions for user ID 998");
        } catch (Exception e) {
            printTestFailure("testDeleteByUserId", e);
        }
    }
    
    /**
     * Test the deleteOtherSessionsForUser method.
     */
    private void testDeleteOtherSessionsForUser() {
        try {
            // Create multiple user sessions for the same user
            UserSession userSession1 = new UserSession();
            userSession1.setUserId(997);
            userSession1.setSessionToken("test-token-" + UUID.randomUUID().toString());
            userSession1.setIpAddress("192.168.1.106");
            userSession1.setUserAgent("Test User Agent 1");
            userSession1.setCreatedAt(OffsetDateTime.now());
            userSession1.setExpiresAt(OffsetDateTime.now().plusDays(1));
            
            UserSession createdUserSession1 = userSessionDao.create(userSession1);
            
            UserSession userSession2 = new UserSession();
            userSession2.setUserId(997);
            userSession2.setSessionToken("test-token-" + UUID.randomUUID().toString());
            userSession2.setIpAddress("192.168.1.107");
            userSession2.setUserAgent("Test User Agent 2");
            userSession2.setCreatedAt(OffsetDateTime.now());
            userSession2.setExpiresAt(OffsetDateTime.now().plusDays(1));
            
            UserSession createdUserSession2 = userSessionDao.create(userSession2);
            
            // Delete other sessions for user
            int deleted = userSessionDao.deleteOtherSessionsForUser(997, createdUserSession1.getId());
            
            // Verify other sessions were deleted
            boolean success = deleted > 0 && 
                              userSessionDao.findById(createdUserSession1.getId()).isPresent() && 
                              !userSessionDao.findById(createdUserSession2.getId()).isPresent();
            
            printTestResult("testDeleteOtherSessionsForUser", success, 
                    "Deleted " + deleted + " other sessions for user ID 997");
            
            // Clean up
            userSessionDao.delete(createdUserSession1.getId());
        } catch (Exception e) {
            printTestFailure("testDeleteOtherSessionsForUser", e);
        }
    }
}