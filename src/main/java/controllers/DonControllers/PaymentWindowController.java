package controllers.DonControllers;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Association;
import models.Don;
import models.User;
import services.associationDon.AssociationServices;
import services.associationDon.DonServices;
import services.user.UserService;
import utils.WebhookUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentWindowController {
    private static final Logger LOGGER = Logger.getLogger(PaymentWindowController.class.getName());

    @FXML private Label associationLabel;
    @FXML private Label montantLabel;
    @FXML private Label referenceLabel;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryMonthField;
    @FXML private TextField expiryYearField;
    @FXML private TextField cvcField;
    @FXML private Label testModeLabel;
    @FXML private Button cancelButton;
    @FXML private Button payButton;
    @FXML private ProgressIndicator progressIndicator;

    // Labels pour les messages d'erreur
    @FXML private Label cardErrorLabel;
    @FXML private Label expiryErrorLabel;
    @FXML private Label cvcErrorLabel;

    private Don don;
    private final DonServices donService = new DonServices();
    private boolean paymentConfirmed = false;

    // Cartes de test
    private final String validCardNumber = StripePaymentService.CARD_SUCCESS;
    private final String declineCardNumber = StripePaymentService.CARD_DECLINE;

    @FXML
    public void initialize() {
        // Configuration des boutons
        cancelButton.setOnAction(event -> fermerFenetre());
        payButton.setOnAction(event -> {
            validateAllFields();
            if (isFormValid()) {
                processPayment();
            }
        });

        cardNumberField.setText(validCardNumber);
        expiryMonthField.setText("12");
        expiryYearField.setText("25");
        cvcField.setText("123");

        setupInputValidation();
        clearErrorMessages();
        setupFocusListeners();

        testModeLabel.setText("Mode test activé: Pour tester une carte qui échoue, utilisez: " + declineCardNumber);
    }

    private void setupFocusListeners() {
        cardNumberField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validateCardNumber();
        });
        expiryMonthField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validateExpiryDate();
        });
        expiryYearField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validateExpiryDate();
        });
        cvcField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validateCVC();
        });
    }

    private void clearErrorMessages() {
        cardErrorLabel.setText("");
        expiryErrorLabel.setText("");
        cvcErrorLabel.setText("");
        cardNumberField.getStyleClass().remove("invalid-field");
        expiryMonthField.getStyleClass().remove("invalid-field");
        expiryYearField.getStyleClass().remove("invalid-field");
        cvcField.getStyleClass().remove("invalid-field");
    }

    private void setupInputValidation() {
        cardNumberField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String filtered = newVal.replaceAll("[^0-9]", "");
                if (filtered.length() > 16) filtered = filtered.substring(0, 16);
                if (!filtered.equals(newVal)) cardNumberField.setText(filtered);
            }
        });

        expiryMonthField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String filtered = newVal.replaceAll("[^0-9]", "");
                if (filtered.length() > 2) filtered = filtered.substring(0, 2);
                try {
                    if (!filtered.isEmpty()) {
                        int month = Integer.parseInt(filtered);
                        if (month > 12) filtered = "12";
                        else if (month < 1 && filtered.length() >= 2) filtered = "01";
                    }
                } catch (NumberFormatException ignored) {}
                if (!filtered.equals(newVal)) expiryMonthField.setText(filtered);
            }
        });

        expiryYearField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String filtered = newVal.replaceAll("[^0-9]", "");
                if (filtered.length() > 2) filtered = filtered.substring(0, 2);
                try {
                    if (!filtered.isEmpty() && filtered.length() >= 2) {
                        int year = Integer.parseInt(filtered);
                        if (year < 25) filtered = "25";
                    }
                } catch (NumberFormatException ignored) {}
                if (!filtered.equals(newVal)) expiryYearField.setText(filtered);
            }
        });

        cvcField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String filtered = newVal.replaceAll("[^0-9]", "");
                if (filtered.length() > 3) filtered = filtered.substring(0, 3);
                if (!filtered.equals(newVal)) cvcField.setText(filtered);
            }
        });
    }

    private boolean validateCardNumber() {
        String cardNumber = cardNumberField.getText().trim();
        if (cardNumber.isEmpty()) {
            setFieldError(cardNumberField, cardErrorLabel, "Le numéro de carte est requis");
            return false;
        } else if (cardNumber.length() != 16) {
            setFieldError(cardNumberField, cardErrorLabel, "Le numéro de carte doit contenir exactement 16 chiffres");
            return false;
        }
        clearFieldError(cardNumberField, cardErrorLabel);
        return true;
    }

    private boolean validateExpiryDate() {
        String expiryMonth = expiryMonthField.getText().trim();
        String expiryYear = expiryYearField.getText().trim();
        if (expiryMonth.isEmpty() || expiryYear.isEmpty()) {
            setFieldError(expiryMonthField, expiryErrorLabel, "La date d'expiration est requise");
            setFieldError(expiryYearField, expiryErrorLabel, "");
            return false;
        }
        try {
            int month = Integer.parseInt(expiryMonth);
            int year = Integer.parseInt(expiryYear);
            if (month < 1 || month > 12) {
                setFieldError(expiryMonthField, expiryErrorLabel, "Le mois d'expiration doit être entre 1 et 12");
                setFieldError(expiryYearField, expiryErrorLabel, "");
                return false;
            }
            if (year < 25) {
                setFieldError(expiryYearField, expiryErrorLabel, "L'année d'expiration doit être 25 ou plus");
                clearFieldError(expiryMonthField, null);
                return false;
            }
        } catch (NumberFormatException e) {
            setFieldError(expiryMonthField, expiryErrorLabel, "La date doit être numérique");
            setFieldError(expiryYearField, expiryErrorLabel, "");
            return false;
        }
        clearFieldError(expiryMonthField, expiryErrorLabel);
        clearFieldError(expiryYearField, null);
        return true;
    }

    private boolean validateCVC() {
        String cvc = cvcField.getText().trim();
        if (cvc.isEmpty()) {
            setFieldError(cvcField, cvcErrorLabel, "Le code CVC est requis");
            return false;
        } else if (cvc.length() != 3) {
            setFieldError(cvcField, cvcErrorLabel, "Le code CVC doit contenir exactement 3 chiffres");
            return false;
        }
        clearFieldError(cvcField, cvcErrorLabel);
        return true;
    }

    private void setFieldError(TextField field, Label errorLabel, String errorMessage) {
        if (!field.getStyleClass().contains("invalid-field")) {
            field.getStyleClass().add("invalid-field");
        }
        if (errorLabel != null) errorLabel.setText(errorMessage);
    }

    private void clearFieldError(TextField field, Label errorLabel) {
        field.getStyleClass().remove("invalid-field");
        if (errorLabel != null) errorLabel.setText("");
    }

    private void validateAllFields() {
        validateCardNumber();
        validateExpiryDate();
        validateCVC();
    }

    private boolean isFormValid() {
        return validateCardNumber() && validateExpiryDate() && validateCVC();
    }

    public boolean isPaymentConfirmed() {
        return paymentConfirmed;
    }

    public void setDon(Don don) {
        this.don = don;
        associationLabel.setText(don.getAssociation().getNom());
        montantLabel.setText(String.format("%.2f TND", don.getMontant()));
        referenceLabel.setText("DON-" + don.getId());
    }

    private void fermerFenetre() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void processPayment() {
        toggleControls(false);
        progressIndicator.setVisible(true);

        Task<Boolean> paymentTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    long amountInCents = Math.round(don.getMontant() * 100);
                    String cardNumber = cardNumberField.getText().trim();
                    String description = "Don pour " + don.getAssociation().getNom() + " (ID: " + don.getId() + ")";
                    Charge charge = StripePaymentService.createTestCharge(amountInCents, description, "tnd", cardNumber);
                    return charge != null && "succeeded".equals(charge.getStatus());
                } catch (StripeException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void succeeded() {
                Boolean success = getValue();
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);

                    if (Boolean.TRUE.equals(success)) {
                        paymentConfirmed = true;
                        try {
                            // Update donation status
                            don.setStatus("confirme");
                            donService.update(don, don.getAssociation().getId());

                            // Webhook notification
                            try {
                                AssociationServices associationServices = new AssociationServices();
                                Association fullAssociation = associationServices.getById(don.getAssociation().getId());
                                new Thread(() -> {
                                    boolean notified = WebhookUtil.notifyDonationProgress(fullAssociation);
                                    Platform.runLater(() -> {
                                        if (notified) {
                                            LOGGER.log(Level.INFO, "Association progress notification sent successfully");
                                        } else {
                                            LOGGER.log(Level.WARNING, "Failed to notify association progress");
                                        }
                                    });
                                }).start();
                            } catch (SQLException ex) {
                                LOGGER.log(Level.WARNING, "Could not load association details for webhook", ex);
                            }

                            // Send confirmation email
                            // Inside the succeeded() block of processPayment()
                            try {
                                Don updatedDon = donService.getDonDetails(don.getId());
                                String donorEmail = getUserEmail(updatedDon.getUser().getId());
                                String donorName = getUserName(updatedDon.getUser().getId());
                                if (donorEmail != null && !donorEmail.isEmpty()) {
                                    new Thread(() -> {
                                        try {
                                            MailApi.sendConfirmationEmailWithPDF(
                                                    donorEmail,
                                                    donorName != null ? donorName : "Donateur",
                                                    updatedDon.getId(),
                                                    updatedDon.getMontant(),
                                                    updatedDon.getAssociation().getNom()
                                            );
                                            Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Envoi d'email", "Un email de confirmation avec reçu PDF a été envoyé au donateur"));
                                        } catch (Exception ex) {
                                            Platform.runLater(() -> showAlert(Alert.AlertType.WARNING, "Avertissement", "Le don a été confirmé, mais l'envoi de l'email de confirmation a échoué: " + ex.getMessage()));
                                        }
                                    }).start();
                                } else {
                                    showAlert(Alert.AlertType.WARNING, "Avertissement", "Le don a été confirmé, mais aucun email n'a été envoyé car l'adresse email est manquante.");
                                }
                            } catch (SQLException ex) {
                                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération des détails du don: " + ex.getMessage());
                            }

                            // Show success message
                            showAlert(Alert.AlertType.INFORMATION, "Paiement réussi",
                                    "Votre don de " + String.format("%.2f TND", don.getMontant()) +
                                            " à " + don.getAssociation().getNom() + " a été confirmé. Merci !");

                            // Navigate to ListDon.fxml
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Don/ListDon.fxml"));
                                Parent root = loader.load();
                                Stage stage = (Stage) payButton.getScene().getWindow();
                                double currentWidth = stage.getWidth();
                                double currentHeight = stage.getHeight();
                                Scene newScene = new Scene(root);
                                stage.setScene(newScene);
                                stage.setWidth(currentWidth);
                                stage.setHeight(currentHeight);
                                stage.setTitle("Donations");
                                stage.centerOnScreen();
                                stage.show();
                                System.out.println("Navigated to ListDon with size: " + currentWidth + "x" + currentHeight);
                            } catch (IOException e) {
                                showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger la page des dons: " + e.getMessage());
                            }
                        } catch (SQLException e) {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour du don: " + e.getMessage());
                            toggleControls(true);
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur de paiement", "Le paiement a été refusé. Veuillez vérifier vos informations et réessayer.");
                        toggleControls(true);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    showAlert(Alert.AlertType.ERROR, "Erreur de paiement", "Une erreur s'est produite: " + getException().getMessage());
                    toggleControls(true);
                });
            }
        };

        new Thread(paymentTask).start();
    }

    private String getUserEmail(int userId) {
        try {
            UserService userService = new UserService();
            User user = userService.getById(userId);
            if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                System.out.println("Retrieved user email: " + user.getEmail() + " for userId: " + userId);
                return user.getEmail();
            } else {
                System.err.println("User email not found or empty for userId: " + userId);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'email de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String getUserName(int userId) {
        try {
            UserService userService = new UserService();
            User user = userService.getById(userId);
            if (user != null) {
                String name = user.getNom() + " " + user.getPrenom();
                System.out.println("Retrieved user name: " + name + " for userId: " + userId);
                return name;
            } else {
                System.err.println("User not found for userId: " + userId);
                return "Donateur";
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du nom de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
            return "Donateur";
        }
    }

    private void toggleControls(boolean enabled) {
        payButton.setDisable(!enabled);
        cancelButton.setDisable(!enabled);
        cardNumberField.setDisable(!enabled);
        expiryMonthField.setDisable(!enabled);
        expiryYearField.setDisable(!enabled);
        cvcField.setDisable(!enabled);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        DialogPane dialogPane = alert.getDialogPane();
        try {
            dialogPane.getStylesheets().add(getClass().getResource("/Don/styles.css").toExternalForm());
            dialogPane.getStyleClass().add("custom-alert");
        } catch (Exception ignored) {}
        alert.showAndWait();
    }
}