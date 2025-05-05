package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FrontController {
    @FXML
    private void openEncyclopediaListView() {
        loadView("/EncyclopediaListView.fxml", "Encyclopédie");
    }

    @FXML
    private void openQuiz() {
        loadView("/QuizFrontView.fxml", "Quiz Culturel");
    }

    private void loadView(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}