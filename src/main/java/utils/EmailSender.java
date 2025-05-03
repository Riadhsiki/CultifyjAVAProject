package utils;

import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailSender {

    private static final String FROM_EMAIL = "riadhtr21@gmail.com"; // Your Gmail address
    private static final String APP_PASSWORD = "bltembmuyaccjwbm";   // Your Gmail App Password

    private Session createEmailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });
    }

    // Original method for plain text emails
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            Session session = createEmailSession();

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("✅ Email sent successfully to " + toEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Failed to send email to " + toEmail);
            e.printStackTrace();
            throw new RuntimeException("Failed to send email", e);
        }
    }

    // Method to send email with LogoCultify.png attachment
    public void sendEmailWithAttachment(String toEmail, String subject, String body, String prenom, String username) {
        try {
            Session session = createEmailSession();

            // Create MimeMessage
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            // Create the message body part
            MimeBodyPart textPart = new MimeBodyPart();
            String personalizedBody = body.replace("{prenom}", prenom).replace("{username}", username);
            textPart.setText(personalizedBody);

            // Create the attachment part
            MimeBodyPart attachmentPart = new MimeBodyPart();
            // Access LogoCultify.png from resources
            InputStream imageStream = getClass().getResourceAsStream("/images/LogoCultify.png");
            if (imageStream == null) {
                throw new IOException("LogoCultify.png not found in resources");
            }

            // Read the image into a byte array
            byte[] imageBytes = imageStream.readAllBytes();
            imageStream.close();

            // Attach the image using ByteArrayDataSource
            ByteArrayDataSource dataSource = new ByteArrayDataSource(imageBytes, "image/png");
            attachmentPart.setDataHandler(new DataHandler(dataSource));
            attachmentPart.setFileName("LogoCultify.png");

            // Create Multipart
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            // Set the multipart content
            message.setContent(multipart);

            // Send the email
            Transport.send(message);
            System.out.println("✅ Email with attachment sent successfully to " + toEmail);

        } catch (MessagingException | IOException e) {
            System.err.println("❌ Failed to send email with attachment to " + toEmail);
            e.printStackTrace();
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }
}
