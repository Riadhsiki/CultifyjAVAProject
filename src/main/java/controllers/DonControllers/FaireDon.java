package controllers.DonControllers;

import entities.Association;
import entities.Don;
import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.AssociationServices;
import services.DonServices;
import utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class FaireDon implements Initializable {

    @FXML
    private Label associationNameLabel;

    @FXML
    private TextField montantField;

    @FXML
    private Label montantErrorLabel; // Ajout du label d'erreur pour le montant

    @FXML
    private Button validerButton;

    @FXML
    private VBox donFormFields; // Référence à la VBox contenant les champs

    private int associationId;
    private Association association;
    private AssociationServices associationServices = new AssociationServices();
    private DonServices donServices = new DonServices();
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUser = new User();
        currentUser.setId(2);
        currentUser.setName("user");
        currentUser.setEmail("codedon10@gmail.com");
        currentUser.setRole("user");

        // Créer et configurer le label d'erreur s'il n'existe pas dans le FXML
        if (montantErrorLabel == null) {
            montantErrorLabel = new Label();
            montantErrorLabel.getStyleClass().add("error-label");
            montantErrorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");

            // Ajout du label d'erreur après le champ de montant dans la VBox
            // Nous devons trouver la HBox contenant le champ montantField
            for (int i = 0; i < donFormFields.getChildren().size(); i++) {
                if (donFormFields.getChildren().get(i) instanceof HBox) {
                    HBox hbox = (HBox) donFormFields.getChildren().get(i);
                    if (hbox.getChildren().contains(montantField)) {
                        // Insérer le label d'erreur après la HBox
                        donFormFields.getChildren().add(i + 1, montantErrorLabel);
                        break;
                    }
                }
            }
        }

        // Clear error label initially
        montantErrorLabel.setText("");

        // Ajouter un écouteur pour effacer le message d'erreur lorsque l'utilisateur commence à taper
        montantField.textProperty().addListener((observable, oldValue, newValue) -> {
            montantErrorLabel.setText("");
        });

        validerButton.setOnAction(event -> handleDonation());
    }

    public void setAssociationId(int associationId) {
        this.associationId = associationId;
        loadAssociationDetails();
    }

    private void loadAssociationDetails() {
        try {
            this.association = associationServices.getById(associationId);
            if (association != null) {
                // Mettre à jour le label avec le nom de l'association
                associationNameLabel.setText("Faire un don à l'association : " + association.getNom());
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de récupérer les informations de l'association.", Alert.AlertType.ERROR);
        }
    }

    private boolean validateMontant() {
        String montantText = montantField.getText().trim();

        // Vérifier si le champ est vide
        if (montantText.isEmpty()) {
            montantErrorLabel.setText("Le montant du don ne peut pas être vide.");
            return false;
        }

        // Vérifier si le montant est un nombre valide
        try {
            double montant = Double.parseDouble(montantText);

            // Vérifier si le montant est positif
            if (montant <= 0) {
                montantErrorLabel.setText("Le montant du don doit être un nombre positif.");
                return false;
            }
        } catch (NumberFormatException e) {
            montantErrorLabel.setText("Le montant doit être un nombre valide.");
            return false;
        }

        // Si toutes les validations passent
        return true;
    }

    private void handleDonation() {
        try {
            if (currentUser == null) {
                showAlert("Erreur", "Aucun utilisateur connecté", Alert.AlertType.ERROR);
                return;
            }

            // Valider le montant
            if (!validateMontant()) {
                return;
            }

            double montant = Double.parseDouble(montantField.getText().trim());

            // Créer un nouveau don
            Don don = new Don();
            don.setMontant(montant);
            don.setDonorType(currentUser.getRole());
            don.setStatus("en_attente");
            don.setType("don");
            don.setAssociation(association);
            don.setUser(currentUser);

            // Enregistrer le don
            donServices.Add(currentUser.getId(), don);

            showAlert("Succès", "Votre don a été enregistré avec succès.", Alert.AlertType.INFORMATION);
            validerButton.getScene().getWindow().hide();

        } catch (SQLException e) {
            showAlert("Erreur", "Une erreur est survenue lors de l'enregistrement du don: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Appliquer le style CSS
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Association/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        alert.showAndWait();
    }
}