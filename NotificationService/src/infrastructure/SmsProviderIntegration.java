package infrastructure;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * SmsProviderIntegration is responsible for integrating with an external SMS provider
 * to send SMS messages. It handles the communication, error handling, and response processing.
 */
public class SmsProviderIntegration {

    private static final Logger LOGGER = Logger.getLogger(SmsProviderIntegration.class.getName());
    private static final String PROVIDER_URL = "https://smsprovider.com/api/v1/send";
    private static final String API_KEY = "api-key";
    private static final String CONTENT_TYPE = "application/json";

    /**
     * Send an SMS message through the provider.
     *
     * @param phoneNumber The recipient's phone number.
     * @param message     The message content to be sent.
     * @return true if the message was sent successfully, false otherwise.
     */
    public boolean sendSms(String phoneNumber, String message) {
        try {
            URL url = new URL(PROVIDER_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", CONTENT_TYPE);
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setDoOutput(true);

            String payload = buildPayload(phoneNumber, message);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    LOGGER.info("SMS sent successfully: " + response.toString());
                    return true;
                }
            } else {
                LOGGER.warning("Failed to send SMS. Response Code: " + responseCode);
                return false;
            }
        } catch (Exception e) {
            LOGGER.severe("Exception occurred while sending SMS: " + e.getMessage());
            return false;
        }
    }

    /**
     * Build the JSON payload to send to the SMS provider.
     *
     * @param phoneNumber The recipient's phone number.
     * @param message     The message content to be sent.
     * @return A JSON-formatted string containing the phone number and message.
     */
    private String buildPayload(String phoneNumber, String message) {
        Map<String, String> payload = new HashMap<>();
        payload.put("to", phoneNumber);
        payload.put("message", message);
        return new StringBuilder()
                .append("{")
                .append("\"to\": \"").append(phoneNumber).append("\",")
                .append("\"message\": \"").append(message).append("\"")
                .append("}")
                .toString();
    }

    /**
     * This method simulates retry logic for SMS sending in case of failure.
     *
     * @param phoneNumber The recipient's phone number.
     * @param message     The message content to be sent.
     * @param retries     The number of retry attempts.
     * @return true if the message was sent successfully within retry attempts, false otherwise.
     */
    public boolean sendWithRetry(String phoneNumber, String message, int retries) {
        int attempts = 0;
        while (attempts < retries) {
            if (sendSms(phoneNumber, message)) {
                return true;
            }
            attempts++;
            LOGGER.info("Retrying to send SMS, attempt: " + attempts);
        }
        LOGGER.warning("Failed to send SMS after " + retries + " attempts.");
        return false;
    }

    /**
     * This method allows sending bulk SMS by accepting a list of phone numbers.
     *
     * @param phoneNumbers A list of recipient phone numbers.
     * @param message      The message content to be sent to all recipients.
     */
    public void sendBulkSms(String[] phoneNumbers, String message) {
        for (String phoneNumber : phoneNumbers) {
            boolean success = sendSms(phoneNumber, message);
            if (!success) {
                LOGGER.warning("Failed to send SMS to: " + phoneNumber);
            }
        }
    }

    /**
     * Check the SMS status by querying the provider.
     *
     * @param messageId The ID of the SMS message sent.
     * @return The status of the message.
     */
    public String checkSmsStatus(String messageId) {
        try {
            URL url = new URL(PROVIDER_URL + "/status/" + messageId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    LOGGER.info("SMS status: " + response.toString());
                    return response.toString();
                }
            } else {
                LOGGER.warning("Failed to check SMS status. Response Code: " + responseCode);
                return "Failed to retrieve status.";
            }
        } catch (Exception e) {
            LOGGER.severe("Exception occurred while checking SMS status: " + e.getMessage());
            return "Error occurred.";
        }
    }

    /**
     * Cancel a scheduled SMS message by its message ID.
     *
     * @param messageId The ID of the SMS message to cancel.
     * @return true if the SMS was canceled successfully, false otherwise.
     */
    public boolean cancelSms(String messageId) {
        try {
            URL url = new URL(PROVIDER_URL + "/cancel/" + messageId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                LOGGER.info("SMS canceled successfully. Message ID: " + messageId);
                return true;
            } else {
                LOGGER.warning("Failed to cancel SMS. Response Code: " + responseCode);
                return false;
            }
        } catch (Exception e) {
            LOGGER.severe("Exception occurred while canceling SMS: " + e.getMessage());
            return false;
        }
    }
}