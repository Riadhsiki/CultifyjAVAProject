package Controllers.User;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class SidebarController {
    @FXML
    private VBox associationSubmenu;

    @FXML
    private Button btnAssociations;

    @FXML
    private ImageView logoImageView;

    // Flag to track submenu state
    private boolean isAssociationSubmenuOpen = false;

    @FXML
    void initialize() {
        System.out.println("SidebarController initialized");

        // Initialize the submenu state
        associationSubmenu.setVisible(false);
        associationSubmenu.setManaged(false);
    }

    @FXML
    void toggleAssociationSubmenu(ActionEvent event) {
        if (isAssociationSubmenuOpen) {
            // Close submenu
            associationSubmenu.setVisible(false);
            associationSubmenu.setManaged(false);
        } else {
            // Open submenu
            associationSubmenu.setVisible(true);
            associationSubmenu.setManaged(true);
        }

        // Toggle the state
        isAssociationSubmenuOpen = !isAssociationSubmenuOpen;
    }

    @FXML
    void navigateToHome(javafx.scene.input.MouseEvent event) {
        navigateTo("/UserInterface/Home.fxml", "Home", event);
    }

    @FXML
    void navigateToUserManagement(ActionEvent event) {
        navigateTo("/UserInterface/AfficherUsers.fxml", "User Management", event);
    }

    @FXML
    void navigateToEvents(ActionEvent event) {
        // Navigate to events view
        navigateTo("/Events/EventView.fxml", "Events", event);
    }

    @FXML
    void navigateToAssociationUser(ActionEvent event) {
        navigateTo("/Associations/UserAssociations.fxml", "User Associations", event);
    }

    @FXML
    void navigateToAssociationAdmin(ActionEvent event) {
        navigateTo("/Associations/AdminAssociations.fxml", "Admin Associations", event);
    }

    @FXML
    void navigateToDons(ActionEvent event) {
        navigateTo("/Dons/DonsView.fxml", "Donations", event);
    }

    @FXML
    void logOut(ActionEvent event) {
        try {
            // Navigate to login screen
            navigateTo("/Login/Login.fxml", "Login", event);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Logout Error", "Failed to log out: " + e.getMessage());
        }
    }

    // Helper method to navigate to different views
    private void navigateTo(String fxmlPath, String title, Object eventSource) {
        try {
            URL fxmlLocation = getClass().getResource(fxmlPath);

            if (fxmlLocation == null) {
                System.err.println("Cannot find FXML file at path: " + fxmlPath);
                showError("Navigation Error", "Could not find the requested view.");
                return;
            }

            // Load the new view
            Parent root = FXMLLoader.load(fxmlLocation);

            // Get the stage from the event source
            Stage stage;
            if (eventSource instanceof ActionEvent) {
                stage = (Stage) ((Node) ((ActionEvent) eventSource).getSource()).getScene().getWindow();
            } else if (eventSource instanceof javafx.scene.input.MouseEvent) {
                stage = (Stage) ((Node) ((javafx.scene.input.MouseEvent) eventSource).getSource()).getScene().getWindow();
            } else {
                throw new IllegalArgumentException("Unknown event source type");
            }

            // Create and set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Failed to load view: " + e.getMessage());
        }
    }

    // Helper method to show errors
    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}