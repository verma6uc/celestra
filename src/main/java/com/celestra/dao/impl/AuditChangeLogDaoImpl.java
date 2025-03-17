package com.celestra.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import com.celestra.dao.AbstractBaseDao;
import com.celestra.dao.AuditChangeLogDao;
import com.celestra.db.DatabaseUtil;
import com.celestra.model.AuditChangeLog;

/**
 * Implementation of the AuditChangeLogDao interface.
 */
public class AuditChangeLogDaoImpl extends AbstractBaseDao<AuditChangeLog, Integer> implements AuditChangeLogDao {
    
    private static final String TABLE_NAME = "audit_change_logs";
    private static final String ID_COLUMN = "id";
    private static final String AUDIT_LOG_ID_COLUMN = "audit_log_id";
    private static final String COLUMN_NAME_COLUMN = "column_name";
    private static final String OLD_VALUE_COLUMN = "old_value";
    private static final String NEW_VALUE_COLUMN = "new_value";
    private static final String CREATED_AT_COLUMN = "created_at";
    
    private static final String INSERT_SQL = 
            "INSERT INTO " + TABLE_NAME + " (" + 
            AUDIT_LOG_ID_COLUMN + ", " + 
            COLUMN_NAME_COLUMN + ", " + 
            OLD_VALUE_COLUMN + ", " + 
            NEW_VALUE_COLUMN + ", " + 
            CREATED_AT_COLUMN + 
            ") VALUES (?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            AUDIT_LOG_ID_COLUMN + " = ?, " + 
            COLUMN_NAME_COLUMN + " = ?, " + 
            OLD_VALUE_COLUMN + " = ?, " + 
            NEW_VALUE_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String FIND_BY_AUDIT_LOG_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + AUDIT_LOG_ID_COLUMN + " = ?";
    
    private static final String FIND_BY_COLUMN_NAME_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_COLUMN + " = ?";
    
    private static final String FIND_BY_OLD_VALUE_CONTAINING_SQL =
            "SELECT * FROM " + TABLE_NAME + " WHERE " + OLD_VALUE_COLUMN + " LIKE ?";
    
    private static final String FIND_BY_NEW_VALUE_CONTAINING_SQL =
            "SELECT * FROM " + TABLE_NAME + " WHERE " + NEW_VALUE_COLUMN + " LIKE ?";
    
    private static final String FIND_BY_AUDIT_LOG_ID_AND_COLUMN_NAME_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + 
            AUDIT_LOG_ID_COLUMN + " = ? AND " + 
            COLUMN_NAME_COLUMN + " = ?";
    
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
    protected AuditChangeLog mapRow(ResultSet rs) throws SQLException {
        AuditChangeLog auditChangeLog = new AuditChangeLog();
        
        auditChangeLog.setId(rs.getInt(ID_COLUMN));
        auditChangeLog.setAuditLogId(rs.getInt(AUDIT_LOG_ID_COLUMN));
        auditChangeLog.setColumnName(rs.getString(COLUMN_NAME_COLUMN));
        auditChangeLog.setOldValue(rs.getString(OLD_VALUE_COLUMN));
        auditChangeLog.setNewValue(rs.getString(NEW_VALUE_COLUMN));
        
        auditChangeLog.setCreatedAt(rs.getTimestamp(CREATED_AT_COLUMN));
        
        return auditChangeLog;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, AuditChangeLog auditChangeLog) throws SQLException {
        ps.setInt(1, auditChangeLog.getAuditLogId());
        ps.setString(2, auditChangeLog.getColumnName());
        
        if (auditChangeLog.getOldValue() != null) {
            ps.setString(3, auditChangeLog.getOldValue());
        } else {
            ps.setNull(3, java.sql.Types.VARCHAR);
        }
        
        if (auditChangeLog.getNewValue() != null) {
            ps.setString(4, auditChangeLog.getNewValue());
        } else {
            ps.setNull(4, java.sql.Types.VARCHAR);
        }
        
        ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, AuditChangeLog auditChangeLog) throws SQLException {
        ps.setInt(1, auditChangeLog.getAuditLogId());
        ps.setString(2, auditChangeLog.getColumnName());
        
        if (auditChangeLog.getOldValue() != null) {
            ps.setString(3, auditChangeLog.getOldValue());
        } else {
            ps.setNull(3, java.sql.Types.VARCHAR);
        }
        
        if (auditChangeLog.getNewValue() != null) {
            ps.setString(4, auditChangeLog.getNewValue());
        } else {
            ps.setNull(4, java.sql.Types.VARCHAR);
        }
        
        ps.setInt(5, auditChangeLog.getId());
    }
    
    @Override
    public AuditChangeLog create(AuditChangeLog auditChangeLog) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(ps, auditChangeLog);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating audit change log failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    auditChangeLog.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating audit change log failed, no ID obtained.");
                }
            }
            
            return auditChangeLog;
        }
    }
    
    @Override
    public AuditChangeLog update(AuditChangeLog auditChangeLog) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            
            setUpdateParameters(ps, auditChangeLog);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating audit change log failed, no rows affected.");
            }
            
            return auditChangeLog;
        }
    }
    
    @Override
    public List<AuditChangeLog> findByAuditLogId(Integer auditLogId) throws SQLException {
        return executeQuery(FIND_BY_AUDIT_LOG_ID_SQL, ps -> 
            ps.setInt(1, auditLogId)
        );
    }
    
    @Override
    public List<AuditChangeLog> findByColumnName(String columnName) throws SQLException {
        return executeQuery(FIND_BY_COLUMN_NAME_SQL, ps -> 
            ps.setString(1, columnName)
        );
    }
    
    @Override
    public List<AuditChangeLog> findByOldValueContaining(String oldValuePattern) throws SQLException {
        return executeQuery(FIND_BY_OLD_VALUE_CONTAINING_SQL, ps -> 
            ps.setString(1, "%" + oldValuePattern + "%")
        );
    }
    
    @Override
    public List<AuditChangeLog> findByNewValueContaining(String newValuePattern) throws SQLException {
        return executeQuery(FIND_BY_NEW_VALUE_CONTAINING_SQL, ps -> 
            ps.setString(1, "%" + newValuePattern + "%")
        );
    }
    
    @Override
    public List<AuditChangeLog> findByAuditLogIdAndColumnName(Integer auditLogId, String columnName) throws SQLException {
        return executeQuery(FIND_BY_AUDIT_LOG_ID_AND_COLUMN_NAME_SQL, ps -> {
            ps.setInt(1, auditLogId);
            ps.setString(2, columnName);
        });
    }
}