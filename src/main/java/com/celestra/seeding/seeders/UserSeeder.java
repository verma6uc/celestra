package com.celestra.seeding.seeders;

import com.celestra.dao.UserDao;
import com.celestra.dao.impl.UserDaoImpl;
import com.celestra.model.User;
import com.celestra.enums.UserRole;
import com.celestra.enums.UserStatus;
import com.celestra.seeding.util.EnumUtil;
import com.celestra.seeding.util.FakerUtil;
import com.celestra.seeding.util.PasswordUtil;
import com.celestra.seeding.util.TimestampUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Seeder class for the users table.
 * This class is responsible for generating and inserting test data for users.
 */
public class UserSeeder {
    
    private static final Logger LOGGER = Logger.getLogger(UserSeeder.class.getName());
    
    // User role distribution (SUPER_ADMIN, COMPANY_ADMIN, SPACE_ADMIN, REGULAR_USER)
    private static final double[] USER_ROLE_DISTRIBUTION = {0.02, 0.2, 0.08, 0.7};
    
    // User status distribution (ACTIVE, SUSPENDED, BLOCKED, ARCHIVED)
    private static final double[] USER_STATUS_DISTRIBUTION = {0.85, 0.1, 0.03, 0.02};
    
    private final Connection connection;
    private final UserDao userDao;
    private final int numUsers;
    private final List<Integer> companyIds;
    
    /**
     * Constructor for UserSeeder.
     * 
     * @param connection Database connection
     * @param numUsers Number of users to seed
     * @param companyIds List of company IDs to associate users with
     */
    public UserSeeder(Connection connection, int numUsers, List<Integer> companyIds) {
        this.connection = connection;
        this.userDao = new UserDaoImpl();
        this.numUsers = numUsers;
        this.companyIds = companyIds;
    }
    
    /**
     * Seed the users table with test data.
     * 
     * @return List of generated user IDs
     * @throws SQLException If a database error occurs
     */
    public List<Integer> seed() throws SQLException {
        LOGGER.info("Seeding users table with " + numUsers + " records...");
        
        if (companyIds.isEmpty()) {
            LOGGER.warning("No company IDs provided. Cannot seed users.");
            return List.of();
        }
        
        List<Integer> userIds = new ArrayList<>();
        
        try {
            // Normalize the distribution weights
            double[] userRoleWeights = EnumUtil.createNormalizedWeights(UserRole.class, USER_ROLE_DISTRIBUTION);
            double[] userStatusWeights = EnumUtil.createNormalizedWeights(UserStatus.class, USER_STATUS_DISTRIBUTION);
            
            // Create a super admin user first
            createSuperAdmin(userIds);
            
            // Create company admin users for each company
            createCompanyAdmins(userIds);
            
            // Create regular users
            int remainingUsers = numUsers - userIds.size();
            createRegularUsers(userIds, remainingUsers, userRoleWeights, userStatusWeights);

            LOGGER.info("Successfully seeded " + userIds.size() + " users.");
            return userIds;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error seeding users table", e);
            throw e;
        }
    }
    
    /**
     * Create a super admin user.
     * 
     * @param userIds List to add the generated user ID to
     * @throws SQLException If a database error occurs
     */
    private void createSuperAdmin(List<Integer> userIds) throws SQLException {
        // Generate super admin data
        String name = "System Administrator";
        String email = "admin@celestra.com";
        String passwordHash = PasswordUtil.hashPassword("admin123");
        UserRole role = UserRole.SUPER_ADMIN;
        UserStatus status = UserStatus.ACTIVE;
        
        // Generate timestamps
        Timestamp[] timestamps = TimestampUtil.getCreatedUpdatedTimestamps(365, 30, 1440);
        Timestamp createdAt = timestamps[0];
        Timestamp updatedAt = timestamps[1];
        
        // Create the user object
        User user = new User();
        user.setCompanyId(null); // No company for super admin
        user.setRole(role);
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash(passwordHash);
        user.setStatus(status);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);
        
        // Save the user
        User createdUser = userDao.create(user);
        if (createdUser != null && createdUser.getId() > 0) {
            userIds.add(createdUser.getId());
            }
    }
    
    /**
     * Create company admin users for each company.
     * 
     * @param userIds List to add the generated user IDs to
     * @throws SQLException If a database error occurs
     */
    private void createCompanyAdmins(List<Integer> userIds) throws SQLException {
        for (Integer companyId : companyIds) {
            // Generate company admin data
            String name = FakerUtil.generatePersonName();
            String email = "admin_" + companyId + "@" + FakerUtil.getFaker().internet().domainName();
            String passwordHash = PasswordUtil.generateRandomPasswordHash();
            UserRole role = UserRole.COMPANY_ADMIN;
            UserStatus status = UserStatus.ACTIVE;
            
            // Generate timestamps
            Timestamp[] timestamps = TimestampUtil.getCreatedUpdatedTimestamps(365, 30, 1440);
            Timestamp createdAt = timestamps[0];
            Timestamp updatedAt = timestamps[1];
            
            // Create the user object
            User user = new User();
            user.setCompanyId(companyId);
            user.setRole(role);
            user.setEmail(email);
            user.setName(name);
            user.setPasswordHash(passwordHash);
            user.setStatus(status);
            user.setCreatedAt(createdAt);
            user.setUpdatedAt(updatedAt);
            
            // Save the user
            User createdUser = userDao.create(user);
            if (createdUser != null && createdUser.getId() > 0) {
                userIds.add(createdUser.getId());
                }
        }
    }
    
    /**
     * Create regular users.
     * 
     * @param userIds List to add the generated user IDs to
     * @param numRegularUsers Number of regular users to create
     * @param userRoleWeights Weights for user role distribution
     * @param userStatusWeights Weights for user status distribution
     * @throws SQLException If a database error occurs
     */
    private void createRegularUsers(List<Integer> userIds, int numRegularUsers, double[] userRoleWeights, 
                                   double[] userStatusWeights) throws SQLException {
        for (int i = 0; i < numRegularUsers; i++) {
            // Generate user data
            String name = FakerUtil.generatePersonName();
            String email = FakerUtil.generateEmail();
            String passwordHash = PasswordUtil.generateRandomPasswordHash();
            
            // For regular users, don't generate SUPER_ADMIN role
            UserRole role;
            do {
                role = EnumUtil.getWeightedRandomEnumValue(UserRole.class, userRoleWeights);
            } while (role == UserRole.SUPER_ADMIN);
            
            UserStatus status = EnumUtil.getWeightedRandomEnumValue(UserStatus.class, userStatusWeights);
            
            // Randomly assign to a company
            Integer companyId = companyIds.get(FakerUtil.generateRandomInt(0, companyIds.size() - 1));
            
            // Generate timestamps
            Timestamp[] timestamps = TimestampUtil.getCreatedUpdatedTimestamps(365, 30, 1440);
            Timestamp createdAt = timestamps[0];
            Timestamp updatedAt = timestamps[1];
            
            // Create the user object
            User user = new User();
            user.setCompanyId(companyId);
            user.setRole(role);
            user.setEmail(email);
            user.setName(name);
            user.setPasswordHash(passwordHash);
            user.setStatus(status);
            user.setCreatedAt(createdAt);
            user.setUpdatedAt(updatedAt);
            
            // Save the user
            User createdUser = userDao.create(user);
            if (createdUser != null && createdUser.getId() > 0) {
                userIds.add(createdUser.getId());
                }
        }
    }
}