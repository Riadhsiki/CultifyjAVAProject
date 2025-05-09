package controllers.event;

import models.Event;
import services.eventreservation.EventService;
import services.eventreservation.SMSService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.time.LocalDate;

public class AjouterEventController {

    @FXML private TextField titre;
    @FXML private TextArea description;
    @FXML private TextField organisation;
    @FXML private TextField capacite;
    @FXML private TextField nbplaces;
    @FXML private ComboBox<String> categorie;
    @FXML private TextField prix;
    @FXML private ImageView imageView;
    @FXML private Button btnUpload;
    @FXML private DatePicker datePicker;

    private String imagePath = "";
    private Runnable onEventAddedCallback;
    // ← Correction ici : numéro complet avec indicatif +216
    private static final String PHONE_NUMBER = "+21658311751";

    public void setOnEventAddedCallback(Runnable callback) {
        this.onEventAddedCallback = callback;
    }

    @FXML
    public void initialize() {
        categorie.getItems().addAll("théâtre", "musée", "musique", "danse", "cuisine");
        datePicker.setValue(LocalDate.now());

        capacite.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                capacite.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        nbplaces.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                nbplaces.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        prix.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                prix.setText(newVal.replaceAll("[^\\d.]", ""));
            }
        });
    }

    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(btnUpload.getScene().getWindow());

        if (selectedFile != null) {
            try {
                File uploadsDir = new File("uploads");
                if (!uploadsDir.exists()) uploadsDir.mkdir();

                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File destFile = new File(uploadsDir, fileName);
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                imagePath = "uploads/" + fileName;
                imageView.setImage(new Image(selectedFile.toURI().toString()));
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder l'image.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    void ajouterEventAction(ActionEvent event) {
        try {
            if (fieldsAreEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
                return;
            }

            int capaciteValue = Integer.parseInt(capacite.getText());
            int nbplacesValue = Integer.parseInt(nbplaces.getText());
            float prixValue = Float.parseFloat(prix.getText());

            if (capaciteValue <= 0 || nbplacesValue <= 0 || prixValue < 0) {
                showAlert(Alert.AlertType.WARNING, "Valeurs invalides", "Les valeurs doivent être positives.");
                return;
            }

            if (capaciteValue < nbplacesValue) {
                showAlert(Alert.AlertType.WARNING, "Capacité insuffisante", "La capacité doit être ≥ nombre de places.");
                return;
            }

            Date dateEvent = Date.valueOf(datePicker.getValue());

            Event ev = new Event(
                    titre.getText(),
                    description.getText(),
                    dateEvent,
                    organisation.getText(),
                    capaciteValue,
                    nbplacesValue,
                    categorie.getValue(),
                    imagePath,
                    prixValue
            );

            new EventService().add(ev);

            // --- Envoi du SMS et gestion du résultat ---
            boolean smsOk = SMSService.sendSMS(
                    PHONE_NUMBER,
                    "Nouvel événement : " + ev.getTitre() + " (" + ev.getCategorie() + ")"
            );
            if (smsOk) {
                System.out.println("SMS de notification envoyé.");
            } else {
                showAlert(Alert.AlertType.ERROR, "SMS non envoyé",
                        "Impossible d'envoyer la notification SMS.");
            }

            if (onEventAddedCallback != null) onEventAddedCallback.run();

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement ajouté !");
            redirectToListView();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Valeurs numériques invalides.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean fieldsAreEmpty() {
        return titre.getText().isEmpty() ||
                description.getText().isEmpty() ||
                organisation.getText().isEmpty() ||
                capacite.getText().isEmpty() ||
                nbplaces.getText().isEmpty() ||
                prix.getText().isEmpty() ||
                categorie.getValue() == null ||
                imagePath.isEmpty() ||
                datePicker.getValue() == null;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void redirectToListView() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/event/DetailEvent.fxml"));
        titre.getScene().setRoot(root);
    }

    public void initEditData(Event event) {
        if (event == null) return;
        titre.setText(event.getTitre());
        description.setText(event.getDescription());
        organisation.setText(event.getOrganisation());
        capacite.setText(String.valueOf(event.getCapacite()));
        nbplaces.setText(String.valueOf(event.getNbplaces()));
        prix.setText(String.valueOf(event.getPrix()));
        categorie.setValue(event.getCategorie());
        if (event.getDateE() != null) {
            datePicker.setValue(event.getDateE().toLocalDate());
        }
        imagePath = event.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            File f = new File(imagePath);
            if (f.exists()) imageView.setImage(new Image(f.toURI().toString()));
        }
    }

    @FXML
    private void annuler(ActionEvent event) throws IOException {
        redirectToListView();
    }

}