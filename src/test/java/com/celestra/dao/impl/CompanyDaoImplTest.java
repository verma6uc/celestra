package com.celestra.dao.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
     * Main method to run the tests.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        CompanyDaoImplTest test = new CompanyDaoImplTest();
        test.runTests();
    }
    
    /**
     * Constructor.
     */
    public CompanyDaoImplTest() {
        companyDao = new CompanyDaoImpl();
    }
    
    /**
     * Run all tests.
     */
    public void runTests() {
        try {
            setUp();
            
            testCreate();
            testFindById();
            testFindAll();
            testUpdate();
            testDelete();
            testFindByStatus();
            testFindByNameContaining();
            testFindByVertical();
            testFindBySize();
            testUpdateStatus();
            
            tearDown();
            
            System.out.println("All tests completed.");
        } catch (Exception e) {
            System.err.println("Error running tests: " + e.getMessage());
            e.printStackTrace();
        }
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
                   "VALUES ('Test Company 1', 'Test Description 1', 'SMALL', 'HEALTHCARE', 'ACTIVE', NOW(), NOW())");
        
        executeSQL("INSERT INTO companies (name, description, size, vertical, status, created_at, updated_at) " +
                   "VALUES ('Test Company 2', 'Test Description 2', 'MEDIUM', 'TECHNOLOGY', 'INACTIVE', NOW(), NOW())");
        
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
    private void testCreate() {
        try {
            // Create a new company
            Company company = new Company();
            company.setName("Test Company Create");
            company.setDescription("Test Description Create");
            company.setSize(CompanySize.SMALL);
            company.setVertical(CompanyVertical.PHARMACEUTICAL);
            company.setStatus(CompanyStatus.ACTIVE);
            
            Company createdCompany = companyDao.create(company);
            
            // Verify the company was created
            boolean success = createdCompany.getId() > 0;
            printTestResult("testCreate", success);
            
            // Clean up
            if (success) {
                companyDao.delete(createdCompany.getId());
            }
        } catch (Exception e) {
            printTestFailure("testCreate", e);
        }
    }
    
    /**
     * Test the findById method.
     */
    private void testFindById() {
        try {
            // Find all companies
            List<Company> companies = companyDao.findAll();
            
            // Verify there are companies
            if (companies.isEmpty()) {
                printTestResult("testFindById", false, "No companies found");
                return;
            }
            
            // Get the first company
            Company company = companies.get(0);
            
            // Find the company by ID
            Optional<Company> foundCompany = companyDao.findById(company.getId());
            
            // Verify the company was found
            boolean success = foundCompany.isPresent() && 
                              foundCompany.get().getId().equals(company.getId()) &&
                              foundCompany.get().getName().equals(company.getName());
            
            printTestResult("testFindById", success);
        } catch (Exception e) {
            printTestFailure("testFindById", e);
        }
    }
    
    /**
     * Test the findAll method.
     */
    private void testFindAll() {
        try {
            // Find all companies
            List<Company> companies = companyDao.findAll();
            
            // Verify there are companies
            boolean success = !companies.isEmpty();
            printTestResult("testFindAll", success, "Found " + companies.size() + " companies");
        } catch (Exception e) {
            printTestFailure("testFindAll", e);
        }
    }
    
    /**
     * Test the update method.
     */
    private void testUpdate() {
        try {
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
            boolean success = updatedCompany.getName().equals("Test Company Updated") &&
                              updatedCompany.getDescription().equals("Test Description Updated");
            
            printTestResult("testUpdate", success);
            
            // Clean up
            companyDao.delete(createdCompany.getId());
        } catch (Exception e) {
            printTestFailure("testUpdate", e);
        }
    }
    
    /**
     * Test the delete method.
     */
    private void testDelete() {
        try {
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
            boolean success = deleted && !companyDao.findById(createdCompany.getId()).isPresent();
            
            printTestResult("testDelete", success);
        } catch (Exception e) {
            printTestFailure("testDelete", e);
        }
    }
    
    /**
     * Test the findByStatus method.
     */
    private void testFindByStatus() {
        try {
            // Find companies by status
            List<Company> companies = companyDao.findByStatus(CompanyStatus.ACTIVE);
            
            // Verify there are companies
            boolean success = !companies.isEmpty();
            printTestResult("testFindByStatus", success, "Found " + companies.size() + " active companies");
        } catch (Exception e) {
            printTestFailure("testFindByStatus", e);
        }
    }
    
    /**
     * Test the findByNameContaining method.
     */
    private void testFindByNameContaining() {
        try {
            // Find companies by name
            List<Company> companies = companyDao.findByNameContaining("Test");
            
            // Verify there are companies
            boolean success = !companies.isEmpty();
            printTestResult("testFindByNameContaining", success, "Found " + companies.size() + " companies with 'Test' in the name");
        } catch (Exception e) {
            printTestFailure("testFindByNameContaining", e);
        }
    }
    
    /**
     * Test the findByVertical method.
     */
    private void testFindByVertical() {
        try {
            // Find companies by vertical
            List<Company> companies = companyDao.findByVertical(CompanyVertical.PHARMACEUTICAL.name());
            
            // Verify there are companies
            boolean success = !companies.isEmpty();
            printTestResult("testFindByVertical", success, "Found " + companies.size() + " healthcare companies");
        } catch (Exception e) {
            printTestFailure("testFindByVertical", e);
        }
    }
    
    /**
     * Test the findBySize method.
     */
    private void testFindBySize() {
        try {
            // Find companies by size
            List<Company> companies = companyDao.findBySize(CompanySize.SMALL.name());
            
            // Verify there are companies
            boolean success = !companies.isEmpty();
            printTestResult("testFindBySize", success, "Found " + companies.size() + " small companies");
        } catch (Exception e) {
            printTestFailure("testFindBySize", e);
        }
    }
    
    /**
     * Test the updateStatus method.
     */
    private void testUpdateStatus() {
        try {
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
            Optional<Company> updatedCompany = companyDao.findById(createdCompany.getId());
            boolean success = updated && 
                              updatedCompany.isPresent() && 
                              updatedCompany.get().getStatus() == CompanyStatus.SUSPENDED;
            
            printTestResult("testUpdateStatus", success);
            
            // Clean up
            companyDao.delete(createdCompany.getId());
        } catch (Exception e) {
            printTestFailure("testUpdateStatus", e);
        }
    }
}