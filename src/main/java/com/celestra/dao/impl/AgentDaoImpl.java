package com.celestra.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import com.celestra.dao.AbstractBaseDao;
import com.celestra.dao.AgentDao;
import com.celestra.dao.EnumConverter;
import com.celestra.db.DatabaseUtil;
import com.celestra.enums.AgentStatus;
import com.celestra.model.Agent;

/**
 * Implementation of the AgentDao interface.
 */
public class AgentDaoImpl extends AbstractBaseDao<Agent, Integer> implements AgentDao {
    
    private static final String TABLE_NAME = "agents";
    private static final String ID_COLUMN = "id";
    private static final String COMPANY_ID_COLUMN = "company_id";
    private static final String NAME_COLUMN = "name";
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String AGENT_PROTOCOL_COLUMN = "agent_protocol";
    private static final String STATUS_COLUMN = "status";
    private static final String CREATED_AT_COLUMN = "created_at";
    private static final String UPDATED_AT_COLUMN = "updated_at";
    
    private static final String INSERT_SQL = 
            "INSERT INTO " + TABLE_NAME + " (" + 
            COMPANY_ID_COLUMN + ", " + 
            NAME_COLUMN + ", " + 
            DESCRIPTION_COLUMN + ", " + 
            AGENT_PROTOCOL_COLUMN + ", " + 
            STATUS_COLUMN + ", " + 
            CREATED_AT_COLUMN + ", " + 
            UPDATED_AT_COLUMN + 
            ") VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            COMPANY_ID_COLUMN + " = ?, " + 
            NAME_COLUMN + " = ?, " + 
            DESCRIPTION_COLUMN + " = ?, " + 
            AGENT_PROTOCOL_COLUMN + " = ?, " + 
            STATUS_COLUMN + " = ?, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String FIND_BY_COMPANY_ID_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + COMPANY_ID_COLUMN + " = ?";
    
    private static final String FIND_BY_STATUS_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COLUMN + " = ?";
    
    private static final String FIND_BY_COMPANY_ID_AND_STATUS_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + COMPANY_ID_COLUMN + " = ? AND " + STATUS_COLUMN + " = ?";
    
    private static final String FIND_BY_NAME_CONTAINING_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + NAME_COLUMN + " LIKE ?";
    
    private static final String UPDATE_STATUS_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            STATUS_COLUMN + " = ?, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String UPDATE_AGENT_PROTOCOL_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            AGENT_PROTOCOL_COLUMN + " = ?, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
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
    protected Agent mapRow(ResultSet rs) throws SQLException {
        Agent agent = new Agent();
        
        agent.setId(rs.getInt(ID_COLUMN));
        agent.setCompanyId(rs.getInt(COMPANY_ID_COLUMN));
        agent.setName(rs.getString(NAME_COLUMN));
        agent.setDescription(rs.getString(DESCRIPTION_COLUMN));
        agent.setAgentProtocol(rs.getString(AGENT_PROTOCOL_COLUMN));
        
        EnumConverter.getEnumFromString(rs, STATUS_COLUMN, AgentStatus.class)
                .ifPresent(agent::setStatus);
        
        agent.setCreatedAt(rs.getTimestamp(CREATED_AT_COLUMN));
        agent.setUpdatedAt(rs.getTimestamp(UPDATED_AT_COLUMN));
        
        return agent;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, Agent agent) throws SQLException {
        ps.setInt(1, agent.getCompanyId());
        ps.setString(2, agent.getName());
        ps.setString(3, agent.getDescription());
        ps.setString(4, agent.getAgentProtocol());
        EnumConverter.setEnumAsString(ps, 5, agent.getStatus());
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        ps.setTimestamp(6, now);
        ps.setTimestamp(7, now);
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, Agent agent) throws SQLException {
        ps.setInt(1, agent.getCompanyId());
        ps.setString(2, agent.getName());
        ps.setString(3, agent.getDescription());
        ps.setString(4, agent.getAgentProtocol());
        EnumConverter.setEnumAsString(ps, 5, agent.getStatus());
        ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
        ps.setInt(7, agent.getId());
    }
    
    @Override
    public Agent create(Agent agent) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(ps, agent);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating agent failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    agent.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating agent failed, no ID obtained.");
                }
            }
            
            return agent;
        }
    }
    
    @Override
    public Agent update(Agent agent) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            
            setUpdateParameters(ps, agent);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating agent failed, no rows affected.");
            }
            
            return agent;
        }
    }
    
    @Override
    public List<Agent> findByCompanyId(Integer companyId) throws SQLException {
        return executeQuery(FIND_BY_COMPANY_ID_SQL, ps -> 
            ps.setInt(1, companyId)
        );
    }
    
    @Override
    public List<Agent> findByStatus(AgentStatus status) throws SQLException {
        return executeQuery(FIND_BY_STATUS_SQL, ps -> 
            EnumConverter.setEnumAsString(ps, 1, status)
        );
    }
    
    @Override
    public List<Agent> findByCompanyIdAndStatus(Integer companyId, AgentStatus status) throws SQLException {
        return executeQuery(FIND_BY_COMPANY_ID_AND_STATUS_SQL, ps -> {
            ps.setInt(1, companyId);
            EnumConverter.setEnumAsString(ps, 2, status);
        });
    }
    
    @Override
    public List<Agent> findByNameContaining(String name) throws SQLException {
        return executeQuery(FIND_BY_NAME_CONTAINING_SQL, ps -> 
            ps.setString(1, "%" + name + "%")
        );
    }
    
    @Override
    public boolean updateStatus(Integer id, AgentStatus status) throws SQLException {
        return executeUpdate(UPDATE_STATUS_SQL, ps -> {
            EnumConverter.setEnumAsString(ps, 1, status);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, id);
        }) > 0;
    }
    
    @Override
    public boolean updateAgentProtocol(Integer id, String agentProtocol) throws SQLException {
        return executeUpdate(UPDATE_AGENT_PROTOCOL_SQL, ps -> {
            ps.setString(1, agentProtocol);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, id);
        }) > 0;
    }
}