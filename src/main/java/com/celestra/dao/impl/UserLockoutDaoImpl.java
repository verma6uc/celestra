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
import com.celestra.dao.UserLockoutDao;
import com.celestra.db.DatabaseUtil;
import com.celestra.model.UserLockout;

/**
 * Implementation of the UserLockoutDao interface.
 */
public class UserLockoutDaoImpl extends AbstractBaseDao<UserLockout, Integer> implements UserLockoutDao {
    
    private static final String TABLE_NAME = "user_lockouts";
    private static final String ID_COLUMN = "id";
    private static final String USER_ID_COLUMN = "user_id";
    private static final String LOCKOUT_START_COLUMN = "lockout_start";
    private static final String LOCKOUT_END_COLUMN = "lockout_end";
    private static final String FAILED_ATTEMPTS_COLUMN = "failed_attempts";
    private static final String REASON_COLUMN = "reason";
    private static final String CREATED_AT_COLUMN = "created_at";
    private static final String UPDATED_AT_COLUMN = "updated_at";
    
    private static final String INSERT_SQL = 
            "INSERT INTO " + TABLE_NAME + " (" + 
            USER_ID_COLUMN + ", " + 
            LOCKOUT_START_COLUMN + ", " + 
            LOCKOUT_END_COLUMN + ", " + 
            FAILED_ATTEMPTS_COLUMN + ", " + 
            REASON_COLUMN + ", " + 
            CREATED_AT_COLUMN + ", " + 
            UPDATED_AT_COLUMN + 
            ") VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            USER_ID_COLUMN + " = ?, " + 
            LOCKOUT_START_COLUMN + " = ?, " + 
            LOCKOUT_END_COLUMN + " = ?, " + 
            FAILED_ATTEMPTS_COLUMN + " = ?, " + 
            REASON_COLUMN + " = ?, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String FIND_ACTIVE_BY_USER_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + USER_ID_COLUMN + " = ? AND " + 
            "(" + LOCKOUT_END_COLUMN + " IS NULL OR " + LOCKOUT_END_COLUMN + " > ?) " + 
            "ORDER BY " + CREATED_AT_COLUMN + " DESC LIMIT 1";
    
    private static final String FIND_BY_USER_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + USER_ID_COLUMN + " = ? " + 
            "ORDER BY " + CREATED_AT_COLUMN + " DESC";
    
    private static final String FIND_ALL_ACTIVE_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + 
            "(" + LOCKOUT_END_COLUMN + " IS NULL OR " + LOCKOUT_END_COLUMN + " > ?) " + 
            "ORDER BY " + CREATED_AT_COLUMN + " DESC";
    
    private static final String FIND_ALL_EXPIRED_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + 
            LOCKOUT_END_COLUMN + " IS NOT NULL AND " + LOCKOUT_END_COLUMN + " <= ? " + 
            "ORDER BY " + CREATED_AT_COLUMN + " DESC";
    
    private static final String FIND_ALL_PERMANENT_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + 
            LOCKOUT_END_COLUMN + " IS NULL " + 
            "ORDER BY " + CREATED_AT_COLUMN + " DESC";
    
    private static final String FIND_ALL_TEMPORARY_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + 
            LOCKOUT_END_COLUMN + " IS NOT NULL " + 
            "ORDER BY " + CREATED_AT_COLUMN + " DESC";
    
    private static final String UPDATE_LOCKOUT_END_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            LOCKOUT_END_COLUMN + " = ?, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String UPDATE_FAILED_ATTEMPTS_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            FAILED_ATTEMPTS_COLUMN + " = ?, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String DELETE_EXPIRED_SQL = 
            "DELETE FROM " + TABLE_NAME + " WHERE " + 
            LOCKOUT_END_COLUMN + " IS NOT NULL AND " + LOCKOUT_END_COLUMN + " <= ?";
    
