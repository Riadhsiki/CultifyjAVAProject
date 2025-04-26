package controllers.user;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.User;
import services.user.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class AfficherUsersController {

    @FXML
    private ListView<User> userListView;

    private ObservableList<User> userList;

    @FXML
    public void initialize() {
        // Initialize the user list
        userList = FXCollections.observableArrayList();
        loadUsers();

        // Set the custom cell factory for the ListView
        userListView.setItems(userList);
        userListView.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    // Create a card-like layout for each user
                    VBox card = new VBox(5);
                    card.setStyle(
                            "-fx-padding: 10; " +
                                    "-fx-border-color: lightgray; " +
                                    "-fx-border-width: 1; " +
                                    "-fx-background-color: #f9f9f9;"
                    );
                    card.setAlignment(Pos.CENTER_LEFT);

                    // Profile image
                    ImageView profileImageView = new ImageView();
                    profileImageView.setFitWidth(50);
                    profileImageView.setFitHeight(50);
                    profileImageView.setPreserveRatio(true);
                    File imgFile = new File(user.getProfilePicture());
                    if (imgFile.exists()) {
                        profileImageView.setImage(new Image(imgFile.toURI().toString()));
                    }

                    // User info labels
                    Label nameLabel = new Label("Nom: " + user.getNom() + " " + user.getPrenom());
                    nameLabel.setStyle("-fx-font-weight: bold;");
                    Label usernameLabel = new Label("Username: " + user.getUsername());
                    Label emailLabel = new Label("Email: " + user.getEmail());
                    Label numTelLabel = new Label("Numéro Téléphone: " + user.getNumTel());
                    Label rolesLabel = new Label("Rôles: " + user.getRoles());
                    Label genderLabel = new Label("Genre: " + user.getGender());

                    String dateNaissance = user.getDatedenaissance() != null
                            ? new SimpleDateFormat("yyyy-MM-dd").format(user.getDatedenaissance())
                            : "Non spécifiée";
                    Label dateNaissanceLabel = new Label("Date de Naissance: " + dateNaissance);

                    // Buttons for actions
                    Button selectButton = new Button("Sélectionner");
                    selectButton.setOnAction(e -> navigateToDetailsSafe(user));

                    Button modifyButton = new Button("Modifier");
                    modifyButton.setOnAction(e -> navigateToDetailsSafe(user));

                    Button deleteButton = new Button("Supprimer");
                    deleteButton.setOnAction(e -> confirmAndDelete(user));

                    HBox buttonBox = new HBox(10, selectButton, modifyButton, deleteButton);
                    buttonBox.setStyle("-fx-padding: 10 0 0 0;");

                    // Assemble card: image + info
                    HBox headerBox = new HBox(10, profileImageView, nameLabel);
                    headerBox.setAlignment(Pos.CENTER_LEFT);

                    card.getChildren().addAll(
                            headerBox,
                            usernameLabel,
                            emailLabel,
                            numTelLabel,
                            rolesLabel,
                            genderLabel,
                            dateNaissanceLabel,
                            buttonBox
                    );

                    setGraphic(card);
                }
            }
        });
    }

    private void loadUsers() {
        UserService userServices = new UserService();
        try {
            userList.setAll(userServices.getAll());
        } catch (SQLException e) {
            showErrorAlert("Erreur de Chargement", "Erreur lors du chargement des utilisateurs: " + e.getMessage());
        }
    }

    @FXML
    private void openAddUserPage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/userinterfaces/AjouterUser.fxml"));
        Parent root = loader.load();
        userListView.getScene().setRoot(root);
    }

    private void navigateToDetailsSafe(User user) {
        try {
            navigateToDetails(user);
        } catch (IOException ex) {
            showErrorAlert("Erreur de Navigation", "Erreur lors du chargement de la vue Détails: " + ex.getMessage());
        }
    }

    private void confirmAndDelete(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer cet utilisateur ?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirm.setTitle("Confirmer la Suppression");
        confirm.setHeaderText(null);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                UserService userServices = new UserService();
                userServices.delete(user);
                userList.remove(user);
                userListView.getSelectionModel().clearSelection();
                showInfoAlert("Succès", "Utilisateur supprimé avec succès !");
            } catch (SQLException ex) {
                showErrorAlert("Erreur de Suppression", "Erreur lors de la suppression de l'utilisateur: " + ex.getMessage());
            }
        }
    }

    private void navigateToDetails(User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/userinterfaces/DetailUser.fxml"));
        Parent root = loader.load();
        DetailsUserController detailController = loader.getController();
        detailController.setUserDetails(user);
        userListView.getScene().setRoot(root);
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void navigateToProfile(ActionEvent event) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            // Optional: redirect to login or show alert
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Non connecté");
            alert.setHeaderText("Vous devez être connecté pour accéder à votre profil.");
            alert.setContentText("Veuillez vous connecter d'abord.");
            alert.showAndWait();

            // Optionally redirect to login
            try {
                Parent loginParent = FXMLLoader.load(getClass().getResource("/Auth/Login.fxml"));
                Scene loginScene = new Scene(loginParent);
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.setScene(loginScene);
                currentStage.centerOnScreen();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return;
        }

        // If user is logged in, proceed to profile
        try {
            Parent profileParent = FXMLLoader.load(getClass().getResource("/Auth/Profile.fxml"));
            Scene profileScene = new Scene(profileParent);
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(profileScene);
            currentStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to load profile view");
            alert.setContentText("An error occurred: " + e.getMessage());
            alert.showAndWait();
        }
    }
}