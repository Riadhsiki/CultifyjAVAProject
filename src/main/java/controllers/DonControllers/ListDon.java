package controllers.DonControllers;

import entities.Association;
import entities.Don;
import entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.AssociationServices;
import services.DonServices;
import services.UserServices;
import utils.PaginationUtils;
import utils.WebhookUtil;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ListDon {

    @FXML
    private ListView<Don> listView;

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

    // Les labels d'en-tête

    @FXML private Label headerAssociation;
    @FXML private Label headerMontant;
    @FXML private Label headerStatus;
    @FXML private Label headerAction;

    private final DonServices donService = new DonServices();
    private ObservableList<Don> observableList;
    private PaginationUtils<Don> paginationUtils;
    private List<Don> allDons;
    private List<Don> filteredDons;
    private final int CURRENT_USER_ID = 2; // Définir l'ID utilisateur comme constante

    // Pour le tri
    private enum SortColumn { ID, ASSOCIATION, MONTANT, STATUS }
    private enum SortOrder { ASC, DESC }
    private SortColumn currentSortColumn = SortColumn.ID;
    private SortOrder currentSortOrder = SortOrder.ASC;

    // Pourcentages de largeur des colonnes
    private static final double[] COLUMN_WIDTHS = {0.1, 0.3, 0.15, 0.15, 0.3};
    private static final Logger LOGGER = Logger.getLogger(ListDon.class.getName());
    @FXML
    public void initialize() {
        try {
            // Désactiver la sélection
            listView.setSelectionModel(null);

            // Initialiser le ComboBox des critères de recherche
            ObservableList<String> searchCriteria = FXCollections.observableArrayList(
                    "association", "montant", "status"
            );
            searchCriteriaComboBox.setItems(searchCriteria);
            searchCriteriaComboBox.setValue("association"); // Valeur par défaut

            // Initialize pagination combo box with options
            itemsPerPageComboBox.getItems().addAll(5, 10, 15, 20, 25);
            itemsPerPageComboBox.setValue(10); // Default value

            // Charger les données
            allDons = donService.getDonsByUser(CURRENT_USER_ID);
            filteredDons = allDons; // Au début, les données filtrées sont les mêmes que toutes les données
            paginationUtils = new PaginationUtils<>(filteredDons, 10);

            // Configure pagination control
            updatePaginationControl();

            // Load first page
            loadCurrentPage();

            // Configurer la ListView
            listView.setCellFactory(param -> new DonListCell());

            // Initialiser les largeurs des colonnes
            updateColumnWidths();

            // Mettre à jour les largeurs quand la fenêtre est redimensionnée
            listView.widthProperty().addListener((obs, oldVal, newVal) -> {
                updateColumnWidths();
                listView.refresh();
            });

            // Setup event listeners for pagination, search, etc.
            setupEventListeners();

            // Setup sort listeners for column headers
            setupSortListeners();

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de base de données: " + e.getMessage());
        }
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

        // Search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                performSearch(newValue);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage());
            }
        });

        // SearchCriteria change listener
        searchCriteriaComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                // Relancer la recherche avec le nouveau critère
                performSearch(searchField.getText());
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors du changement de critère: " + e.getMessage());
            }
        });
    }

    private void performSearch(String searchText) throws SQLException {
        String criteria = searchCriteriaComboBox.getValue();
        if (searchText == null || searchText.isEmpty()) {
            filteredDons = allDons;
        } else {
            // Utiliser la méthode searchDons du service qui prend en compte le critère
            filteredDons = donService.searchDons(searchText, criteria, CURRENT_USER_ID);
        }

        // Appliquer le tri actuel sur les données filtrées
        applySorting();
        paginationUtils.updateFullList(filteredDons);
        updatePaginationControl();
        loadCurrentPage();
    }

    private void setupSortListeners() {
        // Ajouter la classe CSS pour le style du curseur pointer

        headerAssociation.getStyleClass().add("clickable-header");
        headerMontant.getStyleClass().add("clickable-header");
        headerStatus.getStyleClass().add("clickable-header");

        // Configurer les écouteurs de clics


        headerAssociation.setOnMouseClicked(event -> {
            updateSortOrder(SortColumn.ASSOCIATION);
            applySorting();
        });

        headerMontant.setOnMouseClicked(event -> {
            updateSortOrder(SortColumn.MONTANT);
            applySorting();
        });

        headerStatus.setOnMouseClicked(event -> {
            updateSortOrder(SortColumn.STATUS);
            applySorting();
        });
    }

    private void updateSortOrder(SortColumn column) {
        if (currentSortColumn == column) {
            // Inverser l'ordre de tri si on clique sur la même colonne
            currentSortOrder = currentSortOrder == SortOrder.ASC ? SortOrder.DESC : SortOrder.ASC;
        } else {
            // Nouvelle colonne de tri, initialiser à ASC
            currentSortColumn = column;
            currentSortOrder = SortOrder.ASC;
        }

        // Mettre à jour les labels pour montrer l'ordre de tri
        updateSortIndicators();
    }

    private void updateSortIndicators() {
        // Réinitialiser tous les labels d'en-tête

        headerAssociation.setText("Association");
        headerMontant.setText("Montant");
        headerStatus.setText("Statut");

        // Ajouter l'indicateur de tri à la colonne active
        String indicator = currentSortOrder == SortOrder.ASC ? " ↑" : " ↓";

        switch (currentSortColumn) {

            case ASSOCIATION:
                headerAssociation.setText("Association" + indicator);
                break;
            case MONTANT:
                headerMontant.setText("Montant" + indicator);
                break;
            case STATUS:
                headerStatus.setText("Statut" + indicator);
                break;
        }
    }

    private void applySorting() {
        // Créer le comparateur principal selon la colonne sélectionnée
        Comparator<Don> comparator = null;

        switch (currentSortColumn) {
            case ID:
                comparator = Comparator.comparing(Don::getId);
                break;
            case ASSOCIATION:
                comparator = Comparator.comparing(
                        don -> don.getAssociation() != null ?
                                don.getAssociation().getNom() != null ?
                                        don.getAssociation().getNom().toLowerCase() : "" : "",
                        String.CASE_INSENSITIVE_ORDER
                );
                break;
            case MONTANT:
                comparator = Comparator.comparing(Don::getMontant);
                break;
            case STATUS:
                comparator = Comparator.comparing(
                        don -> don.getStatus() != null ? don.getStatus().toLowerCase() : "",
                        String.CASE_INSENSITIVE_ORDER
                );
                break;
        }

        // Ajouter l'ID comme critère secondaire pour garantir un ordre stable
        if (currentSortColumn != SortColumn.ID) {
            comparator = comparator.thenComparing(Don::getId);
        }

        // Inverser le comparateur si l'ordre est descendant
        if (currentSortOrder == SortOrder.DESC) {
            comparator = comparator.reversed();
        }

        // Trier la liste filtrée
        filteredDons = filteredDons.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        // Mettre à jour la pagination et recharger la page
        paginationUtils.updateFullList(filteredDons);
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
            pageInfoLabel.setText(String.format("Affichage %d à %d sur %d dons",
                    startItem, endItem, totalItems));
        }
    }

    private void loadCurrentPage() {
        List<Don> pageDons = paginationUtils.getCurrentPageItems();
        observableList = FXCollections.observableArrayList(pageDons);
        listView.setItems(observableList);
    }

    private void updateColumnWidths() {
        double totalWidth = listView.getWidth();
        if (totalWidth <= 0) totalWidth = 800; // Valeur par défaut

        Label[] headers = {headerAssociation, headerMontant, headerStatus, headerAction};

        for (int i = 0; i < headers.length; i++) {
            headers[i].setPrefWidth(totalWidth * COLUMN_WIDTHS[i]);
            headers[i].setPadding(new Insets(0, 0, 0, 10));
        }
    }

    private class DonListCell extends ListCell<Don> {
        private final HBox row = new HBox();
        private final Label[] labels = new Label[4]; // Pour les 4 premières colonnes
        private final HBox actionBox = new HBox(5);
        private final Button editBtn = new Button("Modifier");
        private final Button deleteBtn = new Button("Supprimer");
        private final Button confirmBtn = new Button("Confirmer");

        public DonListCell() {
            super();

            // Configurer les labels
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new Label();
                labels[i].setStyle("-fx-padding: 8 0 8 10; -fx-wrap-text: false;");
                labels[i].setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(labels[i], Priority.ALWAYS);
            }

            // Configurer les boutons avec les classes CSS
            editBtn.getStyleClass().add("edit-button");
            deleteBtn.getStyleClass().add("delete-button");
            confirmBtn.getStyleClass().add("confirm-button");

            // Ajouter les actions aux boutons
            editBtn.setOnAction(event -> {
                Don don = getItem();
                if (don != null) {
                    editDon(don);
                }
            });

            deleteBtn.setOnAction(event -> {
                Don don = getItem();
                if (don != null) {
                    deleteDon(don);
                }
            });

            confirmBtn.setOnAction(event -> {
                Don don = getItem();
                if (don != null) {
                    confirmDon(don);
                }
            });

            // Configurer la HBox d'actions
            actionBox.setAlignment(Pos.CENTER);
            actionBox.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(actionBox, Priority.ALWAYS);

            // Configurer la ligne
            row.getChildren().addAll(labels);
            row.getChildren().add(actionBox);
        }

        @Override
        protected void updateItem(Don don, boolean empty) {
            super.updateItem(don, empty);

            if (empty || don == null) {
                setGraphic(null);
            } else {
                // Mettre à jour les labels
                labels[0].setText(String.valueOf(don.getId()));
                labels[1].setText(don.getAssociation() != null ? don.getAssociation().getNom() : "N/A");
                labels[2].setText(String.format("%.2f TND", don.getMontant()));
                labels[3].setText(don.getStatus());

                // Gérer les boutons selon le status
                actionBox.getChildren().clear();
                if ("en_attente".equals(don.getStatus())) {
                    actionBox.getChildren().addAll(editBtn, deleteBtn, confirmBtn);
                }

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
            actionBox.setPrefWidth(totalWidth * COLUMN_WIDTHS[COLUMN_WIDTHS.length - 1]);
        }
    }

    private void editDon(Don don) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Don/UpdateDon.fxml"));
            Parent root = loader.load();

            UpdateDon controller = loader.getController();
            controller.setDon(don);
            Stage stage = (Stage) listView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show(); // Utiliser showAndWait pour bloquer jusqu'à ce que la fenêtre soit fermée

            // Après la fermeture de la fenêtre, rafraîchir la liste
            refreshList();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la page de modification: " + e.getMessage());
        }
    }

    private void deleteDon(Don don) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le don");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce don?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    donService.delete(don);
                    // Refresh the list
                    refreshList();
                    showAlert("Succès", "Don supprimé avec succès");
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    private void confirmDon(Don don) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Confirmer le don");
        confirm.setContentText("Êtes-vous sûr de vouloir confirmer ce don?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Charger la fenêtre de paiement
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Don/Paiement.fxml"));
                    Parent root = loader.load();

                    // Configurer le contrôleur
                    PaymentWindowController controller = loader.getController();
                    controller.setDon(don);

                    // Configurer et afficher la fenêtre
                    Stage stage = (Stage) listView.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();

                    // Ajouter un écouteur pour détecter quand la fenêtre est fermée
                    stage.setOnHidden(e -> {
                        // Vérifier si le paiement a été confirmé dans le contrôleur
                        if (controller.isPaymentConfirmed()) {
                            try {
                                // Changer d'abord le statut du don
                                don.setStatus("confirme");
                                donService.update(don, don.getAssociation().getId());

                                // Refresh la liste et montrer un succès même si l'email échoue
                                refreshList();
                                showAlert("Succès", "Don confirmé avec succès");
                                // automation n8n
                                // Récupérer les informations complètes de l'association
                                try {
                                    AssociationServices associationServices = new AssociationServices();
                                    Association fullAssociation = associationServices.getById(don.getAssociation().getId());

                                    // Notifier N8N via webhook (dans un thread séparé pour ne pas bloquer l'UI)
                                    new Thread(() -> {
                                        boolean notified = WebhookUtil.notifyDonationProgress(fullAssociation);
                                        if (notified) {
                                            javafx.application.Platform.runLater(() -> {
                                                // Ne pas afficher d'alerte pour ne pas submerger l'utilisateur
                                                LOGGER.log(Level.INFO, "Association progress notification sent successfully");
                                            });
                                        } else {
                                            javafx.application.Platform.runLater(() -> {
                                                LOGGER.log(Level.WARNING, "Failed to notify association progress");
                                                // Ne pas afficher d'erreur à l'utilisateur pour cette fonctionnalité non essentielle
                                            });
                                        }
                                    }).start();
                                } catch (SQLException ex) {
                                    LOGGER.log(Level.WARNING, "Could not load association details for webhook notification", ex);
                                    // Ne pas bloquer le processus principal si la récupération des infos a échoué
                                }
                                //mail avec pdf
                                // Essayer d'envoyer l'email, mais ne pas bloquer le flux principal
                                new Thread(() -> {
                                    try {
                                        // Récupérer les informations complètes de l'utilisateur et du don
                                        Don updatedDon = donService.getDonDetails(don.getId());

                                        // Récupérer l'email et le nom du donateur
                                        String donorEmail = getUserEmail(updatedDon.getUser().getId());
                                        String donorName = getUserName(updatedDon.getUser().getId());

                                        if (donorEmail != null && !donorEmail.isEmpty()) {
                                            // Envoyer l'email de confirmation avec PDF
                                            MailApi.sendConfirmationEmailWithPDF(
                                                    donorEmail,
                                                    donorName != null ? donorName : "Donateur",
                                                    updatedDon.getId(),
                                                    updatedDon.getMontant(),
                                                    updatedDon.getAssociation().getNom()
                                            );

                                            // Mise à jour de l'interface utilisateur dans le thread JavaFX
                                            javafx.application.Platform.runLater(() -> {
                                                showAlert("Envoi d'email", "Un email de confirmation avec reçu PDF a été envoyé au donateur");
                                            });
                                        }
                                    } catch (Exception ex) {
                                        // Gestion des erreurs dans le thread séparé
                                        javafx.application.Platform.runLater(() -> {
                                            showAlert("Erreur", "Erreur lors de l'envoi de l'email ou de la génération du PDF: " + ex.getMessage());
                                        });
                                    }
                                }).start();
                            } catch (SQLException ex) {
                                showAlert("Erreur", "Erreur lors de la confirmation: " + ex.getMessage());
                            }
                        }
                    });

                    stage.show();

                } catch (IOException e) {
                    showAlert("Erreur", "Impossible d'ouvrir la fenêtre de paiement: " + e.getMessage());
                }
            }
        });
    }

    // Méthode pour récupérer l'email de l'utilisateur
    private String getUserEmail(int userId) {
        try {
            UserServices userService = new UserServices();
            User user = userService.getUserById(userId);
            return user != null ? user.getEmail() : null;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'email de l'utilisateur: " + e.getMessage());
            return null;
        }
    }

    // Méthode pour récupérer le nom de l'utilisateur
    private String getUserName(int userId) {
        try {
            UserServices userService = new UserServices();
            User user = userService.getUserById(userId);
            return user != null ? user.getName() : "Donateur";
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du nom de l'utilisateur: " + e.getMessage());
            return "Donateur";
        }
    }

    private void refreshList() {
        try {
            // Recharger les données
            allDons = donService.getDonsByUser(CURRENT_USER_ID);

            // Appliquer le filtre de recherche actuel
            String searchText = searchField.getText();
            String criteria = searchCriteriaComboBox.getValue();

            if (searchText != null && !searchText.isEmpty()) {
                // Utiliser la méthode searchDons du service avec le critère sélectionné
                filteredDons = donService.searchDons(searchText, criteria, CURRENT_USER_ID);
            } else {
                filteredDons = allDons;
            }

            // Réappliquer le tri actuel
            applySorting();

            // Mettre à jour la pagination
            paginationUtils.updateFullList(filteredDons);
            updatePaginationControl();
            loadCurrentPage();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du rafraîchissement de la liste: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);

        // Appliquer le style CSS
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Don/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        alert.showAndWait();
    }

}