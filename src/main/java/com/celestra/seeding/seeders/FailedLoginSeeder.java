package com.celestra.seeding.seeders;

import com.celestra.dao.FailedLoginDao;
import com.celestra.dao.impl.FailedLoginDaoImpl;
import com.celestra.model.FailedLogin;
import com.celestra.seeding.util.FakerUtil;
import com.celestra.seeding.util.TimestampUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Seeder class for the failed_logins table.
 * This class is responsible for generating and inserting test data for failed login attempts.
 * It uses the FailedLoginDao to interact with the database.
 */
public class FailedLoginSeeder {
    
    private static final Logger LOGGER = Logger.getLogger(FailedLoginSeeder.class.getName());
    
    private final Connection connection;
    private final FailedLoginDao failedLoginDao;
    private final int numFailedLogins;
    private final List<Integer> userIds;
    
    /**
     * Constructor for FailedLoginSeeder.
     * 
     * @param connection Database connection
     * @param numFailedLogins Number of failed login records to seed
     * @param userIds List of user IDs to associate failed logins with
     */
    public FailedLoginSeeder(Connection connection, int numFailedLogins, List<Integer> userIds) {
        this.connection = connection;
        this.failedLoginDao = new FailedLoginDaoImpl();
        this.numFailedLogins = numFailedLogins;
        this.userIds = userIds;
    }
    
    /**
     * Seed the failed_logins table with test data.
     * 
     * @return List of generated failed login IDs
     * @throws SQLException If a database error occurs
     */
    public List<Integer> seed() throws SQLException {
        LOGGER.info("Seeding failed_logins table with " + numFailedLogins + " records...");
        
        if (userIds.isEmpty()) {
            LOGGER.warning("No user IDs provided. Cannot seed failed logins.");
            return List.of();
        }
        
        List<Integer> failedLoginIds = new ArrayList<>();
        
        try {
            // Generate a mix of failed logins:
            // 1. Some with valid usernames but invalid passwords
            // 2. Some with invalid usernames
            
            // For valid usernames but invalid passwords (70% of cases)
            int numValidUsernames = (int) (numFailedLogins * 0.7);
            
            // For invalid usernames (30% of cases)
            int numInvalidUsernames = numFailedLogins - numValidUsernames;
            
            // Generate failed logins with valid usernames
            for (int i = 0; i < numValidUsernames; i++) {
                // Select a random user
                Integer userId = userIds.get(FakerUtil.generateRandomInt(0, userIds.size() - 1));
                
                // Generate IP address and user agent
                String ipAddress = FakerUtil.generateIpAddress();
                String userAgent = FakerUtil.generateUserAgent();
                
                // Generate a random number of consecutive failures (1-5)
                int consecutiveFailures = FakerUtil.generateRandomInt(1, 5);
                
                // Generate timestamp
                Timestamp timestamp = TimestampUtil.getRandomTimestampInRange(-365, -1);
                
                // Create the failed login object
                FailedLogin failedLogin = new FailedLogin();
                failedLogin.setUserId(userId);
                failedLogin.setIpAddress(ipAddress);
                failedLogin.setAttemptedAt(timestamp);
                failedLogin.setFailureReason("Invalid password. Consecutive failures: " + consecutiveFailures + 
                        ". User agent: " + userAgent);
                
                // Save the failed login
                FailedLogin createdFailedLogin = failedLoginDao.create(failedLogin);
                if (createdFailedLogin != null && createdFailedLogin.getId() > 0) {
                    failedLoginIds.add(createdFailedLogin.getId());
                }
            }
            
            // Generate failed logins with invalid usernames
            for (int i = 0; i < numInvalidUsernames; i++) {
                // Generate IP address and user agent
                String ipAddress = FakerUtil.generateIpAddress();
                String userAgent = FakerUtil.generateUserAgent();
                
                // Generate a random number of consecutive failures (1-3)
                int consecutiveFailures = FakerUtil.generateRandomInt(1, 3);
                
                // Generate timestamp
                Timestamp timestamp = TimestampUtil.getRandomTimestampInRange(-365, -1);
                
                // Create the failed login object
                FailedLogin failedLogin = new FailedLogin();
                failedLogin.setUserId(null); // No user ID for invalid usernames
                failedLogin.setIpAddress(ipAddress);
                failedLogin.setAttemptedAt(timestamp);
                failedLogin.setFailureReason("User not found: " + FakerUtil.generateEmail() + 
                        ". Consecutive failures: " + consecutiveFailures + ". User agent: " + userAgent);
                
                // Save the failed login
                FailedLogin createdFailedLogin = failedLoginDao.create(failedLogin);
                if (createdFailedLogin != null && createdFailedLogin.getId() > 0) {
                    failedLoginIds.add(createdFailedLogin.getId());
                }
            }
            
            LOGGER.info("Successfully seeded " + failedLoginIds.size() + " failed logins.");
            return failedLoginIds;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error seeding failed_logins table", e);
            throw e;
        }
    }
}