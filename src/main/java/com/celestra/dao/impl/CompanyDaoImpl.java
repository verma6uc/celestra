package com.celestra.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import com.celestra.dao.AbstractBaseDao;
import com.celestra.dao.CompanyDao;
import com.celestra.dao.EnumConverter;
import com.celestra.enums.CompanySize;
import com.celestra.enums.CompanyStatus;
import com.celestra.enums.CompanyVertical;
import com.celestra.model.Company;
import com.celestra.db.DatabaseUtil;

/**
 * Implementation of the CompanyDao interface.
 */
public class CompanyDaoImpl extends AbstractBaseDao<Company, Integer> implements CompanyDao {
    
    private static final String TABLE_NAME = "companies";
    private static final String ID_COLUMN = "id";
    private static final String NAME_COLUMN = "name";
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String SIZE_COLUMN = "size";
    private static final String VERTICAL_COLUMN = "vertical";
    private static final String STATUS_COLUMN = "status";
    private static final String CREATED_AT_COLUMN = "created_at";
    private static final String UPDATED_AT_COLUMN = "updated_at";
    
    private static final String INSERT_SQL = 
            "INSERT INTO " + TABLE_NAME + " (" + 
            NAME_COLUMN + ", " + 
            DESCRIPTION_COLUMN + ", " + 
            SIZE_COLUMN + ", " + 
            VERTICAL_COLUMN + ", " + 
            STATUS_COLUMN + ", " + 
            CREATED_AT_COLUMN + ", " + 
            UPDATED_AT_COLUMN + 
            ") VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            NAME_COLUMN + " = ?, " + 
            DESCRIPTION_COLUMN + " = ?, " + 
            SIZE_COLUMN + " = ?, " + 
            VERTICAL_COLUMN + " = ?, " + 
            STATUS_COLUMN + " = ?, " + 
            UPDATED_AT_COLUMN + " = ? " + 
            "WHERE " + ID_COLUMN + " = ?";
    
    private static final String FIND_BY_STATUS_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS_COLUMN + " = ?";
    
    private static final String FIND_BY_NAME_CONTAINING_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + NAME_COLUMN + " LIKE ?";
    
    private static final String FIND_BY_VERTICAL_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + VERTICAL_COLUMN + " = ?";
    
    private static final String FIND_BY_SIZE_SQL = 
            "SELECT * FROM " + TABLE_NAME + " WHERE " + SIZE_COLUMN + " = ?";
    
    private static final String UPDATE_STATUS_SQL = 
            "UPDATE " + TABLE_NAME + " SET " + 
            STATUS_COLUMN + " = ?, " + 
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
    protected Company mapRow(ResultSet rs) throws SQLException {
        Company company = new Company();
        
        company.setId(rs.getInt(ID_COLUMN));
        company.setName(rs.getString(NAME_COLUMN));
        company.setDescription(rs.getString(DESCRIPTION_COLUMN));
        
        EnumConverter.getEnumFromString(rs, SIZE_COLUMN, CompanySize.class)
                .ifPresent(company::setSize);
        
        EnumConverter.getEnumFromString(rs, VERTICAL_COLUMN, CompanyVertical.class)
                .ifPresent(company::setVertical);
        
        EnumConverter.getEnumFromString(rs, STATUS_COLUMN, CompanyStatus.class)
                .ifPresent(company::setStatus);
        
        company.setCreatedAt(rs.getTimestamp(CREATED_AT_COLUMN));
        company.setUpdatedAt(rs.getTimestamp(UPDATED_AT_COLUMN));
        
        return company;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement ps, Company company) throws SQLException {
        ps.setString(1, company.getName());
        ps.setString(2, company.getDescription());
        EnumConverter.setEnumAsString(ps, 3, company.getSize());
        EnumConverter.setEnumAsString(ps, 4, company.getVertical());
        EnumConverter.setEnumAsString(ps, 5, company.getStatus());
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        ps.setTimestamp(6, now);
        ps.setTimestamp(7, now);
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement ps, Company company) throws SQLException {
        ps.setString(1, company.getName());
        ps.setString(2, company.getDescription());
        EnumConverter.setEnumAsString(ps, 3, company.getSize());
        EnumConverter.setEnumAsString(ps, 4, company.getVertical());
        EnumConverter.setEnumAsString(ps, 5, company.getStatus());
        ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
        ps.setInt(7, company.getId());
    }
    
    @Override
    public Company create(Company company) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(ps, company);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating company failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    company.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating company failed, no ID obtained.");
                }
            }
            
            return company;
        }
    }
    
    @Override
    public Company update(Company company) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSql())) {
            
            setUpdateParameters(ps, company);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating company failed, no rows affected.");
            }
            
            return company;
        }
    }
    
    @Override
    public List<Company> findByStatus(CompanyStatus status) throws SQLException {
        return executeQuery(FIND_BY_STATUS_SQL, ps -> 
            EnumConverter.setEnumAsString(ps, 1, status)
        );
    }
    
    @Override
    public List<Company> findByNameContaining(String name) throws SQLException {
        return executeQuery(FIND_BY_NAME_CONTAINING_SQL, ps -> 
            ps.setString(1, "%" + name + "%")
        );
    }
    
    @Override
    public List<Company> findByVertical(String vertical) throws SQLException {
        return executeQuery(FIND_BY_VERTICAL_SQL, ps -> 
            ps.setString(1, vertical)
        );
    }
    
    @Override
    public List<Company> findBySize(String size) throws SQLException {
        return executeQuery(FIND_BY_SIZE_SQL, ps -> 
            ps.setString(1, size)
        );
    }
    
    @Override
    public boolean updateStatus(Integer id, CompanyStatus status) throws SQLException {
        return executeUpdate(UPDATE_STATUS_SQL, ps -> {
            EnumConverter.setEnumAsString(ps, 1, status);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, id);
        }) > 0;
    }
}