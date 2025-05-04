package controllers.donControllers;

import models.Association;
import models.Don;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.associationDon.AssociationServices;
import services.associationDon.DonServices;

import java.net.URL;
import java.util.ResourceBundle;

public class UpdateDon implements Initializable {

    @FXML
    private ComboBox<Association> associationCombo;
    @FXML
    private TextField montantField;
    @FXML
    private Label associationErrorLabel;
    @FXML
    private Label montantErrorLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private Don currentDon;
    private final DonServices donService = new DonServices();
    private final AssociationServices associationService = new AssociationServices();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configurer les labels d'erreur
        associationErrorLabel.setWrapText(true);
        associationErrorLabel.setMaxWidth(250);
        montantErrorLabel.setWrapText(true);
        montantErrorLabel.setMaxWidth(250);

        // Initialiser les combobox
        initializeAssociations();

        // Effacer les labels d'erreur au démarrage
        clearErrorLabels();

        // Configurer les boutons
        saveButton.setOnAction(event -> saveDon());
        cancelButton.setOnAction(event -> cancel());

        // Effacer les erreurs quand l'utilisateur commence à taper
        montantField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                montantErrorLabel.setText("");
            }
        });

        associationCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                associationErrorLabel.setText("");
            }
        });
    }

    private void clearErrorLabels() {
        associationErrorLabel.setText("");
        montantErrorLabel.setText("");
    }

    private void initializeAssociations() {
        try {
            ObservableList<Association> associations = FXCollections.observableArrayList(
                    associationService.getAll()
            );
            associationCombo.setItems(associations);

            associationCombo.setCellFactory(lv -> new ListCell<Association>() {
                @Override
                protected void updateItem(Association item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNom());
                }
            });

            associationCombo.setButtonCell(new ListCell<Association>() {
                @Override
                protected void updateItem(Association item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNom());
                }
            });
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les associations: " + e.getMessage());
        }
    }

    public void setDon(Don don) {
        this.currentDon = don;

        if (don != null) {
            montantField.setText(String.valueOf(don.getMontant()));

            if (don.getAssociation() != null) {
                associationCombo.getItems().stream()
                        .filter(a -> a.getId() == don.getAssociation().getId())
                        .findFirst()
                        .ifPresent(associationCombo::setValue);
            }
        }
    }

    private boolean validateFields() {
        boolean isValid = true;
        clearErrorLabels();

        if (associationCombo.getValue() == null) {
            associationErrorLabel.setText("Veuillez sélectionner une association.");
            isValid = false;
        }

        String montantText = montantField.getText().trim();
        if (montantText.isEmpty()) {
            montantErrorLabel.setText("Le montant du don ne peut pas être vide.");
            isValid = false;
        } else {
            try {
                double montantValue = Double.parseDouble(montantText);
                if (montantValue <= 0) {
                    montantErrorLabel.setText("Le montant du don doit être un nombre positif.");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                montantErrorLabel.setText("Le montant doit être un nombre valide.");
                isValid = false;
            }
        }

        return isValid;
    }

    private void saveDon() {
        try {
            if (!validateFields()) {
                return;
            }

            double montant = Double.parseDouble(montantField.getText().trim());
            currentDon.setAssociation(associationCombo.getValue());
            currentDon.setMontant(montant);

            donService.update(currentDon, currentDon.getAssociation().getId());

            showAlert("Succès", "Don mis à jour avec succès");
            closeWindow();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    private void cancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Appliquer le style CSS
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Association/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        alert.showAndWait();
    }
}