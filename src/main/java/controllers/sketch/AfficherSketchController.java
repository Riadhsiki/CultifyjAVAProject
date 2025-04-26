package controllers.sketch;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import models.CultureSketch;
import services.sketch.CultureSketchService;

import java.io.IOException;
import java.util.*;

public class AfficherSketchController {

    @FXML
    private GridPane sketchGrid;

    @FXML
    private Button backButton;

    private final CultureSketchService sketchService;
    private final int currentUserId = 1; // Consistent with CultureSketchController
    private static final int THUMBNAIL_SIZE = 100; // Thumbnail size for display
    private static final int COLUMNS = 3; // Number of columns in GridPane

    public AfficherSketchController() {
        sketchService = new CultureSketchService();
    }

    @FXML
    private void initialize() {
        loadSketches();
    }

    private void loadSketches() {
        try {
            List<CultureSketch> sketches = sketchService.getByUserId(currentUserId);
            sketchGrid.getChildren().clear(); // Clear existing content

            for (int i = 0; i < sketches.size(); i++) {
                CultureSketch sketch = sketches.get(i);
                VBox sketchBox = createSketchBox(sketch);
                int row = i / COLUMNS;
                int col = i % COLUMNS;
                sketchGrid.add(sketchBox, col, row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load sketches: " + e.getMessage());
        }
    }

    private VBox createSketchBox(CultureSketch sketch) {
        VBox box = new VBox(5);
        box.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-padding: 10;");

        // Thumbnail
        ImageView thumbnailView = new ImageView(generateThumbnail(sketch));

        thumbnailView.setFitWidth(THUMBNAIL_SIZE);
        thumbnailView.setFitHeight(THUMBNAIL_SIZE);
        thumbnailView.setPreserveRatio(true);

        // Title
        Label titleLabel = new Label(sketch.getTitle() != null ? sketch.getTitle() : "Untitled");
        titleLabel.setStyle("-fx-font-weight: bold;");

        // Description
        Label descriptionLabel = new Label(
                sketch.getDescription() != null && !sketch.getDescription().isEmpty()
                        ? sketch.getDescription()
                        : "No description"
        );
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(THUMBNAIL_SIZE);

        // Public status
        Label publicLabel = new Label(sketch.isPublic() ? "Public" : "Private");
        publicLabel.setStyle(sketch.isPublic() ? "-fx-text-fill: green;" : "-fx-text-fill: red;");

        // Edit button
        Button editButton = new Button("Edit");
        editButton.setOnAction(event -> handleEditSketch(sketch));

        box.getChildren().addAll(thumbnailView, titleLabel, descriptionLabel, publicLabel, editButton);
        return box;
    }

    private Image generateThumbnail(CultureSketch sketch) {
        Canvas thumbnailCanvas = new Canvas(THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        GraphicsContext gc = thumbnailCanvas.getGraphicsContext2D();

        // Assume main canvas size (consistent with CultureSketchController)
        double mainCanvasWidth = 500;
        double mainCanvasHeight = 400;
        double scale = Math.min(THUMBNAIL_SIZE / mainCanvasWidth, THUMBNAIL_SIZE / mainCanvasHeight);

        // Clear with white background
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, THUMBNAIL_SIZE, THUMBNAIL_SIZE);

        // Draw shapes
        List<Map<String, Object>> shapes = sketch.getShapeData() != null ? sketch.getShapeData() : new ArrayList<>();
        List<String> colors = sketch.getColors() != null ? sketch.getColors() : new ArrayList<>();

        for (int i = 0; i < shapes.size(); i++) {
            Map<String, Object> shape = shapes.get(i);
            if (shape == null || !shape.containsKey("type")) continue;

            String colorStr = i < colors.size() ? colors.get(i) : "#000000";
            try {
                gc.setStroke(Color.web(colorStr));
            } catch (IllegalArgumentException e) {
                gc.setStroke(Color.BLACK);
            }

            String type = (String) shape.get("type");
            if (type == null) continue;

            switch (type.toLowerCase()) {
                case "line":
                    gc.strokeLine(
                            getDouble(shape, "x1") * scale,
                            getDouble(shape, "y1") * scale,
                            getDouble(shape, "x2") * scale,
                            getDouble(shape, "y2") * scale
                    );
                    break;
                case "rectangle":
                    double x = getDouble(shape, "x") * scale;
                    double y = getDouble(shape, "y") * scale;
                    double width = getDouble(shape, "width") * scale;
                    double height = getDouble(shape, "height") * scale;
                    gc.strokeRect(x, y, width, height);
                    break;
                case "circle":
                    double centerX = getDouble(shape, "centerX") * scale;
                    double centerY = getDouble(shape, "centerY") * scale;
                    double radius = getDouble(shape, "radius") * scale;
                    gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
                    break;
                case "freehand":
                    @SuppressWarnings("unchecked")
                    List<Number> points = (List<Number>) shape.get("points");
                    if (points != null && points.size() >= 4) {
                        for (int j = 0; j < points.size() - 3; j += 2) {
                            gc.strokeLine(
                                    points.get(j).doubleValue() * scale,
                                    points.get(j + 1).doubleValue() * scale,
                                    points.get(j + 2).doubleValue() * scale,
                                    points.get(j + 3).doubleValue() * scale
                            );
                        }
                    }
                    break;
            }
        }

        WritableImage writableImage = new WritableImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        thumbnailCanvas.snapshot(null, writableImage);
        return writableImage;
    }

    private double getDouble(Map<String, Object> shape, String key) {
        Object value = shape.get(key);
        return (value instanceof Number) ? ((Number) value).doubleValue() : 0.0;
    }

    private void handleEditSketch(CultureSketch sketch) {
        try {
            // Load the main CultureSketchController FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Sketch/AfficherSketch.fxml"));
            Parent root = loader.load();

            // Pass the selected sketch to CultureSketchController
            CultureSketchController controller = loader.getController();
            controller.loadSketch(sketch);
            // Switch to the main editing scene
            Stage stage = (Stage) sketchGrid.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Culture Sketch Editor");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open sketch editor: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            // Load the main CultureSketchController FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/sketch.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Culture Sketch Editor");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to return to main interface: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}