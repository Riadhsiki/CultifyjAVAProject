package controllers.AssociationControllers;

import controllers.DonControllers.FaireDon;
import entities.Association;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import services.AssociationServices;
import utils.PaginationUtils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javafx.scene.control.ProgressBar;

public class AssociationUserController {

    @FXML
    private GridPane associationsGrid;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> searchCriteriaComboBox;

    @FXML
    private ComboBox<Integer> itemsPerPageComboBox;

    @FXML
    private Pagination pagination;

    @FXML
    private Label pageInfoLabel;

    private final AssociationServices associationService = new AssociationServices();
    private static final String IMAGE_DIR = "http://127.0.0.1/cultify/public/uploads/images/";
    private PaginationUtils<Association> paginationUtils;
    private List<Association> allAssociations;

    // Map pour stocker les critères de recherche
    private final Map<String, String> searchCriteriaMap = new HashMap<>();

    @FXML
    public void initialize() {
        try {
            // Initialiser les critères de recherche
            initSearchCriteria();

            // Initialize pagination combo box with options
            itemsPerPageComboBox.getItems().addAll(5, 10, 15, 20, 25);
            itemsPerPageComboBox.setValue(10); // Default value

            // Load associations and set up pagination
            allAssociations = associationService.getAll();
            paginationUtils = new PaginationUtils<>(allAssociations, 10);

            // Configure pagination control
            updatePaginationControl();

            // Load first page
            loadCurrentPage();

            // Setup listeners
            setupEventListeners();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des associations: " + e.getMessage());
        }
    }

    private void initSearchCriteria() {
        // Initialiser la Map des critères
        searchCriteriaMap.put("Nom", "nom");
        searchCriteriaMap.put("Description", "description");
        searchCriteriaMap.put("But", "but");
        searchCriteriaMap.put("Contact", "contact");
        searchCriteriaMap.put("Site Web", "siteWeb");
        searchCriteriaMap.put("Montant", "montant");
        searchCriteriaMap.put("Tous les champs", "tous");

        // Remplir le ComboBox avec les clés de la Map
        searchCriteriaComboBox.setItems(FXCollections.observableArrayList(searchCriteriaMap.keySet()));
        searchCriteriaComboBox.setValue("Nom"); // Valeur par défaut
    }

