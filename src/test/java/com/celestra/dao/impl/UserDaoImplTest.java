package com.celestra.dao.impl;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.celestra.dao.BaseDaoTest;
import com.celestra.dao.UserDao;
import com.celestra.enums.UserRole;
import com.celestra.enums.UserStatus;
import com.celestra.model.User;

/**
 * Test class for UserDaoImpl.
 */
public class UserDaoImplTest extends BaseDaoTest {
    
    private UserDao userDao;
    
    /**
     * Initialize the DAO before each test.
     */
    @Before
    public void initialize() {
        userDao = new UserDaoImpl();
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // Users table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // Insert test companies first (to satisfy foreign key constraints)
        executeSQL("INSERT INTO companies (name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES ('Test Company 1', 'Test Company Description 1', 'SMALL'::company_size, 'TECH'::company_vertical, 'ACTIVE'::company_status, NOW(), NOW()) RETURNING id");
        
        executeSQL("INSERT INTO companies (name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES ('Test Company 2', 'Test Company Description 2', 'MEDIUM'::company_size, 'PHARMACEUTICAL'::company_vertical, 'ACTIVE'::company_status, NOW(), NOW()) RETURNING id");
        
        // Insert test users
        executeSQL("INSERT INTO users (company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES ((SELECT id FROM companies WHERE name = 'Test Company 1'), 'COMPANY_ADMIN'::user_role, 'admin@test.com', 'Test Admin', 'hash123', 'ACTIVE'::user_status, NOW(), NOW())");
        
        executeSQL("INSERT INTO users (company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES ((SELECT id FROM companies WHERE name = 'Test Company 1'), 'REGULAR_USER'::user_role, 'user@test.com', 'Test User', 'hash456', 'ACTIVE'::user_status, NOW(), NOW())");
        
        executeSQL("INSERT INTO users (company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES ((SELECT id FROM companies WHERE name = 'Test Company 2'), 'COMPANY_ADMIN'::user_role, 'admin2@test.com', 'Another Admin', 'hash789', 'SUSPENDED'::user_status, NOW(), NOW())");
        
        // Insert test user lockout
        executeSQL("INSERT INTO user_lockouts (user_id, lockout_start, lockout_end, failed_attempts, reason) " +
                   "VALUES ((SELECT id FROM users WHERE email = 'admin2@test.com'), NOW(), NOW() + INTERVAL '1 hour', 5, 'Test lockout')");
        
        // Clean up any existing password history entries
        executeSQL("DELETE FROM password_history WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%@test.com')");
        
        // Insert test password history
        executeSQL("INSERT INTO password_history (user_id, password_hash, created_at) " +
                   "VALUES ((SELECT id FROM users WHERE email = 'admin@test.com'), 'oldhash1', NOW() - INTERVAL '30 days')");
        executeSQL("INSERT INTO password_history (user_id, password_hash, created_at) " +
                   "VALUES ((SELECT id FROM users WHERE email = 'admin@test.com'), 'oldhash2', NOW() - INTERVAL '60 days')");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM password_history WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%@test.com')");
        executeSQL("DELETE FROM user_lockouts WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%@test.com')");
        executeSQL("DELETE FROM users WHERE email LIKE '%@test.com'");
        executeSQL("DELETE FROM companies WHERE name LIKE 'Test Company%'");
    }
    
    /**
     * Test the create method.
     */
    @Test
    public void testCreate() throws SQLException {
        // Create a new user
        User user = new User();
        user.setCompanyId(getCompanyId("Test Company 1"));
        user.setRole(UserRole.REGULAR_USER);
        user.setEmail("newuser@test.com");
        user.setName("New Test User");
        user.setPasswordHash("newhash123");
        user.setStatus(UserStatus.ACTIVE);
        
        User createdUser = userDao.create(user);
        
        // Verify the user was created
        assertNotNull("Created user should not be null", createdUser);
        assertTrue("Created user should have an ID", createdUser.getId() > 0);
        
        // Clean up
        boolean deleted = userDao.delete(createdUser.getId());
        assertTrue("User should be deleted successfully", deleted);
    }
    
