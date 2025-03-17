package com.celestra.db;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Context listener for database initialization and cleanup.
 * Initializes the database connection pool when the application starts
 * and shuts it down when the application stops.
 */
@WebListener
public class DatabaseContextListener implements ServletContextListener {
    
    /**
     * Called when the web application is starting.
     * Initializes the database connection pool.
     * 
     * @param sce the servlet context event
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Force initialization of the DatabaseUtil class
        try {
            // Get and immediately release a connection to verify the pool is working
            DatabaseUtil.getConnection().close();
            System.out.println("Database connection pool initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing database connection pool: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Called when the web application is shutting down.
     * Shuts down the database connection pool.
     * 
     * @param sce the servlet context event
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            DatabaseUtil.shutdown();
            System.out.println("Database connection pool shut down successfully");
        } catch (Exception e) {
            System.err.println("Error shutting down database connection pool: " + e.getMessage());
            e.printStackTrace();
        }
    }
}