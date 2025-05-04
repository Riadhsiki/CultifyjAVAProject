package controllers;

import controllers.SideBar.SideBarController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class MainLayoutController {
    @FXML
    private HBox mainContainer;

    @FXML
    private AnchorPane contentPane;

    @FXML
    private AnchorPane sidebar; // Référence à la racine de la sidebar

    @FXML
    private SideBarController sidebarController; // Sera injecté via fx:id

    @FXML
    public void initialize() {
        try {
            // Charger la vue par défaut
            loadView("/Association/AllAssociation.fxml");

            // On doit récupérer explicitement le contrôleur de la sidebar
            FXMLLoader sidebarLoader = new FXMLLoader(getClass().getResource("/SideBar/SideBar.fxml"));
            AnchorPane sidebarPane = sidebarLoader.load();
            SideBarController sidebarCtrl = sidebarLoader.getController();

            // Informer le contrôleur sidebar du contrôleur principal
            sidebarCtrl.setMainController(this);

            // Remplacer la sidebar actuelle par celle nouvellement chargée
            mainContainer.getChildren().set(0, sidebarPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane view = loader.load();

            // Efface le contenu précédent et ajoute la nouvelle vue
            contentPane.getChildren().setAll(view);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}