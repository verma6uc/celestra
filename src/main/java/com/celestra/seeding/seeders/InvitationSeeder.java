package com.celestra.seeding.seeders;

import com.celestra.dao.InvitationDao;
import com.celestra.dao.impl.InvitationDaoImpl;
import com.celestra.model.Invitation;
import com.celestra.enums.InvitationStatus;
import com.celestra.seeding.util.EnumUtil;
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
 * Seeder class for the invitations table.
 * This class is responsible for generating and inserting test data for invitations.
 * It uses the InvitationDao to interact with the database.
 */
public class InvitationSeeder {
    
    private static final Logger LOGGER = Logger.getLogger(InvitationSeeder.class.getName());
    
    // Invitation status distribution (PENDING, ACCEPTED, EXPIRED, REVOKED)
    private static final double[] INVITATION_STATUS_DISTRIBUTION = {0.4, 0.3, 0.2, 0.1};
    
    private final Connection connection;
    private final InvitationDao invitationDao;
    private final int numInvitations;
    private final List<Integer> userIds;
    
    /**
     * Constructor for InvitationSeeder.
     * 
     * @param connection Database connection
     * @param numInvitations Number of invitations to seed
     * @param userIds List of user IDs to associate invitations with
     */
    public InvitationSeeder(Connection connection, int numInvitations, List<Integer> userIds) {
        this.connection = connection;
        this.invitationDao = new InvitationDaoImpl();
        this.numInvitations = numInvitations;
        this.userIds = userIds;
    }
    
    /**
     * Seed the invitations table with test data.
     * 
     * @return List of generated invitation IDs
     * @throws SQLException If a database error occurs
     */
    public List<Integer> seed() throws SQLException {
        LOGGER.info("Seeding invitations table with " + numInvitations + " records...");
        
        if (userIds.isEmpty()) {
            LOGGER.warning("No user IDs provided. Cannot seed invitations.");
            return List.of();
        }
        
        List<Integer> invitationIds = new ArrayList<>();
        
        try {
            // Normalize the distribution weights
            double[] invitationStatusWeights = EnumUtil.createNormalizedWeights(InvitationStatus.class, INVITATION_STATUS_DISTRIBUTION);
            
            for (int i = 0; i < numInvitations; i++) {
                // Select a random user
                Integer userId = userIds.get(FakerUtil.generateRandomInt(0, userIds.size() - 1));
                String email = FakerUtil.generateEmail();
                String name = FakerUtil.generatePersonName();
                String token = UUID.randomUUID().toString();
                InvitationStatus status = EnumUtil.getWeightedRandomEnumValue(InvitationStatus.class, invitationStatusWeights);
                
                // Generate timestamps
                Timestamp[] timestamps = TimestampUtil.getCreatedUpdatedTimestamps(90, 30, 1440);
                Timestamp createdAt = timestamps[0];
                Timestamp updatedAt = timestamps[1];
                
                // Generate sent timestamp (same as created for simplicity)
                Timestamp sentAt = createdAt;
                
                // Generate expiration timestamp (7 days after creation)
                Timestamp expiresAt = new Timestamp(createdAt.getTime() + (7 * 24 * 60 * 60 * 1000));
                
                // Set resend count (0-3)
                Integer resendCount = FakerUtil.generateRandomInt(0, 3);
                
                // Create the invitation object
                Invitation invitation = new Invitation();
                invitation.setUserId(userId);
                invitation.setToken(token);
                invitation.setStatus(status);
                invitation.setSentAt(sentAt);
                invitation.setExpiresAt(expiresAt);
                invitation.setResendCount(resendCount);
                invitation.setCreatedAt(createdAt);
                invitation.setUpdatedAt(updatedAt);

                // Save the invitation
                Invitation createdInvitation = invitationDao.create(invitation);
                if (createdInvitation != null && createdInvitation.getId() > 0) {
                    invitationIds.add(createdInvitation.getId());
                }
            }
            
            LOGGER.info("Successfully seeded " + invitationIds.size() + " invitations.");
            return invitationIds;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error seeding invitations table", e);
            throw e;
        }
    }
}