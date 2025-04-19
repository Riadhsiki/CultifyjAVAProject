package Controllers.User;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class LoginController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Text welcomeText;
    @FXML private ImageView logoImage;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label emailErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Button loginButton;

    // Pattern pour la validation d'email
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Réinitialiser les labels d'erreur
        emailErrorLabel.setText("");
        passwordErrorLabel.setText("");

        // Appliquer l'animation au texte de bienvenue
        animateWelcomeText();

        // Ajouter la validation en temps réel (optionnel)
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateEmail(newValue);
        });
    }

    /**
     * Animation du texte de bienvenue avec plusieurs effets
     */
    private void animateWelcomeText() {
        // Configuration initiale
        welcomeText.setOpacity(0);
        welcomeText.setScaleX(0.8);
        welcomeText.setScaleY(0.8);
        welcomeText.setTranslateY(-20);

        // Transition de fondu
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), welcomeText);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Transition d'échelle
        ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(1.2), welcomeText);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1);
        scaleIn.setToY(1);

        // Transition de position
        TranslateTransition moveUp = new TranslateTransition(Duration.seconds(1), welcomeText);
        moveUp.setFromY(-20);
        moveUp.setToY(0);

        // Combinaison des animations
        SequentialTransition sequence = new SequentialTransition(
                new SequentialTransition(fadeIn, scaleIn, moveUp)
        );

        // Lancer l'animation
        sequence.play();
    }

    @FXML
    void handleLogin(ActionEvent event) {
        boolean isValid = validateForm();

        if (isValid) {
            try {
                // Ici vous pouvez ajouter votre logique d'authentification

                // Navigation vers la page principale après connexion réussie
                Parent root = FXMLLoader.load(getClass().getResource("/views/Dashboard.fxml"));
                rootPane.getScene().setRoot(root);

            } catch (IOException e) {
                showAlert("Erreur", "Erreur de navigation: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validation de l'email
        if (!validateEmail(emailField.getText())) {
            isValid = false;
        }

        // Validation du mot de passe
        if (passwordField.getText().trim().isEmpty()) {
            passwordErrorLabel.setText("Le mot de passe ne peut pas être vide.");
            isValid = false;
        } else if (passwordField.getText().length() < 6) {
            passwordErrorLabel.setText("Le mot de passe doit contenir au moins 6 caractères.");
            isValid = false;
        } else {
            passwordErrorLabel.setText("");
        }

        return isValid;
    }

    private boolean validateEmail(String email) {
        if (email.trim().isEmpty()) {
            emailErrorLabel.setText("L'email ne peut pas être vide.");
            return false;
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            emailErrorLabel.setText("Format d'email invalide.");
            return false;
        } else {
            emailErrorLabel.setText("");
            return true;
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}