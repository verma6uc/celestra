package com.celestra.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Represents a type of knowledge source that agents can use.
 * Maps to the knowledge_types table in the database.
 */
public class KnowledgeType {
    private Integer id;
    private String name;
    private String description;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public KnowledgeType() {
        // Default constructor
    }
    
    /**
     * Parameterized constructor with required fields
     * 
     * @param name The name of the knowledge type
     */
    public KnowledgeType(String name) {
        this.name = name;
    }
    
    /**
     * Full parameterized constructor
     * 
     * @param id The knowledge type ID
     * @param name The name of the knowledge type
     * @param description The description of the knowledge type
     * @param createdAt The creation timestamp
     * @param updatedAt The last update timestamp
     */
    public KnowledgeType(Integer id, String name, String description, 
                        OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeType that = (KnowledgeType) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "KnowledgeType{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", description='" + (description != null ? description.substring(0, Math.min(description.length(), 30)) + "..." : null) + '\'' +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}