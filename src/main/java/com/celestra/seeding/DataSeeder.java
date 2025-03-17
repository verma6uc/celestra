package com.celestra.seeding;

import com.celestra.db.DatabaseUtil;
import com.celestra.dao.AgentDao;
import com.celestra.dao.KnowledgeBaseDao;
import com.celestra.dao.impl.AgentDaoImpl;
import com.celestra.dao.impl.KnowledgeBaseDaoImpl;
import com.celestra.seeding.seeders.CompanySeeder;
import com.celestra.seeding.seeders.KnowledgeTypeSeeder;
import com.celestra.seeding.seeders.AuditChangeLogSeeder;
import com.celestra.seeding.seeders.AgentSeeder;
import com.celestra.seeding.seeders.KnowledgeBaseSeeder;
import com.celestra.seeding.seeders.AgentKnowledgeBaseSeeder;
import com.celestra.seeding.seeders.AuditLogSeeder;
import com.celestra.seeding.seeders.FailedLoginSeeder;
import com.celestra.seeding.seeders.InvitationSeeder;
import com.celestra.seeding.seeders.KnowledgeSourceSeeder;
import com.celestra.seeding.seeders.NotificationSeeder;
import com.celestra.seeding.seeders.PasswordHistorySeeder;
import com.celestra.seeding.seeders.UserLockoutSeeder;
import com.celestra.seeding.seeders.UserSessionSeeder;
import com.celestra.seeding.seeders.UserSeeder;
import com.celestra.seeding.util.FakerUtil;
import com.celestra.seeding.util.EnumUtil;
import com.celestra.seeding.util.PasswordUtil;
import com.celestra.seeding.util.TimestampUtil;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for seeding the database with test data.
 * This class coordinates the seeding process, ensuring that tables are seeded in the correct order
 * and with appropriate relationships.
 */
public class DataSeeder {
    
    private static final Logger LOGGER = Logger.getLogger(DataSeeder.class.getName());
    
    // Seeding configuration
    private static final int NUM_COMPANIES = 8;
    private static final int NUM_USERS = 40;
    private static final int NUM_AGENTS = 15;
    private static final int NUM_KNOWLEDGE_BASES = 25;
    private static final int NUM_KNOWLEDGE_TYPES = 8;
    private static final int NUM_KNOWLEDGE_SOURCES = 50;
    private static final int NUM_AGENT_KNOWLEDGE_BASES = 30;
    private static final int NUM_USER_SESSIONS = 80;
    private static final int NUM_FAILED_LOGINS = 30;
    private static final int NUM_AUDIT_LOGS = 150;
    private static final int NUM_AUDIT_CHANGE_LOGS = 300;
    private static final int NUM_NOTIFICATIONS = 150;
    private static final int NUM_INVITATIONS = 30;
    private static final int NUM_PASSWORD_HISTORY = 80;
    private static final int NUM_USER_LOCKOUTS = 15;
    
