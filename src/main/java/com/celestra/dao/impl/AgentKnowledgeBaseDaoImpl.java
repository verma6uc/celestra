package com.celestra.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import com.celestra.dao.AbstractBaseDao;
import com.celestra.dao.AgentKnowledgeBaseDao;
import com.celestra.db.DatabaseUtil;
import com.celestra.model.AgentKnowledgeBase;

/**
 * Implementation of the AgentKnowledgeBaseDao interface.
 */
public class AgentKnowledgeBaseDaoImpl extends AbstractBaseDao<AgentKnowledgeBase, Integer> implements AgentKnowledgeBaseDao {
    
    private static final String TABLE_NAME = "agent_knowledge_bases";
    private static final String ID_COLUMN = "id";
    private static final String AGENT_ID_COLUMN = "agent_id";
    private static final String KNOWLEDGE_BASE_ID_COLUMN = "knowledge_base_id";
    private static final String CREATED_AT_COLUMN = "created_at";
    
    private static final String INSERT_SQL = 
            "INSERT INTO " + TABLE_NAME + " (" + 
            AGENT_ID_COLUMN + ", " + 
            KNOWLEDGE_BASE_ID_COLUMN + ", " + 
            CREATED_AT_COLUMN + 
            ") VALUES (?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            AGENT_ID_COLUMN + " = ?, " + 
            KNOWLEDGE_BASE_ID_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String FIND_BY_AGENT_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + AGENT_ID_COLUMN + " = ?";
    
    private static final String FIND_BY_KNOWLEDGE_BASE_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + KNOWLEDGE_BASE_ID_COLUMN + " = ?";
    
    private static final String EXISTS_BY_AGENT_ID_AND_KNOWLEDGE_BASE_ID_SQL = 
            "SELECT 1 FROM " + TABLE_NAME + " WHERE " + AGENT_ID_COLUMN + " = ? AND " + KNOWLEDGE_BASE_ID_COLUMN + " = ?";
    
    private static final String DELETE_BY_AGENT_ID_SQL = 
            "DELETE FROM " + TABLE_NAME + " WHERE " + AGENT_ID_COLUMN + " = ?";
    
    private static final String DELETE_BY_KNOWLEDGE_BASE_ID_SQL = 
            "DELETE FROM " + TABLE_NAME + " WHERE " + KNOWLEDGE_BASE_ID_COLUMN + " = ?";
    
    private static final String DELETE_BY_AGENT_ID_AND_KNOWLEDGE_BASE_ID_SQL = 
            "DELETE FROM " + TABLE_NAME + " WHERE " + AGENT_ID_COLUMN + " = ? AND " + KNOWLEDGE_BASE_ID_COLUMN + " = ?";
    
    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }
    
    @Override
    protected String getIdColumnName() {
        return ID_COLUMN;
    }
    
    @Override
    protected String getInsertSql() {
        return INSERT_SQL;
    }
    
    @Override
    protected String getUpdateSql() {
        return UPDATE_SQL;
    }
    
    @Override
    protected AgentKnowledgeBase mapRow(ResultSet rs) throws SQLException {
        AgentKnowledgeBase agentKnowledgeBase = new AgentKnowledgeBase();
        
        agentKnowledgeBase.setId(rs.getInt(ID_COLUMN));
        agentKnowledgeBase.setAgentId(rs.getInt(AGENT_ID_COLUMN));
        agentKnowledgeBase.setKnowledgeBaseId(rs.getInt(KNOWLEDGE_BASE_ID_COLUMN));
        agentKnowledgeBase.setCreatedAt(rs.getTimestamp(CREATED_AT_COLUMN));
        
        return agentKnowledgeBase;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, AgentKnowledgeBase agentKnowledgeBase) throws SQLException {
        ps.setInt(1, agentKnowledgeBase.getAgentId());
        ps.setInt(2, agentKnowledgeBase.getKnowledgeBaseId());
        ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, AgentKnowledgeBase agentKnowledgeBase) throws SQLException {
        ps.setInt(1, agentKnowledgeBase.getAgentId());
        ps.setInt(2, agentKnowledgeBase.getKnowledgeBaseId());
        ps.setInt(3, agentKnowledgeBase.getId());
    }
    
    @Override
    public AgentKnowledgeBase create(AgentKnowledgeBase agentKnowledgeBase) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(ps, agentKnowledgeBase);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating agent-knowledge base association failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    agentKnowledgeBase.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating agent-knowledge base association failed, no ID obtained.");
                }
            }
            
            return agentKnowledgeBase;
        }
    }
    
    @Override
    public AgentKnowledgeBase update(AgentKnowledgeBase agentKnowledgeBase) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            
            setUpdateParameters(ps, agentKnowledgeBase);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating agent-knowledge base association failed, no rows affected.");
            }
            
            return agentKnowledgeBase;
        }
    }
    
    @Override
    public List<AgentKnowledgeBase> findByAgentId(Integer agentId) throws SQLException {
        return executeQuery(FIND_BY_AGENT_ID_SQL, ps -> 
            ps.setInt(1, agentId)
        );
    }
    
    @Override
    public List<AgentKnowledgeBase> findByKnowledgeBaseId(Integer knowledgeBaseId) throws SQLException {
        return executeQuery(FIND_BY_KNOWLEDGE_BASE_ID_SQL, ps -> 
            ps.setInt(1, knowledgeBaseId)
        );
    }
    
    @Override
    public boolean existsByAgentIdAndKnowledgeBaseId(Integer agentId, Integer knowledgeBaseId) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(EXISTS_BY_AGENT_ID_AND_KNOWLEDGE_BASE_ID_SQL)) {
            
            ps.setInt(1, agentId);
            ps.setInt(2, knowledgeBaseId);
            
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    @Override
    public int deleteByAgentId(Integer agentId) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_BY_AGENT_ID_SQL)) {
            
            ps.setInt(1, agentId);
            
            return ps.executeUpdate();
        }
    }
    
    @Override
    public int deleteByKnowledgeBaseId(Integer knowledgeBaseId) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_BY_KNOWLEDGE_BASE_ID_SQL)) {
            
            ps.setInt(1, knowledgeBaseId);
            
            return ps.executeUpdate();
        }
    }
    
    @Override
    public boolean deleteByAgentIdAndKnowledgeBaseId(Integer agentId, Integer knowledgeBaseId) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_BY_AGENT_ID_AND_KNOWLEDGE_BASE_ID_SQL)) {
            
            ps.setInt(1, agentId);
            ps.setInt(2, knowledgeBaseId);
            
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }
}