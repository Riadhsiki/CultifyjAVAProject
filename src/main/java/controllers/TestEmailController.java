package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import utils.EmailSender; // ✅ Correct import (your class is called EmailSender, not MailSender)

public class TestEmailController {

    @FXML
    private void handleSendEmail() {
        EmailSender emailSender = new EmailSender(); // ✅ Use the correct class name

        try {
            // Send test email
            emailSender.sendEmail(
                    "riadhtr21@gmail.com", // <-- Put your real destination email here
                    "Test Email from JavaFX",
                    "Hello, this is a test email sent from the Test Interface!"
            );

            // Show success popup
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("✅ Test email has been sent successfully!");
            alert.showAndWait();

        } catch (Exception e) {
            // Show error popup
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to send email");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
