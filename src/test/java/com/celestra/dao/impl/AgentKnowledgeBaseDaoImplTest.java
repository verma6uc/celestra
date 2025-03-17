package com.celestra.dao.impl;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.celestra.dao.AgentKnowledgeBaseDao;
import com.celestra.dao.BaseDaoTest;
import com.celestra.model.AgentKnowledgeBase;
import com.celestra.db.DatabaseUtil;

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
        
        // Insert test company
        executeSQL("INSERT INTO companies (name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES ('Test Company 1', 'Test Company Description 1', 'SMALL'::company_size, 'TECH'::company_vertical, 'ACTIVE'::company_status, NOW(), NOW()) RETURNING id");
        
        // Insert test users
        executeSQL("INSERT INTO users (company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES ((SELECT id FROM companies WHERE name = 'Test Company 1'), 'COMPANY_ADMIN'::user_role, 'admin@test.com', 'Admin User', 'hash123', 'ACTIVE'::user_status, NOW(), NOW()) RETURNING id");
        
        executeSQL("INSERT INTO users (company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES ((SELECT id FROM companies WHERE name = 'Test Company 1'), 'COMPANY_ADMIN'::user_role, 'admin2@test.com', 'Admin User 2', 'hash456', 'ACTIVE'::user_status, NOW(), NOW()) RETURNING id");
        
        executeSQL("INSERT INTO users (company_id, role, email, name, password_hash, status, created_at, updated_at) " +
                   "VALUES ((SELECT id FROM companies WHERE name = 'Test Company 1'), 'COMPANY_ADMIN'::user_role, 'admin3@test.com', 'Admin User 3', 'hash789', 'ACTIVE'::user_status, NOW(), NOW()) RETURNING id");
        
        // Insert test agents
        executeSQL("INSERT INTO agents (company_id, name, description, status, created_at, updated_at) " +
                   "VALUES ((SELECT id FROM companies WHERE name = 'Test Company 1'), 'Test Agent 1', 'Test Agent Description 1', 'ACTIVE'::agent_status, NOW(), NOW()) RETURNING id");
        
        executeSQL("INSERT INTO agents (company_id, name, description, status, created_at, updated_at) " +
                   "VALUES ((SELECT id FROM companies WHERE name = 'Test Company 1'), 'Test Agent 2', 'Test Agent Description 2', 'ACTIVE'::agent_status, NOW(), NOW()) RETURNING id");
        
        executeSQL("INSERT INTO agents (company_id, name, description, status, created_at, updated_at) " +
                   "VALUES ((SELECT id FROM companies WHERE name = 'Test Company 1'), 'Test Agent 3', 'Test Agent Description 3', 'ACTIVE'::agent_status, NOW(), NOW()) RETURNING id");
        
        // Insert test knowledge bases
        executeSQL("INSERT INTO knowledge_bases (company_id, name, description, status, created_at, updated_at) " +
                   "VALUES ((SELECT id FROM companies WHERE name = 'Test Company 1'), 'Test KB 1', 'Test KB Description 1', 'ACTIVE'::knowledge_base_status, NOW(), NOW()) RETURNING id");
        
        executeSQL("INSERT INTO knowledge_bases (company_id, name, description, status, created_at, updated_at) " +
                   "VALUES ((SELECT id FROM companies WHERE name = 'Test Company 1'), 'Test KB 2', 'Test KB Description 2', 'ACTIVE'::knowledge_base_status, NOW(), NOW()) RETURNING id");
        
        executeSQL("INSERT INTO knowledge_bases (company_id, name, description, status, created_at, updated_at) " +
                   "VALUES ((SELECT id FROM companies WHERE name = 'Test Company 1'), 'Test KB 3', 'Test KB Description 3', 'ACTIVE'::knowledge_base_status, NOW(), NOW()) RETURNING id");
        
        // Insert test agent-knowledge base associations
        executeSQL("INSERT INTO agent_knowledge_bases (agent_id, knowledge_base_id, created_at) " +
                   "VALUES ((SELECT id FROM agents WHERE name = 'Test Agent 1'), (SELECT id FROM knowledge_bases WHERE name = 'Test KB 1'), NOW())");
        
        executeSQL("INSERT INTO agent_knowledge_bases (agent_id, knowledge_base_id, created_at) " +
                   "VALUES ((SELECT id FROM agents WHERE name = 'Test Agent 1'), (SELECT id FROM knowledge_bases WHERE name = 'Test KB 2'), NOW())");
        
        executeSQL("INSERT INTO agent_knowledge_bases (agent_id, knowledge_base_id, created_at) " +
                   "VALUES ((SELECT id FROM agents WHERE name = 'Test Agent 2'), (SELECT id FROM knowledge_bases WHERE name = 'Test KB 1'), NOW())");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM agent_knowledge_bases WHERE agent_id IN (SELECT id FROM agents WHERE name LIKE 'Test Agent%')");
        executeSQL("DELETE FROM knowledge_bases WHERE name LIKE 'Test KB%'");
        executeSQL("DELETE FROM agents WHERE name LIKE 'Test Agent%'");
        executeSQL("DELETE FROM users WHERE email LIKE 'admin%@test.com'");
        executeSQL("DELETE FROM companies WHERE name = 'Test Company 1'");
    }
    
    /**
     * Test the create method.
     */
    @Test
    public void testCreate() throws SQLException {
        // Create a new agent-knowledge base association
        AgentKnowledgeBase agentKnowledgeBase = new AgentKnowledgeBase();
        agentKnowledgeBase.setAgentId(getAgentId("Test Agent 3"));
        agentKnowledgeBase.setKnowledgeBaseId(getKnowledgeBaseId("Test KB 3"));
        
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
        agentKnowledgeBase.setAgentId(getAgentId("Test Agent 3"));
        agentKnowledgeBase.setKnowledgeBaseId(getKnowledgeBaseId("Test KB 3"));
        
        AgentKnowledgeBase createdAgentKnowledgeBase = agentKnowledgeBaseDao.create(agentKnowledgeBase);
        
        // Update the agent-knowledge base association
        createdAgentKnowledgeBase.setAgentId(getAgentId("Test Agent 2"));
        createdAgentKnowledgeBase.setKnowledgeBaseId(getKnowledgeBaseId("Test KB 2"));
        
        AgentKnowledgeBase updatedAgentKnowledgeBase = agentKnowledgeBaseDao.update(createdAgentKnowledgeBase);
        
        // Verify the agent-knowledge base association was updated
        assertEquals("Association agent ID should be updated", Integer.valueOf(getAgentId("Test Agent 2")), updatedAgentKnowledgeBase.getAgentId());
        assertEquals("Association knowledge base ID should be updated", Integer.valueOf(getKnowledgeBaseId("Test KB 2")), updatedAgentKnowledgeBase.getKnowledgeBaseId());
        
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
        agentKnowledgeBase.setAgentId(getAgentId("Test Agent 3"));
        agentKnowledgeBase.setKnowledgeBaseId(getKnowledgeBaseId("Test KB 3"));
        
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
        List<AgentKnowledgeBase> agentKnowledgeBases = agentKnowledgeBaseDao.findByAgentId(getAgentId("Test Agent 1"));
        
        // Verify there are agent-knowledge base associations
        assertFalse("There should be associations for Test Agent 1", agentKnowledgeBases.isEmpty());
        
        // Verify all associations have the correct agent ID
        for (AgentKnowledgeBase agentKnowledgeBase : agentKnowledgeBases) {
            assertEquals("Association agent ID should match Test Agent 1", Integer.valueOf(getAgentId("Test Agent 1")), agentKnowledgeBase.getAgentId());
        }
    }
    
    /**
     * Test the findByKnowledgeBaseId method.
     */
    @Test
    public void testFindByKnowledgeBaseId() throws SQLException {
        // Find agent-knowledge base associations by knowledge base ID
        List<AgentKnowledgeBase> agentKnowledgeBases = agentKnowledgeBaseDao.findByKnowledgeBaseId(getKnowledgeBaseId("Test KB 1"));
        
        // Verify there are agent-knowledge base associations
        assertFalse("There should be associations for Test KB 1", agentKnowledgeBases.isEmpty());
        
        // Verify all associations have the correct knowledge base ID
        for (AgentKnowledgeBase agentKnowledgeBase : agentKnowledgeBases) {
            assertEquals("Association knowledge base ID should match Test KB 1", Integer.valueOf(getKnowledgeBaseId("Test KB 1")), agentKnowledgeBase.getKnowledgeBaseId());
        }
    }
    
    /**
     * Test the existsByAgentIdAndKnowledgeBaseId method.
     */
    @Test
    public void testExistsByAgentIdAndKnowledgeBaseId() throws SQLException {
        // Check if an association exists
        boolean exists = agentKnowledgeBaseDao.existsByAgentIdAndKnowledgeBaseId(getAgentId("Test Agent 1"), getKnowledgeBaseId("Test KB 1"));
        
        // Verify the association exists
        assertTrue("Association between Test Agent 1 and Test KB 1 should exist", exists);
        
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
        agentKnowledgeBase.setAgentId(getAgentId("Test Agent 3"));
        agentKnowledgeBase.setKnowledgeBaseId(getKnowledgeBaseId("Test KB 3"));
        
        AgentKnowledgeBase createdAgentKnowledgeBase = agentKnowledgeBaseDao.create(agentKnowledgeBase);
        
        // Delete associations by agent ID
        int deleted = agentKnowledgeBaseDao.deleteByAgentId(getAgentId("Test Agent 3"));
        
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
        agentKnowledgeBase.setAgentId(getAgentId("Test Agent 3"));
        agentKnowledgeBase.setKnowledgeBaseId(getKnowledgeBaseId("Test KB 3"));
        
        AgentKnowledgeBase createdAgentKnowledgeBase = agentKnowledgeBaseDao.create(agentKnowledgeBase);
        
        // Delete associations by knowledge base ID
        int deleted = agentKnowledgeBaseDao.deleteByKnowledgeBaseId(getKnowledgeBaseId("Test KB 3"));
        
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
        agentKnowledgeBase.setAgentId(getAgentId("Test Agent 3"));
        agentKnowledgeBase.setKnowledgeBaseId(getKnowledgeBaseId("Test KB 3"));
        
        AgentKnowledgeBase createdAgentKnowledgeBase = agentKnowledgeBaseDao.create(agentKnowledgeBase);
        
        // Delete the association by agent ID and knowledge base ID
        boolean deleted = agentKnowledgeBaseDao.deleteByAgentIdAndKnowledgeBaseId(getAgentId("Test Agent 3"), getKnowledgeBaseId("Test KB 3"));
        
        // Verify the association was deleted
        assertTrue("Association should be deleted successfully", deleted);
        
        Optional<AgentKnowledgeBase> foundAgentKnowledgeBase = agentKnowledgeBaseDao.findById(createdAgentKnowledgeBase.getId());
        assertFalse("Association should not be found after deletion", foundAgentKnowledgeBase.isPresent());
    }
    
    /**
     * Helper method to get the ID of an agent by name.
     * 
     * @param name The name of the agent
     * @return The ID of the agent
     * @throws SQLException if a database error occurs
     */
    private int getAgentId(String name) throws SQLException {
        String sql = "SELECT id FROM agents WHERE name = ?";
        return executeQueryForInt(sql, ps -> ps.setString(1, name));
    }
    
    /**
     * Helper method to get the ID of a knowledge base by name.
     * 
     * @param name The name of the knowledge base
     * @return The ID of the knowledge base
     * @throws SQLException if a database error occurs
     */
    private int getKnowledgeBaseId(String name) throws SQLException {
        String sql = "SELECT id FROM knowledge_bases WHERE name = ?";
        return executeQueryForInt(sql, ps -> ps.setString(1, name));
    }
    
    /**
     * Execute a query and return the first integer result.
     * 
     * @param sql The SQL query to execute
     * @param paramSetter A functional interface to set parameters on the PreparedStatement
     * @return The integer result
     * @throws SQLException if a database error occurs
     */
    private int executeQueryForInt(String sql, PreparedStatementSetter paramSetter) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (paramSetter != null) {
                paramSetter.setParameters(ps);
            }
            
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("No results found for query: " + sql);
                }
            }
        }
    }
    
    @FunctionalInterface
    private interface PreparedStatementSetter {
        void setParameters(PreparedStatement ps) throws SQLException;
    }
}