    /**
     * Test the findById method.
     */
    @Test
    public void testFindById() throws SQLException {
        // Find all users
        List<User> users = userDao.findAll();
        
        // Verify there are users
        assertFalse("There should be users in the database", users.isEmpty());
        
        // Get the first user
        User user = users.get(0);
        
        // Find the user by ID
        Optional<User> foundUser = userDao.findById(user.getId());
        
        // Verify the user was found
        assertTrue("User should be found by ID", foundUser.isPresent());
        assertEquals("Found user ID should match", user.getId(), foundUser.get().getId());
        assertEquals("Found user email should match", user.getEmail(), foundUser.get().getEmail());
    }
    
    /**
     * Test the findAll method.
     */
    @Test
    public void testFindAll() throws SQLException {
        // Find all users
        List<User> users = userDao.findAll();
        
        // Verify there are users
        assertFalse("There should be users in the database", users.isEmpty());
    }
    
    /**
     * Test the update method.
     */
    @Test
    public void testUpdate() throws SQLException {
        // Create a new user
        User user = new User();
        user.setCompanyId(getCompanyId("Test Company 1"));
        user.setRole(UserRole.REGULAR_USER);
        user.setEmail("updateuser@test.com");
        user.setName("Update Test User");
        user.setPasswordHash("updatehash123");
        user.setStatus(UserStatus.ACTIVE);
        
        User createdUser = userDao.create(user);
        
        // Update the user
        createdUser.setName("Updated Test User");
        createdUser.setEmail("updated@test.com");
        
        User updatedUser = userDao.update(createdUser);
        
        // Verify the user was updated
        assertEquals("User name should be updated", "Updated Test User", updatedUser.getName());
        assertEquals("User email should be updated", "updated@test.com", updatedUser.getEmail());
        
        // Clean up
        boolean deleted = userDao.delete(createdUser.getId());
        assertTrue("User should be deleted successfully", deleted);
    }
    
    /**
     * Test the delete method.
     */
    @Test
    public void testDelete() throws SQLException {
        // Create a new user
        User user = new User();
        user.setCompanyId(getCompanyId("Test Company 1"));
        user.setRole(UserRole.REGULAR_USER);
        user.setEmail("deleteuser@test.com");
        user.setName("Delete Test User");
        user.setPasswordHash("deletehash123");
        user.setStatus(UserStatus.ACTIVE);
        
        User createdUser = userDao.create(user);
        
        // Delete the user
        boolean deleted = userDao.delete(createdUser.getId());
        
        // Verify the user was deleted
        assertTrue("User should be deleted successfully", deleted);
        
        Optional<User> foundUser = userDao.findById(createdUser.getId());
        assertFalse("User should not be found after deletion", foundUser.isPresent());
    }
    
    /**
     * Test the findByEmail method.
     */
    @Test
    public void testFindByEmail() throws SQLException {
        // Find user by email
        Optional<User> user = userDao.findByEmail("admin@test.com");
        
        // Verify the user was found
        assertTrue("User should be found by email", user.isPresent());
        assertEquals("Found user email should match", "admin@test.com", user.get().getEmail());
    }
    
    /**
     * Test the findByCompanyId method.
     */
    @Test
    public void testFindByCompanyId() throws SQLException {
        // Find users by company ID
        List<User> users = userDao.findByCompanyId(getCompanyId("Test Company 1"));
        
        // Verify there are users
        assertFalse("There should be users for Test Company 1", users.isEmpty());
        
        // Verify all entries have the correct company ID
        for (User user : users) {
            assertEquals("User company ID should match Test Company 1", getCompanyId("Test Company 1"), user.getCompanyId());
        }
    }
    
    /**
     * Test the findByRole method.
     */
    @Test
    public void testFindByRole() throws SQLException {
        // Find users by role
        List<User> users = userDao.findByRole(UserRole.COMPANY_ADMIN);
        
        // Verify there are users
        assertFalse("There should be admin users", users.isEmpty());
        
        // Verify all entries have the correct role
        for (User user : users) {
            assertEquals("User role should be COMPANY_ADMIN", UserRole.COMPANY_ADMIN, user.getRole());
        }
    }
    
