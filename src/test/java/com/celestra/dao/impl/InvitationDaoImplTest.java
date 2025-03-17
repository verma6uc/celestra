package com.celestra.dao.impl;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.celestra.dao.BaseDaoTest;
import com.celestra.dao.InvitationDao;
import com.celestra.enums.InvitationStatus;
import com.celestra.enums.UserRole;
import com.celestra.enums.UserStatus;
import com.celestra.model.Invitation;
import com.celestra.db.DatabaseUtil;

/**
 * Test class for InvitationDaoImpl.
 */
public class InvitationDaoImplTest extends BaseDaoTest {
    
    private InvitationDao invitationDao;
    
    /**
     * Initialize the DAO before each test.
     */
    @Before
    public void initialize() {
        invitationDao = new InvitationDaoImpl();
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // Invitations table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // Insert test companies first (to satisfy foreign key constraints)
        executeSQL("INSERT INTO companies (name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES ('Test Company 1', 'Test Company Description 1', 'SMALL'::company_size, 'TECH'::company_vertical, 'ACTIVE'::company_status, NOW(), NOW()) RETURNING id");
        
        // Insert test users (to satisfy foreign key constraints)
        executeSQL("INSERT INTO users (company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES ((SELECT id FROM companies WHERE name = 'Test Company 1'), 'COMPANY_ADMIN'::user_role, 'admin@test.com', 'Admin User', 'hash123', 'ACTIVE'::user_status, NOW(), NOW()) RETURNING id");
        
        executeSQL("INSERT INTO users (company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES ((SELECT id FROM companies WHERE name = 'Test Company 1'), 'REGULAR_USER'::user_role, 'user@test.com', 'Regular User', 'hash456', 'ACTIVE'::user_status, NOW(), NOW()) RETURNING id");
        
        // Insert test invitations with nextval for id to avoid conflicts
        executeSQL("INSERT INTO invitations (id, user_id, token, status, sent_at, expires_at, resend_count, created_at, updated_at) " +
                   "VALUES (nextval('invitations_id_seq'), (SELECT id FROM users WHERE email = 'admin@test.com'), 'token123', 'PENDING'::invitation_status, NULL, NOW() + INTERVAL '7 days', 0, NOW(), NOW())");
        
        executeSQL("INSERT INTO invitations (id, user_id, token, status, sent_at, expires_at, resend_count, created_at, updated_at) " +
                   "VALUES (nextval('invitations_id_seq'), (SELECT id FROM users WHERE email = 'user@test.com'), 'token456', 'SENT'::invitation_status, NOW(), NOW() + INTERVAL '7 days', 1, NOW(), NOW())");
        
        executeSQL("INSERT INTO invitations (id, user_id, token, status, sent_at, expires_at, resend_count, created_at, updated_at) " +
                   "VALUES (nextval('invitations_id_seq'), (SELECT id FROM users WHERE email = 'admin@test.com'), 'token789', 'EXPIRED'::invitation_status, NOW() - INTERVAL '14 days', NOW() - INTERVAL '7 days', 2, NOW(), NOW())");
    }

    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM invitations WHERE token LIKE 'token%' OR token LIKE 'test%'");
        executeSQL("DELETE FROM users WHERE email IN ('admin@test.com', 'user@test.com')");
        executeSQL("DELETE FROM companies WHERE name = 'Test Company 1'");
    }
    
    /**
     * Test the create method.
     */
    @Test
    public void testCreate() throws SQLException {
        // Create a new invitation
        Invitation invitation = new Invitation();
        invitation.setUserId(getUserId("admin@test.com"));
        invitation.setToken("test-token-" + UUID.randomUUID().toString());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setResendCount(0);
        
        Invitation createdInvitation = invitationDao.create(invitation);
        
        // Verify the invitation was created
        assertNotNull("Created invitation should not be null", createdInvitation);
        assertTrue("Created invitation should have an ID", createdInvitation.getId() > 0);
        
        // Clean up
        boolean deleted = invitationDao.delete(createdInvitation.getId());
        assertTrue("Invitation should be deleted successfully", deleted);
    }
    
    /**
     * Test the findById method.
     */
    @Test
    public void testFindById() throws SQLException {
        // Find all invitations
        List<Invitation> invitations = invitationDao.findAll();
        
        // Verify there are invitations
        assertFalse("There should be invitations in the database", invitations.isEmpty());
        
        // Get the first invitation
        Invitation invitation = invitations.get(0);
        
        // Find the invitation by ID
        Optional<Invitation> foundInvitation = invitationDao.findById(invitation.getId());
        
        // Verify the invitation was found
        assertTrue("Invitation should be found by ID", foundInvitation.isPresent());
        assertEquals("Found invitation ID should match", invitation.getId(), foundInvitation.get().getId());
        assertEquals("Found invitation token should match", invitation.getToken(), foundInvitation.get().getToken());
    }
    
    /**
     * Test the findAll method.
     */
    @Test
    public void testFindAll() throws SQLException {
        // Find all invitations
        List<Invitation> invitations = invitationDao.findAll();
        
        // Verify there are invitations
        assertFalse("There should be invitations in the database", invitations.isEmpty());
    }
    
    /**
     * Test the update method.
     */
    @Test
    public void testUpdate() throws SQLException {
        // Create a new invitation
        Invitation invitation = new Invitation();
        invitation.setUserId(getUserId("admin@test.com"));
        invitation.setToken("test-token-update-" + UUID.randomUUID().toString());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setResendCount(0);
        
        Invitation createdInvitation = invitationDao.create(invitation);
        
        // Update the invitation
        createdInvitation.setStatus(InvitationStatus.SENT);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        createdInvitation.setSentAt(now);
        createdInvitation.setResendCount(1);
        
        Invitation updatedInvitation = invitationDao.update(createdInvitation);
        
        // Verify the invitation was updated
        assertEquals("Invitation status should be updated", InvitationStatus.SENT, updatedInvitation.getStatus());
        assertNotNull("Invitation sent at should be set", updatedInvitation.getSentAt());
        assertEquals("Invitation resend count should be updated", Integer.valueOf(1), updatedInvitation.getResendCount());
        
        // Clean up
        boolean deleted = invitationDao.delete(createdInvitation.getId());
        assertTrue("Invitation should be deleted successfully", deleted);
    }
    
    /**
     * Test the delete method.
     */
    @Test
    public void testDelete() throws SQLException {
        // Create a new invitation
        Invitation invitation = new Invitation();
        invitation.setUserId(getUserId("admin@test.com"));
        invitation.setToken("test-token-delete-" + UUID.randomUUID().toString());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setResendCount(0);
        
        Invitation createdInvitation = invitationDao.create(invitation);
        
        // Delete the invitation
        boolean deleted = invitationDao.delete(createdInvitation.getId());
        
        // Verify the invitation was deleted
        assertTrue("Invitation should be deleted successfully", deleted);
        
        Optional<Invitation> foundInvitation = invitationDao.findById(createdInvitation.getId());
        assertFalse("Invitation should not be found after deletion", foundInvitation.isPresent());
    }
    
    /**
     * Test the findByUserId method.
     */
    @Test
    public void testFindByUserId() throws SQLException {
        // Find invitations by user ID
        List<Invitation> invitations = invitationDao.findByUserId(getUserId("admin@test.com"));
        
        // Verify there are invitations
        assertFalse("There should be invitations for admin user", invitations.isEmpty());
        
        // Verify all entries have the correct user ID
        for (Invitation invitation : invitations) {
            assertEquals("Invitation user ID should match admin user", getUserId("admin@test.com"), invitation.getUserId());
        }
    }
    
    /**
     * Test the findByStatus method.
     */
    @Test
    public void testFindByStatus() throws SQLException {
        // Find invitations by status
        List<Invitation> invitations = invitationDao.findByStatus(InvitationStatus.PENDING);
        
        // Verify there are invitations
        assertFalse("There should be pending invitations", invitations.isEmpty());
        
        // Verify all entries have the correct status
        for (Invitation invitation : invitations) {
            assertEquals("Invitation status should be PENDING", InvitationStatus.PENDING, invitation.getStatus());
        }
    }
    
    /**
     * Test the findByToken method.
     */
    @Test
    public void testFindByToken() throws SQLException {
        // Find invitation by token
        Optional<Invitation> invitation = invitationDao.findByToken("token123");
        
        // Verify the invitation was found
        assertTrue("Invitation should be found by token", invitation.isPresent());
        assertEquals("Found invitation token should match", "token123", invitation.get().getToken());
    }
    
    /**
     * Test the findExpired method.
     */
    @Test
    public void testFindExpired() throws SQLException {
        // Create a new invitation that is expired
        Invitation invitation = new Invitation();
        invitation.setUserId(getUserId("admin@test.com"));
        invitation.setToken("test-token-expired-" + UUID.randomUUID().toString());
        invitation.setStatus(InvitationStatus.SENT);
        // Set sent date to 14 days ago
        invitation.setSentAt(new Timestamp(System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000L));
        // Set expiry date to 7 days ago
        invitation.setExpiresAt(new Timestamp(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L));
        invitation.setResendCount(0);
        
        Invitation createdInvitation = invitationDao.create(invitation);
        
        // Find expired invitations
        List<Invitation> expiredInvitations = invitationDao.findExpired();
        
        // Verify there are expired invitations
        assertFalse("There should be expired invitations", expiredInvitations.isEmpty());
        
        // Clean up
        boolean deleted = invitationDao.delete(createdInvitation.getId());
        assertTrue("Invitation should be deleted successfully", deleted);
    }
    
    /**
     * Test the updateStatus method.
     */
    @Test
    public void testUpdateStatus() throws SQLException {
        // Create a new invitation
        Invitation invitation = new Invitation();
        invitation.setUserId(getUserId("admin@test.com"));
        invitation.setToken("test-token-status-" + UUID.randomUUID().toString());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setResendCount(0);
        
        Invitation createdInvitation = invitationDao.create(invitation);
        
        // Update the invitation status
        boolean updated = invitationDao.updateStatus(createdInvitation.getId(), InvitationStatus.SENT);
        
        // Verify the invitation status was updated
        assertTrue("Invitation status should be updated successfully", updated);
        
        Optional<Invitation> updatedInvitation = invitationDao.findById(createdInvitation.getId());
        assertTrue("Invitation should be found after status update", updatedInvitation.isPresent());
        assertEquals("Invitation status should be updated to SENT", InvitationStatus.SENT, updatedInvitation.get().getStatus());
        
        // Clean up
        boolean deleted = invitationDao.delete(createdInvitation.getId());
        assertTrue("Invitation should be deleted successfully", deleted);
    }
    
    /**
     * Test the incrementResendCount method.
     */
    @Test
    public void testIncrementResendCount() throws SQLException {
        // Create a new invitation
        Invitation invitation = new Invitation();
        invitation.setUserId(getUserId("admin@test.com"));
        invitation.setToken("test-token-resend-" + UUID.randomUUID().toString());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setResendCount(0);
        
        Invitation createdInvitation = invitationDao.create(invitation);
        
        // Increment the resend count
        boolean incremented = invitationDao.incrementResendCount(createdInvitation.getId());
        
        // Verify the resend count was incremented
        assertTrue("Invitation resend count should be incremented successfully", incremented);
        
        Optional<Invitation> updatedInvitation = invitationDao.findById(createdInvitation.getId());
        assertTrue("Invitation should be found after resend count increment", updatedInvitation.isPresent());
        assertEquals("Invitation resend count should be incremented", Integer.valueOf(1), updatedInvitation.get().getResendCount());
        
        // Clean up
        boolean deleted = invitationDao.delete(createdInvitation.getId());
        assertTrue("Invitation should be deleted successfully", deleted);
    }
    
    /**
     * Helper method to get the ID of a user by email.
     * 
     * @param email The email of the user
     * @return The ID of the user
     * @throws SQLException if a database error occurs
     */
    private Integer getUserId(String email) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
            ps.setString(1, email);
            var rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : null;
        }
    }
}