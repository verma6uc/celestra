package com.celestra.model;

import java.time.OffsetDateTime;
import java.util.Objects;

import com.celestra.enums.AgentStatus;

/**
 * Represents an AI agent configured for a company.
 * Maps to the agents table in the database.
 */
public class Agent {
    private Integer id;
    private Integer companyId;
    private String name;
    private String description;
    private String agentProtocol;
    private AgentStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    // Reference to the associated company (not stored in database)
    private Company company;
    
    /**
     * Default constructor
     */
    public Agent() {
        // Default constructor
    }
    
    /**
     * Parameterized constructor with required fields
     * 
     * @param companyId The ID of the company that owns this agent
     * @param name The display name of the agent
     */
    public Agent(Integer companyId, String name) {
        this.companyId = companyId;
        this.name = name;
        this.status = AgentStatus.DRAFT; // Default status
    }
    
    /**
     * Full parameterized constructor
     * 
     * @param id The agent ID
     * @param companyId The ID of the company that owns this agent
     * @param name The display name of the agent
     * @param description The description of the agent
     * @param agentProtocol The protocol configuration for the agent
     * @param status The operational status of the agent
     * @param createdAt The creation timestamp
     * @param updatedAt The last update timestamp
     */
    public Agent(Integer id, Integer companyId, String name, String description, 
                String agentProtocol, AgentStatus status, 
                OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
        this.description = description;
        this.agentProtocol = agentProtocol;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAgentProtocol() {
        return agentProtocol;
    }

    public void setAgentProtocol(String agentProtocol) {
        this.agentProtocol = agentProtocol;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public void setStatus(AgentStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
        if (company != null) {
            this.companyId = company.getId();
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agent agent = (Agent) o;
        return Objects.equals(id, agent.id) &&
               Objects.equals(companyId, agent.companyId) &&
               Objects.equals(name, agent.name) &&
               status == agent.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, companyId, name, status);
    }

    @Override
    public String toString() {
        return "Agent{" +
               "id=" + id +
               ", companyId=" + companyId +
               ", name='" + name + '\'' +
               ", description='" + (description != null ? description.substring(0, Math.min(description.length(), 30)) + "..." : null) + '\'' +
               ", status=" + status +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}