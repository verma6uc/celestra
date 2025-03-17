package com.celestra.dao.impl;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.celestra.dao.BaseDaoTest;
import com.celestra.dao.KnowledgeSourceDao;
import com.celestra.model.KnowledgeSource;

/**
 * Test class for KnowledgeSourceDaoImpl.
 */
public class KnowledgeSourceDaoImplTest extends BaseDaoTest {
    
    private KnowledgeSourceDao knowledgeSourceDao;
    
    /**
     * Initialize the DAO before each test.
     */
    @Before
    public void initialize() {
        knowledgeSourceDao = new KnowledgeSourceDaoImpl();
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // Knowledge sources table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // Insert test companies first (to satisfy foreign key constraints)
        executeSQL("INSERT INTO companies (id, name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES (1, 'Test Company 1', 'Test Company Description 1', 'SMALL'::company_size, 'TECH'::company_vertical, 'ACTIVE'::company_status, NOW(), NOW())");
        
        // Insert test knowledge bases (to satisfy foreign key constraints)
        executeSQL("INSERT INTO knowledge_bases (id, company_id, name, description, status, created_at, updated_at) " +
                   "VALUES (1, 1, 'Test Knowledge Base 1', 'Test Knowledge Base Description 1', 'ACTIVE'::knowledge_base_status, NOW(), NOW())");
        
        executeSQL("INSERT INTO knowledge_bases (id, company_id, name, description, status, created_at, updated_at) " +
                   "VALUES (2, 1, 'Test Knowledge Base 2', 'Test Knowledge Base Description 2', 'ACTIVE'::knowledge_base_status, NOW(), NOW())");
        
        // Insert test knowledge types (to satisfy foreign key constraints)
        executeSQL("INSERT INTO knowledge_types (id, name, description, created_at, updated_at) " +
                   "VALUES (1, 'Test Document', 'Test Document-based knowledge source', NOW(), NOW())");
        
        executeSQL("INSERT INTO knowledge_types (id, name, description, created_at, updated_at) " +
                   "VALUES (2, 'Test Database', 'Test Database-based knowledge source', NOW(), NOW())");
        
        // Insert test knowledge sources
        executeSQL("INSERT INTO knowledge_sources (id, knowledge_base_id, knowledge_type_id, name, created_at, updated_at) " +
                   "VALUES (nextval('knowledge_sources_id_seq'), 1, 1, 'Test Document Source', NOW(), NOW())");
        
        executeSQL("INSERT INTO knowledge_sources (id, knowledge_base_id, knowledge_type_id, name, created_at, updated_at) " +
                   "VALUES (nextval('knowledge_sources_id_seq'), 1, 2, 'Test Database Source', NOW(), NOW())");
        
        executeSQL("INSERT INTO knowledge_sources (id, knowledge_base_id, knowledge_type_id, name, created_at, updated_at) " +
                   "VALUES (nextval('knowledge_sources_id_seq'), 2, 1, 'Test Document Source 2', NOW(), NOW())");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM knowledge_sources WHERE name LIKE 'Test%'");
        executeSQL("DELETE FROM knowledge_types WHERE name LIKE 'Test%'");
        executeSQL("DELETE FROM knowledge_bases WHERE name LIKE 'Test%'");
        executeSQL("DELETE FROM companies WHERE id = 1");
    }
    
    /**
     * Test the create method.
     */
    @Test
    public void testCreate() throws SQLException {
        // Create a new knowledge source
        KnowledgeSource knowledgeSource = new KnowledgeSource();
        knowledgeSource.setKnowledgeBaseId(1);
        knowledgeSource.setKnowledgeTypeId(1);
        knowledgeSource.setName("Test Knowledge Source Create");
        
        KnowledgeSource createdKnowledgeSource = knowledgeSourceDao.create(knowledgeSource);
        
        // Verify the knowledge source was created
        assertNotNull("Created knowledge source should not be null", createdKnowledgeSource);
        assertTrue("Created knowledge source should have an ID", createdKnowledgeSource.getId() > 0);
        
        // Clean up
        boolean deleted = knowledgeSourceDao.delete(createdKnowledgeSource.getId());
        assertTrue("Knowledge source should be deleted successfully", deleted);
    }
    
    /**
     * Test the findById method.
     */
    @Test
    public void testFindById() throws SQLException {
        // Find all knowledge sources
        List<KnowledgeSource> knowledgeSources = knowledgeSourceDao.findAll();
        
        // Verify there are knowledge sources
        assertFalse("There should be knowledge sources in the database", knowledgeSources.isEmpty());
        
        // Get the first knowledge source
        KnowledgeSource knowledgeSource = knowledgeSources.get(0);
        
        // Find the knowledge source by ID
        Optional<KnowledgeSource> foundKnowledgeSource = knowledgeSourceDao.findById(knowledgeSource.getId());
        
        // Verify the knowledge source was found
        assertTrue("Knowledge source should be found by ID", foundKnowledgeSource.isPresent());
        assertEquals("Found knowledge source ID should match", knowledgeSource.getId(), foundKnowledgeSource.get().getId());
        assertEquals("Found knowledge source name should match", knowledgeSource.getName(), foundKnowledgeSource.get().getName());
    }
    
    /**
     * Test the findAll method.
     */
    @Test
    public void testFindAll() throws SQLException {
        // Find all knowledge sources
        List<KnowledgeSource> knowledgeSources = knowledgeSourceDao.findAll();
        
        // Verify there are knowledge sources
        assertFalse("There should be knowledge sources in the database", knowledgeSources.isEmpty());
    }
    
