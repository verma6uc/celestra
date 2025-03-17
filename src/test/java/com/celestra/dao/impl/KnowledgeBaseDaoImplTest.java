package com.celestra.dao.impl;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

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
     * Initialize the DAO before each test.
     */
    @Before
    public void initialize() {
        knowledgeBaseDao = new KnowledgeBaseDaoImpl();
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // Knowledge bases table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // Insert test companies first (to satisfy foreign key constraints)
        executeSQL("INSERT INTO companies (id, name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES (1, 'Test Company 1', 'Test Company Description 1', 'SMALL'::company_size, 'TECH'::company_vertical, 'ACTIVE'::company_status, NOW(), NOW())");
        
        executeSQL("INSERT INTO companies (id, name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES (2, 'Test Company 2', 'Test Company Description 2', 'MEDIUM'::company_size, 'PHARMACEUTICAL'::company_vertical, 'ACTIVE'::company_status, NOW(), NOW())");
        
        // Insert test agents (needed for agent-knowledge base associations)
        executeSQL("INSERT INTO agents (id, company_id, name, description, status, created_at, updated_at) " +
                   "VALUES (1, 1, 'Test Agent 1', 'Test Agent Description 1', 'ACTIVE'::agent_status, NOW(), NOW())");
        
        // Insert test knowledge bases
        executeSQL("INSERT INTO knowledge_bases (id, company_id, name, description, status, created_at, updated_at) " +
                   "VALUES (1, 1, 'Test KB 1', 'Test Description 1', 'ACTIVE'::knowledge_base_status, NOW(), NOW())");
        
        executeSQL("INSERT INTO knowledge_bases (id, company_id, name, description, status, created_at, updated_at) " +
                   "VALUES (2, 1, 'Test KB 2', 'Test Description 2', 'DRAFT'::knowledge_base_status, NOW(), NOW())");
        
        executeSQL("INSERT INTO knowledge_bases (id, company_id, name, description, status, created_at, updated_at) " +
                   "VALUES (3, 2, 'Another KB', 'Another Description', 'ACTIVE'::knowledge_base_status, NOW(), NOW())");
        
        // Insert agent-knowledge base associations
        executeSQL("INSERT INTO agent_knowledge_bases (agent_id, knowledge_base_id, created_at) " +
                   "VALUES (1, 1, NOW())");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM agent_knowledge_bases WHERE knowledge_base_id IN " +
                   "(SELECT id FROM knowledge_bases WHERE name LIKE 'Test KB%' OR name = 'Another KB')");
        executeSQL("DELETE FROM knowledge_bases WHERE name LIKE 'Test KB%' OR name = 'Another KB'");
        executeSQL("DELETE FROM agents WHERE id = 1");
        executeSQL("DELETE FROM companies WHERE id IN (1, 2)");
    }
    
    /**
     * Test the create method.
     */
    @Test
    public void testCreate() throws SQLException {
        // Create a new knowledge base
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setCompanyId(1);
        knowledgeBase.setName("Test KB Create");
        knowledgeBase.setDescription("Test Description Create");
        knowledgeBase.setStatus(KnowledgeBaseStatus.ACTIVE);
        
        KnowledgeBase createdKnowledgeBase = knowledgeBaseDao.create(knowledgeBase);
        
        // Verify the knowledge base was created
        assertNotNull("Created knowledge base should not be null", createdKnowledgeBase);
        assertTrue("Created knowledge base should have an ID", createdKnowledgeBase.getId() > 0);
        
        // Clean up
        boolean deleted = knowledgeBaseDao.delete(createdKnowledgeBase.getId());
        assertTrue("Knowledge base should be deleted successfully", deleted);
    }
    
    /**
     * Test the findById method.
     */
    @Test
    public void testFindById() throws SQLException {
        // Find all knowledge bases
        List<KnowledgeBase> knowledgeBases = knowledgeBaseDao.findAll();
        
        // Verify there are knowledge bases
        assertFalse("There should be knowledge bases in the database", knowledgeBases.isEmpty());
        
        // Get the first knowledge base
        KnowledgeBase knowledgeBase = knowledgeBases.get(0);
        
        // Find the knowledge base by ID
        Optional<KnowledgeBase> foundKnowledgeBase = knowledgeBaseDao.findById(knowledgeBase.getId());
        
        // Verify the knowledge base was found
        assertTrue("Knowledge base should be found by ID", foundKnowledgeBase.isPresent());
        assertEquals("Found knowledge base ID should match", knowledgeBase.getId(), foundKnowledgeBase.get().getId());
        assertEquals("Found knowledge base name should match", knowledgeBase.getName(), foundKnowledgeBase.get().getName());
    }
    
    /**
     * Test the findAll method.
     */
    @Test
    public void testFindAll() throws SQLException {
        // Find all knowledge bases
        List<KnowledgeBase> knowledgeBases = knowledgeBaseDao.findAll();
        
        // Verify there are knowledge bases
        assertFalse("There should be knowledge bases in the database", knowledgeBases.isEmpty());
    }
    
    /**
     * Test the update method.
     */
    @Test
    public void testUpdate() throws SQLException {
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
        assertEquals("Knowledge base name should be updated", "Test KB Updated", updatedKnowledgeBase.getName());
        assertEquals("Knowledge base description should be updated", "Test Description Updated", updatedKnowledgeBase.getDescription());
        
        // Clean up
        boolean deleted = knowledgeBaseDao.delete(createdKnowledgeBase.getId());
        assertTrue("Knowledge base should be deleted successfully", deleted);
    }
    
    /**
     * Test the delete method.
     */
    @Test
    public void testDelete() throws SQLException {
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
        assertTrue("Knowledge base should be deleted successfully", deleted);
        
        Optional<KnowledgeBase> foundKnowledgeBase = knowledgeBaseDao.findById(createdKnowledgeBase.getId());
        assertFalse("Knowledge base should not be found after deletion", foundKnowledgeBase.isPresent());
    }
    
    /**
     * Test the findByCompanyId method.
     */
    @Test
    public void testFindByCompanyId() throws SQLException {
        // Find knowledge bases by company ID
        List<KnowledgeBase> knowledgeBases = knowledgeBaseDao.findByCompanyId(1);
        
        // Verify there are knowledge bases
        assertFalse("There should be knowledge bases for company ID 1", knowledgeBases.isEmpty());
        
        // Verify all entries have the correct company ID
        for (KnowledgeBase knowledgeBase : knowledgeBases) {
            assertEquals("Knowledge base company ID should be 1", Integer.valueOf(1), knowledgeBase.getCompanyId());
        }
    }
    
    /**
     * Test the findByStatus method.
     */
    @Test
    public void testFindByStatus() throws SQLException {
        // Find knowledge bases by status
        List<KnowledgeBase> knowledgeBases = knowledgeBaseDao.findByStatus(KnowledgeBaseStatus.ACTIVE);
        
        // Verify there are knowledge bases
        assertFalse("There should be active knowledge bases", knowledgeBases.isEmpty());
        
        // Verify all entries have the correct status
        for (KnowledgeBase knowledgeBase : knowledgeBases) {
            assertEquals("Knowledge base status should be ACTIVE", KnowledgeBaseStatus.ACTIVE, knowledgeBase.getStatus());
        }
    }
    
    /**
     * Test the findByCompanyIdAndStatus method.
     */
    @Test
    public void testFindByCompanyIdAndStatus() throws SQLException {
        // Find knowledge bases by company ID and status
        List<KnowledgeBase> knowledgeBases = knowledgeBaseDao.findByCompanyIdAndStatus(1, KnowledgeBaseStatus.ACTIVE);
        
        // Verify there are knowledge bases
        assertFalse("There should be active knowledge bases for company ID 1", knowledgeBases.isEmpty());
        
        // Verify all entries have the correct company ID and status
        for (KnowledgeBase knowledgeBase : knowledgeBases) {
            assertEquals("Knowledge base company ID should be 1", Integer.valueOf(1), knowledgeBase.getCompanyId());
            assertEquals("Knowledge base status should be ACTIVE", KnowledgeBaseStatus.ACTIVE, knowledgeBase.getStatus());
        }
    }
    
    /**
     * Test the findByNameContaining method.
     */
    @Test
    public void testFindByNameContaining() throws SQLException {
        // Find knowledge bases by name
        List<KnowledgeBase> knowledgeBases = knowledgeBaseDao.findByNameContaining("Test");
        
        // Verify there are knowledge bases
        assertFalse("There should be knowledge bases with 'Test' in the name", knowledgeBases.isEmpty());
        
        // Verify all entries have the correct name pattern
        for (KnowledgeBase knowledgeBase : knowledgeBases) {
            assertTrue("Knowledge base name should contain 'Test'", knowledgeBase.getName().contains("Test"));
        }
    }
    
    /**
     * Test the findByAgentId method.
     */
    @Test
    public void testFindByAgentId() throws SQLException {
        // Find knowledge bases by agent ID
        List<KnowledgeBase> knowledgeBases = knowledgeBaseDao.findByAgentId(1);
        
        // Verify there are knowledge bases
        assertFalse("There should be knowledge bases for agent ID 1", knowledgeBases.isEmpty());
    }
    
    /**
     * Test the updateStatus method.
     */
    @Test
    public void testUpdateStatus() throws SQLException {
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
        assertTrue("Knowledge base should be found after status update", updatedKnowledgeBase.isPresent());
        assertEquals("Knowledge base status should be updated to DRAFT", KnowledgeBaseStatus.DRAFT, updatedKnowledgeBase.get().getStatus());
        
        // Clean up
        boolean deleted = knowledgeBaseDao.delete(createdKnowledgeBase.getId());
        assertTrue("Knowledge base should be deleted successfully", deleted);
    }
}