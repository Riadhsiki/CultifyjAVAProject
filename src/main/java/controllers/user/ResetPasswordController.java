package controllers.user;

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
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ResetPasswordController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Text titleText;
    @FXML private ImageView logoImage;
    @FXML private TextField emailField;
    @FXML private Label emailErrorLabel;
    @FXML private Button resetButton;
    @FXML private Button backToLoginButton;

    // Pattern pour la validation d'email
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Réinitialiser les labels d'erreur
        emailErrorLabel.setText("");

        // Appliquer l'animation au texte de titre
        animateTitleText();

        // Ajouter la validation en temps réel (optionnel)
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateEmail(newValue);
        });
    }

    /**
     * Animation du texte de titre avec plusieurs effets
     */
    private void animateTitleText() {
        // Configuration initiale
        titleText.setOpacity(0);
        titleText.setScaleX(0.8);
        titleText.setScaleY(0.8);
        titleText.setTranslateY(-20);

        // Transition de fondu
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), titleText);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Transition d'échelle
        ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(1.2), titleText);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1);
        scaleIn.setToY(1);

        // Transition de position
        TranslateTransition moveUp = new TranslateTransition(Duration.seconds(1), titleText);
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
    void handleResetRequest(ActionEvent event) {
        if (validateEmail(emailField.getText())) {
            // Dans une application réelle, ici vous enverriez un email de réinitialisation

            // Afficher une confirmation
            showAlert("Demande envoyée",
                    "Si un compte existe avec l'adresse " + emailField.getText() + ", " +
                            "vous recevrez sous peu un email contenant les instructions de réinitialisation.",
                    Alert.AlertType.INFORMATION);

            // Rediriger vers la page de connexion après confirmation
            navigateToLogin();
        }
    }

    @FXML
    void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            rootPane.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert("Erreur", "Erreur de navigation: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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