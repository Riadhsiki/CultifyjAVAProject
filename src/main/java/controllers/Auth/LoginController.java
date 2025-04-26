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
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import services.auth.AuthenticationService;
import utils.DataSource;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Text errorMessage;
    @FXML private Text successMessage;

    private final AuthenticationService authService;
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    public LoginController() {
        // Initialize AuthenticationService with database connection
        this.authService = new AuthenticationService(DataSource.getInstance().getConnection());
    }

    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        hideMessages();

        String loginInput = usernameField.getText().trim();
        String password = passwordField.getText();
        boolean rememberMe = rememberMeCheckbox.isSelected();

        // Enhanced validation
        if (loginInput.isEmpty()) {
            showError("Username/Email is required");
            return;
        }

        if (password.isEmpty()) {
            showError("Password is required");
            return;
        }

        // Validate email format if input contains @
        if (loginInput.contains("@")) {
            if (!isValidEmail(loginInput)) {
                showError("Please enter a valid email address");
                return;
            }
        }

        // Validate password length
        if (password.length() < 8) {
            showError("Password must be at least 8 characters long");
            return;
        }

        try {
            // Get client IP address
            String ipAddress = "127.0.0.1"; // Default to localhost for now
            // In a real application, you would get this from the request
            
            // Attempt to login with either username or email
            String sessionToken = authService.loginWithUsernameOrEmail(loginInput, password, ipAddress);

            if (sessionToken != null) {
                // Save session with timeout
                SessionManager.getInstance().setSessionToken(sessionToken, rememberMe);
                SessionManager.getInstance().setCurrentUsername(loginInput);
                SessionManager.getInstance().setSessionTimeout(SESSION_TIMEOUT_MINUTES);

                showSuccess("Login successful! Redirecting...");

                // Add a small delay before redirecting
                PauseTransition pause = new PauseTransition(Duration.seconds(1));
                pause.setOnFinished(e -> {
                    try {
                        navigateToDashboard(event);
                    } catch (IOException ex) {
                        showError("Error navigating to dashboard: " + ex.getMessage());
                    }
                });
                pause.play();
            } else {
                showError("Invalid username/email or password");
            }
        } catch (SecurityException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        } catch (Exception e) {
            showError("An unexpected error occurred: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    /**
     * Navigate to registration page
     */
    @FXML
    private void navigateToRegister(ActionEvent event) throws IOException {
        Parent registerParent = FXMLLoader.load(getClass().getResource("/Auth/register.fxml"));
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
        // TODO: Implement forgot password functionality
        showError("Forgot password functionality coming soon!");
    }

    /**
     * Navigate to dashboard
     */
    private void navigateToDashboard(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/userinterfaces/AfficherUsers.fxml"));
            Parent dashboardParent = loader.load();

            // Here you can pass the session to the next controller if needed
            // UserDashboardController controller = loader.getController();
            // controller.initData(SessionManager.getInstance().getSessionToken());

            Scene dashboardScene = new Scene(dashboardParent);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(dashboardScene);
            window.setTitle("Dashboard");
            window.show();
        } catch (IOException e) {
            throw new IOException("Failed to load dashboard: " + e.getMessage(), e);
        }
    }

    /**
     * Show an error message
     */
    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
        errorMessage.setManaged(true);
        successMessage.setVisible(false);
        successMessage.setManaged(false);
    }

    /**
     * Show a success message
     */
    private void showSuccess(String message) {
        successMessage.setText(message);
        successMessage.setVisible(true);
        successMessage.setManaged(true);
        errorMessage.setVisible(false);
        errorMessage.setManaged(false);
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
        // Check for saved session
        if (SessionManager.getInstance().isLoggedIn()) {
            try {
                String sessionToken = SessionManager.getInstance().getSessionToken();
                String ipAddress = "127.0.0.1"; // Same default as above
                if (sessionToken != null && authService.isAuthenticated(sessionToken, ipAddress)) {
                    showSuccess("Welcome back! Redirecting to dashboard...");

                    PauseTransition pause = new PauseTransition(Duration.seconds(1));
                    pause.setOnFinished(e -> {
                        try {
                            navigateToDashboard(new ActionEvent(usernameField, null));
                        } catch (IOException ex) {
                            showError("Error redirecting to dashboard: " + ex.getMessage());
                        }
                    });
                    pause.play();
                } else {
                    // Clear invalid session
                    SessionManager.getInstance().clearSession();
                }
            } catch (Exception e) {
                showError("Session validation error: " + e.getMessage());
                SessionManager.getInstance().clearSession();
            }
        }

        // Check for registration success message
        String message = SessionManager.getInstance().getTemporaryMessage();
        if (message != null && !message.isEmpty()) {
            showSuccess(message);
            SessionManager.getInstance().clearTemporaryMessage();
        }
    }
}