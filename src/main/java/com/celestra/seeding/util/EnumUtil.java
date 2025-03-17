package com.celestra.seeding.util;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Utility class for handling enum values during data seeding.
 * This class provides methods for selecting random enum values with various distribution patterns.
 */
public class EnumUtil {
    
    private static final Random random = new Random();
    
    /**
     * Private constructor to prevent instantiation.
     */
    private EnumUtil() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Get a random enum value from the given enum class.
     * 
     * @param <T> The enum type
     * @param enumClass The enum class
     * @return A random enum value
     */
    public static <T extends Enum<T>> T getRandomEnumValue(Class<T> enumClass) {
        T[] values = enumClass.getEnumConstants();
        return values[random.nextInt(values.length)];
    }
    
    /**
     * Get a random enum value from the given enum class with a weighted distribution.
     * 
     * @param <T> The enum type
     * @param enumClass The enum class
     * @param weights Array of weights for each enum value (must be the same length as the enum values)
     * @return A random enum value based on the weights
     */
    public static <T extends Enum<T>> T getWeightedRandomEnumValue(Class<T> enumClass, double[] weights) {
        T[] values = enumClass.getEnumConstants();
        
        if (values.length != weights.length) {
            throw new IllegalArgumentException("Weights array must be the same length as the enum values");
        }
        
        double randomValue = random.nextDouble();
        double cumulativeProbability = 0.0;
        
        for (int i = 0; i < weights.length; i++) {
            cumulativeProbability += weights[i];
            if (randomValue < cumulativeProbability) {
                return values[i];
            }
        }
        
        // Fallback (should not happen if weights sum to 1.0)
        return values[values.length - 1];
    }
    
    /**
     * Get a random enum value from the given enum class, excluding certain values.
     * 
     * @param <T> The enum type
     * @param enumClass The enum class
     * @param excludedValues Enum values to exclude
     * @return A random enum value, excluding the specified values
     */
    @SafeVarargs
    public static <T extends Enum<T>> T getRandomEnumValueExcluding(Class<T> enumClass, T... excludedValues) {
        T[] allValues = enumClass.getEnumConstants();
        
        // Filter out excluded values
        java.util.List<T> filteredValues = Arrays.stream(allValues)
                .filter(value -> !Arrays.asList(excludedValues).contains(value))
                .collect(Collectors.toList());
        
        if (filteredValues.isEmpty()) {
            throw new IllegalArgumentException("No enum values left after exclusion");
        }
        
        return filteredValues.get(random.nextInt(filteredValues.size()));
    }

    /**
     * Create a normalized weight array for the given enum class based on the specified distribution.
     * 
     * @param <T> The enum type
     * @param enumClass The enum class
     * @param distribution Array of relative weights (will be normalized to sum to 1.0)
     * @return Normalized weight array
     */
    public static <T extends Enum<T>> double[] createNormalizedWeights(Class<T> enumClass, double[] distribution) {
        T[] values = enumClass.getEnumConstants();
        
        if (values.length != distribution.length) {
            throw new IllegalArgumentException("Distribution array must be the same length as the enum values");
        }
        
        double sum = Arrays.stream(distribution).sum();
        double[] normalizedWeights = new double[distribution.length];
        
        for (int i = 0; i < distribution.length; i++) {
            normalizedWeights[i] = distribution[i] / sum;
        }
        
        return normalizedWeights;
    }
    
    /**
     * Get the index of the enum value in its enum class.
     * 
     * @param <T> The enum type
     * @param enumValue The enum value
     * @return The index of the enum value
     */
    public static <T extends Enum<T>> int getEnumIndex(T enumValue) {
        return enumValue.ordinal();
    }
    
    /**
     * Get the enum value at the specified index in the enum class.
     * 
     * @param <T> The enum type
     * @param enumClass The enum class
     * @param index The index
     * @return The enum value at the specified index
     */
    public static <T extends Enum<T>> T getEnumValueAtIndex(Class<T> enumClass, int index) {
        T[] values = enumClass.getEnumConstants();
        
        if (index < 0 || index >= values.length) {
            throw new IllegalArgumentException("Index out of bounds for enum " + enumClass.getSimpleName());
        }
        
        return values[index];
    }
}