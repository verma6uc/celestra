package com.celestra.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import com.celestra.dao.AbstractBaseDao;
import com.celestra.dao.KnowledgeSourceDao;
import com.celestra.db.DatabaseUtil;
import com.celestra.model.KnowledgeSource;

/**
 * Implementation of the KnowledgeSourceDao interface.
 */
public class KnowledgeSourceDaoImpl extends AbstractBaseDao<KnowledgeSource, Integer> implements KnowledgeSourceDao {
    
    private static final String TABLE_NAME = "knowledge_sources";
    private static final String ID_COLUMN = "id";
    private static final String KNOWLEDGE_BASE_ID_COLUMN = "knowledge_base_id";
    private static final String KNOWLEDGE_TYPE_ID_COLUMN = "knowledge_type_id";
    private static final String NAME_COLUMN = "name";
    private static final String CREATED_AT_COLUMN = "created_at";
    private static final String UPDATED_AT_COLUMN = "updated_at";
    
    private static final String INSERT_SQL = 
            "INSERT INTO " + TABLE_NAME + " (" + 
            KNOWLEDGE_BASE_ID_COLUMN + ", " + 
            KNOWLEDGE_TYPE_ID_COLUMN + ", " + 
            NAME_COLUMN + ", " + 
            CREATED_AT_COLUMN + ", " + 
            UPDATED_AT_COLUMN + 
            ") VALUES (?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            KNOWLEDGE_BASE_ID_COLUMN + " = ?, " + 
            KNOWLEDGE_TYPE_ID_COLUMN + " = ?, " + 
            NAME_COLUMN + " = ?, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String FIND_BY_KNOWLEDGE_BASE_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + KNOWLEDGE_BASE_ID_COLUMN + " = ?";
    
    private static final String FIND_BY_KNOWLEDGE_TYPE_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + KNOWLEDGE_TYPE_ID_COLUMN + " = ?";
    
    private static final String FIND_BY_NAME_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + NAME_COLUMN + " = ?";
    
    private static final String FIND_BY_NAME_CONTAINING_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + NAME_COLUMN + " ILIKE ?";
    
    private static final String FIND_BY_KNOWLEDGE_BASE_ID_AND_KNOWLEDGE_TYPE_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + 
            KNOWLEDGE_BASE_ID_COLUMN + " = ? AND " + 
            KNOWLEDGE_TYPE_ID_COLUMN + " = ?";
    
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
    protected KnowledgeSource mapRow(ResultSet rs) throws SQLException {
        KnowledgeSource knowledgeSource = new KnowledgeSource();
        
        knowledgeSource.setId(rs.getInt(ID_COLUMN));
        knowledgeSource.setKnowledgeBaseId(rs.getInt(KNOWLEDGE_BASE_ID_COLUMN));
        knowledgeSource.setKnowledgeTypeId(rs.getInt(KNOWLEDGE_TYPE_ID_COLUMN));
        knowledgeSource.setName(rs.getString(NAME_COLUMN));
        knowledgeSource.setCreatedAt(rs.getTimestamp(CREATED_AT_COLUMN));
        knowledgeSource.setUpdatedAt(rs.getTimestamp(UPDATED_AT_COLUMN));
        
        return knowledgeSource;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, KnowledgeSource knowledgeSource) throws SQLException {
        ps.setInt(1, knowledgeSource.getKnowledgeBaseId());
        ps.setInt(2, knowledgeSource.getKnowledgeTypeId());
        ps.setString(3, knowledgeSource.getName());
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        ps.setTimestamp(4, now);
        ps.setTimestamp(5, now);
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, KnowledgeSource knowledgeSource) throws SQLException {
        ps.setInt(1, knowledgeSource.getKnowledgeBaseId());
        ps.setInt(2, knowledgeSource.getKnowledgeTypeId());
        ps.setString(3, knowledgeSource.getName());
        ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
        ps.setInt(5, knowledgeSource.getId());
    }
    
    @Override
    public KnowledgeSource create(KnowledgeSource knowledgeSource) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(ps, knowledgeSource);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating knowledge source failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    knowledgeSource.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating knowledge source failed, no ID obtained.");
                }
            }
            
            return knowledgeSource;
        }
    }
    
    @Override
    public KnowledgeSource update(KnowledgeSource knowledgeSource) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            
            setUpdateParameters(ps, knowledgeSource);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating knowledge source failed, no rows affected.");
            }
            
            return knowledgeSource;
        }
    }
    
    @Override
    public List<KnowledgeSource> findByKnowledgeBaseId(Integer knowledgeBaseId) throws SQLException {
        return executeQuery(FIND_BY_KNOWLEDGE_BASE_ID_SQL, ps -> 
            ps.setInt(1, knowledgeBaseId)
        );
    }
    
    @Override
    public List<KnowledgeSource> findByKnowledgeTypeId(Integer knowledgeTypeId) throws SQLException {
        return executeQuery(FIND_BY_KNOWLEDGE_TYPE_ID_SQL, ps -> 
            ps.setInt(1, knowledgeTypeId)
        );
    }
    
    @Override
    public Optional<KnowledgeSource> findByName(String name) throws SQLException {
        return executeQueryForObject(FIND_BY_NAME_SQL, ps -> 
            ps.setString(1, name)
        );
    }
    
    @Override
    public List<KnowledgeSource> findByNameContaining(String namePattern) throws SQLException {
        return executeQuery(FIND_BY_NAME_CONTAINING_SQL, ps -> 
            ps.setString(1, "%" + namePattern + "%")
        );
    }
    
    @Override
    public List<KnowledgeSource> findByKnowledgeBaseIdAndKnowledgeTypeId(Integer knowledgeBaseId, Integer knowledgeTypeId) throws SQLException {
        return executeQuery(FIND_BY_KNOWLEDGE_BASE_ID_AND_KNOWLEDGE_TYPE_ID_SQL, ps -> {
            ps.setInt(1, knowledgeBaseId);
            ps.setInt(2, knowledgeTypeId);
        });
    }
}