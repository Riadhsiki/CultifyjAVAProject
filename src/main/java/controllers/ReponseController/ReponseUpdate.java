package Controllers.ReponseController;

import Entities.Reponse;
import Services.ReponseService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.ResourceBundle;

public class ReponseUpdate implements Initializable {

    @FXML
    private Label reclamationLabel;

    @FXML
    private TextField titreField;

    @FXML
    private TextArea contenuArea;

    @FXML
    private TextField offreField;

    @FXML
    private Button modifierBtn;

    @FXML
    private Button annulerBtn;

    @FXML
    private Label messageLabel;

    private ReponseService reponseService;
    private Reponse reponseToUpdate;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reponseService = new ReponseService();

        // Configurer les événements des boutons
        modifierBtn.setOnAction(this::handleModifierAction);
        annulerBtn.setOnAction(this::handleAnnulerAction);

        // Effacer le message d'erreur/succès quand l'utilisateur commence à modifier les champs
        titreField.textProperty().addListener((obs, oldVal, newVal) -> messageLabel.setText(""));
        contenuArea.textProperty().addListener((obs, oldVal, newVal) -> messageLabel.setText(""));
        offreField.textProperty().addListener((obs, oldVal, newVal) -> messageLabel.setText(""));
    }

    public void initData(Reponse reponse) {
        this.reponseToUpdate = reponse;

        // Afficher les informations de la réclamation (non modifiable)
        if (reponse.getReclamation() != null) {
            reclamationLabel.setText("#" + reponse.getReclamation().getId_reclamation() +
                    " - " + reponse.getReclamation().getTitre());
        }

        // Remplir les champs avec les données actuelles
        titreField.setText(reponse.getTitre());
        contenuArea.setText(reponse.getContenu());
        offreField.setText(reponse.getOffre() != null ? reponse.getOffre() : "");
    }

    @FXML
    private void handleModifierAction(ActionEvent event) {
        if (validateInputs()) {
            try {
                // Mettre à jour l'objet réponse
                reponseToUpdate.setTitre(titreField.getText().trim());
                reponseToUpdate.setContenu(contenuArea.getText().trim());
                reponseToUpdate.setOffre(offreField.getText().trim());
                reponseToUpdate.setReponsedate(new Date()); // Mettre à jour la date de modification

                // Mettre à jour dans la base de données (sans toucher à l'ID de la réclamation)
                reponseService.update(reponseToUpdate);

                // Afficher message de succès
                messageLabel.setText("Réponse modifiée avec succès");
                messageLabel.setStyle("-fx-text-fill: green;");

                // Fermer la fenêtre après un court délai
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        javafx.application.Platform.runLater(() -> {
                            Stage stage = (Stage) modifierBtn.getScene().getWindow();
                            stage.close();
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();

            } catch (SQLException e) {
                System.err.println("Erreur lors de la modification de la réponse: " + e.getMessage());
                messageLabel.setText("Erreur lors de la modification de la réponse");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    @FXML
    private void handleAnnulerAction(ActionEvent event) {
        Stage stage = (Stage) annulerBtn.getScene().getWindow();
        stage.close();
    }

    private boolean validateInputs() {
        if (titreField.getText().trim().isEmpty()) {
            messageLabel.setText("Le titre ne peut pas être vide");
            messageLabel.setStyle("-fx-text-fill: red;");
            return false;
        }

        if (contenuArea.getText().trim().isEmpty()) {
            messageLabel.setText("Le contenu ne peut pas être vide");
            messageLabel.setStyle("-fx-text-fill: red;");
            return false;
        }

        return true;
    }
}