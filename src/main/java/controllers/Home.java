package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Home extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML file from resources root
            Parent root = FXMLLoader.load(getClass().getResource("/auth/login.fxml"));

            // Create scene with default size
            Scene scene = new Scene(root, 600, 400);

            // Set up primary stage
            primaryStage.setTitle("User Management System");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("FATAL ERROR: Failed to load FXML file");
            System.err.println("Verify these locations exist:");

            e.printStackTrace();
            System.exit(1);
        }
    }
}