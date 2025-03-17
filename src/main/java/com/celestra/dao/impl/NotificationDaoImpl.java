package com.celestra.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import com.celestra.dao.AbstractBaseDao;
import com.celestra.dao.EnumConverter;
import com.celestra.dao.NotificationDao;
import com.celestra.db.DatabaseUtil;
import com.celestra.enums.NotificationDeliveryMethod;
import com.celestra.enums.NotificationPriority;
import com.celestra.enums.NotificationStatus;
import com.celestra.enums.NotificationType;
import com.celestra.model.Notification;

/**
 * Implementation of the NotificationDao interface.
 */
public class NotificationDaoImpl extends AbstractBaseDao<Notification, Integer> implements NotificationDao {
    
    private static final String TABLE_NAME = "notifications";
    private static final String ID_COLUMN = "id";
    private static final String USER_ID_COLUMN = "user_id";
    private static final String COMPANY_ID_COLUMN = "company_id";
    private static final String NOTIFICATION_TYPE_COLUMN = "notification_type";
    private static final String TITLE_COLUMN = "title";
    private static final String MESSAGE_COLUMN = "message";
    private static final String PRIORITY_COLUMN = "priority";
    private static final String STATUS_COLUMN = "status";
    private static final String DELIVERY_METHOD_COLUMN = "delivery_method";
    private static final String READ_AT_COLUMN = "read_at";
    private static final String ACTION_URL_COLUMN = "action_url";
    private static final String EXPIRES_AT_COLUMN = "expires_at";
    private static final String DELIVERED_AT_COLUMN = "delivered_at";
    private static final String CREATED_AT_COLUMN = "created_at";
    private static final String UPDATED_AT_COLUMN = "updated_at";
    
    private static final String INSERT_SQL = 
            "INSERT INTO " + TABLE_NAME + " (" + 
            USER_ID_COLUMN + ", " + 
            COMPANY_ID_COLUMN + ", " + 
            NOTIFICATION_TYPE_COLUMN + ", " + 
            TITLE_COLUMN + ", " + 
            MESSAGE_COLUMN + ", " + 
            PRIORITY_COLUMN + ", " + 
            STATUS_COLUMN + ", " + 
            DELIVERY_METHOD_COLUMN + ", " + 
            READ_AT_COLUMN + ", " + 
            ACTION_URL_COLUMN + ", " + 
            EXPIRES_AT_COLUMN + ", " + 
            DELIVERED_AT_COLUMN + ", " + 
            CREATED_AT_COLUMN + ", " + 
            UPDATED_AT_COLUMN + 
            ") VALUES (?, ?, ?::notification_type, ?, ?, ?::notification_priority, ?::notification_status, " + 
            "?::notification_delivery_method, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            USER_ID_COLUMN + " = ?, " + 
            COMPANY_ID_COLUMN + " = ?, " + 
            NOTIFICATION_TYPE_COLUMN + " = ?::notification_type, " + 
            TITLE_COLUMN + " = ?, " + 
            MESSAGE_COLUMN + " = ?, " + 
            PRIORITY_COLUMN + " = ?::notification_priority, " + 
            STATUS_COLUMN + " = ?::notification_status, " + 
            DELIVERY_METHOD_COLUMN + " = ?::notification_delivery_method, " + 
            READ_AT_COLUMN + " = ?, " + 
            ACTION_URL_COLUMN + " = ?, " + 
            EXPIRES_AT_COLUMN + " = ?, " + 
            DELIVERED_AT_COLUMN + " = ?, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String FIND_BY_USER_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + USER_ID_COLUMN + " = ?";
    
    private static final String FIND_BY_COMPANY_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + COMPANY_ID_COLUMN + " = ?";
    
    private static final String FIND_BY_TYPE_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + NOTIFICATION_TYPE_COLUMN + " = ?::notification_type";
    
    private static final String FIND_BY_STATUS_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COLUMN + " = ?::notification_status";
    
    private static final String FIND_BY_PRIORITY_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + PRIORITY_COLUMN + " = ?::notification_priority";
    
