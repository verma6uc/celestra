package com.celestra.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import com.celestra.dao.AbstractBaseDao;
import com.celestra.dao.PasswordHistoryDao;
import com.celestra.db.DatabaseUtil;
import com.celestra.model.PasswordHistory;

/**
 * Implementation of the PasswordHistoryDao interface.
 */
public class PasswordHistoryDaoImpl extends AbstractBaseDao<PasswordHistory, Integer> implements PasswordHistoryDao {
    
    private static final String TABLE_NAME = "password_history";
    private static final String ID_COLUMN = "id";
    private static final String USER_ID_COLUMN = "user_id";
    private static final String PASSWORD_HASH_COLUMN = "password_hash";
    private static final String CREATED_AT_COLUMN = "created_at";
    
    private static final String INSERT_SQL = 
            "INSERT INTO " + TABLE_NAME + " (" + 
            USER_ID_COLUMN + ", " + 
            PASSWORD_HASH_COLUMN + ", " + 
            CREATED_AT_COLUMN + 
            ") VALUES (?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            USER_ID_COLUMN + " = ?, " + 
            PASSWORD_HASH_COLUMN + " = ?, " + 
            CREATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String FIND_BY_USER_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + USER_ID_COLUMN + " = ? " + 
            "ORDER BY " + CREATED_AT_COLUMN + " DESC";
    
    private static final String FIND_RECENT_BY_USER_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + USER_ID_COLUMN + " = ? " + 
            "ORDER BY " + CREATED_AT_COLUMN + " DESC LIMIT ?";
    
    private static final String EXISTS_BY_USER_ID_AND_PASSWORD_HASH_SQL = 
            "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + 
            USER_ID_COLUMN + " = ? AND " + PASSWORD_HASH_COLUMN + " = ?";
    
    private static final String DELETE_BY_USER_ID_SQL = 
            "DELETE FROM " + TABLE_NAME + " WHERE " + USER_ID_COLUMN + " = ?";
    
    private static final String DELETE_OLDER_THAN_SQL = 
            "DELETE FROM " + TABLE_NAME + " WHERE " + CREATED_AT_COLUMN + " < ?";
    
    private static final String DELETE_OLDEST_BY_USER_ID_SQL = 
            "DELETE FROM " + TABLE_NAME + " WHERE " + ID_COLUMN + " IN (" + 
            "SELECT id FROM " + TABLE_NAME + " WHERE " + USER_ID_COLUMN + " = ? " + 
            "ORDER BY " + CREATED_AT_COLUMN + " DESC OFFSET ? LIMIT 1000000)";
    
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
    protected PasswordHistory mapRow(ResultSet rs) throws SQLException {
        PasswordHistory passwordHistory = new PasswordHistory();
        
        passwordHistory.setId(rs.getInt(ID_COLUMN));
        passwordHistory.setUserId(rs.getInt(USER_ID_COLUMN));
        passwordHistory.setPasswordHash(rs.getString(PASSWORD_HASH_COLUMN));
        passwordHistory.setCreatedAt(rs.getTimestamp(CREATED_AT_COLUMN));
        
        return passwordHistory;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, PasswordHistory passwordHistory) throws SQLException {
        ps.setInt(1, passwordHistory.getUserId());
        ps.setString(2, passwordHistory.getPasswordHash());
        
        if (passwordHistory.getCreatedAt() != null) {
            ps.setTimestamp(3, passwordHistory.getCreatedAt());
        } else {
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        }
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, PasswordHistory passwordHistory) throws SQLException {
        ps.setInt(1, passwordHistory.getUserId());
        ps.setString(2, passwordHistory.getPasswordHash());
        
        if (passwordHistory.getCreatedAt() != null) {
            ps.setTimestamp(3, passwordHistory.getCreatedAt());
        } else {
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        }
        
        ps.setInt(4, passwordHistory.getId());
    }
    
    @Override
    public PasswordHistory create(PasswordHistory passwordHistory) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(ps, passwordHistory);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating password history failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    passwordHistory.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating password history failed, no ID obtained.");
                }
            }
            
            return passwordHistory;
        }
    }
    
    @Override
    public PasswordHistory update(PasswordHistory passwordHistory) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            
            setUpdateParameters(ps, passwordHistory);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating password history failed, no rows affected.");
            }
            
            return passwordHistory;
        }
    }
    
    @Override
    public List<PasswordHistory> findByUserId(Integer userId) throws SQLException {
        return executeQuery(FIND_BY_USER_ID_SQL, ps -> 
            ps.setInt(1, userId)
        );
    }
    
    @Override
    public List<PasswordHistory> findRecentByUserId(Integer userId, int limit) throws SQLException {
        return executeQuery(FIND_RECENT_BY_USER_ID_SQL, ps -> {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
        });
    }
    
    @Override
    public boolean existsByUserIdAndPasswordHash(Integer userId, String passwordHash) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(EXISTS_BY_USER_ID_AND_PASSWORD_HASH_SQL)) {
            
            ps.setInt(1, userId);
            ps.setString(2, passwordHash);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                } else {
                    return false;
                }
            }
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
    public int deleteOlderThan(Timestamp olderThan) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_OLDER_THAN_SQL)) {
            
            ps.setTimestamp(1, olderThan);
            
            return ps.executeUpdate();
        }
    }
    
    @Override
    public int deleteOldestByUserId(Integer userId, int keepCount) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_OLDEST_BY_USER_ID_SQL)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, keepCount);
            
            return ps.executeUpdate();
        }
    }
}