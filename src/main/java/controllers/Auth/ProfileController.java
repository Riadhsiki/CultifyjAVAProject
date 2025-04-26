package controllers.Auth;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import utils.SessionManager;
import models.User;

import java.io.IOException;
import java.util.Objects;

public class ProfileController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Text errorMessage;

    @FXML
    private Text successMessage;

    @FXML
    private Button saveButton;

    @FXML
    private Button backButton;

    @FXML
    private Button logoutButton;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private User currentUser;

    /**
     * Initializes the controller class.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        clearMessages();

        // Debug output to see what's in the session
        sessionManager.dumpPreferences();

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            System.out.println("No active session detected in initialize()");
            showError("No active session found. Please log in again.");
            disableFormFields(true);
            scheduleLoginRedirect();
            return;
        }

        System.out.println("Active session detected, loading profile");
        loadUserProfile();
    }

    /**
     * Disables or enables all form fields
     */
    private void disableFormFields(boolean disable) {
        emailField.setDisable(disable);
        firstNameField.setDisable(disable);
        lastNameField.setDisable(disable);
        currentPasswordField.setDisable(disable);
        newPasswordField.setDisable(disable);
        confirmPasswordField.setDisable(disable);
        saveButton.setDisable(disable);
    }

    /**
     * Loads the user profile data
     */
    private void loadUserProfile() {
        String username = sessionManager.getCurrentUsername();

        System.out.println("Loading profile for user: " + username);

        if (username == null || username.isEmpty()) {
            System.out.println("Username is null or empty despite session token existing");
            showError("Session error: Username not found. Please login again.");
            scheduleLoginRedirect();
            return;
        }

        // Set the username field (non-editable)
        usernameField.setText(username);

        try {
            // In a real application, load user data from your database
            // For now, we'll use mock data
            mockLoadUserData(username);

            System.out.println("User profile loaded successfully");
            clearMessages();
        } catch (Exception e) {
            System.err.println("Error loading profile: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load profile data: " + e.getMessage());
        }
    }

    /**
     * Mock method to simulate loading user data
     * Replace with actual database/API calls in your application
     */
    private void mockLoadUserData(String username) {
        // Create a user object and populate the form
        currentUser = new User();
        currentUser.setUsername(username);
        currentUser.setEmail(username + "@example.com");
        currentUser.setNom("Doe");
        currentUser.setPrenom("John");

        // Set form fields
        emailField.setText(currentUser.getEmail());
        firstNameField.setText(currentUser.getPrenom());
        lastNameField.setText(currentUser.getNom());
    }

    /**
     * Handles the save profile button action
     */
    @FXML
    private void handleSaveProfile() {
        clearMessages();

        // Check if session is still valid
        if (!sessionManager.isLoggedIn()) {
            showError("Session expired. Please login again.");
            scheduleLoginRedirect();
            return;
        }

        // Validate fields
        if (emailField.getText().isEmpty()) {
            showError("Email cannot be empty.");
            return;
        }
        if (firstNameField.getText().isEmpty()) {
            showError("First Name cannot be empty.");
            return;
        }
        if (lastNameField.getText().isEmpty()) {
            showError("Last Name cannot be empty.");
            return;
        }

        // Validate email format
        if (!isValidEmail(emailField.getText())) {
            showError("Please enter a valid email address.");
            return;
        }

        // Check if user wants to change password
        boolean changingPassword = !currentPasswordField.getText().isEmpty() ||
                !newPasswordField.getText().isEmpty() ||
                !confirmPasswordField.getText().isEmpty();

        if (changingPassword) {
            // Validate password fields
            if (currentPasswordField.getText().isEmpty()) {
                showError("Current password is required to change password.");
                return;
            }

            if (newPasswordField.getText().isEmpty()) {
                showError("New password cannot be empty.");
                return;
            }

            if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                showError("New password and confirmation do not match.");
                return;
            }

            // Validate password strength
            if (newPasswordField.getText().length() < 8) {
                showError("Password must be at least 8 characters long.");
                return;
            }
        }

        try {
            // Update user object with new values
            currentUser.setEmail(emailField.getText());
            currentUser.setPrenom(firstNameField.getText());
            currentUser.setNom(lastNameField.getText());

            if (changingPassword) {
                // In a real app, you would hash the password before storing it
                currentUser.setPassword(newPasswordField.getText());
            }

            // In a real app, save to database:
            // userService.updateUser(currentUser);

            System.out.println("Profile updated successfully");
            showSuccess("Profile successfully updated!");

            // Clear password fields after successful update
            if (changingPassword) {
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
            }
        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            showError("Error updating profile: " + e.getMessage());
        }
    }

    /**
     * Handles the back button action to return to the previous screen
     */
    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/Dashboard.fxml")));
            Scene scene = new Scene(root);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading dashboard: " + e.getMessage());
        }
    }

    /**
     * Handles the logout button action
     */
    @FXML
    private void handleLogout() {
        // Clear the session
        sessionManager.clearSession();

        try {
            // Navigate back to login screen
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/Auth/Login.fxml")));
            Scene scene = new Scene(root);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading login screen: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading login screen: " + e.getMessage());
        }
    }

    /**
     * Shows an error message
     * @param message The error message to display
     */
    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
        errorMessage.setManaged(true);
        successMessage.setVisible(false);
        successMessage.setManaged(false);

        System.err.println("ERROR: " + message);
    }

    /**
     * Shows a success message
     * @param message The success message to display
     */
    private void showSuccess(String message) {
        successMessage.setText(message);
        successMessage.setVisible(true);
        successMessage.setManaged(true);
        errorMessage.setVisible(false);
        errorMessage.setManaged(false);

        System.out.println("SUCCESS: " + message);
    }

    /**
     * Clears all message displays
     */
    private void clearMessages() {
        errorMessage.setVisible(false);
        errorMessage.setManaged(false);
        successMessage.setVisible(false);
        successMessage.setManaged(false);
    }

    /**
     * Validates an email address format
     * @param email The email address to validate
     * @return true if the email format is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    /**
     * Schedules a redirect to the login page after a short delay
     */
    private void scheduleLoginRedirect() {
        System.out.println("Scheduling redirect to login page in 2 seconds");

        // Create a new thread to handle the delay
        new Thread(() -> {
            try {
                // Wait 2 seconds before redirecting
                Thread.sleep(2000);

                // Use JavaFX Platform.runLater to update UI from a non-UI thread
                javafx.application.Platform.runLater(() -> {
                    try {
                        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/Auth/Login.fxml")));
                        Scene scene = new Scene(root);
                        Stage stage = (Stage) usernameField.getScene().getWindow();
                        stage.setScene(scene);
                        stage.show();

                        System.out.println("Redirected to login page");
                    } catch (IOException e) {
                        System.err.println("Error redirecting to login page: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Redirect thread interrupted");
            }
        }).start();
    }

    /**
     * Public method to set user data directly
     */
    public void setUserData(User user) {
        if (user != null) {
            this.currentUser = user;
            usernameField.setText(user.getUsername());
            emailField.setText(user.getEmail());
            firstNameField.setText(user.getPrenom());
            lastNameField.setText(user.getNom());

            clearMessages();
        }
    }
}