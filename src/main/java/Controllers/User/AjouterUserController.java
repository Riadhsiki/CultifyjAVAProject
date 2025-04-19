package Controllers.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import Models.User;
import Services.User.UserServices;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class AjouterUserController {

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
    private TextField genderTextField;

    @FXML
    private TextField datedenaissanceTextField;

    @FXML
    private TextField rolesTextField;

    @FXML
    private TextField profilepicTextField;

    @FXML
    private TextField passwordTextField;

    // Simple regex for basic email validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    );

    @FXML
    void ajouterUserAction(ActionEvent event) {
        // --- Input Validation Start ---

        String nom = nomTextField.getText().trim();
        String prenom = prenomTextField.getText().trim();
        String username = usernameTextField.getText().trim();
        String numTel = numTextField.getText().trim();
        String email = emailTextField.getText().trim();
        String gender = genderTextField.getText().trim();
        String dateNaissanceStr = datedenaissanceTextField.getText().trim();
        String roles = rolesTextField.getText().trim();
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
        if (!numTel.matches("\\d+")) {
            showErrorAlert("Erreur de saisie", "Le champ 'Numéro téléphone' doit contenir uniquement des chiffres.");
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
        if (gender.isEmpty()) {
            showErrorAlert("Erreur de saisie", "Le champ 'Gender' ne peut pas être vide.");
            return;
        }
        if (dateNaissanceStr.isEmpty()) {
            showErrorAlert("Erreur de saisie", "Le champ 'Date de naissance' ne peut pas être vide.");
            return;
        }
        if (roles.isEmpty()) {
            showErrorAlert("Erreur de saisie", "Le champ 'Roles' ne peut pas être vide.");
            return;
        }
        if (password.isEmpty()) {
            showErrorAlert("Erreur de saisie", "Le champ 'Password' ne peut pas être vide.");
            return;
        }

        // --- Input Validation End ---

        try {
            // Date parsing
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setLenient(false);
            java.util.Date parsedDate = format.parse(dateNaissanceStr);
            Date dateNaissance = new Date(parsedDate.getTime());

            // Create User object (montantAPayer set to null)
            User user = new User(nom, prenom, username, numTel, email, gender,
                    dateNaissance, profilePic, password, roles, null);

            // Add user via service
            UserServices userService = new UserServices();
            userService.add(user);

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Utilisateur ajouté avec succès!");
            alert.showAndWait();

            // Navigate to the details view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserInteface/DetailUser.fxml"));
            Parent root = loader.load();
            DetailsUserController detailController = loader.getController();
            detailController.setUserDetails(user);

            // Change the scene to the detail view
            nomTextField.getScene().setRoot(root);

        } catch (ParseException e) {
            showErrorAlert("Format de date invalide", "Veuillez entrer une date valide au format YYYY-MM-DD.");
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