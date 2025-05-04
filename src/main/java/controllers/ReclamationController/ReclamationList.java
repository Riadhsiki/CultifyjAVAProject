package Controllers.ReclamationController;

import Entities.Reclamation;
import Services.ReclamationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ReclamationList implements Initializable {

    @FXML
    private ListView<Reclamation> reclamationListView;

    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private TextField searchField;

    private ReclamationService reclamationService;
    private ObservableList<Reclamation> reclamationObservableList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reclamationService = new ReclamationService();

        // Initialiser le ComboBox pour filtrer par statut
        filterComboBox.setItems(FXCollections.observableArrayList(
                "Tous", "En cours", "Traité"
        ));
        filterComboBox.getSelectionModel().select("Tous");
        filterComboBox.setOnAction(event -> filterReclamations());

        // Configuration de la ListView avec une cellule personnalisée
        reclamationListView.setCellFactory(new Callback<ListView<Reclamation>, ListCell<Reclamation>>() {
            @Override
            public ListCell<Reclamation> call(ListView<Reclamation> param) {
                return new ReclamationListCell();
            }
        });

        // Permettre la sélection dans la ListView
        reclamationListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Charger les réclamations
        loadReclamations();
    }

    private void loadReclamations() {
        try {
            List<Reclamation> reclamations = reclamationService.getAll();
            reclamationObservableList = FXCollections.observableArrayList(reclamations);
            reclamationListView.setItems(reclamationObservableList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des réclamations", e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        filterReclamations();
    }

    @FXML
    private void refreshList() {
        loadReclamations();
        searchField.clear();
        filterComboBox.getSelectionModel().select("Tous");
    }

    @FXML
    private void newReclamation() {
        try {
            // Ouverture de la fenêtre de création d'une nouvelle réclamation
            Parent root = FXMLLoader.load(getClass().getResource("/Reclamation/ReclamationAdd.fxml"));
            Stage stage = (Stage) reclamationListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }

    @FXML
    private void handleModifyButton() {
        // Récupérer la réclamation sélectionnée
        Reclamation selectedReclamation = reclamationListView.getSelectionModel().getSelectedItem();

        if (selectedReclamation != null) {
            handleModify(selectedReclamation);
        } else {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucune sélection",
                    "Veuillez sélectionner une réclamation à modifier.");
        }
    }

    private void filterReclamations() {
        try {
            String searchText = searchField.getText().toLowerCase();
            String filterStatus = filterComboBox.getValue();

            List<Reclamation> allReclamations = reclamationService.getAll();

            List<Reclamation> filteredReclamations = allReclamations.stream()
                    .filter(r -> {
                        // Filtrer par statut si un statut spécifique est sélectionné
                        if (!"Tous".equals(filterStatus) && !r.getStatut().equals(filterStatus)) {
                            return false;
                        }

                        // Filtrer par texte de recherche
                        return searchText.isEmpty() ||
                                r.getTitre().toLowerCase().contains(searchText) ||
                                r.getDescription().toLowerCase().contains(searchText) ||
                                r.getEmail().toLowerCase().contains(searchText);
                    })
                    .collect(Collectors.toList());

            reclamationObservableList = FXCollections.observableArrayList(filteredReclamations);
            reclamationListView.setItems(reclamationObservableList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du filtrage des réclamations", e.getMessage());
        }
    }

    private void handleModify(Reclamation reclamation) {
        try {
            // Préparation des données pour la modification
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/ReclamationUpdate.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et définir la réclamation à modifier
            ReclamationUpdate updateController = loader.getController();
            updateController.setReclamation(reclamation);

            Stage stage = (Stage) reclamationListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture du formulaire de modification", e.getMessage());
        }
    }

    private void handleDelete(Reclamation reclamation) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer la réclamation");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    reclamationService.delete(reclamation);
                    loadReclamations();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Réclamation supprimée",
                            "La réclamation a été supprimée avec succès.");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression", e.getMessage());
                }
            }
        });
    }

    private void handleConfirm(Reclamation reclamation) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Confirmer la réclamation");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir marquer cette réclamation comme traitée?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    reclamation.setStatut("Traité");
                    reclamationService.update(reclamation);
                    loadReclamations();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Statut mis à jour",
                            "La réclamation a été marquée comme traitée.");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour du statut", e.getMessage());
                }
            }
        });
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Classe interne pour personnaliser l'affichage des cellules de la ListView
    private class ReclamationListCell extends ListCell<Reclamation> {
        @Override
        protected void updateItem(Reclamation reclamation, boolean empty) {
            super.updateItem(reclamation, empty);

            if (empty || reclamation == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            VBox container = new VBox(5);
            container.setPadding(new Insets(10));

            // Titre et statut
            HBox headerBox = new HBox(10);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            Label titleLabel = new Label(reclamation.getTitre());
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Label statusLabel = new Label(reclamation.getStatut());
            statusLabel.setStyle("-fx-background-radius: 5; -fx-padding: 3 8 3 8;");

            // Appliquer différentes couleurs selon le statut
            switch (reclamation.getStatut()) {
                case "En cours":
                    statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #FFC107; -fx-text-fill: #000;");
                    break;
                case "Traité":
                    statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #4CAF50; -fx-text-fill: #fff;");
                    break;
                default:
                    statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #E0E0E0;");
            }

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            headerBox.getChildren().addAll(titleLabel, spacer, statusLabel);

            // Type et priorité
            HBox detailsBox = new HBox(10);
            detailsBox.setAlignment(Pos.CENTER_LEFT);

            Label typeLabel = new Label("Type: " + reclamation.getType());
            Label priorityLabel = new Label("Priorité: " + reclamation.getPriorite());

            detailsBox.getChildren().addAll(typeLabel, priorityLabel);

            // Description
            Label descriptionLabel = new Label(reclamation.getDescription());
            descriptionLabel.setWrapText(true);

            // Email
            Label emailLabel = new Label("Contact: " + reclamation.getEmail());
            emailLabel.setStyle("-fx-font-style: italic;");

            // Boutons d'action
            HBox actionBox = new HBox(10);
            actionBox.setAlignment(Pos.CENTER_RIGHT);
            actionBox.setPadding(new Insets(5, 0, 0, 0));

            // Ajouter des boutons pour les actions
            Button modifyButton = new Button("Modifier");
            modifyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            modifyButton.setOnAction(e -> handleModify(reclamation));

            Button deleteButton = new Button("Supprimer");
            deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
            deleteButton.setOnAction(e -> handleDelete(reclamation));

            // Ajout du bouton Confirmer uniquement pour les réclamations "En cours"
            if ("En cours".equals(reclamation.getStatut())) {
                Button confirmButton = new Button("Confirmer");
                confirmButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                confirmButton.setOnAction(e -> handleConfirm(reclamation));

                actionBox.getChildren().addAll(modifyButton, deleteButton, confirmButton);
            } else {
                actionBox.getChildren().addAll(modifyButton, deleteButton);
            }

            // Ajouter tous les éléments au conteneur
            container.getChildren().addAll(headerBox, detailsBox, descriptionLabel, emailLabel, actionBox);

            // Ajouter un séparateur visuel entre les éléments
            setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");

            setGraphic(container);
        }
    }
}