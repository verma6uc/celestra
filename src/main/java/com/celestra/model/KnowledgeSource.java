package com.celestra.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Represents a specific source of knowledge within a knowledge base.
 * Maps to the knowledge_sources table in the database.
 */
public class KnowledgeSource {
    private Integer id;
    private Integer knowledgeBaseId;
    private Integer knowledgeTypeId;
    private String name;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // References to associated entities (not stored in database)
    private KnowledgeBase knowledgeBase;
    private KnowledgeType knowledgeType;
    
    /**
     * Default constructor
     */
    public KnowledgeSource() {
        // Default constructor
    }
    
    /**
     * Parameterized constructor with required fields
     * 
     * @param knowledgeBaseId The ID of the knowledge base this source belongs to
     * @param knowledgeTypeId The ID of the type of knowledge source
     * @param name The display name of the knowledge source
     */
    public KnowledgeSource(Integer knowledgeBaseId, Integer knowledgeTypeId, String name) {
        this.knowledgeBaseId = knowledgeBaseId;
        this.knowledgeTypeId = knowledgeTypeId;
        this.name = name;
    }
    
    /**
     * Full parameterized constructor
     * 
     * @param id The knowledge source ID
     * @param knowledgeBaseId The ID of the knowledge base this source belongs to
     * @param knowledgeTypeId The ID of the type of knowledge source
     * @param name The display name of the knowledge source
     * @param createdAt The creation timestamp
     * @param updatedAt The last update timestamp
     */
    public KnowledgeSource(Integer id, Integer knowledgeBaseId, Integer knowledgeTypeId, 
                          String name, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.knowledgeBaseId = knowledgeBaseId;
        this.knowledgeTypeId = knowledgeTypeId;
        this.name = name;
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

    public Integer getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    public void setKnowledgeBaseId(Integer knowledgeBaseId) {
        this.knowledgeBaseId = knowledgeBaseId;
    }

    public Integer getKnowledgeTypeId() {
        return knowledgeTypeId;
    }

    public void setKnowledgeTypeId(Integer knowledgeTypeId) {
        this.knowledgeTypeId = knowledgeTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
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
    
    public KnowledgeType getKnowledgeType() {
        return knowledgeType;
    }

    public void setKnowledgeType(KnowledgeType knowledgeType) {
        this.knowledgeType = knowledgeType;
        if (knowledgeType != null) {
            this.knowledgeTypeId = knowledgeType.getId();
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeSource that = (KnowledgeSource) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(knowledgeBaseId, that.knowledgeBaseId) &&
               Objects.equals(knowledgeTypeId, that.knowledgeTypeId) &&
               Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, knowledgeBaseId, knowledgeTypeId, name);
    }

    @Override
    public String toString() {
        return "KnowledgeSource{" +
               "id=" + id +
               ", knowledgeBaseId=" + knowledgeBaseId +
               ", knowledgeTypeId=" + knowledgeTypeId +
               ", name='" + name + '\'' +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}