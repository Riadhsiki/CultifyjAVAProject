package controllers.sketch;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
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

public class AfficherSketchController implements Initializable {

    @FXML private Button backButton;
    @FXML private TextField searchField;
    @FXML private TilePane sketchTilePane;
    @FXML private Button refreshButton;
    @FXML private Button profileButton;
    @FXML private Button viewDetailsButton;
    @FXML private VBox detailPane;
    @FXML private Label detailTitle;
    @FXML private ImageView detailImageView;
    @FXML private TextArea detailDescription;
    @FXML private Label detailCreatedAt;
    @FXML private CheckBox detailIsPublic;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button shareButton;
    @FXML private Button closeDetailButton;
    @FXML private Button likeButton;
    @FXML private Button loveButton;
    @FXML private Label reactionCountLabel;
    @FXML private TextArea commentInput;
    @FXML private Button submitCommentButton;
    @FXML private VBox commentList;
    @FXML private Label commentCountLabel;

    private final CultureSketchService sketchService;
    private final AuthenticationService authService;
    private final CommentService commentService;
    private final ReactionService reactionService;
    private List<CultureSketch> sketches;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
    private final ObjectProperty<CultureSketch> selectedSketchProperty = new SimpleObjectProperty<>();
    private User currentUser;

    public AfficherSketchController() {
        sketchService = new CultureSketchService();
        authService = AuthenticationService.getInstance();
        commentService = new CommentService();
        reactionService = new ReactionService();
        sketches = new ArrayList<>();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing AfficherSketchController");
        try {
            String sessionToken = SessionManager.getInstance().getSessionToken();
            currentUser = authService.getCurrentUser(sessionToken);
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Authentication Error", "You must be logged in to view sketches.");
                navigateToLogin();
                return;
            }
            System.out.println("Current user: " + currentUser.getUsername());
        } catch (SQLException e) {
            System.out.println("SQLException in initialize: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve current user: " + e.getMessage());
            navigateToLogin();
            return;
        }

        // Initialize search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterSketches());

        // Set up buttons
        refreshButton.setOnAction(this::handleRefresh);
        backButton.setOnAction(this::handleBackToCanvas);
        profileButton.setOnAction(this::navigateToProfile);
        viewDetailsButton.setOnAction(this::viewSelectedSketchDetails);
        editButton.setOnAction(this::handleEdit);
        deleteButton.setOnAction(this::handleDelete);
        shareButton.setOnAction(this::handleShare);
        closeDetailButton.setOnAction(this::closeDetailPane);
        submitCommentButton.setOnAction(this::handleSubmitComment);
        likeButton.setOnAction(this::handleLikeReaction);
        loveButton.setOnAction(this::handleLoveReaction);

        // Enable/disable viewDetailsButton based on selection
        viewDetailsButton.setDisable(true);
        selectedSketchProperty.addListener((obs, old, newValue) -> {
            viewDetailsButton.setDisable(newValue == null);
            if (newValue != null) {
                showDetailPane(newValue);
            } else {
                detailPane.setVisible(false);
                detailPane.setManaged(false);
            }
        });

        // Initialize the detail pane
        setupDetailPane();

