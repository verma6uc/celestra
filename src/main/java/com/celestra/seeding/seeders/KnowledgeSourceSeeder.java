package com.celestra.seeding.seeders;

import com.celestra.dao.KnowledgeSourceDao;
import com.celestra.dao.impl.KnowledgeSourceDaoImpl;
import com.celestra.model.KnowledgeSource;
import com.celestra.seeding.util.FakerUtil;
import com.celestra.seeding.util.TimestampUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Seeder class for the knowledge_sources table.
 * This class is responsible for generating and inserting test data for knowledge sources.
 * It uses the KnowledgeSourceDao to interact with the database.
 */
public class KnowledgeSourceSeeder {
    
    private static final Logger LOGGER = Logger.getLogger(KnowledgeSourceSeeder.class.getName());
    
    // Predefined knowledge source names by knowledge type
    private static final Map<String, List<String>> KNOWLEDGE_SOURCE_NAMES_BY_TYPE = new HashMap<>();
    static {
        KNOWLEDGE_SOURCE_NAMES_BY_TYPE.put("Document", List.of(
                "Product Manual",
                "Technical Specification",
                "User Guide",
                "White Paper",
                "Research Report",
                "Standard Operating Procedure",
                "Policy Document"));
        
        KNOWLEDGE_SOURCE_NAMES_BY_TYPE.put("Database", List.of(
                "Customer Database",
                "Product Catalog",
                "Inventory Database",
                "Sales Records",
                "Employee Directory",
                "Analytics Database",
                "Historical Data"));
        
        KNOWLEDGE_SOURCE_NAMES_BY_TYPE.put("API", List.of(
                "REST API",
                "GraphQL Endpoint",
                "SOAP Service",
                "Webhook Integration",
                "Third-party API",
                "Internal Service API",
                "Data Exchange API"));
        
        KNOWLEDGE_SOURCE_NAMES_BY_TYPE.put("Website", List.of(
                "Corporate Website",
                "Knowledge Base Portal",
                "Support Forum",
                "Documentation Site",
                "Blog Articles",
                "FAQ Page",
                "Community Wiki"));
        
        KNOWLEDGE_SOURCE_NAMES_BY_TYPE.put("Image", List.of(
                "Product Images",
                "Technical Diagrams",
                "Process Flowcharts",
                "Infographics",
                "Presentation Slides",
                "Architectural Drawings",
                "Visual Guides"));
        
        KNOWLEDGE_SOURCE_NAMES_BY_TYPE.put("Video", List.of(
                "Training Videos",
                "Product Demonstrations",
                "Tutorial Series",
                "Webinar Recordings",
                "Process Walkthroughs",
                "Expert Interviews",
                "Conference Presentations"));
        
        KNOWLEDGE_SOURCE_NAMES_BY_TYPE.put("Audio", List.of(
                "Podcast Episodes",
                "Recorded Meetings",
                "Training Audio",
                "Interview Recordings",
                "Conference Calls",
                "Voice Notes",
                "Audio Guides"));
        
        KNOWLEDGE_SOURCE_NAMES_BY_TYPE.put("Code Repository", List.of(
                "GitHub Repository",
                "GitLab Project",
                "Source Code Archive",
                "Code Samples",
                "Development Wiki",
                "Code Documentation",
                "API Reference"));
        
        KNOWLEDGE_SOURCE_NAMES_BY_TYPE.put("Spreadsheet", List.of(
                "Data Analysis Spreadsheet",
                "Financial Model",
                "Project Tracker",
                "Inventory List",
                "Budget Spreadsheet",
                "Metrics Dashboard",
                "Calculation Template"));
        
        KNOWLEDGE_SOURCE_NAMES_BY_TYPE.put("Email", List.of(
                "Customer Correspondence",
                "Internal Communications",
                "Support Tickets",
                "Newsletter Archive",
                "Email Templates",
                "Announcement Archive",
                "Email Threads"));
        
        KNOWLEDGE_SOURCE_NAMES_BY_TYPE.put("Chat Log", List.of(
                "Customer Support Chats",
                "Team Discussions",
                "Slack Channel Archive",
                "Microsoft Teams Logs",
                "Chat Transcripts",
                "Instant Messaging History",
                "Group Chat Logs"));
        
        KNOWLEDGE_SOURCE_NAMES_BY_TYPE.put("Scientific Paper", List.of(
                "Research Papers",
                "Clinical Studies",
                "Technical Publications",
                "Academic Journals",
                "Scientific Reports",
                "Patent Documents",
                "Literature Reviews"));
    }
    
    private final KnowledgeSourceDao knowledgeSourceDao;
    private final int numKnowledgeSources;
    private final List<Integer> knowledgeBaseIds;
    private final List<Integer> knowledgeTypeIds;
    private final Map<Integer, String> knowledgeTypeNames;
    
