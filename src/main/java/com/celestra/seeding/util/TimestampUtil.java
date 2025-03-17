package com.celestra.seeding.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for generating timestamps during data seeding.
 * This class provides methods for creating various types of timestamps needed for seeding the database.
 */
public class TimestampUtil {
    
    private static final Random random = new Random();
    
    /**
     * Private constructor to prevent instantiation.
     */
    private TimestampUtil() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Get the current timestamp.
     * 
     * @return The current timestamp
     */
    public static Timestamp getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
    
    /**
     * Generate a timestamp for a specified number of days in the past.
     * 
     * @param days Number of days in the past
     * @return A timestamp from the specified number of days ago
     */
    public static Timestamp getDaysAgo(int days) {
        return new Timestamp(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days));
    }
    
    /**
     * Generate a timestamp for a specified number of days in the future.
     * 
     * @param days Number of days in the future
     * @return A timestamp for the specified number of days in the future
     */
    public static Timestamp getDaysAhead(int days) {
        return new Timestamp(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(days));
    }
    
    /**
     * Generate a random timestamp between two dates.
     * 
     * @param start Start timestamp
     * @param end End timestamp
     * @return A random timestamp between start and end
     */
    public static Timestamp getRandomTimestampBetween(Timestamp start, Timestamp end) {
        long startTime = start.getTime();
        long endTime = end.getTime();
        
        if (endTime < startTime) {
            throw new IllegalArgumentException("End timestamp must be after start timestamp");
        }
        
        long randomTime = startTime + (long) (random.nextDouble() * (endTime - startTime));
        return new Timestamp(randomTime);
    }
    
    /**
     * Generate a random timestamp within a range of days from now.
     * 
     * @param minDays Minimum number of days (negative for past, positive for future)
     * @param maxDays Maximum number of days (negative for past, positive for future)
     * @return A random timestamp within the specified range
     */
    public static Timestamp getRandomTimestampInRange(int minDays, int maxDays) {
        if (minDays > maxDays) {
            throw new IllegalArgumentException("Minimum days must be less than or equal to maximum days");
        }
        
        long now = System.currentTimeMillis();
        long minTime = now + TimeUnit.DAYS.toMillis(minDays);
        long maxTime = now + TimeUnit.DAYS.toMillis(maxDays);
        
        long randomTime = minTime + (long) (random.nextDouble() * (maxTime - minTime));
        return new Timestamp(randomTime);
    }
    
    /**
     * Generate a timestamp that is a random amount of time after the specified timestamp.
     * 
     * @param timestamp The base timestamp
     * @param minMinutes Minimum number of minutes after
     * @param maxMinutes Maximum number of minutes after
     * @return A timestamp that is randomly between minMinutes and maxMinutes after the specified timestamp
     */
    public static Timestamp getRandomTimeAfter(Timestamp timestamp, int minMinutes, int maxMinutes) {
        if (minMinutes > maxMinutes) {
            throw new IllegalArgumentException("Minimum minutes must be less than or equal to maximum minutes");
        }
        
        long baseTime = timestamp.getTime();
        long minTime = baseTime + TimeUnit.MINUTES.toMillis(minMinutes);
        long maxTime = baseTime + TimeUnit.MINUTES.toMillis(maxMinutes);
        
        long randomTime = minTime + (long) (random.nextDouble() * (maxTime - minTime));
        return new Timestamp(randomTime);
    }
    
    /**
     * Generate a timestamp that is a random amount of time before the specified timestamp.
     * 
     * @param timestamp The base timestamp
     * @param minMinutes Minimum number of minutes before
     * @param maxMinutes Maximum number of minutes before
     * @return A timestamp that is randomly between minMinutes and maxMinutes before the specified timestamp
     */
    public static Timestamp getRandomTimeBefore(Timestamp timestamp, int minMinutes, int maxMinutes) {
        if (minMinutes > maxMinutes) {
            throw new IllegalArgumentException("Minimum minutes must be less than or equal to maximum minutes");
        }
        
        long baseTime = timestamp.getTime();
        long minTime = baseTime - TimeUnit.MINUTES.toMillis(maxMinutes);
        long maxTime = baseTime - TimeUnit.MINUTES.toMillis(minMinutes);
        
        long randomTime = minTime + (long) (random.nextDouble() * (maxTime - minTime));
        return new Timestamp(randomTime);
    }
    
    /**
     * Generate a created_at and updated_at timestamp pair.
     * The updated_at timestamp will be after the created_at timestamp.
     * 
     * @param minDaysAgo Minimum number of days ago for created_at
     * @param maxDaysAgo Maximum number of days ago for created_at
     * @param maxMinutesAfter Maximum number of minutes after created_at for updated_at
     * @return An array of two timestamps: [created_at, updated_at]
     */
    public static Timestamp[] getCreatedUpdatedTimestamps(int minDaysAgo, int maxDaysAgo, int maxMinutesAfter) {
        if (minDaysAgo > maxDaysAgo) {
            throw new IllegalArgumentException("Minimum days ago must be less than or equal to maximum days ago");
        }
        
        // Generate created_at timestamp
        Timestamp createdAt = getRandomTimestampInRange(-maxDaysAgo, -minDaysAgo);
        
        // Generate updated_at timestamp (0 to maxMinutesAfter after created_at)
        Timestamp updatedAt = getRandomTimeAfter(createdAt, 0, maxMinutesAfter);
        
        return new Timestamp[] { createdAt, updatedAt };
    }
    
    /**
     * Generate a timestamp with the time component set to midnight (00:00:00).
     * 
     * @param timestamp The timestamp to modify
     * @return A new timestamp with the same date but time set to midnight
     */
    public static Timestamp getDateOnly(Timestamp timestamp) {
        LocalDateTime dateTime = timestamp.toLocalDateTime();
        LocalDateTime dateOnly = dateTime.toLocalDate().atStartOfDay();
        return Timestamp.valueOf(dateOnly);
    }
}