        // Load public sketches initially
        try {
            loadSketches();
        } catch (SQLException e) {
            System.out.println("SQLException in loadSketches: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load sketches: " + e.getMessage());
        }
    }

    private void setupDetailPane() {
        detailPane.setVisible(false);
        detailPane.setManaged(false);
    }

    private void loadSketches() throws SQLException {
        sketches.clear();
        sketches = sketchService.getPublicSketches();
        System.out.println("Loaded " + sketches.size() + " public sketches");
        filterSketches();
    }

    private void filterSketches() {
        String searchText = searchField.getText().toLowerCase();
        List<CultureSketch> filteredList = new ArrayList<>();

        for (CultureSketch sketch : sketches) {
            boolean matchesSearch = true;
            if (!searchText.isEmpty()) {
                matchesSearch = sketch.getTitle().toLowerCase().contains(searchText) ||
                        (sketch.getDescription() != null && sketch.getDescription().toLowerCase().contains(searchText));
            }
            if (matchesSearch) {
                filteredList.add(sketch);
            }
        }
        System.out.println("Filtered to " + filteredList.size() + " public sketches with search: " + searchText);
        updateTilePane(filteredList);
    }

    private void updateTilePane(List<CultureSketch> filteredSketches) {
        sketchTilePane.getChildren().clear();
        System.out.println("Updating tile pane with " + filteredSketches.size() + " sketches");
        if (filteredSketches.isEmpty()) {
            Label noSketchesLabel = new Label("No public sketches available.");
            noSketchesLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 14;");
            sketchTilePane.getChildren().add(noSketchesLabel);
        } else {
            for (CultureSketch sketch : filteredSketches) {
                sketchTilePane.getChildren().add(createSketchCard(sketch));
            }
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
        Label visibilityLabel = new Label("Public");
        visibilityLabel.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 2 5;");
        statusBox.getChildren().add(visibilityLabel);

        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        viewBtn.setOnAction(event -> {
            selectedSketchProperty.set(sketch);
        });

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().add(viewBtn);
        if (sketch.getUserId() == currentUser.getId()) {
            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
            editBtn.setOnAction(event -> {
                selectedSketchProperty.set(sketch);
                handleEdit(event);
            });

            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            deleteBtn.setOnAction(event -> {
                selectedSketchProperty.set(sketch);
                handleDelete(event);
            });

            buttonBox.getChildren().addAll(editBtn, deleteBtn);
        }
        buttonBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(previewCanvas, titleLabel, descText, dateLabel, statusBox, buttonBox);
        card.setOnMouseClicked(event -> {
            selectedSketchProperty.set(sketch);
        });

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
                System.out.println("Invalid color format: " + colorStr + " for sketch ID: " + sketch.getId());
                gc.setStroke(Color.BLACK);
            }
            drawShapeOnCanvas(gc, shape, scale);
        }
    }

    private void drawShapeOnCanvas(GraphicsContext gc, Map<String, Object> shape, double scale) {
        String type = (String) shape.get("type");
        if (type == null) {
            System.out.println("Shape type is null");
            return;
        }

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
            default:
                System.out.println("Unknown shape type: " + type);
        }
    }

    private double getDouble(Map<String, Object> shape, String key) {
        Object value = shape.get(key);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
    }

    private void showDetailPane(CultureSketch sketch) {
        System.out.println("Showing detail pane for sketch: " + sketch.getTitle());
        detailTitle.setText(sketch.getTitle());
        detailDescription.setText(sketch.getDescription() != null ? sketch.getDescription() : "");
        detailCreatedAt.setText(dateFormat.format(sketch.getCreatedAt()));
        detailIsPublic.setSelected(true);

        Canvas detailCanvas = new Canvas(270, 200);
        renderSketchOnCanvas(detailCanvas, sketch);
        WritableImage writableImage = new WritableImage(270, 200);
        detailCanvas.snapshot(null, writableImage);
        detailImageView.setImage(writableImage);

        boolean isOwnSketch = sketch.getUserId() == currentUser.getId();
        editButton.setVisible(isOwnSketch);
        editButton.setManaged(isOwnSketch);
        deleteButton.setVisible(isOwnSketch);
        deleteButton.setManaged(isOwnSketch);

        updateReactions(sketch);
        updateComments(sketch);

        detailPane.setVisible(true);
        detailPane.setManaged(true);
    }

    private void updateReactions(CultureSketch sketch) {
        try {
            // Update reaction counts
            Map<String, Integer> reactionCounts = reactionService.countReactionsByType(sketch.getId());
            int totalReactions = reactionService.getTotalReactionsCount(sketch.getId());
            StringBuilder reactionText = new StringBuilder();
            if (totalReactions > 0) {
                List<String> parts = new ArrayList<>();
                if (reactionCounts.containsKey("Like")) {
                    parts.add(reactionCounts.get("Like") + " Like" + (reactionCounts.get("Like") > 1 ? "s" : ""));
                }
                if (reactionCounts.containsKey("Love")) {
                    parts.add(reactionCounts.get("Love") + " Love" + (reactionCounts.get("Love") > 1 ? "s" : ""));
                }
                reactionText.append(String.join(", ", parts));
            } else {
                reactionText.append("No Reactions");
            }
            reactionCountLabel.setText(totalReactions + " Reaction" + (totalReactions != 1 ? "s" : "") + (totalReactions > 0 ? ": " + reactionText : ""));

            // Update button states
            List<Reaction> userReactions = reactionService.getUserReactionsForSketch(currentUser.getId(), sketch.getId());
            boolean hasLiked = userReactions.stream().anyMatch(r -> r.getReactionType().equals("Like"));
            boolean hasLoved = userReactions.stream().anyMatch(r -> r.getReactionType().equals("Love"));
            likeButton.setStyle(hasLiked ?
                    "-fx-background-color: #1e88e5; -fx-text-fill: white;" :
                    "-fx-background-color: #3498db; -fx-text-fill: white;");
            loveButton.setStyle(hasLoved ?
                    "-fx-background-color: #c2185b; -fx-text-fill: white;" :
                    "-fx-background-color: #e91e63; -fx-text-fill: white;");
        } catch (SQLException e) {
            System.out.println("SQLException in updateReactions: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load reactions: " + e.getMessage());
        }
    }

    private void updateComments(CultureSketch sketch) {
        try {
            commentList.getChildren().clear();
            List<Object[]> comments = commentService.getCommentsWithUserInfo(sketch.getId());
            int commentCount = commentService.countCommentsBySketchId(sketch.getId());
            commentCountLabel.setText(commentCount + " Comment" + (commentCount != 1 ? "s" : ""));

            for (Object[] commentData : comments) {
                Comment comment = (Comment) commentData[0];
                String username = (String) commentData[1];
                commentList.getChildren().add(createCommentNode(comment, username));
            }
        } catch (SQLException e) {
            System.out.println("SQLException in updateComments: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load comments: " + e.getMessage());
        }
    }

    private Node createCommentNode(Comment comment, String username) {
        VBox commentNode = new VBox(5);
        commentNode.setPadding(new Insets(5));
        commentNode.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #dddddd; -fx-border-radius: 5;");

        HBox header = new HBox(10);
        Label usernameLabel = new Label(username);
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a86e8;");
        Label timeLabel = new Label(dateFormat.format(comment.getCreatedAt()));
        timeLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #888888;");
        header.getChildren().addAll(usernameLabel, timeLabel);

        Text contentText = new Text(comment.getContent());
        contentText.setWrappingWidth(250);
        contentText.setStyle("-fx-font-size: 12;");

        HBox actionBox = new HBox(5);
        if (comment.getUserId() == currentUser.getId()) {
            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 10;");
            editBtn.setOnAction(event -> handleEditComment(comment));

            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10;");
            deleteBtn.setOnAction(event -> handleDeleteComment(comment));

            actionBox.getChildren().addAll(editBtn, deleteBtn);
        }

        commentNode.getChildren().addAll(header, contentText, actionBox);
        return commentNode;
    }

    @FXML
    private void handleLikeReaction(ActionEvent event) {
        CultureSketch selectedSketch = selectedSketchProperty.get();
        if (selectedSketch != null) {
            try {
                List<Reaction> userReactions = reactionService.getUserReactionsForSketch(currentUser.getId(), selectedSketch.getId());
                boolean hasLiked = userReactions.stream().anyMatch(r -> r.getReactionType().equals("Like"));
                if (hasLiked) {
                    reactionService.deleteUserReaction(currentUser.getId(), selectedSketch.getId(), "Like");
                } else {
                    Reaction reaction = new Reaction();
                    reaction.setSketchId(selectedSketch.getId());
                    reaction.setUserId(currentUser.getId());
                    reaction.setReactionType("Like");
                    reactionService.add(reaction);
                }
                updateReactions(selectedSketch);
            } catch (SQLException e) {
                System.out.println("SQLException in handleLikeReaction: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update reaction: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleLoveReaction(ActionEvent event) {
        CultureSketch selectedSketch = selectedSketchProperty.get();
        if (selectedSketch != null) {
            try {
                List<Reaction> userReactions = reactionService.getUserReactionsForSketch(currentUser.getId(), selectedSketch.getId());
                boolean hasLoved = userReactions.stream().anyMatch(r -> r.getReactionType().equals("Love"));
                if (hasLoved) {
                    reactionService.deleteUserReaction(currentUser.getId(), selectedSketch.getId(), "Love");
                } else {
                    Reaction reaction = new Reaction();
                    reaction.setSketchId(selectedSketch.getId());
                    reaction.setUserId(currentUser.getId());
                    reaction.setReactionType("Love");
                    reactionService.add(reaction);
                }
                updateReactions(selectedSketch);
            } catch (SQLException e) {
                System.out.println("SQLException in handleLoveReaction: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update reaction: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSubmitComment(ActionEvent event) {
        CultureSketch selectedSketch = selectedSketchProperty.get();
        if (selectedSketch != null && !commentInput.getText().trim().isEmpty()) {
            try {
                Comment comment = new Comment();
                comment.setSketchId(selectedSketch.getId());
                comment.setUserId(currentUser.getId());
                comment.setContent(commentInput.getText().trim());
                comment.setIsEdited(false);
                commentService.add(comment);
                commentInput.clear();
                updateComments(selectedSketch);
            } catch (SQLException e) {
                System.out.println("SQLException in handleSubmitComment: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to post comment: " + e.getMessage());
            }
        }
    }

    private void handleEditComment(Comment comment) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Comment");
        dialog.setHeaderText("Edit your comment");

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, cancelType);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 10, 10, 10));
        TextArea editArea = new TextArea(comment.getContent());
        editArea.setWrapText(true);
        editArea.setPrefHeight(100);
        content.getChildren().add(editArea);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveType && !editArea.getText().trim().isEmpty()) {
                try {
                    comment.setContent(editArea.getText().trim());
                    comment.setIsEdited(true);
                    commentService.update(comment);
                    updateComments(selectedSketchProperty.get());
                } catch (SQLException e) {
                    System.out.println("SQLException in handleEditComment: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to edit comment: " + e.getMessage());
                }
            }
            return dialogButton;
        });

        dialog.showAndWait();
    }

    private void handleDeleteComment(Comment comment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Comment");
        alert.setContentText("Are you sure you want to delete this comment?");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    commentService.delete(comment);
                    updateComments(selectedSketchProperty.get());
                } catch (SQLException e) {
                    System.out.println("SQLException in handleDeleteComment: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete comment: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        CultureSketch selectedSketch = selectedSketchProperty.get();
        if (selectedSketch != null && selectedSketch.getUserId() == currentUser.getId()) {
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
                System.out.println("IOException in handleEdit: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to open sketch editor: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        CultureSketch selectedSketch = selectedSketchProperty.get();
        if (selectedSketch != null && selectedSketch.getUserId() == currentUser.getId()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Delete Sketch");
            alert.setContentText("Are you sure you want to delete '" + selectedSketch.getTitle() + "'?");

            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    try {
                        sketchService.delete(selectedSketch.getId());
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Sketch deleted successfully");
                        loadSketches();
                        detailPane.setVisible(false);
                        detailPane.setManaged(false);
                        selectedSketchProperty.set(null);
                    } catch (SQLException e) {
                        System.out.println("SQLException in handleDelete: " + e.getMessage());
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete sketch: " + e.getMessage());
                    }
                }
            });
        }
    }

    @FXML
    private void handleShare(ActionEvent event) {
        CultureSketch selectedSketch = selectedSketchProperty.get();
        if (selectedSketch != null) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Share Sketch");
            dialog.setHeaderText("Share '" + selectedSketch.getTitle() + "'");

            ButtonType exportType = new ButtonType("Export Image", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(exportType, cancelType);

            VBox content = new VBox(10);
            content.setPadding(new Insets(20, 10, 10, 10));

            Label infoLabel = new Label("Export sketch as an image:");
            HBox exportOption = new HBox(10);
            exportOption.setAlignment(Pos.CENTER_LEFT);
            RadioButton exportRadio = new RadioButton("Export as image");
            exportRadio.setSelected(true);
            Label exportLabel = new Label("Save sketch as PNG image file");
            exportLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666666;");
            exportOption.getChildren().addAll(exportRadio, exportLabel);

            content.getChildren().addAll(infoLabel, exportOption);
            dialog.getDialogPane().setContent(content);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == exportType) {
                    exportSketchAsImage(selectedSketch);
                }
                return dialogButton;
            });

            dialog.showAndWait();
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
                System.out.println("IOException in exportSketchAsImage: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to save image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            loadSketches();
        } catch (SQLException e) {
            System.out.println("SQLException in handleRefresh: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to refresh sketches: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToCanvas(ActionEvent event) {
        System.out.println("Navigating back to CultureSketch Editor");
        navigateTo("/sketch/CultureSketch.fxml", "Culture Sketch Editor");
    }

    @FXML
    private void navigateToProfile(ActionEvent event) {
        System.out.println("Navigating to Profile");
        navigateTo("/profile/Profile.fxml", "My Profile");
    }

    @FXML
    private void viewSelectedSketchDetails(ActionEvent event) {
        CultureSketch selectedSketch = selectedSketchProperty.get();
        if (selectedSketch != null) {
            showDetailPane(selectedSketch);
        }
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
            System.out.println("Navigating to: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            System.out.println("IOException in navigateTo: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to " + title + ": " + e.getMessage());
        }
    }

    private void navigateToLogin() {
        System.out.println("Navigating to login");
        navigateTo("/auth/Login.fxml", "Login");
    }

    @FXML
    private void closeDetailPane(ActionEvent event) {
        System.out.println("Closing detail pane");
        detailPane.setVisible(false);
        detailPane.setManaged(false);
        selectedSketchProperty.set(null);
    }
}