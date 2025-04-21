package controllers.Auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Text errorMessage;
    @FXML private Text successMessage;

    private final AuthenticationService authService = new AuthenticationService();

    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        // Clear previous messages
        hideMessages();

        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        boolean rememberMe = rememberMeCheckbox.isSelected();

        // Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required");
            return;
        }

        try {
            // Attempt to login
            String sessionToken = authService.login(username, password);

            if (sessionToken != null) {
                // Save the session token (could use preferences or local storage)
                SessionManager.getInstance().setSessionToken(sessionToken, rememberMe);
                SessionManager.getInstance().setCurrentUsername(username);

                // Navigate to dashboard
                navigateToDashboard(event);
            } else {
                showError("Invalid username or password");
            }
        } catch (SQLException | IOException e) {
            showError("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Navigate to registration page
     */
    @FXML
    private void navigateToRegister(ActionEvent event) throws IOException {
        Parent registerParent = FXMLLoader.load(getClass().getResource("/register.fxml"));
        Scene registerScene = new Scene(registerParent);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(registerScene);
        window.show();
    }

    /**
     * Navigate to forgot password page
     */
    @FXML
    private void navigateToForgotPassword(ActionEvent event) throws IOException {
        // Implement forgot password navigation if needed
    }

    /**
     * Navigate to dashboard
     */
    private void navigateToDashboard(ActionEvent event) throws IOException {
        Parent dashboardParent = FXMLLoader.load(getClass().getResource("/dashboard.fxml"));
        Scene dashboardScene = new Scene(dashboardParent);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(dashboardScene);
        window.setTitle("Dashboard");
        window.show();
    }

    /**
     * Show an error message
     */
    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
        errorMessage.setManaged(true);
    }

    /**
     * Show a success message
     */
    private void showSuccess(String message) {
        successMessage.setText(message);
        successMessage.setVisible(true);
        successMessage.setManaged(true);
    }

    /**
     * Hide all messages
     */
    private void hideMessages() {
        errorMessage.setVisible(false);
        errorMessage.setManaged(false);
        successMessage.setVisible(false);
        successMessage.setManaged(false);
    }

    /**
     * Initialize the controller
     * This method is automatically called after the FXML file has been loaded
     */
    @FXML
    private void initialize() {
        // Check if there's a success message from registration
        String message = SessionManager.getInstance().getTemporaryMessage();
        if (message != null && !message.isEmpty()) {
            showSuccess(message);
            SessionManager.getInstance().clearTemporaryMessage();
        }

        // Check if there's a saved session and auto-login
        String sessionToken = SessionManager.getInstance().getSessionToken();
        if (sessionToken != null && !sessionToken.isEmpty()) {
            try {
                if (authService.isAuthenticated(sessionToken)) {
                    // Auto-login
                    // Note: In a real app, you might want to show a loading screen
                    // or ask for confirmation instead of auto-login
                }
            } catch (Exception e) {
                // If auto-login fails, just show the login screen
            }
        }
    }
}