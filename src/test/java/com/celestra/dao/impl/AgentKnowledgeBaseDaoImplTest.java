package com.celestra.dao.impl;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.celestra.dao.AgentKnowledgeBaseDao;
import com.celestra.dao.BaseDaoTest;
import com.celestra.model.AgentKnowledgeBase;

/**
 * Test class for AgentKnowledgeBaseDaoImpl.
 */
public class AgentKnowledgeBaseDaoImplTest extends BaseDaoTest {
    
    private AgentKnowledgeBaseDao agentKnowledgeBaseDao;
    
    /**
     * Initialize the DAO before each test.
     */
    @Before
    public void initialize() {
        agentKnowledgeBaseDao = new AgentKnowledgeBaseDaoImpl();
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // Agent knowledge bases table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // Insert test data
        executeSQL("INSERT INTO agent_knowledge_bases (agent_id, knowledge_base_id, created_at) " +
                   "VALUES (1, 1, NOW())");
        
        executeSQL("INSERT INTO agent_knowledge_bases (agent_id, knowledge_base_id, created_at) " +
                   "VALUES (1, 2, NOW())");
        
        executeSQL("INSERT INTO agent_knowledge_bases (agent_id, knowledge_base_id, created_at) " +
                   "VALUES (2, 1, NOW())");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM agent_knowledge_bases WHERE agent_id IN (1, 2, 3) AND knowledge_base_id IN (1, 2, 3)");
    }
    
    /**
     * Test the create method.
     */
    @Test
    public void testCreate() throws SQLException {
        // Create a new agent-knowledge base association
        AgentKnowledgeBase agentKnowledgeBase = new AgentKnowledgeBase();
        agentKnowledgeBase.setAgentId(3);
        agentKnowledgeBase.setKnowledgeBaseId(3);
        
        AgentKnowledgeBase createdAgentKnowledgeBase = agentKnowledgeBaseDao.create(agentKnowledgeBase);
        
        // Verify the agent-knowledge base association was created
        assertNotNull("Created association should not be null", createdAgentKnowledgeBase);
        assertTrue("Created association should have an ID", createdAgentKnowledgeBase.getId() > 0);
        
        // Clean up
        boolean deleted = agentKnowledgeBaseDao.delete(createdAgentKnowledgeBase.getId());
        assertTrue("Association should be deleted successfully", deleted);
    }
    
    /**
     * Test the findById method.
     */
    @Test
    public void testFindById() throws SQLException {
        // Find all agent-knowledge base associations
        List<AgentKnowledgeBase> agentKnowledgeBases = agentKnowledgeBaseDao.findAll();
        
        // Verify there are agent-knowledge base associations
        assertFalse("There should be associations in the database", agentKnowledgeBases.isEmpty());
        
        // Get the first agent-knowledge base association
        AgentKnowledgeBase agentKnowledgeBase = agentKnowledgeBases.get(0);
        
        // Find the agent-knowledge base association by ID
        Optional<AgentKnowledgeBase> foundAgentKnowledgeBase = 
                agentKnowledgeBaseDao.findById(agentKnowledgeBase.getId());
        
        // Verify the agent-knowledge base association was found
        assertTrue("Association should be found by ID", foundAgentKnowledgeBase.isPresent());
        assertEquals("Found association ID should match", agentKnowledgeBase.getId(), foundAgentKnowledgeBase.get().getId());
        assertEquals("Found association agent ID should match", agentKnowledgeBase.getAgentId(), foundAgentKnowledgeBase.get().getAgentId());
        assertEquals("Found association knowledge base ID should match", agentKnowledgeBase.getKnowledgeBaseId(), foundAgentKnowledgeBase.get().getKnowledgeBaseId());
    }
    
    /**
     * Test the findAll method.
     */
    @Test
    public void testFindAll() throws SQLException {
        // Find all agent-knowledge base associations
        List<AgentKnowledgeBase> agentKnowledgeBases = agentKnowledgeBaseDao.findAll();
        
        // Verify there are agent-knowledge base associations
        assertFalse("There should be associations in the database", agentKnowledgeBases.isEmpty());
        assertTrue("There should be at least 3 associations", agentKnowledgeBases.size() >= 3);
    }
    
