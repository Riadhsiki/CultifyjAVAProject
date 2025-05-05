package Controllers;

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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.IOException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.util.HashMap;
import java.util.Map;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class EncyclopediaReaderController implements Initializable {
    @FXML
    private ImageView qrCodeImage;
    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private TextArea contentText;
    @FXML private ImageView contentImage;
    @FXML
    private Button btnUpload;
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
    @FXML
    private void handleExportPDF() {
        ContenuMultiMedia currentContent = allContents.get(currentIndex);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        File file = fileChooser.showSaveDialog(btnUpload.getScene().getWindow());

        if (file != null) {
            try {
                PDFGenerator.generateContentPDF(currentContent, file.getAbsolutePath());
                showAlert("Succès", "PDF généré avec succès !");
            } catch (IOException e) {
                showAlert("Erreur", "Échec de génération du PDF : " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }
    @FXML
    private void handleGenerateQR() {
        try {
            ContenuMultiMedia content = allContents.get(currentIndex);
            File qrFile = generateQRCode(content);
            Image qrImage = new Image(qrFile.toURI().toString());
            qrCodeImage.setImage(qrImage);

            // Optionnel : Afficher le chemin du fichier
            showAlert("QR Code généré", "Fichier : " + qrFile.getAbsolutePath());
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private File generateQRCode(ContenuMultiMedia content) throws WriterException, IOException {
        // Structurer les données de manière standard
        String qrData = String.format(
                "CULTIFY_CONTENT\n" +
                        "TITLE:%s\n" +
                        "CATEGORY:%s\n" +
                        "DATE:%s\n" +
                        "CHECKSUM:%d",
                content.getTitre_media(),
                content.getCategorie_media(),
                new SimpleDateFormat("yyyy-MM-dd").format(content.getDate_media()),
                content.hashCode() // Checksum simple
        );

        QRCodeWriter qrWriter = new QRCodeWriter();

        // Configuration avancée
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
        hints.put(EncodeHintType.MARGIN, 4);

        BitMatrix matrix = qrWriter.encode(
                qrData,
                BarcodeFormat.QR_CODE,
                600, // Taille augmentée
                600,
                hints
        );

        // Sauvegarder dans le dossier temporaire système
        File qrFile = File.createTempFile("cultify_", ".qr.png");
        MatrixToImageWriter.writeToPath(matrix, "PNG", qrFile.toPath());

        return qrFile;
    }
     }
