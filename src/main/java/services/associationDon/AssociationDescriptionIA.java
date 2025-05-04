package services.associationDon;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AssociationDescriptionIA {
    private static final String API_URL = "https://api-inference.huggingface.co/models/tiiuae/falcon-7b-instruct";
    private static final String API_TOKEN = "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";

    public static String generateDescription(String nomAssociation, String but) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000); // 30 secondes de timeout
            conn.setReadTimeout(30000);

            // Prompt plus clair avec instructions précises
            String prompt = "Génère UNIQUEMENT la description (sans répéter la question) pour une association nommée \"" + nomAssociation +
                    "\" dont le but est \"" + but + "\". Décris ses activités, valeurs et impacts. " +
                    "Commence directement par le contenu descriptif sans introduction.";

            JSONObject inputPayload = new JSONObject()
                    .put("inputs", prompt)
                    .put("parameters", new JSONObject()
                            .put("max_new_tokens", 250)
                            .put("temperature", 0.7)
                            .put("return_full_text", false)); // Important pour ne pas récupérer le prompt

            String inputJson = inputPayload.toString();

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = inputJson.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Code de réponse API: " + responseCode);

            if (responseCode != 200) {
                try (Scanner scanner = new Scanner(conn.getErrorStream(), "utf-8")) {
                    String errorResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                    System.out.println("Erreur API: " + errorResponse);
                }
                return "L'association " + nomAssociation + " se consacre à " + but + ". "
                        + "Elle œuvre activement pour atteindre ses objectifs grâce à l'engagement de ses membres.";
            }

            StringBuilder response = new StringBuilder();
            try (Scanner scanner = new Scanner(conn.getInputStream(), "utf-8")) {
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
            }

            System.out.println("Réponse brute: " + response.toString());

            // Traitement de la réponse JSON
            try {
                JSONArray jsonResponse = new JSONArray(response.toString());
                String generatedText = jsonResponse.getJSONObject(0).getString("generated_text");
                return cleanGeneratedText(generatedText, prompt, nomAssociation);
            } catch (Exception e) {
                System.out.println("Erreur de parsing JSON: " + e.getMessage());

                // Si le parsing JSON échoue, essayons une approche différente
                String rawText = response.toString();
                if (rawText.contains("generated_text")) {
                    int startIndex = rawText.indexOf("generated_text") + "generated_text".length() + 3; // +3 pour ":"\"
                    int endIndex = rawText.indexOf("\"", startIndex + 1);
                    if (startIndex > 0 && endIndex > startIndex) {
                        String extractedText = rawText.substring(startIndex, endIndex);
                        return cleanGeneratedText(extractedText, prompt, nomAssociation);
                    }
                }

                // Description de secours
                return "L'association " + nomAssociation + " se dédie à " + but + " avec passion et détermination. "
                        + "Elle mobilise ses ressources et son expertise pour avoir un impact positif dans ce domaine.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception complète: " + e.toString());

            // Description de secours en cas d'erreur
            return "L'association " + nomAssociation + " œuvre dans le domaine de " + but + ". "
                    + "Ses membres sont engagés à faire une différence tangible à travers diverses initiatives et projets.";
        }
    }

    private static String cleanGeneratedText(String generatedText, String prompt, String nomAssociation) {
        if (generatedText == null || generatedText.isEmpty()) {
            return "L'association " + nomAssociation + " se consacre à des activités liées à " + prompt + ".";
        }

        // 1. Supprimer le prompt de la réponse
        String cleaned = generatedText.replace(prompt, "").trim();

        // 2. Supprimer les préfixes courants de la réponse IA
        String[] unwantedPrefixes = {
                "Voici la description:",
                "La description est:",
                "Description:",
                "Voici ce que je peux vous dire:",
                "En réponse à votre demande,",
                "Voici une description pour",
                "Je vais générer",
                "Bien sûr,",
                "D'accord,",
                "Voici"
        };

        for (String prefix : unwantedPrefixes) {
            if (cleaned.startsWith(prefix)) {
                cleaned = cleaned.substring(prefix.length()).trim();
            }
        }

        // 3. Nettoyer les références répétées au nom
        cleaned = cleaned.replaceAll("(?i)L'association " + nomAssociation + " est", "L'association est")
                .replaceAll("(?i)" + nomAssociation + " est une association", "L'association est")
                .replaceAll("(?i)L'association " + nomAssociation, "L'association")
                .trim();

        // 4. Nettoyer les sauts de ligne excessifs et espaces multiples
        cleaned = cleaned.replace("\n", " ").replaceAll(" +", " ");

        // 5. Capitaliser la première lettre
        if (!cleaned.isEmpty()) {
            cleaned = cleaned.substring(0, 1).toUpperCase() + cleaned.substring(1);
        }

        // 6. S'assurer que la description commence par une phrase cohérente
        if (!cleaned.startsWith("L'association") && !cleaned.startsWith("Cette association")) {
            cleaned = "L'association " + nomAssociation + " " + cleaned;
        }

        // 7. Si après tout ce nettoyage, la description est trop courte
        if (cleaned.length() < 50) {
            return "L'association " + nomAssociation + " se consacre à " + prompt +
                    " avec dévouement. Ses membres travaillent activement pour créer un impact positif et durable.";
        }

        return cleaned;
    }
}