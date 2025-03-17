package com.celestra.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.celestra.model.KnowledgeSource;

/**
 * Data Access Object interface for KnowledgeSource entities.
 */
public interface KnowledgeSourceDao extends BaseDao<KnowledgeSource, Integer> {
    
    /**
     * Find knowledge sources by knowledge base ID.
     * 
     * @param knowledgeBaseId The knowledge base ID
     * @return A list of knowledge sources for the knowledge base
     * @throws SQLException if a database access error occurs
     */
    List<KnowledgeSource> findByKnowledgeBaseId(Integer knowledgeBaseId) throws SQLException;
    
    /**
     * Find knowledge sources by knowledge type ID.
     * 
     * @param knowledgeTypeId The knowledge type ID
     * @return A list of knowledge sources of the specified type
     * @throws SQLException if a database access error occurs
     */
    List<KnowledgeSource> findByKnowledgeTypeId(Integer knowledgeTypeId) throws SQLException;
    
    /**
     * Find a knowledge source by name.
     * 
     * @param name The knowledge source name
     * @return The knowledge source with the specified name, or empty if not found
     * @throws SQLException if a database access error occurs
     */
    Optional<KnowledgeSource> findByName(String name) throws SQLException;
    
    /**
     * Find knowledge sources by name containing the specified string.
     * 
     * @param namePattern The pattern to search for in knowledge source names
     * @return A list of knowledge sources with names containing the pattern
     * @throws SQLException if a database access error occurs
     */
    List<KnowledgeSource> findByNameContaining(String namePattern) throws SQLException;
    
    /**
     * Find knowledge sources by knowledge base ID and knowledge type ID.
     * 
     * @param knowledgeBaseId The knowledge base ID
     * @param knowledgeTypeId The knowledge type ID
     * @return A list of knowledge sources for the knowledge base and of the specified type
     * @throws SQLException if a database access error occurs
     */
    List<KnowledgeSource> findByKnowledgeBaseIdAndKnowledgeTypeId(Integer knowledgeBaseId, Integer knowledgeTypeId) throws SQLException;
}