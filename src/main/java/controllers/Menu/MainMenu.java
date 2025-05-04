package Controllers.Menu;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;

public class MainMenu {

    @FXML
    private Button gestionReclamationsButton;

    @FXML
    private Button gestionUtilisateursButton;

    @FXML
    private HBox reclamationsMenuBox;

    private boolean isReclamationMenuVisible = false;

    @FXML
    public void initialize() {
        // Initialement, cacher le menu des réclamations et réponses
        reclamationsMenuBox.setVisible(false);
    }

    @FXML
    public void handleGestionReclamations() {
        // Basculer la visibilité du menu des réclamations et réponses
        isReclamationMenuVisible = !isReclamationMenuVisible;
        reclamationsMenuBox.setVisible(isReclamationMenuVisible);

        // Animer le bouton
        if (isReclamationMenuVisible) {
            gestionReclamationsButton.setStyle(
                    "-fx-min-width: 250px; -fx-min-height: 60px; -fx-font-size: 18; " +
                            "-fx-background-color: #45a049; -fx-text-fill: white; " +
                            "-fx-background-radius: 5; -fx-cursor: hand;"
            );
        } else {
            gestionReclamationsButton.setStyle(
                    "-fx-min-width: 250px; -fx-min-height: 60px; -fx-font-size: 18; " +
                            "-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                            "-fx-background-radius: 5; -fx-cursor: hand;"
            );
        }
    }

    @FXML
    public void handleGestionUtilisateurs() {
        loadScene("/fxml/GestionUtilisateurs.fxml", "Gestion des Utilisateurs");
    }

    @FXML
    public void openReclamationList() {
        loadScene("/Reclamation/ReclamationList.fxml", "Liste des Réclamations");
    }

    @FXML
    public void openReponseList() {
        loadScene("/Reponse/ReponseList.fxml", "Liste des Réponses");
    }

    private void loadScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) gestionReclamationsButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger la vue : " + fxmlPath);
        }
    }

    private void showError(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}