    /**
     * Test the findByStatus method.
     */
    @Test
    public void testFindByStatus() throws SQLException {
        // Find users by status
        List<User> users = userDao.findByStatus(UserStatus.ACTIVE);
        
        // Verify there are users
        assertFalse("There should be active users", users.isEmpty());
        
        // Verify all entries have the correct status
        for (User user : users) {
            assertEquals("User status should be ACTIVE", UserStatus.ACTIVE, user.getStatus());
        }
    }
    
    /**
     * Test the findByCompanyIdAndRole method.
     */
    @Test
    public void testFindByCompanyIdAndRole() throws SQLException {
        // Find users by company ID and role
        List<User> users = userDao.findByCompanyIdAndRole(getCompanyId("Test Company 1"), UserRole.COMPANY_ADMIN);
        
        // Verify there are users
        assertFalse("There should be admin users for Test Company 1", users.isEmpty());
        
        // Verify all entries have the correct company ID and role
        for (User user : users) {
            assertEquals("User company ID should match Test Company 1", getCompanyId("Test Company 1"), user.getCompanyId());
            assertEquals("User role should be COMPANY_ADMIN", UserRole.COMPANY_ADMIN, user.getRole());
        }
    }
    
    /**
     * Test the updatePassword method.
     */
    @Test
    public void testUpdatePassword() throws SQLException {
        // Create a new user
        User user = new User();
        user.setCompanyId(getCompanyId("Test Company 1"));
        user.setRole(UserRole.REGULAR_USER);
        user.setEmail("passworduser@test.com");
        user.setName("Password Test User");
        user.setPasswordHash("oldhash123");
        user.setStatus(UserStatus.ACTIVE);
        
        User createdUser = userDao.create(user);
        
        // Update the user's password
        boolean updated = userDao.updatePassword(createdUser.getId(), "newhash456");
        
        // Verify the password was updated
        Optional<User> updatedUser = userDao.findById(createdUser.getId());
        assertTrue("User should be found after password update", updatedUser.isPresent());
        assertEquals("User password hash should be updated", "newhash456", updatedUser.get().getPasswordHash());
        
        // Clean up
        boolean deleted = userDao.delete(createdUser.getId());
        assertTrue("User should be deleted successfully", deleted);
    }
    
    /**
     * Test the updateStatus method.
     */
    @Test
    public void testUpdateStatus() throws SQLException {
        // Create a new user
        User user = new User();
        user.setCompanyId(getCompanyId("Test Company 1"));
        user.setRole(UserRole.REGULAR_USER);
        user.setEmail("statususer@test.com");
        user.setName("Status Test User");
        user.setPasswordHash("statushash123");
        user.setStatus(UserStatus.ACTIVE);
        
        User createdUser = userDao.create(user);
        
        // Update the user's status
        boolean updated = userDao.updateStatus(createdUser.getId(), UserStatus.SUSPENDED);
        
        // Verify the status was updated
        Optional<User> updatedUser = userDao.findById(createdUser.getId());
        assertTrue("User should be found after status update", updatedUser.isPresent());
        assertEquals("User status should be updated to SUSPENDED", UserStatus.SUSPENDED, updatedUser.get().getStatus());
        
        // Clean up
        boolean deleted = userDao.delete(createdUser.getId());
        assertTrue("User should be deleted successfully", deleted);
    }
    
    /**
     * Test the authenticate method.
     */
    @Test
    public void testAuthenticate() throws SQLException {
        // Create a new user
        User user = new User();
        user.setCompanyId(getCompanyId("Test Company 1"));
        user.setRole(UserRole.REGULAR_USER);
        user.setEmail("authuser@test.com");
        user.setName("Auth Test User");
        user.setPasswordHash("authhash123");
        user.setStatus(UserStatus.ACTIVE);
        
        User createdUser = userDao.create(user);
        
        // Authenticate the user
        Optional<User> authenticatedUser = userDao.authenticate("authuser@test.com", "authhash123");
        
        // Verify the user was authenticated
        assertTrue("User should be authenticated", authenticatedUser.isPresent());
        assertEquals("Authenticated user email should match", "authuser@test.com", authenticatedUser.get().getEmail());
        
        // Clean up
        boolean deleted = userDao.delete(createdUser.getId());
        assertTrue("User should be deleted successfully", deleted);
    }
    
