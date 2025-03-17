package com.celestra.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Represents an association between an agent and a knowledge base.
 * Maps to the agent_knowledge_bases junction table in the database.
 */
public class AgentKnowledgeBase {
    private Integer id;
    private Integer agentId;
    private Integer knowledgeBaseId;
    private Timestamp createdAt;
    
    // References to associated entities (not stored in database)
    private Agent agent;
    private KnowledgeBase knowledgeBase;
    
    /**
     * Default constructor
     */
    public AgentKnowledgeBase() {
        // Default constructor
    }
    
    /**
     * Parameterized constructor with required fields
     * 
     * @param agentId The ID of the associated agent
     * @param knowledgeBaseId The ID of the associated knowledge base
     */
    public AgentKnowledgeBase(Integer agentId, Integer knowledgeBaseId) {
        this.agentId = agentId;
        this.knowledgeBaseId = knowledgeBaseId;
    }
    
    /**
     * Full parameterized constructor
     * 
     * @param id The association ID
     * @param agentId The ID of the associated agent
     * @param knowledgeBaseId The ID of the associated knowledge base
     * @param createdAt The creation timestamp
     */
    public AgentKnowledgeBase(Integer id, Integer agentId, Integer knowledgeBaseId, Timestamp createdAt) {
        this.id = id;
        this.agentId = agentId;
        this.knowledgeBaseId = knowledgeBaseId;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAgentId() {
        return agentId;
    }

    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    public Integer getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    public void setKnowledgeBaseId(Integer knowledgeBaseId) {
        this.knowledgeBaseId = knowledgeBaseId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
        if (agent != null) {
            this.agentId = agent.getId();
        }
    }
    
    public KnowledgeBase getKnowledgeBase() {
        return knowledgeBase;
    }

    public void setKnowledgeBase(KnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
        if (knowledgeBase != null) {
            this.knowledgeBaseId = knowledgeBase.getId();
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentKnowledgeBase that = (AgentKnowledgeBase) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(agentId, that.agentId) &&
               Objects.equals(knowledgeBaseId, that.knowledgeBaseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, agentId, knowledgeBaseId);
    }

    @Override
    public String toString() {
        return "AgentKnowledgeBase{" +
               "id=" + id +
               ", agentId=" + agentId +
               ", knowledgeBaseId=" + knowledgeBaseId +
               ", createdAt=" + createdAt +
               '}';
    }
}