package controllers.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
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

public class ModifierUserController implements Initializable {

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

    private User userToUpdate;
    private UserService userService;

    // Simple regex for basic email validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    );

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userService = new UserService();

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

    public void setUserToUpdate(User user) {
        this.userToUpdate = user;

        // Populate fields with user data
        nomTextField.setText(user.getNom());
        prenomTextField.setText(user.getPrenom());
        usernameTextField.setText(user.getUsername());
        numTextField.setText(user.getNumTel());
        emailTextField.setText(user.getEmail());

        // Set gender in combobox
        genderComboBox.setValue(user.getGender());

        // Convert SQL date to LocalDate
        Date sqlDate = user.getDatedenaissance();
        if (sqlDate != null) {
            LocalDate localDate = sqlDate.toLocalDate();
            dateNaissancePicker.setValue(localDate);
        }

        // Set role in combobox
        rolesComboBox.setValue(user.getRoles());

        // Set profile pic path
        if (user.getProfilePicture() != null) {
            profilepicTextField.setText(user.getProfilePicture());
        }

        // Don't set password as it should be re-entered for security reasons
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
    void handleCancel(ActionEvent event) {
        try {
            // Go back to details view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/userinterfaces/DetailUser.fxml"));
            Parent root = loader.load();
            DetailsUserController detailsController = loader.getController();
            detailsController.setUserDetails(userToUpdate);

            // Switch scene
            nomTextField.getScene().setRoot(root);

        } catch (IOException e) {
            showErrorAlert("Erreur de navigation", "Impossible de retourner à la page de détails: " + e.getMessage());
        }
    }

    @FXML
    void handleUpdateUser(ActionEvent event) {
        // --- Input Validation Start ---
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

        // Validate age (minimum 12 years)
        Period period = Period.between(dateNaissanceLocal, LocalDate.now());
        if (period.getYears() < 12) {
            showErrorAlert("Erreur de saisie", "L'utilisateur doit avoir au moins 12 ans.");
            return;
        }

        if (roles == null) {
            showErrorAlert("Erreur de saisie", "Veuillez sélectionner un rôle.");
            return;
        }

        // Only validate password if it's been changed (not empty)
        if (!password.isEmpty() && password.length() < 8) {
            showErrorAlert("Erreur de saisie", "Le mot de passe doit contenir au moins 8 caractères.");
            return;
        }

        try {
            // Convert LocalDate to java.sql.Date
            Date dateNaissance = Date.valueOf(dateNaissanceLocal);

            // Update user object
            userToUpdate.setNom(nom);
            userToUpdate.setPrenom(prenom);
            userToUpdate.setUsername(username);
            userToUpdate.setNumTel(numTel);
            userToUpdate.setEmail(email);
            userToUpdate.setGender(gender);
            userToUpdate.setDatedenaissance(dateNaissance);
            userToUpdate.setRoles(roles);

            if (!profilePic.isEmpty()) {
                userToUpdate.setProfilePicture(profilePic);
            }

            // Only update password if a new one was provided
            if (!password.isEmpty()) {
                userToUpdate.setPassword(password);
            }

            // Update user in database
            userService.update(userToUpdate);

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Utilisateur modifié avec succès!");
            alert.showAndWait();

            // Navigate back to details view with updated user
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/userinterfaces/DetailUser.fxml"));
            Parent root = loader.load();
            DetailsUserController detailsController = loader.getController();
            detailsController.setUserDetails(userToUpdate);

            // Switch scene
            nomTextField.getScene().setRoot(root);

        } catch (SQLException e) {
            showErrorAlert("Erreur de base de données", "Erreur lors de la modification de l'utilisateur: " + e.getMessage());
        } catch (IOException e) {
            showErrorAlert("Erreur de navigation", "Impossible de charger la page de détails: " + e.getMessage());
        } catch (Exception e) {
            showErrorAlert("Erreur inattendue", "Une erreur inattendue est survenue: " + e.getMessage());
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