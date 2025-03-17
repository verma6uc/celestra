package com.celestra.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Utility class for converting between Java enums and database values.
 */
public class EnumConverter {
    
    /**
     * Convert a database string value to an enum value.
     * 
     * @param <E> The enum type
     * @param enumClass The enum class
     * @param value The database value
     * @return An Optional containing the enum value if found, or empty if not found
     */
    public static <E extends Enum<E>> Optional<E> fromString(Class<E> enumClass, String value) {
        if (value == null) {
            return Optional.empty();
        }
        
        try {
            return Optional.of(Enum.valueOf(enumClass, value));
        } catch (IllegalArgumentException e) {
            // Try case-insensitive match
            return Arrays.stream(enumClass.getEnumConstants())
                    .filter(constant -> constant.name().equalsIgnoreCase(value))
                    .findFirst();
        }
    }
    
    /**
     * Convert a database integer value to an enum value.
     * 
     * @param <E> The enum type
     * @param enumClass The enum class
     * @param value The database value
     * @return An Optional containing the enum value if found, or empty if not found
     */
    public static <E extends Enum<E>> Optional<E> fromInteger(Class<E> enumClass, Integer value) {
        if (value == null) {
            return Optional.empty();
        }
        
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(constant -> constant.ordinal() == value)
                .findFirst();
    }
    
    /**
     * Get an enum value from a ResultSet by column name.
     * 
     * @param <E> The enum type
     * @param rs The ResultSet
     * @param columnName The column name
     * @param enumClass The enum class
     * @return An Optional containing the enum value if found, or empty if not found
     * @throws SQLException if a database access error occurs
     */
    public static <E extends Enum<E>> Optional<E> getEnumFromString(ResultSet rs, String columnName, Class<E> enumClass) throws SQLException {
        String value = rs.getString(columnName);
        if (rs.wasNull()) {
            return Optional.empty();
        }
        return fromString(enumClass, value);
    }
    
    /**
     * Get an enum value from a ResultSet by column name.
     * 
     * @param <E> The enum type
     * @param rs The ResultSet
     * @param columnName The column name
     * @param enumClass The enum class
     * @return An Optional containing the enum value if found, or empty if not found
     * @throws SQLException if a database access error occurs
     */
    public static <E extends Enum<E>> Optional<E> getEnumFromInteger(ResultSet rs, String columnName, Class<E> enumClass) throws SQLException {
        int value = rs.getInt(columnName);
        if (rs.wasNull()) {
            return Optional.empty();
        }
        return fromInteger(enumClass, value);
    }
    
    /**
     * Set an enum value on a PreparedStatement.
     * 
     * @param <E> The enum type
     * @param ps The PreparedStatement
     * @param parameterIndex The parameter index
     * @param value The enum value
     * @param useOrdinal Whether to use the ordinal value (true) or name (false)
     * @throws SQLException if a database access error occurs
     */
    public static <E extends Enum<E>> void setEnum(PreparedStatement ps, int parameterIndex, E value, boolean useOrdinal) throws SQLException {
        if (value == null) {
            ps.setNull(parameterIndex, useOrdinal ? java.sql.Types.INTEGER : java.sql.Types.VARCHAR);
        } else if (useOrdinal) {
            ps.setInt(parameterIndex, value.ordinal());
        } else {
            ps.setString(parameterIndex, value.name());
        }
    }
    
    /**
     * Set an enum value on a PreparedStatement using the enum name.
     * 
     * @param <E> The enum type
     * @param ps The PreparedStatement
     * @param parameterIndex The parameter index
     * @param value The enum value
     * @throws SQLException if a database access error occurs
     */
    public static <E extends Enum<E>> void setEnumAsString(PreparedStatement ps, int parameterIndex, E value) throws SQLException {
        setEnum(ps, parameterIndex, value, false);
    }
    
    /**
     * Set an enum value on a PreparedStatement using the enum ordinal.
     * 
     * @param <E> The enum type
     * @param ps The PreparedStatement
     * @param parameterIndex The parameter index
     * @param value The enum value
     * @throws SQLException if a database access error occurs
     */
    public static <E extends Enum<E>> void setEnumAsInteger(PreparedStatement ps, int parameterIndex, E value) throws SQLException {
        setEnum(ps, parameterIndex, value, true);
    }
}