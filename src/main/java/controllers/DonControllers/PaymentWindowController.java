package controllers.DonControllers;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import entities.Don;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.DonServices;

import java.sql.SQLException;

public class PaymentWindowController {

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

    /**
     * Initialise les écouteurs de perte de focus pour validation en temps réel
     */
    private void setupFocusListeners() {
        cardNumberField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                validateCardNumber();
            }
        });

        expiryMonthField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                validateExpiryDate();
            }
        });

        expiryYearField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                validateExpiryDate();
            }
        });

        cvcField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                validateCVC();
            }
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
        // Validation du numéro de carte (exactement 16 chiffres)
        cardNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {

                String filtered = newValue.replaceAll("[^0-9]", "");


                if (filtered.length() > 16) {
                    filtered = filtered.substring(0, 16);
                }


                if (!filtered.equals(newValue)) {
                    cardNumberField.setText(filtered);
                }
            }
        });

        // Validation du mois (1-12)
        expiryMonthField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Ne garder que les chiffres
                String filtered = newValue.replaceAll("[^0-9]", "");

                // Limiter à 2 caractères
                if (filtered.length() > 2) {
                    filtered = filtered.substring(0, 2);
                }

                // Vérifier si le mois est valide (1-12)
                try {
                    if (!filtered.isEmpty()) {
                        int month = Integer.parseInt(filtered);
                        if (month > 12) {
                            filtered = "12"; // Limiter à 12
                        } else if (month < 1 && filtered.length() >= 2) {
                            filtered = "01"; // Minimum 1
                        }
                    }
                } catch (NumberFormatException e) {

                }


                if (!filtered.equals(newValue)) {
                    expiryMonthField.setText(filtered);
                }
            }
        });

        expiryYearField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {

                String filtered = newValue.replaceAll("[^0-9]", "");


                if (filtered.length() > 2) {
                    filtered = filtered.substring(0, 2);
                }

                // Vérifier si l'année est valide (>= 25)
                try {
                    if (!filtered.isEmpty() && filtered.length() >= 2) {
                        int year = Integer.parseInt(filtered);
                        if (year < 25) {
                            filtered = "25"; // Minimum 25
                        }
                    }
                } catch (NumberFormatException e) {
                    // Ignorer, car on a déjà filtré pour ne garder que les chiffres
                }

                // Mettre à jour le champ si la valeur a changé
                if (!filtered.equals(newValue)) {
                    expiryYearField.setText(filtered);
                }
            }
        });


        cvcField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {

                String filtered = newValue.replaceAll("[^0-9]", "");


                if (filtered.length() > 3) {
                    filtered = filtered.substring(0, 3);
                }


                if (!filtered.equals(newValue)) {
                    cvcField.setText(filtered);
                }
            }
        });
    }

    /**
     * Valide le numéro de carte et affiche l'erreur si nécessaire
     * @return true si le numéro de carte est valide, false sinon
     */
    private boolean validateCardNumber() {
        String cardNumber = cardNumberField.getText().trim();
        if (cardNumber.isEmpty()) {
            setFieldError(cardNumberField, cardErrorLabel, "Le numéro de carte est requis");
            return false;
        } else if (cardNumber.length() != 16) {
            setFieldError(cardNumberField, cardErrorLabel, "Le numéro de carte doit contenir exactement 16 chiffres");
            return false;
        }

        // Si tout est correct, effacer l'erreur
        clearFieldError(cardNumberField, cardErrorLabel);
        return true;
    }

    /**
     * Valide la date d'expiration et affiche l'erreur si nécessaire
     * @return true si la date d'expiration est valide, false sinon
     */
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

        // Si tout est correct, effacer l'erreur
        clearFieldError(expiryMonthField, expiryErrorLabel);
        clearFieldError(expiryYearField, null);
        return true;
    }

    /**
     * Valide le code CVC et affiche l'erreur si nécessaire
     * @return true si le code CVC est valide, false sinon
     */
    private boolean validateCVC() {
        String cvc = cvcField.getText().trim();
        if (cvc.isEmpty()) {
            setFieldError(cvcField, cvcErrorLabel, "Le code CVC est requis");
            return false;
        } else if (cvc.length() != 3) {
            setFieldError(cvcField, cvcErrorLabel, "Le code CVC doit contenir exactement 3 chiffres");
            return false;
        }

        // Si tout est correct, effacer l'erreur
        clearFieldError(cvcField, cvcErrorLabel);
        return true;
    }

    /**
     * Ajoute une classe d'erreur au champ et définit le message d'erreur
     * @param field Le champ à marquer comme invalide
     * @param errorLabel Le label d'erreur à mettre à jour
     * @param errorMessage Le message d'erreur à afficher
     */
    private void setFieldError(TextField field, Label errorLabel, String errorMessage) {
        if (!field.getStyleClass().contains("invalid-field")) {
            field.getStyleClass().add("invalid-field");
        }

        if (errorLabel != null) {
            errorLabel.setText(errorMessage);
        }
    }

    /**
     * Supprime la classe d'erreur du champ et efface le message d'erreur
     * @param field Le champ à marquer comme valide
     * @param errorLabel Le label d'erreur à effacer (peut être null)
     */
    private void clearFieldError(TextField field, Label errorLabel) {
        field.getStyleClass().remove("invalid-field");

        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    /**
     * Valide tous les champs du formulaire de paiement
     */
    private void validateAllFields() {
        validateCardNumber();
        validateExpiryDate();
        validateCVC();
    }

    /**
     * Vérifie si tous les champs sont valides
     * @return true si tous les champs sont valides, false sinon
     */
    private boolean isFormValid() {
        return validateCardNumber() && validateExpiryDate() && validateCVC();
    }

    /**
     * Indique si le paiement a été confirmé
     * @return true si le paiement est confirmé, false sinon
     */
    public boolean isPaymentConfirmed() {
        return paymentConfirmed;
    }

    /**
     * Configure la fenêtre avec les détails du don
     * @param don Le don à traiter
     */
    public void setDon(Don don) {
        this.don = don;

        // Mettre à jour l'interface utilisateur avec les détails du don
        associationLabel.setText(don.getAssociation().getNom());
        montantLabel.setText(String.format("%.2f TND", don.getMontant()));
        referenceLabel.setText("DON-" + don.getId());
    }

    /**
     * Ferme la fenêtre actuelle
     */
    private void fermerFenetre() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Traite le paiement en utilisant Stripe
     */
    private void processPayment() {
        // Désactiver les boutons et afficher l'indicateur de progression
        toggleControls(false);
        progressIndicator.setVisible(true);

        Task<Boolean> paymentTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    // Conversion du montant en centimes pour Stripe (1 TND = 100 centimes)
                    long amountInCents = Math.round(don.getMontant() * 100);
                    String cardNumber = cardNumberField.getText().trim();

                    // Utiliser la méthode de test avec le numéro de carte fourni
                    String description = "Don pour " + don.getAssociation().getNom() + " (ID: " + don.getId() + ")";

                    Charge charge = StripePaymentService.createTestCharge(
                            amountInCents,
                            description,
                            "tnd",
                            cardNumber
                    );

                    // Vérifier le statut du paiement
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
                        paymentConfirmed = true;  // Définir le paiement comme confirmé
                        showAlert(Alert.AlertType.INFORMATION, "Paiement réussi",
                                "Votre don de " + String.format("%.2f TND", don.getMontant()) +
                                        " à " + don.getAssociation().getNom() + " a été confirmé. Merci !");
                        fermerFenetre();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur de paiement",
                                "Le paiement a été refusé. Veuillez vérifier vos informations et réessayer.");
                        toggleControls(true);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    showAlert(Alert.AlertType.ERROR, "Erreur de paiement",
                            "Une erreur s'est produite: " + getException().getMessage());
                    toggleControls(true);
                });
            }
        };

        // Démarrer la tâche dans un thread séparé
        new Thread(paymentTask).start();
    }

    /**
     * Active ou désactive les contrôles de l'interface utilisateur
     * @param enabled true pour activer, false pour désactiver
     */
    private void toggleControls(boolean enabled) {
        payButton.setDisable(!enabled);
        cancelButton.setDisable(!enabled);
        cardNumberField.setDisable(!enabled);
        expiryMonthField.setDisable(!enabled);
        expiryYearField.setDisable(!enabled);
        cvcField.setDisable(!enabled);
    }

    /**
     * Affiche une alerte
     * @param type Type d'alerte
     * @param title Titre de l'alerte
     * @param message Message de l'alerte
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Appliquer le style CSS
        DialogPane dialogPane = alert.getDialogPane();
        Scene scene = dialogPane.getScene();

        // Essayer d'appliquer le CSS si disponible
        try {
            scene.getStylesheets().add(getClass().getResource("/Don/styles.css").toExternalForm());
            dialogPane.getStyleClass().add("custom-alert");
        } catch (Exception e) {
            // Ignorer si le CSS n'est pas disponible
        }

        alert.showAndWait();
    }
}