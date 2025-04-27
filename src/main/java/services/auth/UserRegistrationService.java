package services.auth;

import models.User;
import services.user.UserService;
import utils.PasswordHasher;
import utils.EmailSender;
import utils.VerificationService;
import utils.SessionManager;

import java.sql.SQLException;
import java.util.UUID;

public class UserRegistrationService {

    private final UserService userService;
    private final EmailSender emailSender;
    private final VerificationService verificationService;
    private final SessionManager sessionManager;

    public UserRegistrationService() {
        this.userService = new UserService();
        this.emailSender = new EmailSender();
        this.verificationService = new VerificationService();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Register a new user and send welcome email
     * @param user The User object with registration details
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(User user) throws SQLException {
        // Check if username or email already exist
        if (userService.usernameExists(user.getUsername())) {
            return false;
        }

        if (userService.emailExists(user.getEmail())) {
            return false;
        }

        // Hash the password before storing
        String hashedPassword = PasswordHasher.hash(user.getPassword());
        user.setPassword(hashedPassword);

        // Create the user in the database
        boolean created = userService.create(user);

        if (created) {
            // Send welcome email
            String emailSubject = "Welcome to Cultify!";
            String emailBody = "Dear " + user.getUsername() + ",\n\n" +
                    "Welcome to joining us at Cultify! We're excited to have you on board.\n" +
                    "Explore our platform and start your journey with us today.\n\n" +
                    "Best regards,\nThe Cultify Team";
            emailSender.sendEmail(user.getEmail(), emailSubject, emailBody);
        }

        return created;
    }

    /**
     * Log in a user and create session
     * @param username The username
     * @param password The password
     * @return true if login successful, false otherwise
     */
    public boolean login(String username, String password) throws SQLException {
        // Verify credentials
        if (!userService.verifyPassword(username, password)) {
            return false;
        }

        // Get user
        User user = userService.getByUsername(username);
        if (user == null) {
            return false;
        }

        // Generate session token
        String sessionToken = UUID.randomUUID().toString();

        // Store session
        sessionManager.setSessionToken(sessionToken, true); // Remember me enabled
        sessionManager.setCurrentUsername(username);

        return true;
    }

    /**
     * Verify a temporary token (e.g., for future verification needs)
     * @param token The temporary token
     * @return true if verification successful, false otherwise
     */
    public boolean verifyToken(String token) {
        String email = verificationService.verifyToken(token);
        return email != null;
    }

    /**
     * Update user profile information
     * @param user The User object with updated information
     * @return true if update successful, false otherwise
     */
    public boolean updateUserProfile(User user) throws SQLException {
        // Get the current user from the database
        User existingUser = userService.getById(user.getId());
        if (existingUser == null) {
            return false;
        }

        // Check if username is being changed and if it already exists
        if (!user.getUsername().equals(existingUser.getUsername()) &&
                userService.usernameExists(user.getUsername())) {
            return false;
        }

        // Check if email is being changed and if it already exists
        if (!user.getEmail().equals(existingUser.getEmail()) &&
                userService.emailExists(user.getEmail())) {
            return false;
        }

        // Update the user in the database
        return userService.update(user);
    }

    /**
     * Change user password
     * @param userId The user ID
     * @param currentPassword The current password
     * @param newPassword The new password
     * @return true if password change successful, false otherwise
     */
    public boolean changePassword(int userId, String currentPassword, String newPassword) throws SQLException {
        // Get the user from the database
        User user = userService.getById(userId);
        if (user == null) {
            return false;
        }

        // Verify the current password
        if (!PasswordHasher.verify(currentPassword, user.getPassword())) {
            return false;
        }

        // Update the password
        user.setPassword(PasswordHasher.hash(newPassword));
        return userService.update(user);
    }
}