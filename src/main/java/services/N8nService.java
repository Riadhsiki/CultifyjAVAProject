package services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.json.JSONObject;

/**
 * Service class to handle n8n webhook integration
 */
public class N8nService {

    // Base URL for your n8n installation
    private static final String N8N_WEBHOOK_BASE_URL = "http://localhost:5678/webhook/";

    // Specific webhook endpoints
    private static final String MILESTONE_50_WEBHOOK = N8N_WEBHOOK_BASE_URL + "milestone-50-percent";
    private static final String MILESTONE_100_WEBHOOK = N8N_WEBHOOK_BASE_URL + "milestone-complete";

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Notify n8n that an association has reached 50% of its funding goal
     *
     * @param associationId the ID of the association
     * @param associationName the name of the association
     * @param currentAmount the current donation amount
     * @param goalAmount the target donation goal
     * @return true if notification was successful
     */
    public boolean notifyMilestone50Percent(int associationId, String associationName,
                                            double currentAmount, double goalAmount) {
        JSONObject payload = new JSONObject();
        payload.put("associationId", associationId);
        payload.put("associationName", associationName);
        payload.put("currentAmount", currentAmount);
        payload.put("goalAmount", goalAmount);
        payload.put("percentComplete", (currentAmount / goalAmount) * 100);
        payload.put("milestone", "50%");

        return sendWebhook(MILESTONE_50_WEBHOOK, payload);
    }

    /**
     * Notify n8n that an association has reached 100% of its funding goal
     *
     * @param associationId the ID of the association
     * @param associationName the name of the association
     * @param currentAmount the current donation amount
     * @param goalAmount the target donation goal
     * @return true if notification was successful
     */
    public boolean notifyMilestone100Percent(int associationId, String associationName,
                                             double currentAmount, double goalAmount) {
        JSONObject payload = new JSONObject();
        payload.put("associationId", associationId);
        payload.put("associationName", associationName);
        payload.put("currentAmount", currentAmount);
        payload.put("goalAmount", goalAmount);
        payload.put("percentComplete", (currentAmount / goalAmount) * 100);
        payload.put("milestone", "100%");

        return sendWebhook(MILESTONE_100_WEBHOOK, payload);
    }

    /**
     * Helper method to send webhook requests to n8n
     *
     * @param webhookUrl the URL of the webhook endpoint
     * @param payload the JSON payload to send
     * @return true if request was successful
     */
    private boolean sendWebhook(String webhookUrl, JSONObject payload) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (IOException | InterruptedException e) {
            System.err.println("Error sending webhook to n8n: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}