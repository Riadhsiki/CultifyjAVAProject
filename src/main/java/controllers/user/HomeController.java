package controllers.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class HomeController {
    @FXML
    private Button eventButton;

    @FXML
    private Button userManagementButton;

    @FXML
    void handleUserManagementButtonAction(ActionEvent event) {
        navigateTo("/UserInterface/AfficherUsers.fxml", "User Management", event);
    }

    @FXML
    void handleEventButtonAction(ActionEvent event) {
        navigateTo("/EventInterface/EventList.fxml", "Events", event);
    }

    // Helper method to navigate to different views
    private void navigateTo(String fxmlPath, String title, ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource(fxmlPath);

            if (fxmlLocation == null) {
                System.err.println("Cannot find FXML file at path: " + fxmlPath);
                showErrorAlert("Navigation Error", "Could not find the requested view.");
                return;
            }

            // Load the new view
            Parent root = FXMLLoader.load(fxmlLocation);

            // Get the stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Create and set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to load view: " + e.getMessage());
        }
    }

    // Helper method to show error alerts
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void initialize(){
        if (eventButton != null) {
        eventButton.setOnAction(this::handleEventButtonAction);
    }
        if (userManagementButton != null) {
        userManagementButton.setOnAction(this::handleUserManagementButtonAction);
    }
}
}