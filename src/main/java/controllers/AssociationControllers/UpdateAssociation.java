package controllers.AssociationControllers;

import entities.Association;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.AssociationServices;

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

public class UpdateAssociation implements Initializable {

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnUpdate;

    @FXML
    private TextArea butField;

    @FXML
    private TextField contactField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField idField;

    @FXML
    private TextField imageField;

    @FXML
    private ImageView imagePreview;

    @FXML
    private Label currentImageLabel;

    @FXML
    private TextField montantField;

    @FXML
    private TextField nomField;

    @FXML
    private TextField siteWebField;

    // Labels d'erreur
    @FXML
    private Label nomErrorLabel;

    @FXML
    private Label descriptionErrorLabel;

    @FXML
    private Label contactErrorLabel;

    @FXML
    private Label butErrorLabel;

    @FXML
    private Label montantErrorLabel;

    @FXML
    private Label siteWebErrorLabel;

    @FXML
    private Label imageErrorLabel;

    private Association association;
    private AssociationServices associationServices = new AssociationServices();
    private File selectedImageFile;

    // Chemin vers le dossier d'upload de Symfony
    // À ajuster selon l'emplacement de votre projet Symfony
    private final String SYMFONY_UPLOADS_DIR = "C:/xampp/htdocs/cultify/public/uploads/images";