    /**
     * Test the update method.
     */
    @Test
    public void testUpdate() throws SQLException {
        // Create a new knowledge source
        KnowledgeSource knowledgeSource = new KnowledgeSource();
        knowledgeSource.setKnowledgeBaseId(1);
        knowledgeSource.setKnowledgeTypeId(1);
        knowledgeSource.setName("Test Knowledge Source Update");
        
        KnowledgeSource createdKnowledgeSource = knowledgeSourceDao.create(knowledgeSource);
        
        // Update the knowledge source
        createdKnowledgeSource.setName("Test Knowledge Source Updated");
        createdKnowledgeSource.setKnowledgeTypeId(2);
        
        KnowledgeSource updatedKnowledgeSource = knowledgeSourceDao.update(createdKnowledgeSource);
        
        // Verify the knowledge source was updated
        assertEquals("Knowledge source name should be updated", "Test Knowledge Source Updated", updatedKnowledgeSource.getName());
        assertEquals("Knowledge source knowledge type ID should be updated", Integer.valueOf(2), updatedKnowledgeSource.getKnowledgeTypeId());
        
        // Clean up
        boolean deleted = knowledgeSourceDao.delete(createdKnowledgeSource.getId());
        assertTrue("Knowledge source should be deleted successfully", deleted);
    }
    
    /**
     * Test the delete method.
     */
    @Test
    public void testDelete() throws SQLException {
        // Create a new knowledge source
        KnowledgeSource knowledgeSource = new KnowledgeSource();
        knowledgeSource.setKnowledgeBaseId(1);
        knowledgeSource.setKnowledgeTypeId(1);
        knowledgeSource.setName("Test Knowledge Source Delete");
        
        KnowledgeSource createdKnowledgeSource = knowledgeSourceDao.create(knowledgeSource);
        
        // Delete the knowledge source
        boolean deleted = knowledgeSourceDao.delete(createdKnowledgeSource.getId());
        
        // Verify the knowledge source was deleted
        assertTrue("Knowledge source should be deleted successfully", deleted);
        
        Optional<KnowledgeSource> foundKnowledgeSource = knowledgeSourceDao.findById(createdKnowledgeSource.getId());
        assertFalse("Knowledge source should not be found after deletion", foundKnowledgeSource.isPresent());
    }
    
    /**
     * Test the findByKnowledgeBaseId method.
     */
    @Test
    public void testFindByKnowledgeBaseId() throws SQLException {
        // Find knowledge sources by knowledge base ID
        List<KnowledgeSource> knowledgeSources = knowledgeSourceDao.findByKnowledgeBaseId(1);
        
        // Verify there are knowledge sources
        assertFalse("There should be knowledge sources for knowledge base ID 1", knowledgeSources.isEmpty());
        
        // Verify all entries have the correct knowledge base ID
        for (KnowledgeSource knowledgeSource : knowledgeSources) {
            assertEquals("Knowledge source knowledge base ID should be 1", Integer.valueOf(1), knowledgeSource.getKnowledgeBaseId());
        }
    }
    
    /**
     * Test the findByKnowledgeTypeId method.
     */
    @Test
    public void testFindByKnowledgeTypeId() throws SQLException {
        // Find knowledge sources by knowledge type ID
        List<KnowledgeSource> knowledgeSources = knowledgeSourceDao.findByKnowledgeTypeId(1);
        
        // Verify there are knowledge sources
        assertFalse("There should be knowledge sources for knowledge type ID 1", knowledgeSources.isEmpty());
        
        // Verify all entries have the correct knowledge type ID
        for (KnowledgeSource knowledgeSource : knowledgeSources) {
            assertEquals("Knowledge source knowledge type ID should be 1", Integer.valueOf(1), knowledgeSource.getKnowledgeTypeId());
        }
    }
    
    /**
     * Test the findByName method.
     */
    @Test
    public void testFindByName() throws SQLException {
        // Find knowledge source by name
        Optional<KnowledgeSource> knowledgeSource = knowledgeSourceDao.findByName("Test Document Source");
        
        // Verify the knowledge source was found
        assertTrue("Knowledge source should be found by name", knowledgeSource.isPresent());
        assertEquals("Found knowledge source name should match", "Test Document Source", knowledgeSource.get().getName());
    }
    
    /**
     * Test the findByNameContaining method.
     */
    @Test
    public void testFindByNameContaining() throws SQLException {
        // Find knowledge sources by name containing
        List<KnowledgeSource> knowledgeSources = knowledgeSourceDao.findByNameContaining("Document");
        
        // Verify there are knowledge sources
        assertFalse("There should be knowledge sources with names containing 'Document'", knowledgeSources.isEmpty());
        
        // Verify all entries have names containing the pattern
        for (KnowledgeSource knowledgeSource : knowledgeSources) {
            assertTrue("Knowledge source name should contain 'Document'", 
                    knowledgeSource.getName().contains("Document"));
        }
    }
    
    /**
     * Test the findByKnowledgeBaseIdAndKnowledgeTypeId method.
     */
    @Test
    public void testFindByKnowledgeBaseIdAndKnowledgeTypeId() throws SQLException {
        // Find knowledge sources by knowledge base ID and knowledge type ID
        List<KnowledgeSource> knowledgeSources = knowledgeSourceDao.findByKnowledgeBaseIdAndKnowledgeTypeId(1, 1);
        
        // Verify there are knowledge sources
        assertFalse("There should be knowledge sources for knowledge base ID 1 and knowledge type ID 1", knowledgeSources.isEmpty());
        
        // Verify all entries have the correct knowledge base ID and knowledge type ID
        for (KnowledgeSource knowledgeSource : knowledgeSources) {
            assertEquals("Knowledge source knowledge base ID should be 1", Integer.valueOf(1), knowledgeSource.getKnowledgeBaseId());
            assertEquals("Knowledge source knowledge type ID should be 1", Integer.valueOf(1), knowledgeSource.getKnowledgeTypeId());
        }
    }
}