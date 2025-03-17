package com.celestra.dao.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.celestra.dao.BaseDaoTest;
import com.celestra.dao.KnowledgeBaseDao;
import com.celestra.enums.KnowledgeBaseStatus;
import com.celestra.model.KnowledgeBase;

/**
 * Test class for KnowledgeBaseDaoImpl.
 */
public class KnowledgeBaseDaoImplTest extends BaseDaoTest {
    
    private KnowledgeBaseDao knowledgeBaseDao;
    
    /**
     * Main method to run the tests.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        KnowledgeBaseDaoImplTest test = new KnowledgeBaseDaoImplTest();
        test.runTests();
    }
    
    /**
     * Constructor.
     */
    public KnowledgeBaseDaoImplTest() {
        knowledgeBaseDao = new KnowledgeBaseDaoImpl();
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
            testFindByAgentId();
            testUpdateStatus();
            
            tearDown();
            
            System.out.println("All tests completed.");
        } catch (Exception e) {
            System.err.println("Error running tests: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // Knowledge bases table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // Insert test data
        executeSQL("INSERT INTO knowledge_bases (company_id, name, description, status, created_at, updated_at) " +
                   "VALUES (1, 'Test KB 1', 'Test Description 1', 'ACTIVE', NOW(), NOW())");
        
        executeSQL("INSERT INTO knowledge_bases (company_id, name, description, status, created_at, updated_at) " +
                   "VALUES (1, 'Test KB 2', 'Test Description 2', 'DRAFT', NOW(), NOW())");
        
        executeSQL("INSERT INTO knowledge_bases (company_id, name, description, status, created_at, updated_at) " +
                   "VALUES (2, 'Another KB', 'Another Description', 'ACTIVE', NOW(), NOW())");
        
        // Insert agent-knowledge base associations
        executeSQL("INSERT INTO agent_knowledge_bases (agent_id, knowledge_base_id, created_at) " +
                   "VALUES (1, 1, NOW())");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM agent_knowledge_bases WHERE knowledge_base_id IN " +
                   "(SELECT id FROM knowledge_bases WHERE name LIKE 'Test KB%' OR name = 'Another KB')");
        executeSQL("DELETE FROM knowledge_bases WHERE name LIKE 'Test KB%' OR name = 'Another KB'");
    }
    
