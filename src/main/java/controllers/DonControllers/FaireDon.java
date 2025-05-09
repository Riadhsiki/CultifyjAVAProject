package controllers.DonControllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Association;
import models.Don;
import models.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.associationDon.AssociationServices;
import services.associationDon.DonServices;
import services.user.UserService;
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
    private Label montantErrorLabel;

    @FXML
    private Button validerButton;

    @FXML
    private VBox donFormFields;

    private int associationId;
    private Association association;
    private AssociationServices associationServices = new AssociationServices();
    private DonServices donServices = new DonServices();
    private UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the error label if it doesn't exist
        if (montantErrorLabel == null) {
            montantErrorLabel = new Label();
            montantErrorLabel.getStyleClass().add("error-label");
            montantErrorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");

            // Add the error label after the amount field
            if (donFormFields != null) {
                for (int i = 0; i < donFormFields.getChildren().size(); i++) {
                    if (donFormFields.getChildren().get(i) instanceof HBox) {
                        HBox hbox = (HBox) donFormFields.getChildren().get(i);
                        if (hbox.getChildren().contains(montantField)) {
                            donFormFields.getChildren().add(i + 1, montantErrorLabel);
                            break;
                        }
                    }
                }
            }
        }

        // Clear error label initially
        montantErrorLabel.setText("");

        // Add a listener to clear the error message when the user types
        if (montantField != null) {
            montantField.textProperty().addListener((observable, oldValue, newValue) -> {
                montantErrorLabel.setText("");
            });
        }

        // Set the action for the validation button
        if (validerButton != null) {
            validerButton.setOnAction(event -> handleDonation());
        }

        // Check if the user is logged in
        checkUserLoggedIn();
    }

    private void checkUserLoggedIn() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            showAlert("Erreur", "Vous devez être connecté pour faire un don.", Alert.AlertType.ERROR);
            if (validerButton != null) {
                validerButton.setDisable(true);
            }
        }
    }

    public void setAssociationId(int associationId) {
        this.associationId = associationId;
        loadAssociationDetails();
    }

    private void loadAssociationDetails() {
        try {
            this.association = associationServices.getById(associationId);
            if (association != null && associationNameLabel != null) {
                associationNameLabel.setText("Faire un don à l'association : " + association.getNom());
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de récupérer les informations de l'association: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateMontant() {
        String montantText = montantField.getText().trim();

        // Check if the field is empty
        if (montantText.isEmpty()) {
            montantErrorLabel.setText("Le montant du don ne peut pas être vide.");
            return false;
        }

        // Check if the amount is a valid number
        try {
            double montant = Double.parseDouble(montantText);

            // Check if the amount is positive
            if (montant <= 0) {
                montantErrorLabel.setText("Le montant du don doit être un nombre positif.");
                return false;
            }
        } catch (NumberFormatException e) {
            montantErrorLabel.setText("Le montant doit être un nombre valide.");
            return false;
        }

        // If all validations pass
        return true;
    }

    private void handleDonation() {
        try {
            // Check session state
            if (!SessionManager.getInstance().isLoggedIn()) {
                showAlert("Erreur", "Vous devez être connecté pour faire un don.", Alert.AlertType.ERROR);
                return;
            }

            // Get current user from session
            String currentUsername = SessionManager.getInstance().getCurrentUsername();
            if (currentUsername == null || currentUsername.isEmpty()) {
                showAlert("Erreur", "Impossible de récupérer les informations de l'utilisateur de la session.", Alert.AlertType.ERROR);
                return;
            }

            User currentUser = userService.getUserByUsername(currentUsername);
            if (currentUser == null) {
                showAlert("Erreur", "Utilisateur introuvable: " + currentUsername, Alert.AlertType.ERROR);
                return;
            }

            // Validate the amount
            if (!validateMontant()) {
                return;
            }

            double montant = Double.parseDouble(montantField.getText().trim());

            // Create a new donation
            Don don = new Don();
            don.setMontant(montant);
            don.setDonorType(currentUser.getRoles());
            don.setStatus("en_attente");
            don.setType("don");
            don.setAssociation(association);
            don.setUser(currentUser);

            // Save the donation
            donServices.Add(don);

            showAlert("Succès", "Votre don a été enregistré avec succès.", Alert.AlertType.INFORMATION);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Don/ListDon.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) validerButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (SQLException e) {
            showAlert("Erreur", "Une erreur est survenue lors de l'enregistrement du don: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur inattendue est survenue: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Apply CSS style
        DialogPane dialogPane = alert.getDialogPane();
        try {
            String cssUrl = getClass().getResource("/Association/styles.css").toExternalForm();
            if (cssUrl != null) {
                dialogPane.getStylesheets().add(cssUrl);
                dialogPane.getStyleClass().add("custom-alert");
            }
        } catch (Exception e) {
            System.err.println("Failed to load CSS: " + e.getMessage());
        }

        alert.showAndWait();
    }
}