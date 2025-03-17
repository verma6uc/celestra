package com.celestra.dao;

import java.sql.SQLException;
import java.util.List;

import com.celestra.model.AgentKnowledgeBase;

/**
 * Data Access Object (DAO) interface for AgentKnowledgeBase entities.
 */
public interface AgentKnowledgeBaseDao extends BaseDao<AgentKnowledgeBase, Integer> {
    
    /**
     * Find agent-knowledge base associations by agent ID.
     * 
     * @param agentId The agent ID to search for
     * @return A list of agent-knowledge base associations for the specified agent
     * @throws SQLException if a database access error occurs
     */
    List<AgentKnowledgeBase> findByAgentId(Integer agentId) throws SQLException;
    
    /**
     * Find agent-knowledge base associations by knowledge base ID.
     * 
     * @param knowledgeBaseId The knowledge base ID to search for
     * @return A list of agent-knowledge base associations for the specified knowledge base
     * @throws SQLException if a database access error occurs
     */
    List<AgentKnowledgeBase> findByKnowledgeBaseId(Integer knowledgeBaseId) throws SQLException;
    
    /**
     * Check if an association exists between an agent and a knowledge base.
     * 
     * @param agentId The agent ID
     * @param knowledgeBaseId The knowledge base ID
     * @return true if an association exists, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean existsByAgentIdAndKnowledgeBaseId(Integer agentId, Integer knowledgeBaseId) throws SQLException;
    
    /**
     * Delete associations by agent ID.
     * 
     * @param agentId The agent ID
     * @return The number of associations deleted
     * @throws SQLException if a database access error occurs
     */
    int deleteByAgentId(Integer agentId) throws SQLException;
    
    /**
     * Delete associations by knowledge base ID.
     * 
     * @param knowledgeBaseId The knowledge base ID
     * @return The number of associations deleted
     * @throws SQLException if a database access error occurs
     */
    int deleteByKnowledgeBaseId(Integer knowledgeBaseId) throws SQLException;
    
    /**
     * Delete an association by agent ID and knowledge base ID.
     * 
     * @param agentId The agent ID
     * @param knowledgeBaseId The knowledge base ID
     * @return true if the association was deleted, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean deleteByAgentIdAndKnowledgeBaseId(Integer agentId, Integer knowledgeBaseId) throws SQLException;
}