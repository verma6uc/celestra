package com.celestra.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.celestra.db.DatabaseUtil;

/**
 * Base class for DAO tests.
 * Provides common functionality for all DAO test classes.
 */
public abstract class BaseDaoTest {
    
    /**
     * Set up the test environment.
     * This method should be called at the beginning of each test.
     * 
     * @throws SQLException if a database access error occurs
     */
    protected void setUp() throws SQLException {
        // Create test tables if needed
        createTestTables();
        
        // Insert test data
        insertTestData();
    }
    
    /**
     * Tear down the test environment.
     * This method should be called at the end of each test.
     * 
     * @throws SQLException if a database access error occurs
     */
    protected void tearDown() throws SQLException {
        // Clean up test data
        cleanupTestData();
    }
    
    /**
     * Create test tables if needed.
     * 
     * @throws SQLException if a database access error occurs
     */
    protected abstract void createTestTables() throws SQLException;
    
    /**
     * Insert test data.
     * 
     * @throws SQLException if a database access error occurs
     */
    protected abstract void insertTestData() throws SQLException;
    
    /**
     * Clean up test data.
     * 
     * @throws SQLException if a database access error occurs
     */
    protected abstract void cleanupTestData() throws SQLException;
    
    /**
     * Execute a SQL statement.
     * 
     * @param sql The SQL statement to execute
     * @throws SQLException if a database access error occurs
     */
    protected void executeSQL(String sql) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Print a test result.
     * 
     * @param testName The name of the test
     * @param success Whether the test was successful
     * @param message Additional message (optional)
     */
    protected void printTestResult(String testName, boolean success, String message) {
        System.out.println("Test: " + testName);
        System.out.println("Result: " + (success ? "SUCCESS" : "FAILURE"));
        if (message != null && !message.isEmpty()) {
            System.out.println("Message: " + message);
        }
        System.out.println();
    }
    
    /**
     * Print a test result.
     * 
     * @param testName The name of the test
     * @param success Whether the test was successful
     */
    protected void printTestResult(String testName, boolean success) {
        printTestResult(testName, success, null);
    }
    
    /**
     * Print a test failure.
     * 
     * @param testName The name of the test
     * @param e The exception that caused the failure
     */
    protected void printTestFailure(String testName, Exception e) {
        System.out.println("Test: " + testName);
        System.out.println("Result: FAILURE");
        System.out.println("Exception: " + e.getClass().getName());
        System.out.println("Message: " + e.getMessage());
        System.out.println();
    }
}