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
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class RegisterController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Text titleText;
    @FXML private ImageView logoImage;

    // Champs du formulaire
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox termsCheckBox;
    @FXML private Button registerButton;

    // Labels d'erreur
    @FXML private Label firstNameErrorLabel;
    @FXML private Label lastNameErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label confirmPasswordErrorLabel;
    @FXML private Label termsErrorLabel;

    // Patterns pour la validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,3}[-\\s.]?[0-9]{4,6}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} .'-]+$");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Réinitialiser tous les labels d'erreur
        clearErrorLabels();

        // Appliquer l'animation au texte de titre
        animateTitleText();

        // Ajouter des listeners pour la validation en temps réel (optionnel)
        setupValidationListeners();
    }

    private void clearErrorLabels() {
        firstNameErrorLabel.setText("");
        lastNameErrorLabel.setText("");
        emailErrorLabel.setText("");
        phoneErrorLabel.setText("");
        passwordErrorLabel.setText("");
        confirmPasswordErrorLabel.setText("");
        termsErrorLabel.setText("");
    }

    private void setupValidationListeners() {
        emailField.textProperty().addListener((observable, oldValue, newValue) -> validateEmail(newValue));

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePassword(newValue);
            if (!confirmPasswordField.getText().isEmpty()) {
                validatePasswordMatch(confirmPasswordField.getText());
            }
        });

        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) ->
                validatePasswordMatch(newValue));
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
    void handleRegister(ActionEvent event) {
        clearErrorLabels();

        if (validateForm()) {
            try {
                // Dans une application réelle, ici vous créeriez le compte utilisateur
                // et inséreriez les données dans la base de données

                // Afficher une confirmation
                showAlert("Inscription réussie",
                        "Votre compte a été créé avec succès. Bienvenue chez Cultify!",
                        Alert.AlertType.INFORMATION);

                // Rediriger vers la page de connexion ou le tableau de bord après confirmation
                navigateToDashboard();

            } catch (Exception e) {
                showAlert("Erreur", "Une erreur est survenue lors de la création du compte: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    void navigateToLogin(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            rootPane.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert("Erreur", "Erreur de navigation: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void navigateToDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Dashboard.fxml"));
            rootPane.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert("Erreur", "Erreur de navigation: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validation du prénom
        if (firstNameField.getText().trim().isEmpty()) {
            firstNameErrorLabel.setText("Le prénom ne peut pas être vide.");
            isValid = false;
        } else if (!NAME_PATTERN.matcher(firstNameField.getText().trim()).matches()) {
            firstNameErrorLabel.setText("Le prénom contient des caractères non valides.");
            isValid = false;
        } else if (firstNameField.getText().trim().length() < 2) {
            firstNameErrorLabel.setText("Le prénom doit contenir au moins 2 caractères.");
            isValid = false;
        } else {
            firstNameErrorLabel.setText("");
        }

        // Validation du nom
        if (lastNameField.getText().trim().isEmpty()) {
            lastNameErrorLabel.setText("Le nom ne peut pas être vide.");
            isValid = false;
        } else if (!NAME_PATTERN.matcher(lastNameField.getText().trim()).matches()) {
            lastNameErrorLabel.setText("Le nom contient des caractères non valides.");
            isValid = false;
        } else if (lastNameField.getText().trim().length() < 2) {
            lastNameErrorLabel.setText("Le nom doit contenir au moins 2 caractères.");
            isValid = false;
        } else {
            lastNameErrorLabel.setText("");
        }

        // Validation de l'email
        if (!validateEmail(emailField.getText())) {
            isValid = false;
        }

        // Validation du téléphone (optionnel)
        if (!phoneField.getText().trim().isEmpty() && !PHONE_PATTERN.matcher(phoneField.getText().trim()).matches()) {
            phoneErrorLabel.setText("Format de numéro de téléphone invalide.");
            isValid = false;
        } else {
            phoneErrorLabel.setText("");
        }

        // Validation du mot de passe
        if (!validatePassword(passwordField.getText())) {
            isValid = false;
        }

        // Validation de la confirmation du mot de passe
        if (!validatePasswordMatch(confirmPasswordField.getText())) {
            isValid = false;
        }

        // Validation des termes
        if (!termsCheckBox.isSelected()) {
            termsErrorLabel.setText("Vous devez accepter les conditions générales d'utilisation.");
            isValid = false;
        } else {
            termsErrorLabel.setText("");
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

    private boolean validatePassword(String password) {
        if (password.trim().isEmpty()) {
            passwordErrorLabel.setText("Le mot de passe ne peut pas être vide.");
            return false;
        } else if (password.length() < 8) {
            passwordErrorLabel.setText("Le mot de passe doit contenir au moins 8 caractères.");
            return false;
        } else if (!password.matches(".*[A-Z].*")) {
            passwordErrorLabel.setText("Le mot de passe doit contenir au moins une majuscule.");
            return false;
        } else if (!password.matches(".*[a-z].*")) {
            passwordErrorLabel.setText("Le mot de passe doit contenir au moins une minuscule.");
            return false;
        } else if (!password.matches(".*[0-9].*")) {
            passwordErrorLabel.setText("Le mot de passe doit contenir au moins un chiffre.");
            return false;
        } else if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            passwordErrorLabel.setText("Le mot de passe doit contenir au moins un caractère spécial.");
            return false;
        } else {
            passwordErrorLabel.setText("");
            return true;
        }
    }

    private boolean validatePasswordMatch(String confirmPassword) {
        if (confirmPassword.trim().isEmpty()) {
            confirmPasswordErrorLabel.setText("La confirmation du mot de passe ne peut pas être vide.");
            return false;
        } else if (!confirmPassword.equals(passwordField.getText())) {
            confirmPasswordErrorLabel.setText("Les mots de passe ne correspondent pas.");
            return false;
        } else {
            confirmPasswordErrorLabel.setText("");
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