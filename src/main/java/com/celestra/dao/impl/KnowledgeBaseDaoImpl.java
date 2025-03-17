package com.celestra.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import com.celestra.dao.AbstractBaseDao;
import com.celestra.dao.EnumConverter;
import com.celestra.dao.KnowledgeBaseDao;
import com.celestra.db.DatabaseUtil;
import com.celestra.enums.KnowledgeBaseStatus;
import com.celestra.model.KnowledgeBase;

/**
 * Implementation of the KnowledgeBaseDao interface.
 */
public class KnowledgeBaseDaoImpl extends AbstractBaseDao<KnowledgeBase, Integer> implements KnowledgeBaseDao {
    
    private static final String TABLE_NAME = "knowledge_bases";
    private static final String ID_COLUMN = "id";
    private static final String COMPANY_ID_COLUMN = "company_id";
    private static final String NAME_COLUMN = "name";
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String STATUS_COLUMN = "status";
    private static final String CREATED_AT_COLUMN = "created_at";
    private static final String UPDATED_AT_COLUMN = "updated_at";
    
    private static final String AGENT_KNOWLEDGE_BASES_TABLE = "agent_knowledge_bases";
    private static final String AGENT_ID_COLUMN = "agent_id";
    private static final String KNOWLEDGE_BASE_ID_COLUMN = "knowledge_base_id";
    
    private static final String INSERT_SQL = 
            "INSERT INTO " + TABLE_NAME + " (" + 
            COMPANY_ID_COLUMN + ", " + 
            NAME_COLUMN + ", " + 
            DESCRIPTION_COLUMN + ", " + 
            STATUS_COLUMN + ", " + 
            CREATED_AT_COLUMN + ", " + 
            UPDATED_AT_COLUMN + 
            ") VALUES (?, ?, ?, ?::knowledge_base_status, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            COMPANY_ID_COLUMN + " = ?, " + 
            NAME_COLUMN + " = ?, " + 
            DESCRIPTION_COLUMN + " = ?, " + 
            STATUS_COLUMN + " = ?::knowledge_base_status, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String FIND_BY_COMPANY_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + COMPANY_ID_COLUMN + " = ?";
    
    private static final String FIND_BY_STATUS_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COLUMN + " = ?::knowledge_base_status";
    
    private static final String FIND_BY_COMPANY_ID_AND_STATUS_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + COMPANY_ID_COLUMN + " = ? AND " + STATUS_COLUMN + " = ?::knowledge_base_status";
    
    private static final String FIND_BY_NAME_CONTAINING_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + NAME_COLUMN + " LIKE ?";
    
    private static final String FIND_BY_AGENT_ID_SQL = 
            "SELECT kb.* FROM " + TABLE_NAME + " kb " +
            "JOIN " + AGENT_KNOWLEDGE_BASES_TABLE + " akb ON kb." + ID_COLUMN + " = akb." + KNOWLEDGE_BASE_ID_COLUMN + " " +
            "WHERE akb." + AGENT_ID_COLUMN + " = ?";
    
    private static final String UPDATE_STATUS_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            STATUS_COLUMN + " = ?::knowledge_base_status, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
            
    private static final String FIND_BY_COMPANY_NAME_SQL = 
            "SELECT kb.* FROM " + TABLE_NAME + " kb " +
            "JOIN companies c ON kb." + COMPANY_ID_COLUMN + " = c.id " +
            "WHERE c.name = ?";
            
