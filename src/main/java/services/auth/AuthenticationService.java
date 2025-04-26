package services.auth;

import models.User;
import services.user.UserService;
import utils.DataSource;
import utils.PasswordHasher;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationService {
    private final Map<String, SessionInfo> activeSessions;
    private static final int SESSION_TIMEOUT_MINUTES = 30;
    private final Connection con;
    private final UserService userService;
    private final Map<String, LoginAttempt> loginAttempts;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    private static final int RATE_LIMIT_WINDOW_MINUTES = 1;
    private static final int MAX_REQUESTS_PER_WINDOW = 3;

    // Constructor with connection
    public AuthenticationService(Connection con) {
        this.con = con;
        this.activeSessions = new HashMap<>();
        this.userService = new UserService();
        this.loginAttempts = new HashMap<>();
    }

    // Default constructor using DataSource
    public AuthenticationService() {
        this(DataSource.getInstance().getConnection());
    }

    public String loginWithUsernameOrEmail(String loginInput, String password, String ipAddress) throws SQLException {
        // Check for login attempts
        LoginAttempt attempt = loginAttempts.get(loginInput);
        if (attempt != null) {
            if (attempt.isLocked()) {
                throw new SecurityException("Account is temporarily locked. Please try again later.");
            }
            if (attempt.isRateLimited()) {
                throw new SecurityException("Too many login attempts. Please wait a moment and try again.");
            }
        }

        // Get user from database
        User user = null;
        String query = "SELECT * FROM user WHERE username = ? OR email = ?";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, loginInput);
            stmt.setString(2, loginInput);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                // Set other fields as needed
            }
        }

        if (user == null) {
            recordFailedAttempt(loginInput);
            return null;
        }

        // Verify password
        if (!verifyPassword(password, user.getPassword())) {
            recordFailedAttempt(loginInput);
            return null;
        }

        // Clear login attempts on successful login
        loginAttempts.remove(loginInput);

        // Generate session token
        String sessionToken = generateSessionToken();

        // Store session info
        SessionInfo sessionInfo = new SessionInfo(user.getId(), System.currentTimeMillis(), ipAddress);
        activeSessions.put(sessionToken, sessionInfo);

        System.out.println("Login successful for user: " + user.getUsername());
        System.out.println("Generated session token: " + sessionToken);

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
        String query = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, sessionInfo.getUserId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                // Set other fields as needed
                return user;
            }
        }

        return null;
    }

    /**
     * Check if a user is authenticated
     * @param sessionToken The session token
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated(String sessionToken, String currentIpAddress) {
        try {
            SessionInfo sessionInfo = activeSessions.get(sessionToken);
            if (sessionInfo == null) {
                return false;
            }
            
            // Check if IP address matches
            if (!sessionInfo.getIpAddress().equals(currentIpAddress)) {
                activeSessions.remove(sessionToken);
                return false;
            }
            
            return getCurrentUser(sessionToken) != null;
        } catch (SQLException e) {
            e.printStackTrace();
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
        // This is just a placeholder - replace with your actual implementation
        // Ideally use PasswordHasher.verify if implemented
        try {
            return PasswordHasher.verify(inputPassword, storedPassword);
        } catch (Exception e) {
            // As a fallback, do direct comparison (not secure, just for development)
            return inputPassword.equals(storedPassword);
        }
    }

    private void recordFailedAttempt(String loginInput) {
        LoginAttempt attempt = loginAttempts.getOrDefault(loginInput, new LoginAttempt());
        attempt.incrementAttempts();

        if (attempt.getAttempts() >= MAX_LOGIN_ATTEMPTS) {
            attempt.setLocked(true);
            attempt.setLockoutTime(System.currentTimeMillis());
        }

        loginAttempts.put(loginInput, attempt);
    }

    /**
     * Inner class to store session information
     */
    private static class SessionInfo {
        private final int userId;
        private long creationTime;
        private final String ipAddress;

        public SessionInfo(int userId, long creationTime, String ipAddress) {
            this.userId = userId;
            this.creationTime = creationTime;
            this.ipAddress = ipAddress;
        }

        public int getUserId() {
            return userId;
        }

        public long getCreationTime() {
            return creationTime;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setCreationTime(long creationTime) {
            this.creationTime = creationTime;
        }
    }

    private static class LoginAttempt {
        private int attempts;
        private boolean locked;
        private long lockoutTime;
        private long lastAttemptTime;
        private int requestsInWindow;

        public LoginAttempt() {
            this.attempts = 0;
            this.locked = false;
            this.lockoutTime = 0;
            this.lastAttemptTime = 0;
            this.requestsInWindow = 0;
        }

        public void incrementAttempts() {
            long currentTime = System.currentTimeMillis();
            
            // Check if we're in a new rate limit window
            if (currentTime - lastAttemptTime > RATE_LIMIT_WINDOW_MINUTES * 60 * 1000) {
                requestsInWindow = 0;
            }
            
            requestsInWindow++;
            lastAttemptTime = currentTime;
            attempts++;
        }

        public boolean isRateLimited() {
            return requestsInWindow >= MAX_REQUESTS_PER_WINDOW;
        }

        public boolean isLocked() {
            if (locked) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lockoutTime > LOCKOUT_DURATION_MINUTES * 60 * 1000) {
                    locked = false;
                    attempts = 0;
                    return false;
                }
                return true;
            }
            return false;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
            if (locked) {
                this.lockoutTime = System.currentTimeMillis();
            }
        }

        public int getAttempts() {
            return attempts;
        }

        public void setLockoutTime(long lockoutTime) {
            this.lockoutTime = lockoutTime;
        }
    }
}