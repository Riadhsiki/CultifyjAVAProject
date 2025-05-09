package controllers.dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import utils.SessionManager;

public class DashboardController {
    @FXML
    private Label lblWelcome;
    @FXML
    private Label lblUserInfo;

    private SessionManager sessionManager;

    @FXML
    public void initialize() {
        sessionManager = SessionManager.getInstance();
        updateDashboardContent();
    }

    private void updateDashboardContent() {
        String username = sessionManager.getCurrentUsername();
        String role = sessionManager.getUserRole();
        if (sessionManager.isLoggedIn() && username != null && !username.isEmpty()) {
            lblWelcome.setText("Welcome to Cultify Dashboard, " + username + "!");
            lblUserInfo.setText("Role: " + (role != null && !role.isEmpty() ? role : "User"));
        } else {
            lblWelcome.setText("Welcome to Cultify Dashboard!");
            lblUserInfo.setText("Role: Guest");
        }
    }
}