package com.celestra.model;

import java.time.OffsetDateTime;
import java.util.Objects;

import com.celestra.enums.CompanySize;
import com.celestra.enums.CompanyStatus;
import com.celestra.enums.CompanyVertical;

/**
 * Represents a company entity in the system.
 * Maps to the companies table in the database.
 */
public class Company {
    private Integer id;
    private String name;
    private String description;
    private CompanySize size;
    private CompanyVertical vertical;
    private CompanyStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public Company() {
        // Default constructor
    }
    
    /**
     * Parameterized constructor with required fields
     * 
     * @param name The company name
     * @param size The company size classification
     * @param vertical The company industry sector
     */
    public Company(String name, CompanySize size, CompanyVertical vertical) {
        this.name = name;
        this.size = size;
        this.vertical = vertical;
        this.status = CompanyStatus.ACTIVE; // Default status
    }
    
    /**
     * Full parameterized constructor
     * 
     * @param id The company ID
     * @param name The company name
     * @param description The company description
     * @param size The company size classification
     * @param vertical The company industry sector
     * @param status The company status
     * @param createdAt The creation timestamp
     * @param updatedAt The last update timestamp
     */
    public Company(Integer id, String name, String description, CompanySize size, 
                  CompanyVertical vertical, CompanyStatus status, 
                  OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.size = size;
        this.vertical = vertical;
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

    public CompanySize getSize() {
        return size;
    }

    public void setSize(CompanySize size) {
        this.size = size;
    }

    public CompanyVertical getVertical() {
        return vertical;
    }

    public void setVertical(CompanyVertical vertical) {
        this.vertical = vertical;
    }

    public CompanyStatus getStatus() {
        return status;
    }

    public void setStatus(CompanyStatus status) {
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        return Objects.equals(id, company.id) &&
               Objects.equals(name, company.name) &&
               size == company.size &&
               vertical == company.vertical &&
               status == company.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, size, vertical, status);
    }

    @Override
    public String toString() {
        return "Company{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", description='" + (description != null ? description.substring(0, Math.min(description.length(), 30)) + "..." : null) + '\'' +
               ", size=" + size +
               ", vertical=" + vertical +
               ", status=" + status +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}