package controllers.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import models.User;
import services.user.UserService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AjouterUserController implements Initializable {

    @FXML
    private TextField nomTextField;

    @FXML
    private TextField prenomTextField;

    @FXML
    private TextField usernameTextField;

    @FXML
    private TextField numTextField;

    @FXML
    private TextField emailTextField;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private DatePicker dateNaissancePicker;

    @FXML
    private ComboBox<String> rolesComboBox;

    @FXML
    private TextField profilepicTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private Button browseButton;

    // Simple regex for basic email validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    );

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize gender ComboBox
        genderComboBox.getItems().addAll("Homme", "Femme");

        // Initialize roles ComboBox
        rolesComboBox.getItems().addAll("Utilisateur", "Organisateur");

        // Set minimum date (12 years ago from today)
        LocalDate minDate = LocalDate.now().minusYears(12);

        // Disable future dates and dates less than 12 years ago
        dateNaissancePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(minDate));
            }
        });

        // Add listener for phone number to ensure only digits and max 8 characters
        numTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                numTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 8) {
                numTextField.setText(oldValue);
            }
        });
    }

    @FXML
    void browsePhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(browseButton.getScene().getWindow());
        if (selectedFile != null) {
            profilepicTextField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    void clearFields(ActionEvent event) {
        nomTextField.clear();
        prenomTextField.clear();
        usernameTextField.clear();
        numTextField.clear();
        emailTextField.clear();
        genderComboBox.getSelectionModel().clearSelection();
        dateNaissancePicker.setValue(null);
        rolesComboBox.getSelectionModel().clearSelection();
        profilepicTextField.clear();
        passwordTextField.clear();
    }

    @FXML
    void ajouterUserAction(ActionEvent event) {

        String nom = nomTextField.getText().trim();
        String prenom = prenomTextField.getText().trim();
        String username = usernameTextField.getText().trim();
        String numTel = numTextField.getText().trim();
        String email = emailTextField.getText().trim();
        String gender = genderComboBox.getValue();
        LocalDate dateNaissanceLocal = dateNaissancePicker.getValue();
        String roles = rolesComboBox.getValue();
        String profilePic = profilepicTextField.getText().trim();
        String password = passwordTextField.getText();

        // Check for empty mandatory fields
        if (nom.isEmpty()) {
            showErrorAlert("Erreur de saisie", "Le champ 'Nom' ne peut pas être vide.");
            return;
        }
        if (prenom.isEmpty()) {
            showErrorAlert("Erreur de saisie", "Le champ 'Prénom' ne peut pas être vide.");
            return;
        }
        if (username.isEmpty()) {
            showErrorAlert("Erreur de saisie", "Le champ 'Username' ne peut pas être vide.");
            return;
        }
        if (numTel.isEmpty()) {
            showErrorAlert("Erreur de saisie", "Le champ 'Numéro téléphone' ne peut pas être vide.");
            return;
        }
        if (numTel.length() != 8) {
            showErrorAlert("Erreur de saisie", "Le numéro de téléphone doit contenir exactement 8 chiffres.");
            return;
        }
        if (email.isEmpty()) {
            showErrorAlert("Erreur de saisie", "Le champ 'Email' ne peut pas être vide.");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showErrorAlert("Erreur de saisie", "Le format de l'adresse email est invalide.");
            return;
        }
        if (gender == null) {
            showErrorAlert("Erreur de saisie", "Veuillez sélectionner un genre.");
            return;
        }
        if (dateNaissanceLocal == null) {
            showErrorAlert("Erreur de saisie", "Le champ 'Date de naissance' ne peut pas être vide.");
            return;
        }

        Period period = Period.between(dateNaissanceLocal, LocalDate.now());
        if (period.getYears() < 12) {
            showErrorAlert("Erreur de saisie", "L'utilisateur doit avoir au moins 12 ans.");
            return;
        }

        if (roles == null) {
            showErrorAlert("Erreur de saisie", "Veuillez sélectionner un rôle.");
            return;
        }
        if (password.isEmpty()) {
            showErrorAlert("Erreur de saisie", "Le champ 'Password' ne peut pas être vide.");
            return;
        }
        if (password.length() < 8) {
            showErrorAlert("Erreur de saisie", "Le mot de passe doit contenir au moins 8 caractères.");
            return;
        }

        // --- Input Validation End ---

        try {
            // Convert LocalDate to java.sql.Date
            Date dateNaissance = Date.valueOf(dateNaissanceLocal);

            // Create User object (montantAPayer set to null)
            User user = new User(nom, prenom, username, numTel, email, gender,
                    dateNaissance, profilePic, password, roles, null);

            // Add user via service
            UserService userService = new UserService();
            userService.add(user);

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Utilisateur ajouté avec succès!");
            alert.showAndWait();

            // Navigate to the details view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/userinterfaces/DetailUser.fxml"));
            Parent root = loader.load();
            DetailsUserController detailController = loader.getController();
            detailController.setUserDetails(user);

            // Change the scene to the detail view
            nomTextField.getScene().setRoot(root);

        } catch (SQLException e) {
            showErrorAlert("Erreur de base de données", "Erreur lors de l'ajout de l'utilisateur: " + e.getMessage());
        } catch (IOException e) {
            showErrorAlert("Erreur de chargement", "Erreur lors du chargement de la vue Détails: " + e.getMessage());
        } catch (Exception e) {
            showErrorAlert("Erreur Inattendue", "Une erreur inattendue est survenue: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}