    // Store generated IDs for relationships
    private static List<Integer> companyIds;
    private static List<Integer> knowledgeTypeIds;
    private static List<Integer> userIds;
    private static List<Integer> agentIds;
    private static List<Integer> knowledgeBaseIds;
    private static List<Integer> agentKnowledgeBaseIds;
    private static List<Integer> auditLogIds;
    private static List<Integer> failedLoginIds;
    private static List<Integer> userSessionIds;
    private static List<Integer> userLockoutIds;
    private static List<Integer> invitationIds;
    private static List<Integer> notificationIds;
    private static List<Integer> passwordHistoryIds;
    private static List<Integer> auditChangeLogIds;
    private static List<Integer> knowledgeSourceIds;
    /**
     * Main method to run the data seeding process.
     * 
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        LOGGER.info("Starting data seeding process...");
        
        try {
            // Initialize the database connection
            Connection connection = DatabaseUtil.getConnection();
            
            try {
                // Start a transaction
                connection.setAutoCommit(false);
                
                // Seed the database in the correct order
                seedDatabase(connection);
                
                // Commit the transaction
                connection.commit();
                
                LOGGER.info("Data seeding completed successfully!");
            } catch (SQLException e) {
                // Rollback the transaction on error
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", rollbackEx);
                }
                
                LOGGER.log(Level.SEVERE, "Error seeding database", e);
            } finally {
                // Reset auto-commit and close the connection
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    LOGGER.log(Level.SEVERE, "Error closing connection", closeEx);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting database connection", e);
        }
    }
    
    /**
     * Seed the database with test data.
     * 
     * @param connection Database connection
     * @throws SQLException If a database error occurs
     */
    private static void seedDatabase(Connection connection) throws SQLException {
        LOGGER.info("Seeding database...");
        
        // Phase 1: Independent Tables
        seedCompanies(connection);
        seedKnowledgeTypes(connection);
        
        // Phase 2: First-Level Dependent Tables
        seedUsers(connection);
        seedAgents(connection);
        seedKnowledgeBases(connection);
        
        // Phase 3: Second-Level Dependent Tables
        seedAgentKnowledgeBases(connection);
        seedKnowledgeSources(connection);
        seedAuditLogs(connection);
        seedFailedLogins(connection);
        seedInvitations(connection);
        seedNotifications(connection);
        seedPasswordHistory(connection);
        seedUserLockouts(connection);
        seedUserSessions(connection);
        
        // Phase 4: Third-Level Dependent Tables
        seedAuditChangeLogs(connection);
        
        LOGGER.info("Database seeding completed.");
    }
    
    /**
     * Seed the companies table.
     * 
     * @param connection Database connection
     * @throws SQLException If a database error occurs
     */
    private static void seedCompanies(Connection connection) throws SQLException {
        LOGGER.info("Seeding companies table...");
        
        CompanySeeder companySeeder = new CompanySeeder(connection, NUM_COMPANIES);
        companyIds = companySeeder.seed();
        
        LOGGER.info("Seeded " + companyIds.size() + " companies.");
    }
    
    /**
     * Seed the knowledge_types table.
     * 
     * @param connection Database connection
     * @throws SQLException If a database error occurs
     */
    private static void seedKnowledgeTypes(Connection connection) throws SQLException {
        LOGGER.info("Seeding knowledge_types table...");
        
        KnowledgeTypeSeeder knowledgeTypeSeeder = new KnowledgeTypeSeeder(connection, NUM_KNOWLEDGE_TYPES);
        knowledgeTypeIds = knowledgeTypeSeeder.seed();
        
        LOGGER.info("Seeded " + knowledgeTypeIds.size() + " knowledge types.");
    }
    
    /**
     * Seed the users table.
     * 
     * @param connection Database connection
     * @throws SQLException If a database error occurs
     */
    private static void seedUsers(Connection connection) throws SQLException {
        LOGGER.info("Seeding users table...");
        
        UserSeeder userSeeder = new UserSeeder(connection, NUM_USERS, companyIds);
        userIds = userSeeder.seed();
        
        LOGGER.info("Seeded " + userIds.size() + " users.");
    }
    
    /**
     * Seed the agents table.
     * 
     * @param connection Database connection
     * @throws SQLException If a database error occurs
     */
    private static void seedAgents(Connection connection) throws SQLException {
        LOGGER.info("Seeding agents table...");
        
        // Get company verticals for agent type assignment
        HashMap<Integer, String> companyVerticals = getCompanyVerticals(connection);
        
        AgentSeeder agentSeeder = new AgentSeeder(connection, NUM_AGENTS, companyIds, companyVerticals);
        agentIds = agentSeeder.seed();
        
        LOGGER.info("Seeded " + agentIds.size() + " agents.");
    }
    
