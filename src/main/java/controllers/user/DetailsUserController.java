package controllers.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.User;
import services.user.UserService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Optional;

public class DetailsUserController {

    @FXML
    private ImageView profileImageView;

    @FXML
    private Label nomLabel;

    @FXML
    private Label prenomLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label numTelLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label genderLabel;

    @FXML
    private Label dateNaissanceLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button backButton;

    private User currentUser;
    private UserService userService;

    public void initialize() {
        userService = new UserService();

        // Set default profile image if needed
        setDefaultProfileImage();
    }

    public void setUserDetails(User user) {
        this.currentUser = user;

        // Set user details to the labels
        nomLabel.setText(user.getNom());
        prenomLabel.setText(user.getPrenom());
        usernameLabel.setText(user.getUsername());
        numTelLabel.setText(user.getNumTel());
        emailLabel.setText(user.getEmail());
        genderLabel.setText(user.getGender());

        // Format date to a readable format
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        dateNaissanceLabel.setText(sdf.format(user.getDatedenaissance()));

        roleLabel.setText(user.getRoles());

        // Load profile image if available
        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
            File file = new File(user.getProfilePicture());
            if (file.exists()) {
                try {
                    Image image = new Image(file.toURI().toString());
                    profileImageView.setImage(image);
                } catch (Exception e) {
                    setDefaultProfileImage();
                }
            } else {
                setDefaultProfileImage();
            }
        } else {
            setDefaultProfileImage();
        }
    }

    private void setDefaultProfileImage() {
        // Load default profile image
        try {
            // You can specify a path to a default profile image in your resources
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-profile.png"));
            profileImageView.setImage(defaultImage);
        } catch (Exception e) {
            // If default image can't be loaded, we'll just leave it empty
            System.err.println("Default profile image couldn't be loaded: " + e.getMessage());
        }
    }

    @FXML
    void handleUpdateUser(ActionEvent event) {
        try {
            // Load the update user form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/userinterfaces/ModifierUser.fxml"));
            Parent root = loader.load();

            // Pass the current user to the update controller
            ModifierUserController updateController = loader.getController();
            updateController.setUserToUpdate(currentUser);

            // Switch to the update scene
            profileImageView.getScene().setRoot(root);

        } catch (IOException e) {
            showErrorAlert("Erreur de chargement", "Impossible de charger le formulaire de modification: " + e.getMessage());
        }
    }

    @FXML
    void handleDeleteUser(ActionEvent event) {

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer cet utilisateur?");
        confirmDialog.setContentText("Utilisateur: " + currentUser.getNom() + " " + currentUser.getPrenom());

        Optional<ButtonType> result = confirmDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete the user
                userService.delete(userService.getById(currentUser.getId()));

                // Show success message
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Suppression réussie");
                successAlert.setHeaderText(null);
                successAlert.setContentText("L'utilisateur a été supprimé avec succès.");
                successAlert.showAndWait();

                // Navigate back to the user list
                handleBackToList(event);

            } catch (SQLException e) {
                showErrorAlert("Erreur de suppression", "Impossible de supprimer l'utilisateur: " + e.getMessage());
            }
        }
    }

    @FXML
    void handleBackToList(ActionEvent event) {
        try {
            // Load the user list view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/userinterfaces/AfficherUsers.fxml"));
            Parent root = loader.load();

            // Switch to the user list scene
            profileImageView.getScene().setRoot(root);

        } catch (IOException e) {
            showErrorAlert("Erreur de chargement", "Impossible de charger la liste des utilisateurs: " + e.getMessage());
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