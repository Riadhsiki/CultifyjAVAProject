package utils;

import entities.Association;
import entities.Don;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilitaire pour envoyer des notifications au webhook N8N
 */
public class WebhookUtil {
    private static final Logger LOGGER = Logger.getLogger(WebhookUtil.class.getName());
    private static final String WEBHOOK_URL = "http://localhost:5678/webhook/donation-milestone";

    /**
     * Notifie N8N qu'un don a été confirmé et qu'il faut vérifier les jalons de progression
     *
     * @param association L'association concernée par le don
     * @return true si la notification a été envoyée avec succès, false sinon
     */
    public static boolean notifyDonationProgress(Association association) {
        if (association == null) {
            LOGGER.log(Level.WARNING, "Association is null, cannot notify progress");
            return false;
        }

        try {
            // Calculer le pourcentage de progression
            double progressPercentage = association.getPourcentageProgression();

            // Préparer les données JSON à envoyer
            String jsonData = String.format(
                    "{\n" +
                            "  \"associationId\": %d,\n" +
                            "  \"associationName\": \"%s\",\n" +
                            "  \"progressPercentage\": %.2f,\n" +
                            "  \"targetAmount\": %.2f,\n" +
                            "  \"currentAmount\": %.2f\n" +
                            "}",
                    association.getId(),
                    association.getNom().replace("\"", "\\\""), // Échapper les guillemets
                    progressPercentage,
                    association.getMontantDesire(),
                    association.getMontantActuel()
            );

            // Établir la connexion HTTP
            URL url = new URL(WEBHOOK_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Envoyer les données
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Vérifier la réponse
            int responseCode = connection.getResponseCode();
            LOGGER.log(Level.INFO, "Webhook notification sent. Response code: {0}", responseCode);

            return responseCode >= 200 && responseCode < 300;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to notify donation progress via webhook", e);
            return false;
        }
    }
}