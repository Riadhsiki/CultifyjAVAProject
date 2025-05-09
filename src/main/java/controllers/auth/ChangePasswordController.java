package controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;
import services.user.UserService;
import utils.SessionManager;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

public class ChangePasswordController {

    @FXML private PasswordField currentPasswordField;
    @FXML private Label currentPasswordErrorLabel;
    @FXML private PasswordField newPasswordField;
    @FXML private Label newPasswordErrorLabel;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label confirmPasswordErrorLabel;
    @FXML private Button saveButton;
    @FXML private Button backButton;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final UserService userService = new UserService();
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$"
    ); // At least 8 chars, 1 letter, 1 number

    @FXML
    public void initialize() {
        clearErrorLabels();

        // Add tooltips
        saveButton.setTooltip(new Tooltip("Save new password"));
        backButton.setTooltip(new Tooltip("Return to profile"));

        // Add change listener to enable Save button
        currentPasswordField.textProperty().addListener((obs, old, newVal) -> checkForChanges());
        newPasswordField.textProperty().addListener((obs, old, newVal) -> checkForChanges());
        confirmPasswordField.textProperty().addListener((obs, old, newVal) -> checkForChanges());

        // Initially disable Save button
        saveButton.setDisable(true);

        // Check session
        if (!sessionManager.isLoggedIn()) {
            showAlert("No active session found. Please log in again.", Alert.AlertType.WARNING);
            scheduleLoginRedirect();
        }
    }

    private void checkForChanges() {
        boolean hasInput = !currentPasswordField.getText().trim().isEmpty() &&
                !newPasswordField.getText().trim().isEmpty() &&
                !confirmPasswordField.getText().trim().isEmpty();
        saveButton.setDisable(!hasInput);
    }

    @FXML
    private void handleSavePassword(ActionEvent event) {
        clearErrorLabels();

        if (!sessionManager.isLoggedIn()) {
            showAlert("Session expired. Please login again.", Alert.AlertType.WARNING);
            scheduleLoginRedirect();
            return;
        }

        // Confirm save action
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Password Change");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to change your password?");
        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() != ButtonType.OK) {
            return;
        }

        // Validate fields
        if (validateFields()) {
            try {
                String username = sessionManager.getCurrentUsername();
                String newPassword = newPasswordField.getText().trim();
                userService.changePassword(username, newPassword);
                showAlert("Password updated successfully!", Alert.AlertType.INFORMATION);
                clearFields();
                saveButton.setDisable(true);
                navigateTo("/Auth/Profile.fxml", "CultureSketch - Profile");
            } catch (Exception e) {
                showAlert("Error updating password: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validateFields() {
        boolean isValid = true;
        String username = sessionManager.getCurrentUsername();

        // Validate current password
        if (currentPasswordField.getText().trim().isEmpty()) {
            currentPasswordErrorLabel.setText("Current password is required.");
            isValid = false;
        } else if (!userService.verifyPassword(username, currentPasswordField.getText().trim())) {
            currentPasswordErrorLabel.setText("Incorrect current password.");
            isValid = false;
        }

        // Validate new password
        if (newPasswordField.getText().trim().isEmpty()) {
            newPasswordErrorLabel.setText("New password is required.");
            isValid = false;
        } else if (!PASSWORD_PATTERN.matcher(newPasswordField.getText().trim()).matches()) {
            newPasswordErrorLabel.setText("Password must be at least 8 characters with 1 letter and 1 number.");
            isValid = false;
        }

        // Validate confirm password
        if (confirmPasswordField.getText().trim().isEmpty()) {
            confirmPasswordErrorLabel.setText("Please confirm your new password.");
            isValid = false;
        } else if (!newPasswordField.getText().trim().equals(confirmPasswordField.getText().trim())) {
            confirmPasswordErrorLabel.setText("Passwords do not match.");
            isValid = false;
        }

        return isValid;
    }

    private void clearFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        clearErrorLabels();
    }

    private void clearErrorLabels() {
        if (currentPasswordErrorLabel != null) currentPasswordErrorLabel.setText("");
        if (newPasswordErrorLabel != null) newPasswordErrorLabel.setText("");
        if (confirmPasswordErrorLabel != null) confirmPasswordErrorLabel.setText("");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigateTo("/Auth/Profile.fxml", "CultureSketch - Profile");
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                showAlert(title + " page not found.", Alert.AlertType.ERROR);
                return;
            }
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            showAlert("Error loading " + title + ": " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void scheduleLoginRedirect() {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> navigateTo("/Auth/login.fxml", "CultureSketch - Login"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
