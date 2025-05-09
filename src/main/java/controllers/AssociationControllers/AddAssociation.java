package controllers.associationcontrollers;

import models.Association;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import services.associationDon.AssociationDescriptionIA;
import services.associationDon.AssociationServices;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.regex.Pattern;

public class AddAssociation implements Initializable {
    @FXML private TextArea but;
    @FXML private TextField contact;
    @FXML private TextArea description;
    @FXML private TextField montant;
    @FXML private TextField nom;
    @FXML private TextField siteWeb;
    @FXML private TextField imagePathField;
    @FXML private Label imagePreviewLabel;
    @FXML private Button generateDescriptionButton;
    @FXML private ImageView imagePreview;

    // Labels d'erreur
    @FXML private Label nomErrorLabel;
    @FXML private Label montantErrorLabel;
    @FXML private Label butErrorLabel;
    @FXML private Label descriptionErrorLabel;
    @FXML private Label contactErrorLabel;
    @FXML private Label siteWebErrorLabel;
    @FXML private Label imageErrorLabel;

    private AssociationServices as = new AssociationServices();
    private File selectedImageFile;
    private final String SYMFONY_UPLOADS_DIR = "C:/xampp/htdocs/cultify/public/uploads/images";

    // Patterns pour la validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?$");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clearErrorLabels();
    }

    private void clearErrorLabels() {
        nomErrorLabel.setText("");
        montantErrorLabel.setText("");
        butErrorLabel.setText("");
        descriptionErrorLabel.setText("");
        contactErrorLabel.setText("");
        siteWebErrorLabel.setText("");
        imageErrorLabel.setText("");
    }

    @FXML
    void generateDescription(ActionEvent event) {
        String nomAssociation = nom.getText().trim();
        String butAssociation = but.getText().trim();

        if (nomAssociation.isEmpty() || butAssociation.isEmpty()) {
            showAlert("Champs manquants", "Veuillez remplir le nom et le but avant de générer la description.", Alert.AlertType.WARNING);
            return;
        }

        generateDescriptionButton.setDisable(true);
        generateDescriptionButton.setText("Génération en cours...");

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return AssociationDescriptionIA.generateDescription(nomAssociation, butAssociation);
            }
        };

        task.setOnSucceeded(e -> {
            String generatedDescription = task.getValue();
            description.setText(generatedDescription);
            generateDescriptionButton.setDisable(false);
            generateDescriptionButton.setText("Générer Description");

            if (generatedDescription.startsWith("Erreur")) {
                showAlert("Erreur", generatedDescription, Alert.AlertType.ERROR);
            }
        });

        task.setOnFailed(e -> {
            showAlert("Erreur", "Échec de la génération: " + task.getException().getMessage(), Alert.AlertType.ERROR);
            generateDescriptionButton.setDisable(false);
            generateDescriptionButton.setText("Générer Description");
        });

        new Thread(task).start();
    }

    @FXML
    void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );

        selectedImageFile = fileChooser.showOpenDialog(imagePathField.getScene().getWindow());
        if (selectedImageFile != null) {
            imagePathField.setText(selectedImageFile.getName());
            imagePreviewLabel.setText("Image sélectionnée");
            imageErrorLabel.setText("");

            // Afficher l'aperçu de l'image
            try {
                Image image = new Image(selectedImageFile.toURI().toString());
                imagePreview.setImage(image);
                imagePreview.setFitWidth(150);
                imagePreview.setFitHeight(150);
                imagePreview.setPreserveRatio(true);
            } catch (Exception e) {
                System.out.println("Impossible de prévisualiser l'image: " + e.getMessage());
                imageErrorLabel.setText("Erreur de chargement de l'image");
            }
        }
    }

    @FXML
    void save(ActionEvent event) {
        try {
            clearErrorLabels();

            if (validateFields()) {
                Association association = createAssociation();
                if (selectedImageFile != null) {
                    association.setImage(processImage(selectedImageFile));
                }

                as.add(association);
                showAlert("Succès", "Association créée avec succès!", Alert.AlertType.INFORMATION);
                clearFields();
            }
        } catch (SQLException | IOException e) {
            showAlert("Erreur", "Erreur lors de la création: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        // Validation du nom
        if (nom.getText().trim().isEmpty()) {
            nomErrorLabel.setText("Le nom de l'association ne peut pas être vide.");
            isValid = false;
        } else if (nom.getText().trim().length() < 2) {
            nomErrorLabel.setText("Le nom de l'association doit contenir au moins 2 caractères.");
            isValid = false;
        } else if (nom.getText().trim().length() > 255) {
            nomErrorLabel.setText("Le nom de l'association ne peut pas dépasser 255 caractères.");
            isValid = false;
        }

        // Validation du montant
        if (montant.getText().trim().isEmpty()) {
            montantErrorLabel.setText("Le montant du don ne peut pas être vide.");
            isValid = false;
        } else {
            try {
                double montantValue = Double.parseDouble(montant.getText().trim());
                if (montantValue <= 0) {
                    montantErrorLabel.setText("Le montant du don doit être un nombre positif.");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                montantErrorLabel.setText("Le montant doit être un nombre.");
                isValid = false;
            }
        }

        // Validation du but
        if (but.getText().trim().isEmpty()) {
            butErrorLabel.setText("Le but de l'association ne peut pas être vide.");
            isValid = false;
        } else if (but.getText().trim().length() < 10) {
            butErrorLabel.setText("Le but de l'association doit contenir au moins 10 caractères.");
            isValid = false;
        }

        // Validation de la description
        if (description.getText().trim().isEmpty()) {
            descriptionErrorLabel.setText("La description ne peut pas être vide.");
            isValid = false;
        } else if (description.getText().trim().length() < 10) {
            descriptionErrorLabel.setText("La description doit contenir au moins 10 caractères.");
            isValid = false;
        }

        // Validation du contact (email)
        if (contact.getText().trim().isEmpty()) {
            contactErrorLabel.setText("Le contact ne peut pas être vide.");
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(contact.getText().trim()).matches()) {
            contactErrorLabel.setText("L'adresse email n'est pas valide.");
            isValid = false;
        }

        // Validation du site web (optionnel)
        if (!siteWeb.getText().trim().isEmpty() && !URL_PATTERN.matcher(siteWeb.getText().trim()).matches()) {
            siteWebErrorLabel.setText("L'URL du site web n'est pas valide.");
            isValid = false;
        }

        // Pas de validation pour l'image car elle est optionnelle

        return isValid;
    }

    private Association createAssociation() {
        Association association = new Association(
                nom.getText().trim(),
                Double.parseDouble(montant.getText().trim()),
                description.getText().trim(),
                contact.getText().trim(),
                but.getText().trim()
        );

        if (!siteWeb.getText().trim().isEmpty()) {
            association.setSiteWeb(siteWeb.getText().trim());
        }

        return association;
    }

    private String processImage(File imageFile) throws IOException {
        Path uploadDir = Paths.get(SYMFONY_UPLOADS_DIR);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String extension = imageFile.getName().substring(imageFile.getName().lastIndexOf("."));
        String newFilename = UUID.randomUUID() + extension;
        Path destination = uploadDir.resolve(newFilename);

        Files.copy(imageFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        return newFilename;
    }

    private void clearFields() {
        nom.clear();
        montant.clear();
        description.clear();
        contact.clear();
        but.clear();
        siteWeb.clear();
        imagePathField.clear();
        imagePreviewLabel.setText("");
        imagePreview.setImage(null); // Effacer l'aperçu de l'image
        selectedImageFile = null;
        clearErrorLabels();
    }

    @FXML
    void affiche(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Association/AllAssociation.fxml"));
            nom.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert("Erreur", "Erreur de navigation: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}