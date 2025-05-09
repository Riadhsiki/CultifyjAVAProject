package controllers.payment;

import models.Payment;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class PaymentConfirmationController {
    @FXML private Label eventTitleLabel;
    @FXML private Label amountLabel;
    @FXML private Label dateLabel;
    @FXML private Label transactionIdLabel;
    @FXML private Label statusLabel;

    private Consumer<Void> onConfirmationClosed;

    public void setPaymentData(Payment payment) {
        eventTitleLabel.setText(payment.getEvent().getTitre());
        amountLabel.setText(String.format("%.2f €", payment.getAmount()));
        dateLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        transactionIdLabel.setText(payment.getTransactionReference());
        statusLabel.setText(payment.getStatus());
    }

    public void setOnConfirmationClosed(Consumer<Void> callback) {
        this.onConfirmationClosed = callback;
    }

    @FXML
    private void handleClose() {
        if (onConfirmationClosed != null) {
            onConfirmationClosed.accept(null);
        }
    }
}