package com.celestra.seeding.seeders;

import com.celestra.dao.PasswordHistoryDao;
import com.celestra.dao.impl.PasswordHistoryDaoImpl;
import com.celestra.model.PasswordHistory;
import com.celestra.seeding.util.FakerUtil;
import com.celestra.seeding.util.PasswordUtil;
import com.celestra.seeding.util.TimestampUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Seeder class for the password_history table.
 * This class is responsible for generating and inserting test data for password history.
 * It uses the PasswordHistoryDao to interact with the database.
 */
public class PasswordHistorySeeder {
    
    private static final Logger LOGGER = Logger.getLogger(PasswordHistorySeeder.class.getName());
    
    private final Connection connection;
    private final PasswordHistoryDao passwordHistoryDao;
    private final int numPasswordHistories;
    private final List<Integer> userIds;
    
    /**
     * Constructor for PasswordHistorySeeder.
     * 
     * @param connection Database connection
     * @param numPasswordHistories Number of password histories to seed
     * @param userIds List of user IDs to associate password histories with
     */
    public PasswordHistorySeeder(Connection connection, int numPasswordHistories, List<Integer> userIds) {
        this.connection = connection;
        this.passwordHistoryDao = new PasswordHistoryDaoImpl();
        this.numPasswordHistories = numPasswordHistories;
        this.userIds = userIds;
    }
    
    /**
     * Seed the password_history table with test data.
     * 
     * @return List of generated password history IDs
     * @throws SQLException If a database error occurs
     */
    public List<Integer> seed() throws SQLException {
        LOGGER.info("Seeding password_history table with " + numPasswordHistories + " records...");
        
        if (userIds.isEmpty()) {
            LOGGER.warning("No user IDs provided. Cannot seed password histories.");
            return List.of();
        }
        
        List<Integer> passwordHistoryIds = new ArrayList<>();
        
        try {
            // Distribute password histories across users
            // Some users will have multiple password changes, others none
            int usersWithHistory = Math.min(userIds.size(), numPasswordHistories);
            int[] passwordsPerUser = new int[usersWithHistory];
            
            // Ensure each user with history has at least one password history
            for (int i = 0; i < usersWithHistory; i++) {
                passwordsPerUser[i] = 1;
            }
            
            // Distribute remaining password histories
            int remainingHistories = numPasswordHistories - usersWithHistory;
            for (int i = 0; i < remainingHistories; i++) {
                // Randomly select a user to add another password history
                int userIndex = FakerUtil.generateRandomInt(0, usersWithHistory - 1);
                passwordsPerUser[userIndex]++;
            }
            
            // Create password histories for each user
            for (int i = 0; i < usersWithHistory; i++) {
                Integer userId = userIds.get(i);
                int numHistories = passwordsPerUser[i];
                
                // Generate password histories for this user
                List<Timestamp> timestamps = new ArrayList<>();
                
                // Generate timestamps for each password change, in chronological order
                for (int j = 0; j < numHistories; j++) {
                    // Each password change is 30-90 days apart
                    int daysAgo = (numHistories - j) * FakerUtil.generateRandomInt(30, 90);
                    timestamps.add(TimestampUtil.getRandomTimestampInRange(-daysAgo, -daysAgo + 1));
                }
                
                // Sort timestamps chronologically
                timestamps.sort(Timestamp::compareTo);
                
                // Create password history entries
                for (int j = 0; j < numHistories; j++) {
                    // Generate password hash
                    String passwordHash = FakerUtil.generatePasswordHash();
                    
                    Timestamp createdAt = timestamps.get(j);
                    
                    // Create the password history object
                    PasswordHistory passwordHistory = new PasswordHistory();
                    passwordHistory.setUserId(userId);
                    passwordHistory.setPasswordHash(passwordHash);
                    passwordHistory.setCreatedAt(createdAt);
                    
                    // Save the password history
                    PasswordHistory createdPasswordHistory = passwordHistoryDao.create(passwordHistory);
                    if (createdPasswordHistory != null && createdPasswordHistory.getId() > 0) {
                        passwordHistoryIds.add(createdPasswordHistory.getId());
                    }
                }
            }
            
            LOGGER.info("Successfully seeded " + passwordHistoryIds.size() + " password histories.");
            return passwordHistoryIds;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error seeding password_history table", e);
            throw e;
        }
    }
}