    /**
     * Test the update method.
     */
    @Test
    public void testUpdate() throws SQLException {
        // Create a new agent-knowledge base association
        AgentKnowledgeBase agentKnowledgeBase = new AgentKnowledgeBase();
        agentKnowledgeBase.setAgentId(3);
        agentKnowledgeBase.setKnowledgeBaseId(3);
        
        AgentKnowledgeBase createdAgentKnowledgeBase = agentKnowledgeBaseDao.create(agentKnowledgeBase);
        
        // Update the agent-knowledge base association
        createdAgentKnowledgeBase.setAgentId(2);
        createdAgentKnowledgeBase.setKnowledgeBaseId(2);
        
        AgentKnowledgeBase updatedAgentKnowledgeBase = agentKnowledgeBaseDao.update(createdAgentKnowledgeBase);
        
        // Verify the agent-knowledge base association was updated
        assertEquals("Association agent ID should be updated", Integer.valueOf(2), updatedAgentKnowledgeBase.getAgentId());
        assertEquals("Association knowledge base ID should be updated", Integer.valueOf(2), updatedAgentKnowledgeBase.getKnowledgeBaseId());
        
        // Clean up
        boolean deleted = agentKnowledgeBaseDao.delete(createdAgentKnowledgeBase.getId());
        assertTrue("Association should be deleted successfully", deleted);
    }
    
    /**
     * Test the delete method.
     */
    @Test
    public void testDelete() throws SQLException {
        // Create a new agent-knowledge base association
        AgentKnowledgeBase agentKnowledgeBase = new AgentKnowledgeBase();
        agentKnowledgeBase.setAgentId(3);
        agentKnowledgeBase.setKnowledgeBaseId(3);
        
        AgentKnowledgeBase createdAgentKnowledgeBase = agentKnowledgeBaseDao.create(agentKnowledgeBase);
        
        // Delete the agent-knowledge base association
        boolean deleted = agentKnowledgeBaseDao.delete(createdAgentKnowledgeBase.getId());
        
        // Verify the agent-knowledge base association was deleted
        assertTrue("Association should be deleted successfully", deleted);
        
        Optional<AgentKnowledgeBase> foundAgentKnowledgeBase = agentKnowledgeBaseDao.findById(createdAgentKnowledgeBase.getId());
        assertFalse("Association should not be found after deletion", foundAgentKnowledgeBase.isPresent());
    }
    
    /**
     * Test the findByAgentId method.
     */
    @Test
    public void testFindByAgentId() throws SQLException {
        // Find agent-knowledge base associations by agent ID
        List<AgentKnowledgeBase> agentKnowledgeBases = agentKnowledgeBaseDao.findByAgentId(1);
        
        // Verify there are agent-knowledge base associations
        assertFalse("There should be associations for agent ID 1", agentKnowledgeBases.isEmpty());
        
        // Verify all associations have the correct agent ID
        for (AgentKnowledgeBase agentKnowledgeBase : agentKnowledgeBases) {
            assertEquals("Association agent ID should be 1", Integer.valueOf(1), agentKnowledgeBase.getAgentId());
        }
    }
    
    /**
     * Test the findByKnowledgeBaseId method.
     */
    @Test
    public void testFindByKnowledgeBaseId() throws SQLException {
        // Find agent-knowledge base associations by knowledge base ID
        List<AgentKnowledgeBase> agentKnowledgeBases = agentKnowledgeBaseDao.findByKnowledgeBaseId(1);
        
        // Verify there are agent-knowledge base associations
        assertFalse("There should be associations for knowledge base ID 1", agentKnowledgeBases.isEmpty());
        
        // Verify all associations have the correct knowledge base ID
        for (AgentKnowledgeBase agentKnowledgeBase : agentKnowledgeBases) {
            assertEquals("Association knowledge base ID should be 1", Integer.valueOf(1), agentKnowledgeBase.getKnowledgeBaseId());
        }
    }
    