    private static final String DELETE_BY_USER_ID_SQL = 
            "DELETE FROM " + TABLE_NAME + " WHERE " + USER_ID_COLUMN + " = ?";
    
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
    protected UserLockout mapRow(ResultSet rs) throws SQLException {
        UserLockout userLockout = new UserLockout();
        
        userLockout.setId(rs.getInt(ID_COLUMN));
        userLockout.setUserId(rs.getInt(USER_ID_COLUMN));
        
        Timestamp lockoutStartTimestamp = rs.getTimestamp(LOCKOUT_START_COLUMN);
        if (lockoutStartTimestamp != null) {
            userLockout.setLockoutStart(lockoutStartTimestamp);
        }
        
        Timestamp lockoutEndTimestamp = rs.getTimestamp(LOCKOUT_END_COLUMN);
        if (lockoutEndTimestamp != null) {
            userLockout.setLockoutEnd(lockoutEndTimestamp);
        }
        
        userLockout.setFailedAttempts(rs.getInt(FAILED_ATTEMPTS_COLUMN));
        userLockout.setReason(rs.getString(REASON_COLUMN));
        
        Timestamp createdAtTimestamp = rs.getTimestamp(CREATED_AT_COLUMN);
        if (createdAtTimestamp != null) {
            userLockout.setCreatedAt(createdAtTimestamp);
        }
        
        Timestamp updatedAtTimestamp = rs.getTimestamp(UPDATED_AT_COLUMN);
        if (updatedAtTimestamp != null) {
            userLockout.setUpdatedAt(updatedAtTimestamp);
        }
        
        return userLockout;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, UserLockout userLockout) throws SQLException {
        ps.setInt(1, userLockout.getUserId());
        
        if (userLockout.getLockoutStart() != null) {
            ps.setTimestamp(2, userLockout.getLockoutStart());
        } else {
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        }
        
        if (userLockout.getLockoutEnd() != null) {
            ps.setTimestamp(3, userLockout.getLockoutEnd());
        } else {
            ps.setNull(3, java.sql.Types.TIMESTAMP);
        }
        
        ps.setInt(4, userLockout.getFailedAttempts() != null ? userLockout.getFailedAttempts() : 0);
        ps.setString(5, userLockout.getReason());
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        
        if (userLockout.getCreatedAt() != null) {
            ps.setTimestamp(6, userLockout.getCreatedAt());
        } else {
            ps.setTimestamp(6, now);
        }
        
        if (userLockout.getUpdatedAt() != null) {
            ps.setTimestamp(7, userLockout.getUpdatedAt());
        } else {
            ps.setTimestamp(7, now);
        }
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, UserLockout userLockout) throws SQLException {
        ps.setInt(1, userLockout.getUserId());
        
        if (userLockout.getLockoutStart() != null) {
            ps.setTimestamp(2, userLockout.getLockoutStart());
        } else {
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        }
        
        if (userLockout.getLockoutEnd() != null) {
            ps.setTimestamp(3, userLockout.getLockoutEnd());
        } else {
            ps.setNull(3, java.sql.Types.TIMESTAMP);
        }
        
        ps.setInt(4, userLockout.getFailedAttempts() != null ? userLockout.getFailedAttempts() : 0);
        ps.setString(5, userLockout.getReason());
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        
        if (userLockout.getUpdatedAt() != null) {
            ps.setTimestamp(6, userLockout.getUpdatedAt());
        } else {
            ps.setTimestamp(6, now);
        }
        
        ps.setInt(7, userLockout.getId());
    }
    
    @Override
    public UserLockout create(UserLockout userLockout) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(ps, userLockout);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user lockout failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    userLockout.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating user lockout failed, no ID obtained.");
                }
            }
            
            return userLockout;
        }
    }
    
    @Override
    public UserLockout update(UserLockout userLockout) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            
            setUpdateParameters(ps, userLockout);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating user lockout failed, no rows affected.");
            }
            
            return userLockout;
        }
    }
    
    @Override
    public Optional<UserLockout> findActiveByUserId(Integer userId) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ACTIVE_BY_USER_ID_SQL)) {
            
            ps.setInt(1, userId);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            
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
    public List<UserLockout> findByUserId(Integer userId) throws SQLException {
        return executeQuery(FIND_BY_USER_ID_SQL, ps -> 
            ps.setInt(1, userId)
        );
    }
    
    @Override
    public List<UserLockout> findAllActive() throws SQLException {
        return executeQuery(FIND_ALL_ACTIVE_SQL, ps -> 
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()))
        );
    }
    
    @Override
    public List<UserLockout> findAllExpired() throws SQLException {
        return executeQuery(FIND_ALL_EXPIRED_SQL, ps -> 
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()))
        );
    }
    
    @Override
    public List<UserLockout> findAllPermanent() throws SQLException {
        return executeQuery(FIND_ALL_PERMANENT_SQL, ps -> {
            // No parameters needed
        });
    }
    
    @Override
    public List<UserLockout> findAllTemporary() throws SQLException {
        return executeQuery(FIND_ALL_TEMPORARY_SQL, ps -> {
            // No parameters needed
        });
    }
    
    @Override
    public boolean updateLockoutEnd(Integer id, Timestamp lockoutEnd) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_LOCKOUT_END_SQL)) {
            
            if (lockoutEnd != null) {
                ps.setTimestamp(1, lockoutEnd);
            } else {
                ps.setNull(1, java.sql.Types.TIMESTAMP);
            }
            
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, id);
            
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    @Override
    public boolean updateFailedAttempts(Integer id, Integer failedAttempts) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_FAILED_ATTEMPTS_SQL)) {
            
            ps.setInt(1, failedAttempts);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, id);
            
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
}