    /**
     * Test the create method.
     */
    private void testCreate() {
        try {
            // Create a new knowledge base
            KnowledgeBase knowledgeBase = new KnowledgeBase();
            knowledgeBase.setCompanyId(1);
            knowledgeBase.setName("Test KB Create");
            knowledgeBase.setDescription("Test Description Create");
            knowledgeBase.setStatus(KnowledgeBaseStatus.ACTIVE);
            
            KnowledgeBase createdKnowledgeBase = knowledgeBaseDao.create(knowledgeBase);
            
            // Verify the knowledge base was created
            boolean success = createdKnowledgeBase.getId() > 0;
            printTestResult("testCreate", success);
            
            // Clean up
            if (success) {
                knowledgeBaseDao.delete(createdKnowledgeBase.getId());
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
            // Find all knowledge bases
            List<KnowledgeBase> knowledgeBases = knowledgeBaseDao.findAll();
            
            // Verify there are knowledge bases
            if (knowledgeBases.isEmpty()) {
                printTestResult("testFindById", false, "No knowledge bases found");
                return;
            }
            
            // Get the first knowledge base
            KnowledgeBase knowledgeBase = knowledgeBases.get(0);
            
            // Find the knowledge base by ID
            Optional<KnowledgeBase> foundKnowledgeBase = knowledgeBaseDao.findById(knowledgeBase.getId());
            
            // Verify the knowledge base was found
            boolean success = foundKnowledgeBase.isPresent() && 
                              foundKnowledgeBase.get().getId().equals(knowledgeBase.getId()) &&
                              foundKnowledgeBase.get().getName().equals(knowledgeBase.getName());
            
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
            // Find all knowledge bases
            List<KnowledgeBase> knowledgeBases = knowledgeBaseDao.findAll();
            
            // Verify there are knowledge bases
            boolean success = !knowledgeBases.isEmpty();
            printTestResult("testFindAll", success, "Found " + knowledgeBases.size() + " knowledge bases");
        } catch (Exception e) {
            printTestFailure("testFindAll", e);
        }
    }
    
    /**
     * Test the update method.
     */
    private void testUpdate() {
        try {
            // Create a new knowledge base
            KnowledgeBase knowledgeBase = new KnowledgeBase();
            knowledgeBase.setCompanyId(1);
            knowledgeBase.setName("Test KB Update");
            knowledgeBase.setDescription("Test Description Update");
            knowledgeBase.setStatus(KnowledgeBaseStatus.ACTIVE);
            
            KnowledgeBase createdKnowledgeBase = knowledgeBaseDao.create(knowledgeBase);
            
            // Update the knowledge base
            createdKnowledgeBase.setName("Test KB Updated");
            createdKnowledgeBase.setDescription("Test Description Updated");
            
            KnowledgeBase updatedKnowledgeBase = knowledgeBaseDao.update(createdKnowledgeBase);
            
            // Verify the knowledge base was updated
            boolean success = updatedKnowledgeBase.getName().equals("Test KB Updated") &&
                              updatedKnowledgeBase.getDescription().equals("Test Description Updated");
            
            printTestResult("testUpdate", success);
            
            // Clean up
            knowledgeBaseDao.delete(createdKnowledgeBase.getId());
        } catch (Exception e) {
            printTestFailure("testUpdate", e);
        }
    }
    
    /**
     * Test the delete method.
     */
    private void testDelete() {
        try {
            // Create a new knowledge base
            KnowledgeBase knowledgeBase = new KnowledgeBase();
            knowledgeBase.setCompanyId(1);
            knowledgeBase.setName("Test KB Delete");
            knowledgeBase.setDescription("Test Description Delete");
            knowledgeBase.setStatus(KnowledgeBaseStatus.ACTIVE);
            
            KnowledgeBase createdKnowledgeBase = knowledgeBaseDao.create(knowledgeBase);
            
            // Delete the knowledge base
            boolean deleted = knowledgeBaseDao.delete(createdKnowledgeBase.getId());
            
            // Verify the knowledge base was deleted
            boolean success = deleted && !knowledgeBaseDao.findById(createdKnowledgeBase.getId()).isPresent();
            
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
            // Find knowledge bases by company ID
            List<KnowledgeBase> knowledgeBases = knowledgeBaseDao.findByCompanyId(1);
            
            // Verify there are knowledge bases
            boolean success = !knowledgeBases.isEmpty();
            printTestResult("testFindByCompanyId", success, 
                    "Found " + knowledgeBases.size() + " knowledge bases for company ID 1");
        } catch (Exception e) {
            printTestFailure("testFindByCompanyId", e);
        }
    }
    
    /**
     * Test the findByStatus method.
     */
    private void testFindByStatus() {
        try {
            // Find knowledge bases by status
            List<KnowledgeBase> knowledgeBases = knowledgeBaseDao.findByStatus(KnowledgeBaseStatus.ACTIVE);
            
            // Verify there are knowledge bases
            boolean success = !knowledgeBases.isEmpty();
            printTestResult("testFindByStatus", success, 
                    "Found " + knowledgeBases.size() + " active knowledge bases");
        } catch (Exception e) {
            printTestFailure("testFindByStatus", e);
        }
    }
    
    /**
     * Test the findByCompanyIdAndStatus method.
     */
    private void testFindByCompanyIdAndStatus() {
        try {
            // Find knowledge bases by company ID and status
            List<KnowledgeBase> knowledgeBases = knowledgeBaseDao.findByCompanyIdAndStatus(1, KnowledgeBaseStatus.ACTIVE);
            
            // Verify there are knowledge bases
            boolean success = !knowledgeBases.isEmpty();
            printTestResult("testFindByCompanyIdAndStatus", success, 
                    "Found " + knowledgeBases.size() + " active knowledge bases for company ID 1");
        } catch (Exception e) {
            printTestFailure("testFindByCompanyIdAndStatus", e);
        }
    }
    
    /**
     * Test the findByNameContaining method.
     */
    private void testFindByNameContaining() {
        try {
            // Find knowledge bases by name
            List<KnowledgeBase> knowledgeBases = knowledgeBaseDao.findByNameContaining("Test");
            
            // Verify there are knowledge bases
            boolean success = !knowledgeBases.isEmpty();
            printTestResult("testFindByNameContaining", success, 
                    "Found " + knowledgeBases.size() + " knowledge bases with 'Test' in the name");
        } catch (Exception e) {
            printTestFailure("testFindByNameContaining", e);
        }
    }
    
    /**
     * Test the findByAgentId method.
     */
    private void testFindByAgentId() {
        try {
            // Find knowledge bases by agent ID
            List<KnowledgeBase> knowledgeBases = knowledgeBaseDao.findByAgentId(1);
            
            // Verify there are knowledge bases
            boolean success = !knowledgeBases.isEmpty();
            printTestResult("testFindByAgentId", success, 
                    "Found " + knowledgeBases.size() + " knowledge bases for agent ID 1");
        } catch (Exception e) {
            printTestFailure("testFindByAgentId", e);
        }
    }
    
    /**
     * Test the updateStatus method.
     */
    private void testUpdateStatus() {
        try {
            // Create a new knowledge base
            KnowledgeBase knowledgeBase = new KnowledgeBase();
            knowledgeBase.setCompanyId(1);
            knowledgeBase.setName("Test KB Status");
            knowledgeBase.setDescription("Test Description Status");
            knowledgeBase.setStatus(KnowledgeBaseStatus.ACTIVE);
            
            KnowledgeBase createdKnowledgeBase = knowledgeBaseDao.create(knowledgeBase);
            
            // Update the knowledge base status
            boolean updated = knowledgeBaseDao.updateStatus(createdKnowledgeBase.getId(), KnowledgeBaseStatus.DRAFT);
            
            // Verify the knowledge base status was updated
            Optional<KnowledgeBase> updatedKnowledgeBase = knowledgeBaseDao.findById(createdKnowledgeBase.getId());
            boolean success = updated && 
                              updatedKnowledgeBase.isPresent() && 
                              updatedKnowledgeBase.get().getStatus() == KnowledgeBaseStatus.DRAFT;
            
            printTestResult("testUpdateStatus", success);
            
            // Clean up
            knowledgeBaseDao.delete(createdKnowledgeBase.getId());
        } catch (Exception e) {
            printTestFailure("testUpdateStatus", e);
        }
    }
}