package Controllers;

import Models.ContenuMultiMedia;
import Services.ContenuMultiMediaService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class EncyclopediaListController implements Initializable {

    @FXML
    private FlowPane contentPane; // Ajout de fx:id correspondant au FXML

    private final ContenuMultiMediaService service = new ContenuMultiMediaService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            loadContents();
        } catch (Exception e) {
            showError("Erreur d'initialisation", e.getMessage());
        }
    }

    private void loadContents() throws SQLException {
        contentPane.getChildren().clear();
        List<ContenuMultiMedia> contents = service.getAll();

        for (ContenuMultiMedia content : contents) {
            VBox card = createContentCard(content);
            contentPane.getChildren().add(card);
        }
    }

    private VBox createContentCard(ContenuMultiMedia content) {
        VBox card = new VBox(10);
        card.getStyleClass().add("content-card");
        card.setOnMouseClicked(e -> openReader(content));

        ImageView imageView = new ImageView();
        try {
            if (content.getPhoto_media() != null && !content.getPhoto_media().isEmpty()) {
                imageView.setImage(new Image(content.getPhoto_media()));
            }
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResource("/images/default.png").toString()));
        }

        imageView.setFitWidth(230);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        Text title = new Text(content.getTitre_media());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Text category = new Text(content.getCategorie_media());
        category.setStyle("-fx-font-style: italic; -fx-fill: #7f8c8d;");

        card.getChildren().addAll(imageView, title, category);
        return card;
    }

    private void openReader(ContenuMultiMedia content) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EncyclopediaReaderView.fxml"));
            Parent root = loader.load();

            EncyclopediaReaderController controller = loader.getController();
            controller.setContent(content);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(content.getTitre_media());
            stage.show();

        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir le lecteur: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        ((Stage) contentPane.getScene().getWindow()).close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}