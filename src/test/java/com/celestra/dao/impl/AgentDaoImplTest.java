package com.celestra.dao.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
     * Main method to run the tests.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        AgentDaoImplTest test = new AgentDaoImplTest();
        test.runTests();
    }
    
    /**
     * Constructor.
     */
    public AgentDaoImplTest() {
        agentDao = new AgentDaoImpl();
    }
    
    /**
     * Run all tests.
     */
    public void runTests() {
        try {
            setUp();
            
            testCreate();
            testFindById();
            testFindAll();
            testUpdate();
            testDelete();
            testFindByCompanyId();
            testFindByStatus();
            testFindByCompanyIdAndStatus();
            testFindByNameContaining();
            testUpdateStatus();
            testUpdateAgentProtocol();
            
            tearDown();
            
            System.out.println("All tests completed.");
        } catch (Exception e) {
            System.err.println("Error running tests: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // Agents table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // Insert test data
        executeSQL("INSERT INTO agents (company_id, name, description, agent_protocol, status, created_at, updated_at) " +
                   "VALUES (1, 'Test Agent 1', 'Test Description 1', '{\"type\":\"basic\"}', 'ACTIVE', NOW(), NOW())");
        
        executeSQL("INSERT INTO agents (company_id, name, description, agent_protocol, status, created_at, updated_at) " +
                   "VALUES (1, 'Test Agent 2', 'Test Description 2', '{\"type\":\"advanced\"}', 'DISABLED', NOW(), NOW())");
        
        executeSQL("INSERT INTO agents (company_id, name, description, agent_protocol, status, created_at, updated_at) " +
                   "VALUES (2, 'Another Agent', 'Another Description', '{\"type\":\"custom\"}', 'ACTIVE', NOW(), NOW())");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM agents WHERE name LIKE 'Test Agent%' OR name = 'Another Agent'");
    }
    
    /**
     * Test the create method.
     */
    private void testCreate() {
        try {
            // Create a new agent
            Agent agent = new Agent();
            agent.setCompanyId(1);
            agent.setName("Test Agent Create");
            agent.setDescription("Test Description Create");
            agent.setAgentProtocol("{\"type\":\"test\"}");
            agent.setStatus(AgentStatus.ACTIVE);
            
            Agent createdAgent = agentDao.create(agent);
            
            // Verify the agent was created
            boolean success = createdAgent.getId() > 0;
            printTestResult("testCreate", success);
            
            // Clean up
            if (success) {
                agentDao.delete(createdAgent.getId());
            }
        } catch (Exception e) {
            printTestFailure("testCreate", e);
        }
    }
    
    /**
     * Test the findById method.
     */
    private void testFindById() {
        try {
            // Find all agents
            List<Agent> agents = agentDao.findAll();
            
            // Verify there are agents
            if (agents.isEmpty()) {
                printTestResult("testFindById", false, "No agents found");
                return;
            }
            
            // Get the first agent
            Agent agent = agents.get(0);
            
            // Find the agent by ID
            Optional<Agent> foundAgent = agentDao.findById(agent.getId());
            
            // Verify the agent was found
            boolean success = foundAgent.isPresent() && 
                              foundAgent.get().getId().equals(agent.getId()) &&
                              foundAgent.get().getName().equals(agent.getName());
            
            printTestResult("testFindById", success);
        } catch (Exception e) {
            printTestFailure("testFindById", e);
        }
    }
    
    /**
     * Test the findAll method.
     */
    private void testFindAll() {
        try {
            // Find all agents
            List<Agent> agents = agentDao.findAll();
            
            // Verify there are agents
            boolean success = !agents.isEmpty();
            printTestResult("testFindAll", success, "Found " + agents.size() + " agents");
        } catch (Exception e) {
            printTestFailure("testFindAll", e);
        }
    }
    
    /**
     * Test the update method.
     */
    private void testUpdate() {
        try {
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
            boolean success = updatedAgent.getName().equals("Test Agent Updated") &&
                              updatedAgent.getDescription().equals("Test Description Updated");
            
            printTestResult("testUpdate", success);
            
            // Clean up
            agentDao.delete(createdAgent.getId());
        } catch (Exception e) {
            printTestFailure("testUpdate", e);
        }
    }
    
    /**
     * Test the delete method.
     */
    private void testDelete() {
        try {
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
            boolean success = deleted && !agentDao.findById(createdAgent.getId()).isPresent();
            
            printTestResult("testDelete", success);
        } catch (Exception e) {
            printTestFailure("testDelete", e);
        }
    }
    
    /**
     * Test the findByCompanyId method.
     */
    private void testFindByCompanyId() {
        try {
            // Find agents by company ID
            List<Agent> agents = agentDao.findByCompanyId(1);
            
            // Verify there are agents
            boolean success = !agents.isEmpty();
            printTestResult("testFindByCompanyId", success, "Found " + agents.size() + " agents for company ID 1");
        } catch (Exception e) {
            printTestFailure("testFindByCompanyId", e);
        }
    }
    
    /**
     * Test the findByStatus method.
     */
    private void testFindByStatus() {
        try {
            // Find agents by status
            List<Agent> agents = agentDao.findByStatus(AgentStatus.ACTIVE);
            
            // Verify there are agents
            boolean success = !agents.isEmpty();
            printTestResult("testFindByStatus", success, "Found " + agents.size() + " active agents");
        } catch (Exception e) {
            printTestFailure("testFindByStatus", e);
        }
    }
    
    /**
     * Test the findByCompanyIdAndStatus method.
     */
    private void testFindByCompanyIdAndStatus() {
        try {
            // Find agents by company ID and status
            List<Agent> agents = agentDao.findByCompanyIdAndStatus(1, AgentStatus.ACTIVE);
            
            // Verify there are agents
            boolean success = !agents.isEmpty();
            printTestResult("testFindByCompanyIdAndStatus", success, 
                    "Found " + agents.size() + " active agents for company ID 1");
        } catch (Exception e) {
            printTestFailure("testFindByCompanyIdAndStatus", e);
        }
    }
    
    /**
     * Test the findByNameContaining method.
     */
    private void testFindByNameContaining() {
        try {
            // Find agents by name
            List<Agent> agents = agentDao.findByNameContaining("Test");
            
            // Verify there are agents
            boolean success = !agents.isEmpty();
            printTestResult("testFindByNameContaining", success, 
                    "Found " + agents.size() + " agents with 'Test' in the name");
        } catch (Exception e) {
            printTestFailure("testFindByNameContaining", e);
        }
    }
    
    /**
     * Test the updateStatus method.
     */
    private void testUpdateStatus() {
        try {
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
            Optional<Agent> updatedAgent = agentDao.findById(createdAgent.getId());
            boolean success = updated && 
                              updatedAgent.isPresent() && 
                              updatedAgent.get().getStatus() == AgentStatus.DISABLED;
            
            printTestResult("testUpdateStatus", success);
            
            // Clean up
            agentDao.delete(createdAgent.getId());
        } catch (Exception e) {
            printTestFailure("testUpdateStatus", e);
        }
    }
    
    /**
     * Test the updateAgentProtocol method.
     */
    private void testUpdateAgentProtocol() {
        try {
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
            Optional<Agent> updatedAgent = agentDao.findById(createdAgent.getId());
            boolean success = updated && 
                              updatedAgent.isPresent() && 
                              updatedAgent.get().getAgentProtocol().equals(newProtocol);
            
            printTestResult("testUpdateAgentProtocol", success);
            
            // Clean up
            agentDao.delete(createdAgent.getId());
        } catch (Exception e) {
            printTestFailure("testUpdateAgentProtocol", e);
        }
    }
}