package com.celestra.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.celestra.dao.AbstractBaseDao;
import com.celestra.dao.FailedLoginDao;
import com.celestra.db.DatabaseUtil;
import com.celestra.model.FailedLogin;

import java.util.List;

/**
 * Implementation of the FailedLoginDao interface.
 */
public class FailedLoginDaoImpl extends AbstractBaseDao<FailedLogin, Integer> implements FailedLoginDao {
    
    private static final String TABLE_NAME = "failed_logins";
    private static final String ID_COLUMN = "id";
    private static final String USER_ID_COLUMN = "user_id";
    private static final String IP_ADDRESS_COLUMN = "ip_address";
    private static final String ATTEMPTED_AT_COLUMN = "attempted_at";
    private static final String FAILURE_REASON_COLUMN = "failure_reason";
    
    private static final String INSERT_SQL = 
            "INSERT INTO " + TABLE_NAME + " (" + 
            USER_ID_COLUMN + ", " + 
            IP_ADDRESS_COLUMN + ", " + 
            ATTEMPTED_AT_COLUMN + ", " + 
            FAILURE_REASON_COLUMN + 
            ") VALUES (?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            USER_ID_COLUMN + " = ?, " + 
            IP_ADDRESS_COLUMN + " = ?, " + 
            ATTEMPTED_AT_COLUMN + " = ?, " + 
            FAILURE_REASON_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String FIND_BY_USERNAME_SQL = 
            "SELECT f.* FROM " + TABLE_NAME + " f " +
            "JOIN users u ON f." + USER_ID_COLUMN + " = u.id " +
            "WHERE u.email = ? ORDER BY f." + ATTEMPTED_AT_COLUMN + " DESC";
    
    private static final String FIND_BY_IP_ADDRESS_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + IP_ADDRESS_COLUMN + " = ? ORDER BY " + ATTEMPTED_AT_COLUMN + " DESC";
    
    private static final String FIND_BY_USERNAME_AND_IP_ADDRESS_SQL = 
            "SELECT f.* FROM " + TABLE_NAME + " f " +
            "JOIN users u ON f." + USER_ID_COLUMN + " = u.id " +
            "WHERE u.email = ? AND f." + IP_ADDRESS_COLUMN + " = ? " +
            "ORDER BY f." + ATTEMPTED_AT_COLUMN + " DESC";
    
    private static final String FIND_RECENT_BY_USERNAME_SQL = 
            "SELECT f.* FROM " + TABLE_NAME + " f " +
            "JOIN users u ON f." + USER_ID_COLUMN + " = u.id " +
            "WHERE u.email = ? AND f." + ATTEMPTED_AT_COLUMN + " > ? " +
            "ORDER BY f." + ATTEMPTED_AT_COLUMN + " DESC";
    
    private static final String COUNT_RECENT_BY_USERNAME_SQL = 
            "SELECT COUNT(*) FROM " + TABLE_NAME + " f " +
            "JOIN users u ON f." + USER_ID_COLUMN + " = u.id " +
            "WHERE u.email = ? AND f." + ATTEMPTED_AT_COLUMN + " > ?";
    
    private static final String COUNT_RECENT_BY_IP_ADDRESS_SQL = 
            "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + IP_ADDRESS_COLUMN + " = ? AND " + 
            ATTEMPTED_AT_COLUMN + " > ?";
    
