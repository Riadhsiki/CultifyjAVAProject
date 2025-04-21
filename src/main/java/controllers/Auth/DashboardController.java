package controllers.Auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import models.User;

import java.io.IOException;
import java.sql.SQLException;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    private final AuthenticationService authService = new AuthenticationService();

    /**
     * Initialize the controller
     * This method is automatically called after the FXML file has been loaded
     */
    @FXML
    private void initialize() {
        // Set welcome message with username
        String username = SessionManager.getInstance().getCurrentUsername();
        if (username != null && !username.isEmpty()) {
            welcomeLabel.setText("Welcome, " + username + "!");
        }

        // You could also load any user-specific data here
        try {
            // Get the current user using the session token
            String sessionToken = SessionManager.getInstance().getSessionToken();
            User currentUser = authService.getCurrentUser(sessionToken);

            // If user is not authenticated, redirect to login
            if (currentUser == null) {
                // We can't redirect here directly as JavaFX doesn't allow scene changes during initialization
                // You might want to show a message or set up a redirect for later
                System.out.println("User session expired. Please log in again.");
            } else {
                // Load user-specific data
                // For example, you could set user-specific content here
                // dashboardContentLabel.setText("Welcome back, " + currentUser.getPrenom() + "!");
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    /**
     * Handle logout button click
     * @param event The ActionEvent
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        try {

            String sessionToken = SessionManager.getInstance().getSessionToken();
            authService.logout(sessionToken);

            // Clear the session
            SessionManager.getInstance().clearSession();

            // Navigate back to login screen
            navigateToLogin(event);
        } catch (IOException e) {
            System.err.println("Error navigating to login: " + e.getMessage());
        }
    }

    /**
     * Handle profile button click
     * @param event The ActionEvent
     */
    @FXML
    private void handleProfile(ActionEvent event) {
        try {
            // Navigate to profile page
            navigateToProfile(event);
        } catch (IOException e) {
            System.err.println("Error navigating to profile: " + e.getMessage());
        }
    }

    /**
     * Navigate to login page
     * @param event The ActionEvent
     */
    private void navigateToLogin(ActionEvent event) throws IOException {
        Parent loginParent = FXMLLoader.load(getClass().getResource("/login.fxml"));
        Scene loginScene = new Scene(loginParent);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(loginScene);
        window.setTitle("Login");
        window.show();
    }

    /**
     * Navigate to profile page
     * @param event The ActionEvent
     */
    private void navigateToProfile(ActionEvent event) throws IOException {
        // Implement profile navigation when you have a profile page
        // This is just a placeholder for future functionality
        // If you don't have a profile page yet, you can return or show a message
        System.out.println("Profile page not implemented yet.");

        // Uncomment below when you have a profile page
        /*
        Parent profileParent = FXMLLoader.load(getClass().getResource("/fxml/profile.fxml"));
        Scene profileScene = new Scene(profileParent);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(profileScene);
        window.setTitle("User Profile");
        window.show();
        */
    }
}