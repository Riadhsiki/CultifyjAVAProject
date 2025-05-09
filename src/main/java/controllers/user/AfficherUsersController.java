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
    private UserService userService = new UserService();
    private SessionManager sessionManager = SessionManager.getInstance();
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
                                    "-fx-border-color: #2c3e50; " +
                                    "-fx-border-width: 1; " +
                                    "-fx-border-radius: 5; " +
                                    "-fx-background-color: #ffffff; " +
                                    "-fx-background-radius: 5;"
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
                            loadDefaultImage(profileImageView);
                        }
                    } else {
                        loadDefaultImage(profileImageView);
                    }

                    // User info labels
                    Label nameLabel = new Label(user.getPrenom() + " " + user.getNom());
                    nameLabel.setStyle("-fx-font-family: 'Verdana'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                    Label usernameLabel = new Label("Username: " + user.getUsername());
                    usernameLabel.setStyle("-fx-font-family: 'Verdana'; -fx-font-size: 12px; -fx-text-fill: #2c3e50;");
                    Label emailLabel = new Label("Email: " + user.getEmail());
                    emailLabel.setStyle("-fx-font-family: 'Verdana'; -fx-font-size: 12px; -fx-text-fill: #2c3e50;");
                    Label numTelLabel = new Label("Phone: " + user.getNumTel());
                    numTelLabel.setStyle("-fx-font-family: 'Verdana'; -fx-font-size: 12px; -fx-text-fill: #2c3e50;");
                    Label rolesLabel = new Label("Roles: " + user.getRoles());
                    rolesLabel.setStyle("-fx-font-family: 'Verdana'; -fx-font-size: 12px; -fx-text-fill: #2c3e50;");
                    Label genderLabel = new Label("Gender: " + user.getGender());
                    genderLabel.setStyle("-fx-font-family: 'Verdana'; -fx-font-size: 12px; -fx-text-fill: #2c3e50;");

                    String dateNaissance = user.getDatedenaissance() != null
                            ? new SimpleDateFormat("yyyy-MM-dd").format(user.getDatedenaissance())
                            : "Not specified";
                    Label dateNaissanceLabel = new Label("Birth Date: " + dateNaissance);
                    dateNaissanceLabel.setStyle("-fx-font-family: 'Verdana'; -fx-font-size: 12px; -fx-text-fill: #2c3e50;");

                    // Buttons for actions
                    Button selectButton = new Button("Select");
                    styleButton(selectButton);
                    selectButton.setOnAction(e -> navigateToDetailsSafe(user));

                    Button modifyButton = new Button("Edit");
                    styleButton(modifyButton);
                    modifyButton.setOnAction(e -> navigateToDetailsSafe(user));

                    Button deleteButton = new Button("Delete");
                    deleteButton.setStyle(
                            "-fx-background-color: #e74c3c; " +
                                    "-fx-text-fill: #ecf0f1; " +
                                    "-fx-font-family: 'Verdana'; " +
                                    "-fx-font-size: 12px; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-background-radius: 5; " +
                                    "-fx-padding: 5 10;"
                    );
                    deleteButton.setOnAction(e -> confirmAndDelete(user));

                    // Admin toggle button
                    boolean isAdmin = user.getRoles().contains("Admin");
                    Button adminButton = new Button(isAdmin ? "Revoke Admin" : "Make Admin");
                    styleButton(adminButton);
                    adminButton.setStyle(
                            "-fx-background-color: " + (isAdmin ? "#e67e22" : "#1abc9c") + "; " +
                                    "-fx-text-fill: #ecf0f1; " +
                                    "-fx-font-family: 'Verdana'; " +
                                    "-fx-font-size: 12px; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-background-radius: 5; " +
                                    "-fx-padding: 5 10;"
                    );
                    adminButton.setTooltip(new Tooltip(isAdmin ? "Remove admin privileges" : "Grant admin privileges"));
                    adminButton.setOnAction(e -> toggleAdminRole(user, adminButton, rolesLabel));
                    // Disable for non-admins or current user
                    String currentUsername = sessionManager.getCurrentUsername();
                    boolean isCurrentUser = currentUsername != null && currentUsername.equals(user.getUsername());
                    adminButton.setDisable(!isCurrentUserAdmin() || isCurrentUser);

                    HBox buttonBox = new HBox(10, selectButton, modifyButton, deleteButton, adminButton);
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

        // Restrict actions for non-admins
        if (!isCurrentUserAdmin()) {
            addUserButton.setDisable(true);
            addUserButton.setTooltip(new Tooltip("Admin access required"));
            viewDetailsButton.setDisable(true);
            viewDetailsButton.setTooltip(new Tooltip("Admin access required"));
        }
    }

    private void styleButton(Button button) {
        button.setStyle(
                "-fx-background-color: #1abc9c; " +
                        "-fx-text-fill: #ecf0f1; " +
                        "-fx-font-family: 'Verdana'; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 5; " +
                        "-fx-padding: 5 10;"
        );
    }

    private void loadDefaultImage(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default.jpg"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Failed to load default image: " + e.getMessage());
        }
    }

    private boolean isCurrentUserAdmin() {
        try {
            String username = sessionManager.getCurrentUsername();
            if (username == null) return false;
            String roles = userService.getRoleByUsername(username);
            return roles != null && roles.contains("Admin");
        } catch (SQLException e) {
            System.err.println("Error checking admin status: " + e.getMessage());
            return false;
        }
    }

    private void toggleAdminRole(User user, Button adminButton, Label rolesLabel) {
        try {
            boolean isAdmin = user.getRoles().contains("Admin");
            userService.updateUserRoles(user, !isAdmin);
            // Update user object
            user.setRoles(userService.getById(user.getId()).getRoles());
            // Update UI
            adminButton.setText(isAdmin ? "Make Admin" : "Revoke Admin");
            adminButton.setStyle(
                    "-fx-background-color: " + (isAdmin ? "#1abc9c" : "#e67e22") + "; " +
                            "-fx-text-fill: #ecf0f1; " +
                            "-fx-font-family: 'Verdana'; " +
                            "-fx-font-size: 12px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 5; " +
                            "-fx-padding: 5 10;"
            );
            adminButton.setTooltip(new Tooltip(isAdmin ? "Grant admin privileges" : "Remove admin privileges"));
            rolesLabel.setText("Roles: " + user.getRoles());
            showInfoAlert("Success", "User " + user.getUsername() + " " + (isAdmin ? "admin privileges revoked." : "granted admin privileges."));
        } catch (SQLException e) {
            showErrorAlert("Role Update Error", "Failed to update admin role: " + e.getMessage());
        }
    }

    private void loadUsers() {
        try {
            userList.setAll(userService.getAll());
        } catch (SQLException e) {
            showErrorAlert("Load Error", "Failed to load users: " + e.getMessage());
        }
    }

    @FXML
    private void openAddUserPage(ActionEvent event) {
        if (!isCurrentUserAdmin()) {
            showErrorAlert("Permission Denied", "Only admins can add users.");
            return;
        }
        navigateTo(ADD_USER_FXML, "CultureSketch - Add User", event);
    }

    @FXML
    private void navigateToProfile(ActionEvent event) {
        if (!sessionManager.isLoggedIn()) {
            showErrorAlert("Not Logged In", "You must be logged in to view your profile. Please login first.");
            navigateTo(LOGIN_FXML, "CultureSketch - Login", event);
            return;
        }
        navigateTo(PROFILE_FXML, "CultureSketch - Profile", event);
    }

    @FXML
    private void viewSelectedUserDetails(ActionEvent event) {
        if (!isCurrentUserAdmin()) {
            showErrorAlert("Permission Denied", "Only admins can view user details.");
            return;
        }
        User selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            navigateToDetailsSafe(selectedUser);
        } else {
            showErrorAlert("No Selection", "Please select a user to view details.");
        }
    }

    private void navigateToDetailsSafe(User user) {
        try {
            navigateToDetails(user);
        } catch (IOException ex) {
            showErrorAlert("Navigation Error", "Failed to load details view: " + ex.getMessage());
        }
    }

    private void confirmAndDelete(User user) {
        if (!isCurrentUserAdmin()) {
            showErrorAlert("Permission Denied", "Only admins can delete users.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete this user?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText(null);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                userService.delete(user);
                userList.remove(user);
                userListView.getSelectionModel().clearSelection();
                showInfoAlert("Success", "User deleted successfully!");
            } catch (SQLException ex) {
                showErrorAlert("Deletion Error", "Failed to delete user: " + ex.getMessage());
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