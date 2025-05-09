package controllers.event;

import controllers.reservation.AjouterReservation;
import models.Event;
import services.eventreservation.EventService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class DetailEvent {
    @FXML private TableView<Event> tableView;
    @FXML private TableColumn<Event, String> titreCol;
    @FXML private TableColumn<Event, String> dateCol;
    @FXML private TableColumn<Event, String> organisationCol;
    @FXML private TableColumn<Event, Integer> capaciteCol;
    @FXML private TableColumn<Event, Integer> nbplacesCol;
    @FXML private TableColumn<Event, String> categorieCol;
    @FXML private TableColumn<Event, Float> prixCol;
    @FXML private TableColumn<Event, ImageView> imageCol;
    @FXML private TableColumn<Event, Void> actionCol;

    @FXML private Button btnShowStats;
    @FXML private VBox statsContainer;
    @FXML private PieChart categoryPieChart;
    @FXML private TextArea statsTextArea;

    @FXML private FlowPane cardsContainer;
    @FXML private StackPane detailsPane;
    @FXML private ImageView detailImageView;
    @FXML private Label detailTitle;
    @FXML private Label detailDescription;
    @FXML private Label detailDate;
    @FXML private Label detailOrganisation;
    @FXML private Label detailCapacite;
    @FXML private Label detailNbPlaces;
    @FXML private Label detailCategorie;
    @FXML private Label detailPrix;
    @FXML private Button reserveButton;

    @FXML private ImageView logoImageView;
    @FXML private TextField searchField;
    @FXML private TextField cardSearchField;

    private EventService eventService = new EventService();
    private boolean statsVisible = false;

    @FXML
    public void initialize() {
        System.out.println("DetailEvent: Starting initialize");
        loadLogo();
        System.out.println("DetailEvent: loadLogo completed");
        if (tableView != null) {
            System.out.println("DetailEvent: Configuring table");
            configureTable();
        }
        if (cardsContainer != null) {
            System.out.println("DetailEvent: Cards container present");
            // Cards configuration is handled in loadData
        }
        System.out.println("DetailEvent: Loading data");
        loadData();
        if (statsContainer != null && btnShowStats != null) {
            System.out.println("DetailEvent: Configuring stats section");
            configureStatsSection();
        }
        System.out.println("DetailEvent: Configuring details pane");
        configureDetailsPane();

        // Update statistics when table data changes
        if (tableView != null) {
            tableView.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends Event> c) -> {
                if (statsVisible) {
                    loadStatistics();
                }
            });
        }

        // Bind search fields bidirectionally
        if (searchField != null && cardSearchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                cardSearchField.setText(newVal);
            });
            cardSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                searchField.setText(newVal);
            });
        }
        System.out.println("DetailEvent: Initialize completed");
    }

    private void loadLogo() {
        if (logoImageView != null) {
            try {
                String logoPath = "/images/LogoCultify.png";
                Image logoImage = new Image(getClass().getResourceAsStream(logoPath));
                if (logoImage.isError()) {
                    System.err.println("Logo image not found at: " + logoPath);
                } else {
                    logoImageView.setImage(logoImage);
                }
            } catch (Exception e) {
                System.err.println("Error loading logo: " + e.getMessage());
            }
        }
    }

    private void configureTable() {
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateE"));
        organisationCol.setCellValueFactory(new PropertyValueFactory<>("organisation"));
        capaciteCol.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        nbplacesCol.setCellValueFactory(new PropertyValueFactory<>("nbplaces"));
        categorieCol.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prix"));

        imageCol.setCellValueFactory(cellData -> {
            String imagePath = cellData.getValue().getImage();
            ImageView imageView = new ImageView();
            imageView.setFitWidth(150);
            imageView.setFitHeight(100);
            imageView.setPreserveRatio(true);

            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    File file = new File(imagePath);
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString());
                        imageView.setImage(image);
                    } else {
                        System.err.println("Event image not found at: " + imagePath);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading event image: " + e.getMessage());
                }
            }
            return new SimpleObjectProperty<>(imageView);
        });

        imageCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(ImageView imageView, boolean empty) {
                super.updateItem(imageView, empty);
                setGraphic(empty || imageView == null ? null : imageView);
            }
        });

        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");

            {
                editBtn.setOnAction(event -> {
                    Event ev = getTableView().getItems().get(getIndex());
                    handleEdit(ev);
                });

                deleteBtn.setOnAction(event -> {
                    Event ev = getTableView().getItems().get(getIndex());
                    handleDelete(ev);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, editBtn, deleteBtn));
            }
        });
    }

    private void configureStatsSection() {
        statsContainer.setVisible(false);
        statsVisible = false;
    }

    private void configureDetailsPane() {
        detailsPane.setVisible(false);
        reserveButton.setOnAction(e -> {
            Event selectedEvent = (Event) reserveButton.getUserData();
            if (selectedEvent != null) {
                redirectToAddReservation(selectedEvent);
            }
        });
    }

    public void loadData() {
        try {
            System.out.println("DetailEvent: Fetching events from EventService");
            ObservableList<Event> events = FXCollections.observableArrayList(eventService.getAll());
            System.out.println("DetailEvent: Loaded " + events.size() + " events");
            if (tableView != null) {
                tableView.getItems().setAll(events);
            }
            if (cardsContainer != null) {
                System.out.println("DetailEvent: Creating event cards");
                createEventCards(events);
            }
            if (statsVisible) {
                loadStatistics();
            }
        } catch (SQLException e) {
            System.err.println("DetailEvent: SQL error in loadData: " + e.getMessage());
            showAlert("Erreur", "Erreur lors du chargement des données: " + e.getMessage());
        }
    }

    private void createEventCards(ObservableList<Event> events) {
        cardsContainer.getChildren().clear();
        System.out.println("DetailEvent: Creating cards for " + events.size() + " events");

        for (Event event : events) {
            VBox card = new VBox(5);
            card.getStyleClass().add("event-card");
            card.setPrefWidth(250);
            card.setPrefHeight(300);

            ImageView imageView = new ImageView();
            imageView.getStyleClass().add("event-card-image");
            imageView.setFitWidth(250);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(false);

            if (event.getImage() != null && !event.getImage().isEmpty()) {
                try {
                    File file = new File(event.getImage());
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString());
                        imageView.setImage(image);
                    } else {
                        System.err.println("Event image not found at: " + event.getImage());
                    }
                } catch (Exception e) {
                    System.err.println("Error loading event image: " + e.getMessage());
                }
            }

            Label titleLabel = new Label(event.getTitre());
            titleLabel.getStyleClass().add("event-card-title");
            titleLabel.setWrapText(true);
            titleLabel.setMaxWidth(230);

            Label priceLabel = new Label(String.format("%.2f €", event.getPrix()));
            priceLabel.getStyleClass().add("event-card-price");

            Label categoryLabel = new Label(event.getCategorie());
            categoryLabel.getStyleClass().add("event-card-category");

            Button reserveButton = new Button("Réserver");
            reserveButton.getStyleClass().add("reserve-button");
            reserveButton.setOnAction(e -> redirectToAddReservation(event));

            VBox detailsBox = new VBox(5, priceLabel, categoryLabel, reserveButton);
            detailsBox.getStyleClass().add("event-card-details");

            VBox.setMargin(titleLabel, new Insets(5, 10, 5, 10));
            VBox.setMargin(detailsBox, new Insets(0, 10, 10, 10));

            card.getChildren().addAll(imageView, titleLabel, detailsBox);
            card.setOnMouseClicked(e -> showEventDetails(event));
            cardsContainer.getChildren().add(card);
        }
        System.out.println("DetailEvent: Finished creating event cards");
    }

    @FXML
    private void showEventDetails(Event event) {
        if (event.getImage() != null && !event.getImage().isEmpty()) {
            try {
                File file = new File(event.getImage());
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    detailImageView.setImage(image);
                } else {
                    System.err.println("Event image not found at: " + event.getImage());
                    detailImageView.setImage(null);
                }
            } catch (Exception e) {
                System.err.println("Error loading event image: " + e.getMessage());
                detailImageView.setImage(null);
            }
        } else {
            detailImageView.setImage(null);
        }

        detailTitle.setText(event.getTitre());
        detailDescription.setText(event.getDescription());
        detailDate.setText(event.getDateE().toString());
        detailOrganisation.setText(event.getOrganisation());
        detailCapacite.setText(String.valueOf(event.getCapacite()));
        detailNbPlaces.setText(String.valueOf(event.getNbplaces()));
        detailCategorie.setText(event.getCategorie());
        detailPrix.setText(String.format("%.2f €", event.getPrix()));

        reserveButton.setUserData(event);
        detailsPane.setVisible(true);
    }

    @FXML
    private void hideDetails() {
        detailsPane.setVisible(false);
    }

    @FXML
    private void toggleStatistics() {
        if (statsContainer.isVisible()) {
            statsContainer.setVisible(false);
            btnShowStats.setText("Afficher les statistiques");
            statsVisible = false;
        } else {
            loadStatistics();
            statsContainer.setVisible(true);
            btnShowStats.setText("Masquer les statistiques");
            statsVisible = true;
        }
    }

    @FXML
    private void searchByTitle() {
        String searchText = (searchField != null ? searchField.getText() : cardSearchField.getText()).trim();
        if (searchText.isEmpty()) {
            loadData();
            return;
        }

        try {
            ObservableList<Event> filteredEvents = FXCollections.observableArrayList(
                    eventService.searchByTitle(searchText)
            );
            if (tableView != null) {
                tableView.setItems(filteredEvents);
            }
            if (cardsContainer != null) {
                createEventCards(filteredEvents);
            }
            if (statsVisible) {
                loadStatistics();
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Une erreur est survenue lors de la recherche: " + e.getMessage());
        }
    }

    @FXML
    private void sortByDate() {
        try {
            ObservableList<Event> events = FXCollections.observableArrayList(
                    eventService.getAllSortedByDate()
            );
            if (tableView != null) {
                tableView.setItems(events);
            }
            if (cardsContainer != null) {
                createEventCards(events);
            }
            if (statsVisible) {
                loadStatistics();
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Une erreur est survenue lors du tri: " + e.getMessage());
        }
    }

    @FXML
    private void sortByPrice() {
        try {
            ObservableList<Event> events = FXCollections.observableArrayList(
                    eventService.getAllSortedByPrice()
            );
            if (tableView != null) {
                tableView.setItems(events);
            }
            if (cardsContainer != null) {
                createEventCards(events);
            }
            if (statsVisible) {
                loadStatistics();
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Une erreur est survenue lors du tri par prix: " + e.getMessage());
        }
    }

    private void loadStatistics() {
        try {
            Map<String, Integer> eventsByCategory = eventService.countEventsByCategory();
            Map<String, Double> avgPriceByCategory = eventService.averagePriceByCategory();
            Map<String, Double> avgCapacityByCategory = eventService.averageCapacityByCategory();
            Map<String, Double> occupancyRateByCategory = eventService.occupancyRateByCategory();
            int totalEvents = eventService.getTotalEvents();

            updatePieChart(eventsByCategory);
            displayStatistics(eventsByCategory, avgPriceByCategory, avgCapacityByCategory,
                    occupancyRateByCategory, totalEvents);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les statistiques: " + e.getMessage());
        }
    }

    private void updatePieChart(Map<String, Integer> eventsByCategory) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : eventsByCategory.entrySet()) {
            pieChartData.add(new PieChart.Data(
                    entry.getKey() + " (" + entry.getValue() + ")",
                    entry.getValue()
            ));
        }
        categoryPieChart.setData(pieChartData);
        categoryPieChart.setTitle("Événements par catégorie");
        categoryPieChart.setLegendVisible(true);
    }

    private void displayStatistics(Map<String, Integer> eventsByCategory,
                                   Map<String, Double> avgPriceByCategory,
                                   Map<String, Double> avgCapacityByCategory,
                                   Map<String, Double> occupancyRateByCategory,
                                   int totalEvents) {
        StringBuilder statsText = new StringBuilder();
        statsText.append("=== Statistiques globales ===\n");
        statsText.append("Nombre total d'événements: ").append(totalEvents).append("\n\n");
        statsText.append("=== Statistiques par catégorie ===\n");
        for (String category : eventsByCategory.keySet()) {
            statsText.append("\nCatégorie: ").append(category).append("\n");
            statsText.append(" - Nombre d'événements: ").append(eventsByCategory.get(category)).append("\n");
            statsText.append(String.format(" - Prix moyen: %.2f €\n", avgPriceByCategory.getOrDefault(category, 0.0)));
            statsText.append(String.format(" - Capacité moyenne: %.1f places\n", avgCapacityByCategory.getOrDefault(category, 0.0)));
            statsText.append(String.format(" - Taux d'occupation moyen: %.1f%%\n", occupancyRateByCategory.getOrDefault(category, 0.0)));
        }
        statsTextArea.setText(statsText.toString());
    }

    private void handleEdit(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/AjouterEvent.fxml"));
            Parent root = loader.load();
            AjouterEventController controller = loader.getController();
            controller.initEditData(event);
            controller.setOnEventAddedCallback(() -> {
                loadData();
                if (statsVisible) {
                    loadStatistics();
                }
            });
            Scene currentScene = tableView.getScene();
            if (currentScene != null) {
                currentScene.setRoot(root);
            } else {
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le formulaire d'édition: " + e.getMessage());
        }
    }

    private void handleDelete(Event event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer l'événement");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cet événement ?");
        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                String imagePath = event.getImage();
                if (imagePath != null && !imagePath.isEmpty()) {
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }
                eventService.delete(event);
                loadData();
                if (statsVisible) {
                    loadStatistics();
                }
                showAlert("Succès", "Événement supprimé !");
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void redirectToAddForm(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/AjouterEvent.fxml"));
            Parent root = loader.load();
            AjouterEventController controller = loader.getController();
            controller.setOnEventAddedCallback(() -> {
                loadData();
                if (statsVisible) {
                    loadStatistics();
                }
            });
            Scene currentScene = tableView.getScene();
            if (currentScene != null) {
                currentScene.setRoot(root);
            } else {
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le formulaire d'ajout: " + e.getMessage());
        }
    }

    private void redirectToAddReservation(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reservation/AjouterReservation.fxml"));
            Parent root = loader.load();
            AjouterReservation controller = loader.getController();
            controller.setEventData(event);
            Scene currentScene = reserveButton.getScene();
            if (currentScene != null) {
                currentScene.setRoot(root);
            } else {
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de réservation: " + e.getMessage());
        }
    }

    @FXML
    public void redirectToAddReservation(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/reservation/AjouterReservation.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void setEventData(Event event) {
    }

    public void setUserMode(boolean b) {
    }

    public void handleReservation(ActionEvent actionEvent) {
        Event selectedEvent = (Event) reserveButton.getUserData();
        if (selectedEvent != null) {
            redirectToAddReservation(selectedEvent);
        } else {
            showAlert("Erreur", "Aucun événement sélectionné pour la réservation.");
        }
    }

    @FXML
    public void clearCardsSorting(ActionEvent actionEvent) {
        loadData();
    }

    @FXML
    public void sortCardsByPrice(ActionEvent actionEvent) {
        try {
            ObservableList<Event> events = FXCollections.observableArrayList(
                    eventService.getAllSortedByPrice()
            );
            if (tableView != null) {
                tableView.setItems(events);
            }
            if (cardsContainer != null) {
                createEventCards(events);
            }
            if (statsVisible) {
                loadStatistics();
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du tri par prix: " + e.getMessage());
        }
    }

    @FXML
    public void sortCardsByDate(ActionEvent actionEvent) {
        try {
            ObservableList<Event> events = FXCollections.observableArrayList(
                    eventService.getAllSortedByDate()
            );
            if (tableView != null) {
                tableView.setItems(events);
            }
            if (cardsContainer != null) {
                createEventCards(events);
            }
            if (statsVisible) {
                loadStatistics();
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du tri par date: " + e.getMessage());
        }
    }

    @FXML
    public void clearSorting(ActionEvent actionEvent) {
        loadData();
    }

    @FXML
    public void sortByPopularity(ActionEvent actionEvent) throws SQLException {
        ObservableList<Event> events = FXCollections.observableArrayList(
                eventService.getAllSortedByPopularity()
        );
        if (tableView != null) {
            tableView.setItems(events);
        }
        if (cardsContainer != null) {
            createEventCards(events);
        }
        if (statsVisible) {
            loadStatistics();
        }
    }

    @FXML
    public void ajouterReservationAction(ActionEvent actionEvent) {
        Event selectedEvent = (Event) reserveButton.getUserData();
        if (selectedEvent != null) {
            redirectToAddReservation(selectedEvent);
        } else {
            showAlert("Erreur", "Aucun événement sélectionné pour ajouter une réservation.");
        }
    }

    @FXML
    public void retourVersDetails(ActionEvent actionEvent) {
        detailsPane.setVisible(true);
    }
}