    private void setupEventListeners() {
        // Pagination change listener
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            paginationUtils.goToPage(newIndex.intValue() + 1); // +1 because JavaFX pagination is 0-based
            loadCurrentPage();
        });

        // Items per page change listener
        itemsPerPageComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                paginationUtils.setPageSize(newVal);
                updatePaginationControl();
                loadCurrentPage();
            }
        });

        // Search field listener with debounce
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                performSearch(newValue);
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage());
            }
        });

        // Search criteria change listener
        searchCriteriaComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                    performSearch(searchField.getText());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage());
            }
        });
    }

    private void performSearch(String searchText) throws SQLException {
        String selectedCriteria = searchCriteriaMap.get(searchCriteriaComboBox.getValue());

        if (searchText == null || searchText.isEmpty()) {
            allAssociations = associationService.getAll();
        } else {
            allAssociations = associationService.searchAssociations(searchText, selectedCriteria);
        }

        paginationUtils.updateFullList(allAssociations);
        updatePaginationControl();
        loadCurrentPage();
    }

    private void updatePaginationControl() {
        int totalPages = paginationUtils.getTotalPages();
        pagination.setPageCount(totalPages > 0 ? totalPages : 1);
        pagination.setCurrentPageIndex(paginationUtils.getCurrentPage() - 1); // -1 because JavaFX pagination is 0-based

        updatePageInfoLabel();
    }

    private void updatePageInfoLabel() {
        int currentPage = paginationUtils.getCurrentPage();
        int totalPages = paginationUtils.getTotalPages();
        int totalItems = paginationUtils.getTotalItems();
        int pageSize = paginationUtils.getPageSize();

        int startItem = (currentPage - 1) * pageSize + 1;
        int endItem = Math.min(startItem + pageSize - 1, totalItems);

        if (totalItems == 0) {
            pageInfoLabel.setText("Aucun résultat");
        } else {
            pageInfoLabel.setText(String.format("Affichage %d à %d sur %d associations",
                    startItem, endItem, totalItems));
        }
    }

    private void loadCurrentPage() {
        List<Association> pageAssociations = paginationUtils.getCurrentPageItems();
        updateGridWithAssociations(pageAssociations);
        updatePageInfoLabel();
    }

    private void updateGridWithAssociations(List<Association> associations) {
        int column = 0;
        int row = 0;
        int cardsPerRow = 3;

        associationsGrid.getChildren().clear();

        associationsGrid.getColumnConstraints().clear();
        for (int i = 0; i < cardsPerRow; i++) {
            ColumnConstraints colConstraint = new ColumnConstraints();
            colConstraint.setPercentWidth(100.0 / cardsPerRow);
            colConstraint.setHalignment(javafx.geometry.HPos.CENTER);
            associationsGrid.getColumnConstraints().add(colConstraint);
        }

        for (Association association : associations) {
            StackPane card = createAssociationCard(association);
            associationsGrid.add(card, column, row);

            column++;
            if (column >= cardsPerRow) {
                column = 0;
                row++;
            }
        }
    }

    private StackPane createAssociationCard(Association association) {
        StackPane cardPane = new StackPane();
        cardPane.getStyleClass().add("card-pane");
        cardPane.setMinSize(250, 180);
        cardPane.setMaxSize(250, 180);

        // Image
        ImageView imageView = new ImageView();
        String imagePath = association.getImage() != null && !association.getImage().isEmpty()
                ? IMAGE_DIR + association.getImage()
                : "http://127.0.0.1/cultify/public/uploads/images/";

        try {
            Image image = new Image(imagePath);
            imageView.setImage(image);
            imageView.setFitWidth(250);
            imageView.setFitHeight(180);
            imageView.setPreserveRatio(false);
        } catch (Exception e) {
            Image defaultImage = new Image("file:C:/Users/adamo/cultify/public/image/back-logo");
            imageView.setImage(defaultImage);
        }
        imageView.getStyleClass().add("card-image");

        VBox content = new VBox(5);
        content.getStyleClass().add("card-content");
        content.setMaxWidth(230);
        content.setMaxHeight(160);

        Label title = new Label(association.getNom().toUpperCase());
        title.getStyleClass().add("card-title");
        title.setWrapText(true);


        VBox scrollContent = new VBox(5);
        scrollContent.setFillWidth(true);

        Label descriptionLabel = new Label("Description:");
        descriptionLabel.getStyleClass().add("card-description");
        descriptionLabel.setStyle("-fx-font-weight: bold;");

        Label descriptionText = new Label(association.getDescription());
        descriptionText.getStyleClass().add("card-description");
        descriptionText.setWrapText(true);

        // But avec texte complet
        Label butLabel = new Label("But:");
        butLabel.getStyleClass().add("card-description");
        butLabel.setStyle("-fx-font-weight: bold;");

        Label butText = new Label(association.getBut());
        butText.getStyleClass().add("card-description");
        butText.setWrapText(true);

        // Contact avec texte complet
        Label contactLabel = new Label("Contact:");
        contactLabel.getStyleClass().add("card-description");
        contactLabel.setStyle("-fx-font-weight: bold;");

        Label contactText = new Label(association.getContact());
        contactText.getStyleClass().add("card-description");
        contactText.setWrapText(true);

        Label montant = new Label("Objectif: " + String.format("%.2f TND", association.getMontantDesire()));
        montant.getStyleClass().add("card-description");
        montant.setWrapText(true);

        // Ajouter tous les labels au contenu scrollable
        scrollContent.getChildren().addAll(
                descriptionLabel, descriptionText,
                butLabel, butText,
                contactLabel, contactText
        );

        // Créer ScrollPane pour le contenu scrollable
        ScrollPane scrollPane = new ScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(120);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("card-content-scroll");

        // Use HBox for montant and QR code
        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(javafx.geometry.Pos.CENTER);

        // Ajout du code QR
        HBox qrCodeContainer = new HBox();
        qrCodeContainer.getStyleClass().add("qr-container");
        qrCodeContainer.setAlignment(javafx.geometry.Pos.CENTER);

        if (association.getSiteWeb() != null && !association.getSiteWeb().isEmpty()) {
            // Générer le code QR avec la taille appropriée
            Image qrCodeImage = QRCodeGenerator.generateQRCodeImage(association.getSiteWeb(), 100, 100);

            if (qrCodeImage != null) {
                ImageView qrCodeView = new ImageView(qrCodeImage);
                qrCodeView.setFitHeight(40);
                qrCodeView.setFitWidth(40);
                qrCodeView.getStyleClass().add("qr-code");

                // Gérer le clic sur le code QR
                qrCodeView.setOnMouseClicked(event -> {
                    try {
                        Desktop.getDesktop().browse(new URI(association.getSiteWeb()));
                        event.consume(); // Pour éviter que l'événement se propage au cardPane
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                        showAlert("Erreur", "Impossible d'ouvrir le site web: " + e.getMessage());
                    }
                });

                qrCodeContainer.getChildren().add(qrCodeView);

                // Ajouter une petite info sur le code QR
                Label qrInfo = new Label("Visiter");
                qrInfo.getStyleClass().add("qr-info");
                qrCodeContainer.getChildren().add(qrInfo);
            }
        }

        bottomBox.getChildren().addAll(montant, qrCodeContainer);

        // Ajouter les éléments dans l'ordre souhaité
        content.getChildren().addAll(title, scrollPane, bottomBox);

        // Calculer le pourcentage de progression
        double pourcentageProgression = association.getPourcentageProgression();

        // Créer la barre de progression qui sera au fond absolu
        StackPane progressContainer = new StackPane();
        progressContainer.getStyleClass().add("progress-container");

        // Créer la barre de progression
        ProgressBar progressBar = new ProgressBar(pourcentageProgression / 100);
        progressBar.setPrefWidth(250);
        progressBar.setPrefHeight(20);
        progressBar.getStyleClass().add("progress");

        // Ajouter une classe CSS basée sur le pourcentage
        if (pourcentageProgression <= 25) {
            progressBar.getStyleClass().add("progress-bar-danger");
        } else if (pourcentageProgression <= 50) {
            progressBar.getStyleClass().add("progress-bar-warning");
        } else if (pourcentageProgression <= 75) {
            progressBar.getStyleClass().add("progress-bar-info");
        } else {
            progressBar.getStyleClass().add("progress-bar-success");
        }

        // Créer le label pour le pourcentage et le mettre sur la barre de progression
        Label progressLabel = new Label(String.format("%.1f%%", pourcentageProgression));
        progressLabel.getStyleClass().add("progress-label");

        // Ajouter la barre et le label au conteneur
        progressContainer.getChildren().addAll(progressBar, progressLabel);

        // Ajouter les éléments principaux à la carte
        cardPane.getChildren().addAll(imageView, content);

        // Ajouter la barre de progression en dernier pour qu'elle soit au-dessus
        // et la positionner explicitement en bas
        StackPane.setAlignment(progressContainer, javafx.geometry.Pos.BOTTOM_CENTER);
        cardPane.getChildren().add(progressContainer);

        // Gestion du clic sur la carte (pour faire un don)
        cardPane.setOnMouseClicked(event -> {
            try {
                // Charger la vue FaireDon.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Don/FaireDon.fxml"));
                Parent root = loader.load();

                // Passer l'association au contrôleur FaireDon
                FaireDon controller = loader.getController();
                controller.setAssociationId(association.getId());
                Scene currentScene = associationsGrid.getScene();
                Stage currentStage = (Stage) currentScene.getWindow();
                // Créer une nouvelle scène
                currentStage.setScene(new Scene(root));
                currentStage.setTitle("Faire un don à " + association.getNom());

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible d'ouvrir la page de don");
            }
        });

        return cardPane;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);

        // Appliquer le style CSS
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Association/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        alert.showAndWait();
    }
}