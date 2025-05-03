package controllers.auth;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.User;
import services.auth.UserRegistrationService;
import utils.EmailSender;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;

public class RegisterController {

    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField numTelField;
    @FXML private RadioButton maleRadio;
    @FXML private RadioButton femaleRadio;
    @FXML private RadioButton otherRadio;
    @FXML private ToggleGroup genderGroup;
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private TextField profilePicField;
    @FXML private Button browseButton;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordTextField;
    @FXML private Button togglePasswordButton;
    @FXML private Text errorMessage;
    @FXML private Text successMessage;

    private final UserRegistrationService registrationService = new UserRegistrationService();
    private final EmailSender emailSender = new EmailSender();
    private boolean isPasswordVisible = false;

    @FXML
    private void initialize() {
        // Populate roleComboBox
        roleComboBox.getItems().addAll("User", "Artist", "Organizer");
        roleComboBox.setValue("User");

        // Initialize toggle button with eye icon
        updateToggleButtonIcon();
    }

    @FXML
    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(browseButton.getScene().getWindow());
        if (file != null) {
            profilePicField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleRegister() {
        // Clear messages
        errorMessage.setVisible(false);
        successMessage.setVisible(false);

        // Get input values
        String prenom = prenomField.getText().trim();
        String nom = nomField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String numTel = numTelField.getText().trim();
        String gender = getSelectedGender();
        LocalDate dateOfBirth = dateOfBirthPicker.getValue();
        String profilePic = profilePicField.getText().trim();
        String role = roleComboBox.getValue();
        String password = isPasswordVisible ? passwordTextField.getText() : passwordField.getText();
        String confirmPassword = isPasswordVisible ? confirmPasswordTextField.getText() : confirmPasswordField.getText();

        // Validate inputs
        if (!validateInputs(prenom, nom, username, email, numTel, gender, dateOfBirth, role, password, confirmPassword)) {
            return;
        }

        User user = new User();
        user.setPrenom(prenom);
        user.setNom(nom);
        user.setUsername(username);
        user.setEmail(email);
        user.setNumTel(numTel);
        user.setGender(gender);
        user.setDatedenaissance(Date.valueOf(dateOfBirth));
        user.setProfilePicture(profilePic.isEmpty() ? null : profilePic);
        user.setRoles(role);
        user.setPassword(password);
        user.setMontantAPayer(0.0f);

        try {
            if (registrationService.registerUser(user)) {
                // Send welcome email with attachment
                String subject = "Welcome to CultureSketch!";
                String body = "Dear {prenom},\n\n" +
                        "Welcome to Cultify, {username}! We're thrilled to have you join our community of creators and art enthusiasts.\n\n" +
                        "With CultureSketch, you can:\n" +
                        "- Create and share your unique sketches\n" +
                        "- Explore a vibrant gallery of artwork\n" +
                        "- Connect with other artists and organizers\n\n" +
                        "Get started by logging in and exploring your dashboard. Your role as a " + role + " opens up exciting possibilities!\n\n" +
                        "Attached is our logo to inspire your creative journey.\n\n" +
                        "Happy sketching!\n" +
                        "The Cultify Team";
                try {
                    emailSender.sendEmailWithAttachment(email, subject, body, prenom, username);
                } catch (RuntimeException e) {
                    errorMessage.setText("Registration successful, but failed to send welcome email: " + e.getMessage());
                    errorMessage.setVisible(true);
                    return;
                }

                errorMessage.setVisible(false);
                successMessage.setText("Registration successful! Check your email for a welcome message.");
                successMessage.setVisible(true);

                // Navigate to login
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/login.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("CultureSketch - Login");
                stage.show();
            } else {
                errorMessage.setText("Username or email already exists.");
                errorMessage.setVisible(true);
                successMessage.setVisible(false);
            }
        } catch (SQLException | IOException e) {
            errorMessage.setText("Registration error: " + e.getMessage());
            errorMessage.setVisible(true);
            successMessage.setVisible(false);
        }
    }

    @FXML
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("CultureSketch - Login");
            stage.show();
        } catch (IOException e) {
            errorMessage.setText("Navigation error: " + e.getMessage());
            errorMessage.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            // Show plain text
            passwordTextField.setText(passwordField.getText());
            confirmPasswordTextField.setText(confirmPasswordField.getText());
            passwordTextField.setVisible(true);
            passwordField.setVisible(false);
            confirmPasswordTextField.setVisible(true);
            confirmPasswordField.setVisible(false);
        } else {
            // Show password fields
            passwordField.setText(passwordTextField.getText());
            confirmPasswordField.setText(confirmPasswordTextField.getText());
            passwordTextField.setVisible(false);
            passwordField.setVisible(true);
            confirmPasswordTextField.setVisible(false);
            confirmPasswordField.setVisible(true);
        }

        updateToggleButtonIcon();
    }

    private void updateToggleButtonIcon() {
        try {
            String iconPath = isPasswordVisible ? "/images/eye-off.png" : "/images/eye.png";
            Image icon = new Image(getClass().getResourceAsStream(iconPath));
            ImageView imageView = new ImageView(icon);
            imageView.setFitWidth(20);
            imageView.setFitHeight(20);
            togglePasswordButton.setGraphic(imageView);
        } catch (NullPointerException e) {
            errorMessage.setText("Error loading toggle icon: Image not found at /images/");
            errorMessage.setVisible(true);
            e.printStackTrace();
        }
    }

    private String getSelectedGender() {
        if (maleRadio.isSelected()) return "Male";
        if (femaleRadio.isSelected()) return "Female";
        if (otherRadio.isSelected()) return "Other";
        return null;
    }

    private boolean validateInputs(String prenom, String nom, String username, String email, String numTel,
                                   String gender, LocalDate dateOfBirth, String role, String password, String confirmPassword) {
        // Check for empty fields
        if (prenom.isEmpty() || nom.isEmpty() || username.isEmpty() || email.isEmpty() || numTel.isEmpty() ||
                gender == null || dateOfBirth == null || role == null || password.isEmpty() || confirmPassword.isEmpty()) {
            errorMessage.setText("All required fields must be filled.");
            errorMessage.setVisible(true);
            return false;
        }

        // Validate username (4-20 characters, letters, numbers, underscore)
        if (!username.matches("^[a-zA-Z0-9_]{4,20}$")) {
            errorMessage.setText("Username must be 4-20 characters (letters, numbers, underscore).");
            errorMessage.setVisible(true);
            return false;
        }

        // Validate email
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            errorMessage.setText("Please enter a valid email address.");
            errorMessage.setVisible(true);
            return false;
        }

        // Validate phone number (8 digits)
        if (!numTel.matches("^\\d{8}$")) {
            errorMessage.setText("Phone number must be exactly 8 digits.");
            errorMessage.setVisible(true);
            return false;
        }

        // Validate age (at least 12 years old)
        if (Period.between(dateOfBirth, LocalDate.now()).getYears() < 12) {
            errorMessage.setText("You must be at least 12 years old.");
            errorMessage.setVisible(true);
            return false;
        }

        // Validate password (at least 8 characters)
        if (password.length() < 8) {
            errorMessage.setText("Password must be at least 8 characters.");
            errorMessage.setVisible(true);
            return false;
        }

        // Validate password match
        if (!password.equals(confirmPassword)) {
            errorMessage.setText("Passwords do not match.");
            errorMessage.setVisible(true);
            return false;
        }

        return true;
    }
}