    private static final String DELETE_OLDER_THAN_SQL = 
            "DELETE FROM " + TABLE_NAME + " WHERE " + ATTEMPTED_AT_COLUMN + " < ?";
    
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
    protected FailedLogin mapRow(ResultSet rs) throws SQLException {
        FailedLogin failedLogin = new FailedLogin();
        
        failedLogin.setId(rs.getInt(ID_COLUMN));
        
        Integer userId = rs.getInt(USER_ID_COLUMN);
        if (!rs.wasNull()) {
            failedLogin.setUserId(userId);
        }
        
        failedLogin.setIpAddress(rs.getString(IP_ADDRESS_COLUMN));
        failedLogin.setAttemptedAt(rs.getTimestamp(ATTEMPTED_AT_COLUMN));
        failedLogin.setFailureReason(rs.getString(FAILURE_REASON_COLUMN));
        
        return failedLogin;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, FailedLogin failedLogin) throws SQLException {
        if (failedLogin.getUserId() != null) {
            ps.setInt(1, failedLogin.getUserId());
        } else {
            ps.setNull(1, java.sql.Types.INTEGER);
        }
        
        ps.setString(2, failedLogin.getIpAddress());
        
        if (failedLogin.getAttemptedAt() != null) {
            ps.setTimestamp(3, failedLogin.getAttemptedAt());
        } else {
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        }
        
        ps.setString(4, failedLogin.getFailureReason());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, FailedLogin failedLogin) throws SQLException {
        if (failedLogin.getUserId() != null) {
            ps.setInt(1, failedLogin.getUserId());
        } else {
            ps.setNull(1, java.sql.Types.INTEGER);
        }
        
        ps.setString(2, failedLogin.getIpAddress());
        
        if (failedLogin.getAttemptedAt() != null) {
            ps.setTimestamp(3, failedLogin.getAttemptedAt());
        } else {
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        }
        
        ps.setString(4, failedLogin.getFailureReason());
        ps.setInt(5, failedLogin.getId());
    }
    
    @Override
    public FailedLogin create(FailedLogin failedLogin) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(ps, failedLogin);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating failed login record failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    failedLogin.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating failed login record failed, no ID obtained.");
                }
            }
            
            return failedLogin;
        }
    }
    
    @Override
    public FailedLogin update(FailedLogin failedLogin) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            
            setUpdateParameters(ps, failedLogin);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating failed login record failed, no rows affected.");
            }
            
            return failedLogin;
        }
    }
    
    @Override
    public List<FailedLogin> findByUsername(String username) throws SQLException {
        return executeQuery(FIND_BY_USERNAME_SQL, ps -> 
            ps.setString(1, username)
        );
    }
    
    @Override
    public List<FailedLogin> findByIpAddress(String ipAddress) throws SQLException {
        return executeQuery(FIND_BY_IP_ADDRESS_SQL, ps -> 
            ps.setString(1, ipAddress)
        );
    }
    
    @Override
    public List<FailedLogin> findByUsernameAndIpAddress(String username, String ipAddress) throws SQLException {
        return executeQuery(FIND_BY_USERNAME_AND_IP_ADDRESS_SQL, ps -> {
            ps.setString(1, username);
            ps.setString(2, ipAddress);
        });
    }
    
    @Override
    public List<FailedLogin> findRecentByUsername(String username, int minutes) throws SQLException {
        Timestamp cutoffTime = new Timestamp(System.currentTimeMillis() - (minutes * 60 * 1000L));
        
        return executeQuery(FIND_RECENT_BY_USERNAME_SQL, ps -> {
            ps.setString(1, username);
            ps.setTimestamp(2, cutoffTime);
        });
    }
    
    @Override
    public int countRecentByUsername(String username, int minutes) throws SQLException {
        Timestamp cutoffTime = new Timestamp(System.currentTimeMillis() - (minutes * 60 * 1000L));
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_RECENT_BY_USERNAME_SQL)) {
            
            ps.setString(1, username);
            ps.setTimestamp(2, cutoffTime);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return 0;
                }
            }
        }
    }
    
    @Override
    public int countRecentByIpAddress(String ipAddress, int minutes) throws SQLException {
        Timestamp cutoffTime = new Timestamp(System.currentTimeMillis() - (minutes * 60 * 1000L));
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_RECENT_BY_IP_ADDRESS_SQL)) {
            
            ps.setString(1, ipAddress);
            ps.setTimestamp(2, cutoffTime);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return 0;
                }
            }
        }
    }
    
    @Override
    public int deleteOlderThan(int days) throws SQLException {
        Timestamp cutoffTime = new Timestamp(System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L));
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_OLDER_THAN_SQL)) {
            
            ps.setTimestamp(1, cutoffTime);
            
            return ps.executeUpdate();
        }
    }
}