package controllers.encyclopedie;

import Models.ContenuMultiMedia;
import Services.ContenuMultiMediaService;
import Utils.PDFGenerator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class EncyclopediaReaderController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private TextArea contentText;
    @FXML private ImageView contentImage;
    @FXML
    private Button btnExport;
    private ContenuMultiMediaService service = new ContenuMultiMediaService();
    private List<ContenuMultiMedia> allContents;
    private int currentIndex = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            allContents = service.getAll();
            if (!allContents.isEmpty()) {
                setContent(allContents.get(0)); // Charge le premier élément par défaut
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les contenus.");
        }
    }

    public void setContent(ContenuMultiMedia content) {
        titleLabel.setText(content.getTitre_media());
        categoryLabel.setText(content.getCategorie_media());
        contentText.setText(content.getText_media());
        currentIndex = allContents.indexOf(content);
        if (currentIndex == -1) {
            showAlert("Erreur", "Contenu introuvable.");
            return;
        }
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
    @FXML
    private void handleExportPDF() {
        if (allContents == null || allContents.isEmpty()) {
            showAlert("Erreur", "Aucun contenu à exporter.");
            return;
        }
        // Vérification 2 : L'index est-il valide ?
        if (currentIndex < 0 || currentIndex >= allContents.size()) {
            showAlert("Erreur", "Sélection invalide.");
            return;
        }
        // Procéder à l'export
        ContenuMultiMedia currentContent = allContents.get(currentIndex);
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = fileChooser.showSaveDialog(btnExport.getScene().getWindow());

        if (file != null) {
            try {
                PDFGenerator.generateContentPDF(currentContent, file.getAbsolutePath());
                showAlert("Succès", "PDF exporté avec succès !");
            } catch (IOException e) {
                showAlert("Erreur", "Échec de l'export : " + e.getMessage());
            }
        }
    }
    private void showAlert(String title, String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }
}