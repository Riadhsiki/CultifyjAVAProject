package Controllers.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import Models.User;
import Services.User.UserServices;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class DetailsUserController {

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
    private PasswordField passwordField;

    private User currentUser;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    );

    public void setUserDetails(User user) {
        if (user == null) {
            System.err.println("Error: Attempted to set details for a null user.");
            clearFields();
            return;
        }
        this.currentUser = user;

        nomTextField.setText(user.getNom());
        prenomTextField.setText(user.getPrenom());
        usernameTextField.setText(user.getUsername());
        numTextField.setText(user.getNumTel());
        emailTextField.setText(user.getEmail());
        genderTextField.setText(user.getGender());

        if (user.getDatedenaissance() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            datedenaissanceTextField.setText(sdf.format(user.getDatedenaissance()));
        } else {
            datedenaissanceTextField.setText("");
        }

        rolesTextField.setText(user.getRoles());
        profilepicTextField.setText(user.getProfilePicture());
        passwordField.setText(user.getPassword());
    }

    private void clearFields() {
        nomTextField.clear();
        prenomTextField.clear();
        usernameTextField.clear();
        numTextField.clear();
        emailTextField.clear();
        genderTextField.clear();
        datedenaissanceTextField.clear();
        rolesTextField.clear();
        profilepicTextField.clear();
        passwordField.clear();
    }

    @FXML
    private void updateUserAction(ActionEvent event) {
        if (currentUser == null) {
            showErrorAlert("Erreur", "Aucun utilisateur n'est chargé pour la mise à jour.");
            return;
        }

        String nom = nomTextField.getText().trim();
        String prenom = prenomTextField.getText().trim();
        String username = usernameTextField.getText().trim();
        String numTel = numTextField.getText().trim();
        String email = emailTextField.getText().trim();
        String gender = genderTextField.getText().trim();
        String dateNaissanceStr = datedenaissanceTextField.getText().trim();
        String roles = rolesTextField.getText().trim();
        String profilePic = profilepicTextField.getText().trim();
        String password = passwordField.getText();

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
            showErrorAlert("Erreur de saisie", "Le champ 'Rôles' ne peut pas être vide.");
            return;
        }
        if (password.isEmpty()) {
            showErrorAlert("Erreur de saisie", "Le champ 'Mot de Passe' ne peut pas être vide.");
            return;
        }

        try {
            currentUser.setNom(nom);
            currentUser.setPrenom(prenom);
            currentUser.setUsername(username);
            currentUser.setNumTel(numTel);
            currentUser.setEmail(email);
            currentUser.setGender(gender);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Date parsedDate = sdf.parse(dateNaissanceStr);
            currentUser.setDatedenaissance(new java.sql.Date(parsedDate.getTime()));

            currentUser.setRoles(roles);
            currentUser.setProfilePicture(profilePic);
            currentUser.setPassword(password);

            UserServices userServices = new UserServices();
            userServices.update(currentUser);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Mise à jour réussie");
            alert.setHeaderText(null);
            alert.setContentText("L'utilisateur a été mis à jour avec succès !");
            alert.showAndWait();

            navigateToUserList();

        } catch (ParseException pe) {
            showErrorAlert("Format de date invalide", "Veuillez entrer une date valide au format yyyy-MM-dd.");
        } catch (SQLException se) {
            showErrorAlert("Erreur Base de Données", "Impossible de mettre à jour l'utilisateur: " + se.getMessage());
        } catch (Exception e) {
            showErrorAlert("Erreur Inattendue", "Une erreur inattendue est survenue: " + e.getMessage());
        }
    }

    @FXML
    private void deleteUserAction(ActionEvent event) {
        if (currentUser == null) {
            showErrorAlert("Erreur", "Aucun utilisateur n'est chargé pour la suppression.");
            return;
        }
        try {
            UserServices userServices = new UserServices();
            userServices.delete(currentUser);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Suppression réussie");
            alert.setHeaderText(null);
            alert.setContentText("L'utilisateur a été supprimé avec succès !");
            alert.showAndWait();

            navigateToUserList();

        } catch (SQLException se) {
            showErrorAlert("Erreur Base de Données", "Impossible de supprimer l'utilisateur: " + se.getMessage());
        } catch (Exception e) {
            showErrorAlert("Erreur Inattendue", "Une erreur inattendue est survenue lors de la suppression: " + e.getMessage());
        }
    }

    private void navigateToUserList() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserInteface/AfficherUsers.fxml"));
        if (loader.getLocation() == null) {
            throw new IOException("Cannot find /AfficherUsers.fxml. Check the file path.");
        }
        Parent root = loader.load();
        nomTextField.getScene().setRoot(root);
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}