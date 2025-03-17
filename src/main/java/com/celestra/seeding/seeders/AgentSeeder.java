package com.celestra.seeding.seeders;

import com.celestra.enums.AgentStatus;
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
 * Seeder class for the agents table.
 * This class is responsible for generating and inserting test data for agents.
 */
public class AgentSeeder {
    
    private static final Logger LOGGER = Logger.getLogger(AgentSeeder.class.getName());
    
    private static final String INSERT_AGENT_SQL = 
            "INSERT INTO agents (company_id, name, description, agent_protocol, status, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?::agent_status, ?, ?)";
    
    // Agent status distribution (ACTIVE, DISABLED, ARCHIVED, DRAFT)
    private static final double[] AGENT_STATUS_DISTRIBUTION = {0.6, 0.15, 0.05, 0.2};
    
    // Predefined agent protocols for different agent types
    private static final Map<String, String> AGENT_PROTOCOLS = new HashMap<>();
    static {
        AGENT_PROTOCOLS.put("Customer Support Agent", 
                "{\n" +
                "  \"name\": \"Customer Support Agent\",\n" +
                "  \"description\": \"Handles customer inquiries and support requests\",\n" +
                "  \"capabilities\": [\"answer_questions\", \"troubleshoot_issues\", \"escalate_tickets\"],\n" +
                "  \"response_format\": \"conversational\",\n" +
                "  \"knowledge_base_requirements\": [\"product_documentation\", \"faq\", \"troubleshooting_guides\"]\n" +
                "}");
        
        AGENT_PROTOCOLS.put("Data Analysis Agent", 
                "{\n" +
                "  \"name\": \"Data Analysis Agent\",\n" +
                "  \"description\": \"Analyzes data and generates insights\",\n" +
                "  \"capabilities\": [\"data_processing\", \"statistical_analysis\", \"report_generation\"],\n" +
                "  \"response_format\": \"structured\",\n" +
                "  \"knowledge_base_requirements\": [\"data_schemas\", \"analysis_methods\", \"reporting_templates\"]\n" +
                "}");
        
        AGENT_PROTOCOLS.put("Deviation Investigation Agent", 
                "{\n" +
                "  \"name\": \"Deviation Investigation Agent\",\n" +
                "  \"description\": \"Investigates manufacturing deviations and quality issues\",\n" +
                "  \"capabilities\": [\"deviation_classification\", \"root_cause_analysis\", \"corrective_action_recommendation\"],\n" +
                "  \"response_format\": \"structured\",\n" +
                "  \"knowledge_base_requirements\": [\"manufacturing_processes\", \"quality_standards\", \"historical_deviations\"]\n" +
                "}");
        
        AGENT_PROTOCOLS.put("Document Processing Agent", 
                "{\n" +
                "  \"name\": \"Document Processing Agent\",\n" +
                "  \"description\": \"Processes and extracts information from documents\",\n" +
                "  \"capabilities\": [\"document_parsing\", \"information_extraction\", \"document_classification\"],\n" +
                "  \"response_format\": \"structured\",\n" +
                "  \"knowledge_base_requirements\": [\"document_templates\", \"extraction_rules\", \"classification_criteria\"]\n" +
                "}");
        
        AGENT_PROTOCOLS.put("Research Assistant Agent", 
                "{\n" +
                "  \"name\": \"Research Assistant Agent\",\n" +
                "  \"description\": \"Assists with research tasks and information gathering\",\n" +
                "  \"capabilities\": [\"information_search\", \"literature_review\", \"summarization\"],\n" +
                "  \"response_format\": \"conversational\",\n" +
                "  \"knowledge_base_requirements\": [\"research_databases\", \"academic_papers\", \"search_methodologies\"]\n" +
                "}");
    }
    
    // Agent types by company vertical
    private static final Map<String, List<String>> AGENT_TYPES_BY_VERTICAL = new HashMap<>();
    static {
        AGENT_TYPES_BY_VERTICAL.put("TECH", List.of(
                "Customer Support Agent", 
                "Data Analysis Agent", 
                "Document Processing Agent", 
                "Research Assistant Agent"));
        
        AGENT_TYPES_BY_VERTICAL.put("PHARMACEUTICAL", List.of(
                "Deviation Investigation Agent", 
                "Data Analysis Agent", 
                "Document Processing Agent", 
                "Research Assistant Agent"));
        
        AGENT_TYPES_BY_VERTICAL.put("FINANCE", List.of(
                "Customer Support Agent", 
                "Data Analysis Agent", 
                "Document Processing Agent"));
        
        AGENT_TYPES_BY_VERTICAL.put("RETAIL", List.of(
                "Customer Support Agent", 
                "Data Analysis Agent"));
        
        AGENT_TYPES_BY_VERTICAL.put("OTHER", List.of(
                "Customer Support Agent", 
                "Document Processing Agent"));
    }
    
