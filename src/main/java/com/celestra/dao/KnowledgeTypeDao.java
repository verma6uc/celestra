package com.celestra.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.celestra.model.KnowledgeType;

/**
 * Data Access Object interface for KnowledgeType entities.
 */
public interface KnowledgeTypeDao extends BaseDao<KnowledgeType, Integer> {
    
    /**
     * Find a knowledge type by name.
     * 
     * @param name The knowledge type name
     * @return The knowledge type with the specified name, or empty if not found
     * @throws SQLException if a database access error occurs
     */
    Optional<KnowledgeType> findByName(String name) throws SQLException;
    
    /**
     * Find knowledge types by name containing the specified string.
     * 
     * @param namePattern The pattern to search for in knowledge type names
     * @return A list of knowledge types with names containing the pattern
     * @throws SQLException if a database access error occurs
     */
    List<KnowledgeType> findByNameContaining(String namePattern) throws SQLException;
}