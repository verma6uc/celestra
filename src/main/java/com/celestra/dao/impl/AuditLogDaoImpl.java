package com.celestra.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import com.celestra.dao.AbstractBaseDao;
import com.celestra.dao.AuditLogDao;
import com.celestra.dao.EnumConverter;
import com.celestra.db.DatabaseUtil;
import com.celestra.enums.AuditEventType;
import com.celestra.model.AuditLog;

/**
 * Implementation of the AuditLogDao interface.
 */
public class AuditLogDaoImpl extends AbstractBaseDao<AuditLog, Integer> implements AuditLogDao {
    
    private static final String TABLE_NAME = "audit_logs";
    private static final String ID_COLUMN = "id";
    private static final String USER_ID_COLUMN = "user_id";
    private static final String EVENT_TYPE_COLUMN = "event_type";
    private static final String EVENT_DESCRIPTION_COLUMN = "event_description";
    private static final String IP_ADDRESS_COLUMN = "ip_address";
    private static final String SIGNED_BY_COLUMN = "signed_by";
    private static final String DIGITAL_SIGNATURE_COLUMN = "digital_signature";
    private static final String REASON_COLUMN = "reason";
    private static final String TABLE_NAME_COLUMN = "table_name";
    private static final String RECORD_ID_COLUMN = "record_id";
    private static final String GROUP_ID_COLUMN = "group_id";
    private static final String CREATED_AT_COLUMN = "created_at";
    
    private static final String INSERT_SQL = 
            "INSERT INTO " + TABLE_NAME + " (" + 
            USER_ID_COLUMN + ", " + 
            EVENT_TYPE_COLUMN + ", " + 
            EVENT_DESCRIPTION_COLUMN + ", " + 
            IP_ADDRESS_COLUMN + ", " + 
            SIGNED_BY_COLUMN + ", " + 
            DIGITAL_SIGNATURE_COLUMN + ", " + 
            REASON_COLUMN + ", " + 
            TABLE_NAME_COLUMN + ", " + 
            RECORD_ID_COLUMN + ", " + 
            GROUP_ID_COLUMN + ", " + 
            CREATED_AT_COLUMN + 
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            USER_ID_COLUMN + " = ?, " + 
            EVENT_TYPE_COLUMN + " = ?, " + 
            EVENT_DESCRIPTION_COLUMN + " = ?, " + 
            IP_ADDRESS_COLUMN + " = ?, " + 
            SIGNED_BY_COLUMN + " = ?, " + 
            DIGITAL_SIGNATURE_COLUMN + " = ?, " + 
            REASON_COLUMN + " = ?, " + 
            TABLE_NAME_COLUMN + " = ?, " + 
            RECORD_ID_COLUMN + " = ?, " + 
            GROUP_ID_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String FIND_BY_USER_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + USER_ID_COLUMN + " = ?";
    
    private static final String FIND_BY_EVENT_TYPE_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + EVENT_TYPE_COLUMN + " = ?";
    
    private static final String FIND_BY_TABLE_NAME_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + TABLE_NAME_COLUMN + " = ?";
    
    private static final String FIND_BY_RECORD_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + RECORD_ID_COLUMN + " = ?";
    
    private static final String FIND_BY_GROUP_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + GROUP_ID_COLUMN + " = ?";
    
    private static final String FIND_BY_DATE_RANGE_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + CREATED_AT_COLUMN + " >= ? AND " + CREATED_AT_COLUMN + " <= ?";
    
    private static final String FIND_BY_USER_ID_AND_EVENT_TYPE_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + USER_ID_COLUMN + " = ? AND " + EVENT_TYPE_COLUMN + " = ?";
    
    private static final String FIND_BY_TABLE_NAME_AND_RECORD_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + TABLE_NAME_COLUMN + " = ? AND " + RECORD_ID_COLUMN + " = ?";
    
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
    protected AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog auditLog = new AuditLog();
        
        auditLog.setId(rs.getInt(ID_COLUMN));
        
        Integer userId = rs.getInt(USER_ID_COLUMN);
        if (!rs.wasNull()) {
            auditLog.setUserId(userId);
        }
        
        EnumConverter.getEnumFromString(rs, EVENT_TYPE_COLUMN, AuditEventType.class)
                .ifPresent(auditLog::setEventType);
        
        auditLog.setEventDescription(rs.getString(EVENT_DESCRIPTION_COLUMN));
        auditLog.setIpAddress(rs.getString(IP_ADDRESS_COLUMN));
        
        Integer signedBy = rs.getInt(SIGNED_BY_COLUMN);
        if (!rs.wasNull()) {
            auditLog.setSignedBy(signedBy);
        }
        
        auditLog.setDigitalSignature(rs.getString(DIGITAL_SIGNATURE_COLUMN));
        auditLog.setReason(rs.getString(REASON_COLUMN));
        auditLog.setTableName(rs.getString(TABLE_NAME_COLUMN));
        auditLog.setRecordId(rs.getString(RECORD_ID_COLUMN));
        