    /**
     * Test the findActiveUserByEmail method.
     */
    @Test
    public void testFindActiveUserByEmail() throws SQLException {
        // Find active user by email
        Optional<User> user = userDao.findActiveUserByEmail("admin@test.com");
        
        // Verify the user was found
        assertTrue("Active user should be found by email", user.isPresent());
        assertEquals("Found user email should match", "admin@test.com", user.get().getEmail());
        assertEquals("Found user status should be ACTIVE", UserStatus.ACTIVE, user.get().getStatus());
        
        // Try to find a suspended user
        Optional<User> suspendedUser = userDao.findActiveUserByEmail("admin2@test.com");
        
        // Verify the suspended user was not found
        assertFalse("Suspended user should not be found as active", suspendedUser.isPresent());
    }
    
    /**
     * Test the isUserLockedOut method.
     */
    @Test
    public void testIsUserLockedOut() throws SQLException {
        // Get user IDs
        Integer activeUserId = getUserIdByEmail("admin@test.com");
        Integer lockedUserId = getUserIdByEmail("admin2@test.com");
        
        // Check if active user is locked out
        boolean isActiveUserLocked = userDao.isUserLockedOut(activeUserId);
        
        // Verify active user is not locked out
        assertFalse("Active user should not be locked out", isActiveUserLocked);
        
        // Check if locked user is locked out
        boolean isLockedUserLocked = userDao.isUserLockedOut(lockedUserId);
        
        // Verify locked user is locked out
        assertTrue("Locked user should be locked out", isLockedUserLocked);
    }
    
    /**
     * Test the hasRole method.
     */
    @Test
    public void testHasRole() throws SQLException {
        // Get user ID
        Integer adminUserId = getUserIdByEmail("admin@test.com");
        
        // Check if user has COMPANY_ADMIN role
        boolean hasAdminRole = userDao.hasRole(adminUserId, UserRole.COMPANY_ADMIN);
        
        // Verify user has COMPANY_ADMIN role
        assertTrue("User should have COMPANY_ADMIN role", hasAdminRole);
        
        // Check if user has SUPER_ADMIN role
        boolean hasSuperAdminRole = userDao.hasRole(adminUserId, UserRole.SUPER_ADMIN);
        
        // Verify user does not have SUPER_ADMIN role
        assertFalse("User should not have SUPER_ADMIN role", hasSuperAdminRole);
    }
    
    /**
     * Test the updateRole method.
     */
    @Test
    public void testUpdateRole() throws SQLException {
        // Create a new user
        User user = new User();
        user.setCompanyId(getCompanyId("Test Company 1"));
        user.setRole(UserRole.REGULAR_USER);
        user.setEmail("roleuser@test.com");
        user.setName("Role Test User");
        user.setPasswordHash("rolehash123");
        user.setStatus(UserStatus.ACTIVE);
        
        User createdUser = userDao.create(user);
        
        // Update the user's role
        boolean updated = userDao.updateRole(createdUser.getId(), UserRole.SPACE_ADMIN);
        
        // Verify the role was updated
        Optional<User> updatedUser = userDao.findById(createdUser.getId());
        assertTrue("User should be found after role update", updatedUser.isPresent());
        assertEquals("User role should be updated to SPACE_ADMIN", UserRole.SPACE_ADMIN, updatedUser.get().getRole());
        
        // Clean up
        boolean deleted = userDao.delete(createdUser.getId());
        assertTrue("User should be deleted successfully", deleted);
    }
    
