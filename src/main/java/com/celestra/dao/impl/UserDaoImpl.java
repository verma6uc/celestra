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
import com.celestra.dao.UserDao;
import com.celestra.db.DatabaseUtil;
import com.celestra.enums.UserRole;
import com.celestra.enums.UserStatus;
import com.celestra.model.User;

/**
 * Implementation of the UserDao interface.
 */
public class UserDaoImpl extends AbstractBaseDao<User, Integer> implements UserDao {
    
    private static final String TABLE_NAME = "users";
    private static final String ID_COLUMN = "id";
    private static final String COMPANY_ID_COLUMN = "company_id";
    private static final String ROLE_COLUMN = "role";
    private static final String EMAIL_COLUMN = "email";
    private static final String NAME_COLUMN = "name";
    private static final String PASSWORD_HASH_COLUMN = "password_hash";
    private static final String STATUS_COLUMN = "status";
    private static final String CREATED_AT_COLUMN = "created_at";
    private static final String UPDATED_AT_COLUMN = "updated_at";
    
    private static final String INSERT_SQL = 
            "INSERT INTO " + TABLE_NAME + " (" + 
            COMPANY_ID_COLUMN + ", " + 
            ROLE_COLUMN + ", " + 
            EMAIL_COLUMN + ", " + 
            NAME_COLUMN + ", " + 
            PASSWORD_HASH_COLUMN + ", " + 
            STATUS_COLUMN + ", " + 
            CREATED_AT_COLUMN + ", " + 
            UPDATED_AT_COLUMN + 
            ") VALUES (?, ?::user_role, ?, ?, ?, ?::user_status, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            COMPANY_ID_COLUMN + " = ?, " + 
            ROLE_COLUMN + " = ?::user_role, " + 
            EMAIL_COLUMN + " = ?, " + 
            NAME_COLUMN + " = ?, " + 
            STATUS_COLUMN + " = ?::user_status, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String FIND_BY_EMAIL_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + EMAIL_COLUMN + " = ?";
    
    private static final String FIND_BY_COMPANY_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + COMPANY_ID_COLUMN + " = ?";
    
    private static final String FIND_BY_ROLE_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + ROLE_COLUMN + " = ?::user_role";
    
    private static final String FIND_BY_STATUS_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COLUMN + " = ?::user_status";
    
    private static final String FIND_BY_COMPANY_ID_AND_ROLE_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + COMPANY_ID_COLUMN + " = ? AND " + ROLE_COLUMN + " = ?::user_role";
    
    private static final String UPDATE_PASSWORD_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            PASSWORD_HASH_COLUMN + " = ?, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String UPDATE_STATUS_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            STATUS_COLUMN + " = ?::user_status, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String AUTHENTICATE_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + 
            EMAIL_COLUMN + " = ? AND " + 
            PASSWORD_HASH_COLUMN + " = ? AND " + 
            STATUS_COLUMN + " = ?::user_status";
            
    private static final String FIND_ACTIVE_USER_BY_EMAIL_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + 
            EMAIL_COLUMN + " = ? AND " + 
            STATUS_COLUMN + " = ?::user_status";
            
    private static final String IS_USER_LOCKED_OUT_SQL = 
            "SELECT COUNT(*) FROM user_lockouts WHERE user_id = ? AND " + 
            "(lockout_end IS NULL OR lockout_end > NOW())";
            
    private static final String HAS_ROLE_SQL = 
            "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + 
            ID_COLUMN + " = ? AND " + 
            ROLE_COLUMN + " = ?::user_role";
            
    private static final String UPDATE_ROLE_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            ROLE_COLUMN + " = ?::user_role, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
            
    private static final String COUNT_ACTIVE_SESSIONS_SQL = 
            "SELECT COUNT(*) FROM user_sessions WHERE user_id = ? AND expires_at > NOW()";
            
    private static final String INVALIDATE_ALL_SESSIONS_SQL = 
            "DELETE FROM user_sessions WHERE user_id = ?";
            
    private static final String FIND_BY_COMPANY_ID_AND_ROLE_AND_STATUS_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + 
            COMPANY_ID_COLUMN + " = ? AND " + 
            ROLE_COLUMN + " = ?::user_role AND " + 
            STATUS_COLUMN + " = ?::user_status";
            
