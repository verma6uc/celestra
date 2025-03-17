package com.celestra.dao.impl;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.celestra.dao.BaseDaoTest;
import com.celestra.dao.CompanyDao;
import com.celestra.enums.CompanySize;
import com.celestra.enums.CompanyStatus;
import com.celestra.enums.CompanyVertical;
import com.celestra.model.Company;

/**
 * Test class for CompanyDaoImpl.
 */
public class CompanyDaoImplTest extends BaseDaoTest {
    
    private CompanyDao companyDao;
    
    /**
     * Initialize the DAO before each test.
     */
    @Before
    public void initialize() {
        companyDao = new CompanyDaoImpl();
    }
    
    @Override
    protected void createTestTables() throws SQLException {
        // Companies table should already exist in the database
    }
    
    @Override
    protected void insertTestData() throws SQLException {
        // Clean up any existing test data
        cleanupTestData();
        
        // Insert test data
        executeSQL("INSERT INTO companies (name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES ('Test Company 1', 'Test Description 1', 'SMALL', 'TECH', 'ACTIVE', NOW(), NOW())");
        
        executeSQL("INSERT INTO companies (name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES ('Test Company 2', 'Test Description 2', 'MEDIUM', 'TECH', 'SUSPENDED', NOW(), NOW())");
        
        executeSQL("INSERT INTO companies (name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES ('Another Company', 'Another Description', 'LARGE', 'FINANCE', 'ACTIVE', NOW(), NOW())");
    }
    
    @Override
    protected void cleanupTestData() throws SQLException {
        executeSQL("DELETE FROM companies WHERE name LIKE 'Test Company%' OR name = 'Another Company'");
    }
    
    /**
     * Test the create method.
     */
    @Test
    public void testCreate() throws SQLException {
        // Create a new company
        Company company = new Company();
        company.setName("Test Company Create");
        company.setDescription("Test Description Create");
        company.setSize(CompanySize.SMALL);
        company.setVertical(CompanyVertical.PHARMACEUTICAL);
        company.setStatus(CompanyStatus.ACTIVE);
        
        Company createdCompany = companyDao.create(company);
        
        // Verify the company was created
        assertNotNull("Created company should not be null", createdCompany);
        assertTrue("Created company should have an ID", createdCompany.getId() > 0);
        
        // Clean up
        boolean deleted = companyDao.delete(createdCompany.getId());
        assertTrue("Company should be deleted successfully", deleted);
    }
    
    /**
     * Test the findById method.
     */
    @Test
    public void testFindById() throws SQLException {
        // Find all companies
        List<Company> companies = companyDao.findAll();
        
        // Verify there are companies
        assertFalse("There should be companies in the database", companies.isEmpty());
        
        // Get the first company
        Company company = companies.get(0);
        
        // Find the company by ID
        Optional<Company> foundCompany = companyDao.findById(company.getId());
        
        // Verify the company was found
        assertTrue("Company should be found by ID", foundCompany.isPresent());
        assertEquals("Found company ID should match", company.getId(), foundCompany.get().getId());
        assertEquals("Found company name should match", company.getName(), foundCompany.get().getName());
    }
    
    /**
     * Test the findAll method.
     */
    @Test
    public void testFindAll() throws SQLException {
        // Find all companies
        List<Company> companies = companyDao.findAll();
        
        // Verify there are companies
        assertFalse("There should be companies in the database", companies.isEmpty());
        assertTrue("There should be at least 3 companies", companies.size() >= 3);
    }
    
    /**
     * Test the update method.
     */
    @Test
    public void testUpdate() throws SQLException {
        // Create a new company
        Company company = new Company();
        company.setName("Test Company Update");
        company.setDescription("Test Description Update");
        company.setSize(CompanySize.SMALL);
        company.setVertical(CompanyVertical.PHARMACEUTICAL);
        company.setStatus(CompanyStatus.ACTIVE);
        
        Company createdCompany = companyDao.create(company);
        
        // Update the company
        createdCompany.setName("Test Company Updated");
        createdCompany.setDescription("Test Description Updated");
        
        Company updatedCompany = companyDao.update(createdCompany);
        
        // Verify the company was updated
        assertEquals("Company name should be updated", "Test Company Updated", updatedCompany.getName());
        assertEquals("Company description should be updated", "Test Description Updated", updatedCompany.getDescription());
        
        // Clean up
        boolean deleted = companyDao.delete(createdCompany.getId());
        assertTrue("Company should be deleted successfully", deleted);
    }
    
