package com.celestra.dao.impl;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.celestra.dao.AgentDao;
import com.celestra.dao.BaseDaoTest;
import com.celestra.enums.AgentStatus;
import com.celestra.model.Agent;

/**
 * Test class for AgentDaoImpl.
 */
public class AgentDaoImplTest extends BaseDaoTest {
    
    private AgentDao agentDao;
    
    /**
     * Initialize the DAO before each test.
     */
    @Before
    public void initialize() {
        agentDao = new AgentDaoImpl();
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // Agents table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // First insert test companies to satisfy foreign key constraints
        executeSQL("INSERT INTO companies (id, name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES (1, 'Test Company 1', 'Test Company Description 1', 'SMALL'::company_size, 'TECH'::company_vertical, 'ACTIVE'::company_status, NOW(), NOW())");
        
        executeSQL("INSERT INTO companies (id, name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES (2, 'Test Company 2', 'Test Company Description 2', 'MEDIUM'::company_size, 'PHARMACEUTICAL'::company_vertical, 'ACTIVE'::company_status, NOW(), NOW())");
        
        // Then insert test agents
        executeSQL("INSERT INTO agents (company_id, name, description, agent_protocol, status, created_at, updated_at) " +
                   "VALUES (1, 'Test Agent 1', 'Test Description 1', '{\"type\":\"basic\"}', 'ACTIVE'::agent_status, NOW(), NOW())");
        
        executeSQL("INSERT INTO agents (company_id, name, description, agent_protocol, status, created_at, updated_at) " +
                   "VALUES (1, 'Test Agent 2', 'Test Description 2', '{\"type\":\"advanced\"}', 'DISABLED'::agent_status, NOW(), NOW())");
        
        executeSQL("INSERT INTO agents (company_id, name, description, agent_protocol, status, created_at, updated_at) " +
                   "VALUES (2, 'Another Agent', 'Another Description', '{\"type\":\"custom\"}', 'ACTIVE'::agent_status, NOW(), NOW())");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        // First delete agents (child records)
        executeSQL("DELETE FROM agents WHERE name LIKE 'Test Agent%' OR name = 'Another Agent'");
        
        // Then delete companies (parent records)
        executeSQL("DELETE FROM companies WHERE id IN (1, 2)");
    }
    
    /**
     * Test the create method.
     */
    @Test
    public void testCreate() throws SQLException {
        // Create a new agent
        Agent agent = new Agent();
        agent.setCompanyId(1);
        agent.setName("Test Agent Create");
        agent.setDescription("Test Description Create");
        agent.setAgentProtocol("{\"type\":\"test\"}");
        agent.setStatus(AgentStatus.ACTIVE);
        
        Agent createdAgent = agentDao.create(agent);
        
        // Verify the agent was created
        assertNotNull("Created agent should not be null", createdAgent);
        assertTrue("Created agent should have an ID", createdAgent.getId() > 0);
        
        // Clean up
        boolean deleted = agentDao.delete(createdAgent.getId());
        assertTrue("Agent should be deleted successfully", deleted);
    }
    
    /**
     * Test the findById method.
     */
    @Test
    public void testFindById() throws SQLException {
        // Find all agents
        List<Agent> agents = agentDao.findAll();
        
        // Verify there are agents
        assertFalse("There should be agents in the database", agents.isEmpty());
        
        // Get the first agent
        Agent agent = agents.get(0);
        
        // Find the agent by ID
        Optional<Agent> foundAgent = agentDao.findById(agent.getId());
        
        // Verify the agent was found
        assertTrue("Agent should be found by ID", foundAgent.isPresent());
        assertEquals("Found agent ID should match", agent.getId(), foundAgent.get().getId());
        assertEquals("Found agent name should match", agent.getName(), foundAgent.get().getName());
    }
    
    /**
     * Test the findAll method.
     */
    @Test
    public void testFindAll() throws SQLException {
        // Find all agents
        List<Agent> agents = agentDao.findAll();
        
        // Verify there are agents
        assertFalse("There should be agents in the database", agents.isEmpty());
        assertTrue("There should be at least 3 agents", agents.size() >= 3);
    }
    
    /**
     * Test the update method.
     */
    @Test
    public void testUpdate() throws SQLException {
        // Create a new agent
        Agent agent = new Agent();
        agent.setCompanyId(1);
        agent.setName("Test Agent Update");
        agent.setDescription("Test Description Update");
        agent.setAgentProtocol("{\"type\":\"test\"}");
        agent.setStatus(AgentStatus.ACTIVE);
        
        Agent createdAgent = agentDao.create(agent);
        
        // Update the agent
        createdAgent.setName("Test Agent Updated");
        createdAgent.setDescription("Test Description Updated");
        
        Agent updatedAgent = agentDao.update(createdAgent);
        
        // Verify the agent was updated
        assertEquals("Agent name should be updated", "Test Agent Updated", updatedAgent.getName());
        assertEquals("Agent description should be updated", "Test Description Updated", updatedAgent.getDescription());
        
        // Clean up
        boolean deleted = agentDao.delete(createdAgent.getId());
        assertTrue("Agent should be deleted successfully", deleted);
    }
    