    /**
     * Get the vertical for each company.
     * 
     * @param connection Database connection
     * @return Map of company IDs to their verticals
     * @throws SQLException If a database error occurs
     */
    private static HashMap<Integer, String> getCompanyVerticals(Connection connection) throws SQLException {
        HashMap<Integer, String> companyVerticals = new HashMap<>();
        
        String sql = "SELECT id, vertical FROM companies";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                int companyId = resultSet.getInt("id");
                String vertical = resultSet.getString("vertical");
                companyVerticals.put(companyId, vertical);
            }
        }
        return companyVerticals;
    }
    
    /**
     * Get the name for each knowledge type.
     * 
     * @param connection Database connection
     * @return Map of knowledge type IDs to their names
     * @throws SQLException If a database error occurs
     */
    private static HashMap<Integer, String> getKnowledgeTypeNames(Connection connection) throws SQLException {
        HashMap<Integer, String> knowledgeTypeNames = new HashMap<>();
        
        String sql = "SELECT id, name FROM knowledge_types";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                knowledgeTypeNames.put(resultSet.getInt("id"), resultSet.getString("name"));
            }
        }
        return knowledgeTypeNames;
    }
    
    /**
     * Seed the knowledge_bases table.
     * 
     * @param connection Database connection
     * @throws SQLException If a database error occurs
     */
    private static void seedKnowledgeBases(Connection connection) throws SQLException {
        LOGGER.info("Seeding knowledge_bases table...");
        
        // Get company verticals for knowledge base type assignment
        HashMap<Integer, String> companyVerticals = getCompanyVerticals(connection);
        
        KnowledgeBaseSeeder knowledgeBaseSeeder = new KnowledgeBaseSeeder(connection, NUM_KNOWLEDGE_BASES, companyIds, companyVerticals);
        knowledgeBaseIds = knowledgeBaseSeeder.seed();
        
        LOGGER.info("Seeded " + knowledgeBaseIds.size() + " knowledge bases.");
    }
    
    /**
     * Seed the agent_knowledge_bases table.
     * 
     * @param connection Database connection
     * @throws SQLException If a database error occurs
     */
    private static void seedAgentKnowledgeBases(Connection connection) throws SQLException {
        LOGGER.info("Seeding agent_knowledge_bases table...");
        
        AgentDao agentDao = new AgentDaoImpl();
        KnowledgeBaseDao knowledgeBaseDao = new KnowledgeBaseDaoImpl();
        
        AgentKnowledgeBaseSeeder agentKnowledgeBaseSeeder = new AgentKnowledgeBaseSeeder(connection, NUM_AGENT_KNOWLEDGE_BASES, agentDao, knowledgeBaseDao);
        agentKnowledgeBaseIds = agentKnowledgeBaseSeeder.seed();
        
        LOGGER.info("Seeded " + agentKnowledgeBaseIds.size() + " agent knowledge base relationships.");
    }
    
    /**
     * Seed the knowledge_sources table.
     * 
     * @param connection Database connection
     * @throws SQLException If a database error occurs
     */
    private static void seedKnowledgeSources(Connection connection) throws SQLException {
        LOGGER.info("Seeding knowledge_sources table...");
        
        // Get knowledge type names for source type assignment
        HashMap<Integer, String> knowledgeTypeNames = getKnowledgeTypeNames(connection);
        
        KnowledgeSourceSeeder knowledgeSourceSeeder = new KnowledgeSourceSeeder(connection, NUM_KNOWLEDGE_SOURCES, knowledgeBaseIds, knowledgeTypeIds, knowledgeTypeNames);
        knowledgeSourceIds = knowledgeSourceSeeder.seed();
        
        LOGGER.info("Seeded " + knowledgeSourceIds.size() + " knowledge sources.");
    }
    
    /**
     * Seed the audit_logs table.
     * 
     * @param connection Database connection
     * @throws SQLException If a database error occurs
     */
    private static void seedAuditLogs(Connection connection) throws SQLException {
        LOGGER.info("Seeding audit_logs table...");
        
        AuditLogSeeder auditLogSeeder = new AuditLogSeeder(connection, NUM_AUDIT_LOGS, userIds, companyIds);
        auditLogIds = auditLogSeeder.seed();
        
        LOGGER.info("Seeded " + auditLogIds.size() + " audit logs.");
    }
    
    /**
     * Seed the failed_logins table.
     * 
     * @param connection Database connection
     * @throws SQLException If a database error occurs
     */
    private static void seedFailedLogins(Connection connection) throws SQLException {
        LOGGER.info("Seeding failed_logins table...");
        
        FailedLoginSeeder failedLoginSeeder = new FailedLoginSeeder(connection, NUM_FAILED_LOGINS, userIds);
        failedLoginIds = failedLoginSeeder.seed();
        
        LOGGER.info("Seeded " + failedLoginIds.size() + " failed logins.");
    }
    
    /**
     * Seed the invitations table.
     * 
     * @param connection Database connection
     * @throws SQLException If a database error occurs
     */
    private static void seedInvitations(Connection connection) throws SQLException {
        LOGGER.info("Seeding invitations table...");
        
        InvitationSeeder invitationSeeder = new InvitationSeeder(connection, NUM_INVITATIONS, userIds);
        invitationIds = invitationSeeder.seed();
        
        LOGGER.info("Seeded " + invitationIds.size() + " invitations.");
    }
    
    /**
     * Seed the notifications table.
     * 
     * @param connection Database connection
     * @throws SQLException If a database error occurs
     */
    private static void seedNotifications(Connection connection) throws SQLException {
        LOGGER.info("Seeding notifications table...");
        
        NotificationSeeder notificationSeeder = new NotificationSeeder(connection, NUM_NOTIFICATIONS, companyIds, userIds);
        notificationIds = notificationSeeder.seed();
        
        LOGGER.info("Seeded " + notificationIds.size() + " notifications.");
    }
    
    /**
     * Seed the password_history table.
     * 
     * @param connection Database connection
     * @throws SQLException If a database error occurs
     */
    private static void seedPasswordHistory(Connection connection) throws SQLException {
        LOGGER.info("Seeding password_history table...");
        
        PasswordHistorySeeder passwordHistorySeeder = new PasswordHistorySeeder(connection, NUM_PASSWORD_HISTORY, userIds);
        passwordHistoryIds = passwordHistorySeeder.seed();
        
        LOGGER.info("Seeded " + passwordHistoryIds.size() + " password history entries.");
    }
    
    /**
     * Seed the user_lockouts table.
     * 
     * @param connection Database connection
     * @throws SQLException If a database error occurs
     */
    private static void seedUserLockouts(Connection connection) throws SQLException {
        LOGGER.info("Seeding user_lockouts table...");
        
        UserLockoutSeeder userLockoutSeeder = new UserLockoutSeeder(connection, NUM_USER_LOCKOUTS, userIds);
        userLockoutIds = userLockoutSeeder.seed();
        
        LOGGER.info("Seeded " + userLockoutIds.size() + " user lockouts.");
    }
    
    /**
     * Seed the user_sessions table.
     * 
     * @param connection Database connection
     * @throws SQLException If a database error occurs
     */
    private static void seedUserSessions(Connection connection) throws SQLException {
        LOGGER.info("Seeding user_sessions table...");
        
        UserSessionSeeder userSessionSeeder = new UserSessionSeeder(connection, NUM_USER_SESSIONS, userIds);
        userSessionIds = userSessionSeeder.seed();
        
        LOGGER.info("Seeded " + userSessionIds.size() + " user sessions.");
    }
    
    /**
     * Seed the audit_change_logs table.
     * 
     * @param connection Database connection
     * @throws SQLException If a database error occurs
     */
    private static void seedAuditChangeLogs(Connection connection) throws SQLException {
        LOGGER.info("Seeding audit_change_logs table...");
        
        AuditChangeLogSeeder auditChangeLogSeeder = new AuditChangeLogSeeder(connection, NUM_AUDIT_CHANGE_LOGS, auditLogIds);
        auditChangeLogIds = auditChangeLogSeeder.seed();
        
        LOGGER.info("Seeded " + auditChangeLogIds.size() + " audit change logs.");
    }
}