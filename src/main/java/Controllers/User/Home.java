package Controllers.User;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Home extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load FXML with multiple fallback paths
        URL fxmlResource = getClass().getResource("/UserInterface/Home.fxml");

        if (fxmlResource == null) {
            System.out.println("Couldn't find /UserInterface/Home.fxml, trying alternative paths...");
            fxmlResource = getClass().getResource("Home.fxml");

            if (fxmlResource == null) {
                throw new IOException("Fatal Error: Could not find Home.fxml in any of the searched paths.\n" +
                        "Please ensure the file exists in one of these locations:\n" +
                        "1. src/main/resources/UserInterface/Home.fxml\n" +
                        "2. src/main/resources/Home.fxml");
            }
        }

        System.out.println("Found FXML at: " + fxmlResource);

        // Load the FXML
        FXMLLoader loader = new FXMLLoader(fxmlResource);
        Parent root = loader.load();

        // Create scene with default size
        Scene scene = new Scene(root, 1050, 800);

        // Load CSS files (with fallbacks)
        loadCSS(scene, "/SideBar/styles.css", "Sidebar CSS");
        loadCSS(scene, "/Styles.css", "Main CSS");

        // Configure stage
        primaryStage.setTitle("Art & Cultural Experiences");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    private void loadCSS(Scene scene, String path, String cssName) {
        URL cssResource = getClass().getResource(path);
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
            System.out.println("Successfully loaded " + cssName);
        } else {
            System.out.println("Warning: Could not find " + cssName + " at " + path);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}