package controllers.ReponseController;

import models.Reponse;
import services.reponse.ReponseService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class ReponseList implements Initializable {

    @FXML
    private AnchorPane mainPane;

    @FXML
    private TableView<Reponse> reponsesTable;

    @FXML
    private TableColumn<Reponse, Integer> idColumn;

    @FXML
    private TableColumn<Reponse, String> dateColumn;

    @FXML
    private TableColumn<Reponse, String> titreColumn;

    @FXML
    private TableColumn<Reponse, String> contenuColumn;

    @FXML
    private TableColumn<Reponse, String> offreColumn;

    @FXML
    private TableColumn<Reponse, Integer> reclamationColumn;

    @FXML
    private Button addButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button refreshButton;

    private ReponseService reponseService;
    private ObservableList<Reponse> reponsesList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reponseService = new ReponseService();
        reponsesList = FXCollections.observableArrayList();

        setupTable();
        loadReponses();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id_reponse"));

        // Formatage de la date
        dateColumn.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getReponsedate();
            if (date != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                return new javafx.beans.property.SimpleStringProperty(sdf.format(date));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        contenuColumn.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        offreColumn.setCellValueFactory(new PropertyValueFactory<>("offre"));

        // Afficher l'ID de la réclamation
        reclamationColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getReclamation() != null) {
                return new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getReclamation().getId_reclamation());
            }
            return new javafx.beans.property.SimpleObjectProperty<>(null);
        });

        reponsesTable.setItems(reponsesList);
    }

    private void loadReponses() {
        try {
            reponsesList.clear();
            reponsesList.addAll(reponseService.getAll());
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des réponses: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reponse/ReponseAdd.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter une réponse");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Rafraîchir la liste après la fermeture de la fenêtre d'ajout
            loadReponses();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'ajout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadReponses();
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        Reponse selectedReponse = reponsesTable.getSelectionModel().getSelectedItem();
        if (selectedReponse != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation");
            confirmAlert.setHeaderText("Supprimer une réponse");
            confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        reponseService.delete(selectedReponse);
                        reponsesList.remove(selectedReponse);
                        showAlert("Succès", "La réponse a été supprimée avec succès.", Alert.AlertType.INFORMATION);
                    } catch (SQLException e) {
                        showAlert("Erreur", "Erreur lors de la suppression de la réponse: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
        } else {
            showAlert("Information", "Veuillez sélectionner une réponse à supprimer.", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        Reponse selectedReponse = reponsesTable.getSelectionModel().getSelectedItem();
        if (selectedReponse != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reponse/ReponseUpdate.fxml"));
                Parent root = loader.load();

                // Passer les données à la fenêtre de modification
                ReponseUpdate controller = loader.getController();
                controller.initData(selectedReponse);

                Stage stage = new Stage();
                stage.setTitle("Modifier une réponse");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

                // Rafraîchir la liste après la fermeture de la fenêtre de modification
                loadReponses();
            } catch (IOException e) {
                showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Information", "Veuillez sélectionner une réponse à modifier.", Alert.AlertType.INFORMATION);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}