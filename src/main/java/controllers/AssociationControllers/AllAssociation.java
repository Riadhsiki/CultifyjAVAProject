package controllers.associationcontrollers;


import models.Association;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import services.associationDon.AssociationServices;
import utils.PaginationUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AllAssociation {

    @FXML
    private BorderPane mainAnchorPane;

    @FXML
    private ListView<Association> listView;

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

    // Les labels d'en-tête (Supprimé headerID)
    @FXML private Label headerNom;
    @FXML private Label headerDescription;
    @FXML private Label headerBut;
    @FXML private Label headerContact;
    @FXML private Label headerSiteweb;
    @FXML private Label headerImage;
    @FXML private Label headerAction;

    private final AssociationServices ass = new AssociationServices();
    private ObservableList<Association> observableList;
    private PaginationUtils<Association> paginationUtils;
    private List<Association> allAssociations;

    // Variables pour le tri
    private String currentSortField = "id"; // Champ de tri par défaut
    private boolean sortAscending = true; // Tri ascendant par défaut

    // Icônes pour indiquer le tri
    private final String SORT_ASC = "▲";
    private final String SORT_DESC = "▼";
    private final String SORT_NONE = "";

    // Chemin où sont stockées les images
    private static final String IMAGE_DIR = "http://127.0.0.1/cultify/public/uploads/images/";

    // Pourcentages de largeur des colonnes (Supprimé la première colonne ID)
    private static final double[] COLUMN_WIDTHS = {0.15, 0.15, 0.12, 0.12, 0.15, 0.12, 0.19};

    // Liste des critères de recherche
    private final ObservableList<String> searchCriteria = FXCollections.observableArrayList(
            "nom", "description", "but", "contact", "siteWeb", "montant", "tous"
    );

    @FXML
    public void initialize() {
        try {
            // Désactiver la sélection
            listView.setSelectionModel(null);

            // Initialize pagination combo box with options
            itemsPerPageComboBox.getItems().addAll(5, 10, 15, 20, 25);
            itemsPerPageComboBox.setValue(10); // Default value

            // Initialize search criteria combo box
            searchCriteriaComboBox.setItems(searchCriteria);
            searchCriteriaComboBox.setValue("nom"); // Default search by name

            // Charger les données
            allAssociations = ass.getAll();
            paginationUtils = new PaginationUtils<>(allAssociations, 10);

            // Configure pagination control
            updatePaginationControl();

            // Load first page
            loadCurrentPage();

            // Configurer la ListView
            listView.setCellFactory(param -> new AssociationListCell());

            // Initialiser les largeurs des colonnes
            updateColumnWidths();

            // Mettre à jour les largeurs quand la fenêtre est redimensionnée
            listView.widthProperty().addListener((obs, oldVal, newVal) -> {
                updateColumnWidths();
                listView.refresh();
            });

            // Configurer les gestionnaires de tri
            setupSortHandlers();

            // Setup event listeners
            setupEventListeners();

            // Personnaliser le champ de recherche
            setupSearchField();

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de base de données: " + e.getMessage());
        }
    }



    private void setupSortHandlers() {
        // Configurer les gestionnaires d'événements pour les en-têtes (Supprimé headerID)
        headerNom.setOnMouseClicked(event -> sortBy("nom"));
        headerDescription.setOnMouseClicked(event -> sortBy("description"));
        headerBut.setOnMouseClicked(event -> sortBy("but"));
        headerContact.setOnMouseClicked(event -> sortBy("contact"));
        headerSiteweb.setOnMouseClicked(event -> sortBy("siteWeb"));
        headerImage.setOnMouseClicked(event -> sortBy("image"));

        // Ajouter un style de curseur pour indiquer que les en-têtes sont cliquables
        Label[] headers = {headerNom, headerDescription, headerBut,
                headerContact, headerSiteweb, headerImage};
        for (Label header : headers) {
            header.getStyleClass().add("sortable-header");
        }

        // Mettre à jour l'affichage initial des indicateurs de tri
        updateSortIndicators();
    }

    private void sortBy(String field) {
        // Si on clique sur le même champ, inverser l'ordre de tri
        if (currentSortField.equals(field)) {
            sortAscending = !sortAscending;
        } else {
            // Nouveau champ, réinitialiser en ordre ascendant
            currentSortField = field;
            sortAscending = true;
        }

        // Mettre à jour les indicateurs de tri dans les en-têtes
        updateSortIndicators();

        // Trier la liste complète
        sortAssociations();

        // Mettre à jour la pagination avec la nouvelle liste triée
        paginationUtils.updateFullList(allAssociations);

        // Revenir à la page 1 après le tri
        paginationUtils.goToPage(1);
        pagination.setCurrentPageIndex(0);

        // Recharger la page courante
        loadCurrentPage();
    }

    private void updateSortIndicators() {
        // Effacer tous les indicateurs (Supprimé headerID)
        headerNom.setText("Nom " + (currentSortField.equals("nom") ? (sortAscending ? SORT_ASC : SORT_DESC) : SORT_NONE));
        headerDescription.setText("Description " + (currentSortField.equals("description") ? (sortAscending ? SORT_ASC : SORT_DESC) : SORT_NONE));
        headerBut.setText("But " + (currentSortField.equals("but") ? (sortAscending ? SORT_ASC : SORT_DESC) : SORT_NONE));
        headerContact.setText("Contact " + (currentSortField.equals("contact") ? (sortAscending ? SORT_ASC : SORT_DESC) : SORT_NONE));
        headerSiteweb.setText("Site Web " + (currentSortField.equals("siteWeb") ? (sortAscending ? SORT_ASC : SORT_DESC) : SORT_NONE));
        headerImage.setText("Image " + (currentSortField.equals("image") ? (sortAscending ? SORT_ASC : SORT_DESC) : SORT_NONE));
    }

    private void sortAssociations() {
        // Utiliser les streams pour trier la liste selon le champ et l'ordre de tri
        allAssociations = allAssociations.stream()
                .sorted((a1, a2) -> {
                    int result = 0;

                    // Comparaison selon le champ sélectionné
                    switch (currentSortField) {
                        case "id":
                            result = Integer.compare(a1.getId(), a2.getId());
                            break;
                        case "nom":
                            result = compareNullableStrings(a1.getNom(), a2.getNom());
                            break;
                        case "description":
                            result = compareNullableStrings(a1.getDescription(), a2.getDescription());
                            break;
                        case "but":
                            result = compareNullableStrings(a1.getBut(), a2.getBut());
                            break;
                        case "contact":
                            result = compareNullableStrings(a1.getContact(), a2.getContact());
                            break;
                        case "siteWeb":
                            result = compareNullableStrings(a1.getSiteWeb(), a2.getSiteWeb());
                            break;
                        case "image":
                            result = compareNullableStrings(a1.getImage(), a2.getImage());
                            break;
                        default:
                            result = Integer.compare(a1.getId(), a2.getId());
                            break;
                    }

                    // Si les valeurs sont égales, trier par ID comme critère secondaire
                    if (result == 0 && !currentSortField.equals("id")) {
                        result = Integer.compare(a1.getId(), a2.getId());
                    }

                    // Inverser le résultat si le tri est descendant
                    return sortAscending ? result : -result;
                })
                .collect(Collectors.toList());
    }

    // Utilitaire pour comparer des chaînes potentiellement null
    private int compareNullableStrings(String s1, String s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return -1;
        if (s2 == null) return 1;
        return s1.compareTo(s2);
    }

    private void setupSearchField() {
        // Ajouter une icône de recherche ou un style spécial si nécessaire
        searchField.setPromptText("🔍 Rechercher...");

        // Animation focus
        searchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                searchField.setStyle("-fx-border-color: #EFD093; -fx-effect: dropshadow(gaussian, rgba(239, 208, 147, 0.3), 5, 0, 0, 0);");
            } else {
                searchField.setStyle("-fx-border-color: #DCDACF; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 2, 0, 0, 1);");
            }
        });
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

        // Setup search field with search criteria
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            performSearch(newValue);
        });

        // Add listener for search criteria changes to instantly update results
        searchCriteriaComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                performSearch(searchField.getText());
            }
        });
    }

    private void performSearch(String searchText) {
        try {
            if (searchText == null || searchText.isEmpty()) {
                // Si la recherche est vide, charger toutes les associations
                allAssociations = ass.getAll();
            } else {
                // Utiliser la recherche par flux avec le critère sélectionné
                String criteria = searchCriteriaComboBox.getValue();
                allAssociations = ass.searchAssociations(searchText, criteria);
            }

            // Appliquer le tri actuel aux résultats de recherche
            sortAssociations();

            // Mettre à jour la pagination
            paginationUtils.updateFullList(allAssociations);
            updatePaginationControl();

            // Revenir à la page 1 après une recherche
            paginationUtils.goToPage(1);
            pagination.setCurrentPageIndex(0);

            // Recharger la page
            loadCurrentPage();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage());
        }
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
        observableList = FXCollections.observableArrayList(pageAssociations);
        listView.setItems(observableList);
    }

    private void updateColumnWidths() {
        double totalWidth = listView.getWidth();
        if (totalWidth <= 0) totalWidth = 800; // Valeur par défaut

        // Mise à jour des en-têtes sans la colonne ID
        Label[] headers = {headerNom, headerDescription, headerBut,
                headerContact, headerSiteweb, headerImage, headerAction};

        for (int i = 0; i < headers.length; i++) {
            headers[i].setPrefWidth(totalWidth * COLUMN_WIDTHS[i]);
            headers[i].setPadding(new Insets(0, 0, 0, 10));
        }
    }

    private class AssociationListCell extends ListCell<Association> {
        private final HBox row = new HBox();
        // Maintenant 6 labels au lieu de 7 (sans ID)
        private final Label[] labels = new Label[5]; // Pour les 5 premières colonnes (sans ID et sans image)
        private final ImageView imageView = new ImageView(); // Pour afficher l'image
        private final HBox imageBox = new HBox(); // Pour contenir l'image
        private final HBox actionBox = new HBox(5);
        private final Button editBtn = new Button("Modifier");
        private final Button deleteBtn = new Button("Supprimer");

        public AssociationListCell() {
            super();

            // Configurer les labels
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new Label();
                labels[i].getStyleClass().add("cell-label");
                labels[i].setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(labels[i], Priority.ALWAYS);
            }

            // Configurer l'ImageView
            imageView.setFitHeight(40); // Hauteur fixe de l'image
            imageView.setFitWidth(40);  // Largeur fixe de l'image
            imageView.setPreserveRatio(true); // Préserve le ratio de l'image

            // Configurer le conteneur d'image
            imageBox.getChildren().add(imageView);
            imageBox.setAlignment(Pos.CENTER);
            imageBox.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(imageBox, Priority.ALWAYS);

            // Configurer les boutons avec les classes CSS
            editBtn.getStyleClass().add("edit-button");
            deleteBtn.getStyleClass().add("delete-button");

            // Ajouter les actions aux boutons
            editBtn.setOnAction(event -> {
                Association association = getItem();
                if (association != null) {
                    editAssociation(association);
                }
            });

            deleteBtn.setOnAction(event -> {
                Association association = getItem();
                if (association != null) {
                    deleteAssociation(association);
                }
            });

            // Configurer la HBox d'actions
            actionBox.setAlignment(Pos.CENTER);
            actionBox.getChildren().addAll(editBtn, deleteBtn);
            actionBox.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(actionBox, Priority.ALWAYS);

            // Configurer la ligne
            row.getChildren().addAll(labels);
            row.getChildren().add(imageBox); // Ajouter l'imageBox après les labels
            row.getChildren().add(actionBox);

            // Espace entre les boutons
            actionBox.setSpacing(10);
        }

        @Override
        protected void updateItem(Association association, boolean empty) {
            super.updateItem(association, empty);

            if (empty || association == null) {
                setGraphic(null);
            } else {
                // Mettre à jour les labels (sans ID)
                labels[0].setText(association.getNom());

                // Tronquer les textes longs
                String description = association.getDescription();
                if (description != null && description.length() > 30) {
                    description = description.substring(0, 30) + "...";
                }
                labels[1].setText(description);

                String but = association.getBut();
                if (but != null && but.length() > 30) {
                    but = but.substring(0, 30) + "...";
                }
                labels[2].setText(but);

                labels[3].setText(association.getContact());

                String siteWeb = association.getSiteWeb();
                if (siteWeb != null && siteWeb.length() > 30) {
                    siteWeb = siteWeb.substring(0, 30) + "...";
                }
                labels[4].setText(siteWeb);

                // Charger et afficher l'image
                // Charger et afficher l'image
                try {
                    String imageName = association.getImage();
                    if (imageName != null && !imageName.isEmpty()) {
                        // Charger directement depuis l'URL
                        String imageUrl = IMAGE_DIR + imageName;
                        Image image = new Image(imageUrl, true); // true pour le chargement en arrière-plan
                        imageView.setImage(image);

                        // Gestion des erreurs de chargement
                        image.errorProperty().addListener((obs, wasError, isNowError) -> {
                            if (isNowError) {
                                System.err.println("Erreur de chargement de l'image: " + imageUrl);
                                imageView.setImage(null);
                            }
                        });
                    } else {
                        imageView.setImage(null);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
                    imageView.setImage(null);
                }

                // Ajouter des tooltips pour le texte tronqué
                labels[1].setTooltip(new Tooltip(association.getDescription()));
                labels[2].setTooltip(new Tooltip(association.getBut()));
                labels[4].setTooltip(new Tooltip(association.getSiteWeb()));

                // Tooltip pour l'image


                // Ajuster les largeurs
                updateLabelWidths();
                setGraphic(row);
            }
        }

        private void updateLabelWidths() {
            double totalWidth = getListView().getWidth();
            if (totalWidth <= 0) totalWidth = 800;

            for (int i = 0; i < labels.length; i++) {
                labels[i].setPrefWidth(totalWidth * COLUMN_WIDTHS[i]);
            }

            imageBox.setPrefWidth(totalWidth * COLUMN_WIDTHS[5]); // Largeur pour l'image
            actionBox.setPrefWidth(totalWidth * COLUMN_WIDTHS[6]); // Largeur pour les actions
        }
    }

    private void editAssociation(Association association) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Association/UpdateAssociation.fxml"));
            Parent root = loader.load();

            UpdateAssociation controller = loader.getController();
            controller.setAssociation(association);

            Stage stage = (Stage) listView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la page de modification");
        }
    }

    private void deleteAssociation(Association association) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'association");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer " + association.getNom() + "?");

        // Styliser l'alerte
        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Association/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    ass.delete(association);
                    // Refresh the list
                    refreshList();
                    showAlert("Succès", "Association supprimée avec succès");
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors de la suppression");
                }
            }
        });
    }

    private void refreshList() {
        try {
            allAssociations = ass.getAll();
            // Appliquer le tri actuel aux résultats après rafraîchissement
            sortAssociations();
            paginationUtils.updateFullList(allAssociations);
            updatePaginationControl();
            loadCurrentPage();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du rafraîchissement de la liste: " + e.getMessage());
        }
    }

    @FXML
    private void addAssociation() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Association/AddAssociation.fxml"));
            Stage stage = (Stage) listView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la page d'ajout");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);

        // Styliser l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Association/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        alert.showAndWait();
    }
}