    /**
     * Test the existsByAgentIdAndKnowledgeBaseId method.
     */
    @Test
    public void testExistsByAgentIdAndKnowledgeBaseId() throws SQLException {
        // Check if an association exists
        boolean exists = agentKnowledgeBaseDao.existsByAgentIdAndKnowledgeBaseId(1, 1);
        
        // Verify the association exists
        assertTrue("Association between agent ID 1 and knowledge base ID 1 should exist", exists);
        
        // Check if a non-existent association exists
        boolean notExists = agentKnowledgeBaseDao.existsByAgentIdAndKnowledgeBaseId(999, 999);
        
        // Verify the association does not exist
        assertFalse("Association between agent ID 999 and knowledge base ID 999 should not exist", notExists);
    }
    
    /**
     * Test the deleteByAgentId method.
     */
    @Test
    public void testDeleteByAgentId() throws SQLException {
        // Create a new agent-knowledge base association
        AgentKnowledgeBase agentKnowledgeBase = new AgentKnowledgeBase();
        agentKnowledgeBase.setAgentId(3);
        agentKnowledgeBase.setKnowledgeBaseId(3);
        
        AgentKnowledgeBase createdAgentKnowledgeBase = agentKnowledgeBaseDao.create(agentKnowledgeBase);
        
        // Delete associations by agent ID
        int deleted = agentKnowledgeBaseDao.deleteByAgentId(3);
        
        // Verify the associations were deleted
        assertTrue("At least one association should be deleted", deleted > 0);
        
        Optional<AgentKnowledgeBase> foundAgentKnowledgeBase = agentKnowledgeBaseDao.findById(createdAgentKnowledgeBase.getId());
        assertFalse("Association should not be found after deletion", foundAgentKnowledgeBase.isPresent());
    }
    
    /**
     * Test the deleteByKnowledgeBaseId method.
     */
    @Test
    public void testDeleteByKnowledgeBaseId() throws SQLException {
        // Create a new agent-knowledge base association
        AgentKnowledgeBase agentKnowledgeBase = new AgentKnowledgeBase();
        agentKnowledgeBase.setAgentId(3);
        agentKnowledgeBase.setKnowledgeBaseId(3);
        
        AgentKnowledgeBase createdAgentKnowledgeBase = agentKnowledgeBaseDao.create(agentKnowledgeBase);
        
        // Delete associations by knowledge base ID
        int deleted = agentKnowledgeBaseDao.deleteByKnowledgeBaseId(3);
        
        // Verify the associations were deleted
        assertTrue("At least one association should be deleted", deleted > 0);
        
        Optional<AgentKnowledgeBase> foundAgentKnowledgeBase = agentKnowledgeBaseDao.findById(createdAgentKnowledgeBase.getId());
        assertFalse("Association should not be found after deletion", foundAgentKnowledgeBase.isPresent());
    }
    
    /**
     * Test the deleteByAgentIdAndKnowledgeBaseId method.
     */
    @Test
    public void testDeleteByAgentIdAndKnowledgeBaseId() throws SQLException {
        // Create a new agent-knowledge base association
        AgentKnowledgeBase agentKnowledgeBase = new AgentKnowledgeBase();
        agentKnowledgeBase.setAgentId(3);
        agentKnowledgeBase.setKnowledgeBaseId(3);
        
        AgentKnowledgeBase createdAgentKnowledgeBase = agentKnowledgeBaseDao.create(agentKnowledgeBase);
        
        // Delete the association by agent ID and knowledge base ID
        boolean deleted = agentKnowledgeBaseDao.deleteByAgentIdAndKnowledgeBaseId(3, 3);
        
        // Verify the association was deleted
        assertTrue("Association should be deleted successfully", deleted);
        
        Optional<AgentKnowledgeBase> foundAgentKnowledgeBase = agentKnowledgeBaseDao.findById(createdAgentKnowledgeBase.getId());
        assertFalse("Association should not be found after deletion", foundAgentKnowledgeBase.isPresent());
    }
}