package controllers.encyclopedie;

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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EncyclopediaListController implements Initializable {

    @FXML
    private FlowPane contentPane;

    private final ContenuMultiMediaService service = new ContenuMultiMediaService();
    private final String API_KEY = System.getenv("AIzaSyAuPH2fryde6FFW7pgrT8yfi29ZyelLaFc"); // Clé API sécurisée via variable d'environnement
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private static final int MAX_TEXT_LENGTH = 1500; // Ajusté pour correspondre à varchar(255) dans la base de données
    private static final String DEFAULT_IMAGE_URL = "https://via.placeholder.com/150";
    private static final Logger LOGGER = Logger.getLogger(EncyclopediaListController.class.getName());

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            loadContents();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur d'initialisation", e);
            showError("Erreur d'initialisation", "Une erreur est survenue lors du chargement initial.");
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
        imageView.setFitWidth(230);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        // Chargement asynchrone de l'image
        new Thread(() -> {
            try {
                Image image;
                if (content.getPhoto_media() != null && !content.getPhoto_media().isEmpty()) {
                    image = new Image(content.getPhoto_media(), true);
                } else {
                    URL defaultImageUrl = getClass().getResource("/images/default.png");
                    image = (defaultImageUrl != null) ? new Image(defaultImageUrl.toString(), true)
                            : new Image(DEFAULT_IMAGE_URL, true);
                }
                imageView.setImage(image);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erreur de chargement d'image", e);
                imageView.setStyle("-fx-background-color: #d3d3d3;");
            }
        }).start();

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
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ouverture du lecteur", e);
            showError("Erreur", "Impossible d'ouvrir le lecteur.");
        }
    }

    @FXML
    private void handleBack() {
        ((Stage) contentPane.getScene().getWindow()).close();
    }

    @FXML
    private void handleGenerateContent() {
        try {
            if (API_KEY == null || API_KEY.isEmpty()) {
                throw new IllegalStateException("Clé API non définie. Configurez GOOGLE_AI_API_KEY.");
            }
            String generatedText = generateContentWithAI("Génère un article sur l'un de ces sujets suivants (Film, Documentaire, Série, Sport, Expérience sociale, Expérience culturelle) en français.");
            if (generatedText.length() > MAX_TEXT_LENGTH) {
                generatedText = generatedText.substring(0, MAX_TEXT_LENGTH);
            }
            ContenuMultiMedia newContent = new ContenuMultiMedia();
            newContent.setTitre_media("Article généré par IA");
            newContent.setText_media(generatedText);
            newContent.setCategorie_media("Culture");
            newContent.setDate_media(new Date());
            newContent.setPhoto_media(DEFAULT_IMAGE_URL);
            service.add(newContent);
            loadContents();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de l'ajout", e);
            showError("Erreur SQL", "Échec de l'ajout à la base de données.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur de génération de contenu", e);
            showError("Erreur de génération", e.getMessage());
        }
    }

    private String generateContentWithAI(String prompt) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_URL + "?key=" + API_KEY);
            httpPost.setHeader("Content-Type", "application/json");

            JSONObject json = new JSONObject();
            JSONObject contents = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", prompt);
            parts.put(part);
            contents.put("parts", parts);
            json.put("contents", new JSONArray().put(contents));

            httpPost.setEntity(new StringEntity(json.toString(), "UTF-8"));
            try (CloseableHttpResponse response = client.execute(httpPost)) {
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject responseJson = new JSONObject(result);

                if (responseJson.has("error")) {
                    throw new Exception("Erreur API: " + responseJson.getJSONObject("error").getString("message"));
                }

                JSONArray candidates = responseJson.getJSONArray("candidates");
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject content = candidate.getJSONObject("content");
                JSONArray contentParts = content.getJSONArray("parts");
                return contentParts.getJSONObject(0).getString("text");
            }
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}