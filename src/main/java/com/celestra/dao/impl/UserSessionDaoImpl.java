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
import com.celestra.dao.UserSessionDao;
import com.celestra.db.DatabaseUtil;
import com.celestra.model.UserSession;

/**
 * Implementation of the UserSessionDao interface.
 */
public class UserSessionDaoImpl extends AbstractBaseDao<UserSession, Integer> implements UserSessionDao {
    
    private static final String TABLE_NAME = "user_sessions";
    private static final String ID_COLUMN = "id";
    private static final String USER_ID_COLUMN = "user_id";
    private static final String SESSION_TOKEN_COLUMN = "session_token";
    private static final String IP_ADDRESS_COLUMN = "ip_address";
    private static final String USER_AGENT_COLUMN = "user_agent";
    private static final String CREATED_AT_COLUMN = "created_at";
    private static final String EXPIRES_AT_COLUMN = "expires_at";
    
    private static final String INSERT_SQL = 
            "INSERT INTO " + TABLE_NAME + " (" + 
            USER_ID_COLUMN + ", " + 
            SESSION_TOKEN_COLUMN + ", " + 
            IP_ADDRESS_COLUMN + ", " + 
            USER_AGENT_COLUMN + ", " + 
            CREATED_AT_COLUMN + ", " + 
            EXPIRES_AT_COLUMN + 
            ") VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            USER_ID_COLUMN + " = ?, " + 
            SESSION_TOKEN_COLUMN + " = ?, " + 
            IP_ADDRESS_COLUMN + " = ?, " + 
            USER_AGENT_COLUMN + " = ?, " + 
            CREATED_AT_COLUMN + " = ?, " + 
            EXPIRES_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String FIND_BY_SESSION_TOKEN_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + SESSION_TOKEN_COLUMN + " = ?";
    
    private static final String FIND_BY_USER_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + USER_ID_COLUMN + " = ?";
    
    private static final String FIND_ACTIVE_BY_USER_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + USER_ID_COLUMN + " = ? AND " + 
            EXPIRES_AT_COLUMN + " > ?";
    
    private static final String FIND_BY_IP_ADDRESS_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + IP_ADDRESS_COLUMN + " = ?";
    
    private static final String FIND_ALL_ACTIVE_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + EXPIRES_AT_COLUMN + " > ?";
    
    private static final String FIND_ALL_EXPIRED_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + EXPIRES_AT_COLUMN + " <= ?";
    
    private static final String UPDATE_EXPIRES_AT_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + EXPIRES_AT_COLUMN + " = ? WHERE " + ID_COLUMN + " = ?";
    
    private static final String DELETE_EXPIRED_SQL = 
            "DELETE FROM " + TABLE_NAME + " WHERE " + EXPIRES_AT_COLUMN + " <= ?";
    
    private static final String DELETE_BY_USER_ID_SQL = 
            "DELETE FROM " + TABLE_NAME + " WHERE " + USER_ID_COLUMN + " = ?";
    
