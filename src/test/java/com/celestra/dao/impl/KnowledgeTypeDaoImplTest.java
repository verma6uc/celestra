package com.celestra.dao.impl;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.celestra.dao.BaseDaoTest;
import com.celestra.dao.KnowledgeTypeDao;
import com.celestra.model.KnowledgeType;

/**
 * Test class for KnowledgeTypeDaoImpl.
 */
public class KnowledgeTypeDaoImplTest extends BaseDaoTest {
    
    private KnowledgeTypeDao knowledgeTypeDao;
    
    /**
     * Initialize the DAO before each test.
     */
    @Before
    public void initialize() {
        knowledgeTypeDao = new KnowledgeTypeDaoImpl();
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // Knowledge types table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // Insert test knowledge types
        executeSQL("INSERT INTO knowledge_types (name, description, created_at, updated_at) " +
                   "VALUES ('Document', 'Document-based knowledge source', NOW(), NOW()) RETURNING id");
        
        executeSQL("INSERT INTO knowledge_types (name, description, created_at, updated_at) " +
                   "VALUES ('Database', 'Database-based knowledge source', NOW(), NOW()) RETURNING id");
        
        executeSQL("INSERT INTO knowledge_types (name, description, created_at, updated_at) " +
                   "VALUES ('API', 'API-based knowledge source', NOW(), NOW()) RETURNING id");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM knowledge_types WHERE name LIKE 'Test%' OR name IN ('Document', 'Database', 'API')");
    }
    
    /**
     * Test the create method.
     */
    @Test
    public void testCreate() throws SQLException {
        // Create a new knowledge type
        KnowledgeType knowledgeType = new KnowledgeType();
        knowledgeType.setName("Test Knowledge Type");
        knowledgeType.setDescription("Test description for knowledge type");
        
        KnowledgeType createdKnowledgeType = knowledgeTypeDao.create(knowledgeType);
        
        // Verify the knowledge type was created
        assertNotNull("Created knowledge type should not be null", createdKnowledgeType);
        assertTrue("Created knowledge type should have an ID", createdKnowledgeType.getId() > 0);
        
        // Clean up
        boolean deleted = knowledgeTypeDao.delete(createdKnowledgeType.getId());
        assertTrue("Knowledge type should be deleted successfully", deleted);
    }
    
    /**
     * Test the findById method.
     */
    @Test
    public void testFindById() throws SQLException {
        // Find all knowledge types
        List<KnowledgeType> knowledgeTypes = knowledgeTypeDao.findAll();
        
        // Verify there are knowledge types
        assertFalse("There should be knowledge types in the database", knowledgeTypes.isEmpty());
        
        // Get the first knowledge type
        KnowledgeType knowledgeType = knowledgeTypes.get(0);
        
        // Find the knowledge type by ID
        Optional<KnowledgeType> foundKnowledgeType = knowledgeTypeDao.findById(knowledgeType.getId());
        
        // Verify the knowledge type was found
        assertTrue("Knowledge type should be found by ID", foundKnowledgeType.isPresent());
        assertEquals("Found knowledge type ID should match", knowledgeType.getId(), foundKnowledgeType.get().getId());
        assertEquals("Found knowledge type name should match", knowledgeType.getName(), foundKnowledgeType.get().getName());
    }
    
    /**
     * Test the findAll method.
     */
    @Test
    public void testFindAll() throws SQLException {
        // Find all knowledge types
        List<KnowledgeType> knowledgeTypes = knowledgeTypeDao.findAll();
        
        // Verify there are knowledge types
        assertFalse("There should be knowledge types in the database", knowledgeTypes.isEmpty());
    }
    
    /**
     * Test the update method.
     */
    @Test
    public void testUpdate() throws SQLException {
        // Create a new knowledge type
        KnowledgeType knowledgeType = new KnowledgeType();
        knowledgeType.setName("Test Knowledge Type Update");
        knowledgeType.setDescription("Test description for knowledge type update");
        
        KnowledgeType createdKnowledgeType = knowledgeTypeDao.create(knowledgeType);
        
        // Update the knowledge type
        createdKnowledgeType.setName("Test Knowledge Type Updated");
        createdKnowledgeType.setDescription("Updated test description for knowledge type");
        
        KnowledgeType updatedKnowledgeType = knowledgeTypeDao.update(createdKnowledgeType);
        
        // Verify the knowledge type was updated
        assertEquals("Knowledge type name should be updated", "Test Knowledge Type Updated", updatedKnowledgeType.getName());
        assertEquals("Knowledge type description should be updated", "Updated test description for knowledge type", updatedKnowledgeType.getDescription());
        
        // Clean up
        boolean deleted = knowledgeTypeDao.delete(createdKnowledgeType.getId());
        assertTrue("Knowledge type should be deleted successfully", deleted);
    }
    
    /**
     * Test the delete method.
     */
    @Test
    public void testDelete() throws SQLException {
        // Create a new knowledge type
        KnowledgeType knowledgeType = new KnowledgeType();
        knowledgeType.setName("Test Knowledge Type Delete");
        knowledgeType.setDescription("Test description for knowledge type delete");
        
        KnowledgeType createdKnowledgeType = knowledgeTypeDao.create(knowledgeType);
        
        // Delete the knowledge type
        boolean deleted = knowledgeTypeDao.delete(createdKnowledgeType.getId());
        
        // Verify the knowledge type was deleted
        assertTrue("Knowledge type should be deleted successfully", deleted);
        
        Optional<KnowledgeType> foundKnowledgeType = knowledgeTypeDao.findById(createdKnowledgeType.getId());
        assertFalse("Knowledge type should not be found after deletion", foundKnowledgeType.isPresent());
    }
    
    /**
     * Test the findByName method.
     */
    @Test
    public void testFindByName() throws SQLException {
        // Find knowledge type by name
        Optional<KnowledgeType> knowledgeType = knowledgeTypeDao.findByName("Document");
        
        // Verify the knowledge type was found
        assertTrue("Knowledge type should be found by name", knowledgeType.isPresent());
        assertEquals("Found knowledge type name should match", "Document", knowledgeType.get().getName());
    }
    
    /**
     * Test the findByNameContaining method.
     */
    @Test
    public void testFindByNameContaining() throws SQLException {
        // Find knowledge types by name containing
        List<KnowledgeType> knowledgeTypes = knowledgeTypeDao.findByNameContaining("base");
        
        // Verify there are knowledge types
        assertFalse("There should be knowledge types with names containing 'base'", knowledgeTypes.isEmpty());
        
        // Verify all entries have names containing the pattern
        for (KnowledgeType knowledgeType : knowledgeTypes) {
            assertTrue("Knowledge type name should contain 'base'", 
                    knowledgeType.getName().toLowerCase().contains("base"));
        }
    }
}