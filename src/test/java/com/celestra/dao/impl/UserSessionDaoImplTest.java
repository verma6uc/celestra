package com.celestra.dao.impl;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.celestra.dao.BaseDaoTest;
import com.celestra.dao.UserSessionDao;
import com.celestra.model.UserSession;

/**
 * Test class for UserSessionDaoImpl.
 */
public class UserSessionDaoImplTest extends BaseDaoTest {
    
    private UserSessionDao userSessionDao;
    
    /**
     * Initialize the DAO before each test.
     */
    @Before
    public void initialize() {
        userSessionDao = new UserSessionDaoImpl();
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
    @Test
    public void testCreate() throws SQLException {
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
        assertNotNull("Created user session should not be null", createdUserSession);
        assertTrue("Created user session should have an ID", createdUserSession.getId() > 0);
        
        // Clean up
        boolean deleted = userSessionDao.delete(createdUserSession.getId());
        assertTrue("User session should be deleted successfully", deleted);
    }
    
    /**
     * Test the findById method.
     */
    @Test
    public void testFindById() throws SQLException {
        // Find all user sessions
        List<UserSession> userSessions = userSessionDao.findAll();
        
        // Verify there are user sessions
        assertFalse("There should be user sessions in the database", userSessions.isEmpty());
        
        // Get the first user session
        UserSession userSession = userSessions.get(0);
        
        // Find the user session by ID
        Optional<UserSession> foundUserSession = userSessionDao.findById(userSession.getId());
        
        // Verify the user session was found
        assertTrue("User session should be found by ID", foundUserSession.isPresent());
        assertEquals("Found user session ID should match", userSession.getId(), foundUserSession.get().getId());
        assertEquals("Found user session token should match", userSession.getSessionToken(), foundUserSession.get().getSessionToken());
    }
    
    /**
     * Test the findAll method.
     */
    @Test
    public void testFindAll() throws SQLException {
        // Find all user sessions
        List<UserSession> userSessions = userSessionDao.findAll();
        
        // Verify there are user sessions
        assertFalse("There should be user sessions in the database", userSessions.isEmpty());
        assertTrue("There should be at least 3 user sessions", userSessions.size() >= 3);
    }
    
    /**
     * Test the update method.
     */
    @Test
    public void testUpdate() throws SQLException {
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
        assertEquals("User session IP address should be updated", "192.168.1.102", updatedUserSession.getIpAddress());
        assertEquals("User session user agent should be updated", "Updated User Agent", updatedUserSession.getUserAgent());
        
        // Clean up
        boolean deleted = userSessionDao.delete(createdUserSession.getId());
        assertTrue("User session should be deleted successfully", deleted);
    }
    
    /**
     * Test the delete method.
     */
    @Test
    public void testDelete() throws SQLException {
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
        assertTrue("User session should be deleted successfully", deleted);
        
        Optional<UserSession> foundUserSession = userSessionDao.findById(createdUserSession.getId());
        assertFalse("User session should not be found after deletion", foundUserSession.isPresent());
    }
    
    /**
     * Test the findBySessionToken method.
     */
    @Test
    public void testFindBySessionToken() throws SQLException {
        // Find user session by token
        Optional<UserSession> userSession = userSessionDao.findBySessionToken("active-token-1");
        
        // Verify the user session was found
        assertTrue("User session should be found by token", userSession.isPresent());
        assertEquals("Found user session token should match", "active-token-1", userSession.get().getSessionToken());
    }
    
    /**
     * Test the findByUserId method.
     */
    @Test
    public void testFindByUserId() throws SQLException {
        // Find user sessions by user ID
        List<UserSession> userSessions = userSessionDao.findByUserId(999);
        
        // Verify there are user sessions
        assertFalse("There should be user sessions for user ID 999", userSessions.isEmpty());
        
        // Verify all user sessions have the correct user ID
        for (UserSession userSession : userSessions) {
            assertEquals("User session user ID should be 999", Integer.valueOf(999), userSession.getUserId());
        }
    }
    
    /**
     * Test the findActiveByUserId method.
     */
    @Test
    public void testFindActiveByUserId() throws SQLException {
        // Find active user sessions by user ID
        List<UserSession> userSessions = userSessionDao.findActiveByUserId(999);
        
        // Verify there are active user sessions
        assertFalse("There should be active user sessions for user ID 999", userSessions.isEmpty());
        
        // Verify all user sessions are active
        for (UserSession userSession : userSessions) {
            assertTrue("User session should be active", userSession.isActive());
        }
    }
    
    /**
     * Test the findByIpAddress method.
     */
    @Test
    public void testFindByIpAddress() throws SQLException {
        // Find user sessions by IP address
        List<UserSession> userSessions = userSessionDao.findByIpAddress("192.168.1.1");
        
        // Verify there are user sessions
        assertFalse("There should be user sessions for IP address 192.168.1.1", userSessions.isEmpty());
        
        // Verify all user sessions have the correct IP address
        for (UserSession userSession : userSessions) {
            assertEquals("User session IP address should be 192.168.1.1", "192.168.1.1", userSession.getIpAddress());
        }
    }
    
    /**
     * Test the findAllActive method.
     */
    @Test
    public void testFindAllActive() throws SQLException {
        // Find all active user sessions
        List<UserSession> userSessions = userSessionDao.findAllActive();
        
        // Verify there are active user sessions
        assertFalse("There should be active user sessions", userSessions.isEmpty());
        
        // Verify all user sessions are active
        for (UserSession userSession : userSessions) {
            assertTrue("User session should be active", userSession.isActive());
        }
    }
    
    /**
     * Test the findAllExpired method.
     */
    @Test
    public void testFindAllExpired() throws SQLException {
        // Find all expired user sessions
        List<UserSession> userSessions = userSessionDao.findAllExpired();
        
        // Verify there are expired user sessions
        assertFalse("There should be expired user sessions", userSessions.isEmpty());
        
        // Verify all user sessions are expired
        for (UserSession userSession : userSessions) {
            assertTrue("User session should be expired", userSession.isExpired());
        }
    }
    
    /**
     * Test the updateExpiresAt method.
     */
    @Test
    public void testUpdateExpiresAt() throws SQLException {
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
        assertTrue("User session expiration time should be updated successfully", updated);
        
        Optional<UserSession> updatedUserSession = userSessionDao.findById(createdUserSession.getId());
        assertTrue("User session should be found after expiration update", updatedUserSession.isPresent());
        assertTrue("User session expiration time should be updated", 
                   updatedUserSession.get().getExpiresAt().isAfter(OffsetDateTime.now().plusDays(6)));
        
        // Clean up
        boolean deleted = userSessionDao.delete(createdUserSession.getId());
        assertTrue("User session should be deleted successfully", deleted);
    }
    
    /**
     * Test the deleteExpired method.
     */
    @Test
    public void testDeleteExpired() throws SQLException {
        // Delete expired user sessions
        int deleted = userSessionDao.deleteExpired();
        
        // Verify expired user sessions were deleted
        assertTrue("At least one expired user session should be deleted", deleted > 0);
        
        // Verify no expired sessions remain
        List<UserSession> expiredSessions = userSessionDao.findAllExpired();
        assertTrue("No expired sessions should remain", expiredSessions.isEmpty());
    }
    
    /**
     * Test the deleteByUserId method.
     */
    @Test
    public void testDeleteByUserId() throws SQLException {
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
        assertTrue("At least one user session should be deleted", deleted > 0);
        
        Optional<UserSession> foundUserSession = userSessionDao.findById(createdUserSession.getId());
        assertFalse("User session should not be found after deletion", foundUserSession.isPresent());
    }
    
    /**
     * Test the deleteOtherSessionsForUser method.
     */
    @Test
    public void testDeleteOtherSessionsForUser() throws SQLException {
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
        assertTrue("At least one other session should be deleted", deleted > 0);
        
        Optional<UserSession> foundUserSession1 = userSessionDao.findById(createdUserSession1.getId());
        assertTrue("Current user session should still exist", foundUserSession1.isPresent());
        
        Optional<UserSession> foundUserSession2 = userSessionDao.findById(createdUserSession2.getId());
        assertFalse("Other user session should be deleted", foundUserSession2.isPresent());
        
        // Clean up
        userSessionDao.delete(createdUserSession1.getId());
    }
}