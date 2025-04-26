package Controllers;

import Models.ContenuMultiMedia;
import Services.ContenuMultiMediaService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;


import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.ResourceBundle;

public class GestionContenuMultiMediaController implements Initializable {

    @FXML
    private ListView<ContenuMultiMedia> listView;
    @FXML
    private TextField txtTitre, txtCategorie;
    @FXML
    private TextArea txtDescription;
    @FXML
    private ImageView imageView;
    @FXML
    private Button btnUpload;
    @FXML
    private Label titreError, descriptionError, categorieError, imageError;
    private ContenuMultiMediaService service = new ContenuMultiMediaService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupListView();
        loadData();
        setupValidation();
    }

    private void setupValidation() {
        txtTitre.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                titreError.setText("Champ obligatoire");
            } else {
                titreError.setText("");
            }
        });

        txtDescription.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() < 10) {
                descriptionError.setText("Minimum 10 caractères");
            } else {
                descriptionError.setText("");
            }
        });

        txtCategorie.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("^[a-zA-Z ]+$")) {
                categorieError.setText("Lettres uniquement");
            } else {
                categorieError.setText("");
            }
        });
    }

    private void setupListView() {
        listView.setCellFactory(param -> new ListCell<ContenuMultiMedia>() {
            @Override
            protected void updateItem(ContenuMultiMedia item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitre_media() + " (" + item.getCategorie_media() + ")");
            }
        });

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) populateFields(newVal);
        });
    }

    private void loadData() {
        try {
            listView.getItems().setAll(service.getAll());
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void populateFields(ContenuMultiMedia contenu) {
        txtTitre.setText(contenu.getTitre_media());
        txtDescription.setText(contenu.getText_media());
        txtCategorie.setText(contenu.getCategorie_media());
        if (contenu.getPhoto_media() != null) {
            imageView.setImage(new Image(contenu.getPhoto_media()));
        }
    }

    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(btnUpload.getScene().getWindow());
        if (file != null) {
            imageView.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void handleAdd() {
        boolean isValid = true;

        if (txtTitre.getText().isEmpty()) {
            titreError.setText("Champ obligatoire");
            isValid = false;
        }

        if (txtDescription.getText().length() < 10) {
            descriptionError.setText("Minimum 10 caractères");
            isValid = false;
        }

        if (imageView.getImage() == null) {
            imageError.setText("Image obligatoire");
            isValid = false;
        }

        if (!isValid) return;


        try {
            ContenuMultiMedia nouveau = new ContenuMultiMedia(
                    txtTitre.getText(),
                    txtDescription.getText(),
                    imageView.getImage() != null ? imageView.getImage().getUrl() : "",
                    txtCategorie.getText(),
                    new Date()
            );
            service.add(nouveau);
            loadData();
            clearFields();
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        boolean isValid = true;

        if (txtTitre.getText().isEmpty()) {
            titreError.setText("Champ obligatoire");
            isValid = false;
        }

        if (txtDescription.getText().length() < 10) {
            descriptionError.setText("Minimum 10 caractères");
            isValid = false;
        }

        if (imageView.getImage() == null) {
            imageError.setText("Image obligatoire");
            isValid = false;
        }

        if (!isValid) return;
        ContenuMultiMedia selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                selected.setTitre_media(txtTitre.getText());
                selected.setText_media(txtDescription.getText());
                selected.setCategorie_media(txtCategorie.getText());
                if (imageView.getImage() != null) {
                    selected.setPhoto_media(imageView.getImage().getUrl());
                }
                service.update(selected);
                loadData();
            } catch (SQLException e) {
                showAlert("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        ContenuMultiMedia selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                service.delete(selected);
                loadData();
                clearFields();
            } catch (SQLException e) {
                showAlert("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
    }

    private void clearFields() {
        txtTitre.clear();
        txtDescription.clear();
        txtCategorie.clear();
        imageView.setImage(null);
        titreError.setText("");
        descriptionError.setText("");
        categorieError.setText("");
        imageError.setText("");
    }

    private void showAlert(String title, String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}