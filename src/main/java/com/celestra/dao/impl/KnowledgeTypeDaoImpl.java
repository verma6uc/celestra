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
import com.celestra.dao.KnowledgeTypeDao;
import com.celestra.db.DatabaseUtil;
import com.celestra.model.KnowledgeType;

/**
 * Implementation of the KnowledgeTypeDao interface.
 */
public class KnowledgeTypeDaoImpl extends AbstractBaseDao<KnowledgeType, Integer> implements KnowledgeTypeDao {
    
    private static final String TABLE_NAME = "knowledge_types";
    private static final String ID_COLUMN = "id";
    private static final String NAME_COLUMN = "name";
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String CREATED_AT_COLUMN = "created_at";
    private static final String UPDATED_AT_COLUMN = "updated_at";
    
    private static final String INSERT_SQL = 
            "INSERT INTO " + TABLE_NAME + " (" + 
            NAME_COLUMN + ", " + 
            DESCRIPTION_COLUMN + ", " + 
            CREATED_AT_COLUMN + ", " + 
            UPDATED_AT_COLUMN + 
            ") VALUES (?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            NAME_COLUMN + " = ?, " + 
            DESCRIPTION_COLUMN + " = ?, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String FIND_BY_NAME_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + NAME_COLUMN + " = ?";
    
    private static final String FIND_BY_NAME_CONTAINING_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + NAME_COLUMN + " ILIKE ?";
    
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
    protected KnowledgeType mapRow(ResultSet rs) throws SQLException {
        KnowledgeType knowledgeType = new KnowledgeType();
        
        knowledgeType.setId(rs.getInt(ID_COLUMN));
        knowledgeType.setName(rs.getString(NAME_COLUMN));
        knowledgeType.setDescription(rs.getString(DESCRIPTION_COLUMN));
        knowledgeType.setCreatedAt(rs.getTimestamp(CREATED_AT_COLUMN));
        knowledgeType.setUpdatedAt(rs.getTimestamp(UPDATED_AT_COLUMN));
        
        return knowledgeType;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, KnowledgeType knowledgeType) throws SQLException {
        ps.setString(1, knowledgeType.getName());
        
        if (knowledgeType.getDescription() != null) {
            ps.setString(2, knowledgeType.getDescription());
        } else {
            ps.setNull(2, java.sql.Types.VARCHAR);
        }
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        ps.setTimestamp(3, now);
        ps.setTimestamp(4, now);
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, KnowledgeType knowledgeType) throws SQLException {
        ps.setString(1, knowledgeType.getName());
        
        if (knowledgeType.getDescription() != null) {
            ps.setString(2, knowledgeType.getDescription());
        } else {
            ps.setNull(2, java.sql.Types.VARCHAR);
        }
        
        ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        ps.setInt(4, knowledgeType.getId());
    }
    
    @Override
    public KnowledgeType create(KnowledgeType knowledgeType) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(ps, knowledgeType);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating knowledge type failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    knowledgeType.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating knowledge type failed, no ID obtained.");
                }
            }
            
            return knowledgeType;
        }
    }
    
    @Override
    public KnowledgeType update(KnowledgeType knowledgeType) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            
            setUpdateParameters(ps, knowledgeType);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating knowledge type failed, no rows affected.");
            }
            
            return knowledgeType;
        }
    }
    
    @Override
    public Optional<KnowledgeType> findByName(String name) throws SQLException {
        return executeQueryForObject(FIND_BY_NAME_SQL, ps -> 
            ps.setString(1, name)
        );
    }
    
    @Override
    public List<KnowledgeType> findByNameContaining(String namePattern) throws SQLException {
        return executeQuery(FIND_BY_NAME_CONTAINING_SQL, ps -> 
            ps.setString(1, "%" + namePattern + "%")
        );
    }
}