    private static final String FIND_BY_EMAIL_CONTAINING_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + 
            EMAIL_COLUMN + " ILIKE ? ORDER BY " + EMAIL_COLUMN + " ASC";
            
    private static final String FIND_BY_CREATED_AT_AFTER_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + 
            CREATED_AT_COLUMN + " > ? ORDER BY " + CREATED_AT_COLUMN + " DESC";
            
    private static final String IS_PASSWORD_PREVIOUSLY_USED_SQL = 
            "SELECT COUNT(*) FROM password_history WHERE user_id = ? AND password_hash = ? LIMIT ?";
            
    private static final String ADD_PASSWORD_TO_HISTORY_SQL = 
            "INSERT INTO password_history (user_id, password_hash, created_at) VALUES (?, ?, ?)";
    
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
    protected User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        
        user.setId(rs.getInt(ID_COLUMN));
        
        Integer companyId = rs.getInt(COMPANY_ID_COLUMN);
        if (!rs.wasNull()) {
            user.setCompanyId(companyId);
        }
        
        EnumConverter.getEnumFromString(rs, ROLE_COLUMN, UserRole.class)
                .ifPresent(user::setRole);
        
        user.setEmail(rs.getString(EMAIL_COLUMN));
        user.setName(rs.getString(NAME_COLUMN));
        user.setPasswordHash(rs.getString(PASSWORD_HASH_COLUMN));
        
        EnumConverter.getEnumFromString(rs, STATUS_COLUMN, UserStatus.class)
                .ifPresent(user::setStatus);
        
        user.setCreatedAt(rs.getTimestamp(CREATED_AT_COLUMN));
        user.setUpdatedAt(rs.getTimestamp(UPDATED_AT_COLUMN));
        