    private static final String FIND_BY_DELIVERY_METHOD_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + DELIVERY_METHOD_COLUMN + " = ?::notification_delivery_method";
    
    private static final String FIND_UNREAD_BY_USER_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + USER_ID_COLUMN + " = ? AND " + READ_AT_COLUMN + " IS NULL";
    
    private static final String MARK_AS_READ_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            READ_AT_COLUMN + " = ?, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String UPDATE_STATUS_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            STATUS_COLUMN + " = ?::notification_status, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String FIND_EXPIRED_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + 
            EXPIRES_AT_COLUMN + " < NOW() AND " + 
            EXPIRES_AT_COLUMN + " IS NOT NULL";
    
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
    protected Notification mapRow(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        
        notification.setId(rs.getInt(ID_COLUMN));
        notification.setUserId(rs.getInt(USER_ID_COLUMN));
        
        Integer companyId = rs.getInt(COMPANY_ID_COLUMN);
        if (!rs.wasNull()) {
            notification.setCompanyId(companyId);
        }
        
        EnumConverter.getEnumFromString(rs, NOTIFICATION_TYPE_COLUMN, NotificationType.class)
                .ifPresent(notification::setNotificationType);
        
        notification.setTitle(rs.getString(TITLE_COLUMN));
        notification.setMessage(rs.getString(MESSAGE_COLUMN));
        
        EnumConverter.getEnumFromString(rs, PRIORITY_COLUMN, NotificationPriority.class)
                .ifPresent(notification::setPriority);
        
        EnumConverter.getEnumFromString(rs, STATUS_COLUMN, NotificationStatus.class)
                .ifPresent(notification::setStatus);
        
        EnumConverter.getEnumFromString(rs, DELIVERY_METHOD_COLUMN, NotificationDeliveryMethod.class)
                .ifPresent(notification::setDeliveryMethod);
        
        notification.setReadAt(rs.getTimestamp(READ_AT_COLUMN));
        notification.setActionUrl(rs.getString(ACTION_URL_COLUMN));
        notification.setExpiresAt(rs.getTimestamp(EXPIRES_AT_COLUMN));
        notification.setDeliveredAt(rs.getTimestamp(DELIVERED_AT_COLUMN));
        notification.setCreatedAt(rs.getTimestamp(CREATED_AT_COLUMN));
        notification.setUpdatedAt(rs.getTimestamp(UPDATED_AT_COLUMN));
        
        return notification;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, Notification notification) throws SQLException {
        ps.setInt(1, notification.getUserId());
        
        if (notification.getCompanyId() != null) {
            ps.setInt(2, notification.getCompanyId());
        } else {
            ps.setNull(2, java.sql.Types.INTEGER);
        }
        
        EnumConverter.setEnumAsString(ps, 3, notification.getNotificationType());
        ps.setString(4, notification.getTitle());
        ps.setString(5, notification.getMessage());
        EnumConverter.setEnumAsString(ps, 6, notification.getPriority());
        EnumConverter.setEnumAsString(ps, 7, notification.getStatus());
        EnumConverter.setEnumAsString(ps, 8, notification.getDeliveryMethod());
        
        if (notification.getReadAt() != null) {
            ps.setTimestamp(9, notification.getReadAt());
        } else {
            ps.setNull(9, java.sql.Types.TIMESTAMP);
        }
        
        ps.setString(10, notification.getActionUrl());
        
        if (notification.getExpiresAt() != null) {
            ps.setTimestamp(11, notification.getExpiresAt());
        } else {
            ps.setNull(11, java.sql.Types.TIMESTAMP);
        }
        
        if (notification.getDeliveredAt() != null) {
            ps.setTimestamp(12, notification.getDeliveredAt());
        } else {
            ps.setNull(12, java.sql.Types.TIMESTAMP);
        }
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        ps.setTimestamp(13, now);
        ps.setTimestamp(14, now);
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, Notification notification) throws SQLException {
        ps.setInt(1, notification.getUserId());
        
        if (notification.getCompanyId() != null) {
            ps.setInt(2, notification.getCompanyId());
        } else {
            ps.setNull(2, java.sql.Types.INTEGER);
        }
        
        EnumConverter.setEnumAsString(ps, 3, notification.getNotificationType());
        ps.setString(4, notification.getTitle());
        ps.setString(5, notification.getMessage());
        EnumConverter.setEnumAsString(ps, 6, notification.getPriority());
        EnumConverter.setEnumAsString(ps, 7, notification.getStatus());
        EnumConverter.setEnumAsString(ps, 8, notification.getDeliveryMethod());
        
        if (notification.getReadAt() != null) {
            ps.setTimestamp(9, notification.getReadAt());
        } else {
            ps.setNull(9, java.sql.Types.TIMESTAMP);
        }
        
        ps.setString(10, notification.getActionUrl());
        
        if (notification.getExpiresAt() != null) {
            ps.setTimestamp(11, notification.getExpiresAt());
        } else {
            ps.setNull(11, java.sql.Types.TIMESTAMP);
        }
        
        if (notification.getDeliveredAt() != null) {
            ps.setTimestamp(12, notification.getDeliveredAt());
        } else {
            ps.setNull(12, java.sql.Types.TIMESTAMP);
        }
        
        ps.setTimestamp(13, new Timestamp(System.currentTimeMillis()));
        ps.setInt(14, notification.getId());
    }
    
