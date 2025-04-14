package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import Models.User;
import Services.UserServices;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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

    @FXML
    void ajouterUserAction(ActionEvent event) {
        try {
            // Retrieve input values
            String nom = nomTextField.getText();
            String prenom = prenomTextField.getText();
            String username = usernameTextField.getText();
            String numTel = numTextField.getText();
            String email = emailTextField.getText();
            String gender = genderTextField.getText();
            String roles = rolesTextField.getText();
            String profilePic = profilepicTextField.getText();
            String password = passwordTextField.getText();

            // Parse date from input text (format: YYYY-MM-DD)
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date parsedDate = format.parse(datedenaissanceTextField.getText());
            Date dateNaissance = new Date(parsedDate.getTime());

            // Create a new User instance
            User user = new User(nom, prenom, username, numTel, email, gender,
                    dateNaissance, roles, profilePic, password);

            // Add user using the service layer
            UserServices userService = new UserServices();
            userService.add(user);

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Utilisateur ajouté avec succès!");
            alert.showAndWait();

            // Load the detail view and redirect
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailUser.fxml"));
            Parent root = loader.load();
            DetailsUserController detailController = loader.getController();
            detailController.setUserDetails(user);

            // Change the scene to the detail view
            nomTextField.getScene().setRoot(root);

        } catch (SQLException e) {
            showErrorAlert("Erreur de base de données", e.getMessage());
        } catch (ParseException e) {
            showErrorAlert("Format de date invalide", "Veuillez entrer une date au format YYYY-MM-DD");
        } catch (IOException e) {
            showErrorAlert("Erreur de chargement", "Erreur lors du chargement de la vue: " + e.getMessage());
        } catch (Exception e) {
            showErrorAlert("Erreur", e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }
}
