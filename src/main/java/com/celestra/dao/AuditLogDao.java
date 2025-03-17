package com.celestra.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

import com.celestra.enums.AuditEventType;
import com.celestra.model.AuditLog;

/**
 * Data Access Object (DAO) interface for AuditLog entities.
 */
public interface AuditLogDao extends BaseDao<AuditLog, Integer> {
    
    /**
     * Find audit logs by user ID.
     * 
     * @param userId The user ID to search for
     * @return A list of audit logs for the specified user
     * @throws SQLException if a database access error occurs
     */
    List<AuditLog> findByUserId(Integer userId) throws SQLException;
    
    /**
     * Find audit logs by event type.
     * 
     * @param eventType The event type to search for
     * @return A list of audit logs with the specified event type
     * @throws SQLException if a database access error occurs
     */
    List<AuditLog> findByEventType(AuditEventType eventType) throws SQLException;
    
    /**
     * Find audit logs by table name.
     * 
     * @param tableName The table name to search for
     * @return A list of audit logs for the specified table
     * @throws SQLException if a database access error occurs
     */
    List<AuditLog> findByTableName(String tableName) throws SQLException;
    
    /**
     * Find audit logs by record ID.
     * 
     * @param recordId The record ID to search for
     * @return A list of audit logs for the specified record
     * @throws SQLException if a database access error occurs
     */
    List<AuditLog> findByRecordId(String recordId) throws SQLException;
    
    /**
     * Find audit logs by group ID.
     * 
     * @param groupId The group ID to search for
     * @return A list of audit logs with the specified group ID
     * @throws SQLException if a database access error occurs
     */
    List<AuditLog> findByGroupId(UUID groupId) throws SQLException;
    
    /**
     * Find audit logs by date range.
     * 
     * @param startDate The start date (inclusive) in ISO format (yyyy-MM-dd)
     * @param endDate The end date (inclusive) in ISO format (yyyy-MM-dd)
     * @return A list of audit logs within the specified date range
     * @throws SQLException if a database access error occurs
     */
    List<AuditLog> findByDateRange(String startDate, String endDate) throws SQLException;
    
    /**
     * Find audit logs by user ID and event type.
     * 
     * @param userId The user ID to search for
     * @param eventType The event type to search for
     * @return A list of audit logs for the specified user with the specified event type
     * @throws SQLException if a database access error occurs
     */
    List<AuditLog> findByUserIdAndEventType(Integer userId, AuditEventType eventType) throws SQLException;
    
    /**
     * Find audit logs by table name and record ID.
     * 
     * @param tableName The table name to search for
     * @param recordId The record ID to search for
     * @return A list of audit logs for the specified table and record
     * @throws SQLException if a database access error occurs
     */
    List<AuditLog> findByTableNameAndRecordId(String tableName, String recordId) throws SQLException;
    
    /**
     * Find an audit log by event description.
     * 
     * @param eventDescription The event description to search for
     * @return An Optional containing the audit log if found, or empty if not found
     * @throws SQLException if a database access error occurs
     */
    Optional<AuditLog> findByEventDescription(String eventDescription) throws SQLException;
}