    @Override
    public Notification create(Notification notification) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(ps, notification);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating notification failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    notification.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating notification failed, no ID obtained.");
                }
            }
            
            return notification;
        }
    }
    
    @Override
    public Notification update(Notification notification) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            
            setUpdateParameters(ps, notification);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating notification failed, no rows affected.");
            }
            
            return notification;
        }
    }
    
    @Override
    public List<Notification> findByUserId(Integer userId) throws SQLException {
        return executeQuery(FIND_BY_USER_ID_SQL, ps -> 
            ps.setInt(1, userId)
        );
    }
    
    @Override
    public List<Notification> findByCompanyId(Integer companyId) throws SQLException {
        return executeQuery(FIND_BY_COMPANY_ID_SQL, ps -> 
            ps.setInt(1, companyId)
        );
    }
    
    @Override
    public List<Notification> findByType(NotificationType type) throws SQLException {
        return executeQuery(FIND_BY_TYPE_SQL, ps -> 
            EnumConverter.setEnumAsString(ps, 1, type)
        );
    }
    
    @Override
    public List<Notification> findByStatus(NotificationStatus status) throws SQLException {
        return executeQuery(FIND_BY_STATUS_SQL, ps -> 
            EnumConverter.setEnumAsString(ps, 1, status)
        );
    }
    
    @Override
    public List<Notification> findByPriority(NotificationPriority priority) throws SQLException {
        return executeQuery(FIND_BY_PRIORITY_SQL, ps -> 
            EnumConverter.setEnumAsString(ps, 1, priority)
        );
    }
    
    @Override
    public List<Notification> findByDeliveryMethod(NotificationDeliveryMethod deliveryMethod) throws SQLException {
        return executeQuery(FIND_BY_DELIVERY_METHOD_SQL, ps -> 
            EnumConverter.setEnumAsString(ps, 1, deliveryMethod)
        );
    }
    
    @Override
    public List<Notification> findUnreadByUserId(Integer userId) throws SQLException {
        return executeQuery(FIND_UNREAD_BY_USER_ID_SQL, ps -> 
            ps.setInt(1, userId)
        );
    }
    
    @Override
    public boolean markAsRead(Integer id) throws SQLException {
        return executeUpdate(MARK_AS_READ_SQL, ps -> {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, id);
        }) > 0;
    }
    
    @Override
    public boolean updateStatus(Integer id, NotificationStatus status) throws SQLException {
        return executeUpdate(UPDATE_STATUS_SQL, ps -> {
            EnumConverter.setEnumAsString(ps, 1, status);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, id);
        }) > 0;
    }
    
    @Override
    public List<Notification> findExpired() throws SQLException {
        return executeQuery(FIND_EXPIRED_SQL, ps -> {
            // No parameters needed
        });
    }
}