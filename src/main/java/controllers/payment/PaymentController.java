package controllers.payment;

import models.Event;
import models.Payment;
import services.eventreservation.PaymentService;
import services.eventreservation.SMSService;
import utils.EmailSender;
import com.twilio.exception.ApiException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class PaymentController implements Initializable {
    @FXML private Label eventTitleLabel;
    @FXML private Label eventPriceLabel;
    @FXML private Label eventDateLabel;
    @FXML private Label eventOrganisationLabel;

    @FXML private TabPane paymentMethodTabPane;
    @FXML private VBox cardPaymentForm;
    @FXML private VBox paypalPaymentForm;

    @FXML private TextField cardNumberField;
    @FXML private TextField cardExpiryField;
    @FXML private TextField cardCvvField;
    @FXML private TextField cardHolderField;

    @FXML private TextField paypalEmailField;
    @FXML private PasswordField paypalPasswordField;

    private Event currentEvent;
    private int userId;
    private PaymentService paymentService = new PaymentService();
    private Consumer<Void> onPaymentClosed;
    private VBox paymentContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cardNumberField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                cardNumberField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (newVal.length() > 16) {
                cardNumberField.setText(newVal.substring(0, 16));
            }
        });

        cardExpiryField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() == 2 && oldVal.length() == 1) {
                cardExpiryField.setText(newVal + "/");
            }
            if (newVal.length() > 5) {
                cardExpiryField.setText(newVal.substring(0, 5));
            }
        });

        cardCvvField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                cardCvvField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (newVal.length() > 3) {
                cardCvvField.setText(newVal.substring(0, 3));
            }
        });

        eventTitleLabel.setText("");
        eventPriceLabel.setText("");
        eventDateLabel.setText("");
        eventOrganisationLabel.setText("");
    }

    public void setEvent(Event event) {
        this.currentEvent = event;
        updateUI();
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setOnPaymentClosed(Consumer<Void> callback) {
        this.onPaymentClosed = callback;
    }

    public void setPaymentContainer(VBox container) {
        this.paymentContainer = container;
    }

    private void updateUI() {
        if (currentEvent != null) {
            eventTitleLabel.setText(currentEvent.getTitre());
            eventPriceLabel.setText(String.format("%.2f €", currentEvent.getPrix()));
            eventDateLabel.setText(currentEvent.getDateE().toString());
            eventOrganisationLabel.setText(currentEvent.getOrganisation());
        }
    }

    @FXML
    private void handleCardPayment(ActionEvent event) {
        if (!validateCardFields()) {
            showAlert("Erreur", "Veuillez remplir tous les champs de la carte correctement", Alert.AlertType.ERROR);
            return;
        }

        String transactionRef = "CARD-" + System.currentTimeMillis();
        Payment payment = new Payment(
                currentEvent.getIdE(),
                userId,
                currentEvent.getPrix(),
                "CARD",
                transactionRef,
                currentEvent,
                "COMPLETED"
        );

        if (paymentService.processPayment(payment)) {
            try {
                SMSService.sendSMS("+21658311751", "Paiement confirmé pour " + currentEvent.getTitre());
            } catch (ApiException e) {
                System.err.println("SMS sending failed: " + e.getMessage());
                showAlert("Avertissement", "Paiement réussi, mais l'envoi du SMS a échoué : " + e.getMessage(), Alert.AlertType.WARNING);
            }

            // Send confirmation email
            sendConfirmationEmail(payment);

            showAlert("Succès", "Paiement effectué avec succès!", Alert.AlertType.INFORMATION);
            showConfirmation(payment);
        } else {
            showAlert("Erreur", "Le paiement a échoué", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handlePaypalPayment(ActionEvent event) {
        if (paypalEmailField.getText().isEmpty() || paypalPasswordField.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez entrer vos identifiants PayPal", Alert.AlertType.ERROR);
            return;
        }

        String transactionRef = "PAYPAL-" + System.currentTimeMillis();
        Payment payment = new Payment(
                currentEvent.getIdE(),
                userId,
                currentEvent.getPrix(),
                "PAYPAL",
                transactionRef,
                currentEvent,
                "COMPLETED"
        );

        if (paymentService.processPayment(payment)) {
            try {
                SMSService.sendSMS("+21612345678", "Paiement confirmé pour " + currentEvent.getTitre());
            } catch (ApiException e) {
                System.err.println("SMS sending failed: " + e.getMessage());
                showAlert("Avertissement", "Paiement réussi, mais l'envoi du SMS a échoué : " + e.getMessage(), Alert.AlertType.WARNING);
            }

            // Send confirmation email
            sendConfirmationEmail(payment);

            showAlert("Succès", "Paiement PayPal effectué avec succès!", Alert.AlertType.INFORMATION);
            showConfirmation(payment);
        } else {
            showAlert("Erreur", "Le paiement PayPal a échoué", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closePayment();
    }

    private void sendConfirmationEmail(Payment payment) {
        EmailSender emailSender = new EmailSender();
        String emailBody = String.format(
                "Bonjour,\n\nVotre paiement a été effectué avec succès !\n\nDétails du paiement :\n" +
                        "- Événement : %s\n" +
                        "- Montant : %.2f €\n" +
                        "- Méthode de paiement : %s\n" +
                        "- Référence de transaction : %s\n" +
                        "- Statut : %s\n" +
                        "- Date de paiement : %s\n\n" +
                        "Merci de votre confiance !",
                payment.getEvent().getTitre(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getTransactionReference(),
                payment.getStatus(),
                payment.getPaymentDate().toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );

        try {
            emailSender.sendEmail(
                    "amalchourabi203@gmail.com",
                    "Confirmation de Paiement",
                    emailBody
            );
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            showAlert("Avertissement", "Paiement réussi, mais l'envoi de l'e-mail a échoué : " + e.getMessage(), Alert.AlertType.WARNING);
        }
    }

    private void showConfirmation(Payment payment) {
        if (paymentContainer == null) {
            showAlert("Erreur", "Conteneur de paiement non initialisé.", Alert.AlertType.ERROR);
            return;
        }

        try {
            paymentContainer.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/payment/PaymentConfirmation.fxml"));
            Parent confirmationRoot = loader.load();
            PaymentConfirmationController controller = loader.getController();
            controller.setPaymentData(payment);
            controller.setOnConfirmationClosed(v -> closePayment());
            paymentContainer.getChildren().add(confirmationRoot);
        } catch (IOException e) {
            System.err.println("Error loading confirmation interface: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'interface de confirmation : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateCardFields() {
        return !cardNumberField.getText().isEmpty() &&
                cardNumberField.getText().length() == 16 &&
                !cardExpiryField.getText().isEmpty() &&
                cardExpiryField.getText().matches("\\d{2}/\\d{2}") &&
                !cardCvvField.getText().isEmpty() &&
                cardCvvField.getText().length() == 3 &&
                !cardHolderField.getText().isEmpty();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closePayment() {
        if (onPaymentClosed != null) {
            onPaymentClosed.accept(null);
        }
    }
}