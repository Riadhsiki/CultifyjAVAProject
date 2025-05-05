package Controllers;

import Models.ContenuMultiMedia;
import Services.ContenuMultiMediaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.util.stream.Collectors;

public class EncyclopediaListController implements Initializable {

    @FXML private FlowPane contentPane;
    @FXML private TextField titleSearchField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField contentSearchField;
    @FXML private Button searchButton;
    @FXML private Button clearSearchButton;

    private final ContenuMultiMediaService service = new ContenuMultiMediaService();
    private final String API_KEY = "AIzaSyAuPH2fryde6FFW7pgrT8yfi29ZyelLaFc";
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private static final int MAX_TEXT_LENGTH = 1500;
    private ObservableList<ContenuMultiMedia> allContents = FXCollections.observableArrayList();
    private ObservableList<String> categories = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            loadContents();
            setupSearchControls();
        } catch (Exception e) {
            showError("Erreur d'initialisation", e.getMessage());
        }
    }

    private void loadContents() throws SQLException {
        allContents.clear();
        contentPane.getChildren().clear();
        List<ContenuMultiMedia> contents = service.getAll();
        allContents.setAll(contents);

        // Populate categories
        categories.setAll(contents.stream()
                .map(ContenuMultiMedia::getCategorie_media)
                .filter(category -> category != null && !category.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList()));
        categoryComboBox.setItems(categories);

        // Display all contents
        for (ContenuMultiMedia content : contents) {
            VBox card = createContentCard(content);
            contentPane.getChildren().add(card);
        }
    }

    private void setupSearchControls() {
        searchButton.setOnAction(event -> performSearch());
        clearSearchButton.setOnAction(event -> clearSearch());
        // Enable search button only if at least one search field is non-empty
        titleSearchField.textProperty().addListener((obs, oldValue, newValue) -> updateSearchButtonState());
        contentSearchField.textProperty().addListener((obs, oldValue, newValue) -> updateSearchButtonState());
        categoryComboBox.valueProperty().addListener((obs, oldValue, newValue) -> updateSearchButtonState());
        updateSearchButtonState();
    }

    @FXML
    private void performSearch() {
        String titleQuery = titleSearchField.getText().trim().toLowerCase();
        String categoryQuery = categoryComboBox.getValue();
        String contentQuery = contentSearchField.getText().trim().toLowerCase();

        List<ContenuMultiMedia> filteredContents = allContents.stream()
                .filter(content -> {
                    boolean matchesTitle = titleQuery.isEmpty() || content.getTitre_media().toLowerCase().contains(titleQuery);
                    boolean matchesCategory = categoryQuery == null || content.getCategorie_media().equals(categoryQuery);
                    boolean matchesContent = contentQuery.isEmpty() ||
                            (content.getText_media() != null && content.getText_media().toLowerCase().contains(contentQuery));
                    return matchesTitle && matchesCategory && matchesContent;
                })
                .collect(Collectors.toList());

        contentPane.getChildren().clear();
        for (ContenuMultiMedia content : filteredContents) {
            VBox card = createContentCard(content);
            contentPane.getChildren().add(card);
        }
    }

    @FXML
    private void clearSearch() {
        titleSearchField.clear();
        categoryComboBox.setValue(null);
        contentSearchField.clear();
        contentPane.getChildren().clear();
        for (ContenuMultiMedia content : allContents) {
            VBox card = createContentCard(content);
            contentPane.getChildren().add(card);
        }
    }

    private void updateSearchButtonState() {
        boolean isSearchFieldEmpty = titleSearchField.getText().trim().isEmpty() &&
                categoryComboBox.getValue() == null &&
                contentSearchField.getText().trim().isEmpty();
        searchButton.setDisable(isSearchFieldEmpty);
    }

    private VBox createContentCard(ContenuMultiMedia content) {
        VBox card = new VBox(10);
        card.getStyleClass().add("content-card");
        card.setOnMouseClicked(e -> openReader(content));

        ImageView imageView = new ImageView();
        try {
            if (content.getPhoto_media() != null && !content.getPhoto_media().isEmpty()) {
                imageView.setImage(new Image(content.getPhoto_media()));
            } else {
                URL defaultImageUrl = getClass().getResource("/images/default.png");
                if (defaultImageUrl != null) {
                    imageView.setImage(new Image(defaultImageUrl.toString()));
                } else {
                    imageView.setStyle("-fx-background-color: #d3d3d3;");
                }
            }
        } catch (Exception e) {
            showError("Erreur de chargement d'image", "Impossible de charger l'image: " + e.getMessage());
            imageView.setStyle("-fx-background-color: #d3d3d3;");
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

    @FXML
    private void handleGenerateContent() {
        try {
            String generatedText = generateContentWithAI("Génère un article sur une expérience culturelle française en français.");
            if (generatedText.length() > MAX_TEXT_LENGTH) {
                generatedText = generatedText.substring(0, MAX_TEXT_LENGTH);
            }
            ContenuMultiMedia newContent = new ContenuMultiMedia();
            newContent.setTitre_media("Article généré par IA");
            newContent.setText_media(generatedText);
            newContent.setCategorie_media("Culture");
            newContent.setDate_media(new Date());
            URL defaultImageUrl = getClass().getResource("/images/default.png");
            newContent.setPhoto_media(defaultImageUrl != null ? defaultImageUrl.toString() : "");
            service.add(newContent);
            loadContents();
        } catch (SQLException e) {
            showError("Erreur SQL", "Échec de l'ajout à la base de données: " + e.getMessage());
        } catch (Exception e) {
            showError("Erreur de génération", "Échec de la génération de contenu: " + e.getMessage());
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

            httpPost.setEntity(new StringEntity(json.toString()));
            try (CloseableHttpResponse response = client.execute(httpPost)) {
                String result = EntityUtils.toString(response.getEntity());
                JSONObject responseJson = new JSONObject(result);

                if (responseJson.has("error")) {
                    JSONObject error = responseJson.getJSONObject("error");
                    String errorMessage = error.getString("message");
                    throw new Exception("Erreur API: " + errorMessage);
                }

                if (responseJson.has("candidates")) {
                    JSONArray candidates = responseJson.getJSONArray("candidates");
                    if (candidates.length() > 0) {
                        JSONObject candidate = candidates.getJSONObject(0);
                        if (candidate.has("content")) {
                            JSONObject content = candidate.getJSONObject("content");
                            if (content.has("parts")) {
                                JSONArray contentParts = content.getJSONArray("parts");
                                if (contentParts.length() > 0 && contentParts.getJSONObject(0).has("text")) {
                                    return contentParts.getJSONObject(0).getString("text");
                                }
                            }
                        }
                    }
                }
                throw new Exception("Structure de réponse inattendue: " + result);
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}