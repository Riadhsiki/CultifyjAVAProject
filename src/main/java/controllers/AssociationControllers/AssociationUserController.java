package controllers.associationcontrollers;

import controllers.DonControllers.FaireDon;
import models.Association;
import models.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import services.associationDon.AssociationServices;
import services.user.UserService;
import utils.PaginationUtils;
import utils.SessionManager;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @FXML
    private Label montantAPayerLabel;

    private final AssociationServices associationService = new AssociationServices();
    private final UserService userService = new UserService();
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

            // Load associations and filter out those with progress >= 100%
            allAssociations = associationService.getAll().stream()
                    .filter(assoc -> assoc.getPourcentageProgression() < 100)
                    .collect(Collectors.toList());
            paginationUtils = new PaginationUtils<>(allAssociations, 10);

            // Configure pagination control
            updatePaginationControl();

            // Load first page
            loadCurrentPage();

            // Setup listeners
            setupEventListeners();

            // Handle montantAPayer for the current user
            handleMontantAPayer();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des associations: " + e.getMessage());
        }
    }

    private void initSearchCriteria() {
        searchCriteriaMap.put("Nom", "nom");
        searchCriteriaMap.put("Description", "description");
        searchCriteriaMap.put("But", "but");
        searchCriteriaMap.put("Contact", "contact");
        searchCriteriaMap.put("Site Web", "siteWeb");
        searchCriteriaMap.put("Montant", "montant");
        searchCriteriaMap.put("Tous les champs", "tous");

        searchCriteriaComboBox.setItems(FXCollections.observableArrayList(searchCriteriaMap.keySet()));
        searchCriteriaComboBox.setValue("Nom"); // Valeur par défaut
    }

    private void setupEventListeners() {
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            paginationUtils.goToPage(newIndex.intValue() + 1);
            loadCurrentPage();
        });

        itemsPerPageComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                paginationUtils.setPageSize(newVal);
                updatePaginationControl();
                loadCurrentPage();
            }
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                performSearch(newValue);
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage());
            }
        });

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
            allAssociations = associationService.getAll().stream()
                    .filter(assoc -> assoc.getPourcentageProgression() < 100)
                    .collect(Collectors.toList());
        } else {
            allAssociations = associationService.searchAssociations(searchText, selectedCriteria).stream()
                    .filter(assoc -> assoc.getPourcentageProgression() < 100)
                    .collect(Collectors.toList());
        }

        paginationUtils.updateFullList(allAssociations);
        updatePaginationControl();
        loadCurrentPage();
    }

    private void updatePaginationControl() {
        int totalPages = paginationUtils.getTotalPages();
        pagination.setPageCount(totalPages > 0 ? totalPages : 1);
        pagination.setCurrentPageIndex(paginationUtils.getCurrentPage() - 1);

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
            pageInfoLabel.setText(String.format("Affichage %d à %d sur %d associations", startItem, endItem, totalItems));
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

        Label butLabel = new Label("But:");
        butLabel.getStyleClass().add("card-description");
        butLabel.setStyle("-fx-font-weight: bold;");

        Label butText = new Label(association.getBut());
        butText.getStyleClass().add("card-description");
        butText.setWrapText(true);

        Label contactLabel = new Label("Contact:");
        contactLabel.getStyleClass().add("card-description");
        contactLabel.setStyle("-fx-font-weight: bold;");

        Label contactText = new Label(association.getContact());
        contactText.getStyleClass().add("card-description");
        contactText.setWrapText(true);

        Label montant = new Label("Objectif: " + String.format("%.2f TND", association.getMontantDesire()));
        montant.getStyleClass().add("card-description");
        montant.setWrapText(true);

        scrollContent.getChildren().addAll(descriptionLabel, descriptionText, butLabel, butText, contactLabel, contactText);

        ScrollPane scrollPane = new ScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(120);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("card-content-scroll");

        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(javafx.geometry.Pos.CENTER);

        HBox qrCodeContainer = new HBox();
        qrCodeContainer.getStyleClass().add("qr-container");
        qrCodeContainer.setAlignment(javafx.geometry.Pos.CENTER);

        if (association.getSiteWeb() != null && !association.getSiteWeb().isEmpty()) {
            Image qrCodeImage = QRCodeGenerator.generateQRCodeImage(association.getSiteWeb(), 100, 100);

            if (qrCodeImage != null) {
                ImageView qrCodeView = new ImageView(qrCodeImage);
                qrCodeView.setFitHeight(40);
                qrCodeView.setFitWidth(40);
                qrCodeView.getStyleClass().add("qr-code");

                qrCodeView.setOnMouseClicked(event -> {
                    try {
                        Desktop.getDesktop().browse(new URI(association.getSiteWeb()));
                        event.consume();
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                        showAlert("Erreur", "Impossible d'ouvrir le site web: " + e.getMessage());
                    }
                });

                qrCodeContainer.getChildren().add(qrCodeView);

                Label qrInfo = new Label("Visiter");
                qrInfo.getStyleClass().add("qr-info");
                qrCodeContainer.getChildren().add(qrInfo);
            }
        }

        bottomBox.getChildren().addAll(montant, qrCodeContainer);

        content.getChildren().addAll(title, scrollPane, bottomBox);

        double pourcentageProgression = association.getPourcentageProgression();

        StackPane progressContainer = new StackPane();
        progressContainer.getStyleClass().add("progress-container");

        ProgressBar progressBar = new ProgressBar(pourcentageProgression / 100);
        progressBar.setPrefWidth(250);
        progressBar.setPrefHeight(20);
        progressBar.getStyleClass().add("progress");

        if (pourcentageProgression <= 25) {
            progressBar.getStyleClass().add("progress-bar-danger");
        } else if (pourcentageProgression <= 50) {
            progressBar.getStyleClass().add("progress-bar-warning");
        } else if (pourcentageProgression <= 75) {
            progressBar.getStyleClass().add("progress-bar-info");
        } else {
            progressBar.getStyleClass().add("progress-bar-success");
        }

        Label progressLabel = new Label(String.format("%.1f%%", pourcentageProgression));
        progressLabel.getStyleClass().add("progress-label");

        progressContainer.getChildren().addAll(progressBar, progressLabel);

        cardPane.getChildren().addAll(imageView, content);

        StackPane.setAlignment(progressContainer, javafx.geometry.Pos.BOTTOM_CENTER);
        cardPane.getChildren().add(progressContainer);

        cardPane.setOnMouseClicked(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Don/FaireDon.fxml"));
                Parent root = loader.load();

                FaireDon controller = loader.getController();
                controller.setAssociationId(association.getId());
                Scene currentScene = associationsGrid.getScene();
                Stage currentStage = (Stage) currentScene.getWindow();
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

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Association/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        alert.showAndWait();
    }

    private void handleMontantAPayer() {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn()) {
            montantAPayerLabel.setVisible(false);
            return;
        }

        String username = session.getCurrentUsername();
        if (username == null) {
            montantAPayerLabel.setVisible(false);
            return;
        }

        try {
            String role = userService.getRoleByUsername(username);
            if (role != null && "Organizer".equals(role)) {
                User currentUser = userService.getUserByUsername(username);
                if (currentUser != null) {
                    userService.calculateMontantAPayer(currentUser);
                    montantAPayerLabel.setText("Montant à payer: " + String.format("%.2f TND", currentUser.getMontantAPayer()));
                    montantAPayerLabel.setVisible(true);
                } else {
                    montantAPayerLabel.setVisible(false);
                }
            } else {
                montantAPayerLabel.setVisible(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du calcul du montant à payer: " + e.getMessage());
            montantAPayerLabel.setVisible(false);
        }
    }
}