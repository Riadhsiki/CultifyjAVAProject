package controllers.DonControllers;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FlouciPaymentService {
    // Informations d'authentification pour l'API Flouci (mode test)
    private static final String APP_TOKEN = "60ddc7a7-4ca6-450f-b001-43977426ffb5";
    private static final String APP_SECRET = "4b203d88-fd47-416d-93d6-6c4492339ebc";
    private static final String API_BASE_URL = "https://developers.flouci.com/api";

    // Ajouter une méthode pour vérifier la connectivité
    public static boolean checkConnectivity() {
        try {
            URL url = new URL("https://developers.flouci.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000); // 5 secondes
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return (responseCode >= 200 && responseCode < 300);
        } catch (IOException e) {
            System.out.println("Erreur de connectivité: " + e.getMessage());
            return false;
        }
    }

    /**
     * Génère un lien de paiement Flouci
     *
     * @param amount Montant en millimes (30.5 TND = 30500 millimes)
     * @param description Description du paiement
     * @param trackingId ID de suivi pour la transaction (par exemple, l'ID du don)
     * @return Map contenant le lien de paiement et l'ID de paiement
     * @throws IOException En cas d'erreur avec l'API Flouci
     */
    public static Map<String, String> generatePaymentLink(long amount, String description, String trackingId) throws IOException {
        // Vérifier la connectivité avant d'envoyer la requête
        if (!checkConnectivity()) {
            throw new IOException("Impossible de se connecter à Flouci. Vérifiez votre connexion Internet.");
        }

        URL url = new URL(API_BASE_URL + "/generate_payment");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(10000); // 10 secondes
        connection.setReadTimeout(15000); // 15 secondes

        // Configuration de la requête
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Préparer le corps de la requête
        JSONObject requestBody = new JSONObject();
        requestBody.put("app_token", APP_TOKEN);
        requestBody.put("app_secret", APP_SECRET);
        requestBody.put("amount", amount);
        requestBody.put("accept_card", true);
        requestBody.put("session_timeout_secs", 1200); // 20 minutes

        // URLs de redirection après paiement
        requestBody.put("success_link", "https://yourapp.com/success?donid=" + trackingId);
        requestBody.put("fail_link", "https://yourapp.com/fail?donid=" + trackingId);
        requestBody.put("developer_tracking_id", trackingId);

        System.out.println("Requête Flouci: " + requestBody.toString());

        // Envoyer la requête
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Lire la réponse
        int responseCode = connection.getResponseCode();
        System.out.println("Code de réponse: " + responseCode);

        if (responseCode == 201) { // Créé avec succès
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                String responseText = response.toString();
                System.out.println("Réponse: " + responseText);

                // Analyser la réponse JSON
                JSONObject jsonResponse = new JSONObject(responseText);
                JSONObject result = jsonResponse.getJSONObject("result");

                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("paymentLink", result.getString("link"));
                responseMap.put("paymentId", result.getString("payment_id"));
                return responseMap;
            }
        } else {
            // Lire le corps de la réponse d'erreur
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    connection.getErrorStream() != null ? connection.getErrorStream() : connection.getInputStream(),
                    StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println("Erreur détaillée: " + response.toString());
            } catch (Exception e) {
                System.out.println("Impossible de lire le corps de l'erreur: " + e.getMessage());
            }

            throw new IOException("Erreur lors de la génération du lien de paiement: " + responseCode);
        }
    }

    /**
     * Simule un paiement direct en utilisant des informations de carte ou Wallet
     * Cette méthode est utilisée pour les tests uniquement
     *
     * @param paymentMethod "WALLET" ou "CARD"
     * @param cardNumber Numéro de carte (pour CARD)
     * @param cardExpiry Date d'expiration (pour CARD)
     * @param cardCVV CVV (pour CARD)
     * @param walletCode Code Wallet (pour WALLET)
     * @param amount Montant en millimes
     * @param trackingId ID de suivi
     * @return ID de paiement simulé
     */
    public static String simulateDirectPayment(String paymentMethod, String cardNumber,
                                               String cardExpiry, String cardCVV,
                                               String walletCode, long amount, String trackingId) {
        // Simulation de paiement pour les tests

        // Vérification du succès basée sur les informations de test
        boolean isSuccess = false;

        if ("WALLET".equals(paymentMethod)) {
            // Code 111111 pour succès avec wallet
            isSuccess = "111111".equals(walletCode);
        } else if ("CARD".equals(paymentMethod)) {
            // Carte 4242 4242 4242 4242 pour succès
            isSuccess = cardNumber != null && cardNumber.replace(" ", "").equals("4242424242424242");
        }

        // Générer un ID de paiement simulé
        String paymentId = "TEST-" + System.currentTimeMillis() + "-" + trackingId;

        System.out.println("Paiement simulé: " + paymentMethod + ", Succès: " + isSuccess + ", ID: " + paymentId);

        return paymentId;
    }

    /**
     * Vérifie l'état d'un paiement
     *
     * @param paymentId ID du paiement à vérifier
     * @return true si le paiement a réussi
     * @throws IOException En cas d'erreur avec l'API Flouci
     */
    public static boolean verifyPayment(String paymentId) throws IOException {
        // Si c'est un paiement de test simulé
        if (paymentId != null && paymentId.startsWith("TEST-")) {
            // Pour les tests, les paiements réussis pour 4242... ou code 111111
            return paymentId.contains("-DON-");
        }

        // Tentative de vérifier le paiement réel via l'API
        try {
            if (!checkConnectivity()) {
                throw new IOException("Impossible de se connecter à Flouci. Vérifiez votre connexion Internet.");
            }

            URL url = new URL(API_BASE_URL + "/verify_payment/" + paymentId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000); // 10 secondes
            connection.setReadTimeout(15000); // 15 secondes

            // Configuration de la requête
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("apppublic", APP_TOKEN);
            connection.setRequestProperty("appsecret", APP_SECRET);

            // Lire la réponse
            int responseCode = connection.getResponseCode();
            System.out.println("Code de réponse vérification: " + responseCode);

            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    String responseText = response.toString();
                    System.out.println("Réponse vérification: " + responseText);

                    // Analyser la réponse JSON
                    JSONObject jsonResponse = new JSONObject(responseText);
                    boolean success = jsonResponse.getBoolean("success");

                    if (success) {
                        JSONObject result = jsonResponse.getJSONObject("result");
                        return "SUCCESS".equals(result.getString("status"));
                    }
                }
            } else {
                // Lire le corps de la réponse d'erreur
                try (BufferedReader br = new BufferedReader(new InputStreamReader(
                        connection.getErrorStream() != null ? connection.getErrorStream() : connection.getInputStream(),
                        StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println("Erreur détaillée vérification: " + response.toString());
                } catch (Exception e) {
                    System.out.println("Impossible de lire le corps de l'erreur: " + e.getMessage());
                }
            }

            return false;
        } catch (IOException e) {
            System.out.println("Erreur lors de la vérification du paiement: " + e.getMessage());
            throw e;
        }
    }
}