    private static final String DELETE_OTHER_SESSIONS_FOR_USER_SQL = 
            "DELETE FROM " + TABLE_NAME + " WHERE " + USER_ID_COLUMN + " = ? AND " + ID_COLUMN + " != ?";
    
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
    protected UserSession mapRow(ResultSet rs) throws SQLException {
        UserSession userSession = new UserSession();
        
        userSession.setId(rs.getInt(ID_COLUMN));
        userSession.setUserId(rs.getInt(USER_ID_COLUMN));
        userSession.setSessionToken(rs.getString(SESSION_TOKEN_COLUMN));
        userSession.setIpAddress(rs.getString(IP_ADDRESS_COLUMN));
        userSession.setUserAgent(rs.getString(USER_AGENT_COLUMN));
        userSession.setCreatedAt(rs.getTimestamp(CREATED_AT_COLUMN));
        userSession.setExpiresAt(rs.getTimestamp(EXPIRES_AT_COLUMN));
        
        return userSession;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, UserSession userSession) throws SQLException {
        ps.setInt(1, userSession.getUserId());
        ps.setString(2, userSession.getSessionToken());
        ps.setString(3, userSession.getIpAddress());
        ps.setString(4, userSession.getUserAgent());
        
        if (userSession.getCreatedAt() != null) {
            ps.setTimestamp(5, userSession.getCreatedAt());
        } else {
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
        }
        
        if (userSession.getExpiresAt() != null) {
            ps.setTimestamp(6, userSession.getExpiresAt());
        } else {
            // Default expiration: 24 hours from now
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis() + (24 * 60 * 60 * 1000L)));
        }
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, UserSession userSession) throws SQLException {
        ps.setInt(1, userSession.getUserId());
        ps.setString(2, userSession.getSessionToken());
        ps.setString(3, userSession.getIpAddress());
        ps.setString(4, userSession.getUserAgent());
        
        if (userSession.getCreatedAt() != null) {
            ps.setTimestamp(5, userSession.getCreatedAt());
        } else {
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
        }
        
        if (userSession.getExpiresAt() != null) {
            ps.setTimestamp(6, userSession.getExpiresAt());
        } else {
            // Default expiration: 24 hours from now
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis() + (24 * 60 * 60 * 1000L)));
        }
        
        ps.setInt(7, userSession.getId());
    }
    
    @Override
    public UserSession create(UserSession userSession) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(ps, userSession);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user session failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    userSession.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating user session failed, no ID obtained.");
                }
            }
            
            return userSession;
        }
    }
    
    @Override
    public UserSession update(UserSession userSession) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            
            setUpdateParameters(ps, userSession);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating user session failed, no rows affected.");
            }
            
            return userSession;
        }
    }
    
    @Override
    public Optional<UserSession> findBySessionToken(String sessionToken) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_SESSION_TOKEN_SQL)) {
            
            ps.setString(1, sessionToken);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                } else {
                    return Optional.empty();
                }
            }
        }
    }
    
    @Override
    public List<UserSession> findByUserId(Integer userId) throws SQLException {
        return executeQuery(FIND_BY_USER_ID_SQL, ps -> 
            ps.setInt(1, userId)
        );
    }
    
    @Override
    public List<UserSession> findActiveByUserId(Integer userId) throws SQLException {
        return executeQuery(FIND_ACTIVE_BY_USER_ID_SQL, ps -> {
            ps.setInt(1, userId);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        });
    }
    
    @Override
    public List<UserSession> findByIpAddress(String ipAddress) throws SQLException {
        return executeQuery(FIND_BY_IP_ADDRESS_SQL, ps -> 
            ps.setString(1, ipAddress)
        );
    }
    
    @Override
    public List<UserSession> findAllActive() throws SQLException {
        return executeQuery(FIND_ALL_ACTIVE_SQL, ps -> 
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()))
        );
    }
    
    @Override
    public List<UserSession> findAllExpired() throws SQLException {
        return executeQuery(FIND_ALL_EXPIRED_SQL, ps -> 
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()))
        );
    }
    
    @Override
    public boolean updateExpiresAt(Integer id, Timestamp expiresAt) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_EXPIRES_AT_SQL)) {
            
            ps.setTimestamp(1, expiresAt);
            ps.setInt(2, id);
            
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    @Override
    public int deleteExpired() throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_EXPIRED_SQL)) {
            
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            
            return ps.executeUpdate();
        }
    }
    
    @Override
    public int deleteByUserId(Integer userId) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_BY_USER_ID_SQL)) {
            
            ps.setInt(1, userId);
            
            return ps.executeUpdate();
        }
    }
    
    @Override
    public int deleteOtherSessionsForUser(Integer userId, Integer currentSessionId) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_OTHER_SESSIONS_FOR_USER_SQL)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, currentSessionId);
            
            return ps.executeUpdate();
        }
    }
}