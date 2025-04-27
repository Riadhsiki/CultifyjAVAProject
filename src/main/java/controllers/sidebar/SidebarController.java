package controllers.sidebar;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.auth.AuthenticationService;
import utils.SessionManager;
import java.io.IOException;
import java.sql.SQLException;

public class SidebarController {

    @FXML private ImageView logoImageView;
    @FXML private Button btnUserManagement;
    @FXML private VBox userManagementSubmenu;
    @FXML private Button btnProfile;
    @FXML private Button btnBrowseArt;
    @FXML private Button btnCreateArt;
    @FXML private Button btnMyPortfolio;
    @FXML private Button btnLogout;

    private AuthenticationService authService;
    private static final String LOGIN_FXML = "/auth/Login.fxml";

    @FXML
    public void initialize() {
        authService = AuthenticationService.getInstance();
        userManagementSubmenu.setVisible(false);
        userManagementSubmenu.setManaged(false);
        userManagementSubmenu.setPrefHeight(0);
        SessionManager.getInstance().dumpPreferences();
        System.out.println("SidebarController initialized");
    }

    @FXML
    private void navigateToHome(ActionEvent event) {
        navigateTo("/home/Home.fxml", "Home");
    }

    @FXML
    private void toggleUserManagementSubmenu(ActionEvent event) {
        String sessionToken = SessionManager.getInstance().getSessionToken();
        System.out.println("Toggling User Management - Session Token: " + (sessionToken != null ? sessionToken : "null"));
        if (!SessionManager.getInstance().isLoggedIn()) {
            System.out.println("User not authenticated, redirecting to login.");
            showLoginAlertAndRedirect();
            return;
        }

        // Temporarily bypass role check for testing
        // if (!authService.hasRole(sessionToken, "Organisateur")) {
        //     System.out.println("User does not have 'Organisateur' role.");
        //     showAlert("Access Denied", "You do not have permission to access User Management.");
        //     return;
        // }

        boolean isVisible = userManagementSubmenu.isVisible();
        userManagementSubmenu.setVisible(!isVisible);
        userManagementSubmenu.setManaged(!isVisible);
        userManagementSubmenu.setPrefHeight(isVisible ? 0 : 80);
        System.out.println("User Management submenu visibility toggled to: " + !isVisible);
    }

    @FXML
    private void navigateToAddUser(ActionEvent event) {
        checkAuthAndNavigate("/userinterfaces/AjouterUser.fxml", "Add User");
    }

    @FXML
    private void navigateToUserList(ActionEvent event) {
        checkAuthAndNavigate("/userinterfaces/AfficherUsers.fxml", "User List");
    }

    @FXML
    private void navigateToProfile(ActionEvent event) {
        checkAuthAndNavigate("/auth/Profile.fxml", "My Profile");
    }

    @FXML
    private void navigateToBrowseArt(ActionEvent event) {
        checkAuthAndNavigate("/sketch/AfficherSketch.fxml", "Browse Art");
    }

    @FXML
    private void navigateToCreateArt(ActionEvent event) {
        checkAuthAndNavigate("/sketch/CultureSketch.fxml", "Create Art");
    }

    @FXML
    private void navigateToMyPortfolio(ActionEvent event) {
        checkAuthAndNavigate("/sketch/ArtView.fxml", "My Portfolio");
    }

    @FXML
    private void logOut(ActionEvent event) {
        String sessionToken = SessionManager.getInstance().getSessionToken();
        System.out.println("Logging out - Session Token: " + (sessionToken != null ? sessionToken : "null"));
        if (sessionToken != null) {
            authService.logout(sessionToken);
            SessionManager.getInstance().clearSession();
            System.out.println("Session cleared and logged out.");
        }
        navigateTo(LOGIN_FXML, "Login");
    }

    private void checkAuthAndNavigate(String fxmlPath, String title) {
        String sessionToken = SessionManager.getInstance().getSessionToken();
        String username = SessionManager.getInstance().getCurrentUsername();
        boolean isLoggedIn = SessionManager.getInstance().isLoggedIn();
        System.out.println("Checking authentication for navigation to " + title +
                " - Session Token: " + (sessionToken != null ? sessionToken : "null") +
                ", Username: " + (username != null ? username : "null") +
                ", isLoggedIn: " + isLoggedIn);
        if (sessionToken == null || username == null || !isLoggedIn) {
            System.out.println("Authentication failed, redirecting to login.");
            showLoginAlertAndRedirect();
            return;
        }
        try {
            // Test authentication service
            System.out.println("Validating user with authService");
            if (authService.getCurrentUser(sessionToken) == null) {
                System.out.println("authService.getCurrentUser returned null");
                showLoginAlertAndRedirect();
                return;
            }
        } catch (SQLException e) {
            System.out.println("SQLException in authService: " + e.getMessage());
            showAlert("Error", "Database error during authentication: " + e.getMessage());
            return;
        }
        System.out.println("Authentication successful, navigating to " + title);
        navigateTo(fxmlPath, title);
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                System.out.println("FXML resource not found: " + fxmlPath);
                showAlert("Navigation Error", "Failed to load view: " + fxmlPath);
                return;
            }
            Parent root = loader.load();
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
            System.out.println("Navigated to " + title);
            SessionManager.getInstance().dumpPreferences();
        } catch (IOException e) {
            System.out.println("IOException during navigation to " + fxmlPath + ": " + e.getMessage());
            showAlert("Navigation Error", "Failed to load view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showLoginAlertAndRedirect() {
        showAlert("Not Logged In", "You must be logged in to access this page.");
        navigateTo(LOGIN_FXML, "Login");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getClientIp() {
        return "127.0.0.1";
    }
}