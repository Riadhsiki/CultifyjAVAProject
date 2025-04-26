package controllers.Auth;

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
import services.auth.UserRegistrationService;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class RegisterController {

    // FXML Components
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
    @FXML private TextField profilePicField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Text errorMessage;
    @FXML private Button browseButton;

    // Services
    private final UserRegistrationService registrationService = new UserRegistrationService();

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    );
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{4,20}$"
    );
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    );

    @FXML
    public void initialize() {
        // Initialize role combobox
        roleComboBox.getItems().addAll("ROLE_USER", "ROLE_ORGANIZER");
        roleComboBox.getSelectionModel().selectFirst();

        // Set minimum date (12 years ago from today)
        LocalDate minDate = LocalDate.now().minusYears(12);
        dateOfBirthPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(minDate));
            }
        });

        // Phone number validation - only digits and max 8 characters
        numTelField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                numTelField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 8) {
                numTelField.setText(oldValue);
            }
        });
    }

    @FXML
    private void handleBrowse(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(browseButton.getScene().getWindow());
        if (selectedFile != null) {
            profilePicField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        hideErrors();

        // Get form values
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String numTel = numTelField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String profilePic = profilePicField.getText().trim();
        String role = roleComboBox.getValue();

        // Get selected gender
        String gender = getSelectedGender();
        LocalDate localDateOfBirth = dateOfBirthPicker.getValue();

        // Validate inputs
        if (!validateInputs(nom, prenom, username, email, numTel, password,
                confirmPassword, gender, localDateOfBirth, profilePic)) {
            return;
        }

        try {
            // Create user object
            User user = new User(
                    nom, prenom, username, numTel, email, gender,
                    Date.valueOf(localDateOfBirth),
                    profilePic.isEmpty() ? "default.jpg" : profilePic,
                    password, role, null
            );

            // Attempt registration
            if (registrationService.registerUser(user)) {
                SessionManager.getInstance().setTemporaryMessage("Registration successful. Please login.");
                navigateToLogin(event);
            } else {
                showError("Username or email already exists.");
            }
        } catch (SQLException | IOException e) {
            showError("An error occurred: " + e.getMessage());
        }
    }

    private String getSelectedGender() {
        Toggle selectedGender = genderGroup.getSelectedToggle();
        if (selectedGender == maleRadio) return "Male";
        if (selectedGender == femaleRadio) return "Female";
        if (selectedGender == otherRadio) return "Other";
        return null;
    }

    private boolean validateInputs(String nom, String prenom, String username, String email,
                                   String numTel, String password, String confirmPassword,
                                   String gender, LocalDate dateOfBirth, String profilePic) {
        // Check empty fields
        if (nom.isEmpty()) {
            showError("Last name (Nom) is required");
            return false;
        }
        if (prenom.isEmpty()) {
            showError("First name (Prénom) is required");
            return false;
        }
        if (username.isEmpty()) {
            showError("Username is required");
            return false;
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            showError("Username must be 4-20 characters (letters, numbers, underscores)");
            return false;
        }
        if (email.isEmpty()) {
            showError("Email is required");
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Invalid email format");
            return false;
        }
        if (numTel.isEmpty()) {
            showError("Phone number is required");
            return false;
        }
        if (numTel.length() != 8) {
            showError("Phone number must be 8 digits");
            return false;
        }
        if (gender == null) {
            showError("Please select a gender");
            return false;
        }
        if (dateOfBirth == null) {
            showError("Date of birth is required");
            return false;
        }
        if (Period.between(dateOfBirth, LocalDate.now()).getYears() < 12) {
            showError("You must be at least 12 years old");
            return false;
        }
        if (password.isEmpty()) {
            showError("Password is required");
            return false;
        }
        if (password.length() < 8) {
            showError("Password must be at least 8 characters");
            return false;
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            showError("Password must contain at least one digit, one lowercase letter, one uppercase letter, one special character (@#$%^&+=) and no whitespace");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return false;
        }
        return true;
    }

    @FXML
    private void navigateToLogin(ActionEvent event) throws IOException {
        Parent loginParent = FXMLLoader.load(getClass().getResource("/Auth/login.fxml"));
        Scene loginScene = new Scene(loginParent);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(loginScene);
        window.show();
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
        errorMessage.setManaged(true);
    }

    private void hideErrors() {
        errorMessage.setVisible(false);
        errorMessage.setManaged(false);
    }
}