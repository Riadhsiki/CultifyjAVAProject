package controllers.sketch;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.CultureSketch;
import services.sketch.CultureSketchService;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class AfficherSketchController implements Initializable {

    @FXML
    private Button backButton;

    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private TextField searchField;

    @FXML
    private TabPane viewTabPane;

    @FXML
    private TilePane sketchTilePane;

    @FXML
    private TableView<CultureSketch> sketchTableView;

    @FXML
    private TableColumn<CultureSketch, ImageView> thumbnailColumn;

    @FXML
    private TableColumn<CultureSketch, String> titleColumn;

    @FXML
    private TableColumn<CultureSketch, String> descriptionColumn;

    @FXML
    private TableColumn<CultureSketch, Date> createdAtColumn;

    @FXML
    private TableColumn<CultureSketch, Boolean> publicColumn;

    @FXML
    private TableColumn<CultureSketch, CultureSketch> actionsColumn;

    @FXML
    private Button refreshButton;

    @FXML
    private VBox detailPane;

    @FXML
    private Label detailTitle;

    @FXML
    private ImageView detailImageView;

    @FXML
    private TextArea detailDescription;

    @FXML
    private Label detailCreatedAt;

    @FXML
    private CheckBox detailIsPublic;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button shareButton;

    @FXML
    private Button closeDetailButton;

    private final CultureSketchService sketchService;
    private ObservableList<CultureSketch> sketches;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
    private CultureSketch selectedSketch;
    private final int currentUserId = 1; // Mock user ID, replace with actual user ID logic

    public AfficherSketchController() {
        sketchService = new CultureSketchService();
        sketches = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize filter ComboBox
        filterComboBox.setItems(FXCollections.observableArrayList(
                "All Sketches", "My Sketches", "Public Only", "Most Recent", "Oldest First"
        ));
        filterComboBox.setValue("All Sketches");
        filterComboBox.setOnAction(this::handleFilterChange);

        // Initialize search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterSketches());

        // Setup table view
        setupTableView();

        // Set up buttons
        refreshButton.setOnAction(this::handleRefresh);
        backButton.setOnAction(this::handleBackToCanvas);
        editButton.setOnAction(this::handleEdit);
        deleteButton.setOnAction(this::handleDelete);
        shareButton.setOnAction(this::handleShare);
        closeDetailButton.setOnAction(event -> {
            detailPane.setVisible(false);
            detailPane.setManaged(false);
        });

        // Initialize the detail pane
        setupDetailPane();

        // Load sketches initially
        loadSketches();
    }

    private void setupTableView() {
        // Set up thumbnail column
        thumbnailColumn.setCellValueFactory(param -> {
            ImageView imageView = new ImageView(generateThumbnail(param.getValue()));
            imageView.setFitWidth(80);
            imageView.setFitHeight(60);
            imageView.setPreserveRatio(true);
            return new javafx.beans.property.SimpleObjectProperty<>(imageView);
        });

        // Set up other columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        publicColumn.setCellValueFactory(new PropertyValueFactory<>("isPublic"));

        // Format the created at column
        createdAtColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : dateFormat.format(item));
            }
        });

        // Format the public column
        publicColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item ? "Yes" : "No");
            }
        });

        // Setup actions column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                editBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                viewBtn.setOnAction(event -> {
                    CultureSketch sketch = getTableView().getItems().get(getIndex());
                    showDetailPane(sketch);
                });

                editBtn.setOnAction(event -> {
                    CultureSketch sketch = getTableView().getItems().get(getIndex());
                    handleEdit(event);
                });

                deleteBtn.setOnAction(event -> {
                    CultureSketch sketch = getTableView().getItems().get(getIndex());
                    handleDelete(event);
                });
            }

            @Override
            protected void updateItem(CultureSketch sketch, boolean empty) {
                super.updateItem(sketch, empty);
                setGraphic(empty || sketch == null ? null : new HBox(5, viewBtn, editBtn, deleteBtn));
            }
        });

        // Add row selection listener
        sketchTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        showDetailPane(newValue);
                    }
                });
    }

    private void setupDetailPane() {
        detailPane.setVisible(false);
        detailPane.setManaged(false);
    }

    private void loadSketches() {
        List<CultureSketch> allSketches = sketchService.getPublicSketches();
        allSketches.addAll(sketchService.getByUserId(currentUserId));

        // Remove duplicates
        Set<Integer> sketchIds = new HashSet<>();
        List<CultureSketch> uniqueSketches = new ArrayList<>();
        for (CultureSketch sketch : allSketches) {
            if (sketchIds.add(sketch.getId())) {
                uniqueSketches.add(sketch);
            }
        }

        sketches.setAll(uniqueSketches);
        filterSketches();
    }

    private void filterSketches() {
        String filter = filterComboBox.getValue();
        String searchText = searchField.getText().toLowerCase();

        List<CultureSketch> filteredList = new ArrayList<>();
        for (CultureSketch sketch : sketches) {
            boolean matchesFilter = true;
            boolean matchesSearch = true;

            // Apply filter
            switch (filter) {
                case "My Sketches":
                    matchesFilter = sketch.getUserId() == currentUserId;
                    break;
                case "Public Only":
                    matchesFilter = sketch.isPublic();
                    break;
                case "Most Recent":
                    filteredList.add(sketch);
                    filteredList.sort((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()));
                    matchesFilter = true;
                    break;
                case "Oldest First":
                    filteredList.add(sketch);
                    filteredList.sort(Comparator.comparing(CultureSketch::getCreatedAt));
                    matchesFilter = true;
                    break;
                case "All Sketches":
                default:
                    matchesFilter = true;
                    break;
            }

            // Apply search
            if (!searchText.isEmpty()) {
                matchesSearch = sketch.getTitle().toLowerCase().contains(searchText) ||
                        (sketch.getDescription() != null && sketch.getDescription().toLowerCase().contains(searchText));
            }

            if (matchesFilter && matchesSearch) {
                filteredList.add(sketch);
            }
        }

        updateTableView(filteredList);
        updateTilePane(filteredList);
    }

    private void updateTableView(List<CultureSketch> filteredSketches) {
        sketchTableView.setItems(FXCollections.observableArrayList(filteredSketches));
    }

    private void updateTilePane(List<CultureSketch> filteredSketches) {
        sketchTilePane.getChildren().clear();
        for (CultureSketch sketch : filteredSketches) {
            sketchTilePane.getChildren().add(createSketchCard(sketch));
        }
    }

    private VBox createSketchCard(CultureSketch sketch) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-radius: 5;");
        card.setEffect(new DropShadow(5, Color.gray(0, 0.2)));
        card.setPrefWidth(220);
        card.setMaxWidth(220);
        card.setMinHeight(240);

        Canvas previewCanvas = new Canvas(200, 150);
        renderSketchOnCanvas(previewCanvas, sketch);

        Label titleLabel = new Label(sketch.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: #4a86e8;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(200);

        Text descText = new Text(sketch.getDescription() != null ? sketch.getDescription() : "");
        descText.setWrappingWidth(200);
        descText.setFont(Font.font("System", 12));
        if (descText.getText().length() > 50) {
            descText.setText(descText.getText().substring(0, 47) + "...");
        }

        Label dateLabel = new Label(dateFormat.format(sketch.getCreatedAt()));
        dateLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #888888;");

        HBox statusBox = new HBox(5);
        Label visibilityLabel = new Label(sketch.isPublic() ? "Public" : "Private");
        visibilityLabel.setStyle(sketch.isPublic() ?
                "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 2 5;" :
                "-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-padding: 2 5;");
        statusBox.getChildren().add(visibilityLabel);

        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        viewBtn.setOnAction(event -> showDetailPane(sketch));

        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        editBtn.setOnAction(event -> handleEdit(event));

        HBox buttonBox = new HBox(10, viewBtn, editBtn);
        buttonBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(previewCanvas, titleLabel, descText, dateLabel, statusBox, buttonBox);
        card.setOnMouseClicked(event -> showDetailPane(sketch));

        return card;
    }

    private void renderSketchOnCanvas(Canvas canvas, CultureSketch sketch) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double scaleX = canvas.getWidth() / 500.0;
        double scaleY = canvas.getHeight() / 400.0;
        double scale = Math.min(scaleX, scaleY);

        List<Map<String, Object>> shapes = sketch.getShapeData() != null ? sketch.getShapeData() : new ArrayList<>();
        List<String> colors = sketch.getColors() != null ? sketch.getColors() : new ArrayList<>();

        for (int i = 0; i < shapes.size(); i++) {
            Map<String, Object> shape = shapes.get(i);
            String colorStr = i < colors.size() ? colors.get(i) : "#000000";
            try {
                gc.setStroke(Color.web(colorStr));
            } catch (IllegalArgumentException e) {
                gc.setStroke(Color.BLACK);
            }
            drawShapeOnCanvas(gc, shape, scale);
        }
    }

    private void drawShapeOnCanvas(GraphicsContext gc, Map<String, Object> shape, double scale) {
        String type = (String) shape.get("type");
        if (type == null) return;

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
                gc.strokeRect(
                        getDouble(shape, "x") * scale,
                        getDouble(shape, "y") * scale,
                        getDouble(shape, "width") * scale,
                        getDouble(shape, "height") * scale
                );
                break;
            case "circle":
                double radius = getDouble(shape, "radius") * scale;
                gc.strokeOval(
                        (getDouble(shape, "centerX") - radius) * scale,
                        (getDouble(shape, "centerY") - radius) * scale,
                        radius * 2,
                        radius * 2
                );
                break;
            case "freehand":
                @SuppressWarnings("unchecked")
                List<Number> points = (List<Number>) shape.get("points");
                if (points != null && points.size() >= 4) {
                    for (int i = 0; i < points.size() - 3; i += 2) {
                        gc.strokeLine(
                                points.get(i).doubleValue() * scale,
                                points.get(i + 1).doubleValue() * scale,
                                points.get(i + 2).doubleValue() * scale,
                                points.get(i + 3).doubleValue() * scale
                        );
                    }
                }
                break;
        }
    }

    private double getDouble(Map<String, Object> shape, String key) {
        Object value = shape.get(key);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
    }

    private void showDetailPane(CultureSketch sketch) {
        selectedSketch = sketch;
        detailTitle.setText(sketch.getTitle());
        detailDescription.setText(sketch.getDescription() != null ? sketch.getDescription() : "");
        detailCreatedAt.setText(dateFormat.format(sketch.getCreatedAt()));
        detailIsPublic.setSelected(sketch.isPublic());

        Canvas detailCanvas = new Canvas(270, 200);
        renderSketchOnCanvas(detailCanvas, sketch);

        WritableImage writableImage = new WritableImage(270, 200);
        detailCanvas.snapshot(null, writableImage);
        detailImageView.setImage(writableImage);

        detailPane.setVisible(true);
        detailPane.setManaged(true);
    }

    private Image generateThumbnail(CultureSketch sketch) {
        Canvas thumbnailCanvas = new Canvas(80, 60);
        renderSketchOnCanvas(thumbnailCanvas, sketch);

        WritableImage writableImage = new WritableImage(80, 60);
        thumbnailCanvas.snapshot(null, writableImage);
        return writableImage;
    }

    private void handleEdit(ActionEvent event) {
        if (selectedSketch != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/sketch/sketch.fxml"));
                Parent root = loader.load();
                CultureSketchController controller = loader.getController();
                controller.loadSketch(selectedSketch);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Edit Sketch - " + selectedSketch.getTitle());
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to open sketch editor: " + e.getMessage());
            }
        }
    }

    private void handleDelete(ActionEvent event) {
        if (selectedSketch != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Delete Sketch");
            alert.setContentText("Are you sure you want to delete '" + selectedSketch.getTitle() + "'?");

            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    sketchService.delete(selectedSketch.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Sketch deleted successfully");
                    loadSketches();
                    detailPane.setVisible(false);
                    detailPane.setManaged(false);
                }
            });
        }
    }

    private void handleShare(ActionEvent event) {
        if (selectedSketch != null) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Share Sketch");
            dialog.setHeaderText("Share '" + selectedSketch.getTitle() + "'");

            ButtonType sharePublicType = new ButtonType("Make Public", ButtonBar.ButtonData.OK_DONE);
            ButtonType exportType = new ButtonType("Export Image", ButtonBar.ButtonData.OTHER);
            ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(sharePublicType, exportType, cancelType);

            VBox content = new VBox(10);
            content.setPadding(new Insets(20, 10, 10, 10));

            Label infoLabel = new Label("Choose how you want to share this sketch:");

            HBox publicOption = new HBox(10);
            publicOption.setAlignment(Pos.CENTER_LEFT);
            RadioButton publicRadio = new RadioButton("Make sketch public");
            publicRadio.setSelected(!selectedSketch.isPublic());
            publicRadio.setDisable(selectedSketch.isPublic());
            Label publicLabel = new Label(selectedSketch.isPublic() ?
                    "This sketch is already public" :
                    "Others will be able to view this sketch");
            publicLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666666;");
            publicOption.getChildren().addAll(publicRadio, publicLabel);

            HBox exportOption = new HBox(10);
            exportOption.setAlignment(Pos.CENTER_LEFT);
            RadioButton exportRadio = new RadioButton("Export as image");
            Label exportLabel = new Label("Save sketch as PNG image file");
            exportLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666666;");
            exportOption.getChildren().addAll(exportRadio, exportLabel);

            ToggleGroup shareOptions = new ToggleGroup();
            publicRadio.setToggleGroup(shareOptions);
            exportRadio.setToggleGroup(shareOptions);

            if (selectedSketch.isPublic()) {
                exportRadio.setSelected(true);
            } else {
                publicRadio.setSelected(true);
            }

            content.getChildren().addAll(infoLabel, publicOption, exportOption);
            dialog.getDialogPane().setContent(content);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == sharePublicType && publicRadio.isSelected()) {
                    updateSketchVisibility(selectedSketch, true);
                } else if (dialogButton == sharePublicType && exportRadio.isSelected() || dialogButton == exportType) {
                    exportSketchAsImage(selectedSketch);
                }
                return dialogButton;
            });

            dialog.showAndWait();
        }
    }

    private void updateSketchVisibility(CultureSketch sketch, boolean isPublic) {
        if (sketch.isPublic() != isPublic) {
            sketch.setPublic(isPublic);
            sketchService.update(sketch);
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Sketch is now " + (isPublic ? "public" : "private"));
            loadSketches();
            if (selectedSketch != null && selectedSketch.getId() == sketch.getId()) {
                detailIsPublic.setSelected(isPublic);
                selectedSketch.setPublic(isPublic);
            }
        }
    }

    private void exportSketchAsImage(CultureSketch sketch) {
        Canvas exportCanvas = new Canvas(800, 600);
        renderSketchOnCanvas(exportCanvas, sketch);

        WritableImage writableImage = new WritableImage(800, 600);
        exportCanvas.snapshot(null, writableImage);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Sketch As Image");
        fileChooser.setInitialFileName(sketch.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + ".png");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Image", "*.png")
        );

        Stage stage = (Stage) refreshButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Sketch saved as image: " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to save image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleFilterChange(ActionEvent event) {
        filterSketches();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadSketches();
    }

    @FXML
    private void handleBackToCanvas(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sketch/sketch.fxml"));
            if (loader.getLocation() == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "FXML file not found: /Sketch/sketch.fxml");
                return;
            }
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Culture Sketch Editor");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to return to canvas: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}