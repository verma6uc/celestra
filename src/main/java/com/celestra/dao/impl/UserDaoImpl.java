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
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            COMPANY_ID_COLUMN + " = ?, " + 
            ROLE_COLUMN + " = ?, " + 
            EMAIL_COLUMN + " = ?, " + 
            NAME_COLUMN + " = ?, " + 
            STATUS_COLUMN + " = ?, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String FIND_BY_EMAIL_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + EMAIL_COLUMN + " = ?";
    
    private static final String FIND_BY_COMPANY_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + COMPANY_ID_COLUMN + " = ?";
    
    private static final String FIND_BY_ROLE_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + ROLE_COLUMN + " = ?";
    
    private static final String FIND_BY_STATUS_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COLUMN + " = ?";
    
    private static final String FIND_BY_COMPANY_ID_AND_ROLE_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + COMPANY_ID_COLUMN + " = ? AND " + ROLE_COLUMN + " = ?";
    
    private static final String UPDATE_PASSWORD_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            PASSWORD_HASH_COLUMN + " = ?, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String UPDATE_STATUS_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            STATUS_COLUMN + " = ?, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String AUTHENTICATE_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + 
            EMAIL_COLUMN + " = ? AND " + 
            PASSWORD_HASH_COLUMN + " = ? AND " + 
            STATUS_COLUMN + " = ?";
    
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
}