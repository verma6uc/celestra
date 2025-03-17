package com.celestra.model;

import java.sql.Timestamp;
import java.util.Objects;

import com.celestra.enums.UserRole;
import com.celestra.enums.UserStatus;

/**
 * Represents a user account in the system.
 * Maps to the users table in the database.
 */
public class User {
    private Integer id;
    private Integer companyId;
    private UserRole role;
    private String email;
    private String name;
    private String passwordHash;
    private UserStatus status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Reference to the associated company (not stored in database)
    private Company company;
    
    /**
     * Default constructor
     */
    public User() {
        // Default constructor
    }
    
    /**
     * Parameterized constructor with required fields
     * 
     * @param role The user's role in the system
     * @param email The user's email address
     * @param name The user's full name
     * @param passwordHash The hashed password
     */
    public User(UserRole role, String email, String name, String passwordHash) {
        this.role = role;
        this.email = email;
        this.name = name;
        this.passwordHash = passwordHash;
        this.status = UserStatus.ACTIVE; // Default status
    }
    
    /**
     * Full parameterized constructor
     * 
     * @param id The user ID
     * @param companyId The ID of the associated company (null for super admins)
     * @param role The user's role in the system
     * @param email The user's email address
     * @param name The user's full name
     * @param passwordHash The hashed password
     * @param status The user's account status
     * @param createdAt The creation timestamp
     * @param updatedAt The last update timestamp
     */
    public User(Integer id, Integer companyId, UserRole role, String email, String name, 
               String passwordHash, UserStatus status, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.companyId = companyId;
        this.role = role;
        this.email = email;
        this.name = name;
        this.passwordHash = passwordHash;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
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
    
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
        if (company != null) {
            this.companyId = company.getId();
        }
    }
    
    /**
     * Checks if the user is a super admin
     * 
     * @return true if the user is a super admin, false otherwise
     */
    public boolean isSuperAdmin() {
        return role == UserRole.SUPER_ADMIN;
    }
    
    /**
     * Checks if the user is a company admin
     * 
     * @return true if the user is a company admin, false otherwise
     */
    public boolean isCompanyAdmin() {
        return role == UserRole.COMPANY_ADMIN;
    }
    
    /**
     * Checks if the user is a space admin
     * 
     * @return true if the user is a space admin, false otherwise
     */
    public boolean isSpaceAdmin() {
        return role == UserRole.SPACE_ADMIN;
    }
    
    /**
     * Checks if the user is active
     * 
     * @return true if the user is active, false otherwise
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
               Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", companyId=" + companyId +
               ", role=" + role +
               ", email='" + email + '\'' +
               ", name='" + name + '\'' +
               ", status=" + status +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}