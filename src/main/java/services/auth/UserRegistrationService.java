package services.auth;

import models.User;
import services.user.UserService;
import utils.PasswordHasher;
import utils.EmailSender;

import java.sql.SQLException;
import java.util.UUID;

public class UserRegistrationService {

    private final UserService userService;
    private final EmailSender emailSender;

    public UserRegistrationService() {
        this.userService = new UserService();
        this.emailSender = new EmailSender();
    }

    /**
     * Register a new user
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

        // Generate verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerified(false);

        // Hash the password before storing
        String hashedPassword = PasswordHasher.hash(user.getPassword());
        user.setPassword(hashedPassword);

        // Create the user in the database
        boolean created = userService.create(user);
        
        if (created) {
            // Send verification email
            String verificationLink = "http://yourdomain.com/verify?token=" + verificationToken;
            String emailBody = "Please click the following link to verify your email: " + verificationLink;
            emailSender.sendEmail(user.getEmail(), "Email Verification", emailBody);
        }
        
        return created;
    }

    /**
     * Verify user's email using the verification token
     * @param token The verification token
     * @return true if verification successful, false otherwise
     */
    public boolean verifyEmail(String token) throws SQLException {
        User user = userService.getByVerificationToken(token);
        if (user != null) {
            user.setVerified(true);
            user.setVerificationToken(null);
            return userService.update(user);
        }
        return false;
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