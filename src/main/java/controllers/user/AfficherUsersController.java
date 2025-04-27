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
import javafx.collections.transformation.FilteredList;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class AfficherUsersController {

    @FXML private ListView<User> userListView;
    @FXML private TextField searchField;
    @FXML private Button addUserButton;
    @FXML private Button profileButton;
    @FXML private Button viewDetailsButton;

    private ObservableList<User> userList;
    private FilteredList<User> filteredUserList;
    private static final String ADD_USER_FXML = "/userinterfaces/AjouterUser.fxml";
    private static final String PROFILE_FXML = "/Auth/Profile.fxml";
    private static final String LOGIN_FXML = "/Auth/Login.fxml";
    private static final String DETAIL_USER_FXML = "/userinterfaces/DetailUser.fxml";

    @FXML
    public void initialize() {
        // Initialize the user list
        userList = FXCollections.observableArrayList();
        loadUsers();

        // Set up filtered list for search functionality
        filteredUserList = new FilteredList<>(userList, p -> true);
        userListView.setItems(filteredUserList);

        // Add listener to search field to filter users by username
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUserList.setPredicate(user -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true; // Show all users if search is empty
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return user.getUsername().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // Add tooltips to buttons
        addUserButton.setTooltip(new Tooltip("Add a new user to the system"));
        profileButton.setTooltip(new Tooltip("View your profile"));
        viewDetailsButton.setTooltip(new Tooltip("View details of the selected user"));

        // Enable viewDetailsButton only when a user is selected
        userListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            viewDetailsButton.setDisable(newSelection == null);
        });

        // Set the custom cell factory for the ListView
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
                    String profilePicPath = user.getProfilePicture();
                    if (profilePicPath != null && !profilePicPath.isEmpty()) {
                        File imgFile = new File(profilePicPath);
                        if (imgFile.exists()) {
                            profileImageView.setImage(new Image(imgFile.toURI().toString()));
                        } else {
                            // Load a default image
                            try {
                                profileImageView.setImage(new Image("file:src/main/resources/images/default.jpg"));
                            } catch (Exception e) {
                                System.err.println("Default image not found: " + e.getMessage());
                            }
                        }
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
                    modifyButton.setOnAction(e -> navigateToDetailsSafe(user)); // Same navigation as "Voir Détails"

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
    private void openAddUserPage(ActionEvent event) {
        navigateTo(ADD_USER_FXML, "CultureSketch - Add User", event);
    }

    @FXML
    private void navigateToProfile(ActionEvent event) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            showErrorAlert("Non connecté", "Vous devez être connecté pour accéder à votre profil. Veuillez vous connecter d'abord.");
            navigateTo(LOGIN_FXML, "CultureSketch - Login", event);
            return;
        }

        navigateTo(PROFILE_FXML, "CultureSketch - Profile", event);
    }

    @FXML
    private void viewSelectedUserDetails(ActionEvent event) {
        User selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            navigateToDetailsSafe(selectedUser);
        } else {
            showErrorAlert("Aucune sélection", "Veuillez sélectionner un utilisateur pour voir les détails.");
        }
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource(DETAIL_USER_FXML));
        Parent root = loader.load();
        DetailsUserController detailController = loader.getController();
        detailController.setUserDetails(user); // Pass the user to the details controller
        Stage stage = (Stage) userListView.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("CultureSketch - User Details");
        stage.centerOnScreen();
        stage.show();
    }

    private void navigateTo(String fxmlPath, String title, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                showErrorAlert("Navigation Error", "Failed to load view: " + fxmlPath);
                return;
            }
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to load view: " + e.getMessage());
        }
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