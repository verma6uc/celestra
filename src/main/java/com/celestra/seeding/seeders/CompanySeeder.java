package com.celestra.seeding.seeders;

import com.celestra.dao.CompanyDao;
import com.celestra.dao.impl.CompanyDaoImpl;
import com.celestra.model.Company;
import com.celestra.enums.CompanySize;
import com.celestra.enums.CompanyStatus;
import com.celestra.enums.CompanyVertical;
import com.celestra.seeding.util.EnumUtil;
import com.celestra.seeding.util.FakerUtil;
import com.celestra.seeding.util.TimestampUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Seeder class for the companies table.
 * This class is responsible for generating and inserting test data for companies.
 */
public class CompanySeeder {
    
    private static final Logger LOGGER = Logger.getLogger(CompanySeeder.class.getName());
    
    // Company size distribution (SMALL, MEDIUM, LARGE, ENTERPRISE)
    private static final double[] COMPANY_SIZE_DISTRIBUTION = {0.4, 0.3, 0.2, 0.1};
    
    // Company vertical distribution (TECH, PHARMACEUTICAL, FINANCE, RETAIL, OTHER)
    private static final double[] COMPANY_VERTICAL_DISTRIBUTION = {0.3, 0.2, 0.2, 0.2, 0.1};
    
    // Company status distribution (ACTIVE, SUSPENDED, ARCHIVED)
    private static final double[] COMPANY_STATUS_DISTRIBUTION = {0.8, 0.15, 0.05};
    
    private final Connection connection;
    private final CompanyDao companyDao;
    private final int numCompanies;
    
    /**
     * Constructor for CompanySeeder.
     * 
     * @param connection Database connection
     * @param numCompanies Number of companies to seed
     */
    public CompanySeeder(Connection connection, int numCompanies) {
        this.connection = connection;
        this.companyDao = new CompanyDaoImpl();
        this.numCompanies = numCompanies;
    }
    
    /**
     * Seed the companies table with test data.
     * 
     * @return List of generated company IDs
     * @throws SQLException If a database error occurs
     */
    public List<Integer> seed() throws SQLException {
        LOGGER.info("Seeding companies table with " + numCompanies + " records...");
        
        List<Integer> companyIds = new ArrayList<>();
        
        try {
            // Normalize the distribution weights
            double[] companySizeWeights = EnumUtil.createNormalizedWeights(CompanySize.class, COMPANY_SIZE_DISTRIBUTION);
            double[] companyVerticalWeights = EnumUtil.createNormalizedWeights(CompanyVertical.class, COMPANY_VERTICAL_DISTRIBUTION);
            double[] companyStatusWeights = EnumUtil.createNormalizedWeights(CompanyStatus.class, COMPANY_STATUS_DISTRIBUTION);
            
            // Create a set of pharmaceutical companies for deviation investigation
            int numPharmaceuticalCompanies = Math.min(2, numCompanies / 4);
            
            for (int i = 0; i < numCompanies; i++) {
                // Generate company data
                String name = FakerUtil.generateCompanyName();
                String description = FakerUtil.generateCompanyDescription();
                
                CompanySize size;
                CompanyVertical vertical;
                CompanyStatus status;
                
                // For the first few companies, create pharmaceutical companies
                if (i < numPharmaceuticalCompanies) {
                    size = CompanySize.LARGE;
                    vertical = CompanyVertical.PHARMACEUTICAL;
                    status = CompanyStatus.ACTIVE;
                } else {
                    // For the rest, use weighted random distribution
                    size = EnumUtil.getWeightedRandomEnumValue(CompanySize.class, companySizeWeights);
                    vertical = EnumUtil.getWeightedRandomEnumValue(CompanyVertical.class, companyVerticalWeights);
                    status = EnumUtil.getWeightedRandomEnumValue(CompanyStatus.class, companyStatusWeights);
                }
                
                // Generate timestamps
                Timestamp[] timestamps = TimestampUtil.getCreatedUpdatedTimestamps(30, 365, 1440);
                Timestamp createdAt = timestamps[0];
                Timestamp updatedAt = timestamps[1];
                
                // Create the company object
                Company company = new Company();
                company.setName(name);
                company.setDescription(description);
                company.setSize(size);
                company.setVertical(vertical);
                company.setStatus(status);
                company.setCreatedAt(createdAt);
                company.setUpdatedAt(updatedAt);
                
                // Save the company
                Company createdCompany = companyDao.create(company);
                if (createdCompany != null && createdCompany.getId() > 0) {
                    companyIds.add(createdCompany.getId());
                    }
            }
            
            LOGGER.info("Successfully seeded " + companyIds.size() + " companies.");
            return companyIds;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error seeding companies table", e);
            throw e;
        }
    }
}