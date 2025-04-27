package controllers.sidebar;

import utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class SidebarController {
    public void navigateToHome(MouseEvent mouseEvent) {
    }

    public void toggleAssociationAdd(ActionEvent actionEvent) {
    }

    public void navigateToUsersList(ActionEvent actionEvent) {
    }

    public void toggleUserManagementSubmenu(ActionEvent actionEvent) {
    }

    public void navigateToAddUser(ActionEvent actionEvent) {
    }

    public void navigateToUserList(ActionEvent actionEvent) {
    }

    public void logOut(ActionEvent actionEvent) {
        try {
            // Clear session data
            SessionManager sessionManager = SessionManager.getInstance();
            sessionManager.clearSession();

            // Show logout confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Logout Successful");
            alert.setHeaderText(null);
            alert.setContentText("You have been successfully logged out.");
            alert.showAndWait();

            // Load the login view
            Parent loginParent = FXMLLoader.load(getClass().getResource("/Auth/login.fxml"));
            Scene loginScene = new Scene(loginParent);

            // Get the current stage (window)
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            // Set the new scene (login view)
            currentStage.setScene(loginScene);
            currentStage.centerOnScreen();

            // Set a temporary message for the login screen
            sessionManager.setTemporaryMessage("You have been successfully logged out.");

        } catch (IOException e) {
            // Handle any errors that occur during view loading
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Logout Failed");
            alert.setContentText("An error occurred while trying to log out: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void navigateToProfile(ActionEvent event) {
        try {
            // Load the profile view
            Parent profileParent = FXMLLoader.load(getClass().getResource("/Auth/Profile.fxml"));
            Scene profileScene = new Scene(profileParent);

            // Get the current stage
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the new scene
            currentStage.setScene(profileScene);

            // Optional: Pass the current user data to the profile controller
            // You would need to implement this based on your session management

        } catch (IOException e) {
            e.printStackTrace();
            // Handle error appropriately
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to load profile view");
            alert.setContentText("An error occurred: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
