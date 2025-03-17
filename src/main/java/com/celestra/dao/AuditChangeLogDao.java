package com.celestra.dao;

import java.sql.SQLException;
import java.util.List;

import com.celestra.model.AuditChangeLog;

/**
 * Data Access Object interface for AuditChangeLog entities.
 */
public interface AuditChangeLogDao extends BaseDao<AuditChangeLog, Integer> {
    
    /**
     * Find audit change logs by audit log ID.
     * 
     * @param auditLogId The audit log ID
     * @return A list of audit change logs for the audit log
     * @throws SQLException if a database access error occurs
     */
    List<AuditChangeLog> findByAuditLogId(Integer auditLogId) throws SQLException;
    
    /**
     * Find audit change logs by column name.
     * 
     * @param columnName The column name
     * @return A list of audit change logs for the column
     * @throws SQLException if a database access error occurs
     */
    List<AuditChangeLog> findByColumnName(String columnName) throws SQLException;
    
    /**
     * Find audit change logs by old value containing the specified string.
     * 
     * @param oldValuePattern The pattern to search for in old values
     * @return A list of audit change logs with old values containing the pattern
     * @throws SQLException if a database access error occurs
     */
    List<AuditChangeLog> findByOldValueContaining(String oldValuePattern) throws SQLException;
    
    /**
     * Find audit change logs by new value containing the specified string.
     * 
     * @param newValuePattern The pattern to search for in new values
     * @return A list of audit change logs with new values containing the pattern
     * @throws SQLException if a database access error occurs
     */
    List<AuditChangeLog> findByNewValueContaining(String newValuePattern) throws SQLException;
    
    /**
     * Find audit change logs by audit log ID and column name.
     * 
     * @param auditLogId The audit log ID
     * @param columnName The column name
     * @return A list of audit change logs for the audit log and column
     * @throws SQLException if a database access error occurs
     */
    List<AuditChangeLog> findByAuditLogIdAndColumnName(Integer auditLogId, String columnName) throws SQLException;
}