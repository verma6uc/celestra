package com.celestra.model;

import java.time.OffsetDateTime;
import java.util.Objects;

import com.celestra.enums.KnowledgeBaseStatus;

/**
 * Represents a collection of knowledge for company agents to use.
 * Maps to the knowledge_bases table in the database.
 */
public class KnowledgeBase {
    private Integer id;
    private Integer companyId;
    private String name;
    private String description;
    private KnowledgeBaseStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    // Reference to the associated company (not stored in database)
    private Company company;
    
    /**
     * Default constructor
     */
    public KnowledgeBase() {
        // Default constructor
    }
    
    /**
     * Parameterized constructor with required fields
     * 
     * @param companyId The ID of the company that owns this knowledge base
     * @param name The display name of the knowledge base
     */
    public KnowledgeBase(Integer companyId, String name) {
        this.companyId = companyId;
        this.name = name;
        this.status = KnowledgeBaseStatus.DRAFT; // Default status
    }
    
    /**
     * Full parameterized constructor
     * 
     * @param id The knowledge base ID
     * @param companyId The ID of the company that owns this knowledge base
     * @param name The display name of the knowledge base
     * @param description The description of the knowledge base
     * @param status The operational status of the knowledge base
     * @param createdAt The creation timestamp
     * @param updatedAt The last update timestamp
     */
    public KnowledgeBase(Integer id, Integer companyId, String name, String description, 
                        KnowledgeBaseStatus status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
        this.description = description;
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

    public KnowledgeBaseStatus getStatus() {
        return status;
    }

    public void setStatus(KnowledgeBaseStatus status) {
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
        KnowledgeBase that = (KnowledgeBase) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(companyId, that.companyId) &&
               Objects.equals(name, that.name) &&
               status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, companyId, name, status);
    }

    @Override
    public String toString() {
        return "KnowledgeBase{" +
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