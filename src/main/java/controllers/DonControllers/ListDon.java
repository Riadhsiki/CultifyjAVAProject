package controllers.DonControllers;

import models.Association;
import models.Don;
import models.User;
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
import javafx.stage.Stage;
import services.associationDon.AssociationServices;
import services.associationDon.DonServices;
import services.user.UserService;
import utils.PaginationUtils;
import utils.SessionManager;
import utils.WebhookUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ListDon {

    @FXML private ListView<Don> listView;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> searchCriteriaComboBox;
    @FXML private ComboBox<Integer> itemsPerPageComboBox;
    @FXML private Pagination pagination;
    @FXML private Label pageInfoLabel;
    @FXML private Label headerAssociation;
    @FXML private Label headerMontant;
    @FXML private Label headerStatus;
    @FXML private Label headerAction;

    private final DonServices donService = new DonServices();
    private ObservableList<Don> observableList;
    private PaginationUtils<Don> paginationUtils;
    private List<Don> allDons;
    private List<Don> filteredDons;
    private Integer currentUserId = null;
    private enum SortColumn { ID, ASSOCIATION, MONTANT, STATUS }
    private enum SortOrder { ASC, DESC }
    private SortColumn currentSortColumn = SortColumn.ID;
    private SortOrder currentSortOrder = SortOrder.ASC;
    private static final double[] COLUMN_WIDTHS = {0.1, 0.3, 0.15, 0.15, 0.3};
    private static final Logger LOGGER = Logger.getLogger(ListDon.class.getName());

    private Integer getCurrentUserId() {
        String currentUsername = SessionManager.getInstance().getCurrentUsername();
        if (currentUsername == null || currentUsername.isEmpty()) {
            showAlert("Erreur", "Aucun utilisateur connecté");
            return null;
        }
        UserService userService = new UserService();
        User currentUser = userService.getUserByUsername(currentUsername);
        if (currentUser == null) {
            showAlert("Erreur", "Impossible de récupérer les informations de l'utilisateur");
            return null;
        }
        System.out.println("CurrentUser ID: " + currentUser.getId() + ", Username: " + currentUser.getUsername());
        return currentUser.getId();
    }

    @FXML
    public void initialize() {
        System.out.println("Initializing ListDon controller");
        SessionManager.getInstance().dumpPreferences();

        if (!SessionManager.getInstance().isLoggedIn()) {
            showAlert("Erreur", "Veuillez vous connecter pour voir vos dons");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/Login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) listView.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                System.err.println("Error navigating to login: " + e.getMessage());
            }
            return;
        }

        try {
            this.currentUserId = getCurrentUserId();
            if (this.currentUserId == null) {
                showAlert("Erreur", "Veuillez vous connecter pour voir vos dons");
                return;
            }
            System.out.println("Current user ID retrieved: " + this.currentUserId);

            listView.setSelectionModel(null);
            ObservableList<String> searchCriteria = FXCollections.observableArrayList("association", "montant", "status");
            searchCriteriaComboBox.setItems(searchCriteria);
            searchCriteriaComboBox.setValue("association");
            itemsPerPageComboBox.getItems().addAll(5, 10, 15, 20, 25);
            itemsPerPageComboBox.setValue(10);

            allDons = donService.getDonsByUser(this.currentUserId);
            System.out.println("Loaded " + (allDons != null ? allDons.size() : 0) + " donations for user ID: " + this.currentUserId);
            filteredDons = allDons;
            paginationUtils = new PaginationUtils<>(filteredDons, 10);

            updatePaginationControl();
            loadCurrentPage();
            listView.setCellFactory(param -> new DonListCell());
            updateColumnWidths();

            listView.widthProperty().addListener((obs, oldVal, newVal) -> {
                updateColumnWidths();
                listView.refresh();
            });

            setupEventListeners();
            setupSortListeners();
        } catch (Exception e) {
            System.err.println("Error in ListDon initialization: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur d'initialisation: " + e.getMessage());
        }
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
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage());
            }
        });
        searchCriteriaComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                performSearch(searchField.getText());
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors du changement de critère: " + e.getMessage());
            }
        });
    }

    private void performSearch(String searchText) throws SQLException {
        if (this.currentUserId == null) {
            showAlert("Erreur", "Aucun utilisateur connecté");
            return;
        }
        String criteria = searchCriteriaComboBox.getValue();
        if (searchText == null || searchText.isEmpty()) {
            filteredDons = allDons;
        } else {
            filteredDons = donService.searchDons(searchText, criteria, this.currentUserId);
        }
        applySorting();
        paginationUtils.updateFullList(filteredDons);
        updatePaginationControl();
        loadCurrentPage();
    }

    private void setupSortListeners() {
        headerAssociation.getStyleClass().add("clickable-header");
        headerMontant.getStyleClass().add("clickable-header");
        headerStatus.getStyleClass().add("clickable-header");
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
            currentSortOrder = currentSortOrder == SortOrder.ASC ? SortOrder.DESC : SortOrder.ASC;
        } else {
            currentSortColumn = column;
            currentSortOrder = SortOrder.ASC;
        }
        updateSortIndicators();
    }

    private void updateSortIndicators() {
        headerAssociation.setText("Association");
        headerMontant.setText("Montant");
        headerStatus.setText("Statut");
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
        if (currentSortColumn != SortColumn.ID && comparator != null) {
            comparator = comparator.thenComparing(Don::getId);
        }
        if (comparator != null && currentSortOrder == SortOrder.DESC) {
            comparator = comparator.reversed();
        }
        if (comparator != null && filteredDons != null) {
            filteredDons = filteredDons.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
            paginationUtils.updateFullList(filteredDons);
            updatePaginationControl();
            loadCurrentPage();
        }
    }

    private void updatePaginationControl() {
        int totalPages = paginationUtils.getTotalPages();
        pagination.setPageCount(totalPages > 0 ? totalPages : 1);
        pagination.setCurrentPageIndex(paginationUtils.getCurrentPage() - 1);
        updatePageInfoLabel();
        System.out.println("Pagination updated: Total Pages = " + totalPages + ", Current Page = " + paginationUtils.getCurrentPage());
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
            pageInfoLabel.setText(String.format("Affichage %d à %d sur %d dons", startItem, endItem, totalItems));
        }
        System.out.println("Page Info Updated: " + pageInfoLabel.getText());
    }

    private void loadCurrentPage() {
        List<Don> pageDons = paginationUtils.getCurrentPageItems();
        observableList = FXCollections.observableArrayList(pageDons);
        listView.setItems(observableList);
        listView.refresh();
        System.out.println("Loaded page with " + pageDons.size() + " items");
    }

    private void updateColumnWidths() {
        double totalWidth = listView.getWidth();
        if (totalWidth <= 0) totalWidth = 800;
        Label[] headers = {headerAssociation, headerMontant, headerStatus, headerAction};
        for (int i = 0; i < headers.length; i++) {
            headers[i].setPrefWidth(totalWidth * COLUMN_WIDTHS[i+1]);
            headers[i].setPadding(new Insets(0, 0, 0, 10));
        }
    }

    private class DonListCell extends ListCell<Don> {
        private final HBox row = new HBox();
        private final Label[] labels = new Label[4];
        private final HBox actionBox = new HBox(5);
        private final Button editBtn = new Button("Modifier");
        private final Button deleteBtn = new Button("Supprimer");
        private final Button confirmBtn = new Button("Confirmer");

        public DonListCell() {
            super();
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new Label();
                labels[i].setStyle("-fx-padding: 8 0 8 10; -fx-wrap-text: false;");
                labels[i].setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(labels[i], Priority.ALWAYS);
            }
            editBtn.getStyleClass().add("edit-button");
            deleteBtn.getStyleClass().add("delete-button");
            confirmBtn.getStyleClass().add("confirm-button");
            editBtn.setOnAction(event -> {
                Don don = getItem();
                if (don != null) editDon(don);
            });
            deleteBtn.setOnAction(event -> {
                Don don = getItem();
                if (don != null) deleteDon(don);
            });
            confirmBtn.setOnAction(event -> {
                Don don = getItem();
                if (don != null) confirmDon(don);
            });
            actionBox.setAlignment(Pos.CENTER);
            actionBox.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(actionBox, Priority.ALWAYS);
            row.getChildren().addAll(labels);
            row.getChildren().add(actionBox);
        }

        @Override
        protected void updateItem(Don don, boolean empty) {
            super.updateItem(don, empty);
            if (empty || don == null) {
                setGraphic(null);
            } else {
                labels[0].setText(don.getAssociation() != null ? don.getAssociation().getNom() : "N/A");
                labels[1].setText(String.format("%.2f TND", don.getMontant()));
                labels[2].setText(don.getStatus());
                labels[3].setText("");
                actionBox.getChildren().clear();
                if ("en_attente".equals(don.getStatus())) {
                    actionBox.getChildren().addAll(editBtn, deleteBtn, confirmBtn);
                }
                updateLabelWidths();
                setGraphic(row);
            }
        }

        private void updateLabelWidths() {
            double totalWidth = getListView().getWidth();
            if (totalWidth <= 0) totalWidth = 800;
            for (int i = 0; i < labels.length; i++) {
                labels[i].setPrefWidth(totalWidth * COLUMN_WIDTHS[i+1]);
            }
            actionBox.setPrefWidth(totalWidth * COLUMN_WIDTHS[COLUMN_WIDTHS.length - 1]);
        }
    }

    private void editDon(Don don) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Don/UpdateDon.fxml"));
            Parent root = loader.load();
            controllers.donControllers.UpdateDon controller = loader.getController();
            controller.setDon(don);
            Stage stage = (Stage) listView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
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
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Don/Paiement.fxml"));
                    Parent root = loader.load();
                    PaymentWindowController controller = loader.getController();
                    controller.setDon(don);
                    Stage stage = (Stage) listView.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setOnHidden(e -> {
                        if (controller.isPaymentConfirmed()) {
                            try {
                                don.setStatus("confirme");
                                donService.update(don, don.getAssociation().getId());
                                refreshList();
                                showAlert("Succès", "Don confirmé avec succès");
                                try {
                                    AssociationServices associationServices = new AssociationServices();
                                    Association fullAssociation = associationServices.getById(don.getAssociation().getId());
                                    new Thread(() -> {
                                        boolean notified = WebhookUtil.notifyDonationProgress(fullAssociation);
                                        if (notified) {
                                            javafx.application.Platform.runLater(() -> {
                                                LOGGER.log(Level.INFO, "Association progress notification sent successfully");
                                            });
                                        } else {
                                            javafx.application.Platform.runLater(() -> {
                                                LOGGER.log(Level.WARNING, "Failed to notify association progress");
                                            });
                                        }
                                    }).start();
                                } catch (SQLException ex) {
                                    LOGGER.log(Level.WARNING, "Could not load association details for webhook notification", ex);
                                }
                                new Thread(() -> {
                                    try {
                                        Don updatedDon = donService.getDonDetails(don.getId());
                                        String donorEmail = getUserEmail(updatedDon.getUser().getId());
                                        String donorName = getUserName(updatedDon.getUser().getId());
                                        if (donorEmail != null && !donorEmail.isEmpty()) {
                                            MailApi.sendConfirmationEmailWithPDF(
                                                    donorEmail,
                                                    donorName != null ? donorName : "Donateur",
                                                    updatedDon.getUser().getId(),
                                                    updatedDon.getMontant(),
                                                    updatedDon.getAssociation().getNom()
                                            );
                                            javafx.application.Platform.runLater(() -> {
                                                showAlert("Envoi d'email", "Un email de confirmation avec reçu PDF a été envoyé au donateur");
                                            });
                                        }
                                    } catch (Exception ex) {
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

    private String getUserEmail(int userId) {
        try {
            UserService userService = new UserService();
            User user = userService.getById(userId);
            if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                System.out.println("Retrieved user email: " + user.getEmail() + " for userId: " + userId);
                return user.getEmail();
            } else {
                System.err.println("User email not found or empty for userId: " + userId);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'email de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String getUserName(int userId) {
        try {
            UserService userService = new UserService();
            User user = userService.getById(userId);
            if (user != null) {
                String name = user.getNom() + " " + user.getPrenom();
                System.out.println("Retrieved user name: " + name + " for userId: " + userId);
                return name;
            } else {
                System.err.println("User not found for userId: " + userId);
                return "Donateur";
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du nom de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
            return "Donateur";
        }
    }

    private void refreshList() {
        try {
            if (this.currentUserId == null) {
                showAlert("Erreur", "Aucun utilisateur connecté");
                return;
            }
            allDons = donService.getDonsByUser(this.currentUserId);
            String searchText = searchField.getText();
            String criteria = searchCriteriaComboBox.getValue();
            if (searchText != null && !searchText.isEmpty()) {
                filteredDons = donService.searchDons(searchText, criteria, this.currentUserId);
            } else {
                filteredDons = allDons;
            }
            applySorting();
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
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Don/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");
        alert.showAndWait();
    }
}