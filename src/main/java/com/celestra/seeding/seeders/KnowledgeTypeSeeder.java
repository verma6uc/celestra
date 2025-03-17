package com.celestra.seeding.seeders;

import com.celestra.dao.KnowledgeTypeDao;
import com.celestra.dao.impl.KnowledgeTypeDaoImpl;
import com.celestra.model.KnowledgeType;
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
 * Seeder class for the knowledge_types table.
 * This class is responsible for generating and inserting test data for knowledge types.
 */
public class KnowledgeTypeSeeder {
    
    private static final Logger LOGGER = Logger.getLogger(KnowledgeTypeSeeder.class.getName());
    
    // Predefined knowledge type names and descriptions for realistic data
    private static final String[][] PREDEFINED_KNOWLEDGE_TYPES = {
        {"Document", "Knowledge extracted from documents such as PDFs, Word files, and text documents."},
        {"Database", "Structured data stored in databases, including records, tables, and query results."},
        {"API", "Data retrieved from external APIs and web services."},
        {"Website", "Information extracted from websites and web pages."},
        {"Image", "Knowledge extracted from images, including charts, diagrams, and photographs."},
        {"Video", "Information extracted from video content, including transcripts and visual data."},
        {"Audio", "Knowledge extracted from audio files, including transcripts and spoken content."},
        {"Code Repository", "Information from code repositories, including source code and documentation."},
        {"Spreadsheet", "Data from spreadsheets and tabular data files."},
        {"Email", "Information extracted from email communications."},
        {"Chat Log", "Knowledge from chat logs and conversation histories."},
        {"Scientific Paper", "Information from scientific papers and research publications."}
    };
    
    private final Connection connection;
    private final KnowledgeTypeDao knowledgeTypeDao;
    private final int numKnowledgeTypes;
    
    /**
     * Constructor for KnowledgeTypeSeeder.
     * 
     * @param connection Database connection
     * @param numKnowledgeTypes Number of knowledge types to seed
     */
    public KnowledgeTypeSeeder(Connection connection, int numKnowledgeTypes) {
        this.connection = connection;
        this.knowledgeTypeDao = new KnowledgeTypeDaoImpl();
        this.numKnowledgeTypes = Math.min(numKnowledgeTypes, PREDEFINED_KNOWLEDGE_TYPES.length);
    }
    
    /**
     * Seed the knowledge_types table with test data.
     * 
     * @return List of generated knowledge type IDs
     * @throws SQLException If a database error occurs
     */
    public List<Integer> seed() throws SQLException {
        LOGGER.info("Seeding knowledge_types table with " + numKnowledgeTypes + " records...");
        
        List<Integer> knowledgeTypeIds = new ArrayList<>();
        
        try {
            for (int i = 0; i < numKnowledgeTypes; i++) {
                // Get predefined knowledge type data
                String name = PREDEFINED_KNOWLEDGE_TYPES[i][0];
                String description = PREDEFINED_KNOWLEDGE_TYPES[i][1];
                
                // Generate timestamps
                Timestamp[] timestamps = TimestampUtil.getCreatedUpdatedTimestamps(30, 365, 1440);
                Timestamp createdAt = timestamps[0];
                Timestamp updatedAt = timestamps[1];
                
                // Create the knowledge type object
                KnowledgeType knowledgeType = new KnowledgeType();
                knowledgeType.setName(name);
                knowledgeType.setDescription(description);
                knowledgeType.setCreatedAt(createdAt);
                knowledgeType.setUpdatedAt(updatedAt);
                
                // Save the knowledge type
                KnowledgeType createdType = knowledgeTypeDao.create(knowledgeType);
                if (createdType != null && createdType.getId() > 0) {
                    knowledgeTypeIds.add(createdType.getId());
                    }
            }
            
            LOGGER.info("Successfully seeded " + knowledgeTypeIds.size() + " knowledge types.");
            return knowledgeTypeIds;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error seeding knowledge_types table", e);
            throw e;
        }
    }
}