    /**
     * Test the delete method.
     */
    @Test
    public void testDelete() throws SQLException {
        // Create a new company
        Company company = new Company();
        company.setName("Test Company Delete");
        company.setDescription("Test Description Delete");
        company.setSize(CompanySize.SMALL);
        company.setVertical(CompanyVertical.PHARMACEUTICAL);
        company.setStatus(CompanyStatus.ACTIVE);
        
        Company createdCompany = companyDao.create(company);
        
        // Delete the company
        boolean deleted = companyDao.delete(createdCompany.getId());
        
        // Verify the company was deleted
        assertTrue("Company should be deleted successfully", deleted);
        
        Optional<Company> foundCompany = companyDao.findById(createdCompany.getId());
        assertFalse("Company should not be found after deletion", foundCompany.isPresent());
    }
    
    /**
     * Test the findByStatus method.
     */
    @Test
    public void testFindByStatus() throws SQLException {
        // Find companies by status
        List<Company> companies = companyDao.findByStatus(CompanyStatus.ACTIVE);
        
        // Verify there are companies
        assertFalse("There should be active companies", companies.isEmpty());
        
        // Verify all companies have the correct status
        for (Company company : companies) {
            assertEquals("Company status should be ACTIVE", CompanyStatus.ACTIVE, company.getStatus());
        }
    }
    
    /**
     * Test the findByNameContaining method.
     */
    @Test
    public void testFindByNameContaining() throws SQLException {
        // Find companies by name
        List<Company> companies = companyDao.findByNameContaining("Test");
        
        // Verify there are companies
        assertFalse("There should be companies with 'Test' in the name", companies.isEmpty());
        
        // Verify all companies have the correct name pattern
        for (Company company : companies) {
            assertTrue("Company name should contain 'Test'", company.getName().contains("Test"));
        }
    }
    
    /**
     * Test the findByName method.
     */
    @Test
    public void testFindByName() throws SQLException {
        // Find company by exact name
        Optional<Company> company = companyDao.findByName("Test Company 1");
        
        // Verify the company was found
        assertTrue("Company should be found by exact name", company.isPresent());
        assertEquals("Found company name should match", "Test Company 1", company.get().getName());
        
        // Test with a non-existent company name
        Optional<Company> nonExistentCompany = companyDao.findByName("Non-Existent Company");
        
        // Verify the company was not found
        assertFalse("Non-existent company should not be found", nonExistentCompany.isPresent());
    }
    
    /**
     * Test the findByVertical method.
     */
    @Test
    public void testFindByVertical() throws SQLException {
        // Find companies by vertical
        List<Company> companies = companyDao.findByVertical(CompanyVertical.TECH.name());
        
        // Verify there are companies
        assertFalse("There should be TECH companies", companies.isEmpty());
        
        // Verify all companies have the correct vertical
        for (Company company : companies) {
            assertEquals("Company vertical should be TECH", CompanyVertical.TECH, company.getVertical());
        }
    }
    
    /**
     * Test the findBySize method.
     */
    @Test
    public void testFindBySize() throws SQLException {
        // Find companies by size
        List<Company> companies = companyDao.findBySize(CompanySize.SMALL.name());
        
        // Verify there are companies
        assertFalse("There should be SMALL companies", companies.isEmpty());
        
        // Verify all companies have the correct size
        for (Company company : companies) {
            assertEquals("Company size should be SMALL", CompanySize.SMALL, company.getSize());
        }
    }
    
    /**
     * Test the updateStatus method.
     */
    @Test
    public void testUpdateStatus() throws SQLException {
        // Create a new company
        Company company = new Company();
        company.setName("Test Company Status");
        company.setDescription("Test Description Status");
        company.setSize(CompanySize.SMALL);
        company.setVertical(CompanyVertical.PHARMACEUTICAL);
        company.setStatus(CompanyStatus.ACTIVE);
        
        Company createdCompany = companyDao.create(company);
        
        // Update the company status
        boolean updated = companyDao.updateStatus(createdCompany.getId(), CompanyStatus.SUSPENDED);
        
        // Verify the company status was updated
        assertTrue("Company status should be updated successfully", updated);
        
        Optional<Company> updatedCompany = companyDao.findById(createdCompany.getId());
        assertTrue("Company should be found after status update", updatedCompany.isPresent());
        assertEquals("Company status should be SUSPENDED", CompanyStatus.SUSPENDED, updatedCompany.get().getStatus());
        
        // Clean up
        boolean deleted = companyDao.delete(createdCompany.getId());
        assertTrue("Company should be deleted successfully", deleted);
    }
}