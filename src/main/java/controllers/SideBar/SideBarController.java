package controllers.SideBar;

import controllers.MainLayoutController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;


import java.io.IOException;

public class SideBarController {

    private MainLayoutController mainController;

    @FXML
    private ImageView logoImageView;

    @FXML
    private Button btnAssociations;

    @FXML
    private VBox associationSubmenu;

    @FXML
    private Button btnLogout;

    private boolean isAssociationSubmenuVisible = false;

    @FXML
    public void initialize() {
        // Initialize the sidebar components
    }

    @FXML
    private void navigateToHome(MouseEvent event) {
        try {
            loadNewScene("/Association/AllAssociation.fxml", event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleAssociationSubmenu(ActionEvent event) {
        isAssociationSubmenuVisible = !isAssociationSubmenuVisible;
        associationSubmenu.setVisible(isAssociationSubmenuVisible);
        associationSubmenu.setManaged(isAssociationSubmenuVisible);

        // Animation pour le sous-menu
        if (isAssociationSubmenuVisible) {
            associationSubmenu.setPrefHeight(80); // Hauteur pour 2 boutons
        } else {
            associationSubmenu.setPrefHeight(0);
        }
    }
    public void setMainController(MainLayoutController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void navigateToAssociationUser(ActionEvent event) {
        mainController.loadView("/Association/AssociationUserView.fxml");
    }

    @FXML
    private void navigateToAssociationAdmin(ActionEvent event) {

            mainController.loadView("/Association/AllAssociation.fxml");

    }

    @FXML
    private void navigateToDons(ActionEvent event) {
            mainController.loadView("/Don/ListDon.fxml");
    }

    @FXML
    private void navigateToReclamations(ActionEvent event) {

    }

    @FXML
    private void navigateToEncyclopedie(ActionEvent event) {

    }

    @FXML
    private void navigateToUsers(ActionEvent event) {

    }

    @FXML
    private void logOut(ActionEvent event) {

    }

    private void loadNewScene(String fxmlPath, Object event) throws IOException {
        // Charger la nouvelle interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Récupérer la scène actuelle
        Stage stage;
        if (event instanceof ActionEvent) {
            stage = (Stage) ((Node) ((ActionEvent) event).getSource()).getScene().getWindow();
        } else {
            stage = (Stage) ((Node) ((MouseEvent) event).getSource()).getScene().getWindow();
        }

        // Créer une nouvelle scène et la définir
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}