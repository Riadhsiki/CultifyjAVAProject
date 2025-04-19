package Controllers.User;

import Models.User;
import Services.User.UserServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
                    setText(null);
                } else {
                    // Create a card-like layout for each user
                    VBox card = new VBox(5);
                    card.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-border-width: 1; -fx-background-color: #f9f9f9;");

                    Label nameLabel = new Label("Nom: " + user.getNom() + " " + user.getPrenom());
                    nameLabel.setStyle("-fx-font-weight: bold;");

                    Label usernameLabel = new Label("Username: " + user.getUsername());
                    Label emailLabel = new Label("Email: " + user.getEmail());
                    Label numTelLabel = new Label("Numéro Téléphone: " + user.getNumTel());
                    Label rolesLabel = new Label("Rôles: " + user.getRoles());
                    Label genderLabel = new Label("Gender: " + user.getGender());
                    Label profilePicLabel = new Label("Photo de Profil: " + user.getProfilePicture());

                    // Format the date of birth
                    String dateNaissance = user.getDatedenaissance() != null
                            ? new SimpleDateFormat("yyyy-MM-dd").format(user.getDatedenaissance())
                            : "Non spécifiée";
                    Label dateNaissanceLabel = new Label("Date de Naissance: " + dateNaissance);

                    // Create buttons for Select, Modify, and Delete
                    Button selectButton = new Button("Sélectionner");
                    selectButton.setOnAction(e -> {
                        try {
                            navigateToDetails(user);
                        } catch (IOException ex) {
                            showErrorAlert("Erreur de Navigation", "Erreur lors du chargement de la vue Détails: " + ex.getMessage());
                        }
                    });

                    Button modifyButton = new Button("Modifier");
                    modifyButton.setOnAction(e -> {
                        try {
                            navigateToDetails(user); // Same as Select, since editing is in DetailUser.fxml
                        } catch (IOException ex) {
                            showErrorAlert("Erreur de Navigation", "Erreur lors du chargement de la vue Détails: " + ex.getMessage());
                        }
                    });

                    Button deleteButton = new Button("Supprimer");
                    deleteButton.setOnAction(e -> {
                        // Add confirmation dialog
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cet utilisateur ?", ButtonType.YES, ButtonType.NO);
                        confirm.setTitle("Confirmer la Suppression");
                        confirm.setHeaderText(null);
                        confirm.showAndWait();
                        if (confirm.getResult() == ButtonType.YES) {
                            try {
                                UserServices userServices = new UserServices();
                                userServices.delete(user);
                                userList.remove(user); // Refresh the list by removing the user
                                userListView.getSelectionModel().clearSelection(); // Clear selection
                                showInfoAlert("Succès", "Utilisateur supprimé avec succès !");
                            } catch (SQLException ex) {
                                showErrorAlert("Erreur de Suppression", "Erreur lors de la suppression de l'utilisateur: " + ex.getMessage());
                            }
                        }
                    });

                    // Add buttons to an HBox
                    HBox buttonBox = new HBox(10, selectButton, modifyButton, deleteButton);
                    buttonBox.setStyle("-fx-padding: 10 0 0 0;");

                    // Add all elements to the card
                    card.getChildren().addAll(
                            nameLabel,
                            usernameLabel,
                            emailLabel,
                            numTelLabel,
                            rolesLabel,
                            genderLabel,
                            profilePicLabel,
                            dateNaissanceLabel,
                            buttonBox
                    );
                    setGraphic(card);
                }
            }
        });
    }

    private void loadUsers() {
        userList = FXCollections.observableArrayList();
        UserServices userServices = new UserServices();
        try {
            userList.addAll(userServices.getAll());
        } catch (SQLException e) {
            showErrorAlert("Erreur de Chargement", "Erreur lors du chargement des utilisateurs: " + e.getMessage());
        }
    }

    @FXML
    private void openAddUserPage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserInteface/AjouterUser.fxml"));
        if (loader.getLocation() == null) {
            throw new IOException("Cannot find /AjouterUser.fxml. Check the file path.");
        }
        Parent root = loader.load();
        userListView.getScene().setRoot(root);
    }

    private void navigateToDetails(User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserInteface/DetailUser.fxml"));
        if (loader.getLocation() == null) {
            throw new IOException("Cannot find /DetailUser.fxml. Check the file path.");
        }
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
}