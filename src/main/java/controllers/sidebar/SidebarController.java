package controllers.sidebar;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.auth.AuthenticationService;
import services.user.UserService;
import utils.SessionManager;
import java.io.IOException;
import java.sql.SQLException;

public class SidebarController {

    @FXML private ImageView logoImageView;
    @FXML private Button btnUserManagement;
    @FXML private VBox userManagementSubmenu;
    @FXML private Button btnAddUser;
    @FXML private Button btnUserList;
    @FXML private Button btnProfile;
    @FXML private Button btnBrowseArt;
    @FXML private Button btnCreateArt;
    @FXML private Button btnMyPortfolio;
    @FXML private Button btnAssociations;
    @FXML private VBox associationSubmenu;
    @FXML private Button btnAssociationUser;
    @FXML private Button btnAssociationAdmin;
    @FXML private Button btnDons;
    @FXML private Button btnEventAdmin;
    @FXML private Button btnEventUser;
    @FXML private Button btnManageReservations;
    @FXML private Button btnAddEvent;
    @FXML private Button btnReclamations;
    @FXML private VBox reclamationSubmenu;
    @FXML private Button btnReclamationList;
    @FXML private Button btnReponseList;
    @FXML private Button btnLogout;
    @FXML private Button btnQuit;

    private AuthenticationService authService;
    private UserService userService;
    private static final String LOGIN_FXML = "/auth/Login.fxml";
    private boolean isAdmin;

    @FXML
    public void initialize() {
        authService = AuthenticationService.getInstance();
        userService = new UserService();

        // Initialize submenus
        userManagementSubmenu.setVisible(false);
        userManagementSubmenu.setManaged(false);
        userManagementSubmenu.setPrefHeight(0);

        associationSubmenu.setVisible(false);
        associationSubmenu.setManaged(false);
        associationSubmenu.setPrefHeight(0);

        reclamationSubmenu.setVisible(false);
        reclamationSubmenu.setManaged(false);
        reclamationSubmenu.setPrefHeight(0);

        // Check user role and set button visibility
        String username = SessionManager.getInstance().getCurrentUsername();
        String sessionToken = SessionManager.getInstance().getSessionToken();
        System.out.println("Initializing Sidebar - Username: " + (username != null ? username : "null") +
                ", Session Token: " + (sessionToken != null ? sessionToken : "null"));

        if (username != null && SessionManager.getInstance().isLoggedIn()) {
            try {
                String role = userService.getRoleByUsername(username);
                System.out.println("User role: " + (role != null ? role : "null"));
                isAdmin = role != null && (role.equals("Admin") || role.contains("Admin"));

                // Set visibility for admin-specific buttons
                btnUserManagement.setVisible(isAdmin);
                btnUserManagement.setManaged(isAdmin);
                userManagementSubmenu.setVisible(false); // Always hidden initially
                btnAddUser.setVisible(isAdmin);
                btnAddUser.setManaged(isAdmin);
                btnUserList.setVisible(isAdmin);
                btnUserList.setManaged(isAdmin);

                btnAssociationAdmin.setVisible(isAdmin);
                btnAssociationAdmin.setManaged(isAdmin);

                btnEventAdmin.setVisible(isAdmin);
                btnEventAdmin.setManaged(isAdmin);
                btnManageReservations.setVisible(isAdmin);
                btnManageReservations.setManaged(isAdmin);

                btnReclamationList.setVisible(isAdmin);
                btnReclamationList.setManaged(isAdmin);
                btnReponseList.setVisible(isAdmin);
                btnReponseList.setManaged(isAdmin);

                System.out.println("Admin status: " + isAdmin + ", Button visibility set accordingly");
            } catch (SQLException e) {
                System.err.println("Error fetching user role: " + e.getMessage());
                showAlert("Error", "Failed to fetch user role: " + e.getMessage());
                navigateTo(LOGIN_FXML, "Login");
            }
        } else {
            System.out.println("No user logged in, redirecting to login");
            showLoginAlertAndRedirect();
        }

        SessionManager.getInstance().dumpPreferences();
        System.out.println("SidebarController initialized");
    }

