package controllers.Auth;

import controllers.Auth.SessionManager;
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
import services.Auth.UserRegistrationService;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

public class RegisterController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField numTelField;
    @FXML private RadioButton maleRadio;
    @FXML private RadioButton femaleRadio;
    @FXML private RadioButton otherRadio;
    @FXML private ToggleGroup genderGroup;
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Text errorMessage;

    private final UserRegistrationService registrationService = new UserRegistrationService();

    /**
     * Handle register button click
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        // Clear previous error messages
        hideErrors();

        // Get form values
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String numTel = numTelField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Get selected gender
        String gender = null;
        Toggle selectedGender = genderGroup.getSelectedToggle();
        if (selectedGender == maleRadio) {
            gender = "Male";
        } else if (selectedGender == femaleRadio) {
            gender = "Female";
        } else if (selectedGender == otherRadio) {
            gender = "Other";
        }

        // Get date of birth
        LocalDate localDateOfBirth = dateOfBirthPicker.getValue();
        Date dateOfBirth = null;
        if (localDateOfBirth != null) {
            dateOfBirth = Date.valueOf(localDateOfBirth);
        }

        // Basic validation
        if (nom.isEmpty() || prenom.isEmpty() || username.isEmpty() || email.isEmpty()) {
            showError("Please fill in all required fields");
            return;
        }

        if (password.isEmpty()) {
            showError("Password is required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (gender == null) {
            showError("Please select a gender");
            return;
        }

        try {
            // Create user object
            User user = new User(nom, prenom, username, numTel, email, gender,
                    dateOfBirth, "default.jpg", password, "ROLE_USER", null);

            // Attempt to register the user
            boolean success = registrationService.registerUser(user);

            if (success) {
                // Registration successful, navigate to login
                SessionManager.getInstance().setTemporaryMessage("Registration successful. Please login.");
                navigateToLogin(event);
            } else {
                showError("Username already exists. Please choose another.");
            }
        } catch (SQLException | IOException e) {
            showError("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Navigate to login page
     */
    @FXML
    private void navigateToLogin(ActionEvent event) throws IOException {
        Parent loginParent = FXMLLoader.load(getClass().getResource("/Auth/login.fxml"));
        Scene loginScene = new Scene(loginParent);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(loginScene);
        window.show();
    }

    /**
     * Show an error message
     */
    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
        errorMessage.setManaged(true);
    }

    /**
     * Hide error messages
     */
    private void hideErrors() {
        errorMessage.setVisible(false);
        errorMessage.setManaged(false);
    }

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Set default values if needed
    }
}