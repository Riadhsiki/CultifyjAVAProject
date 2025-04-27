package controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.User;
import services.user.UserService;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class ProfileController {

    @FXML private TextField idField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneNumberField;
    @FXML private TextField genderField;
    @FXML private TextField dateOfBirthField;
    @FXML private TextField roleField;
    @FXML private TextField montantAPayerField;
    @FXML private TextField profilePicField;
    @FXML private Button browseButton;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Text errorMessage;
    @FXML private Text successMessage;
    @FXML private Button saveButton;
    @FXML private Button backButton;
    @FXML private Button logoutButton;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private User currentUser;
    private final UserService userService = new UserService();
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d{8}");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    );

    @FXML
    public void initialize() {
        clearMessages();
        sessionManager.dumpPreferences();

        if (!sessionManager.isLoggedIn()) {
            showError("No active session found. Please log in again.");
            disableFormFields(true);
            scheduleLoginRedirect();
            return;
        }

        // Restrict phone number input to 8 digits
        phoneNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                phoneNumberField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 8) {
                phoneNumberField.setText(oldValue);
            }
        });

        loadUserProfile();
    }

    private void disableFormFields(boolean disable) {
        emailField.setDisable(disable);
        firstNameField.setDisable(disable);
        lastNameField.setDisable(disable);
        phoneNumberField.setDisable(disable);
        profilePicField.setDisable(disable);
        browseButton.setDisable(disable);
        currentPasswordField.setDisable(disable);
        newPasswordField.setDisable(disable);
        confirmPasswordField.setDisable(disable);
        saveButton.setDisable(disable);
        backButton.setDisable(disable);
        logoutButton.setDisable(disable);
    }

    private void loadUserProfile() {
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
                return;
            }

            idField.setText(currentUser.getId() != null ? currentUser.getId().toString() : "");
            usernameField.setText(currentUser.getUsername());
            emailField.setText(currentUser.getEmail());
            firstNameField.setText(currentUser.getPrenom());
            lastNameField.setText(currentUser.getNom());
            phoneNumberField.setText(currentUser.getNumTel());
            genderField.setText(currentUser.getGender());
            dateOfBirthField.setText(currentUser.getDatedenaissance() != null ?
                    currentUser.getDatedenaissance().toString() : "");
            roleField.setText(currentUser.getRoles());
            montantAPayerField.setText(currentUser.getMontantAPayer() != null ?
                    String.format("%.2f", currentUser.getMontantAPayer()) : "0.00");
            profilePicField.setText(currentUser.getProfilePicture());

            clearMessages();
        } catch (Exception e) {
            showError("Failed to load profile data: " + e.getMessage());
            disableFormFields(true);
        }
    }

    @FXML
    private void handleBrowse(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        Stage stage = (Stage) browseButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            profilePicField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleSaveProfile() {
        clearMessages();

        if (!sessionManager.isLoggedIn()) {
            showError("Session expired. Please login again.");
            scheduleLoginRedirect();
            return;
        }

        // Validate fields
        String email = emailField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phoneNumber = phoneNumberField.getText().trim();
        String profilePic = profilePicField.getText().trim();

        if (email.isEmpty()) {
            showError("Email is required.");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Invalid email format.");
            return;
        }
        try {
            if (!email.equals(currentUser.getEmail()) && userService.emailExists(email)) {
                showError("Email is already in use.");
                return;
            }
        } catch (SQLException e) {
            showError("Error checking email: " + e.getMessage());
            return;
        }
        if (firstName.isEmpty()) {
            showError("First name is required.");
            return;
        }
        if (lastName.isEmpty()) {
            showError("Last name is required.");
            return;
        }
        if (phoneNumber.isEmpty()) {
            showError("Phone number is required.");
            return;
        }
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            showError("Phone number must be 8 digits.");
            return;
        }

        // Validate password change
        boolean changingPassword = !currentPasswordField.getText().isEmpty() ||
                !newPasswordField.getText().isEmpty() ||
                !confirmPasswordField.getText().isEmpty();

        if (changingPassword) {
            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (currentPassword.isEmpty()) {
                showError("Current password is required to change password.");
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
        }

        try {
            // Update user object
            currentUser.setEmail(email);
            currentUser.setPrenom(firstName);
            currentUser.setNom(lastName);
            currentUser.setNumTel(phoneNumber);
            currentUser.setProfilePicture(profilePic.isEmpty() ? "default.jpg" : profilePic);

            if (changingPassword) {
                currentUser.setPassword(newPasswordField.getText());
            }

            userService.updateUser(currentUser);

            showSuccess("Profile updated successfully!");
            if (changingPassword) {
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
            }
        } catch (Exception e) {
            showError("Error updating profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/userinterfaces/Dashboard.fxml"));
            if (loader.getLocation() == null) {
                showError("Dashboard not found.");
                return;
            }
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("CultureSketch - Dashboard");
            stage.show();
        } catch (IOException e) {
            showError("Error loading dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        sessionManager.clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Auth/login.fxml"));
            if (loader.getLocation() == null) {
                showError("Login page not found.");
                return;
            }
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("CultureSketch - Login");
            stage.show();
        } catch (IOException e) {
            showError("Error loading login screen: " + e.getMessage());
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
                        Stage stage = (Stage) usernameField.getScene().getWindow();
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

    public void setUserData(User user) {
        if (user != null) {
            this.currentUser = user;
            idField.setText(user.getId() != null ? user.getId().toString() : "");
            usernameField.setText(user.getUsername());
            emailField.setText(user.getEmail());
            firstNameField.setText(user.getPrenom());
            lastNameField.setText(user.getNom());
            phoneNumberField.setText(user.getNumTel());
            genderField.setText(user.getGender());
            dateOfBirthField.setText(user.getDatedenaissance() != null ?
                    user.getDatedenaissance().toString() : "");
            roleField.setText(user.getRoles());
            montantAPayerField.setText(user.getMontantAPayer() != null ?
                    String.format("%.2f", user.getMontantAPayer()) : "0.00");
            profilePicField.setText(user.getProfilePicture());
            clearMessages();
        }
    }
}