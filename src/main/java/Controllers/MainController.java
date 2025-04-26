package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainController {

    @FXML
    private void openContenuView() {
        loadView("/ContenuMultiMediaView.fxml", "Gestion des Contenus Multimédia"); // Sans "t" après "Conten"
    }

    @FXML
    private void openQuizView() {
        loadView("/QuizView.fxml", "Gestion des Quiz");
    }

    @FXML
    private void openQuestionView() {
        loadView("/QuestionView.fxml", "Gestion des Questions");
    }

    private void loadView(String fxmlPath, String title) {
        try {
            System.out.println("Tentative de chargement: " + fxmlPath);

            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                throw new IOException("Fichier FXML non trouvé: " + fxmlPath);
            }

            Parent root = FXMLLoader.load(url);
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            String errorDetails = """
                Erreur lors du chargement de la vue:
                Chemin: %s
                Raison: %s
                """.formatted(fxmlPath, e.getMessage());

            showError("Erreur de chargement", errorDetails);
            e.printStackTrace();
        }
    }
    @FXML
    private void openEncyclopediaListView() {
        loadView("/EncyclopediaListView.fxml", "Encyclopédie - Sélection");
    }

    @FXML
    private void openEncyclopediaReaderView() {
        loadView("/EncyclopediaReaderView.fxml", "Encyclopédie - Lecture");
    }
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}