package controllers.ReponseController;

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
import models.Reponse;
import services.reponse.ReponseService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ReponseListAdmin implements Initializable {

    @FXML
    private VBox mainPane;

    @FXML
    private ListView<Reponse> reponsesListView;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Button addButton;

    @FXML
    private Button refreshButton;

    private ReponseService reponseService;
    private ObservableList<Reponse> reponsesList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reponseService = new ReponseService();
        reponsesList = FXCollections.observableArrayList();

        // Configuration de la ListView avec une cellule personnalisée
        reponsesListView.setCellFactory(new Callback<ListView<Reponse>, ListCell<Reponse>>() {
            @Override
            public ListCell<Reponse> call(ListView<Reponse> param) {
                return new ReponseListCell();
            }
        });

        // Permettre la sélection dans la ListView
        reponsesListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Charger les réponses
        loadReponses();
    }

    private void loadReponses() {
        try {
            reponsesList.clear();
            reponsesList.addAll(reponseService.getAll());
            reponsesListView.setItems(reponsesList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des réponses", e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        try {
            // Obtenir le Stage actuel
            Stage currentStage = (Stage) mainPane.getScene().getWindow();

            // Charger le formulaire d'ajout de réponse
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reponse/ReponseAdd.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène
            Scene addReponseScene = new Scene(root);

            // Changer la scène du Stage actuel
            currentStage.setScene(addReponseScene);
            currentStage.setTitle("Ajouter une réponse");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre d'ajout", e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadReponses();
        searchField.clear();
    }

    @FXML
    private void handleSearch() {
        try {
            String searchText = searchField.getText().toLowerCase();

            List<Reponse> allReponses = reponseService.getAll();

            List<Reponse> filteredReponses = allReponses.stream()
                    .filter(r -> searchText.isEmpty() || r.getTitre().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());

            reponsesList = FXCollections.observableArrayList(filteredReponses);
            reponsesListView.setItems(reponsesList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du filtrage des réponses", e.getMessage());
        }
    }

    private void handleUpdate(Reponse reponse) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reponse/ReponseUpdate.fxml"));
            Parent root = loader.load();

            // Passer les données à la fenêtre de modification
            controllers.ReponseController.ReponseUpdate controller = loader.getController();
            controller.initData(reponse);

            // Réutiliser le stage actuel
            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier une réponse");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de modification", e.getMessage());
        }
    }

    private void handleDelete(Reponse reponse) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer une réponse");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    reponseService.delete(reponse);
                    loadReponses();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Réponse supprimée",
                            "La réponse a été supprimée avec succès.");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression", e.getMessage());
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
    private class ReponseListCell extends ListCell<Reponse> {
        @Override
        protected void updateItem(Reponse reponse, boolean empty) {
            super.updateItem(reponse, empty);

            if (empty || reponse == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            VBox container = new VBox(5);
            container.setPadding(new Insets(10));

            // Titre
            HBox headerBox = new HBox(10);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            Label titleLabel = new Label(reponse.getTitre());
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            headerBox.getChildren().addAll(titleLabel, spacer);

            // Date
            HBox detailsBox = new HBox(10);
            detailsBox.setAlignment(Pos.CENTER_LEFT);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String dateText = reponse.getReponsedate() != null ? sdf.format(reponse.getReponsedate()) : "";
            Label dateLabel = new Label("Date: " + dateText);

            detailsBox.getChildren().add(dateLabel);

            // Contenu
            Label contenuLabel = new Label(reponse.getContenu());
            contenuLabel.setWrapText(true);

            // Offre
            Label offreLabel = new Label("Offre: " + reponse.getOffre());
            offreLabel.setStyle("-fx-font-style: italic;");

            // Boutons d'action
            HBox actionBox = new HBox(10);
            actionBox.setAlignment(Pos.CENTER_RIGHT);
            actionBox.setPadding(new Insets(5, 0, 0, 0));

            // Boutons Modifier et Supprimer
            Button modifyButton = new Button("Modifier");
            modifyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            modifyButton.setOnAction(e -> handleUpdate(reponse));

            Button deleteButton = new Button("Supprimer");
            deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
            deleteButton.setOnAction(e -> handleDelete(reponse));

            actionBox.getChildren().addAll(modifyButton, deleteButton);

            // Ajouter tous les éléments au conteneur
            container.getChildren().addAll(headerBox, detailsBox, contenuLabel, offreLabel, actionBox);

            // Ajouter un séparateur visuel entre les éléments
            setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");

            setGraphic(container);
        }
    }
}