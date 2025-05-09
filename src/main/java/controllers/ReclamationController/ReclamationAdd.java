package controllers.ReclamationController;

import models.Reclamation;
import services.reclamation.ReclamationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ReclamationAdd implements Initializable {

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private TextField titreField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ComboBox<String> prioriteComboBox;

    @FXML
    private TextField emailField;

    @FXML
    private Button annulerButton;

    @FXML
    private Button ajouterButton;

    @FXML
    private Label messageLabel;

    private ReclamationService reclamationService;

    // Regex pour validation d'email
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser le service
        reclamationService = new ReclamationService();

        // Remplir les comboBox
        ObservableList<String> typeOptions = FXCollections.observableArrayList(
                "Problème dans le compte",
                "Problème de réservation",
                "Problème de don",
                "Problème dans l'encyclopédie"
        );
        typeComboBox.setItems(typeOptions);

        ObservableList<String> prioriteOptions = FXCollections.observableArrayList(
                "Haute",
                "Moyenne",
                "Basse"
        );
        prioriteComboBox.setItems(prioriteOptions);

        // Définir une priorité par défaut
        prioriteComboBox.setValue("Moyenne");
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        if (validateInputs()) {
            try {
                // Créer la réclamation
                Reclamation reclamation = new Reclamation();
                reclamation.setType(typeComboBox.getValue());
                reclamation.setTitre(titreField.getText().trim());
                reclamation.setDescription(descriptionArea.getText().trim());
                reclamation.setStatut("En cours"); // Statut par défaut
                reclamation.setPriorite(prioriteComboBox.getValue());
                reclamation.setEmail(emailField.getText().trim());

                // Enregistrer la réclamation
                reclamationService.add(reclamation);

                // Afficher un message de succès
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Réclamation ajoutée avec succès!");

                // Fermer la fenêtre
                closeWindow();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout de la réclamation: " + e.getMessage());
                System.err.println(e.getMessage());
            }
        }
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        closeWindow();
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        if (typeComboBox.getValue() == null || typeComboBox.getValue().isEmpty()) {
            errorMessage.append("- Veuillez sélectionner un type de réclamation.\n");
        }

        if (titreField.getText().trim().isEmpty()) {
            errorMessage.append("- Le titre ne peut pas être vide.\n");
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            errorMessage.append("- La description ne peut pas être vide.\n");
        }

        if (prioriteComboBox.getValue() == null || prioriteComboBox.getValue().isEmpty()) {
            errorMessage.append("- Veuillez sélectionner une priorité.\n");
        }

        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            errorMessage.append("- L'email ne peut pas être vide.\n");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            errorMessage.append("- Format d'email invalide.\n");
        }

        if (errorMessage.length() > 0) {
            // Afficher les erreurs
            messageLabel.setText(errorMessage.toString());
            messageLabel.setVisible(true);
            return false;
        } else {
            messageLabel.setVisible(false);
            return true;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) annulerButton.getScene().getWindow();
        stage.close();
    }
}