    private final Connection connection;
    private final int numAgents;
    private final List<Integer> companyIds;
    private final Map<Integer, String> companyVerticals;
    
    /**
     * Constructor for AgentSeeder.
     * 
     * @param connection Database connection
     * @param numAgents Number of agents to seed
     * @param companyIds List of company IDs to associate agents with
     * @param companyVerticals Map of company IDs to their verticals
     */
    public AgentSeeder(Connection connection, int numAgents, List<Integer> companyIds, Map<Integer, String> companyVerticals) {
        this.connection = connection;
        this.numAgents = numAgents;
        this.companyIds = companyIds;
        this.companyVerticals = companyVerticals;
    }
    
    /**
     * Seed the agents table with test data.
     * 
     * @return List of generated agent IDs
     * @throws SQLException If a database error occurs
     */
    public List<Integer> seed() throws SQLException {
        LOGGER.info("Seeding agents table with " + numAgents + " records...");
        
        if (companyIds.isEmpty()) {
            LOGGER.warning("No company IDs provided. Cannot seed agents.");
            return List.of();
        }
        
        List<Integer> agentIds = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_AGENT_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            // Normalize the distribution weights
            double[] agentStatusWeights = EnumUtil.createNormalizedWeights(AgentStatus.class, AGENT_STATUS_DISTRIBUTION);
            
            // Distribute agents across companies
            Map<Integer, Integer> agentsPerCompany = distributeAgentsAcrossCompanies();
            
            for (Map.Entry<Integer, Integer> entry : agentsPerCompany.entrySet()) {
                Integer companyId = entry.getKey();
                Integer numAgentsForCompany = entry.getValue();
                
                // Get the company's vertical
                String vertical = companyVerticals.getOrDefault(companyId, "OTHER");
                
                // Get the list of agent types for this vertical
                List<String> agentTypes = AGENT_TYPES_BY_VERTICAL.getOrDefault(vertical, 
                        AGENT_TYPES_BY_VERTICAL.get("OTHER"));
                
                for (int i = 0; i < numAgentsForCompany; i++) {
                    // Select an agent type for this company
                    String agentType = agentTypes.get(i % agentTypes.size());
                    
                    // Generate agent data
                    String name = agentType + " " + FakerUtil.getFaker().number().digits(3);
                    String description = "A " + agentType.toLowerCase() + " for " + 
                            FakerUtil.getFaker().company().catchPhrase();
                    String agentProtocol = AGENT_PROTOCOLS.get(agentType);
                    
                    // For pharmaceutical companies, ensure at least one Deviation Investigation Agent is ACTIVE
                    AgentStatus status;
                    if (vertical.equals("PHARMACEUTICAL") && agentType.equals("Deviation Investigation Agent") && i == 0) {
                        status = AgentStatus.ACTIVE;
                    } else {
                        status = EnumUtil.getWeightedRandomEnumValue(AgentStatus.class, agentStatusWeights);
                    }
                    
                    // Generate timestamps
                    Timestamp[] timestamps = TimestampUtil.getCreatedUpdatedTimestamps(365, 30, 1440);
                    Timestamp createdAt = timestamps[0];
                    Timestamp updatedAt = timestamps[1];
                    
                    // Set parameters
                    statement.setInt(1, companyId);
                    statement.setString(2, name);
                    statement.setString(3, description);
                    statement.setString(4, agentProtocol);
                    statement.setString(5, status.name());
                    statement.setTimestamp(6, createdAt);
                    statement.setTimestamp(7, updatedAt);
                    
                    // Execute the insert
                    statement.executeUpdate();
                    
                    // Get the generated ID
                    try (var generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            agentIds.add(generatedKeys.getInt(1));
                        }
                    }
                }
            }
            
            LOGGER.info("Successfully seeded " + agentIds.size() + " agents.");
            return agentIds;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error seeding agents table", e);
            throw e;
        }
    }
    
    /**
     * Distribute agents across companies based on company size.
     * 
     * @return Map of company IDs to number of agents for that company
     */
    private Map<Integer, Integer> distributeAgentsAcrossCompanies() {
        Map<Integer, Integer> agentsPerCompany = new HashMap<>();
        
        // Ensure each company gets at least one agent
        for (Integer companyId : companyIds) {
            agentsPerCompany.put(companyId, 1);
        }
        
        // Distribute remaining agents
        int remainingAgents = numAgents - companyIds.size();
        if (remainingAgents > 0) {
            // Simple distribution: round-robin
            int companyIndex = 0;
            for (int i = 0; i < remainingAgents; i++) {
                Integer companyId = companyIds.get(companyIndex);
                agentsPerCompany.put(companyId, agentsPerCompany.get(companyId) + 1);
                companyIndex = (companyIndex + 1) % companyIds.size();
            }
        }
        
        return agentsPerCompany;
    }
}