    /**
     * Constructor for KnowledgeSourceSeeder.
     * 
     * @param connection Database connection
     * @param numKnowledgeSources Number of knowledge sources to seed
     * @param knowledgeBaseIds List of knowledge base IDs to associate knowledge sources with
     * @param knowledgeTypeIds List of knowledge type IDs to associate knowledge sources with
     * @param knowledgeTypeNames Map of knowledge type IDs to their names
     */
    public KnowledgeSourceSeeder(Connection connection, int numKnowledgeSources, 
                                List<Integer> knowledgeBaseIds, List<Integer> knowledgeTypeIds,
                                Map<Integer, String> knowledgeTypeNames) {
        this.knowledgeSourceDao = new KnowledgeSourceDaoImpl();
        this.numKnowledgeSources = numKnowledgeSources;
        this.knowledgeBaseIds = knowledgeBaseIds;
        this.knowledgeTypeIds = knowledgeTypeIds;
        this.knowledgeTypeNames = knowledgeTypeNames;
    }
    
    /**
     * Seed the knowledge_sources table with test data.
     * 
     * @return List of generated knowledge source IDs
     * @throws SQLException If a database error occurs
     */
    public List<Integer> seed() throws SQLException {
        LOGGER.info("Seeding knowledge_sources table with " + numKnowledgeSources + " records...");
        
        if (knowledgeBaseIds.isEmpty() || knowledgeTypeIds.isEmpty()) {
            LOGGER.warning("No knowledge base IDs or knowledge type IDs provided. Cannot seed knowledge sources.");
            return List.of();
        }
        
        List<Integer> knowledgeSourceIds = new ArrayList<>();
        
        try {
            // Distribute knowledge sources across knowledge bases
            Map<Integer, Integer> knowledgeSourcesPerBase = distributeKnowledgeSourcesAcrossKnowledgeBases();
            
            for (Map.Entry<Integer, Integer> entry : knowledgeSourcesPerBase.entrySet()) {
                Integer knowledgeBaseId = entry.getKey();
                Integer numSourcesForBase = entry.getValue();
                
                for (int i = 0; i < numSourcesForBase; i++) {
                    // Select a random knowledge type
                    Integer knowledgeTypeId = knowledgeTypeIds.get(
                            FakerUtil.generateRandomInt(0, knowledgeTypeIds.size() - 1));
                    
                    // Get the knowledge type name
                    String knowledgeTypeName = knowledgeTypeNames.getOrDefault(
                            knowledgeTypeId, "Document");
                    
                    // Get the list of source names for this type
                    List<String> sourceNames = KNOWLEDGE_SOURCE_NAMES_BY_TYPE.getOrDefault(
                            knowledgeTypeName, KNOWLEDGE_SOURCE_NAMES_BY_TYPE.get("Document"));
                    
                    // Select a source name
                    String sourceName;
                    if (i < sourceNames.size()) {
                        sourceName = sourceNames.get(i);
                    } else {
                        sourceName = sourceNames.get(FakerUtil.generateRandomInt(0, sourceNames.size() - 1)) + 
                                " " + FakerUtil.getFaker().number().digits(3);
                    }
                    
                    // Create the knowledge source
                    KnowledgeSource knowledgeSource = new KnowledgeSource();
                    knowledgeSource.setKnowledgeBaseId(knowledgeBaseId);
                    knowledgeSource.setKnowledgeTypeId(knowledgeTypeId);
                    knowledgeSource.setName(sourceName);
                    
                    // Generate timestamps
                    Timestamp[] timestamps = TimestampUtil.getCreatedUpdatedTimestamps(365, 30, 1440);
                    knowledgeSource.setCreatedAt(timestamps[0]);
                    knowledgeSource.setUpdatedAt(timestamps[1]);
                    
                    // Save the knowledge source
                    KnowledgeSource createdSource = knowledgeSourceDao.create(knowledgeSource);
                    knowledgeSourceIds.add(createdSource.getId());
                    
                    // If we've reached the target number, stop
                    if (knowledgeSourceIds.size() >= numKnowledgeSources) {
                        break;
                    }
                }
                
                // If we've reached the target number, stop
                if (knowledgeSourceIds.size() >= numKnowledgeSources) {
                    break;
                }
            }
            
            LOGGER.info("Successfully seeded " + knowledgeSourceIds.size() + " knowledge sources.");
            return knowledgeSourceIds;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error seeding knowledge_sources table", e);
            throw e;
        }
    }
    
    /**
     * Distribute knowledge sources across knowledge bases.
     * 
     * @return Map of knowledge base IDs to number of knowledge sources for that base
     */
    private Map<Integer, Integer> distributeKnowledgeSourcesAcrossKnowledgeBases() {
        Map<Integer, Integer> sourcesPerBase = new HashMap<>();
        
        // Ensure each knowledge base gets at least one knowledge source
        for (Integer baseId : knowledgeBaseIds) {
            sourcesPerBase.put(baseId, 1);
        }
        
        // Distribute remaining knowledge sources
        int remainingSources = numKnowledgeSources - knowledgeBaseIds.size();
        if (remainingSources > 0) {
            // Simple distribution: round-robin
            int baseIndex = 0;
            for (int i = 0; i < remainingSources; i++) {
                Integer baseId = knowledgeBaseIds.get(baseIndex);
                sourcesPerBase.put(baseId, sourcesPerBase.get(baseId) + 1);
                baseIndex = (baseIndex + 1) % knowledgeBaseIds.size();
            }
        }
        
        return sourcesPerBase;
    }
}