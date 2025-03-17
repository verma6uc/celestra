package com.celestra.seeding.seeders;

import com.celestra.dao.UserLockoutDao;
import com.celestra.dao.impl.UserLockoutDaoImpl;
import com.celestra.model.UserLockout;
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
 * Seeder class for the user_lockouts table.
 * This class is responsible for generating and inserting test data for user lockouts.
 * It uses the UserLockoutDao to interact with the database.
 */
public class UserLockoutSeeder {
    
    private static final Logger LOGGER = Logger.getLogger(UserLockoutSeeder.class.getName());
    
    private final Connection connection;
    private final UserLockoutDao userLockoutDao;
    private final int numUserLockouts;
    private final List<Integer> userIds;
    
    /**
     * Constructor for UserLockoutSeeder.
     * 
     * @param connection Database connection
     * @param numUserLockouts Number of user lockouts to seed
     * @param userIds List of user IDs to associate lockouts with
     */
    public UserLockoutSeeder(Connection connection, int numUserLockouts, List<Integer> userIds) {
        this.connection = connection;
        this.userLockoutDao = new UserLockoutDaoImpl();
        this.numUserLockouts = numUserLockouts;
        this.userIds = userIds;
    }
    
    /**
     * Seed the user_lockouts table with test data.
     * 
     * @return List of generated user lockout IDs
     * @throws SQLException If a database error occurs
     */
    public List<Integer> seed() throws SQLException {
        LOGGER.info("Seeding user_lockouts table with " + numUserLockouts + " records...");
        
        if (userIds.isEmpty()) {
            LOGGER.warning("No user IDs provided. Cannot seed user lockouts.");
            return List.of();
        }
        
        List<Integer> userLockoutIds = new ArrayList<>();
        
        try {
            // Generate a mix of active and expired lockouts
            int numActiveLockouts = (int) (numUserLockouts * 0.2); // 20% active lockouts
            int numExpiredLockouts = numUserLockouts - numActiveLockouts; // 80% expired lockouts
            
            // Generate active lockouts
            for (int i = 0; i < numActiveLockouts; i++) {
                // Select a random user
                Integer userId = userIds.get(FakerUtil.generateRandomInt(0, userIds.size() - 1));
                
                // Generate lockout data                
                String reason = generateLockoutReason();
                Integer failedAttempts = FakerUtil.generateRandomInt(3, 10);
                
                // Generate timestamps
                Timestamp createdAt = TimestampUtil.getRandomTimestampInRange(-2, 0); // Within the last 2 days
                Timestamp expiresAt = TimestampUtil.getRandomTimeAfter(createdAt, 60 * 24, 60 * 24 * 7); // 1-7 days after creation
                
                // Create the user lockout object
                UserLockout userLockout = new UserLockout();
                userLockout.setUserId(userId);
                userLockout.setLockoutStart(createdAt);
                userLockout.setLockoutEnd(expiresAt);
                userLockout.setFailedAttempts(failedAttempts);
                userLockout.setReason(reason);
                userLockout.setCreatedAt(createdAt);
                userLockout.setUpdatedAt(createdAt);
                
                // Save the user lockout
                UserLockout createdLockout = userLockoutDao.create(userLockout);
                if (createdLockout != null && createdLockout.getId() > 0) {
                    userLockoutIds.add(createdLockout.getId());
                }
            }
            
            // Generate expired lockouts
            for (int i = 0; i < numExpiredLockouts; i++) {
                // Select a random user
                Integer userId = userIds.get(FakerUtil.generateRandomInt(0, userIds.size() - 1));

                // Generate lockout data
                String reason = generateLockoutReason();
                Integer failedAttempts = FakerUtil.generateRandomInt(3, 10);
                
                // Generate timestamps for expired lockouts
                Timestamp createdAt = TimestampUtil.getRandomTimestampInRange(-60, -3); // 3-60 days ago
                Timestamp expiresAt = TimestampUtil.getRandomTimeAfter(createdAt, 60 * 24, 60 * 24 * 3); // 1-3 days after creation
                
                // Create the user lockout object
                UserLockout userLockout = new UserLockout();
                userLockout.setUserId(userId);
                userLockout.setLockoutStart(createdAt);
                userLockout.setLockoutEnd(expiresAt);
                userLockout.setFailedAttempts(failedAttempts);
                userLockout.setReason(reason);
                userLockout.setCreatedAt(createdAt);
                userLockout.setUpdatedAt(createdAt);
                
                // Save the user lockout
                UserLockout createdLockout = userLockoutDao.create(userLockout);
                if (createdLockout != null && createdLockout.getId() > 0) {
                    userLockoutIds.add(createdLockout.getId());
                }
            }
            
            LOGGER.info("Successfully seeded " + userLockoutIds.size() + " user lockouts.");
            return userLockoutIds;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error seeding user_lockouts table", e);
            throw e;
        }
    }
    
    /**
     * Generate a random lockout reason.
     * 
     * @return A random lockout reason
     */
    private String generateLockoutReason() {
        String[] reasons = {
            "Too many failed login attempts",
            "Suspicious activity detected",
            "Multiple concurrent login attempts",
            "Unusual login location",
            "Potential brute force attack",
            "Security policy violation",
            "Administrative action",
            "Temporary security measure"
        };
        
        return reasons[FakerUtil.generateRandomInt(0, reasons.length - 1)];
    }
}