package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import Models.User;
import Services.UserServices;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private TextField passwordTextField;

    // Hold the currently displayed user
    private User currentUser;

    /**
     * Sets the details of the user into the UI fields.
     *
     * @param user The User to display
     */
    public void setUserDetails(User user) {
        this.currentUser = user;
        nomTextField.setText(user.getNom());
        prenomTextField.setText(user.getPrenom());
        usernameTextField.setText(user.getUsername());
        numTextField.setText(user.getNumTel());
        emailTextField.setText(user.getEmail());
        genderTextField.setText(user.getGender());

        // Format the date to a string (YYYY-MM-DD)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        datedenaissanceTextField.setText(sdf.format(user.getDatedenaissance()));

        rolesTextField.setText(user.getRoles());
        profilepicTextField.setText(user.getProfilePicture());
        passwordTextField.setText(user.getPassword());
    }

    /**
     * Called when the Add button is pressed.
     * It creates a new user from the text fields and adds it via the service layer.
     */
    @FXML
    private void ajouterUserAction(ActionEvent event) {
        try {
            // Create a new User object
            User newUser = new User();
            newUser.setNom(nomTextField.getText());
            newUser.setPrenom(prenomTextField.getText());
            newUser.setUsername(usernameTextField.getText());
            newUser.setNumTel(numTextField.getText());
            newUser.setEmail(emailTextField.getText());
            newUser.setGender(genderTextField.getText());

            // Parse the date from the text field
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date parsedDate = sdf.parse(datedenaissanceTextField.getText());
            newUser.setDatedenaissance(new java.sql.Date(parsedDate.getTime()));

            newUser.setRoles(rolesTextField.getText());
            newUser.setProfilePicture(profilepicTextField.getText());
            newUser.setPassword(passwordTextField.getText());

            // Add user via the service layer
            UserServices userServices = new UserServices();
            userServices.add(newUser); // Assumes UserServices has an add method

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ajout réussi");
            alert.setHeaderText("L'utilisateur a été ajouté avec succès !");
            alert.showAndWait();

            // Optionally: Clear the form or redirect to another scene

        } catch (ParseException pe) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de formatage de date");
            alert.setHeaderText("Veuillez entrer une date au format YYYY-MM-DD");
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d'ajout");
            alert.setHeaderText("Impossible d'ajouter l'utilisateur : " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Called when the Update button is pressed.
     * It reads the updated values from the text fields, updates the user,
     * and calls the service layer to persist the changes.
     */
    @FXML
    private void updateUserAction(ActionEvent event) {
        try {
            // Update the user fields with values from the text fields.
            currentUser.setNom(nomTextField.getText());
            currentUser.setPrenom(prenomTextField.getText());
            currentUser.setUsername(usernameTextField.getText());
            currentUser.setNumTel(numTextField.getText());
            currentUser.setEmail(emailTextField.getText());
            currentUser.setGender(genderTextField.getText());

            // Parse the date from the text field.
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date parsedDate = sdf.parse(datedenaissanceTextField.getText());
            currentUser.setDatedenaissance(new java.sql.Date(parsedDate.getTime()));

            currentUser.setRoles(rolesTextField.getText());
            currentUser.setProfilePicture(profilepicTextField.getText());
            currentUser.setPassword(passwordTextField.getText());

            // Update user via the service layer.
            UserServices userServices = new UserServices();
            userServices.update(currentUser);  // Make sure the update method exists in UserServices

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Mise à jour réussie");
            alert.setHeaderText("L'utilisateur a été mis à jour avec succès !");
            alert.showAndWait();

        } catch (ParseException pe) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de formatage de date");
            alert.setHeaderText("Veuillez entrer une date au format YYYY-MM-DD");
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de mise à jour");
            alert.setHeaderText("Impossible de mettre à jour l'utilisateur : " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Called when the Delete button is pressed.
     * It deletes the current user using the service layer.
     */
    @FXML
    private void deleteUserAction(ActionEvent event) {
        try {
            UserServices userServices = new UserServices();
            // Pass the entire User object instead of just the ID
            userServices.delete(currentUser);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Suppression réussie");
            alert.setHeaderText("L'utilisateur a été supprimé avec succès !");
            alert.showAndWait();

            // Optionally: Redirect to a list view or another scene after deletion.

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de suppression");
            alert.setHeaderText("Impossible de supprimer l'utilisateur : " + e.getMessage());
            alert.showAndWait();
        }
    }
}