    private static final String FIND_BY_AGENT_NAME_SQL = 
            "SELECT kb.* FROM " + TABLE_NAME + " kb " +
            "JOIN " + AGENT_KNOWLEDGE_BASES_TABLE + " akb ON kb." + ID_COLUMN + " = akb." + KNOWLEDGE_BASE_ID_COLUMN + " " +
            "JOIN agents a ON akb." + AGENT_ID_COLUMN + " = a.id " +
            "WHERE a.name = ?";
    
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
    protected KnowledgeBase mapRow(ResultSet rs) throws SQLException {
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        
        knowledgeBase.setId(rs.getInt(ID_COLUMN));
        knowledgeBase.setCompanyId(rs.getInt(COMPANY_ID_COLUMN));
        knowledgeBase.setName(rs.getString(NAME_COLUMN));
        knowledgeBase.setDescription(rs.getString(DESCRIPTION_COLUMN));
        
        EnumConverter.getEnumFromString(rs, STATUS_COLUMN, KnowledgeBaseStatus.class)
                .ifPresent(knowledgeBase::setStatus);
        
        knowledgeBase.setCreatedAt(rs.getTimestamp(CREATED_AT_COLUMN));
        knowledgeBase.setUpdatedAt(rs.getTimestamp(UPDATED_AT_COLUMN));
        
        return knowledgeBase;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, KnowledgeBase knowledgeBase) throws SQLException {
        ps.setInt(1, knowledgeBase.getCompanyId());
        ps.setString(2, knowledgeBase.getName());
        ps.setString(3, knowledgeBase.getDescription());
        EnumConverter.setEnumAsString(ps, 4, knowledgeBase.getStatus());
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        ps.setTimestamp(5, now);
        ps.setTimestamp(6, now);
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, KnowledgeBase knowledgeBase) throws SQLException {
        ps.setInt(1, knowledgeBase.getCompanyId());
        ps.setString(2, knowledgeBase.getName());
        ps.setString(3, knowledgeBase.getDescription());
        EnumConverter.setEnumAsString(ps, 4, knowledgeBase.getStatus());
        ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
        ps.setInt(6, knowledgeBase.getId());
    }
    
    @Override
    public KnowledgeBase create(KnowledgeBase knowledgeBase) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(ps, knowledgeBase);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating knowledge base failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    knowledgeBase.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating knowledge base failed, no ID obtained.");
                }
            }
            
            return knowledgeBase;
        }
    }
    
    @Override
    public KnowledgeBase update(KnowledgeBase knowledgeBase) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            
            setUpdateParameters(ps, knowledgeBase);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating knowledge base failed, no rows affected.");
            }
            
            return knowledgeBase;
        }
    }
    
    @Override
    public List<KnowledgeBase> findByCompanyId(Integer companyId) throws SQLException {
        return executeQuery(FIND_BY_COMPANY_ID_SQL, ps -> 
            ps.setInt(1, companyId)
        );
    }
    
    @Override
    public List<KnowledgeBase> findByStatus(KnowledgeBaseStatus status) throws SQLException {
        return executeQuery(FIND_BY_STATUS_SQL, ps -> 
            EnumConverter.setEnumAsString(ps, 1, status)
        );
    }
    
    @Override
    public List<KnowledgeBase> findByCompanyIdAndStatus(Integer companyId, KnowledgeBaseStatus status) throws SQLException {
        return executeQuery(FIND_BY_COMPANY_ID_AND_STATUS_SQL, ps -> {
            ps.setInt(1, companyId);
            EnumConverter.setEnumAsString(ps, 2, status);
        });
    }
    
    @Override
    public List<KnowledgeBase> findByNameContaining(String name) throws SQLException {
        return executeQuery(FIND_BY_NAME_CONTAINING_SQL, ps -> 
            ps.setString(1, "%" + name + "%")
        );
    }
    
    @Override
    public List<KnowledgeBase> findByAgentId(Integer agentId) throws SQLException {
        return executeQuery(FIND_BY_AGENT_ID_SQL, ps -> 
            ps.setInt(1, agentId)
        );
    }
    
    @Override
    public boolean updateStatus(Integer id, KnowledgeBaseStatus status) throws SQLException {
        return executeUpdate(UPDATE_STATUS_SQL, ps -> {
            EnumConverter.setEnumAsString(ps, 1, status);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, id);
        }) > 0;
    }
    
    @Override
    public List<KnowledgeBase> findByCompanyName(String companyName) throws SQLException {
        return executeQuery(FIND_BY_COMPANY_NAME_SQL, ps -> 
            ps.setString(1, companyName)
        );
    }
    
    @Override
    public List<KnowledgeBase> findByAgentName(String agentName) throws SQLException {
        return executeQuery(FIND_BY_AGENT_NAME_SQL, ps -> 
            ps.setString(1, agentName)
        );
    }
    
    
    
    
}