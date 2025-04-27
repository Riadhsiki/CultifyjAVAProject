package controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.User;
import services.user.UserService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class ChangerMotDePasseController {

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Text errorMessage;
    @FXML private Text successMessage;
    @FXML private Button saveButton;
    @FXML private Button backButton;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private User currentUser;
    private final UserService userService = new UserService();
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    );

    @FXML
    public void initialize() {
        clearMessages();

        if (!sessionManager.isLoggedIn()) {
            showError("No active session found. Please log in again.");
            disableFormFields(true);
            scheduleLoginRedirect();
            return;
        }

        String username = sessionManager.getCurrentUsername();
        if (username == null || username.isEmpty()) {
            showError("Session error: Username not found. Please login again.");
            scheduleLoginRedirect();
            return;
        }

        try {
            currentUser = userService.getUserByUsername(username);
            if (currentUser == null) {
                showError("User not found in database.");
                scheduleLoginRedirect();
            }
        } catch (Exception e) {
            showError("Failed to load user data: " + e.getMessage());
            disableFormFields(true);
        }
    }

    private void disableFormFields(boolean disable) {
        currentPasswordField.setDisable(disable);
        newPasswordField.setDisable(disable);
        confirmPasswordField.setDisable(disable);
        saveButton.setDisable(disable);
        backButton.setDisable(disable);
    }

    @FXML
    private void handleSavePassword() {
        clearMessages();

        if (!sessionManager.isLoggedIn()) {
            showError("Session expired. Please login again.");
            scheduleLoginRedirect();
            return;
        }

        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (currentPassword.isEmpty()) {
            showError("Current password is required.");
            return;
        }
        if (newPassword.isEmpty()) {
            showError("New password is required.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showError("New password and confirmation do not match.");
            return;
        }
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            showError("Password must be at least 8 characters, including a digit, lowercase, uppercase, and special character.");
            return;
        }
        if (!userService.verifyPassword(currentUser.getUsername(), currentPassword)) {
            showError("Current password is incorrect.");
            return;
        }

        try {
            currentUser.setPassword(newPassword);
            userService.updateUser(currentUser);
            showSuccess("Password updated successfully!");
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } catch (Exception e) {
            showError("Error updating password: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Auth/profile.fxml"));
            if (loader.getLocation() == null) {
                showError("Profile page not found.");
                return;
            }
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("CultureSketch - Profile");
            stage.show();
        } catch (IOException e) {
            showError("Error loading profile page: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
        errorMessage.setManaged(true);
        successMessage.setVisible(false);
        successMessage.setManaged(false);
    }

    private void showSuccess(String message) {
        successMessage.setText(message);
        successMessage.setVisible(true);
        successMessage.setManaged(true);
        errorMessage.setVisible(false);
        errorMessage.setManaged(false);
    }

    private void clearMessages() {
        errorMessage.setVisible(false);
        errorMessage.setManaged(false);
        successMessage.setVisible(false);
        successMessage.setManaged(false);
    }

    private void scheduleLoginRedirect() {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Auth/login.fxml"));
                        if (loader.getLocation() == null) {
                            showError("Login page not found.");
                            return;
                        }
                        Parent root = loader.load();
                        Scene scene = new Scene(root);
                        Stage stage = (Stage) currentPasswordField.getScene().getWindow();
                        stage.setScene(scene);
                        stage.setTitle("CultureSketch - Login");
                        stage.show();
                    } catch (IOException e) {
                        showError("Error redirecting to login: " + e.getMessage());
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}