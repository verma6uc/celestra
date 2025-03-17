package com.celestra.dao.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
     * Main method to run the tests.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        UserDaoImplTest test = new UserDaoImplTest();
        test.runTests();
    }
    
    /**
     * Constructor.
     */
    public UserDaoImplTest() {
        userDao = new UserDaoImpl();
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
            testFindByEmail();
            testFindByCompanyId();
            testFindByRole();
            testFindByStatus();
            testFindByCompanyIdAndRole();
            testUpdatePassword();
            testUpdateStatus();
            testAuthenticate();
            
            tearDown();
            
            System.out.println("All tests completed.");
        } catch (Exception e) {
            System.err.println("Error running tests: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // Users table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // Insert test data
        executeSQL("INSERT INTO users (company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES (1, 'COMPANY_ADMIN', 'admin@test.com', 'Test Admin', 'hash123', 'ACTIVE', NOW(), NOW())");
        
        executeSQL("INSERT INTO users (company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES (1, 'REGULAR_USER', 'user@test.com', 'Test User', 'hash456', 'ACTIVE', NOW(), NOW())");
        
        executeSQL("INSERT INTO users (company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES (2, 'COMPANY_ADMIN', 'admin2@test.com', 'Another Admin', 'hash789', 'SUSPENDED', NOW(), NOW())");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM users WHERE email LIKE '%@test.com'");
    }
    
    /**
     * Test the create method.
     */
    private void testCreate() {
        try {
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
            boolean success = createdUser.getId() > 0;
            printTestResult("testCreate", success);
            
            // Clean up
            if (success) {
                userDao.delete(createdUser.getId());
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
            // Find all users
            List<User> users = userDao.findAll();
            
            // Verify there are users
            if (users.isEmpty()) {
                printTestResult("testFindById", false, "No users found");
                return;
            }
            
            // Get the first user
            User user = users.get(0);
            
            // Find the user by ID
            Optional<User> foundUser = userDao.findById(user.getId());
            
            // Verify the user was found
            boolean success = foundUser.isPresent() && 
                              foundUser.get().getId().equals(user.getId()) &&
                              foundUser.get().getEmail().equals(user.getEmail());
            
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
            // Find all users
            List<User> users = userDao.findAll();
            
            // Verify there are users
            boolean success = !users.isEmpty();
            printTestResult("testFindAll", success, "Found " + users.size() + " users");
        } catch (Exception e) {
            printTestFailure("testFindAll", e);
        }
    }
    
    /**
     * Test the update method.
     */
    private void testUpdate() {
        try {
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
            boolean success = updatedUser.getName().equals("Updated Test User") &&
                              updatedUser.getEmail().equals("updated@test.com");
            
            printTestResult("testUpdate", success);
            
            // Clean up
            userDao.delete(createdUser.getId());
        } catch (Exception e) {
            printTestFailure("testUpdate", e);
        }
    }
    
    /**
     * Test the delete method.
     */
    private void testDelete() {
        try {
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
            boolean success = deleted && !userDao.findById(createdUser.getId()).isPresent();
            
            printTestResult("testDelete", success);
        } catch (Exception e) {
            printTestFailure("testDelete", e);
        }
    }
    
    /**
     * Test the findByEmail method.
     */
    private void testFindByEmail() {
        try {
            // Find user by email
            Optional<User> user = userDao.findByEmail("admin@test.com");
            
            // Verify the user was found
            boolean success = user.isPresent() && user.get().getEmail().equals("admin@test.com");
            printTestResult("testFindByEmail", success);
        } catch (Exception e) {
            printTestFailure("testFindByEmail", e);
        }
    }
    
    /**
     * Test the findByCompanyId method.
     */
    private void testFindByCompanyId() {
        try {
            // Find users by company ID
            List<User> users = userDao.findByCompanyId(1);
            
            // Verify there are users
            boolean success = !users.isEmpty();
            printTestResult("testFindByCompanyId", success, "Found " + users.size() + " users for company ID 1");
        } catch (Exception e) {
            printTestFailure("testFindByCompanyId", e);
        }
    }
    
    /**
     * Test the findByRole method.
     */
    private void testFindByRole() {
        try {
            // Find users by role
            List<User> users = userDao.findByRole(UserRole.COMPANY_ADMIN);
            
            // Verify there are users
            boolean success = !users.isEmpty();
            printTestResult("testFindByRole", success, "Found " + users.size() + " admin users");
        } catch (Exception e) {
            printTestFailure("testFindByRole", e);
        }
    }
    
    /**
     * Test the findByStatus method.
     */
    private void testFindByStatus() {
        try {
            // Find users by status
            List<User> users = userDao.findByStatus(UserStatus.ACTIVE);
            
            // Verify there are users
            boolean success = !users.isEmpty();
            printTestResult("testFindByStatus", success, "Found " + users.size() + " active users");
        } catch (Exception e) {
            printTestFailure("testFindByStatus", e);
        }
    }
    
    /**
     * Test the findByCompanyIdAndRole method.
     */
    private void testFindByCompanyIdAndRole() {
        try {
            // Find users by company ID and role
            List<User> users = userDao.findByCompanyIdAndRole(1, UserRole.COMPANY_ADMIN);
            
            // Verify there are users
            boolean success = !users.isEmpty();
            printTestResult("testFindByCompanyIdAndRole", success, 
                    "Found " + users.size() + " admin users for company ID 1");
        } catch (Exception e) {
            printTestFailure("testFindByCompanyIdAndRole", e);
        }
    }
    
    /**
     * Test the updatePassword method.
     */
    private void testUpdatePassword() {
        try {
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
            boolean success = updated && 
                              updatedUser.isPresent() && 
                              updatedUser.get().getPasswordHash().equals("newhash456");
            
            printTestResult("testUpdatePassword", success);
            
            // Clean up
            userDao.delete(createdUser.getId());
        } catch (Exception e) {
            printTestFailure("testUpdatePassword", e);
        }
    }
    
    /**
     * Test the updateStatus method.
     */
    private void testUpdateStatus() {
        try {
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
            boolean success = updated && 
                              updatedUser.isPresent() && 
                              updatedUser.get().getStatus() == UserStatus.SUSPENDED;
            
            printTestResult("testUpdateStatus", success);
            
            // Clean up
            userDao.delete(createdUser.getId());
        } catch (Exception e) {
            printTestFailure("testUpdateStatus", e);
        }
    }
    
    /**
     * Test the authenticate method.
     */
    private void testAuthenticate() {
        try {
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
            boolean success = authenticatedUser.isPresent() && 
                              authenticatedUser.get().getEmail().equals("authuser@test.com");
            
            printTestResult("testAuthenticate", success);
            
            // Clean up
            userDao.delete(createdUser.getId());
        } catch (Exception e) {
            printTestFailure("testAuthenticate", e);
        }
    }
}