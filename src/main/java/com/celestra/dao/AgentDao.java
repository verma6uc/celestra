package com.celestra.dao;

import java.sql.SQLException;
import java.util.List;

import com.celestra.enums.AgentStatus;
import com.celestra.model.Agent;

/**
 * Data Access Object (DAO) interface for Agent entities.
 */
public interface AgentDao extends BaseDao<Agent, Integer> {
    
    /**
     * Find agents by company ID.
     * 
     * @param companyId The company ID to search for
     * @return A list of agents belonging to the specified company
     * @throws SQLException if a database access error occurs
     */
    List<Agent> findByCompanyId(Integer companyId) throws SQLException;
    
    /**
     * Find agents by status.
     * 
     * @param status The agent status to search for
     * @return A list of agents with the specified status
     * @throws SQLException if a database access error occurs
     */
    List<Agent> findByStatus(AgentStatus status) throws SQLException;
    
    /**
     * Find agents by company ID and status.
     * 
     * @param companyId The company ID to search for
     * @param status The agent status to search for
     * @return A list of agents belonging to the specified company with the specified status
     * @throws SQLException if a database access error occurs
     */
    List<Agent> findByCompanyIdAndStatus(Integer companyId, AgentStatus status) throws SQLException;
    
    /**
     * Find agents by name (partial match).
     * 
     * @param name The agent name to search for
     * @return A list of agents with names containing the specified string
     * @throws SQLException if a database access error occurs
     */
    List<Agent> findByNameContaining(String name) throws SQLException;
    
    /**
     * Update the status of an agent.
     * 
     * @param id The ID of the agent to update
     * @param status The new status
     * @return true if the agent was updated, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean updateStatus(Integer id, AgentStatus status) throws SQLException;
    
    /**
     * Update the agent protocol of an agent.
     * 
     * @param id The ID of the agent to update
     * @param agentProtocol The new agent protocol
     * @return true if the agent was updated, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean updateAgentProtocol(Integer id, String agentProtocol) throws SQLException;
    
    /**
     * Find agents by company name.
     * 
     * @param companyName The name of the company
     * @return A list of agents belonging to the company with the specified name
     * @throws SQLException if a database access error occurs
     */
    List<Agent> findByCompanyName(String companyName) throws SQLException;
}