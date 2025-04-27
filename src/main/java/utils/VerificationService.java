package utils;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VerificationService {
    private static final ConcurrentHashMap<String, String> tokenStore = new ConcurrentHashMap<>();

    /**
     * Generate a temporary token for an email
     * @param email The user's email
     * @return The generated token
     */
    public String generateToken(String email) {
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, email);
        return token;
    }

    /**
     * Verify and remove a token
     * @param token The token to verify
     * @return The associated email, or null if invalid
     */
    public String verifyToken(String token) {
        return tokenStore.remove(token);
    }
}