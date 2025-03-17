package com.celestra.seeding.seeders;

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
    
    private static final String INSERT_USER_SQL = 
            "INSERT INTO users (company_id, role, email, name, password_hash, status, created_at, updated_at) " +
            "VALUES (?, ?::user_role, ?, ?, ?, ?::user_status, ?, ?)";
    
    // User role distribution (SUPER_ADMIN, COMPANY_ADMIN, SPACE_ADMIN, REGULAR_USER)
    private static final double[] USER_ROLE_DISTRIBUTION = {0.02, 0.2, 0.08, 0.7};
    
    // User status distribution (ACTIVE, SUSPENDED, BLOCKED, ARCHIVED)
    private static final double[] USER_STATUS_DISTRIBUTION = {0.85, 0.1, 0.03, 0.02};
    
    private final Connection connection;
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
        
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_USER_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            // Normalize the distribution weights
            double[] userRoleWeights = EnumUtil.createNormalizedWeights(UserRole.class, USER_ROLE_DISTRIBUTION);
            double[] userStatusWeights = EnumUtil.createNormalizedWeights(UserStatus.class, USER_STATUS_DISTRIBUTION);
            
            // Create a super admin user first
            createSuperAdmin(statement, userIds);
            
            // Create company admin users for each company
            createCompanyAdmins(statement, userIds);
            
            // Create regular users
            int remainingUsers = numUsers - userIds.size();
            createRegularUsers(statement, userIds, remainingUsers, userRoleWeights, userStatusWeights);
            
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
     * @param statement Prepared statement for inserting users
     * @param userIds List to add the generated user ID to
     * @throws SQLException If a database error occurs
     */
    private void createSuperAdmin(PreparedStatement statement, List<Integer> userIds) throws SQLException {
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
        
        // Set parameters
        statement.setNull(1, java.sql.Types.INTEGER); // No company for super admin
        statement.setString(2, role.name());
        statement.setString(3, email);
        statement.setString(4, name);
        statement.setString(5, passwordHash);
        statement.setString(6, status.name());
        statement.setTimestamp(7, createdAt);
        statement.setTimestamp(8, updatedAt);
        
        // Execute the insert
        statement.executeUpdate();
        
        // Get the generated ID
        try (var generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                userIds.add(generatedKeys.getInt(1));
            }
        }
    }
    
    /**
     * Create company admin users for each company.
     * 
     * @param statement Prepared statement for inserting users
     * @param userIds List to add the generated user IDs to
     * @throws SQLException If a database error occurs
     */
    private void createCompanyAdmins(PreparedStatement statement, List<Integer> userIds) throws SQLException {
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
            
            // Set parameters
            statement.setInt(1, companyId);
            statement.setString(2, role.name());
            statement.setString(3, email);
            statement.setString(4, name);
            statement.setString(5, passwordHash);
            statement.setString(6, status.name());
            statement.setTimestamp(7, createdAt);
            statement.setTimestamp(8, updatedAt);
            
            // Execute the insert
            statement.executeUpdate();
            
            // Get the generated ID
            try (var generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    userIds.add(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    /**
     * Create regular users.
     * 
     * @param statement Prepared statement for inserting users
     * @param userIds List to add the generated user IDs to
     * @param numRegularUsers Number of regular users to create
     * @param userRoleWeights Weights for user role distribution
     * @param userStatusWeights Weights for user status distribution
     * @throws SQLException If a database error occurs
     */
    private void createRegularUsers(PreparedStatement statement, List<Integer> userIds, 
                                   int numRegularUsers, double[] userRoleWeights, 
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
            
            // Set parameters
            statement.setInt(1, companyId);
            statement.setString(2, role.name());
            statement.setString(3, email);
            statement.setString(4, name);
            statement.setString(5, passwordHash);
            statement.setString(6, status.name());
            statement.setTimestamp(7, createdAt);
            statement.setTimestamp(8, updatedAt);
            
            // Execute the insert
            statement.executeUpdate();
            
            // Get the generated ID
            try (var generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    userIds.add(generatedKeys.getInt(1));
                }
            }
        }
    }
}