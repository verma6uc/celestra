package com.celestra.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import com.celestra.dao.AbstractBaseDao;
import com.celestra.dao.EnumConverter;
import com.celestra.dao.InvitationDao;
import com.celestra.db.DatabaseUtil;
import com.celestra.enums.InvitationStatus;
import com.celestra.model.Invitation;

/**
 * Implementation of the InvitationDao interface.
 */
public class InvitationDaoImpl extends AbstractBaseDao<Invitation, Integer> implements InvitationDao {
    
    private static final String TABLE_NAME = "invitations";
    private static final String ID_COLUMN = "id";
    private static final String USER_ID_COLUMN = "user_id";
    private static final String TOKEN_COLUMN = "token";
    private static final String STATUS_COLUMN = "status";
    private static final String SENT_AT_COLUMN = "sent_at";
    private static final String EXPIRES_AT_COLUMN = "expires_at";
    private static final String RESEND_COUNT_COLUMN = "resend_count";
    private static final String CREATED_AT_COLUMN = "created_at";
    private static final String UPDATED_AT_COLUMN = "updated_at";
    
    private static final String INSERT_SQL = 
            "INSERT INTO " + TABLE_NAME + " (" + 
            USER_ID_COLUMN + ", " + 
            TOKEN_COLUMN + ", " + 
            STATUS_COLUMN + ", " + 
            SENT_AT_COLUMN + ", " + 
            EXPIRES_AT_COLUMN + ", " + 
            RESEND_COUNT_COLUMN + ", " + 
            CREATED_AT_COLUMN + ", " + 
            UPDATED_AT_COLUMN + 
            ") VALUES (?, ?, ?::invitation_status, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            USER_ID_COLUMN + " = ?, " + 
            TOKEN_COLUMN + " = ?, " + 
            STATUS_COLUMN + " = ?::invitation_status, " + 
            SENT_AT_COLUMN + " = ?, " + 
            EXPIRES_AT_COLUMN + " = ?, " + 
            RESEND_COUNT_COLUMN + " = ?, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String FIND_BY_USER_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + USER_ID_COLUMN + " = ?";
    
    private static final String FIND_BY_STATUS_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COLUMN + " = ?::invitation_status";
    
    private static final String FIND_BY_TOKEN_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + TOKEN_COLUMN + " = ?";
    
    private static final String FIND_EXPIRED_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + 
            "(" + STATUS_COLUMN + " = 'SENT'::invitation_status OR " + STATUS_COLUMN + " = 'PENDING'::invitation_status) AND " + 
            EXPIRES_AT_COLUMN + " < NOW()";
    
    private static final String UPDATE_STATUS_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            STATUS_COLUMN + " = ?::invitation_status, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String INCREMENT_RESEND_COUNT_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            RESEND_COUNT_COLUMN + " = " + RESEND_COUNT_COLUMN + " + 1, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }
    
    @Override
    protected String getIdColumnName() {
        return ID_COLUMN;
    }
    
    @Override
    protected String getInsertSql() {
        return INSERT_SQL;
    }
    
    @Override
    protected String getUpdateSql() {
        return UPDATE_SQL;
    }
    
    @Override
    protected Invitation mapRow(ResultSet rs) throws SQLException {
        Invitation invitation = new Invitation();
        
        invitation.setId(rs.getInt(ID_COLUMN));
        invitation.setUserId(rs.getInt(USER_ID_COLUMN));
        invitation.setToken(rs.getString(TOKEN_COLUMN));
        
        EnumConverter.getEnumFromString(rs, STATUS_COLUMN, InvitationStatus.class)
                .ifPresent(invitation::setStatus);
        
        invitation.setSentAt(rs.getTimestamp(SENT_AT_COLUMN));
        invitation.setExpiresAt(rs.getTimestamp(EXPIRES_AT_COLUMN));
        
        invitation.setResendCount(rs.getInt(RESEND_COUNT_COLUMN));
        invitation.setCreatedAt(rs.getTimestamp(CREATED_AT_COLUMN));
        invitation.setUpdatedAt(rs.getTimestamp(UPDATED_AT_COLUMN));
        
        return invitation;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, Invitation invitation) throws SQLException {
        ps.setInt(1, invitation.getUserId());
        ps.setString(2, invitation.getToken());
        EnumConverter.setEnumAsString(ps, 3, invitation.getStatus());
        
        if (invitation.getSentAt() != null) {
            ps.setTimestamp(4, invitation.getSentAt());
        } else {
            ps.setNull(4, java.sql.Types.TIMESTAMP);
        }
        
        if (invitation.getExpiresAt() != null) {
            ps.setTimestamp(5, invitation.getExpiresAt());
        } else {
            ps.setNull(5, java.sql.Types.TIMESTAMP);
        }
        
        ps.setInt(6, invitation.getResendCount() != null ? invitation.getResendCount() : 0);
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        ps.setTimestamp(7, now);
        ps.setTimestamp(8, now);
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, Invitation invitation) throws SQLException {
        ps.setInt(1, invitation.getUserId());
        ps.setString(2, invitation.getToken());
        EnumConverter.setEnumAsString(ps, 3, invitation.getStatus());
        
        if (invitation.getSentAt() != null) {
            ps.setTimestamp(4, invitation.getSentAt());
        } else {
            ps.setNull(4, java.sql.Types.TIMESTAMP);
        }
        
        if (invitation.getExpiresAt() != null) {
            ps.setTimestamp(5, invitation.getExpiresAt());
        } else {
            ps.setNull(5, java.sql.Types.TIMESTAMP);
        }
        
        ps.setInt(6, invitation.getResendCount() != null ? invitation.getResendCount() : 0);
        ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
        ps.setInt(8, invitation.getId());
    }
    
    @Override
    public Invitation create(Invitation invitation) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(ps, invitation);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating invitation failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    invitation.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating invitation failed, no ID obtained.");
                }
            }
            
            return invitation;
        }
    }
    
    @Override
    public Invitation update(Invitation invitation) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            
            setUpdateParameters(ps, invitation);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating invitation failed, no rows affected.");
            }
            
            return invitation;
        }
    }
    
    @Override
    public List<Invitation> findByUserId(Integer userId) throws SQLException {
        return executeQuery(FIND_BY_USER_ID_SQL, ps -> 
            ps.setInt(1, userId)
        );
    }
    
    @Override
    public List<Invitation> findByStatus(InvitationStatus status) throws SQLException {
        return executeQuery(FIND_BY_STATUS_SQL, ps -> 
            EnumConverter.setEnumAsString(ps, 1, status)
        );
    }
    
    @Override
    public Optional<Invitation> findByToken(String token) throws SQLException {
        return executeQueryForObject(FIND_BY_TOKEN_SQL, ps -> 
            ps.setString(1, token)
        );
    }
    
    @Override
    public List<Invitation> findExpired() throws SQLException {
        return executeQuery(FIND_EXPIRED_SQL, ps -> {
            // No parameters needed
        });
    }
    
    @Override
    public boolean updateStatus(Integer id, InvitationStatus status) throws SQLException {
        return executeUpdate(UPDATE_STATUS_SQL, ps -> {
            EnumConverter.setEnumAsString(ps, 1, status);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, id);
        }) > 0;
    }
    
    @Override
    public boolean incrementResendCount(Integer id) throws SQLException {
        return executeUpdate(INCREMENT_RESEND_COUNT_SQL, ps -> {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, id);
        }) > 0;
    }
}