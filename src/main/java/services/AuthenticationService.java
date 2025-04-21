package services;

import models.User;
import services.user.UserService;
import utils.PasswordHasher;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationService {
    private final UserService userService;
    private final Map<String, SessionInfo> activeSessions;
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    public AuthenticationService() {
        this.userService = new UserService();
        this.activeSessions = new HashMap<>();
    }

    /**
     * Authenticate a user and generate a session token
     * @param username The username
     * @param password The password
     * @return Session token if authentication successful, null otherwise
     */
    public String login(String username, String password) throws SQLException {
        User user = userService.getByUsername(username);

        if (user == null) {
            return null; // User not found
        }

        // Verify password - in a real-world scenario, you would use password hashing
        if (!verifyPassword(password, user.getPassword())) {
            return null; // Password doesn't match
        }

        // Generate session token
        String sessionToken = generateSessionToken();

        // Store session info
        SessionInfo sessionInfo = new SessionInfo(user.getId(), System.currentTimeMillis());
        activeSessions.put(sessionToken, sessionInfo);

        return sessionToken;
    }

    /**
     * Log out the user by invalidating their session token
     * @param sessionToken The session token to invalidate
     * @return true if logout was successful, false otherwise
     */
    public boolean logout(String sessionToken) {
        if (sessionToken != null && activeSessions.containsKey(sessionToken)) {
            activeSessions.remove(sessionToken);
            return true;
        }
        return false;
    }

    /**
     * Get the currently authenticated user based on session token
     * @param sessionToken The session token
     * @return The authenticated User object or null if session is invalid
     */
    public User getCurrentUser(String sessionToken) throws SQLException {
        if (sessionToken == null || !activeSessions.containsKey(sessionToken)) {
            return null; // Invalid or expired session
        }

        SessionInfo sessionInfo = activeSessions.get(sessionToken);

        // Check if session has expired
        long currentTime = System.currentTimeMillis();
        long sessionDuration = currentTime - sessionInfo.getCreationTime();
        if (sessionDuration > SESSION_TIMEOUT_MINUTES * 60 * 1000) {
            activeSessions.remove(sessionToken); // Remove expired session
            return null;
        }

        // Refresh session time (optional)
        sessionInfo.setCreationTime(currentTime);

        // Get user by ID
        return userService.getById(sessionInfo.getUserId());
    }

    /**
     * Check if a user is authenticated
     * @param sessionToken The session token
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated(String sessionToken) {
        try {
            return getCurrentUser(sessionToken) != null;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Check if a user has a specific role
     * @param sessionToken The session token
     * @param role The role to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(String sessionToken, String role) throws SQLException {
        User user = getCurrentUser(sessionToken);
        return user != null && user.getRoles() != null && user.getRoles().contains(role);
    }

    /**
     * Generate a random session token
     * @return A secure random token
     */
    private String generateSessionToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Verify a password against stored password
     * In a real application, you would use a proper password hashing algorithm
     * @param inputPassword The password provided during login
     * @param storedPassword The password stored in the database
     * @return true if password matches, false otherwise
     */
    private boolean verifyPassword(String inputPassword, String storedPassword) {
        // In a real implementation, use a secure password hashing algorithm
        // This is just a placeholder for demonstration
        return PasswordHasher.verify(inputPassword, storedPassword);
    }

    /**
     * Inner class to store session information
     */
    private static class SessionInfo {
        private final int userId;
        private long creationTime;

        public SessionInfo(int userId, long creationTime) {
            this.userId = userId;
            this.creationTime = creationTime;
        }

        public int getUserId() {
            return userId;
        }

        public long getCreationTime() {
            return creationTime;
        }

        public void setCreationTime(long creationTime) {
            this.creationTime = creationTime;
        }
    }
}