        String groupIdStr = rs.getString(GROUP_ID_COLUMN);
        if (groupIdStr != null) {
            auditLog.setGroupId(UUID.fromString(groupIdStr));
        }
        
        auditLog.setCreatedAt(rs.getTimestamp(CREATED_AT_COLUMN));
        
        return auditLog;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, AuditLog auditLog) throws SQLException {
        if (auditLog.getUserId() != null) {
            ps.setInt(1, auditLog.getUserId());
        } else {
            ps.setNull(1, java.sql.Types.INTEGER);
        }
        
        EnumConverter.setEnumAsString(ps, 2, auditLog.getEventType());
        ps.setString(3, auditLog.getEventDescription());
        ps.setString(4, auditLog.getIpAddress());
        
        if (auditLog.getSignedBy() != null) {
            ps.setInt(5, auditLog.getSignedBy());
        } else {
            ps.setNull(5, java.sql.Types.INTEGER);
        }
        
        ps.setString(6, auditLog.getDigitalSignature());
        ps.setString(7, auditLog.getReason());
        ps.setString(8, auditLog.getTableName());
        ps.setString(9, auditLog.getRecordId());
        
        if (auditLog.getGroupId() != null) {
            ps.setString(10, auditLog.getGroupId().toString());
        } else {
            ps.setNull(10, java.sql.Types.VARCHAR);
        }
        
        ps.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, AuditLog auditLog) throws SQLException {
        if (auditLog.getUserId() != null) {
            ps.setInt(1, auditLog.getUserId());
        } else {
            ps.setNull(1, java.sql.Types.INTEGER);
        }
        
        EnumConverter.setEnumAsString(ps, 2, auditLog.getEventType());
        ps.setString(3, auditLog.getEventDescription());
        ps.setString(4, auditLog.getIpAddress());
        
        if (auditLog.getSignedBy() != null) {
            ps.setInt(5, auditLog.getSignedBy());
        } else {
            ps.setNull(5, java.sql.Types.INTEGER);
        }
        
        ps.setString(6, auditLog.getDigitalSignature());
        ps.setString(7, auditLog.getReason());
        ps.setString(8, auditLog.getTableName());
        ps.setString(9, auditLog.getRecordId());
        
        if (auditLog.getGroupId() != null) {
            ps.setString(10, auditLog.getGroupId().toString());
        } else {
            ps.setNull(10, java.sql.Types.VARCHAR);
        }
        
        ps.setInt(11, auditLog.getId());
    }
    
    @Override
    public AuditLog create(AuditLog auditLog) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(ps, auditLog);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating audit log failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    auditLog.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating audit log failed, no ID obtained.");
                }
            }
            
            return auditLog;
        }
    }
    
    @Override
    public AuditLog update(AuditLog auditLog) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            
            setUpdateParameters(ps, auditLog);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating audit log failed, no rows affected.");
            }
            
            return auditLog;
        }
    }
    
    @Override
    public List<AuditLog> findByUserId(Integer userId) throws SQLException {
        return executeQuery(FIND_BY_USER_ID_SQL, ps -> 
            ps.setInt(1, userId)
        );
    }
    
    @Override
    public List<AuditLog> findByEventType(AuditEventType eventType) throws SQLException {
        return executeQuery(FIND_BY_EVENT_TYPE_SQL, ps -> 
            EnumConverter.setEnumAsString(ps, 1, eventType)
        );
    }
    
    @Override
    public List<AuditLog> findByTableName(String tableName) throws SQLException {
        return executeQuery(FIND_BY_TABLE_NAME_SQL, ps -> 
            ps.setString(1, tableName)
        );
    }
    
    @Override
    public List<AuditLog> findByRecordId(String recordId) throws SQLException {
        return executeQuery(FIND_BY_RECORD_ID_SQL, ps -> 
            ps.setString(1, recordId)
        );
    }
    
    @Override
    public List<AuditLog> findByGroupId(UUID groupId) throws SQLException {
        return executeQuery(FIND_BY_GROUP_ID_SQL, ps -> 
            ps.setString(1, groupId.toString())
        );
    }
    
    @Override
    public List<AuditLog> findByDateRange(String startDate, String endDate) throws SQLException {
        return executeQuery(FIND_BY_DATE_RANGE_SQL, ps -> {
            ps.setString(1, startDate + " 00:00:00");
            ps.setString(2, endDate + " 23:59:59");
        });
    }
    
    @Override
    public List<AuditLog> findByUserIdAndEventType(Integer userId, AuditEventType eventType) throws SQLException {
        return executeQuery(FIND_BY_USER_ID_AND_EVENT_TYPE_SQL, ps -> {
            ps.setInt(1, userId);
            EnumConverter.setEnumAsString(ps, 2, eventType);
        });
    }
    
    @Override
    public List<AuditLog> findByTableNameAndRecordId(String tableName, String recordId) throws SQLException {
        return executeQuery(FIND_BY_TABLE_NAME_AND_RECORD_ID_SQL, ps -> {
            ps.setString(1, tableName);
            ps.setString(2, recordId);
        });
    }
}