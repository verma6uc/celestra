package com.celestra.dao;

import java.sql.SQLException;
import java.util.List;

import com.celestra.enums.KnowledgeBaseStatus;
import com.celestra.model.KnowledgeBase;

/**
 * Data Access Object (DAO) interface for KnowledgeBase entities.
 */
public interface KnowledgeBaseDao extends BaseDao<KnowledgeBase, Integer> {
    
    /**
     * Find knowledge bases by company ID.
     * 
     * @param companyId The company ID to search for
     * @return A list of knowledge bases belonging to the specified company
     * @throws SQLException if a database access error occurs
     */
    List<KnowledgeBase> findByCompanyId(Integer companyId) throws SQLException;
    
    /**
     * Find knowledge bases by status.
     * 
     * @param status The knowledge base status to search for
     * @return A list of knowledge bases with the specified status
     * @throws SQLException if a database access error occurs
     */
    List<KnowledgeBase> findByStatus(KnowledgeBaseStatus status) throws SQLException;
    
    /**
     * Find knowledge bases by company ID and status.
     * 
     * @param companyId The company ID to search for
     * @param status The knowledge base status to search for
     * @return A list of knowledge bases belonging to the specified company with the specified status
     * @throws SQLException if a database access error occurs
     */
    List<KnowledgeBase> findByCompanyIdAndStatus(Integer companyId, KnowledgeBaseStatus status) throws SQLException;
    
    /**
     * Find knowledge bases by name (partial match).
     * 
     * @param name The knowledge base name to search for
     * @return A list of knowledge bases with names containing the specified string
     * @throws SQLException if a database access error occurs
     */
    List<KnowledgeBase> findByNameContaining(String name) throws SQLException;
    
    /**
     * Find knowledge bases by agent ID.
     * 
     * @param agentId The agent ID to search for
     * @return A list of knowledge bases associated with the specified agent
     * @throws SQLException if a database access error occurs
     */
    List<KnowledgeBase> findByAgentId(Integer agentId) throws SQLException;
    
    /**
     * Update the status of a knowledge base.
     * 
     * @param id The ID of the knowledge base to update
     * @param status The new status
     * @return true if the knowledge base was updated, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean updateStatus(Integer id, KnowledgeBaseStatus status) throws SQLException;
    
    /**
     * Find knowledge bases by company name.
     * 
     * @param companyName The name of the company
     * @return A list of knowledge bases belonging to the company with the specified name
     * @throws SQLException if a database access error occurs
     */
    List<KnowledgeBase> findByCompanyName(String companyName) throws SQLException;
    
    /**
     * Find knowledge bases by agent name.
     * 
     * @param agentName The name of the agent
     * @return A list of knowledge bases associated with the agent with the specified name
     * @throws SQLException if a database access error occurs
     */
    List<KnowledgeBase> findByAgentName(String agentName) throws SQLException;
    
    
}