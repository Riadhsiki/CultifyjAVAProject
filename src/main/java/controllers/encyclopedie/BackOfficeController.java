package controllers.encyclopedie;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BackOfficeController {
    @FXML
    private void openContenuView() { loadCrudView("/ContenuMultiMediaView.fxml", "Gestion Contenus"); }

    @FXML
    private void openQuizView() { loadCrudView("/QuizView.fxml", "Gestion Quiz"); }

    @FXML
    private void openQuestionView() { loadCrudView("/QuestionView.fxml", "Gestion Questions"); }

    private void loadCrudView(String fxmlPath, String title) {
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