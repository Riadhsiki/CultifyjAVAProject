package controllers.encyclopedie;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MainController {
    @FXML private StackPane contentPane;

    @FXML
    private void loadFrontView() {
        loadView("/FrontView.fxml");
    }

    @FXML
    private void loadBackOfficeView() {
        loadView("/BackOfficeView.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}