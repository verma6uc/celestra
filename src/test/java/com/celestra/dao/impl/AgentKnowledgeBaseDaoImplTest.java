package com.celestra.dao.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.celestra.dao.AgentKnowledgeBaseDao;
import com.celestra.dao.BaseDaoTest;
import com.celestra.model.AgentKnowledgeBase;

/**
 * Test class for AgentKnowledgeBaseDaoImpl.
 */
public class AgentKnowledgeBaseDaoImplTest extends BaseDaoTest {
    
    private AgentKnowledgeBaseDao agentKnowledgeBaseDao;
    
    /**
     * Main method to run the tests.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        AgentKnowledgeBaseDaoImplTest test = new AgentKnowledgeBaseDaoImplTest();
        test.runTests();
    }
    
    /**
     * Constructor.
     */
    public AgentKnowledgeBaseDaoImplTest() {
        agentKnowledgeBaseDao = new AgentKnowledgeBaseDaoImpl();
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
            testFindByAgentId();
            testFindByKnowledgeBaseId();
            testExistsByAgentIdAndKnowledgeBaseId();
            testDeleteByAgentId();
            testDeleteByKnowledgeBaseId();
            testDeleteByAgentIdAndKnowledgeBaseId();
            
            tearDown();
            
            System.out.println("All tests completed.");
        } catch (Exception e) {
            System.err.println("Error running tests: " + e.getMessage());
            e.printStackTrace();
        }
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
    private void testCreate() {
        try {
            // Create a new agent-knowledge base association
            AgentKnowledgeBase agentKnowledgeBase = new AgentKnowledgeBase();
            agentKnowledgeBase.setAgentId(3);
            agentKnowledgeBase.setKnowledgeBaseId(3);
            
            AgentKnowledgeBase createdAgentKnowledgeBase = agentKnowledgeBaseDao.create(agentKnowledgeBase);
            
            // Verify the agent-knowledge base association was created
            boolean success = createdAgentKnowledgeBase.getId() > 0;
            printTestResult("testCreate", success);
            
            // Clean up
            if (success) {
                agentKnowledgeBaseDao.delete(createdAgentKnowledgeBase.getId());
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
            // Find all agent-knowledge base associations
            List<AgentKnowledgeBase> agentKnowledgeBases = agentKnowledgeBaseDao.findAll();
            
            // Verify there are agent-knowledge base associations
            if (agentKnowledgeBases.isEmpty()) {
                printTestResult("testFindById", false, "No agent-knowledge base associations found");
                return;
            }
            
            // Get the first agent-knowledge base association
            AgentKnowledgeBase agentKnowledgeBase = agentKnowledgeBases.get(0);
            
            // Find the agent-knowledge base association by ID
            Optional<AgentKnowledgeBase> foundAgentKnowledgeBase = 
                    agentKnowledgeBaseDao.findById(agentKnowledgeBase.getId());
            
            // Verify the agent-knowledge base association was found
            boolean success = foundAgentKnowledgeBase.isPresent() && 
                              foundAgentKnowledgeBase.get().getId().equals(agentKnowledgeBase.getId()) &&
                              foundAgentKnowledgeBase.get().getAgentId().equals(agentKnowledgeBase.getAgentId()) &&
                              foundAgentKnowledgeBase.get().getKnowledgeBaseId().equals(agentKnowledgeBase.getKnowledgeBaseId());
            
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
            // Find all agent-knowledge base associations
            List<AgentKnowledgeBase> agentKnowledgeBases = agentKnowledgeBaseDao.findAll();
            
            // Verify there are agent-knowledge base associations
            boolean success = !agentKnowledgeBases.isEmpty();
            printTestResult("testFindAll", success, 
                    "Found " + agentKnowledgeBases.size() + " agent-knowledge base associations");
        } catch (Exception e) {
            printTestFailure("testFindAll", e);
        }
    }
    
    /**
     * Test the update method.
     */
    private void testUpdate() {
        try {
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
            boolean success = updatedAgentKnowledgeBase.getAgentId() == 2 &&
                              updatedAgentKnowledgeBase.getKnowledgeBaseId() == 2;
            
            printTestResult("testUpdate", success);
            
            // Clean up
            agentKnowledgeBaseDao.delete(createdAgentKnowledgeBase.getId());
        } catch (Exception e) {
            printTestFailure("testUpdate", e);
        }
    }
    
    /**
     * Test the delete method.
     */
    private void testDelete() {
        try {
            // Create a new agent-knowledge base association
            AgentKnowledgeBase agentKnowledgeBase = new AgentKnowledgeBase();
            agentKnowledgeBase.setAgentId(3);
            agentKnowledgeBase.setKnowledgeBaseId(3);
            
            AgentKnowledgeBase createdAgentKnowledgeBase = agentKnowledgeBaseDao.create(agentKnowledgeBase);
            
            // Delete the agent-knowledge base association
            boolean deleted = agentKnowledgeBaseDao.delete(createdAgentKnowledgeBase.getId());
            
            // Verify the agent-knowledge base association was deleted
            boolean success = deleted && 
                              !agentKnowledgeBaseDao.findById(createdAgentKnowledgeBase.getId()).isPresent();
            
            printTestResult("testDelete", success);
        } catch (Exception e) {
            printTestFailure("testDelete", e);
        }
    }
    
    /**
     * Test the findByAgentId method.
     */
    private void testFindByAgentId() {
        try {
            // Find agent-knowledge base associations by agent ID
            List<AgentKnowledgeBase> agentKnowledgeBases = agentKnowledgeBaseDao.findByAgentId(1);
            
            // Verify there are agent-knowledge base associations
            boolean success = !agentKnowledgeBases.isEmpty();
            printTestResult("testFindByAgentId", success, 
                    "Found " + agentKnowledgeBases.size() + " agent-knowledge base associations for agent ID 1");
        } catch (Exception e) {
            printTestFailure("testFindByAgentId", e);
        }
    }
    
    /**
     * Test the findByKnowledgeBaseId method.
     */
    private void testFindByKnowledgeBaseId() {
        try {
            // Find agent-knowledge base associations by knowledge base ID
            List<AgentKnowledgeBase> agentKnowledgeBases = agentKnowledgeBaseDao.findByKnowledgeBaseId(1);
            
            // Verify there are agent-knowledge base associations
            boolean success = !agentKnowledgeBases.isEmpty();
            printTestResult("testFindByKnowledgeBaseId", success, 
                    "Found " + agentKnowledgeBases.size() + 
                    " agent-knowledge base associations for knowledge base ID 1");
        } catch (Exception e) {
            printTestFailure("testFindByKnowledgeBaseId", e);
        }
    }
    
    /**
     * Test the existsByAgentIdAndKnowledgeBaseId method.
     */
    private void testExistsByAgentIdAndKnowledgeBaseId() {
        try {
            // Check if an association exists
            boolean exists = agentKnowledgeBaseDao.existsByAgentIdAndKnowledgeBaseId(1, 1);
            
            // Verify the association exists
            printTestResult("testExistsByAgentIdAndKnowledgeBaseId", exists, 
                    "Association between agent ID 1 and knowledge base ID 1 " + 
                    (exists ? "exists" : "does not exist"));
        } catch (Exception e) {
            printTestFailure("testExistsByAgentIdAndKnowledgeBaseId", e);
        }
    }
    
    /**
     * Test the deleteByAgentId method.
     */
    private void testDeleteByAgentId() {
        try {
            // Create a new agent-knowledge base association
            AgentKnowledgeBase agentKnowledgeBase = new AgentKnowledgeBase();
            agentKnowledgeBase.setAgentId(3);
            agentKnowledgeBase.setKnowledgeBaseId(3);
            
            AgentKnowledgeBase createdAgentKnowledgeBase = agentKnowledgeBaseDao.create(agentKnowledgeBase);
            
            // Delete associations by agent ID
            int deleted = agentKnowledgeBaseDao.deleteByAgentId(3);
            
            // Verify the associations were deleted
            boolean success = deleted > 0 && 
                              !agentKnowledgeBaseDao.findById(createdAgentKnowledgeBase.getId()).isPresent();
            
            printTestResult("testDeleteByAgentId", success, "Deleted " + deleted + " associations");
        } catch (Exception e) {
            printTestFailure("testDeleteByAgentId", e);
        }
    }
    
    /**
     * Test the deleteByKnowledgeBaseId method.
     */
    private void testDeleteByKnowledgeBaseId() {
        try {
            // Create a new agent-knowledge base association
            AgentKnowledgeBase agentKnowledgeBase = new AgentKnowledgeBase();
            agentKnowledgeBase.setAgentId(3);
            agentKnowledgeBase.setKnowledgeBaseId(3);
            
            AgentKnowledgeBase createdAgentKnowledgeBase = agentKnowledgeBaseDao.create(agentKnowledgeBase);
            
            // Delete associations by knowledge base ID
            int deleted = agentKnowledgeBaseDao.deleteByKnowledgeBaseId(3);
            
            // Verify the associations were deleted
            boolean success = deleted > 0 && 
                              !agentKnowledgeBaseDao.findById(createdAgentKnowledgeBase.getId()).isPresent();
            
            printTestResult("testDeleteByKnowledgeBaseId", success, "Deleted " + deleted + " associations");
        } catch (Exception e) {
            printTestFailure("testDeleteByKnowledgeBaseId", e);
        }
    }
    
    /**
     * Test the deleteByAgentIdAndKnowledgeBaseId method.
     */
    private void testDeleteByAgentIdAndKnowledgeBaseId() {
        try {
            // Create a new agent-knowledge base association
            AgentKnowledgeBase agentKnowledgeBase = new AgentKnowledgeBase();
            agentKnowledgeBase.setAgentId(3);
            agentKnowledgeBase.setKnowledgeBaseId(3);
            
            AgentKnowledgeBase createdAgentKnowledgeBase = agentKnowledgeBaseDao.create(agentKnowledgeBase);
            
            // Delete the association by agent ID and knowledge base ID
            boolean deleted = agentKnowledgeBaseDao.deleteByAgentIdAndKnowledgeBaseId(3, 3);
            
            // Verify the association was deleted
            boolean success = deleted && 
                              !agentKnowledgeBaseDao.findById(createdAgentKnowledgeBase.getId()).isPresent();
            
            printTestResult("testDeleteByAgentIdAndKnowledgeBaseId", success);
        } catch (Exception e) {
            printTestFailure("testDeleteByAgentIdAndKnowledgeBaseId", e);
        }
    }
}