    /**
     * Test the delete method.
     */
    @Test
    public void testDelete() throws SQLException {
        // Create a new agent
        Agent agent = new Agent();
        agent.setCompanyId(1);
        agent.setName("Test Agent Delete");
        agent.setDescription("Test Description Delete");
        agent.setAgentProtocol("{\"type\":\"test\"}");
        agent.setStatus(AgentStatus.ACTIVE);
        
        Agent createdAgent = agentDao.create(agent);
        
        // Delete the agent
        boolean deleted = agentDao.delete(createdAgent.getId());
        
        // Verify the agent was deleted
        assertTrue("Agent should be deleted successfully", deleted);
        
        Optional<Agent> foundAgent = agentDao.findById(createdAgent.getId());
        assertFalse("Agent should not be found after deletion", foundAgent.isPresent());
    }
    
    /**
     * Test the findByCompanyId method.
     */
    @Test
    public void testFindByCompanyId() throws SQLException {
        // Find agents by company ID
        List<Agent> agents = agentDao.findByCompanyId(1);
        
        // Verify there are agents
        assertFalse("There should be agents for company ID 1", agents.isEmpty());
        
        // Verify all agents have the correct company ID
        for (Agent agent : agents) {
            assertEquals("Agent company ID should be 1", Integer.valueOf(1), agent.getCompanyId());
        }
    }
    
    /**
     * Test the findByStatus method.
     */
    @Test
    public void testFindByStatus() throws SQLException {
        // Find agents by status
        List<Agent> agents = agentDao.findByStatus(AgentStatus.ACTIVE);
        
        // Verify there are agents
        assertFalse("There should be active agents", agents.isEmpty());
        
        // Verify all agents have the correct status
        for (Agent agent : agents) {
            assertEquals("Agent status should be ACTIVE", AgentStatus.ACTIVE, agent.getStatus());
        }
    }
    
    /**
     * Test the findByCompanyIdAndStatus method.
     */
    @Test
    public void testFindByCompanyIdAndStatus() throws SQLException {
        // Find agents by company ID and status
        List<Agent> agents = agentDao.findByCompanyIdAndStatus(1, AgentStatus.ACTIVE);
        
        // Verify there are agents
        assertFalse("There should be active agents for company ID 1", agents.isEmpty());
        
        // Verify all agents have the correct company ID and status
        for (Agent agent : agents) {
            assertEquals("Agent company ID should be 1", Integer.valueOf(1), agent.getCompanyId());
            assertEquals("Agent status should be ACTIVE", AgentStatus.ACTIVE, agent.getStatus());
        }
    }
    
    /**
     * Test the findByNameContaining method.
     */
    @Test
    public void testFindByNameContaining() throws SQLException {
        // Find agents by name
        List<Agent> agents = agentDao.findByNameContaining("Test");
        
        // Verify there are agents
        assertFalse("There should be agents with 'Test' in the name", agents.isEmpty());
        
        // Verify all agents have the correct name pattern
        for (Agent agent : agents) {
            assertTrue("Agent name should contain 'Test'", agent.getName().contains("Test"));
        }
    }
    
    /**
     * Test the updateStatus method.
     */
    @Test
    public void testUpdateStatus() throws SQLException {
        // Create a new agent
        Agent agent = new Agent();
        agent.setCompanyId(1);
        agent.setName("Test Agent Status");
        agent.setDescription("Test Description Status");
        agent.setAgentProtocol("{\"type\":\"test\"}");
        agent.setStatus(AgentStatus.ACTIVE);
        
        Agent createdAgent = agentDao.create(agent);
        
        // Update the agent status
        boolean updated = agentDao.updateStatus(createdAgent.getId(), AgentStatus.DISABLED);
        
        // Verify the agent status was updated
        assertTrue("Agent status should be updated successfully", updated);
        
        Optional<Agent> updatedAgent = agentDao.findById(createdAgent.getId());
        assertTrue("Agent should be found after status update", updatedAgent.isPresent());
        assertEquals("Agent status should be DISABLED", AgentStatus.DISABLED, updatedAgent.get().getStatus());
        
        // Clean up
        boolean deleted = agentDao.delete(createdAgent.getId());
        assertTrue("Agent should be deleted successfully", deleted);
    }
    
    /**
     * Test the updateAgentProtocol method.
     */
    @Test
    public void testUpdateAgentProtocol() throws SQLException {
        // Create a new agent
        Agent agent = new Agent();
        agent.setCompanyId(1);
        agent.setName("Test Agent Protocol");
        agent.setDescription("Test Description Protocol");
        agent.setAgentProtocol("{\"type\":\"old\"}");
        agent.setStatus(AgentStatus.ACTIVE);
        
        Agent createdAgent = agentDao.create(agent);
        
        // Update the agent protocol
        String newProtocol = "{\"type\":\"new\",\"version\":2}";
        boolean updated = agentDao.updateAgentProtocol(createdAgent.getId(), newProtocol);
        
        // Verify the agent protocol was updated
        assertTrue("Agent protocol should be updated successfully", updated);
        
        Optional<Agent> updatedAgent = agentDao.findById(createdAgent.getId());
        assertTrue("Agent should be found after protocol update", updatedAgent.isPresent());
        assertEquals("Agent protocol should be updated", newProtocol, updatedAgent.get().getAgentProtocol());
        
        // Clean up
        boolean deleted = agentDao.delete(createdAgent.getId());
        assertTrue("Agent should be deleted successfully", deleted);
    }
}