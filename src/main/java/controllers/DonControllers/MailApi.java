package controllers.DonControllers;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import java.util.Properties;

public class MailApi {
    private static final String FROM_EMAIL = "codedon10@gmail.com"; // Remplacez par votre email
    private static final String PASSWORD = "abwr jajg odpf ihqy";

    public static void sendConfirmationEmailWithPDF(String toEmail, String donorName, int donId, double montant, String associationName) {
        // Configuration des propriétés pour le serveur SMTP
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        // Ajouter ces propriétés pour le débogage et la gestion des timeout
        properties.put("mail.debug", "true");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.connectiontimeout", "10000");

        // Désactiver le SSL pour tester
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        // Créer une session avec authentification
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        try {
            // Générer le PDF
            byte[] pdfBytes = PDFGenerator.generateDonConfirmationPDF(
                    donorName,
                    toEmail,
                    donId,
                    associationName,
                    montant
            );

            // Création du message avec pièce jointe
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Confirmation de votre don");

            // Partie texte du message
            String htmlContent =
                    "<html><body>" +
                            "<h2>Confirmation de don</h2>" +
                            "<p>Cher(e) " + donorName + ",</p>" +
                            "<p>Nous vous remercions pour votre généreuse contribution de <b>" + montant + " TND</b> " +
                            "à l'association <b>" + associationName + "</b>.</p>" +
                            "<p>Votre don a été confirmé et sera utilisé pour soutenir nos activités.</p>" +
                            "<p>Vous trouverez ci-joint votre reçu de don au format PDF.</p>" +
                            "<p>Cordialement,<br>L'équipe de gestion des dons</p>" +
                            "</body></html>";

            // Créer les parties du message multipart
            Multipart multipart = new MimeMultipart();

            // Partie HTML
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(htmlContent, "text/html; charset=utf-8");
            multipart.addBodyPart(textPart);

            // Partie pièce jointe PDF
            MimeBodyPart pdfPart = new MimeBodyPart();
            ByteArrayDataSource dataSource = new ByteArrayDataSource(pdfBytes, "application/pdf");
            pdfPart.setDataHandler(new DataHandler(dataSource));
            pdfPart.setFileName("confirmation_don_" + donId + ".pdf");
            multipart.addBodyPart(pdfPart);

            // Associer le multipart au message
            message.setContent(multipart);

            // Envoi du message
            Transport.send(message);

            System.out.println("Email de confirmation avec PDF envoyé avec succès à " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode originale sans PDF (pour la rétrocompatibilité)
    public static void sendConfirmationEmail(String toEmail, String donorName, double montant, String associationName) {
        // Configuration des propriétés pour le serveur SMTP
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        // Ajouter ces propriétés pour le débogage et la gestion des timeout
        properties.put("mail.debug", "true");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.connectiontimeout", "10000");

        // Désactiver le SSL pour tester
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        // Créer une session avec authentification
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        try {
            // Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Confirmation de votre don");

            // Corps du message
            String htmlContent =
                    "<html><body>" +
                            "<h2>Confirmation de don</h2>" +
                            "<p>Cher(e) " + donorName + ",</p>" +
                            "<p>Nous vous remercions pour votre généreuse contribution de <b>" + montant + " TND</b> " +
                            "à l'association <b>" + associationName + "</b>.</p>" +
                            "<p>Votre don a été confirmé et sera utilisé pour soutenir nos activités.</p>" +
                            "<p>Cordialement,<br>L'équipe de gestion des dons</p>" +
                            "</body></html>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            // Envoi du message
            Transport.send(message);

            System.out.println("Email de confirmation envoyé avec succès à " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}