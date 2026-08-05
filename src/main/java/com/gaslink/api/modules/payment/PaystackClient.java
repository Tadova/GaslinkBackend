package com.gaslink.api.modules.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaslink.api.modules.payment.dto.InitiatePaymentResponse;
import com.gaslink.api.shared.exception.PaymentVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PaystackClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.paystack.secret-key:sk_test_mock_paystack_secret_key_12345}")
    private String secretKey;

    @Value("${app.paystack.endpoint:https://api.paystack.co}")
    private String paystackEndpoint;

    public PaystackClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Initialize a Paystack transaction
     */
    public InitiatePaymentResponse initializeTransaction(String email, BigDecimal amount,
                                                         String reference, String callbackUrl) {
        try {
            String url = paystackEndpoint + "/transaction/initialize";

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("amount", amount.multiply(new BigDecimal("100")).intValue()); // Convert to kobo
            requestBody.put("reference", reference);
            requestBody.put("callback_url", callbackUrl);

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + secretKey);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Make the request
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());

                if (jsonResponse.get("status").asBoolean()) {
                    JsonNode data = jsonResponse.get("data");

                    InitiatePaymentResponse paymentResponse = new InitiatePaymentResponse();
                    paymentResponse.setAuthorizationUrl(data.get("authorization_url").asText());
                    paymentResponse.setReference(data.get("reference").asText());
                    paymentResponse.setAccessCode(data.get("access_code").asText());

                    log.info("✅ Paystack transaction initialized: {}", reference);
                    return paymentResponse;
                } else {
                    String message = jsonResponse.has("message") ?
                            jsonResponse.get("message").asText() : "Unknown error";
                    throw new PaymentVerificationException("Paystack initialization failed: " + message);
                }
            } else {
                throw new PaymentVerificationException("Paystack API error: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ Failed to initialize Paystack transaction: {}", e.getMessage());
            throw new PaymentVerificationException("Failed to initialize payment: " + e.getMessage());
        }
    }

    /**
     * Verify a Paystack transaction
     */
    public boolean verifyTransaction(String reference) {
        try {
            String url = paystackEndpoint + "/transaction/verify/" + reference;

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + secretKey);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Make the request
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());

                if (jsonResponse.get("status").asBoolean()) {
                    JsonNode data = jsonResponse.get("data");
                    String status = data.get("status").asText();

                    if ("success".equalsIgnoreCase(status)) {
                        log.info("✅ Paystack transaction verified: {}", reference);
                        return true;
                    } else {
                        log.warn("⚠️ Paystack transaction not successful: {} - {}", reference, status);
                        return false;
                    }
                } else {
                    String message = jsonResponse.has("message") ?
                            jsonResponse.get("message").asText() : "Unknown error";
                    log.error("❌ Paystack verification failed: {}", message);
                    return false;
                }
            } else {
                log.error("❌ Paystack API error: {}", response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("❌ Failed to verify Paystack transaction: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get transaction details
     */
    public JsonNode getTransactionDetails(String reference) {
        try {
            String url = paystackEndpoint + "/transaction/verify/" + reference;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + secretKey);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                if (jsonResponse.get("status").asBoolean()) {
                    return jsonResponse.get("data");
                }
            }
            return null;

        } catch (Exception e) {
            log.error("❌ Failed to get transaction details: {}", e.getMessage());
            return null;
        }
    }
}