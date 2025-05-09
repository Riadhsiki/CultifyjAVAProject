package controllers.reservation;

import models.Event;
import models.Reservation;
import services.eventreservation.EventService;
import services.eventreservation.ReservationService;
import utils.EmailSender;
import controllers.payment.PaymentController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class AjouterReservation {

    @FXML private ComboBox<String> etat;
    @FXML private DatePicker dateR;
    @FXML private TextField theme;
    @FXML private TextField url;
    @FXML private RadioButton ticket1;
    @FXML private RadioButton ticket2;
    @FXML private RadioButton ticket3;
    @FXML private RadioButton ticket4;
    @FXML private RadioButton ticket5;
    @FXML private ToggleGroup ticketGroup;
    @FXML private VBox paymentContainer;

    private ReservationService reservationService = new ReservationService();
    private EventService eventService = new EventService();
    private Event selectedEvent;
    private Reservation currentReservation;

    @FXML
    public void initialize() {
        dateR.setValue(LocalDate.now());

        etat.getItems().addAll("confirmée", "en cours", "annulée");
        etat.setValue("en cours");

        ticketGroup = new ToggleGroup();
        ticket1.setToggleGroup(ticketGroup);
        ticket2.setToggleGroup(ticketGroup);
        ticket3.setToggleGroup(ticketGroup);
        ticket4.setToggleGroup(ticketGroup);
        ticket5.setToggleGroup(ticketGroup);

        ticket1.setSelected(true);

        paymentContainer.setVisible(false);
        paymentContainer.setManaged(false);
    }

    @FXML
    void ajouterReservationAction(ActionEvent event) {
        try {
            if (!isInputValid()) return;

            RadioButton selectedRadio = (RadioButton) ticketGroup.getSelectedToggle();
            int nbTickets = Integer.parseInt(selectedRadio.getText());

            currentReservation = new Reservation(
                    etat.getValue(),
                    dateR.getValue(),
                    theme.getText().trim(),
                    url.getText().trim(),
                    nbTickets,
                    selectedEvent.getIdE()
            );

            // Add reservation to database
            reservationService.add(currentReservation);
            System.out.println("Reservation added successfully: " + currentReservation);

            // Send confirmation email
            sendConfirmationEmail();

            showAlert("Succès", "Réservation ajoutée et e-mail de confirmation envoyé !", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            System.err.println("Error in ajouterReservationAction: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Échec de l'ajout de la réservation : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handlePaymentAction(ActionEvent event) {
        if (selectedEvent == null) {
            showAlert("Erreur", "Aucun événement sélectionné pour le paiement.", Alert.AlertType.ERROR);
            return;
        }

        try {
            paymentContainer.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/payment/Payment.fxml"));
            Parent paymentRoot = loader.load();
            PaymentController controller = loader.getController();
            controller.setEvent(selectedEvent);
            controller.setUserId(1); // Replace with actual user ID
            controller.setPaymentContainer(paymentContainer);
            controller.setOnPaymentClosed((Void v) -> hidePaymentContainer());
            paymentContainer.getChildren().add(paymentRoot);
            paymentContainer.setVisible(true);
            paymentContainer.setManaged(true);
        } catch (IOException e) {
            System.err.println("Error loading payment interface: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'interface de paiement : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void retourAction(ActionEvent event) {
        navigateTo("/event/DetailEvent.fxml", "Détails de l'Événement");
    }

    private void sendConfirmationEmail() {
        EmailSender emailSender = new EmailSender();
        String emailBody = String.format(
                "Bonjour,\n\nVotre réservation a été confirmée avec succès !\n\nDétails de la réservation :\n" +
                        "- Événement : %s\n" +
                        "- Date de réservation : %s\n" +
                        "- Thème : %s\n" +
                        "- URL : %s\n" +
                        "- Nombre de tickets : %d\n" +
                        "- Statut : %s\n\n" +
                        "Merci de votre confiance !",
                selectedEvent.getTitre(),
                dateR.getValue().toString(),
                theme.getText().trim(),
                url.getText().trim(),
                Integer.parseInt(((RadioButton) ticketGroup.getSelectedToggle()).getText()),
                etat.getValue()
        );

        try {
            System.out.println("Attempting to send email to amalchourabi203@gmail.com");
            emailSender.sendEmail(
                    "amalchourabi203@gmail.com",
                    "Confirmation de Réservation",
                    emailBody
            );
            System.out.println("Email sending completed");
        } catch (Exception e) {
            System.err.println("Failed to send confirmation email: " + e.getMessage());
            e.printStackTrace();
            showAlert("Avertissement", "Réservation ajoutée, mais l'envoi de l'e-mail a échoué : " + e.getMessage(), Alert.AlertType.WARNING);
        }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                showAlert("Erreur de navigation", "Impossible de charger la vue : " + fxmlPath, Alert.AlertType.ERROR);
                return;
            }
            Parent root = loader.load();
            Stage stage = (Stage) etat.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur de navigation", "Impossible de charger la vue : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void hidePaymentContainer() {
        paymentContainer.setVisible(false);
        paymentContainer.setManaged(false);
        paymentContainer.getChildren().clear();
    }

    private boolean isInputValid() {
        if (dateR.getValue() == null || theme.getText().isEmpty() ||
                url.getText().isEmpty() || ticketGroup.getSelectedToggle() == null ||
                selectedEvent == null) {
            showAlert("Champs manquants", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
            return false;
        }

        if (!url.getText().matches("^(http|https)://.*$")) {
            showAlert("URL invalide", "Veuillez entrer une URL valide (commençant par http:// ou https://).", Alert.AlertType.ERROR);
            return false;
        }

        RadioButton selectedRadio = (RadioButton) ticketGroup.getSelectedToggle();
        int nbTickets = Integer.parseInt(selectedRadio.getText());
        if (nbTickets <= 0) {
            showAlert("Erreur de saisie", "Le nombre de tickets doit être positif.", Alert.AlertType.ERROR);
            return false;
        }

        if (dateR.getValue().isBefore(LocalDate.now())) {
            showAlert("Date invalide", "La date ne peut pas être dans le passé.", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setEventData(Event event) {
        if (event != null) {
            this.selectedEvent = event;
        }
    }

    public void initEditData(Reservation reservation) {
        if (reservation != null) {
            etat.setValue(reservation.getEtat());
            dateR.setValue(reservation.getDateR());
            theme.setText(reservation.getTheme());
            url.setText(reservation.getUrl());
            int nbTickets = reservation.getNbTickets();
            switch (nbTickets) {
                case 1: ticket1.setSelected(true); break;
                case 2: ticket2.setSelected(true); break;
                case 3: ticket3.setSelected(true); break;
                case 4: ticket4.setSelected(true); break;
                case 5: ticket5.setSelected(true); break;
                default: ticket1.setSelected(true);
            }
            this.selectedEvent = reservation.getEvent();
            this.currentReservation = reservation;
        }
    }
}