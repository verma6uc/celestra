package com.celestra.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.celestra.db.DatabaseUtil;

/**
 * Abstract base implementation of the BaseDao interface.
 * Provides common functionality for all DAO implementations.
 * 
 * @param <T> The entity type this DAO handles
 * @param <ID> The type of the entity's primary key
 */
public abstract class AbstractBaseDao<T, ID> implements BaseDao<T, ID> {
    
    /**
     * Get the name of the table associated with this DAO.
     * 
     * @return The table name
     */
    protected abstract String getTableName();
    
    /**
     * Get the name of the primary key column.
     * 
     * @return The primary key column name
     */
    protected abstract String getIdColumnName();
    
    /**
     * Create an entity from a ResultSet.
     * 
     * @param rs The ResultSet containing the entity data
     * @return The created entity
     * @throws SQLException if a database access error occurs
     */
    protected abstract T mapRow(ResultSet rs) throws SQLException;
    
    /**
     * Set parameters for an insert statement.
     * 
     * @param ps The PreparedStatement to set parameters for
     * @param entity The entity to insert
     * @throws SQLException if a database access error occurs
     */
    protected abstract void setInsertParameters(PreparedStatement ps, T entity) throws SQLException;
    
    /**
     * Set parameters for an update statement.
     * 
     * @param ps The PreparedStatement to set parameters for
     * @param entity The entity to update
     * @throws SQLException if a database access error occurs
     */
    protected abstract void setUpdateParameters(PreparedStatement ps, T entity) throws SQLException;
    
    /**
     * Get the SQL for creating an entity.
     * 
     * @return The SQL insert statement
     */
    protected abstract String getInsertSql();
    
    /**
     * Get the SQL for updating an entity.
     * 
     * @return The SQL update statement
     */
    protected abstract String getUpdateSql();
    
    /**
     * Get the SQL for finding all entities.
     * 
     * @return The SQL select statement
     */
    protected String getFindAllSql() {
        return "SELECT * FROM " + getTableName();
    }
    
    /**
     * Get the SQL for finding an entity by ID.
     * 
     * @return The SQL select statement
     */
    protected String getFindByIdSql() {
        return "SELECT * FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
    }
    
    /**
     * Get the SQL for deleting an entity.
     * 
     * @return The SQL delete statement
     */
    protected String getDeleteSql() {
        return "DELETE FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
    }
    
    @Override
    public Optional<T> findById(ID id) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getFindByIdSql())) {
            
            ps.setObject(1, id);
            
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
    public List<T> findAll() throws SQLException {
        List<T> entities = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getFindAllSql());
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                entities.add(mapRow(rs));
            }
        }
        
        return entities;
    }
    
    @Override
    public boolean delete(ID id) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getDeleteSql())) {
            
            ps.setObject(1, id);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Execute a query and map the results to a list of entities.
     * 
     * @param sql The SQL query to execute
     * @param paramSetter A functional interface to set parameters on the PreparedStatement
     * @return A list of entities
     * @throws SQLException if a database access error occurs
     */
    protected List<T> executeQuery(String sql, PreparedStatementSetter paramSetter) throws SQLException {
        List<T> entities = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (paramSetter != null) {
                paramSetter.setParameters(ps);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entities.add(mapRow(rs));
                }
            }
        }
        
        return entities;
    }
    
    /**
     * Execute a query and map the first result to an entity.
     * 
     * @param sql The SQL query to execute
     * @param paramSetter A functional interface to set parameters on the PreparedStatement
     * @return An Optional containing the entity if found, or empty if not found
     * @throws SQLException if a database access error occurs
     */
    protected Optional<T> executeQueryForObject(String sql, PreparedStatementSetter paramSetter) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (paramSetter != null) {
                paramSetter.setParameters(ps);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                } else {
                    return Optional.empty();
                }
            }
        }
    }
    
    /**
     * Execute an update statement.
     * 
     * @param sql The SQL update statement to execute
     * @param paramSetter A functional interface to set parameters on the PreparedStatement
     * @return The number of rows affected
     * @throws SQLException if a database access error occurs
     */
    protected int executeUpdate(String sql, PreparedStatementSetter paramSetter) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (paramSetter != null) {
                paramSetter.setParameters(ps);
            }
            
            return ps.executeUpdate();
        }
    }
    
    /**
     * Functional interface for setting parameters on a PreparedStatement.
     */
    @FunctionalInterface
    protected interface PreparedStatementSetter {
        /**
         * Set parameters on a PreparedStatement.
         * 
         * @param ps The PreparedStatement to set parameters on
         * @throws SQLException if a database access error occurs
         */
        void setParameters(PreparedStatement ps) throws SQLException;
    }
}