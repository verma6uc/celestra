package com.celestra.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;

import com.celestra.db.DatabaseUtil;

/**
 * Base class for DAO tests.
 * Provides common functionality for all DAO test classes.
 */
public abstract class BaseDaoTest {
    
    /**
     * Set up the test environment.
     * This method is called before each test.
     * 
     * @throws SQLException if a database access error occurs
     */
    @Before
    public void setUp() throws SQLException {
        // Create test tables if needed
        createTestTables();
        
        // Insert test data
        insertTestData();
    }
    
    /**
     * Tear down the test environment.
     * This method is called after each test.
     * 
     * @throws SQLException if a database access error occurs
     */
    @After
    public void tearDown() throws SQLException {
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
}