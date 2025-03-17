package com.celestra.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.celestra.enums.CompanyStatus;
import com.celestra.model.Company;

/**
 * Data Access Object (DAO) interface for Company entities.
 */
public interface CompanyDao extends BaseDao<Company, Integer> {
    
    /**
     * Find companies by status.
     * 
     * @param status The company status to search for
     * @return A list of companies with the specified status
     * @throws SQLException if a database access error occurs
     */
    List<Company> findByStatus(CompanyStatus status) throws SQLException;
    
    /**
     * Find companies by name (partial match).
     * 
     * @param name The company name to search for
     * @return A list of companies with names containing the specified string
     * @throws SQLException if a database access error occurs
     */
    List<Company> findByNameContaining(String name) throws SQLException;
    
    /**
     * Find a company by exact name.
     * 
     * @param name The exact company name to search for
     * @return An Optional containing the company if found, or empty if not found
     * @throws SQLException if a database access error occurs
     */
    Optional<Company> findByName(String name) throws SQLException;
    
    /**
     * Find companies by vertical.
     * 
     * @param vertical The company vertical to search for
     * @return A list of companies with the specified vertical
     * @throws SQLException if a database access error occurs
     */
    List<Company> findByVertical(String vertical) throws SQLException;
    
    /**
     * Find companies by size.
     * 
     * @param size The company size to search for
     * @return A list of companies with the specified size
     * @throws SQLException if a database access error occurs
     */
    List<Company> findBySize(String size) throws SQLException;
    
    /**
     * Update the status of a company.
     * 
     * @param id The ID of the company to update
     * @param status The new status
     * @return true if the company was updated, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean updateStatus(Integer id, CompanyStatus status) throws SQLException;
}