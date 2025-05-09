package controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.User;
import services.user.UserService;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.sql.Date;

public class ProfileController {

    @FXML private TextField imagePathField;
    @FXML private Label imagePreviewLabel;
    @FXML private Label imageErrorLabel;
    @FXML private ImageView profilePicView;
    @FXML private TextField usernameField;
    @FXML private Label usernameErrorLabel;
    @FXML private TextField emailField;
    @FXML private Label emailErrorLabel;
    @FXML private TextField firstNameField;
    @FXML private Label firstNameErrorLabel;
    @FXML private TextField lastNameField;
    @FXML private Label lastNameErrorLabel;
    @FXML private TextField phoneNumberField;
    @FXML private Label phoneNumberErrorLabel;
    @FXML private TextField genderField;
    @FXML private Label genderErrorLabel;
    @FXML private TextField ageField;
    @FXML private Label ageErrorLabel;
    @FXML private TextField roleField;
    @FXML private Label roleErrorLabel;
    @FXML private TextField montantAPayerField;
    @FXML private Label montantAPayerErrorLabel;
    @FXML private Button browseButton;
    @FXML private Button saveButton;
    @FXML private Button changePasswordButton;
    @FXML private Button backButton;
    @FXML private Button logoutButton;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private User currentUser;
    private final UserService userService = new UserService();
    private File selectedImageFile;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d{8}");
    private static final String UPLOADS_DIR = "C:/Users/adamo/cultify/public/uploads/images";
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private boolean hasChanges = false;

    @FXML
    public void initialize() {
        clearErrorLabels();
        sessionManager.dumpPreferences();

        // Add tooltips to buttons
        setupTooltips();

        if (!sessionManager.isLoggedIn()) {
            showAlert("No active session found. Please log in again.", Alert.AlertType.WARNING);
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
            checkForChanges();
        });

        // Add change listeners to editable fields to enable Save button
        emailField.textProperty().addListener((obs, old, newVal) -> checkForChanges());
        firstNameField.textProperty().addListener((obs, old, newVal) -> checkForChanges());
        lastNameField.textProperty().addListener((obs, old, newVal) -> checkForChanges());

        // Initially disable Save button
        saveButton.setDisable(true);

