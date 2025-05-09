package controllers;

import javafx.event.ActionEvent;
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

import java.io.IOException;

public class SidebarController {

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
        // Ensure submenu is hidden by default
        associationSubmenu.setVisible(false);
        associationSubmenu.setManaged(false);
        associationSubmenu.setPrefHeight(0);
    }

    @FXML
    private void navigateToHome(MouseEvent event) {
        loadScene("/Association/AllAssociation.fxml", event);
    }

    @FXML
    private void toggleAssociationSubmenu(ActionEvent event) {
        isAssociationSubmenuVisible = !isAssociationSubmenuVisible;
        associationSubmenu.setVisible(isAssociationSubmenuVisible);
        associationSubmenu.setManaged(isAssociationSubmenuVisible);

        // Smooth height transition for submenu
        if (isAssociationSubmenuVisible) {
            associationSubmenu.setPrefHeight(80); // Height for 2 submenu buttons
        } else {
            associationSubmenu.setPrefHeight(0);
        }
    }

    @FXML
    private void navigateToAssociationUser(ActionEvent event) {
        loadScene("/Association/AssociationUserView.fxml", event);
    }

    @FXML
    private void navigateToAssociationAdmin(ActionEvent event) {
        loadScene("/Association/AllAssociation.fxml", event);
    }

    @FXML
    private void navigateToDons(ActionEvent event) {
        loadScene("/Don/ListDon.fxml", event);
    }

    @FXML
    private void logOut(ActionEvent event) {
        // Placeholder for logout logic
        // For example, navigate to a login screen or clear session
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login/Login.fxml")); // Adjust path as needed
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Optionally show an alert
        }
    }

    private void loadScene(String fxmlPath, Object event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            Stage stage;
            if (event instanceof ActionEvent) {
                stage = (Stage) ((Node) ((ActionEvent) event).getSource()).getScene().getWindow();
            } else {
                stage = (Stage) ((Node) ((MouseEvent) event).getSource()).getScene().getWindow();
            }
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Optionally show an alert for error handling
        }
    }
}