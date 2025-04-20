package Controllers.ReponseController;

import Entities.Reclamation;
import Entities.Reponse;
import Services.ReclamationService;
import Services.ReponseService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

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
        // Initialiser les services
        reponseService = new ReponseService();
        reclamationService = new ReclamationService();

        // Configurer le ComboBox pour afficher le titre de la réclamation
        reclamationComboBox.setConverter(new StringConverter<Reclamation>() {
            @Override
            public String toString(Reclamation reclamation) {
                return reclamation != null ? "#" + reclamation.getId_reclamation() + " - " + reclamation.getTitre() : "";
            }

            @Override
            public Reclamation fromString(String string) {
                return null; // Non nécessaire pour cette fonctionnalité
            }
        });

        // Charger les réclamations non traitées
        loadReclamationsNonTraitees();

        // Configurer les événements des boutons
        ajouterBtn.setOnAction(this::handleAjouterAction);
        annulerBtn.setOnAction(this::handleAnnulerAction);

        // Effacer le message d'erreur/succès quand l'utilisateur commence à modifier les champs
        titreField.textProperty().addListener((obs, oldVal, newVal) -> messageLabel.setText(""));
        contenuArea.textProperty().addListener((obs, oldVal, newVal) -> messageLabel.setText(""));
        offreField.textProperty().addListener((obs, oldVal, newVal) -> messageLabel.setText(""));
    }

    private void loadReclamationsNonTraitees() {
        try {
            // Récupérer toutes les réclamations
            List<Reclamation> allReclamations = reclamationService.getAll();

            // Filtrer pour ne garder que celles qui ne sont pas traitées (statut différent de "Traité")
            List<Reclamation> reclamationsNonTraitees = allReclamations.stream()
                    .filter(r -> !"Traité".equals(r.getStatut()))
                    .collect(Collectors.toList());

            // Mettre à jour le ComboBox
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
                // Récupérer la réclamation sélectionnée
                Reclamation selectedReclamation = reclamationComboBox.getValue();

                // Créer une nouvelle réponse
                Reponse reponse = new Reponse();
                reponse.setTitre(titreField.getText().trim());
                reponse.setContenu(contenuArea.getText().trim());
                reponse.setOffre(offreField.getText().trim());
                reponse.setReponsedate(new Date()); // Date actuelle
                reponse.setReclamation(selectedReclamation);

                // Ajouter la réponse à la base de données
                reponseService.add(reponse);

                // Mettre à jour le statut de la réclamation à "Traité"
                selectedReclamation.setStatut("Traité");
                selectedReclamation.setReponse(reponse);
                reclamationService.update(selectedReclamation);

                // Afficher message de succès
                messageLabel.setText("Réponse ajoutée avec succès");
                messageLabel.getStyleClass().setAll("success-message");

                // Réinitialiser les champs
                clearFields();

                // Recharger les réclamations non traitées
                loadReclamationsNonTraitees();

            } catch (SQLException e) {
                System.err.println("Erreur lors de l'ajout de la réponse: " + e.getMessage());
                messageLabel.setText("Erreur lors de l'ajout de la réponse");
                messageLabel.getStyleClass().setAll("error-message");
            }
        }
    }

    @FXML
    private void handleAnnulerAction(ActionEvent event) {
        // Fermer la fenêtre
        Stage stage = (Stage) annulerBtn.getScene().getWindow();
        stage.close();
    }

    private boolean validateInputs() {
        // Vérifier que tous les champs sont remplis
        if (reclamationComboBox.getValue() == null) {
            messageLabel.setText("Veuillez sélectionner une réclamation");
            messageLabel.getStyleClass().setAll("error-message");
            return false;
        }

        if (titreField.getText().trim().isEmpty()) {
            messageLabel.setText("Le titre ne peut pas être vide");
            messageLabel.getStyleClass().setAll("error-message");
            return false;
        }

        if (contenuArea.getText().trim().isEmpty()) {
            messageLabel.setText("Le contenu ne peut pas être vide");
            messageLabel.getStyleClass().setAll("error-message");
            return false;
        }

        return true;
    }

    private void clearFields() {
        titreField.clear();
        contenuArea.clear();
        offreField.clear();
        reclamationComboBox.getSelectionModel().clearSelection();
    }
}