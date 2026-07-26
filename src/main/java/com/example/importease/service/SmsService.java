package com.example.importease.service;

import com.example.importease.model.dto.BrevoEmailRequest;
import com.example.importease.model.dto.BrevoSmsRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class SmsService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sms.sender}")
    private String senderName;

    @Value("${brevo.email.sender.name}")
    private String emailSenderName;

    @Value("${brevo.email.sender.email}")
    private String emailSenderEmail;

    private final String BREVO_SMS_URL = "https://api.brevo.com/v3/transactionalSMS/send";
    private final String BREVO_EMAIL_URL = "https://api.brevo.com/v3/smtp/email";
    private final RestTemplate restTemplate = new RestTemplate();

    // === METHOD 1: SEND OTP VIA SMS ===
    public void sendOtpSms(String phoneNumber, String otpCode) {
        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            System.err.println("Brevo SMS skipped: missing api key.");
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("accept", "application/json");
        headers.set("api-key", brevoApiKey);

        String message = "Your ImportEase login code is: " + otpCode + ". Do not share this code with anyone.";
        String cleanPhone = phoneNumber.replace("+", "");

        BrevoSmsRequest requestPayload = new BrevoSmsRequest(senderName, cleanPhone, message, "marketing");
        HttpEntity<BrevoSmsRequest> requestEntity = new HttpEntity<>(requestPayload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(BREVO_SMS_URL, HttpMethod.POST, requestEntity, String.class);
            System.out.println("Brevo SMS response: " + response.getStatusCode() + " " + response.getBody());
        } catch (HttpClientErrorException e) {
            System.err.println("Brevo SMS rejected the request: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
        }
    }

    // === METHOD 2: SEND OTP VIA EMAIL (Brevo) ===
    public void sendOtpEmail(String recipientEmail, String otpCode) {
        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            System.err.println("Brevo Email skipped: missing api key.");
            return;
        }

        String htmlContent = "<html><body>"
                + "<h2>Welcome to ImportEase</h2>"
                + "<p>Your security verification code is:</p>"
                + "<h1 style='color: #4F46E5; letter-spacing: 2px;'>" + otpCode + "</h1>"
                + "<p>This code is valid for 10 minutes. Please do not share it with anyone.</p>"
                + "</body></html>";
        String textContent = "Your ImportEase verification code is " + otpCode + ". This code is valid for 10 minutes.";

        BrevoEmailRequest requestPayload = new BrevoEmailRequest(
                emailSenderName, emailSenderEmail, recipientEmail,
                "Your ImportEase Login Code", htmlContent, textContent);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("accept", "application/json");
        headers.set("api-key", brevoApiKey);

        HttpEntity<BrevoEmailRequest> requestEntity = new HttpEntity<>(requestPayload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(BREVO_EMAIL_URL, HttpMethod.POST, requestEntity, String.class);
            System.out.println("Brevo Email response: " + response.getStatusCode() + " " + response.getBody());
        } catch (HttpClientErrorException e) {
            System.err.println("Brevo Email rejected the request: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}