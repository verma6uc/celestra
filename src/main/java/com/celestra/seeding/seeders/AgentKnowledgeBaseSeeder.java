package com.celestra.seeding.seeders;

import com.celestra.dao.AgentKnowledgeBaseDao;
import com.celestra.dao.impl.AgentKnowledgeBaseDaoImpl;
import com.celestra.model.AgentKnowledgeBase;
import com.celestra.seeding.util.TimestampUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Seeder class for the agent_knowledge_bases table.
 * This class is responsible for generating and inserting test data for the junction table
 * that links agents to knowledge bases.
 */
public class AgentKnowledgeBaseSeeder {
    
    private static final Logger LOGGER = Logger.getLogger(AgentKnowledgeBaseSeeder.class.getName());
    
    private static final String GET_AGENTS_BY_COMPANY_SQL = 
            "SELECT id, company_id FROM agents";
    
    private static final String GET_KNOWLEDGE_BASES_BY_COMPANY_SQL = 
            "SELECT id, company_id FROM knowledge_bases";
    
    private final Connection connection;
    private final AgentKnowledgeBaseDao agentKnowledgeBaseDao;
    private final int numAgentKnowledgeBases;
    
    /**
     * Constructor for AgentKnowledgeBaseSeeder.
     * 
     * @param connection Database connection
     * @param numAgentKnowledgeBases Number of agent-knowledge base relationships to seed
     */
    public AgentKnowledgeBaseSeeder(Connection connection, int numAgentKnowledgeBases) {
        this.connection = connection;
        this.agentKnowledgeBaseDao = new AgentKnowledgeBaseDaoImpl();
        this.numAgentKnowledgeBases = numAgentKnowledgeBases;
    }
    
    /**
     * Seed the agent_knowledge_bases table with test data.
     * 
     * @return List of generated agent_knowledge_base IDs
     * @throws SQLException If a database error occurs
     */
    public List<Integer> seed() throws SQLException {
        LOGGER.info("Seeding agent_knowledge_bases table with " + numAgentKnowledgeBases + " records...");
        
        // Get agents grouped by company
        Map<Integer, List<Integer>> agentsByCompany = getAgentsByCompany();
        
        // Get knowledge bases grouped by company
        Map<Integer, List<Integer>> knowledgeBasesByCompany = getKnowledgeBasesByCompany();
        
        // Check if we have data to work with
        if (agentsByCompany.isEmpty() || knowledgeBasesByCompany.isEmpty()) {
            LOGGER.warning("No agents or knowledge bases found. Cannot seed agent_knowledge_bases.");
            return List.of();
        }
        
        List<Integer> agentKnowledgeBaseIds = new ArrayList<>();
        
        try {
            // Track existing relationships to avoid duplicates
            Map<String, Boolean> existingRelationships = new HashMap<>();
            
            // For each company, create relationships between its agents and knowledge bases
            for (Integer companyId : agentsByCompany.keySet()) {
                List<Integer> companyAgents = agentsByCompany.get(companyId);
                List<Integer> companyKnowledgeBases = knowledgeBasesByCompany.getOrDefault(companyId, List.of());
                
                if (companyAgents.isEmpty() || companyKnowledgeBases.isEmpty()) {
                    continue;
                }
                
                // For each agent, assign 1-4 knowledge bases
                for (Integer agentId : companyAgents) {
                    // Determine how many knowledge bases to assign to this agent
                    int numKnowledgeBasesToAssign = Math.min(
                            1 + (int) (Math.random() * 3), // 1-4 knowledge bases
                            companyKnowledgeBases.size());
                    
                    // Assign random knowledge bases to this agent
                    for (int i = 0; i < numKnowledgeBasesToAssign; i++) {
                        // Select a random knowledge base
                        Integer knowledgeBaseId = companyKnowledgeBases.get(
                                (int) (Math.random() * companyKnowledgeBases.size()));
                        
                        // Check if this relationship already exists
                        String relationshipKey = agentId + "-" + knowledgeBaseId;
                        if (existingRelationships.containsKey(relationshipKey)) {
                            continue;
                        }
                        
                        // Mark this relationship as existing
                        existingRelationships.put(relationshipKey, true);
                        
                        // Generate timestamp
                        Timestamp createdAt = TimestampUtil.getRandomTimestampInRange(-365, -1);
                        
                        // Create the agent knowledge base relationship
                        AgentKnowledgeBase agentKnowledgeBase = new AgentKnowledgeBase();
                        agentKnowledgeBase.setAgentId(agentId);
                        agentKnowledgeBase.setKnowledgeBaseId(knowledgeBaseId);
                        agentKnowledgeBase.setCreatedAt(createdAt);
                        
                        
                            // Save the agent knowledge base relationship
                            AgentKnowledgeBase created = agentKnowledgeBaseDao.create(agentKnowledgeBase);
                            if (created != null && created.getId() > 0) {
                                agentKnowledgeBaseIds.add(created.getId());
                                // If we've reached the target number, stop
                                if (agentKnowledgeBaseIds.size() >= numAgentKnowledgeBases) {
                                    break;
                                }
                            }
                        
                    }
                    
                    // If we've reached the target number, stop
                    if (agentKnowledgeBaseIds.size() >= numAgentKnowledgeBases) {
                        break;
                    }
                }
                
                // If we've reached the target number, stop
                if (agentKnowledgeBaseIds.size() >= numAgentKnowledgeBases) {
                    break;
                }
            }
            
            LOGGER.info("Successfully seeded " + agentKnowledgeBaseIds.size() + " agent-knowledge base relationships.");
            return agentKnowledgeBaseIds;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error seeding agent_knowledge_bases table", e);
            throw e;
        }
    }
    
    /**
     * Get agents grouped by company.
     * 
     * @return Map of company IDs to lists of agent IDs
     * @throws SQLException If a database error occurs
     */
    private Map<Integer, List<Integer>> getAgentsByCompany() throws SQLException {
        Map<Integer, List<Integer>> agentsByCompany = new HashMap<>();
        
        try (PreparedStatement statement = connection.prepareStatement(GET_AGENTS_BY_COMPANY_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                int agentId = resultSet.getInt("id");
                int companyId = resultSet.getInt("company_id");
                
                agentsByCompany.computeIfAbsent(companyId, k -> new ArrayList<>()).add(agentId);
            }
        }
        
        return agentsByCompany;
    }
    
    /**
     * Get knowledge bases grouped by company.
     * 
     * @return Map of company IDs to lists of knowledge base IDs
     * @throws SQLException If a database error occurs
     */
    private Map<Integer, List<Integer>> getKnowledgeBasesByCompany() throws SQLException {
        Map<Integer, List<Integer>> knowledgeBasesByCompany = new HashMap<>();
        
        try (PreparedStatement statement = connection.prepareStatement(GET_KNOWLEDGE_BASES_BY_COMPANY_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                int knowledgeBaseId = resultSet.getInt("id");
                int companyId = resultSet.getInt("company_id");
                
                knowledgeBasesByCompany.computeIfAbsent(companyId, k -> new ArrayList<>()).add(knowledgeBaseId);
            }
        }
        
        return knowledgeBasesByCompany;
    }
}