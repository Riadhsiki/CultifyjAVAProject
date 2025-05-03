package controllers.auth;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.auth.AuthenticationService;
import utils.DataSource;
import utils.SessionManager;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Text errorMessage;
    @FXML private Text successMessage;
    @FXML private Button fingerprintLoginButton;

    private final AuthenticationService authService;
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    public LoginController() {
        this.authService = AuthenticationService.getInstance();
    }

    @FXML
    private void initialize() throws SQLException {
        // Debug session state
        SessionManager.getInstance().dumpPreferences();
        // Check for saved session
        if (SessionManager.getInstance().isLoggedIn()) {
            String sessionToken = SessionManager.getInstance().getSessionToken();
            String ipAddress = "127.0.0.1";
            if (sessionToken != null && authService.isAuthenticated(sessionToken, ipAddress)) {
                showSuccess("Welcome back! Redirecting to dashboard...");
                redirectToDashboardWithDelay(null);
                return;
            }
            // Only clear session if explicitly invalid
            SessionManager.getInstance().clearSession();
        }

        // Check for registration success message
        String message = SessionManager.getInstance().getTemporaryMessage();
        if (message != null && !message.isEmpty()) {
            showSuccess(message);
            SessionManager.getInstance().clearTemporaryMessage();
        }

        // Disable fingerprint login button if Windows Hello is not available
        fingerprintLoginButton.setDisable(!isWindowsHelloAvailable());
    }

    private boolean isWindowsHelloAvailable() {
        try {
            return System.getProperty("os.name").toLowerCase().contains("windows") &&
                    Integer.parseInt(System.getProperty("os.version").split("\\.")[0]) >= 10;
        } catch (Exception e) {
            return false;
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        hideMessages();

        String loginInput = usernameField.getText().trim();
        String password = passwordField.getText();
        boolean rememberMe = rememberMeCheckbox.isSelected();

        // Validation
        if (loginInput.isEmpty()) {
            showError("Username or email is required");
            return;
        }

        if (password.isEmpty()) {
            showError("Password is required");
            return;
        }

        if (loginInput.contains("@") && !isValidEmail(loginInput)) {
            showError("Please enter a valid email address");
            return;
        }

        if (password.length() < 8) {
            showError("Password must be at least 8 characters long");
            return;
        }

        try {
            String ipAddress = "127.0.0.1";
            String sessionToken = authService.loginWithUsernameOrEmail(loginInput, password, ipAddress);

            if (sessionToken != null) {
                SessionManager.getInstance().setSessionToken(sessionToken, rememberMe);
                SessionManager.getInstance().setCurrentUsername(loginInput);
                SessionManager.getInstance().setSessionTimeout(SESSION_TIMEOUT_MINUTES);
                SessionManager.getInstance().dumpPreferences();
                showSuccess("Login successful! Redirecting...");
                redirectToDashboardWithDelay(event);
            } else {
                showError("Invalid username/email or password");
            }
        } catch (SecurityException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFingerprintLogin(ActionEvent event) {
        hideMessages();

        try {
            String biometricId = promptWindowsHelloFingerprint();
            if (biometricId == null) {
                showError("Fingerprint authentication failed.");
                return;
            }

            String ipAddress = "127.0.0.1";
            String sessionToken = authService.loginWithBiometrics(biometricId, ipAddress);

            if (sessionToken != null) {
                String username = getUsernameFromBiometricId(biometricId);
                SessionManager.getInstance().setSessionToken(sessionToken, rememberMeCheckbox.isSelected());
                SessionManager.getInstance().setCurrentUsername(username);
                SessionManager.getInstance().setSessionTimeout(SESSION_TIMEOUT_MINUTES);
                SessionManager.getInstance().dumpPreferences();
                showSuccess("Fingerprint login successful! Redirecting...");
                redirectToDashboardWithDelay(event);
            } else {
                showError("Fingerprint not recognized.");
            }
        } catch (SecurityException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String promptWindowsHelloFingerprint() {
        try {
            // Placeholder: Replace with actual Windows Hello verification API
            return "biometric_id_" + UUID.randomUUID().toString();
        } catch (Exception e) {
            System.err.println("Windows Hello error: " + e.getMessage());
            return null;
        }
    }

    private String getUsernameFromBiometricId(String biometricId) throws SQLException {
        String query = "SELECT username FROM user WHERE biometric_Id = ?";
        try (PreparedStatement pstmt = DataSource.getInstance().getConnection().prepareStatement(query)) {
            pstmt.setString(1, biometricId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        }
        return "unknown";
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    @FXML
    private void navigateToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/register.fxml"));
            if (loader.getLocation() == null) {
                showError("Registration page not found");
                return;
            }
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/auth.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Register - Cultify");
            stage.show();
        } catch (IOException e) {
            showError("Failed to load registration page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void navigateToForgotPassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/forgotPassword.fxml"));
            if (loader.getLocation() == null) {
                showError("Registration page not found");
                return;
            }
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/auth.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("ForgotenPassword - Cultify");
            stage.show();
        } catch (IOException e) {
            showError("Failed to load forget password page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void redirectToDashboardWithDelay(ActionEvent event) {
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/sidebar/Sidebar.fxml"));
                if (loader.getLocation() == null) {
                    showError("Dashboard page not found");
                    return;
                }
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = event != null ?
                        (Stage) ((Node) event.getSource()).getScene().getWindow() :
                        (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("CultureSketch Dashboard");
                stage.show();
                // Debug session state after navigation
                SessionManager.getInstance().dumpPreferences();
            } catch (IOException ex) {
                showError("Error navigating to dashboard: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        pause.play();
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
        errorMessage.setManaged(true);
        successMessage.setVisible(false);
        successMessage.setManaged(false);
    }

    private void showSuccess(String message) {
        successMessage.setText(message);
        successMessage.setVisible(true);
        successMessage.setManaged(true);
        errorMessage.setVisible(false);
        errorMessage.setManaged(false);
    }

    private void hideMessages() {
        errorMessage.setVisible(false);
        errorMessage.setManaged(false);
        successMessage.setVisible(false);
        successMessage.setManaged(false);
    }
}