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
import com.celestra.dao.PasswordResetTokenDao;
import com.celestra.db.DatabaseUtil;
import com.celestra.model.PasswordResetToken;

/**
 * Implementation of the PasswordResetTokenDao interface.
 */
public class PasswordResetTokenDaoImpl extends AbstractBaseDao<PasswordResetToken, Integer> implements PasswordResetTokenDao {
    
    private static final String TABLE_NAME = "password_reset_tokens";
    private static final String ID_COLUMN = "id";
    private static final String USER_ID_COLUMN = "user_id";
    private static final String TOKEN_COLUMN = "token";
    private static final String CREATED_AT_COLUMN = "created_at";
    private static final String EXPIRES_AT_COLUMN = "expires_at";
    private static final String USED_AT_COLUMN = "used_at";
    
    private static final String INSERT_SQL = 
            "INSERT INTO " + TABLE_NAME + " (" + 
            USER_ID_COLUMN + ", " + 
            TOKEN_COLUMN + ", " + 
            CREATED_AT_COLUMN + ", " + 
            EXPIRES_AT_COLUMN + ", " + 
            USED_AT_COLUMN + 
            ") VALUES (?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            USER_ID_COLUMN + " = ?, " + 
            TOKEN_COLUMN + " = ?, " + 
            CREATED_AT_COLUMN + " = ?, " + 
            EXPIRES_AT_COLUMN + " = ?, " + 
            USED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String FIND_BY_TOKEN_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + TOKEN_COLUMN + " = ?";
    
    private static final String FIND_BY_USER_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + USER_ID_COLUMN + " = ?";
    
    private static final String FIND_ACTIVE_BY_USER_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + USER_ID_COLUMN + " = ? AND " + 
            EXPIRES_AT_COLUMN + " > ? AND " + USED_AT_COLUMN + " IS NULL";
    
    private static final String MARK_AS_USED_BY_ID_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + USED_AT_COLUMN + " = ? WHERE " + ID_COLUMN + " = ?";
    
    private static final String MARK_AS_USED_BY_TOKEN_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + USED_AT_COLUMN + " = ? WHERE " + TOKEN_COLUMN + " = ?";
    
    private static final String INVALIDATE_ALL_FOR_USER_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + USED_AT_COLUMN + " = ? WHERE " + 
            USER_ID_COLUMN + " = ? AND " + USED_AT_COLUMN + " IS NULL";
    
    private static final String DELETE_EXPIRED_TOKENS_SQL = 
            "DELETE FROM " + TABLE_NAME + " WHERE " + 
            "((" + EXPIRES_AT_COLUMN + " < ? AND " + USED_AT_COLUMN + " IS NULL) OR " + 
            CREATED_AT_COLUMN + " < ?)";
    
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
    protected PasswordResetToken mapRow(ResultSet rs) throws SQLException {
        PasswordResetToken token = new PasswordResetToken();
        
        token.setId(rs.getInt(ID_COLUMN));
        token.setUserId(rs.getInt(USER_ID_COLUMN));
        token.setToken(rs.getString(TOKEN_COLUMN));
        token.setCreatedAt(rs.getTimestamp(CREATED_AT_COLUMN));
        token.setExpiresAt(rs.getTimestamp(EXPIRES_AT_COLUMN));
        token.setUsedAt(rs.getTimestamp(USED_AT_COLUMN));
        
        return token;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, PasswordResetToken token) throws SQLException {
        ps.setInt(1, token.getUserId());
        ps.setString(2, token.getToken());
        
        if (token.getCreatedAt() != null) {
            ps.setTimestamp(3, token.getCreatedAt());
        } else {
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        }
        
        ps.setTimestamp(4, token.getExpiresAt());
        
        if (token.getUsedAt() != null) {
            ps.setTimestamp(5, token.getUsedAt());
        } else {
            ps.setNull(5, java.sql.Types.TIMESTAMP);
        }
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, PasswordResetToken token) throws SQLException {
        ps.setInt(1, token.getUserId());
        ps.setString(2, token.getToken());
        
        if (token.getCreatedAt() != null) {
            ps.setTimestamp(3, token.getCreatedAt());
        } else {
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        }
        
        ps.setTimestamp(4, token.getExpiresAt());
        
        if (token.getUsedAt() != null) {
            ps.setTimestamp(5, token.getUsedAt());
        } else {
            ps.setNull(5, java.sql.Types.TIMESTAMP);
        }
        
        ps.setInt(6, token.getId());
    }
    
    @Override
    public PasswordResetToken create(PasswordResetToken token) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(ps, token);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating password reset token failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    token.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating password reset token failed, no ID obtained.");
                }
            }
            
            return token;
        }
    }
    
    @Override
    public PasswordResetToken update(PasswordResetToken token) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            
            setUpdateParameters(ps, token);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating password reset token failed, no rows affected.");
            }
            
            return token;
        }
    }
    
    @Override
    public Optional<PasswordResetToken> findByToken(String token) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_TOKEN_SQL)) {
            
            ps.setString(1, token);
            
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
    public List<PasswordResetToken> findByUserId(Integer userId) throws SQLException {
        return executeQuery(FIND_BY_USER_ID_SQL, ps -> 
            ps.setInt(1, userId)
        );
    }
    
    @Override
    public List<PasswordResetToken> findActiveByUserId(Integer userId) throws SQLException {
        return executeQuery(FIND_ACTIVE_BY_USER_ID_SQL, ps -> {
            ps.setInt(1, userId);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        });
    }
    
    @Override
    public boolean markAsUsed(Integer id, Timestamp usedAt) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(MARK_AS_USED_BY_ID_SQL)) {
            
            ps.setTimestamp(1, usedAt);
            ps.setInt(2, id);
            
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    @Override
    public boolean markAsUsed(String token, Timestamp usedAt) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(MARK_AS_USED_BY_TOKEN_SQL)) {
            
            ps.setTimestamp(1, usedAt);
            ps.setString(2, token);
            
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    @Override
    public int invalidateAllForUser(Integer userId) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(INVALIDATE_ALL_FOR_USER_SQL)) {
            
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, userId);
            
            return ps.executeUpdate();
        }
    }
    
    @Override
    public int deleteExpiredTokens(Timestamp olderThan) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_EXPIRED_TOKENS_SQL)) {
            
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setTimestamp(2, olderThan);
            
            return ps.executeUpdate();
        }
    }
}