package com.celestra.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for managing database connections.
 * Provides centralized connection management with connection pooling.
 */
public class DatabaseUtil {
    private static final String PROPERTIES_FILE = "application.properties";
    private static final String DB_DRIVER = "db.driver";
    private static final String DB_URL = "db.url";
    private static final String DB_USERNAME = "db.username";
    private static final String DB_PASSWORD = "db.password";
    private static final String DB_POOL_INITIAL_SIZE = "db.pool.initialSize";
    private static final String DB_POOL_MAX_ACTIVE = "db.pool.maxActive";
    private static final String DB_POOL_MAX_IDLE = "db.pool.maxIdle";
    private static final String DB_POOL_MIN_IDLE = "db.pool.minIdle";
    private static final String DB_POOL_MAX_WAIT = "db.pool.maxWait";
    private static final String DB_VALIDATION_QUERY = "db.validation.query";
    private static final String DB_VALIDATION_INTERVAL = "db.validation.interval";
    private static final String DB_VALIDATION_ON_BORROW = "db.validation.onBorrow";
    
    private static DataSource dataSource;
    private static Properties properties;
    
    static {
        try {
            initialize();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize database connection pool", e);
        }
    }
    
    /**
     * Initialize the database connection pool.
     * 
     * @throws IOException if the properties file cannot be loaded
     */
    private static void initialize() throws IOException {
        properties = loadProperties();
        
        BasicDataSource ds = new BasicDataSource();
        
        // Basic connection properties
        ds.setDriverClassName(getProperty(DB_DRIVER));
        ds.setUrl(getProperty(DB_URL));
        ds.setUsername(getProperty(DB_USERNAME));
        ds.setPassword(getProperty(DB_PASSWORD));
        
        // Connection pool settings
        ds.setInitialSize(getIntProperty(DB_POOL_INITIAL_SIZE, 5));
        ds.setMaxTotal(getIntProperty(DB_POOL_MAX_ACTIVE, 20));
        ds.setMaxIdle(getIntProperty(DB_POOL_MAX_IDLE, 10));
        ds.setMinIdle(getIntProperty(DB_POOL_MIN_IDLE, 5));
        ds.setMaxWaitMillis(getIntProperty(DB_POOL_MAX_WAIT, 30000));
        
        // Validation settings
        String validationQuery = getProperty(DB_VALIDATION_QUERY);
        if (StringUtils.isNotBlank(validationQuery)) {
            ds.setValidationQuery(validationQuery);
            ds.setValidationQueryTimeout(30);
            ds.setTestOnBorrow(getBooleanProperty(DB_VALIDATION_ON_BORROW, true));
            ds.setTimeBetweenEvictionRunsMillis(getIntProperty(DB_VALIDATION_INTERVAL, 30000));
        }
        
        dataSource = ds;
    }
    
    /**
     * Load properties from the application.properties file.
     * 
     * @return Properties object containing the configuration
     * @throws IOException if the properties file cannot be loaded
     */
    private static Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = DatabaseUtil.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new IOException("Unable to find " + PROPERTIES_FILE);
            }
            props.load(input);
        }
        return props;
    }
    
    /**
     * Get a property value from the properties file.
     * 
     * @param key the property key
     * @return the property value
     * @throws IllegalArgumentException if the property is not found
     */
    private static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Required property '" + key + "' not found in " + PROPERTIES_FILE);
        }
        return value;
    }
    
    /**
     * Get an integer property value from the properties file.
     * 
     * @param key the property key
     * @param defaultValue the default value to use if the property is not found
     * @return the property value as an integer
     */
    private static int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Get a boolean property value from the properties file.
     * 
     * @param key the property key
     * @param defaultValue the default value to use if the property is not found
     * @return the property value as a boolean
     */
    private static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Get a connection from the connection pool.
     * 
     * @return a database connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database connection pool has not been initialized");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Close a database connection.
     * 
     * @param connection the connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Log the exception but don't rethrow it
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Close the connection pool.
     * This method should be called when the application is shutting down.
     */
    public static void shutdown() {
        if (dataSource instanceof BasicDataSource) {
            try {
                ((BasicDataSource) dataSource).close();
            } catch (SQLException e) {
                System.err.println("Error closing connection pool: " + e.getMessage());
            }
        }
    }
}