package Controllers;

import Models.ContenuMultiMedia;
import Services.ContenuMultiMediaService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class EncyclopediaReaderController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private TextArea contentText;
    @FXML private ImageView contentImage;

    private ContenuMultiMediaService service = new ContenuMultiMediaService();
    private List<ContenuMultiMedia> allContents;
    private int currentIndex = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            allContents = service.getAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setContent(ContenuMultiMedia content) {
        titleLabel.setText(content.getTitre_media());
        categoryLabel.setText(content.getCategorie_media());
        contentText.setText(content.getText_media());

        if (content.getPhoto_media() != null && !content.getPhoto_media().isEmpty()) {
            contentImage.setImage(new Image(content.getPhoto_media()));
        }

        currentIndex = allContents.indexOf(content);
    }

    @FXML
    private void handlePrevious() {
        if (currentIndex > 0) {
            currentIndex--;
            setContent(allContents.get(currentIndex));
        }
    }

    @FXML
    private void handleNext() {
        if (currentIndex < allContents.size() - 1) {
            currentIndex++;
            setContent(allContents.get(currentIndex));
        }
    }

    @FXML
    private void handleBack() {
        ((Stage) titleLabel.getScene().getWindow()).close();
    }
}