package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;

public class ProfanityFilter {
    private static final String API_KEY = "AIzaSyCn9ZEuOBuQXDpaw0_M5LAMB06dan9RfT8";
    private static final String API_URL = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + API_KEY;

    public static double checkProfanity(String text) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        // Créer la requête JSON
        JsonObject requestBody = new JsonObject();

        // Ajouter le commentaire
        JsonObject comment = new JsonObject();
        comment.addProperty("text", text);
        requestBody.add("comment", comment);

        // Ajouter les attributs demandés (TOXICITY)
        JsonObject attributes = new JsonObject();
        attributes.add("TOXICITY", new JsonObject()); // Objet vide pour TOXICITY
        requestBody.add("requestedAttributes", attributes);

        // Ajouter les langues
        JsonArray languages = new JsonArray();
        languages.add("fr"); // Langue française
        requestBody.add("languages", languages);

        // Envoyer la requête POST
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), gson.toJson(requestBody)
        );
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        // Exécuter la requête
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Erreur API : " + response);

            // Parser la réponse
            JsonObject responseJson = gson.fromJson(response.body().string(), JsonObject.class);
            return responseJson
                    .getAsJsonObject("attributeScores")
                    .getAsJsonObject("TOXICITY")
                    .getAsJsonObject("summaryScore")
                    .get("value")
                    .getAsDouble();
        }
    }

    public static void main(String[] args) {
        try {
            String reclamation = "C'est un service nul et stupide !";
            double toxicityScore = checkProfanity(reclamation);
            System.out.println("Score de toxicité : " + toxicityScore);
            if (toxicityScore > 0.7) {
                System.out.println("Contenu inapproprié détecté !");
            } else {
                System.out.println("Contenu acceptable.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}