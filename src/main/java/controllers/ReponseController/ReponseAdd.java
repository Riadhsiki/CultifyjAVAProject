package controllers.ReponseController;

import models.Reclamation;
import models.Reponse;
import services.reclamation.ReclamationService;
import services.reponse.ReponseService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ReponseAdd implements Initializable {

    @FXML
    private ComboBox<Reclamation> reclamationComboBox;

    @FXML
    private TextField titreField;

    @FXML
    private TextArea contenuArea;

    @FXML
    private TextField offreField;

    @FXML
    private Button ajouterBtn;

    @FXML
    private Button annulerBtn;

    @FXML
    private Label messageLabel;

    private ReponseService reponseService;
    private ReclamationService reclamationService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reponseService = new ReponseService();
        reclamationService = new ReclamationService();

        // Initialiser le ReponseService dans ReclamationService si nécessaire
        reclamationService.setReponseService(reponseService);

        reclamationComboBox.setConverter(new StringConverter<Reclamation>() {
            @Override
            public String toString(Reclamation reclamation) {
                return reclamation != null ? "#" + reclamation.getId_reclamation() + " - " + reclamation.getTitre() : "";
            }

            @Override
            public Reclamation fromString(String string) {
                return null;
            }
        });

        loadReclamationsNonTraitees();

        ajouterBtn.setOnAction(this::handleAjouterAction);
        annulerBtn.setOnAction(this::handleAnnulerAction);

        titreField.textProperty().addListener((obs, oldVal, newVal) -> messageLabel.setText(""));
        contenuArea.textProperty().addListener((obs, oldVal, newVal) -> messageLabel.setText(""));
        offreField.textProperty().addListener((obs, oldVal, newVal) -> messageLabel.setText(""));
    }

    private void loadReclamationsNonTraitees() {
        try {
            List<Reclamation> allReclamations = reclamationService.getAll();
            List<Reclamation> reclamationsNonTraitees = allReclamations.stream()
                    .filter(r -> !"Traité".equals(r.getStatut()))
                    .collect(Collectors.toList());

            ObservableList<Reclamation> reclamationsList = FXCollections.observableArrayList(reclamationsNonTraitees);
            reclamationComboBox.setItems(reclamationsList);

        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des réclamations: " + e.getMessage());
            messageLabel.setText("Erreur lors du chargement des réclamations");
            messageLabel.getStyleClass().setAll("error-message");
        }
    }

    @FXML
    private void handleAjouterAction(ActionEvent event) {
        if (validateInputs()) {
            try {
                Reclamation selectedReclamation = reclamationComboBox.getValue();

                Reponse reponse = new Reponse();
                reponse.setTitre(titreField.getText().trim());
                reponse.setContenu(contenuArea.getText().trim());
                reponse.setOffre(offreField.getText().trim());
                reponse.setReponsedate(new Date());
                reponse.setReclamation(selectedReclamation);

                // Ajouter la réponse à la base de données
                reponseService.add(reponse);

                // Mettre à jour le statut de la réclamation
                selectedReclamation.setStatut("Traité");
                selectedReclamation.setReponse(reponse);
                reclamationService.update(selectedReclamation);

                // Afficher un message de succès
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Réponse ajoutée",
                        "La réponse a été créée avec succès et la réclamation a été marquée comme traitée.");

                // Redirection vers la liste des réponses
                redirectToReponseListAdmin();

            } catch (SQLException e) {
                System.err.println("Erreur lors de l'ajout de la réponse: " + e.getMessage());
                messageLabel.setText("Erreur lors de l'ajout de la réponse: " + e.getMessage());
                messageLabel.getStyleClass().setAll("error-message");
            }
        }
    }

    @FXML
    private void handleAnnulerAction(ActionEvent event) {
        redirectToReponseListAdmin();
    }

    private void redirectToReponseListAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reponse/ReponseListAdmin.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ajouterBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Réponses");
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur lors de la redirection: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de navigation",
                    "Impossible de charger la liste des réponses: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();
        boolean isValid = true;

        if (reclamationComboBox.getValue() == null) {
            errorMessage.append("Veuillez sélectionner une réclamation\n");
            isValid = false;
        }

        if (titreField.getText().trim().isEmpty()) {
            errorMessage.append("Le titre ne peut pas être vide\n");
            isValid = false;
        }

        if (contenuArea.getText().trim().isEmpty()) {
            errorMessage.append("Le contenu ne peut pas être vide\n");
            isValid = false;
        }

        if (!isValid) {
            messageLabel.setText(errorMessage.toString().trim());
            messageLabel.getStyleClass().setAll("error-message");
        }

        return isValid;
    }

    private void clearFields() {
        titreField.clear();
        contenuArea.clear();
        offreField.clear();
        reclamationComboBox.getSelectionModel().clearSelection();
        messageLabel.setText("");
    }

    public void setSelectedReclamation(Reclamation reclamation) {
        reclamationComboBox.getSelectionModel().select(reclamation);
        reclamationComboBox.setDisable(true);
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}