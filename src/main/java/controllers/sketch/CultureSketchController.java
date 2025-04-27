package controllers.sketch;

import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.CultureSketch;
import models.User;
import services.auth.AuthenticationService;
import services.sketch.CultureSketchService;
import utils.SessionManager;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class CultureSketchController implements Initializable {

    @FXML
    private Canvas drawingCanvas;

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private ComboBox<String> shapeTypeComboBox;

    @FXML
    private ComboBox<String> entityTypeComboBox;

    @FXML
    private TextField entityIdField;

    @FXML
    private CheckBox isPublicCheckBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button clearButton;

    @FXML
    private ListView<CultureSketch> sketchListView;

    @FXML
    private Button exportButton;

    @FXML
    private Button shareButton;

    private final CultureSketchService sketchService;
    private final AuthenticationService authService;
    private GraphicsContext gc;
    private List<Map<String, Object>> currentShapes;
    private List<String> currentColors;
    private boolean isDrawing = false;
    private double startX, startY;
    private String currentShapeType = "line";
    private CultureSketch currentSketch;
    private User currentUser;

    public CultureSketchController() {
        sketchService = new CultureSketchService();
        authService = AuthenticationService.getInstance();
        currentShapes = new ArrayList<>();
        currentColors = new ArrayList<>();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get the current user
        try {
            String sessionToken = SessionManager.getInstance().getSessionToken();
            currentUser = authService.getCurrentUser(sessionToken);
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Authentication Error", "You must be logged in to create sketches.");
                navigateToLogin();
                return;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve current user: " + e.getMessage());
            navigateToLogin();
            return;
        }

        // Initialize canvas and graphics context
        gc = drawingCanvas.getGraphicsContext2D();
        setupCanvas();

        // Initialize shape type combo box
        shapeTypeComboBox.setItems(FXCollections.observableArrayList("Line", "Rectangle", "Circle", "Freehand"));
        shapeTypeComboBox.setValue("Line");
        shapeTypeComboBox.setOnAction(this::handleShapeTypeChange);

        // Initialize entity type combo box
        entityTypeComboBox.setItems(FXCollections.observableArrayList("Event", "Encyclopedia", "Donation", "Community", "None"));
        entityTypeComboBox.setValue("None");

        // Set default color
        colorPicker.setValue(Color.BLACK);
        colorPicker.setOnAction(this::handleColorChange);

        // Setup buttons
        saveButton.setOnAction(this::handleSave);
        clearButton.setOnAction(this::handleClear);
        exportButton.setOnAction(this::handleExport);
        shareButton.setOnAction(this::handleShare);

        // Configure sketch list view
        setupSketchListView();
        loadSketches();
    }

    private void setupCanvas() {
        // Use reasonable defaults if dimensions are not set in FXML
        double canvasWidth = drawingCanvas.getWidth() > 0 ? drawingCanvas.getWidth() : 500;
        double canvasHeight = drawingCanvas.getHeight() > 0 ? drawingCanvas.getHeight() : 400;

        // Set dimensions if not already set
        if (drawingCanvas.getWidth() <= 0) drawingCanvas.setWidth(canvasWidth);
        if (drawingCanvas.getHeight() <= 0) drawingCanvas.setHeight(canvasHeight);

        // Clear canvas with white background
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        // Setup canvas event handlers
        drawingCanvas.setOnMousePressed(this::handleMousePressed);
        drawingCanvas.setOnMouseDragged(this::handleMouseDragged);
        drawingCanvas.setOnMouseReleased(this::handleMouseReleased);
    }

    private void setupSketchListView() {
        sketchListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(CultureSketch sketch, boolean empty) {
                super.updateItem(sketch, empty);
                setText(empty || sketch == null ? null : sketch.getTitle());
            }
        });

        sketchListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        loadSketch(newValue);
                    }
                });

        // Add context menu for delete
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
            CultureSketch selectedSketch = sketchListView.getSelectionModel().getSelectedItem();
            if (selectedSketch != null) {
                deleteSketch(selectedSketch);
            }
        });
        contextMenu.getItems().add(deleteItem);
        sketchListView.setContextMenu(contextMenu);
    }

    private void loadSketches() {
        try {
            List<CultureSketch> userSketches = sketchService.getByUserId(currentUser.getId());
            sketchListView.setItems(FXCollections.observableArrayList(userSketches));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load sketches: " + e.getMessage());
        }
    }

    public void loadSketch(CultureSketch sketch) {
        currentSketch = sketch;
        titleField.setText(sketch.getTitle());
        descriptionArea.setText(sketch.getDescription());

        // Handle entity fields (not stored in model, just UI elements)
        entityTypeComboBox.setValue("None");
        entityIdField.clear();

        isPublicCheckBox.setSelected(sketch.isPublic());

        // Load shapes and colors
        currentShapes = new ArrayList<>(sketch.getShapeData());
        currentColors = new ArrayList<>(sketch.getColors());
        if (!currentColors.isEmpty()) {
            try {
                colorPicker.setValue(Color.web(currentColors.get(0)));
            } catch (IllegalArgumentException e) {
                colorPicker.setValue(Color.BLACK);
            }
        }

        // Redraw canvas
        redrawCanvas();
    }

    private void redrawCanvas() {
        // Clear canvas with white background
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());

        // Redraw all shapes
        for (int i = 0; i < currentShapes.size(); i++) {
            Map<String, Object> shape = currentShapes.get(i);
            String colorStr = i < currentColors.size() ? currentColors.get(i) : "#000000";
            try {
                gc.setStroke(Color.web(colorStr));
            } catch (IllegalArgumentException e) {
                gc.setStroke(Color.BLACK);
            }
            drawShape(shape);
        }
    }

    private void drawShape(Map<String, Object> shape) {
        String type = (String) shape.get("type");
        if (type == null) return;

        switch (type.toLowerCase()) {
            case "line":
                gc.strokeLine(
                        getDouble(shape, "x1"),
                        getDouble(shape, "y1"),
                        getDouble(shape, "x2"),
                        getDouble(shape, "y2")
                );
                break;
            case "rectangle":
                double x = getDouble(shape, "x");
                double y = getDouble(shape, "y");
                double width = getDouble(shape, "width");
                double height = getDouble(shape, "height");
                gc.strokeRect(x, y, width, height);
                break;
            case "circle":
                double centerX = getDouble(shape, "centerX");
                double centerY = getDouble(shape, "centerY");
                double radius = getDouble(shape, "radius");
                gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
                break;
            case "freehand":
                @SuppressWarnings("unchecked")
                List<Number> points = (List<Number>) shape.get("points");
                if (points != null && points.size() >= 4) {
                    for (int i = 0; i < points.size() - 3; i += 2) {
                        gc.strokeLine(
                                points.get(i).doubleValue(),
                                points.get(i + 1).doubleValue(),
                                points.get(i + 2).doubleValue(),
                                points.get(i + 3).doubleValue()
                        );
                    }
                }
                break;
        }
    }

    private double getDouble(Map<String, Object> shape, String key) {
        Object value = shape.get(key);
        return (value instanceof Number) ? ((Number) value).doubleValue() : 0.0;
    }

    private void handleMousePressed(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        if (x < 0 || x > drawingCanvas.getWidth() || y < 0 || y > drawingCanvas.getHeight()) return;

        isDrawing = true;
        startX = x;
        startY = y;

        if ("freehand".equalsIgnoreCase(currentShapeType)) {
            Map<String, Object> freehandShape = new HashMap<>();
            freehandShape.put("type", "freehand");
            List<Double> points = new ArrayList<>();
            points.add(startX);
            points.add(startY);
            freehandShape.put("points", points);
            currentShapes.add(freehandShape);
            currentColors.add(colorToHex(colorPicker.getValue()));
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (!isDrawing) return;

        double currentX = Math.max(0, Math.min(event.getX(), drawingCanvas.getWidth()));
        double currentY = Math.max(0, Math.min(event.getY(), drawingCanvas.getHeight()));

        redrawCanvas();
        gc.setStroke(colorPicker.getValue());

        switch (currentShapeType.toLowerCase()) {
            case "line":
                gc.strokeLine(startX, startY, currentX, currentY);
                break;
            case "rectangle":
                double width = currentX - startX;
                double height = currentY - startY;
                gc.strokeRect(startX, startY, width, height);
                break;
            case "circle":
                double radius = Math.sqrt(Math.pow(currentX - startX, 2) + Math.pow(currentY - startY, 2));
                gc.strokeOval(startX - radius, startY - radius, radius * 2, radius * 2);
                break;
            case "freehand":
                Map<String, Object> freehandShape = currentShapes.get(currentShapes.size() - 1);
                @SuppressWarnings("unchecked")
                List<Double> points = (List<Double>) freehandShape.get("points");
                points.add(currentX);
                points.add(currentY);

                int size = points.size();
                if (size >= 4) {
                    gc.strokeLine(
                            points.get(size - 4),
                            points.get(size - 3),
                            points.get(size - 2),
                            points.get(size - 1)
                    );
                }
                break;
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (!isDrawing) return;

        double endX = Math.max(0, Math.min(event.getX(), drawingCanvas.getWidth()));
        double endY = Math.max(0, Math.min(event.getY(), drawingCanvas.getHeight()));

        if (!"freehand".equalsIgnoreCase(currentShapeType)) {
            Map<String, Object> newShape = new HashMap<>();
            newShape.put("type", currentShapeType.toLowerCase());

            switch (currentShapeType.toLowerCase()) {
                case "line":
                    newShape.put("x1", startX);
                    newShape.put("y1", startY);
                    newShape.put("x2", endX);
                    newShape.put("y2", endY);
                    break;
                case "rectangle":
                    newShape.put("x", startX);
                    newShape.put("y", startY);
                    newShape.put("width", endX - startX);
                    newShape.put("height", endY - startY);
                    break;
                case "circle":
                    double radius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
                    newShape.put("centerX", startX);
                    newShape.put("centerY", startY);
                    newShape.put("radius", radius);
                    break;
            }

            currentShapes.add(newShape);
            currentColors.add(colorToHex(colorPicker.getValue()));
        }

        isDrawing = false;
        redrawCanvas();
    }

    private void handleShapeTypeChange(ActionEvent event) {
        currentShapeType = shapeTypeComboBox.getValue().toLowerCase();
    }

    private void handleColorChange(ActionEvent event) {
        // Update color picker value - will be used for next shape
    }

    private void handleSave(ActionEvent event) {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Title is required");
            return;
        }

        String description = descriptionArea.getText().trim();
        boolean isPublic = isPublicCheckBox.isSelected();

        if (currentShapes.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Cannot save an empty sketch");
            return;
        }

        try {
            if (currentSketch == null) {
                // Create new sketch
                currentSketch = new CultureSketch();
                currentSketch.setUserId(currentUser.getId());
                currentSketch.setTitle(title);
                currentSketch.setDescription(description);
                currentSketch.setColors(new ArrayList<>(currentColors));
                currentSketch.setShapeData(new ArrayList<>(currentShapes));
                currentSketch.setPublic(isPublic);

                Integer newId = sketchService.add(currentSketch);
                if (newId != null) {
                    currentSketch.setId(newId);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Sketch saved successfully");
                    loadSketches();
                } else {
                    throw new SQLException("Failed to save sketch: Database error");
                }
            } else {
                // Update existing sketch
                currentSketch.setTitle(title);
                currentSketch.setDescription(description);
                currentSketch.setColors(new ArrayList<>(currentColors));
                currentSketch.setShapeData(new ArrayList<>(currentShapes));
                currentSketch.setPublic(isPublic);

                if (sketchService.update(currentSketch)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Sketch updated successfully");
                    loadSketches();
                } else {
                    throw new SQLException("Failed to update sketch: Database error");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save sketch: " + e.getMessage());
        }
    }

    private void handleClear(ActionEvent event) {
        currentSketch = null;
        titleField.clear();
        descriptionArea.clear();
        entityTypeComboBox.setValue("None");
        entityIdField.clear();
        isPublicCheckBox.setSelected(false);
        currentShapes.clear();
        currentColors.clear();
        setupCanvas();
    }

    private void handleExport(ActionEvent event) {
        if (currentShapes.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Cannot export an empty canvas");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Sketch as PNG");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Files", "*.png")
        );
        fileChooser.setInitialFileName(
                (currentSketch != null && !currentSketch.getTitle().isEmpty())
                        ? currentSketch.getTitle() + ".png"
                        : "sketch.png"
        );

        File file = fileChooser.showSaveDialog(drawingCanvas.getScene().getWindow());
        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage(
                        (int) drawingCanvas.getWidth(),
                        (int) drawingCanvas.getHeight()
                );
                drawingCanvas.snapshot(null, writableImage);
                ImageIO.write(
                        SwingFXUtils.fromFXImage(writableImage, null),
                        "png",
                        file
                );
                showAlert(Alert.AlertType.INFORMATION, "Success", "Sketch exported successfully to " + file.getName());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to export sketch: " + e.getMessage());
            }
        }
    }

    private void handleShare(ActionEvent event) {
        if (currentSketch == null || currentSketch.getId() == 0) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please save the sketch before sharing");
            return;
        }

        // Simulate generating a shareable link (placeholder for actual implementation)
        String shareLink = String.format("http://culturesketch.studio/sketches/%d", currentSketch.getId());
        TextInputDialog dialog = new TextInputDialog(shareLink);
        dialog.setTitle("Share Sketch");
        dialog.setHeaderText("Share your sketch");
        dialog.setContentText("Copy the link below to share your sketch:");
        dialog.getEditor().setEditable(false);
        dialog.showAndWait();

        showAlert(Alert.AlertType.INFORMATION, "Share", "Share link generated. In a real application, this would be sent via email or social media.");
    }

    private void deleteSketch(CultureSketch sketch) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Sketch");
        alert.setContentText("Are you sure you want to delete '" + sketch.getTitle() + "'?");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    if (sketchService.delete(sketch.getId())) {
                        loadSketches();
                        if (currentSketch != null && sketch.getId() == currentSketch.getId()) {
                            handleClear(null);
                        }
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Sketch deleted successfully");
                    } else {
                        throw new SQLException("Failed to delete sketch: Database error");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete sketch: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    @FXML
    private void handleViewAllSketches(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sketch/afficherSketch.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("View All Sketches");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open all sketches view: " + e.getMessage());
        }
    }

    private WritableImage generateThumbnail(CultureSketch sketch) {
        // Create a small off-screen canvas for the thumbnail
        Canvas thumbnailCanvas = new Canvas(50, 50);
        GraphicsContext thumbnailGc = thumbnailCanvas.getGraphicsContext2D();

        // Scale the sketch to fit the thumbnail
        double scaleX = 50.0 / drawingCanvas.getWidth();
        double scaleY = 50.0 / drawingCanvas.getHeight();
        double scale = Math.min(scaleX, scaleY);

        // Clear thumbnail canvas with white background
        thumbnailGc.setFill(Color.WHITE);
        thumbnailGc.fillRect(0, 0, 50, 50);

        // Draw scaled shapes
        List<Map<String, Object>> shapes = sketch.getShapeData() != null ? sketch.getShapeData() : new ArrayList<>();
        List<String> colors = sketch.getColors() != null ? sketch.getColors() : new ArrayList<>();

        for (int i = 0; i < shapes.size(); i++) {
            Map<String, Object> shape = shapes.get(i);
            if (shape == null || !shape.containsKey("type")) continue;

            String colorStr = i < colors.size() ? colors.get(i) : "#000000";
            try {
                thumbnailGc.setStroke(Color.web(colorStr));
            } catch (IllegalArgumentException e) {
                thumbnailGc.setStroke(Color.BLACK);
            }

            String type = (String) shape.get("type");
            if (type == null) continue;

            switch (type.toLowerCase()) {
                case "line":
                    thumbnailGc.strokeLine(
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
                    thumbnailGc.strokeRect(x, y, width, height);
                    break;
                case "circle":
                    double centerX = getDouble(shape, "centerX") * scale;
                    double centerY = getDouble(shape, "centerY") * scale;
                    double radius = getDouble(shape, "radius") * scale;
                    thumbnailGc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
                    break;
                case "freehand":
                    @SuppressWarnings("unchecked")
                    List<Number> points = (List<Number>) shape.get("points");
                    if (points != null && points.size() >= 4) {
                        for (int j = 0; j < points.size() - 3; j += 2) {
                            thumbnailGc.strokeLine(
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
        WritableImage writableImage = new WritableImage(50, 50);
        thumbnailCanvas.snapshot(null, writableImage);
        return writableImage;
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Auth/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) drawingCanvas.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to login: " + e.getMessage());
        }
    }
}