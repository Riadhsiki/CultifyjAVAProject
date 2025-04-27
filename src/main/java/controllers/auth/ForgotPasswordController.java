package controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.auth.AuthenticationService;
import utils.DataSource;
import utils.SessionManager;

import java.io.IOException;
import java.util.regex.Pattern;

public class ForgotPasswordController {
/**
    @FXML private TextField emailField;
    @FXML private Button submitButton;
    @FXML private Text errorMessage;
    @FXML private Text successMessage;

    private final AuthenticationService authService;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    );

    public ForgotPasswordController() {
        this.authService = new AuthenticationService(DataSource.getInstance().getConnection());
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        hideMessages();

        String email = emailField.getText().trim();

        // Validate email
        if (email.isEmpty()) {
            showError("Email is required");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Invalid email format");
            return;
        }

        try {
            // Simulate password reset request
            boolean resetInitiated = authService.initiatePasswordReset(email);
            if (resetInitiated) {
                SessionManager.getInstance().setTemporaryMessage("Password reset link sent to your email.");
                showSuccess("Check your email for a reset link.");
                // Optionally navigate to login after delay
                navigateToLogin(event);
            } else {
                showError("Email not found in our system.");
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    private void navigateToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Auth/login.fxml"));
            if (loader.getLocation() == null) {
                showError("Login page not found");
                return;
            }
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("CultureSketch - Login");
            stage.show();
        } catch (IOException e) {
            showError("Failed to load login page: " + e.getMessage());
        }
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
    }**/
}