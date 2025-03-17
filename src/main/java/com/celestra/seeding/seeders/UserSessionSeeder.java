package com.celestra.seeding.seeders;

import com.celestra.dao.UserSessionDao;
import com.celestra.dao.impl.UserSessionDaoImpl;
import com.celestra.model.UserSession;
import com.celestra.seeding.util.FakerUtil;
import com.celestra.seeding.util.TimestampUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Seeder class for the user_sessions table.
 * This class is responsible for generating and inserting test data for user sessions.
 * It uses the UserSessionDao to interact with the database.
 */
public class UserSessionSeeder {
    
    private static final Logger LOGGER = Logger.getLogger(UserSessionSeeder.class.getName());
    
    private final Connection connection;
    private final UserSessionDao userSessionDao;
    private final int numUserSessions;
    private final List<Integer> userIds;
    
    /**
     * Constructor for UserSessionSeeder.
     * 
     * @param connection Database connection
     * @param numUserSessions Number of user sessions to seed
     * @param userIds List of user IDs to associate sessions with
     */
    public UserSessionSeeder(Connection connection, int numUserSessions, List<Integer> userIds) {
        this.connection = connection;
        this.userSessionDao = new UserSessionDaoImpl();
        this.numUserSessions = numUserSessions;
        this.userIds = userIds;
    }
    
    /**
     * Seed the user_sessions table with test data.
     * 
     * @return List of generated user session IDs
     * @throws SQLException If a database error occurs
     */
    public List<Integer> seed() throws SQLException {
        LOGGER.info("Seeding user_sessions table with " + numUserSessions + " records...");
        
        if (userIds.isEmpty()) {
            LOGGER.warning("No user IDs provided. Cannot seed user sessions.");
            return List.of();
        }
        
        List<Integer> userSessionIds = new ArrayList<>();
        
        try {
            // Generate a mix of active and expired sessions
            int numActiveSessions = (int) (numUserSessions * 0.3); // 30% active sessions
            int numExpiredSessions = numUserSessions - numActiveSessions; // 70% expired sessions
            
            // Generate active sessions
            for (int i = 0; i < numActiveSessions; i++) {
                // Select a random user
                Integer userId = userIds.get(FakerUtil.generateRandomInt(0, userIds.size() - 1));
                
                // Generate session data
                String sessionToken = UUID.randomUUID().toString();
                String ipAddress = FakerUtil.generateIpAddress();
                String userAgent = FakerUtil.generateUserAgent();
                
                // Generate timestamps
                Timestamp createdAt = TimestampUtil.getRandomTimestampInRange(-7, -1); // Within the last week
                Timestamp expiresAt = TimestampUtil.getRandomTimeAfter(createdAt, 60 * 24, 60 * 24 * 7); // 1-7 days after creation
                
                // Create the user session object
                UserSession userSession = new UserSession();
                userSession.setUserId(userId);
                userSession.setSessionToken(sessionToken);
                userSession.setIpAddress(ipAddress);
                userSession.setUserAgent(userAgent);
                userSession.setCreatedAt(createdAt);
                userSession.setExpiresAt(expiresAt);
                
                // Save the user session
                UserSession createdSession = userSessionDao.create(userSession);
                if (createdSession != null && createdSession.getId() > 0) {
                    userSessionIds.add(createdSession.getId());
                }
            }
            
            // Generate expired sessions
            for (int i = 0; i < numExpiredSessions; i++) {
                // Select a random user
                Integer userId = userIds.get(FakerUtil.generateRandomInt(0, userIds.size() - 1));
                
                // Generate session data
                String sessionToken = UUID.randomUUID().toString();
                String ipAddress = FakerUtil.generateIpAddress();
                String userAgent = FakerUtil.generateUserAgent();
                
                // Generate timestamps for expired sessions
                Timestamp createdAt = TimestampUtil.getRandomTimestampInRange(-60, -8); // 8-60 days ago
                Timestamp expiresAt = TimestampUtil.getRandomTimeAfter(createdAt, 60 * 24, 60 * 24 * 7); // 1-7 days after creation
                
                // Create the user session object
                UserSession userSession = new UserSession();
                userSession.setUserId(userId);
                userSession.setSessionToken(sessionToken);
                userSession.setIpAddress(ipAddress);
                userSession.setUserAgent(userAgent);
                userSession.setCreatedAt(createdAt);
                userSession.setExpiresAt(expiresAt);
                
                // Save the user session
                UserSession createdSession = userSessionDao.create(userSession);
                if (createdSession != null && createdSession.getId() > 0) {
                    userSessionIds.add(createdSession.getId());
                }
            }
            
            LOGGER.info("Successfully seeded " + userSessionIds.size() + " user sessions.");
            return userSessionIds;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error seeding user_sessions table", e);
            throw e;
        }
    }
}