    /**
     * Test the findByCompanyIdAndRoleAndStatus method.
     */
    @Test
    public void testFindByCompanyIdAndRoleAndStatus() throws SQLException {
        // Find users by company ID, role, and status
        List<User> users = userDao.findByCompanyIdAndRoleAndStatus(
                getCompanyId("Test Company 1"), UserRole.COMPANY_ADMIN, UserStatus.ACTIVE);
        
        // Verify there are users
        assertFalse("There should be active admin users for Test Company 1", users.isEmpty());
        
        // Verify all entries have the correct company ID, role, and status
        for (User user : users) {
            assertEquals("User company ID should match Test Company 1", getCompanyId("Test Company 1"), user.getCompanyId());
            assertEquals("User role should be COMPANY_ADMIN", UserRole.COMPANY_ADMIN, user.getRole());
            assertEquals("User status should be ACTIVE", UserStatus.ACTIVE, user.getStatus());
        }
        
        // Test with null status (should use findByCompanyIdAndRole)
        List<User> usersWithNullStatus = userDao.findByCompanyIdAndRoleAndStatus(
                getCompanyId("Test Company 1"), UserRole.COMPANY_ADMIN, null);
        
        // Verify there are users
        assertFalse("There should be admin users for Test Company 1 with null status", usersWithNullStatus.isEmpty());
    }
    
    /**
     * Test the findByEmailContaining method.
     */
    @Test
    public void testFindByEmailContaining() throws SQLException {
        // Find users by email containing "admin"
        List<User> users = userDao.findByEmailContaining("admin", null);
        
        // Verify there are users
        assertFalse("There should be users with email containing 'admin'", users.isEmpty());
        
        // Verify all entries have email containing "admin"
        for (User user : users) {
            assertTrue("User email should contain 'admin'", user.getEmail().contains("admin"));
        }
        
        // Test with limit
        List<User> limitedUsers = userDao.findByEmailContaining("admin", 1);
        
        // Verify there is exactly one user
        assertEquals("There should be exactly one user with limit 1", 1, limitedUsers.size());
    }
    
    /**
     * Test the findByCreatedAtAfter method.
     */
    @Test
    public void testFindByCreatedAtAfter() throws SQLException {
        // Create a timestamp for 1 day ago
        Timestamp oneDayAgo = new Timestamp(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        
        // Find users created after 1 day ago
        List<User> users = userDao.findByCreatedAtAfter(oneDayAgo);
        
        // Verify there are users
        assertFalse("There should be users created after 1 day ago", users.isEmpty());
    }
    
    /**
     * Test the isPasswordPreviouslyUsed and addPasswordToHistory methods.
     */
    @Test
    public void testPasswordHistory() throws SQLException {
        // Get user ID
        Integer adminUserId = getUserIdByEmail("admin@test.com");
        
        // Check if a password is previously used
        boolean isOldPasswordUsed = userDao.isPasswordPreviouslyUsed(adminUserId, "oldhash1", 5);
        
        // Verify old password is found in history
        assertTrue("Old password should be found in history", isOldPasswordUsed);
        
        // Add a new password to history
        boolean added = userDao.addPasswordToHistory(adminUserId, "newhash123");
        
        // Verify password was added to history
        assertTrue("Password should be added to history", added);
        
        // Check if the new password is now in history
        boolean isNewPasswordUsed = userDao.isPasswordPreviouslyUsed(adminUserId, "newhash123", 5);
        
        // Verify new password is found in history
        assertTrue("New password should be found in history", isNewPasswordUsed);
    }
    
    /**
     * Helper method to get the ID of a company by name.
     * 
     * @param name The name of the company
     * @return The ID of the company
     * @throws SQLException if a database error occurs
     */
    private Integer getCompanyId(String name) throws SQLException {
        try (var conn = com.celestra.db.DatabaseUtil.getConnection();
             var ps = conn.prepareStatement("SELECT id FROM companies WHERE name = ?")) {
            ps.setString(1, name);
            var rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : null;
        }
    }
    
    /**
     * Helper method to get the ID of a user by email.
     * 
     * @param email The email of the user
     * @return The ID of the user
     * @throws SQLException if a database error occurs
     */
    private Integer getUserIdByEmail(String email) throws SQLException {
        try (var conn = com.celestra.db.DatabaseUtil.getConnection();
             var ps = conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
            ps.setString(1, email);
            var rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : null;
        }
    }
}