    // Patterns pour la validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?$");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (nomField == null || descriptionField == null || contactField == null ||
                butField == null || montantField == null || siteWebField == null ||
                imageField == null || imagePreview == null) {
            System.err.println("Warning: One or more FXML fields were not injected properly!");
        }

        clearErrorLabels();
    }

    public void setAssociation(Association association) {
        this.association = association;
        populateFields();
    }

    private void populateFields() {
        if (association != null) {
            idField.setText(String.valueOf(association.getId()));
            nomField.setText(association.getNom());
            descriptionField.setText(association.getDescription());
            contactField.setText(association.getContact());
            butField.setText(association.getBut());
            montantField.setText(String.valueOf(association.getMontantDesire()));
            siteWebField.setText(association.getSiteWeb());

            // Afficher l'image actuelle si elle existe
            if (association.getImage() != null && !association.getImage().isEmpty()) {
                imageField.setText(association.getImage());
                currentImageLabel.setText("Image actuelle: " + association.getImage());

                // Charger et afficher l'image si possible
                try {
                    File imageFile = new File(SYMFONY_UPLOADS_DIR + "/" + association.getImage());
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        imagePreview.setImage(image);
                        imagePreview.setFitWidth(150);
                        imagePreview.setFitHeight(150);
                        imagePreview.setPreserveRatio(true);
                    }
                } catch (Exception e) {
                    // Ignorer les erreurs d'affichage d'image
                    System.out.println("Impossible de charger l'image: " + e.getMessage());
                }
            }

            // Désactiver le champ ID car il ne doit pas être modifié
            idField.setDisable(true);
        }
    }

    private void clearErrorLabels() {
        nomErrorLabel.setText("");
        descriptionErrorLabel.setText("");
        contactErrorLabel.setText("");
        butErrorLabel.setText("");
        montantErrorLabel.setText("");
        siteWebErrorLabel.setText("");
        imageErrorLabel.setText("");
    }

    @FXML
    void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");

        // Configurer les filtres pour n'accepter que les images
        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);

        // Afficher le sélecteur de fichiers
        selectedImageFile = fileChooser.showOpenDialog(btnCancel.getScene().getWindow());

        if (selectedImageFile != null) {
            // Afficher le chemin du fichier sélectionné
            imageField.setText(selectedImageFile.getName());
            currentImageLabel.setText("Nouvelle image sélectionnée: " + selectedImageFile.getName());

            // Prévisualiser l'image
            try {
                Image image = new Image(selectedImageFile.toURI().toString());
                imagePreview.setImage(image);
                imagePreview.setFitWidth(150);
                imagePreview.setFitHeight(150);
                imagePreview.setPreserveRatio(true);
                imageErrorLabel.setText("");
            } catch (Exception e) {
                System.out.println("Impossible de prévisualiser l'image: " + e.getMessage());
                imageErrorLabel.setText("Impossible de charger l'image: " + e.getMessage());
            }
        }
    }

    @FXML
    void cancelUpdate(ActionEvent event) {
        navigateToAllAssociations();
    }

    @FXML
    void updateAssociation(ActionEvent event) {
        try {
            // Nettoyer les messages d'erreur précédents
            clearErrorLabels();

            // Valider les champs
            if (validateFields()) {
                // Mettre à jour l'objet association avec les nouvelles valeurs
                if (nomField.getText() != null) {
                    association.setNom(nomField.getText().trim());
                }

                if (descriptionField.getText() != null) {
                    association.setDescription(descriptionField.getText().trim());
                }

                if (contactField.getText() != null) {
                    association.setContact(contactField.getText().trim());
                }

                if (butField.getText() != null) {
                    association.setBut(butField.getText().trim());
                }

                if (montantField.getText() != null && !montantField.getText().trim().isEmpty()) {
                    association.setMontantDesire(Double.parseDouble(montantField.getText().trim()));
                }

                if (siteWebField.getText() != null) {
                    association.setSiteWeb(siteWebField.getText().trim());
                }

                // Traiter la nouvelle image si une a été sélectionnée
                if (selectedImageFile != null) {
                    String filename = processImage(selectedImageFile);
                    association.setImage(filename);
                }

                // Appeler le service pour mettre à jour l'association
                associationServices.update(association);


                // Retourner à la liste des associations
                navigateToAllAssociations();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour: " + e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Problème de fichier: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        // Validation du nom
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            nomErrorLabel.setText("Le nom de l'association ne peut pas être vide.");
            isValid = false;
        } else if (nomField.getText().trim().length() < 2) {
            nomErrorLabel.setText("Le nom de l'association doit contenir au moins 2 caractères.");
            isValid = false;
        } else if (nomField.getText().trim().length() > 255) {
            nomErrorLabel.setText("Le nom de l'association ne peut pas dépasser 255 caractères.");
            isValid = false;
        }

        // Validation de la description
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
            descriptionErrorLabel.setText("La description ne peut pas être vide.");
            isValid = false;
        } else if (descriptionField.getText().trim().length() < 10) {
            descriptionErrorLabel.setText("La description doit contenir au moins 10 caractères.");
            isValid = false;
        }

        // Validation du contact (email)
        if (contactField.getText() == null || contactField.getText().trim().isEmpty()) {
            contactErrorLabel.setText("Le contact ne peut pas être vide.");
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(contactField.getText().trim()).matches()) {
            contactErrorLabel.setText("L'adresse email n'est pas valide.");
            isValid = false;
        }

        // Validation du but
        if (butField.getText() == null || butField.getText().trim().isEmpty()) {
            butErrorLabel.setText("Le but de l'association ne peut pas être vide.");
            isValid = false;
        } else if (butField.getText().trim().length() < 10) {
            butErrorLabel.setText("Le but de l'association doit contenir au moins 10 caractères.");
            isValid = false;
        }

        // Validation du montant
        if (montantField.getText() == null || montantField.getText().trim().isEmpty()) {
            montantErrorLabel.setText("Le montant du don ne peut pas être vide.");
            isValid = false;
        } else {
            try {
                double montantValue = Double.parseDouble(montantField.getText().trim());
                if (montantValue <= 0) {
                    montantErrorLabel.setText("Le montant du don doit être un nombre positif.");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                montantErrorLabel.setText("Le montant doit être un nombre.");
                isValid = false;
            }
        }

        // Validation du site web (optionnel)
        if (siteWebField.getText() != null && !siteWebField.getText().trim().isEmpty() &&
                !URL_PATTERN.matcher(siteWebField.getText().trim()).matches()) {
            siteWebErrorLabel.setText("L'URL du site web n'est pas valide.");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Traite l'image sélectionnée et la copie dans le dossier d'uploads de Symfony
     * @param imageFile Le fichier image sélectionné
     * @return Le nom du fichier généré
     * @throws IOException Si une erreur se produit lors de la copie
     */
    private String processImage(File imageFile) throws IOException {
        // Vérifier si le dossier existe, sinon le créer
        File uploadsDir = new File(SYMFONY_UPLOADS_DIR);
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs();
        }

        // Générer un nom de fichier unique (comme dans Symfony)
        String fileExtension = getFileExtension(imageFile.getName());
        String newFileName = UUID.randomUUID().toString() + "." + fileExtension;

        // Chemin complet du fichier de destination
        Path destination = Paths.get(SYMFONY_UPLOADS_DIR, newFileName);

        // Copier le fichier dans le dossier d'upload
        Files.copy(imageFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        return newFileName;
    }

    /**
     * Récupère l'extension d'un fichier
     * @param filename Nom du fichier
     * @return Extension du fichier
     */
    private String getFileExtension(String filename) {
        if (filename.lastIndexOf(".") != -1 && filename.lastIndexOf(".") != 0) {
            return filename.substring(filename.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void navigateToAllAssociations() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Association/AllAssociation.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation",
                    "Impossible de retourner à la liste des associations: " + e.getMessage());
        }
    }
}