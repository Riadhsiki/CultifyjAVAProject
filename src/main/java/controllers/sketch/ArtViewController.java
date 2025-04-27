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
import models.Comment;
import models.CultureSketch;
import models.Reaction;
import models.User;
import services.auth.AuthenticationService;
import services.comment.CommentService;
import services.reaction.ReactionService;
import services.sketch.CultureSketchService;
import utils.SessionManager;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ArtViewController implements Initializable {

    @FXML private Button backButton;
    @FXML private TabPane viewTabPane;
    @FXML private TilePane sketchTilePane;
    @FXML private TilePane publicSketchTilePane;
    @FXML private TableView<CultureSketch> sketchTableView;
    @FXML private TableColumn<CultureSketch, ImageView> thumbnailColumn;
    @FXML private TableColumn<CultureSketch, String> titleColumn;
    @FXML private TableColumn<CultureSketch, String> descriptionColumn;
    @FXML private TableColumn<CultureSketch, Date> createdAtColumn;
    @FXML private TableColumn<CultureSketch, Boolean> publicColumn;
    @FXML private TableColumn<CultureSketch, CultureSketch> actionsColumn;
    @FXML private Button refreshButton;
    @FXML private VBox detailPane;
    @FXML private Label detailTitle;
    @FXML private Label detailUsername;
    @FXML private ImageView detailImageView;
    @FXML private TextArea detailDescription;
    @FXML private Label detailCreatedAt;
    @FXML private CheckBox detailIsPublic;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button shareButton;
    @FXML private Button closeDetailButton;
    @FXML private HBox reactionBox;
    @FXML private Button likeButton;
    @FXML private Button loveButton;
    @FXML private Button ideaButton;
    @FXML private ScrollPane commentScrollPane;
    @FXML private VBox commentBox;
    @FXML private TextArea newCommentArea;
    @FXML private Button addCommentButton;

    private final CultureSketchService sketchService;
    private final AuthenticationService authService;
    private final CommentService commentService;
    private final ReactionService reactionService;
    private ObservableList<CultureSketch> sketches;
    private ObservableList<Object[]> publicSketches;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
    private CultureSketch selectedSketch;
    private User currentUser;

    public ArtViewController() {
        sketchService = new CultureSketchService();
        authService = AuthenticationService.getInstance();
        commentService = new CommentService();
        reactionService = new ReactionService();
        sketches = FXCollections.observableArrayList();
        publicSketches = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            String sessionToken = SessionManager.getInstance().getSessionToken();
            currentUser = authService.getCurrentUser(sessionToken);
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Authentication Error", "You must be logged in to view your portfolio.");
                navigateToLogin();
                return;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve current user: " + e.getMessage());
            navigateToLogin();
            return;
        }

        setupTableView();
        refreshButton.setOnAction(this::handleRefresh);
        backButton.setOnAction(this::handleBackButton);
        editButton.setOnAction(this::handleEdit);
        deleteButton.setOnAction(this::handleDelete);
        shareButton.setOnAction(this::handleShare);
        closeDetailButton.setOnAction(this::handleCloseDetail);
        likeButton.setOnAction(this::handleLike);
        loveButton.setOnAction(this::handleLove);
        ideaButton.setOnAction(this::handleIdea);
        addCommentButton.setOnAction(this::handleAddComment);
        setupDetailPane();
        loadSketches();
        loadPublicSketches();
    }

    private void setupTableView() {
        thumbnailColumn.setCellValueFactory(param -> {
            ImageView imageView = new ImageView(generateThumbnail(param.getValue()));
            imageView.setFitWidth(80);
            imageView.setFitHeight(60);
            imageView.setPreserveRatio(true);
            return new javafx.beans.property.SimpleObjectProperty<>(imageView);
        });

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        publicColumn.setCellValueFactory(new PropertyValueFactory<>("isPublic"));

        createdAtColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : dateFormat.format(item));
            }
        });

        publicColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item ? "Yes" : "No");
            }
        });

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
                    showDetailPane(sketch, null);
                });

                editBtn.setOnAction(event -> {
                    CultureSketch sketch = getTableView().getItems().get(getIndex());
                    selectedSketch = sketch;
                    handleEdit(event);
                });

                deleteBtn.setOnAction(event -> {
                    CultureSketch sketch = getTableView().getItems().get(getIndex());
                    selectedSketch = sketch;
                    handleDelete(event);
                });
            }

            @Override
            protected void updateItem(CultureSketch sketch, boolean empty) {
                super.updateItem(sketch, empty);
                setGraphic(empty || sketch == null ? null : new HBox(5, viewBtn, editBtn, deleteBtn));
            }
        });

        sketchTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        showDetailPane(newValue, null);
                    }
                });
    }

    private void setupDetailPane() {
        detailPane.setVisible(false);
        detailPane.setManaged(false);
        editButton.setVisible(currentUser != null);
        deleteButton.setVisible(currentUser != null);
    }

    private void loadSketches() {
        List<CultureSketch> userSketches = sketchService.getByUserId(currentUser.getId());
        sketches.setAll(userSketches);
        updateTableView(userSketches);
        updateTilePane(userSketches);
    }

    private void loadPublicSketches() {
        try {
            List<Object[]> publicSketchData = sketchService.getPublicSketchesWithUsernames();
            publicSketches.setAll(publicSketchData);
            updatePublicTilePane(publicSketchData);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load public sketches: " + e.getMessage());
        }
    }

    private void updateTableView(List<CultureSketch> filteredSketches) {
        sketchTableView.setItems(FXCollections.observableArrayList(filteredSketches));
    }

    private void updateTilePane(List<CultureSketch> filteredSketches) {
        sketchTilePane.getChildren().clear();
        for (CultureSketch sketch : filteredSketches) {
            sketchTilePane.getChildren().add(createSketchCard(sketch, null));
        }
    }

    private void updatePublicTilePane(List<Object[]> publicSketchData) {
        publicSketchTilePane.getChildren().clear();
        for (Object[] data : publicSketchData) {
            CultureSketch sketch = (CultureSketch) data[0];
            String username = (String) data[1];
            publicSketchTilePane.getChildren().add(createSketchCard(sketch, username));
        }
    }

    private VBox createSketchCard(CultureSketch sketch, String username) {
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

        Label usernameLabel = new Label(username != null ? "By: " + username : "");
        usernameLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        usernameLabel.setStyle("-fx-text-fill: #666666;");
        usernameLabel.setWrapText(true);
        usernameLabel.setMaxWidth(200);

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
        viewBtn.setOnAction(event -> showDetailPane(sketch, username));

        HBox buttonBox = new HBox(10, viewBtn);
        buttonBox.setAlignment(Pos.CENTER);

        if (currentUser != null && sketch.getUserId() == currentUser.getId()) {
            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
            editBtn.setOnAction(event -> {
                selectedSketch = sketch;
                handleEdit(event);
            });

            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            deleteBtn.setOnAction(event -> {
                selectedSketch = sketch;
                handleDelete(event);
            });

            buttonBox.getChildren().addAll(editBtn, deleteBtn);
        }

        card.getChildren().addAll(previewCanvas, titleLabel, usernameLabel, descText, dateLabel, statusBox, buttonBox);
        card.setOnMouseClicked(event -> showDetailPane(sketch, username));

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

    private void showDetailPane(CultureSketch sketch, String username) {
        selectedSketch = sketch;
        detailTitle.setText(sketch.getTitle());
        detailUsername.setText(username != null ? "By: " + username : "");
        detailDescription.setText(sketch.getDescription() != null ? sketch.getDescription() : "");
        detailCreatedAt.setText(dateFormat.format(sketch.getCreatedAt()));
        detailIsPublic.setSelected(sketch.isPublic());

        Canvas detailCanvas = new Canvas(270, 200);
        renderSketchOnCanvas(detailCanvas, sketch);

        WritableImage writableImage = new WritableImage(270, 200);
        detailCanvas.snapshot(null, writableImage);
        detailImageView.setImage(writableImage);

        boolean isOwner = currentUser != null && sketch.getUserId() == currentUser.getId();
        editButton.setVisible(isOwner);
        deleteButton.setVisible(isOwner);
        reactionBox.setVisible(currentUser != null);
        newCommentArea.setVisible(currentUser != null);
        addCommentButton.setVisible(currentUser != null);

        updateComments();
        updateReactions();

        detailPane.setVisible(true);
        detailPane.setManaged(true);
    }

    private void updateComments() {
        commentBox.getChildren().clear();
        try {
            List<Object[]> comments = commentService.getCommentsWithUserInfo(selectedSketch.getId());
            for (Object[] commentData : comments) {
                Comment comment = (Comment) commentData[0];
                String username = (String) commentData[1];
                VBox commentNode = createCommentNode(comment, username);
                commentBox.getChildren().add(commentNode);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load comments: " + e.getMessage());
        }
    }

    private VBox createCommentNode(Comment comment, String username) {
        VBox commentNode = new VBox(5);
        commentNode.setPadding(new Insets(5));
        commentNode.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #dddddd; -fx-border-radius: 5;");

        Label userLabel = new Label(username);
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a86e8;");

        Label contentLabel = new Label(comment.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(250);

        Label timestampLabel = new Label(dateFormat.format(comment.getCreatedAt()));
        timestampLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #888888;");

        commentNode.getChildren().addAll(userLabel, contentLabel, timestampLabel);
        return commentNode;
    }

    private void updateReactions() {
        try {
            Map<String, Integer> reactionCounts = reactionService.countReactionsByType(selectedSketch.getId());
            likeButton.setText("Like (" + reactionCounts.getOrDefault("LIKE", 0) + ")");
            loveButton.setText("Love (" + reactionCounts.getOrDefault("LOVE", 0) + ")");
            ideaButton.setText("Idea (" + reactionCounts.getOrDefault("IDEA", 0) + ")");

            if (currentUser != null) {
                List<Reaction> userReactions = reactionService.getUserReactionsForSketch(currentUser.getId(), selectedSketch.getId());
                likeButton.setDisable(userReactions.stream().anyMatch(r -> r.getReactionType().equals("LIKE")));
                loveButton.setDisable(userReactions.stream().anyMatch(r -> r.getReactionType().equals("LOVE")));
                ideaButton.setDisable(userReactions.stream().anyMatch(r -> r.getReactionType().equals("IDEA")));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load reactions: " + e.getMessage());
        }
    }

    private Image generateThumbnail(CultureSketch sketch) {
        Canvas thumbnailCanvas = new Canvas(80, 60);
        renderSketchOnCanvas(thumbnailCanvas, sketch);

        WritableImage writableImage = new WritableImage(80, 60);
        thumbnailCanvas.snapshot(null, writableImage);
        return writableImage;
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        if (selectedSketch != null && currentUser != null && selectedSketch.getUserId() == currentUser.getId()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/sketch/CultureSketch.fxml"));
                Parent root = loader.load();
                CultureSketchController controller = loader.getController();
                controller.loadSketch(selectedSketch);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Edit Sketch - " + selectedSketch.getTitle());
                stage.show();
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to open sketch editor: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedSketch != null && currentUser != null && selectedSketch.getUserId() == currentUser.getId()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Delete Sketch");
            alert.setContentText("Are you sure you want to delete '" + selectedSketch.getTitle() + "'?");

            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    sketchService.delete(selectedSketch.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Sketch deleted successfully");
                    loadSketches();
                    loadPublicSketches();
                    detailPane.setVisible(false);
                    detailPane.setManaged(false);
                }
            });
        }
    }

    @FXML
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

    @FXML
    private void handleLike(ActionEvent event) {
        addReaction("LIKE");
    }

    @FXML
    private void handleLove(ActionEvent event) {
        addReaction("LOVE");
    }

    @FXML
    private void handleIdea(ActionEvent event) {
        addReaction("IDEA");
    }

    private void addReaction(String reactionType) {
        if (selectedSketch != null && currentUser != null) {
            try {
                Reaction reaction = new Reaction(selectedSketch.getId(), currentUser.getId(), reactionType);
                reactionService.add(reaction);
                updateReactions();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add reaction: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAddComment(ActionEvent event) {
        if (selectedSketch != null && currentUser != null && !newCommentArea.getText().trim().isEmpty()) {
            try {
                Comment comment = new Comment(selectedSketch.getId(), currentUser.getId(), newCommentArea.getText().trim(), false);
                commentService.add(comment);
                newCommentArea.clear();
                updateComments();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add comment: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCloseDetail(ActionEvent event) {
        detailPane.setVisible(false);
        detailPane.setManaged(false);
    }

    private void updateSketchVisibility(CultureSketch sketch, boolean isPublic) {
        if (sketch.isPublic() != isPublic) {
            sketch.setPublic(isPublic);
            sketchService.update(sketch);
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Sketch is now " + (isPublic ? "public" : "private"));
            loadSketches();
            loadPublicSketches();
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
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to save image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadSketches();
        loadPublicSketches();
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        navigateTo("/sidebar/Sidebar.fxml", "Dashboard");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to " + title + ": " + e.getMessage());
        }
    }

    private void navigateToLogin() {
        navigateTo("/Auth/Login.fxml", "Login");
    }
}