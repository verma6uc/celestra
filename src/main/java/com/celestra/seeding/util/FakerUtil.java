package com.celestra.seeding.util;

import com.github.javafaker.Faker;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for generating fake data using the Java Faker library.
 * This class provides methods for generating various types of data needed for seeding the database.
 */
public class FakerUtil {
    
    private static final Faker faker = new Faker();
    private static final Random random = new Random();
    
    /**
     * Private constructor to prevent instantiation.
     */
    private FakerUtil() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Get the Faker instance.
     * 
     * @return The Faker instance
     */
    public static Faker getFaker() {
        return faker;
    }
    
    /**
     * Generate a random company name.
     * 
     * @return A random company name
     */
    public static String generateCompanyName() {
        return faker.company().name();
    }
    
    /**
     * Generate a random company description.
     * 
     * @return A random company description
     */
    public static String generateCompanyDescription() {
        return faker.company().catchPhrase() + ". " + faker.company().bs() + ".";
    }
    
    /**
     * Generate a random person's name.
     * 
     * @return A random person's name
     */
    public static String generatePersonName() {
        return faker.name().fullName();
    }
    
    /**
     * Generate a random email address.
     * 
     * @return A random email address
     */
    public static String generateEmail() {
        return faker.internet().emailAddress();
    }
    
    /**
     * Generate a random password hash using BCrypt.
     * 
     * @return A BCrypt password hash
     */
    public static String generatePasswordHash() {
        String password = faker.internet().password(8, 16, true, true);
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    
    /**
     * Generate a random IP address (IPv4).
     * 
     * @return A random IP address
     */
    public static String generateIpAddress() {
        return faker.internet().ipV4Address();
    }
    
    /**
     * Generate a random user agent string.
     * 
     * @return A random user agent string
     */
    public static String generateUserAgent() {
        return faker.internet().userAgentAny();
    }
    
    /**
     * Generate a random UUID as a string.
     * 
     * @return A random UUID as a string
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Generate a random paragraph of text.
     * 
     * @return A random paragraph
     */
    public static String generateParagraph() {
        return faker.lorem().paragraph();
    }
    
    /**
     * Generate a random sentence.
     * 
     * @return A random sentence
     */
    public static String generateSentence() {
        return faker.lorem().sentence();
    }
    
    /**
     * Generate a random timestamp in the past.
     * 
     * @param daysBack Maximum number of days in the past
     * @return A random timestamp in the past
     */
    public static Timestamp generatePastTimestamp(int daysBack) {
        return new Timestamp(
            faker.date().past(daysBack, TimeUnit.DAYS).getTime()
        );
    }
    
    /**
     * Generate a random timestamp in the future.
     * 
     * @param daysAhead Maximum number of days in the future
     * @return A random timestamp in the future
     */
    public static Timestamp generateFutureTimestamp(int daysAhead) {
        return new Timestamp(
            faker.date().future(daysAhead, TimeUnit.DAYS).getTime()
        );
    }
    
    /**
     * Generate a random timestamp between two dates.
     * 
     * @param startDaysBack Days in the past for the start date
     * @param endDaysBack Days in the past for the end date (must be less than startDaysBack)
     * @return A random timestamp between the two dates
     */
    public static Timestamp generateTimestampBetween(int startDaysBack, int endDaysBack) {
        if (endDaysBack >= startDaysBack) {
            throw new IllegalArgumentException("endDaysBack must be less than startDaysBack");
        }
        
        return new Timestamp(
            faker.date().between(
                faker.date().past(startDaysBack, TimeUnit.DAYS),
                faker.date().past(endDaysBack, TimeUnit.DAYS)
            ).getTime()
        );
    }
    
    /**
     * Generate a random timestamp that is after the given timestamp.
     * 
     * @param after The timestamp to generate after
     * @param maxDaysAfter Maximum number of days after the given timestamp
     * @return A random timestamp after the given timestamp
     */
    public static Timestamp generateTimestampAfter(Timestamp after, int maxDaysAfter) {
        LocalDateTime afterDateTime = after.toLocalDateTime();
        LocalDateTime maxDateTime = afterDateTime.plusDays(maxDaysAfter);
        
        long minMillis = after.getTime();
        long maxMillis = Timestamp.valueOf(maxDateTime).getTime();
        long randomMillis = minMillis + (long) (random.nextDouble() * (maxMillis - minMillis));
        
        return new Timestamp(randomMillis);
    }
    
    /**
     * Generate a random integer within a range.
     * 
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return A random integer within the range
     */
    public static int generateRandomInt(int min, int max) {
        return faker.number().numberBetween(min, max);
    }
    
    /**
     * Generate a random boolean with the given probability of being true.
     * 
     * @param probabilityOfTrue Probability of generating true (0.0 to 1.0)
     * @return A random boolean
     */
    public static boolean generateRandomBoolean(double probabilityOfTrue) {
        return random.nextDouble() < probabilityOfTrue;
    }
    
    /**
     * Generate a random element from an array.
     * 
     * @param <T> The type of elements in the array
     * @param array The array to choose from
     * @return A random element from the array
     */
    public static <T> T generateRandomElement(T[] array) {
        return array[random.nextInt(array.length)];
    }
    
    /**
     * Generate a weighted random index based on the given weights.
     * 
     * @param weights Array of weights (must sum to 1.0)
     * @return A random index based on the weights
     */
    public static int generateWeightedRandomIndex(double[] weights) {
        double randomValue = random.nextDouble();
        double cumulativeProbability = 0.0;
        
        for (int i = 0; i < weights.length; i++) {
            cumulativeProbability += weights[i];
            if (randomValue < cumulativeProbability) {
                return i;
            }
        }
        
        // Fallback (should not happen if weights sum to 1.0)
        return weights.length - 1;
    }
}