    @FXML
    private void navigateToHome(MouseEvent event) {
        navigateTo("/home/Home.fxml", "Home");
    }

    @FXML
    private void toggleUserManagementSubmenu(ActionEvent event) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            showLoginAlertAndRedirect();
            return;
        }
        if (!isAdmin) {
            showAlert("Access Denied", "You do not have permission to access User Management.");
            return;
        }
        boolean isVisible = userManagementSubmenu.isVisible();
        userManagementSubmenu.setVisible(!isVisible);
        userManagementSubmenu.setManaged(!isVisible);
        userManagementSubmenu.setPrefHeight(isVisible ? 0 : 80);
        System.out.println("User Management submenu visibility toggled to: " + !isVisible);
    }

    @FXML
    private void toggleAssociationSubmenu(ActionEvent event) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            showLoginAlertAndRedirect();
            return;
        }
        String username = SessionManager.getInstance().getCurrentUsername();
        if (username == null) {
            showLoginAlertAndRedirect();
            return;
        }
        if (isAdmin) {
            boolean isVisible = associationSubmenu.isVisible();
            associationSubmenu.setVisible(!isVisible);
            associationSubmenu.setManaged(!isVisible);
            associationSubmenu.setPrefHeight(isVisible ? 0 : 80);
            System.out.println("Association submenu visibility toggled to: " + !isVisible);
        } else {
            navigateToAssociationUser(event);
        }
    }

    @FXML
    private void toggleReclamationSubmenu(ActionEvent event) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            showLoginAlertAndRedirect();
            return;
        }
        String username = SessionManager.getInstance().getCurrentUsername();
        if (username == null) {
            showLoginAlertAndRedirect();
            return;
        }
        if (isAdmin) {
            boolean isVisible = reclamationSubmenu.isVisible();
            reclamationSubmenu.setVisible(!isVisible);
            reclamationSubmenu.setManaged(!isVisible);
            reclamationSubmenu.setPrefHeight(isVisible ? 0 : 80);
            System.out.println("Reclamation submenu visibility toggled to: " + !isVisible);
        } else {
            navigateToReclamationUser(event);
        }
    }

    @FXML
    private void navigateToAddUser(ActionEvent event) {
        checkAuthAndNavigate("/userinterfaces/AjouterUser.fxml", "Add User", true);
    }

    @FXML
    private void navigateToUserList(ActionEvent event) {
        checkAuthAndNavigate("/userinterfaces/AfficherUsers.fxml", "User List", true);
    }

    @FXML
    private void navigateToProfile(ActionEvent event) {
        checkAuthAndNavigate("/auth/Profile.fxml", "My Profile", false);
    }

    @FXML
    private void navigateToBrowseArt(ActionEvent event) {
        checkAuthAndNavigate("/sketch/AfficherSketch.fxml", "Browse Art", false);
    }

    @FXML
    private void navigateToCreateArt(ActionEvent event) {
        checkAuthAndNavigate("/sketch/CultureSketch.fxml", "Create Art", false);
    }

    @FXML
    private void navigateToMyPortfolio(ActionEvent event) {
        checkAuthAndNavigate("/sketch/ArtView.fxml", "My Portfolio", false);
    }

    @FXML
    private void navigateToAssociationUser(ActionEvent event) {
        checkAuthAndNavigate("/Association/AssociationUserView.fxml", "Associations User", false);
    }

    @FXML
    private void navigateToAssociationAdmin(ActionEvent event) {
        checkAuthAndNavigate("/Association/AllAssociation.fxml", "Associations Admin", true);
    }

    @FXML
    private void navigateToDons(ActionEvent event) {
        checkAuthAndNavigate("/Don/ListDon.fxml", "Donations", false);
    }

    @FXML
    private void navigateToReclamationList(ActionEvent event) {
        checkAuthAndNavigate("/Reclamation/ReclamationListAdmin.fxml", "Reclamation List", true);
    }

    @FXML
    private void navigateToReponseList(ActionEvent event) {
        checkAuthAndNavigate("/Reponse/ReponseListAdmin.fxml", "Response List", true);
    }

    @FXML
    private void navigateToReclamationUser(ActionEvent event) {
        checkAuthAndNavigate("/Reclamation/ReclamationUserView.fxml", "Reclamations User", false);
    }

    @FXML
    private void navigateToEventAdmin(ActionEvent event) {
        checkAuthAndNavigate("/event/TableView.fxml", "Event Admin", true);
    }

    @FXML
    private void navigateToEventUser(ActionEvent event) {
        checkAuthAndNavigate("/event/CardView.fxml", "Event User", false);
    }

    @FXML
    private void navigateToAdminReservations(ActionEvent event) {
        checkAuthAndNavigate("/reservation/DetailReservation.fxml", "Manage Reservations", true);
    }

    @FXML
    private void navigateToAddEvent(ActionEvent event) {
        checkAuthAndNavigate("/event/AjouterEvent.fxml", "Add Event", false);
    }

    @FXML
    private void logOut(ActionEvent event) {
        String sessionToken = SessionManager.getInstance().getSessionToken();
        System.out.println("Logging out - Session Token: " + (sessionToken != null ? sessionToken : "null"));
        if (sessionToken != null) {
            authService.logout(sessionToken);
            SessionManager.getInstance().clearSession();
            System.out.println("Session cleared and logged out.");
        }
        navigateTo(LOGIN_FXML, "Login");
    }

    @FXML
    private void quit(ActionEvent event) {
        System.exit(0);
    }

    private void checkAuthAndNavigate(String fxmlPath, String title, boolean adminOnly) {
        String sessionToken = SessionManager.getInstance().getSessionToken();
        String username = SessionManager.getInstance().getCurrentUsername();
        boolean isLoggedIn = SessionManager.getInstance().isLoggedIn();
        System.out.println("Checking authentication for navigation to " + title +
                " - Session Token: " + (sessionToken != null ? sessionToken : "null") +
                ", Username: " + (username != null ? username : "null") +
                ", isLoggedIn: " + isLoggedIn);

        if (sessionToken == null || username == null || !isLoggedIn) {
            System.out.println("Authentication failed, redirecting to login.");
            showLoginAlertAndRedirect();
            return;
        }

        try {
            if (authService.getCurrentUser(sessionToken) == null) {
                System.out.println("authService.getCurrentUser returned null");
                showLoginAlertAndRedirect();
                return;
            }
            if (adminOnly && !isAdmin) {
                System.out.println("Access denied: User is not an admin for " + title);
                showAlert("Access Denied", "You do not have permission to access this page.");
                return;
            }
        } catch (SQLException e) {
            System.out.println("SQLException in authService: " + e.getMessage());
            showAlert("Error", "Database error during authentication: " + e.getMessage());
            return;
        }

        System.out.println("Authentication successful, navigating to " + title);
        navigateTo(fxmlPath, title);
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                System.out.println("FXML resource not found: " + fxmlPath);
                showAlert("Navigation Error", "Failed to load view: " + fxmlPath);
                return;
            }
            Parent root = loader.load();
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            Scene scene = new Scene(root, 900, 600);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.centerOnScreen();
            stage.show();
            System.out.println("Navigated to " + title);
            SessionManager.getInstance().dumpPreferences();
        } catch (IOException e) {
            System.out.println("IOException during navigation to " + fxmlPath + ": " + e.getMessage());
            showAlert("Navigation Error", "Failed to load view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showLoginAlertAndRedirect() {
        showAlert("Not Logged In", "You must be logged in to access this page.");
        navigateTo(LOGIN_FXML, "Login");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}