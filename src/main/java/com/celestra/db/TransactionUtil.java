package com.celestra.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Utility class for managing database transactions.
 * Provides methods for beginning, committing, and rolling back transactions.
 */
public class TransactionUtil {
    
    /**
     * Begin a transaction by setting auto-commit to false.
     * 
     * @param connection the database connection
     * @throws SQLException if a database access error occurs
     */
    public static void beginTransaction(Connection connection) throws SQLException {
        if (connection != null) {
            connection.setAutoCommit(false);
        }
    }
    
    /**
     * Commit a transaction and restore auto-commit to true.
     * 
     * @param connection the database connection
     * @throws SQLException if a database access error occurs
     */
    public static void commitTransaction(Connection connection) throws SQLException {
        if (connection != null) {
            connection.commit();
            connection.setAutoCommit(true);
        }
    }
    
    /**
     * Roll back a transaction and restore auto-commit to true.
     * 
     * @param connection the database connection
     */
    public static void rollbackTransaction(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error rolling back transaction: " + e.getMessage());
            }
        }
    }
    
    /**
     * Execute a database operation within a transaction.
     * 
     * @param operation the database operation to execute
     * @throws Exception if an error occurs during the operation
     */
    public static void executeInTransaction(DatabaseOperation operation) throws Exception {
        Connection connection = null;
        try {
            connection = DatabaseUtil.getConnection();
            beginTransaction(connection);
            
            operation.execute(connection);
            
            commitTransaction(connection);
        } catch (Exception e) {
            rollbackTransaction(connection);
            throw e;
        } finally {
            DatabaseUtil.closeConnection(connection);
        }
    }
    
    /**
     * Functional interface for database operations.
     */
    @FunctionalInterface
    public interface DatabaseOperation {
        /**
         * Execute a database operation.
         * 
         * @param connection the database connection
         * @throws Exception if an error occurs during the operation
         */
        void execute(Connection connection) throws Exception;
    }
}