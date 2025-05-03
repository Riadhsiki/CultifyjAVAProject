package services.auth;

import models.User;
import services.user.UserService;
import utils.PasswordHasher;

import java.sql.SQLException;

public class UserRegistrationService {

    private final UserService userService;

    public UserRegistrationService() {
        this.userService = new UserService();
    }

    public boolean registerUser(User user) throws SQLException {
        if (userService.usernameExists(user.getUsername())) {
            return false;
        }

        if (userService.emailExists(user.getEmail())) {
            return false;
        }

        if (user.getBiometricId() != null && userService.biometricIdExists(user.getBiometricId())) {
            return false;
        }

        String hashedPassword = PasswordHasher.hash(user.getPassword());
        user.setPassword(hashedPassword);

        return userService.create(user);
    }

    public boolean updateUserProfile(User user) throws SQLException {
        User existingUser = userService.getById(user.getId());
        if (existingUser == null) {
            return false;
        }

        if (!user.getUsername().equals(existingUser.getUsername()) &&
                userService.usernameExists(user.getUsername())) {
            return false;
        }

        if (!user.getEmail().equals(existingUser.getEmail()) &&
                userService.emailExists(user.getEmail())) {
            return false;
        }

        if (user.getBiometricId() != null &&
                !user.getBiometricId().equals(existingUser.getBiometricId()) &&
                userService.biometricIdExists(user.getBiometricId())) {
            return false;
        }

        return userService.update(user);
    }

    public boolean enrollFingerprint(int userId, String biometricId) throws SQLException {
        User user = userService.getById(userId);
        if (user == null) {
            return false;
        }

        if (biometricId != null && userService.biometricIdExists(biometricId)) {
            return false;
        }

        user.setBiometricId(biometricId);
        return userService.update(user);
    }

    public boolean changePassword(int userId, String currentPassword, String newPassword) throws SQLException {
        User user = userService.getById(userId);
        if (user == null) {
            return false;
        }

        if (!PasswordHasher.verify(currentPassword, user.getPassword())) {
            return false;
        }

        user.setPassword(PasswordHasher.hash(newPassword));
        return userService.update(user);
    }
}