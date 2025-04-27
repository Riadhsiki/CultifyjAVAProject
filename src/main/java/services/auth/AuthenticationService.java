package services.auth;

import models.User;
import services.user.UserService;
import utils.DataSource;
import utils.PasswordHasher;

import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationService {
    private static final AuthenticationService instance = new AuthenticationService(DataSource.getInstance().getConnection());
    private final Connection con;
    private final UserService userService;
    private final Map<String, LoginAttempt> loginAttempts;
    private static final int SESSION_TIMEOUT_MINUTES = 60; // Match LoginController
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    private static final int RATE_LIMIT_WINDOW_MINUTES = 1;
    private static final int MAX_REQUESTS_PER_WINDOW = 3;

    public AuthenticationService(Connection connection) {
        this.con = connection;
        this.userService = new UserService();
        this.loginAttempts = new HashMap<>();
        initializeDatabase();
    }

    public static AuthenticationService getInstance() {
        return instance;
    }

    private void initializeDatabase() {
        try (Statement stmt = con.createStatement()) {
            String createSessionTableSQL = "CREATE TABLE IF NOT EXISTS sessions (" +
                    "session_token VARCHAR(255) PRIMARY KEY, " +
                    "user_id INT NOT NULL, " +
                    "ip_address VARCHAR(45), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "expires_at TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB";
            stmt.execute(createSessionTableSQL);
        } catch (SQLException e) {
            System.err.println("Error initializing sessions table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String loginWithUsernameOrEmail(String loginInput, String password, String ipAddress) throws SQLException {
        LoginAttempt attempt = loginAttempts.get(loginInput);
        if (attempt != null) {
            if (attempt.isLocked()) {
                throw new SecurityException("Account is temporarily locked. Please try again later.");
            }
            if (attempt.isRateLimited()) {
                throw new SecurityException("Too many login attempts. Please wait a moment and try again.");
            }
        }

        User user = userService.getByUsername(loginInput);
        if (user == null) {
            user = userService.getByEmail(loginInput);
        }

        if (user == null) {
            recordFailedAttempt(loginInput);
            System.out.println("Login failed: User not found for " + loginInput);
            return null;
        }

        if (!verifyPassword(password, user.getPassword())) {
            recordFailedAttempt(loginInput);
            System.out.println("Login failed: Incorrect password for " + loginInput);
            return null;
        }

        loginAttempts.remove(loginInput);
        String sessionToken = generateSessionToken();
        createSession(sessionToken, user.getId(), ipAddress);
        System.out.println("Login successful for user: " + user.getUsername());
        System.out.println("Generated session token: " + sessionToken);
        return sessionToken;
    }

    public boolean logout(String sessionToken) {
        String sql = "DELETE FROM sessions WHERE session_token = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, sessionToken);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Logged out session: " + sessionToken + ", Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error logging out session " + sessionToken + ": " + e.getMessage());
            return false;
        }
    }

    public User getCurrentUser(String sessionToken) throws SQLException {
        if (sessionToken == null) {
            System.out.println("No session token provided");
            return null;
        }

        String sql = "SELECT u.id, u.username, u.email, u.roles " +
                "FROM user u JOIN sessions s ON u.id = s.user_id " +
                "WHERE s.session_token = ? AND (s.expires_at IS NULL OR s.expires_at > CURRENT_TIMESTAMP)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, sessionToken);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setRoles(rs.getString("roles"));
                System.out.println("Retrieved user: " + user.getUsername() + " for session token: " + sessionToken);
                return user;
            }
        }
        System.out.println("No valid session found for token: " + sessionToken);
        return null;
    }

    public boolean isAuthenticated(String sessionToken, String currentIpAddress) throws SQLException {
        if (sessionToken == null) {
            System.out.println("No session token provided");
            return false;
        }

        String sql = "SELECT user_id, ip_address, expires_at FROM sessions WHERE session_token = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, sessionToken);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Timestamp expiresAt = rs.getTimestamp("expires_at");
                boolean isValid = expiresAt == null || expiresAt.after(new Timestamp(System.currentTimeMillis()));
                if (!isValid) {
                    System.out.println("Session expired for token: " + sessionToken);
                    return false;
                }
                // Optionally re-enable IP check with proper handling
                /*
                String storedIp = rs.getString("ip_address");
                if (!currentIpAddress.equals(storedIp)) {
                    System.out.println("IP mismatch for session token: " + sessionToken + ", stored: " + storedIp + ", current: " + currentIpAddress);
                    return false;
                }
                */
                System.out.println("Session validated for token: " + sessionToken);
                return true;
            }
        }
        System.out.println("Session token not found: " + sessionToken);
        return false;
    }

    public boolean hasRole(String sessionToken, String role) throws SQLException {
        User user = getCurrentUser(sessionToken);
        if (user == null || user.getRoles() == null) {
            System.out.println("No user or roles found for session token: " + sessionToken);
            return false;
        }
        boolean hasRole = user.getRoles().contains(role);
        System.out.println("Role check for " + role + " on token " + sessionToken + ": " + hasRole);
        return hasRole;
    }

    private String generateSessionToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        System.out.println("Generated session token: " + token);
        return token;
    }

    private boolean verifyPassword(String inputPassword, String storedPassword) {
        try {
            boolean verified = PasswordHasher.verify(inputPassword, storedPassword);
            System.out.println("Password verification: " + (verified ? "success" : "failure"));
            return verified;
        } catch (Exception e) {
            System.err.println("Error verifying password: " + e.getMessage());
            return false;
        }
    }

    private void createSession(String sessionToken, int userId, String ipAddress) throws SQLException {
        String sql = "INSERT INTO sessions (session_token, user_id, ip_address, expires_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, sessionToken);
            pstmt.setInt(2, userId);
            pstmt.setString(3, ipAddress);
            Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + SESSION_TIMEOUT_MINUTES * 60 * 1000);
            pstmt.setTimestamp(4, expiresAt);
            pstmt.executeUpdate();
            System.out.println("Created session for user ID: " + userId + " with token: " + sessionToken);
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
        System.out.println("Recorded failed login attempt for: " + loginInput + ", Attempts: " + attempt.getAttempts());
    }

    public boolean initiatePasswordReset(String email) {
        // Placeholder for future implementation
        System.out.println("Password reset requested for email: " + email);
        return false;
    }

    private static class SessionInfo {
        private final int userId;
        private final String ipAddress;

        public SessionInfo(int userId, String ipAddress) {
            this.userId = userId;
            this.ipAddress = ipAddress;
        }

        public int getUserId() {
            return userId;
        }

        public String getIpAddress() {
            return ipAddress;
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