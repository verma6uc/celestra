package com.celestra.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Base Data Access Object (DAO) interface that defines common CRUD operations.
 * 
 * @param <T> The entity type this DAO handles
 * @param <ID> The type of the entity's primary key
 */
public interface BaseDao<T, ID> {
    
    /**
     * Create a new entity in the database.
     * 
     * @param entity The entity to create
     * @return The created entity with its generated ID
     * @throws SQLException if a database access error occurs
     */
    T create(T entity) throws SQLException;
    
    /**
     * Retrieve an entity by its ID.
     * 
     * @param id The ID of the entity to retrieve
     * @return An Optional containing the entity if found, or empty if not found
     * @throws SQLException if a database access error occurs
     */
    Optional<T> findById(ID id) throws SQLException;
    
    /**
     * Retrieve all entities.
     * 
     * @return A list of all entities
     * @throws SQLException if a database access error occurs
     */
    List<T> findAll() throws SQLException;
    
    /**
     * Update an existing entity in the database.
     * 
     * @param entity The entity to update
     * @return The updated entity
     * @throws SQLException if a database access error occurs
     */
    T update(T entity) throws SQLException;
    
    /**
     * Delete an entity from the database.
     * 
     * @param id The ID of the entity to delete
     * @return true if the entity was deleted, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean delete(ID id) throws SQLException;
}