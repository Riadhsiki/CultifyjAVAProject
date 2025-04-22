package services.Auth;

import models.User;
import services.user.UserService;
import utils.PasswordHasher;

import java.sql.SQLException;

public class UserRegistrationService {

    private final UserService userService;

    public UserRegistrationService() {
        this.userService = new UserService();
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

        // Hash the password before storing
        String hashedPassword = PasswordHasher.hash(user.getPassword());
        user.setPassword(hashedPassword);

        // Create the user in the database
        return userService.create(user);
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