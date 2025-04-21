package utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

public class PasswordHasher {

    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Hash a password using PBKDF2 with HMAC-SHA256
     * @param password The password to hash
     * @return The hashed password with salt prepended
     */
    public static String hash(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);

        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

        // Format: iterations:base64(salt):base64(hash)
        return ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verify a password against a stored hash
     * @param password The password to verify
     * @param storedHash The stored hash to verify against
     * @return true if the password matches, false otherwise
     */
    public static boolean verify(String password, String storedHash) {
        // Format: iterations:base64(salt):base64(hash)
        String[] parts = storedHash.split(":");

        if (parts.length != 3) {
            return false; // Invalid hash format
        }

        int iterations;
        try {
            iterations = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return false; // Invalid iterations
        }

        byte[] salt;
        byte[] hash;

        try {
            salt = Base64.getDecoder().decode(parts[1]);
            hash = Base64.getDecoder().decode(parts[2]);
        } catch (IllegalArgumentException e) {
            return false; // Invalid base64
        }

        byte[] testHash = pbkdf2(password.toCharArray(), salt, iterations, hash.length * 8);
        return Arrays.equals(hash, testHash);
    }

    /**
     * Implementation of PBKDF2 (Password-Based Key Derivation Function 2)
     * @param password The password
     * @param salt The salt
     * @param iterations The number of iterations
     * @param keyLength The key length in bits
     * @return The derived key
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error while hashing password: " + e.getMessage(), e);
        }
    }
}