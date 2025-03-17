package com.celestra.seeding.seeders;

import com.celestra.dao.KnowledgeBaseDao;
import com.celestra.dao.impl.KnowledgeBaseDaoImpl;
import com.celestra.model.KnowledgeBase;
import com.celestra.enums.KnowledgeBaseStatus;
import com.celestra.seeding.util.EnumUtil;
import com.celestra.seeding.util.FakerUtil;
import com.celestra.seeding.util.TimestampUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Seeder class for the knowledge_bases table.
 * This class is responsible for generating and inserting test data for knowledge bases.
 */
public class KnowledgeBaseSeeder {
    
    private static final Logger LOGGER = Logger.getLogger(KnowledgeBaseSeeder.class.getName());
    
    // Knowledge base status distribution (ACTIVE, BUILDING, DISABLED, ARCHIVED, DRAFT)
    private static final double[] KNOWLEDGE_BASE_STATUS_DISTRIBUTION = {0.5, 0.3, 0.05, 0.05, 0.1};
    
    // Predefined knowledge base types for different company verticals
    private static final Map<String, List<String>> KNOWLEDGE_BASE_TYPES_BY_VERTICAL = new HashMap<>();
    static {
        KNOWLEDGE_BASE_TYPES_BY_VERTICAL.put("TECH", List.of(
                "Product Documentation",
                "Technical Support Knowledge Base",
                "Developer Resources",
                "API Documentation",
                "Customer FAQs"));
        
        KNOWLEDGE_BASE_TYPES_BY_VERTICAL.put("PHARMACEUTICAL", List.of(
                "Manufacturing Processes",
                "Quality Standards",
                "Regulatory Compliance",
                "Research Publications",
                "Clinical Trial Data",
                "Standard Operating Procedures",
                "Deviation Investigation Guidelines"));
        
        KNOWLEDGE_BASE_TYPES_BY_VERTICAL.put("FINANCE", List.of(
                "Financial Regulations",
                "Investment Guidelines",
                "Risk Assessment Procedures",
                "Customer Service Protocols",
                "Compliance Documentation"));
        
        KNOWLEDGE_BASE_TYPES_BY_VERTICAL.put("RETAIL", List.of(
                "Product Catalog",
                "Customer Service Guidelines",
                "Store Operations Manual",
                "Inventory Management Procedures",
                "Marketing Materials"));
        
        KNOWLEDGE_BASE_TYPES_BY_VERTICAL.put("OTHER", List.of(
                "Company Policies",
                "Employee Handbook",
                "Training Materials",
                "Project Documentation",
                "General Knowledge Base"));
    }
    
    private final Connection connection;
    private final int numKnowledgeBases;
    private final KnowledgeBaseDao knowledgeBaseDao;
    private final List<Integer> companyIds;
    private final Map<Integer, String> companyVerticals;
    
    /**
     * Constructor for KnowledgeBaseSeeder.
     * 
     * @param connection Database connection
     * @param numKnowledgeBases Number of knowledge bases to seed
     * @param companyIds List of company IDs to associate knowledge bases with
     * @param companyVerticals Map of company IDs to their verticals
     */
    public KnowledgeBaseSeeder(Connection connection, int numKnowledgeBases, 
                              List<Integer> companyIds, Map<Integer, String> companyVerticals) {
        this.connection = connection;
        this.numKnowledgeBases = numKnowledgeBases;
        this.companyIds = companyIds;
        this.companyVerticals = companyVerticals;
        this.knowledgeBaseDao = new KnowledgeBaseDaoImpl();
    }
    