        return user;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, User user) throws SQLException {
        if (user.getCompanyId() != null) {
            ps.setInt(1, user.getCompanyId());
        } else {
            ps.setNull(1, java.sql.Types.INTEGER);
        }
        
        EnumConverter.setEnumAsString(ps, 2, user.getRole());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getName());
        ps.setString(5, user.getPasswordHash());
        EnumConverter.setEnumAsString(ps, 6, user.getStatus());
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        ps.setTimestamp(7, now);
        ps.setTimestamp(8, now);
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, User user) throws SQLException {
        if (user.getCompanyId() != null) {
            ps.setInt(1, user.getCompanyId());
        } else {
            ps.setNull(1, java.sql.Types.INTEGER);
        }
        
        EnumConverter.setEnumAsString(ps, 2, user.getRole());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getName());
        EnumConverter.setEnumAsString(ps, 5, user.getStatus());
        ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
        ps.setInt(7, user.getId());
    }
    
    @Override
    public User create(User user) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(ps, user);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
            
            return user;
        }
    }
    
    @Override
    public User update(User user) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            
            setUpdateParameters(ps, user);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating user failed, no rows affected.");
            }
            
            return user;
        }
    }
    
    @Override
    public Optional<User> findByEmail(String email) throws SQLException {
        return executeQueryForObject(FIND_BY_EMAIL_SQL, ps -> 
            ps.setString(1, email)
        );
    }
    
    @Override
    public List<User> findByCompanyId(Integer companyId) throws SQLException {
        return executeQuery(FIND_BY_COMPANY_ID_SQL, ps -> 
            ps.setInt(1, companyId)
        );
    }
    
    @Override
    public List<User> findByRole(UserRole role) throws SQLException {
        return executeQuery(FIND_BY_ROLE_SQL, ps -> 
            EnumConverter.setEnumAsString(ps, 1, role)
        );
    }
    
    @Override
    public List<User> findByStatus(UserStatus status) throws SQLException {
        return executeQuery(FIND_BY_STATUS_SQL, ps -> 
            EnumConverter.setEnumAsString(ps, 1, status)
        );
    }
    
    @Override
    public List<User> findByCompanyIdAndRole(Integer companyId, UserRole role) throws SQLException {
        return executeQuery(FIND_BY_COMPANY_ID_AND_ROLE_SQL, ps -> {
            ps.setInt(1, companyId);
            EnumConverter.setEnumAsString(ps, 2, role);
        });
    }
    
    @Override
    public boolean updatePassword(Integer id, String passwordHash) throws SQLException {
        return executeUpdate(UPDATE_PASSWORD_SQL, ps -> {
            ps.setString(1, passwordHash);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, id);
        }) > 0;
    }
    
    @Override
    public boolean updateStatus(Integer id, UserStatus status) throws SQLException {
        return executeUpdate(UPDATE_STATUS_SQL, ps -> {
            EnumConverter.setEnumAsString(ps, 1, status);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, id);
        }) > 0;
    }
    
    @Override
    public Optional<User> authenticate(String email, String passwordHash) throws SQLException {
        return executeQueryForObject(AUTHENTICATE_SQL, ps -> {
            ps.setString(1, email);
            ps.setString(2, passwordHash);
            EnumConverter.setEnumAsString(ps, 3, UserStatus.ACTIVE);
        });
    }
    
    @Override
    public Optional<User> findActiveUserByEmail(String email) throws SQLException {
        return executeQueryForObject(FIND_ACTIVE_USER_BY_EMAIL_SQL, ps -> {
            ps.setString(1, email);
            EnumConverter.setEnumAsString(ps, 2, UserStatus.ACTIVE);
        });
    }
    
    @Override
    public boolean isUserLockedOut(Integer userId) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(IS_USER_LOCKED_OUT_SQL)) {
            
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }
    
    @Override
    public boolean hasRole(Integer userId, UserRole role) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(HAS_ROLE_SQL)) {
            
            ps.setInt(1, userId);
            EnumConverter.setEnumAsString(ps, 2, role);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }
    
    @Override
    public boolean updateRole(Integer id, UserRole role) throws SQLException {
        return executeUpdate(UPDATE_ROLE_SQL, ps -> {
            EnumConverter.setEnumAsString(ps, 1, role);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, id);
        }) > 0;
    }
    
    @Override
    public int countActiveSessions(Integer userId) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_ACTIVE_SESSIONS_SQL)) {
            
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }
    
    @Override
    public int invalidateAllSessions(Integer userId) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(INVALIDATE_ALL_SESSIONS_SQL)) {
            
            ps.setInt(1, userId);
            
            return ps.executeUpdate();
        }
    }
    
    @Override
    public List<User> findByCompanyIdAndRoleAndStatus(Integer companyId, UserRole role, UserStatus status) throws SQLException {
        if (status == null) {
            // If status is null, use the existing findByCompanyIdAndRole method
            return findByCompanyIdAndRole(companyId, role);
        }
        
        return executeQuery(FIND_BY_COMPANY_ID_AND_ROLE_AND_STATUS_SQL, ps -> {
            ps.setInt(1, companyId);
            EnumConverter.setEnumAsString(ps, 2, role);
            EnumConverter.setEnumAsString(ps, 3, status);
        });
    }
    
    @Override
    public List<User> findByEmailContaining(String searchTerm, Integer limit) throws SQLException {
        String sql = FIND_BY_EMAIL_CONTAINING_SQL;
        if (limit != null && limit > 0) {
            sql += " LIMIT " + limit;
        }
        
        final String finalSql = sql;
        return executeQuery(finalSql, ps -> {
            ps.setString(1, "%" + searchTerm + "%");
        });
    }
    
    @Override
    public List<User> findByCreatedAtAfter(Timestamp date) throws SQLException {
        return executeQuery(FIND_BY_CREATED_AT_AFTER_SQL, ps -> {
            ps.setTimestamp(1, date);
        });
    }
    
    @Override
    public boolean isPasswordPreviouslyUsed(Integer userId, String passwordHash, Integer limit) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(IS_PASSWORD_PREVIOUSLY_USED_SQL)) {
            
            ps.setInt(1, userId);
            ps.setString(2, passwordHash);
            ps.setInt(3, limit != null && limit > 0 ? limit : 5); // Default to checking the last 5 passwords
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }
    
    @Override
    public boolean addPasswordToHistory(Integer userId, String passwordHash) throws SQLException {
        return executeUpdate(ADD_PASSWORD_TO_HISTORY_SQL, ps -> {
            ps.setInt(1, userId);
            ps.setString(2, passwordHash);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        }) > 0;
    }
}