        loadUserProfile();
    }

    private void setupTooltips() {
        browseButton.setTooltip(new Tooltip("Select a new profile picture"));
        saveButton.setTooltip(new Tooltip("Save changes to your profile"));
        changePasswordButton.setTooltip(new Tooltip("Change your password"));
        backButton.setTooltip(new Tooltip("Return to dashboard"));
        logoutButton.setTooltip(new Tooltip("Log out of your account"));
    }

    private void checkForChanges() {
        boolean changed = false;
        if (currentUser != null) {
            if (!emailField.getText().trim().equals(currentUser.getEmail()) ||
                    !firstNameField.getText().trim().equals(currentUser.getPrenom()) ||
                    !lastNameField.getText().trim().equals(currentUser.getNom()) ||
                    !phoneNumberField.getText().trim().equals(currentUser.getNumTel()) ||
                    selectedImageFile != null) {
                changed = true;
            }
        }
        hasChanges = changed;
        saveButton.setDisable(!hasChanges);
    }

    private void disableFormFields(boolean disable) {
        imagePathField.setDisable(disable);
        usernameField.setDisable(disable);
        emailField.setDisable(disable);
        firstNameField.setDisable(disable);
        lastNameField.setDisable(disable);
        phoneNumberField.setDisable(disable);
        genderField.setDisable(disable);
        ageField.setDisable(disable);
        roleField.setDisable(disable);
        montantAPayerField.setDisable(disable);
        browseButton.setDisable(disable);
        saveButton.setDisable(disable || !hasChanges);
        changePasswordButton.setDisable(disable);
        backButton.setDisable(disable);
        logoutButton.setDisable(disable);
    }

    private void loadUserProfile() {
        String username = sessionManager.getCurrentUsername();
        if (username == null || username.isEmpty()) {
            showAlert("Session error: Username not found. Please login again.", Alert.AlertType.WARNING);
            scheduleLoginRedirect();
            return;
        }

        try {
            currentUser = userService.getUserByUsername(username);
            if (currentUser == null) {
                showAlert("User not found in database.", Alert.AlertType.ERROR);
                scheduleLoginRedirect();
                return;
            }

            usernameField.setText(currentUser.getUsername());
            emailField.setText(currentUser.getEmail());
            firstNameField.setText(currentUser.getPrenom());
            lastNameField.setText(currentUser.getNom());
            phoneNumberField.setText(currentUser.getNumTel());
            genderField.setText(currentUser.getGender());
            ageField.setText(currentUser.getDatedenaissance() != null ?
                    String.valueOf(calculateAge(currentUser.getDatedenaissance())) : "");
            roleField.setText(currentUser.getRoles());
            montantAPayerField.setText(currentUser.getMontantAPayer() != null ?
                    String.format("%.2f", currentUser.getMontantAPayer()) : "0.00");

            // Load profile picture
            loadProfilePicture();

            clearErrorLabels();
        } catch (Exception e) {
            showAlert("Failed to load profile data: " + e.getMessage(), Alert.AlertType.ERROR);
            disableFormFields(true);
        }
    }

    private void loadProfilePicture() {
        String profilePicPath = currentUser.getProfilePicture();
        if (profilePicPath != null && !profilePicPath.isEmpty()) {
            try {
                File imageFile = new File(profilePicPath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    profilePicView.setImage(image);
                    imagePathField.setText(imageFile.getName());
                    if (imagePreviewLabel != null) {
                        imagePreviewLabel.setText("Image loaded");
                    }
                } else {
                    loadDefaultProfilePicture();
                }
            } catch (Exception e) {
                System.err.println("Failed to load profile picture: " + e.getMessage());
                loadDefaultProfilePicture();
            }
        } else {
            loadDefaultProfilePicture();
        }
    }

    private void loadDefaultProfilePicture() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default.jpg"));
            profilePicView.setImage(defaultImage);
            imagePathField.setText("");
            if (imagePreviewLabel != null) {
                imagePreviewLabel.setText("No image selected");
            }
        } catch (Exception e) {
            System.err.println("Failed to load default profile picture: " + e.getMessage());
            profilePicView.setImage(null);
            if (imageErrorLabel != null) {
                imageErrorLabel.setText("Default image not found");
            }
            if (imagePreviewLabel != null) {
                imagePreviewLabel.setText("");
            }
        }
    }

    private int calculateAge(Date dateOfBirth) {
        if (dateOfBirth == null) return 0;
        LocalDate birthDate = dateOfBirth.toLocalDate();
        LocalDate currentDate = LocalDate.now();
        return Period.between(birthDate, currentDate).getYears();
    }

    @FXML
    private void handleBrowse(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        Stage stage = (Stage) browseButton.getScene().getWindow();
        selectedImageFile = fileChooser.showOpenDialog(stage);

        if (selectedImageFile != null) {
            // Validate image
            if (!validateImage(selectedImageFile)) {
                selectedImageFile = null;
                return;
            }

            try {
                Image image = new Image(selectedImageFile.toURI().toString());
                profilePicView.setImage(image);
                imagePathField.setText(selectedImageFile.getName());
                if (imagePreviewLabel != null) {
                    imagePreviewLabel.setText("Image selected");
                }
                if (imageErrorLabel != null) {
                    imageErrorLabel.setText("");
                }
                checkForChanges();
            } catch (Exception e) {
                if (imageErrorLabel != null) {
                    imageErrorLabel.setText("Invalid image file.");
                }
                selectedImageFile = null;
            }
        }
    }

    private boolean validateImage(File imageFile) {
        // Check file size
        if (imageFile.length() > MAX_IMAGE_SIZE) {
            if (imageErrorLabel != null) {
                imageErrorLabel.setText("Image size exceeds 5MB.");
            }
            return false;
        }

        // Check file extension
        String extension = imageFile.getName().substring(imageFile.getName().lastIndexOf(".")).toLowerCase();
        if (!extension.matches("\\.(png|jpg|jpeg|gif)")) {
            if (imageErrorLabel != null) {
                imageErrorLabel.setText("Only PNG, JPG, JPEG, and GIF files are allowed.");
            }
            return false;
        }

        return true;
    }

    @FXML
    private void handleSaveProfile() {
        clearErrorLabels();

        if (!sessionManager.isLoggedIn()) {
            showAlert("Session expired. Please login again.", Alert.AlertType.WARNING);
            scheduleLoginRedirect();
            return;
        }

        // Confirm save action
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Save");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to save changes to your profile?");
        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() != ButtonType.OK) {
            return;
        }

        // Validate fields
        if (validateFields()) {
            try {
                // Update user object
                currentUser.setEmail(emailField.getText().trim());
                currentUser.setPrenom(firstNameField.getText().trim());
                currentUser.setNom(lastNameField.getText().trim());
                currentUser.setNumTel(phoneNumberField.getText().trim());

                // Process image if selected
                if (selectedImageFile != null) {
                    String newImagePath = processImage(selectedImageFile);
                    currentUser.setProfilePicture(newImagePath);
                }

                userService.updateUser(currentUser);
                showAlert("Profile updated successfully!", Alert.AlertType.INFORMATION);
                clearFields();
                hasChanges = false;
                saveButton.setDisable(true);
                loadUserProfile(); // Reload to ensure UI consistency
            } catch (Exception e) {
                showAlert("Error updating profile: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        // Validate First Name
        if (firstNameField.getText().trim().isEmpty()) {
            firstNameErrorLabel.setText("First name is required.");
            isValid = false;
        } else if (firstNameField.getText().trim().length() < 2) {
            firstNameErrorLabel.setText("First name must be at least 2 characters.");
            isValid = false;
        }

        // Validate Last Name
        if (lastNameField.getText().trim().isEmpty()) {
            lastNameErrorLabel.setText("Last name is required.");
            isValid = false;
        } else if (lastNameField.getText().trim().length() < 2) {
            lastNameErrorLabel.setText("Last name must be at least 2 characters.");
            isValid = false;
        }

        // Validate Email
        if (emailField.getText().trim().isEmpty()) {
            emailErrorLabel.setText("Email is required.");
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(emailField.getText().trim()).matches()) {
            emailErrorLabel.setText("Invalid email format.");
            isValid = false;
        } else {
            try {
                if (!emailField.getText().trim().equals(currentUser.getEmail()) && userService.emailExists(emailField.getText().trim())) {
                    emailErrorLabel.setText("Email is already in use.");
                    isValid = false;
                }
            } catch (SQLException e) {
                emailErrorLabel.setText("Error checking email: " + e.getMessage());
                isValid = false;
            }
        }

        // Validate Phone Number
        if (phoneNumberField.getText().trim().isEmpty()) {
            phoneNumberErrorLabel.setText("Phone number is required.");
            isValid = false;
        } else if (!PHONE_PATTERN.matcher(phoneNumberField.getText().trim()).matches()) {
            phoneNumberErrorLabel.setText("Phone number must be 8 digits.");
            isValid = false;
        }

        return isValid;
    }

    private String processImage(File imageFile) throws IOException {
        Path uploadDir = Paths.get(UPLOADS_DIR);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String extension = imageFile.getName().substring(imageFile.getName().lastIndexOf("."));
        String newFilename = UUID.randomUUID() + extension;
        Path destination = uploadDir.resolve(newFilename);

        Files.copy(imageFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toString();
    }

    private void clearFields() {
        selectedImageFile = null;
        imagePathField.clear();
        if (imagePreviewLabel != null) {
            imagePreviewLabel.setText("");
        }
        loadProfilePicture();
        clearErrorLabels();
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        navigateTo("/userinterfaces/changePassword.fxml", "CultureSketch - Change Password");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigateTo("/userinterfaces/Dashboard.fxml", "CultureSketch - Dashboard");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        sessionManager.clearSession();
        navigateTo("/Auth/login.fxml", "CultureSketch - Login");
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

    private void clearErrorLabels() {
        if (imageErrorLabel != null) imageErrorLabel.setText("");
        if (usernameErrorLabel != null) usernameErrorLabel.setText("");
        if (firstNameErrorLabel != null) firstNameErrorLabel.setText("");
        if (lastNameErrorLabel != null) lastNameErrorLabel.setText("");
        if (emailErrorLabel != null) emailErrorLabel.setText("");
        if (phoneNumberErrorLabel != null) phoneNumberErrorLabel.setText("");
        if (genderErrorLabel != null) genderErrorLabel.setText("");
        if (ageErrorLabel != null) ageErrorLabel.setText("");
        if (roleErrorLabel != null) roleErrorLabel.setText("");
        if (montantAPayerErrorLabel != null) montantAPayerErrorLabel.setText("");
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

    public void setUserData(User user) {
        if (user != null) {
            this.currentUser = user;
            loadUserProfile();
        }
    }
}
