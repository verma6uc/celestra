package com.celestra.dao.impl;

import static org.junit.Assert.*;

import java.sql.SQLException;
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
        executeSQL("INSERT INTO companies (id, name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES (1, 'Test Company 1', 'Test Company Description 1', 'SMALL'::company_size, 'TECH'::company_vertical, 'ACTIVE'::company_status, NOW(), NOW())");
        
        executeSQL("INSERT INTO companies (id, name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES (2, 'Test Company 2', 'Test Company Description 2', 'MEDIUM'::company_size, 'PHARMACEUTICAL'::company_vertical, 'ACTIVE'::company_status, NOW(), NOW())");
        
        // Insert test users
        executeSQL("INSERT INTO users (company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES (1, 'COMPANY_ADMIN'::user_role, 'admin@test.com', 'Test Admin', 'hash123', 'ACTIVE'::user_status, NOW(), NOW())");
        
        executeSQL("INSERT INTO users (company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES (1, 'REGULAR_USER'::user_role, 'user@test.com', 'Test User', 'hash456', 'ACTIVE'::user_status, NOW(), NOW())");
        
        executeSQL("INSERT INTO users (company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES (2, 'COMPANY_ADMIN'::user_role, 'admin2@test.com', 'Another Admin', 'hash789', 'SUSPENDED'::user_status, NOW(), NOW())");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM users WHERE email LIKE '%@test.com'");
        executeSQL("DELETE FROM companies WHERE id IN (1, 2)");
    }
    
    /**
     * Test the create method.
     */
    @Test
    public void testCreate() throws SQLException {
        // Create a new user
        User user = new User();
        user.setCompanyId(1);
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
        user.setCompanyId(1);
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
        user.setCompanyId(1);
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
        List<User> users = userDao.findByCompanyId(1);
        
        // Verify there are users
        assertFalse("There should be users for company ID 1", users.isEmpty());
        
        // Verify all entries have the correct company ID
        for (User user : users) {
            assertEquals("User company ID should be 1", Integer.valueOf(1), user.getCompanyId());
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
        List<User> users = userDao.findByCompanyIdAndRole(1, UserRole.COMPANY_ADMIN);
        
        // Verify there are users
        assertFalse("There should be admin users for company ID 1", users.isEmpty());
        
        // Verify all entries have the correct company ID and role
        for (User user : users) {
            assertEquals("User company ID should be 1", Integer.valueOf(1), user.getCompanyId());
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
        user.setCompanyId(1);
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
        user.setCompanyId(1);
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
        user.setCompanyId(1);
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
}