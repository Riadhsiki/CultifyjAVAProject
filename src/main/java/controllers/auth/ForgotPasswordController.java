package controllers.auth;

import javafx.animation.PauseTransition;
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
import javafx.util.Duration;
import services.auth.AuthenticationService;
import utils.EmailSender;
import utils.SessionManager;

import java.io.IOException;
import java.util.regex.Pattern;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Button submitButton;
    @FXML private Text errorMessage;
    @FXML private Text successMessage;

    private final AuthenticationService authService;
    private final EmailSender emailSender;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    );

    public ForgotPasswordController() {
        this.authService = AuthenticationService.getInstance();
        this.emailSender = new EmailSender();
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
            System.out.println("Starting password reset for email: " + email);
            // Initiate password reset
            boolean resetInitiated = authService.initiatePasswordReset(email);
            System.out.println("Reset initiated: " + resetInitiated);

            if (resetInitiated) {
                // Generate a reset token
                String resetToken = "sample-token-" + System.currentTimeMillis();
                String resetLink = "http://yourapp.com/reset?token=" + resetToken;

                // Prepare email content
                String subject = "Cultify Password Reset Request";
                String body = "Hello {prenom},\n\nYou requested a password reset for your Cultify account (username: {username}).\n" +
                        "Please click the link below to reset your password:\n\n" +
                        resetLink + "\n\nIf you did not request this, please ignore this email.\n\n" +
                        "Best regards,\nThe Cultify Team";

                // Placeholder values for prenom and username
                String prenom = "User"; // Replace with authService.getUserFirstName(email) if available
                String username = email; // Replace with authService.getUsername(email) if available

                // Try sending email with attachment
                System.out.println("Attempting to send email with attachment to: " + email);
                try {
                    emailSender.sendEmailWithAttachment(email, subject, body, prenom, username);
                    System.out.println("Email with attachment sent successfully");
                } catch (Exception attachmentEx) {
                    System.err.println("Failed to send email with attachment: " + attachmentEx.getMessage());
                    attachmentEx.printStackTrace();
                    // Fallback to plain text email
                    System.out.println("Falling back to plain text email");
                    emailSender.sendEmail(email, subject, body.replace("{prenom}", prenom).replace("{username}", username));
                    System.out.println("Plain text email sent successfully");
                }

                // Store success message in SessionManager
                SessionManager.getInstance().setTemporaryMessage("Password reset link sent to your email.");
                showSuccess("Check your email for a reset link.");

                // Navigate back to login after a delay
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(e -> navigateToLogin(event));
                pause.play();
            } else {
                showError("Email not found in our system.");
            }
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "An unexpected error occurred";
            showError("Error sending reset email: " + errorMsg);
            System.err.println("Error in handleSubmit: " + errorMsg);
            e.printStackTrace();
        }
    }

    @FXML
    private void navigateToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/Login.fxml"));
            if (loader.getLocation() == null) {
                showError("Login page resource not found");
                return;
            }
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Cultify - Login");
            stage.show();
        } catch (IOException e) {
            showError("Failed to load login page: " + e.getMessage());
            e.printStackTrace();
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
    }
}