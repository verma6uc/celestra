package com.celestra.dao.impl;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.celestra.dao.BaseDaoTest;
import com.celestra.dao.KnowledgeSourceDao;
import com.celestra.model.KnowledgeSource;
import com.celestra.db.DatabaseUtil;

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
        executeSQL("INSERT INTO companies (name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES ('Test Company 1', 'Test Company Description 1', 'SMALL'::company_size, 'TECH'::company_vertical, 'ACTIVE'::company_status, NOW(), NOW()) RETURNING id");
        
        // Insert test knowledge bases (to satisfy foreign key constraints)
        executeSQL("INSERT INTO knowledge_bases (company_id, name, description, status, created_at, updated_at) " +
                   "VALUES ((SELECT id FROM companies WHERE name = 'Test Company 1'), 'Test Knowledge Base 1', 'Test Knowledge Base Description 1', 'ACTIVE'::knowledge_base_status, NOW(), NOW()) RETURNING id");
        
        executeSQL("INSERT INTO knowledge_bases (company_id, name, description, status, created_at, updated_at) " +
                   "VALUES ((SELECT id FROM companies WHERE name = 'Test Company 1'), 'Test Knowledge Base 2', 'Test Knowledge Base Description 2', 'ACTIVE'::knowledge_base_status, NOW(), NOW()) RETURNING id");
        
        // Insert test knowledge types (to satisfy foreign key constraints)
        executeSQL("INSERT INTO knowledge_types (name, description, created_at, updated_at) " +
                   "VALUES ('Test Document', 'Test Document-based knowledge source', NOW(), NOW()) RETURNING id");
        
        executeSQL("INSERT INTO knowledge_types (name, description, created_at, updated_at) " +
                   "VALUES ('Test Database', 'Test Database-based knowledge source', NOW(), NOW()) RETURNING id");
        
        // Insert test knowledge sources
        executeSQL("INSERT INTO knowledge_sources (id, knowledge_base_id, knowledge_type_id, name, created_at, updated_at) " +
                   "VALUES (nextval('knowledge_sources_id_seq'), (SELECT id FROM knowledge_bases WHERE name = 'Test Knowledge Base 1'), (SELECT id FROM knowledge_types WHERE name = 'Test Document'), 'Test Document Source', NOW(), NOW())");
        
        executeSQL("INSERT INTO knowledge_sources (id, knowledge_base_id, knowledge_type_id, name, created_at, updated_at) " +
                   "VALUES (nextval('knowledge_sources_id_seq'), (SELECT id FROM knowledge_bases WHERE name = 'Test Knowledge Base 1'), (SELECT id FROM knowledge_types WHERE name = 'Test Database'), 'Test Database Source', NOW(), NOW())");
        
        executeSQL("INSERT INTO knowledge_sources (id, knowledge_base_id, knowledge_type_id, name, created_at, updated_at) " +
                   "VALUES (nextval('knowledge_sources_id_seq'), (SELECT id FROM knowledge_bases WHERE name = 'Test Knowledge Base 2'), (SELECT id FROM knowledge_types WHERE name = 'Test Document'), 'Test Document Source 2', NOW(), NOW())");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM knowledge_sources WHERE name LIKE 'Test%'");
        executeSQL("DELETE FROM knowledge_types WHERE name LIKE 'Test%'");
        executeSQL("DELETE FROM knowledge_bases WHERE name LIKE 'Test%'");
        executeSQL("DELETE FROM companies WHERE name = 'Test Company 1'");
    }
    
    /**
     * Test the create method.
     */
    @Test
    public void testCreate() throws SQLException {
        // Create a new knowledge source
        KnowledgeSource knowledgeSource = new KnowledgeSource();
        knowledgeSource.setKnowledgeBaseId(getKnowledgeBaseId("Test Knowledge Base 1"));
        knowledgeSource.setKnowledgeTypeId(getKnowledgeTypeId("Test Document"));
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
        knowledgeSource.setKnowledgeBaseId(getKnowledgeBaseId("Test Knowledge Base 1"));
        knowledgeSource.setKnowledgeTypeId(getKnowledgeTypeId("Test Document"));
        knowledgeSource.setName("Test Knowledge Source Update");
        
        KnowledgeSource createdKnowledgeSource = knowledgeSourceDao.create(knowledgeSource);
        
        // Update the knowledge source
        createdKnowledgeSource.setName("Test Knowledge Source Updated");
        createdKnowledgeSource.setKnowledgeTypeId(getKnowledgeTypeId("Test Database"));
        
        KnowledgeSource updatedKnowledgeSource = knowledgeSourceDao.update(createdKnowledgeSource);
        
        // Verify the knowledge source was updated
        assertEquals("Knowledge source name should be updated", "Test Knowledge Source Updated", updatedKnowledgeSource.getName());
        assertEquals("Knowledge source knowledge type ID should be updated", getKnowledgeTypeId("Test Database"), updatedKnowledgeSource.getKnowledgeTypeId());
        
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
        knowledgeSource.setKnowledgeBaseId(getKnowledgeBaseId("Test Knowledge Base 1"));
        knowledgeSource.setKnowledgeTypeId(getKnowledgeTypeId("Test Document"));
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
        List<KnowledgeSource> knowledgeSources = knowledgeSourceDao.findByKnowledgeBaseId(getKnowledgeBaseId("Test Knowledge Base 1"));
        
        // Verify there are knowledge sources
        assertFalse("There should be knowledge sources for Test Knowledge Base 1", knowledgeSources.isEmpty());
        
        // Verify all entries have the correct knowledge base ID
        for (KnowledgeSource knowledgeSource : knowledgeSources) {
            assertEquals("Knowledge source knowledge base ID should match Test Knowledge Base 1", getKnowledgeBaseId("Test Knowledge Base 1"), knowledgeSource.getKnowledgeBaseId());
        }
    }
    
    /**
     * Test the findByKnowledgeTypeId method.
     */
    @Test
    public void testFindByKnowledgeTypeId() throws SQLException {
        // Find knowledge sources by knowledge type ID
        List<KnowledgeSource> knowledgeSources = knowledgeSourceDao.findByKnowledgeTypeId(getKnowledgeTypeId("Test Document"));
        
        // Verify there are knowledge sources
        assertFalse("There should be knowledge sources for Test Document", knowledgeSources.isEmpty());
        
        // Verify all entries have the correct knowledge type ID
        for (KnowledgeSource knowledgeSource : knowledgeSources) {
            assertEquals("Knowledge source knowledge type ID should match Test Document", getKnowledgeTypeId("Test Document"), knowledgeSource.getKnowledgeTypeId());
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
        List<KnowledgeSource> knowledgeSources = knowledgeSourceDao.findByKnowledgeBaseIdAndKnowledgeTypeId(
                getKnowledgeBaseId("Test Knowledge Base 1"), 
                getKnowledgeTypeId("Test Document"));
        
        // Verify there are knowledge sources
        assertFalse("There should be knowledge sources for Test Knowledge Base 1 and Test Document", knowledgeSources.isEmpty());
        
        // Verify all entries have the correct knowledge base ID and knowledge type ID
        for (KnowledgeSource knowledgeSource : knowledgeSources) {
            assertEquals("Knowledge source knowledge base ID should match Test Knowledge Base 1", 
                    getKnowledgeBaseId("Test Knowledge Base 1"), knowledgeSource.getKnowledgeBaseId());
            assertEquals("Knowledge source knowledge type ID should match Test Document", 
                    getKnowledgeTypeId("Test Document"), knowledgeSource.getKnowledgeTypeId());
        }
    }
    
    /**
     * Helper method to get the ID of a knowledge base by name.
     * 
     * @param name The name of the knowledge base
     * @return The ID of the knowledge base
     * @throws SQLException if a database error occurs
     */
    private Integer getKnowledgeBaseId(String name) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM knowledge_bases WHERE name = ?")) {
            ps.setString(1, name);
            var rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : null;
        }
    }
    
    /**
     * Helper method to get the ID of a knowledge type by name.
     * 
     * @param name The name of the knowledge type
     * @return The ID of the knowledge type
     * @throws SQLException if a database error occurs
     */
    private Integer getKnowledgeTypeId(String name) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM knowledge_types WHERE name = ?")) {
            ps.setString(1, name);
            var rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : null;
        }
    }
}