    /**
     * Seed the knowledge_bases table with test data.
     * 
     * @return List of generated knowledge base IDs
     * @throws SQLException If a database error occurs
     */
    public List<Integer> seed() throws SQLException {
        LOGGER.info("Seeding knowledge_bases table with " + numKnowledgeBases + " records...");
        
        if (companyIds.isEmpty()) {
            LOGGER.warning("No company IDs provided. Cannot seed knowledge bases.");
            return List.of();
        }
        
        List<Integer> knowledgeBaseIds = new ArrayList<>();
        
        try {
            // Normalize the distribution weights
            double[] knowledgeBaseStatusWeights = EnumUtil.createNormalizedWeights(
                    KnowledgeBaseStatus.class, KNOWLEDGE_BASE_STATUS_DISTRIBUTION);
            
            // Distribute knowledge bases across companies
            Map<Integer, Integer> knowledgeBasesPerCompany = distributeKnowledgeBasesAcrossCompanies();
            
            for (Map.Entry<Integer, Integer> entry : knowledgeBasesPerCompany.entrySet()) {
                Integer companyId = entry.getKey();
                Integer numKnowledgeBasesForCompany = entry.getValue();
                
                // Get the company's vertical
                String vertical = companyVerticals.getOrDefault(companyId, "OTHER");
                
                // Get the list of knowledge base types for this vertical
                List<String> knowledgeBaseTypes = KNOWLEDGE_BASE_TYPES_BY_VERTICAL.getOrDefault(
                        vertical, KNOWLEDGE_BASE_TYPES_BY_VERTICAL.get("OTHER"));
                
                for (int i = 0; i < numKnowledgeBasesForCompany; i++) {
                    // Select a knowledge base type for this company
                    String knowledgeBaseType;
                    if (i < knowledgeBaseTypes.size()) {
                        knowledgeBaseType = knowledgeBaseTypes.get(i);
                    } else {
                        // If we've used all predefined types, generate a random one
                        knowledgeBaseType = knowledgeBaseTypes.get(
                                FakerUtil.generateRandomInt(0, knowledgeBaseTypes.size() - 1)) + 
                                " " + FakerUtil.getFaker().number().digits(3);
                    }
                    
                    // Generate knowledge base data
                    String name = knowledgeBaseType;
                    String description = "A knowledge base containing information about " + 
                            knowledgeBaseType.toLowerCase();
                    
                    // For pharmaceutical companies, ensure the Manufacturing Processes and 
                    // Deviation Investigation Guidelines knowledge bases are ACTIVE
                    KnowledgeBaseStatus status;
                    if (vertical.equals("PHARMACEUTICAL") && 
                            (knowledgeBaseType.equals("Manufacturing Processes") || 
                             knowledgeBaseType.equals("Deviation Investigation Guidelines"))) {
                        status = KnowledgeBaseStatus.ACTIVE;
                    } else {
                        status = EnumUtil.getWeightedRandomEnumValue(
                                KnowledgeBaseStatus.class, knowledgeBaseStatusWeights);
                    }
                    
                    // Generate timestamps
                    Timestamp[] timestamps = TimestampUtil.getCreatedUpdatedTimestamps(365, 30, 1440);
                    Timestamp createdAt = timestamps[0];
                    Timestamp updatedAt = timestamps[1];
                    
                    // Create the knowledge base object
                    KnowledgeBase knowledgeBase = new KnowledgeBase();
                    knowledgeBase.setCompanyId(companyId);
                    knowledgeBase.setName(name);
                    knowledgeBase.setDescription(description);
                    knowledgeBase.setStatus(status);
                    knowledgeBase.setCreatedAt(createdAt);
                    knowledgeBase.setUpdatedAt(updatedAt);
                    
                    // Save the knowledge base
                    KnowledgeBase createdBase = knowledgeBaseDao.create(knowledgeBase);
                    if (createdBase != null && createdBase.getId() > 0) {
                        knowledgeBaseIds.add(createdBase.getId());
                        }
                }
            }
            
            LOGGER.info("Successfully seeded " + knowledgeBaseIds.size() + " knowledge bases.");
            return knowledgeBaseIds;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error seeding knowledge_bases table", e);
            throw e;
        }
    }
    
    /**
     * Distribute knowledge bases across companies based on company size.
     * 
     * @return Map of company IDs to number of knowledge bases for that company
     */
    private Map<Integer, Integer> distributeKnowledgeBasesAcrossCompanies() {
        Map<Integer, Integer> knowledgeBasesPerCompany = new HashMap<>();
        
        // Ensure each company gets at least two knowledge bases
        for (Integer companyId : companyIds) {
            knowledgeBasesPerCompany.put(companyId, 2);
        }
        
        // Distribute remaining knowledge bases
        int remainingKnowledgeBases = numKnowledgeBases - (companyIds.size() * 2);
        if (remainingKnowledgeBases > 0) {
            // Simple distribution: round-robin
            int companyIndex = 0;
            for (int i = 0; i < remainingKnowledgeBases; i++) {
                Integer companyId = companyIds.get(companyIndex);
                knowledgeBasesPerCompany.put(companyId, knowledgeBasesPerCompany.get(companyId) + 1);
                companyIndex = (companyIndex + 1) % companyIds.size();
            }
        }
        
        return knowledgeBasesPerCompany;
    }
}