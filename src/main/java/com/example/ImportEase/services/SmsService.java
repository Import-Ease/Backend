package com.example.ImportEase.services;

import com.example.ImportEase.dtos.BrevoSmsRequest;
import com.example.ImportEase.dtos.BrevoEmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SmsService {

    @Value("${brevo.api.key}")
    private String apiKey;

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
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("accept", "application/json");
        headers.set("api-key", apiKey);

        String message = "Your ImportEase login code is: " + otpCode + ". Do not share this code with anyone.";
        String cleanPhone = phoneNumber.replace("+", "");

        BrevoSmsRequest requestPayload = new BrevoSmsRequest(senderName, cleanPhone, message, "marketing");
        HttpEntity<BrevoSmsRequest> requestEntity = new HttpEntity<>(requestPayload, headers);

        try {
            restTemplate.exchange(BREVO_SMS_URL, HttpMethod.POST, requestEntity, String.class);
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
        }
    }

    // === METHOD 2: SEND OTP VIA EMAIL ===
    public void sendOtpEmail(String recipientEmail, String otpCode) {
        // Here are the lines you asked about! They config the request for Brevo's email API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("accept", "application/json");
        headers.set("api-key", apiKey);

        String htmlBody = "<html><body>"
                + "<h2>Welcome to ImportEase</h2>"
                + "<p>Your security verification code is:</p>"
                + "<h1 style='color: #4F46E5; letter-spacing: 2px;'>" + otpCode + "</h1>"
                + "<p>This code is valid for 10 minutes. Please do not share it with anyone.</p>"
                + "</body></html>";

        BrevoEmailRequest.Sender senderObj = new BrevoEmailRequest.Sender(emailSenderName, emailSenderEmail);
        BrevoEmailRequest.Recipient recipientObj = new BrevoEmailRequest.Recipient(recipientEmail, "ImportEase User");

        BrevoEmailRequest emailPayload = new BrevoEmailRequest(
                senderObj,
                java.util.Collections.singletonList(recipientObj),
                "Your ImportEase Login Code",
                htmlBody
        );

        HttpEntity<BrevoEmailRequest> requestEntity = new HttpEntity<>(emailPayload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(BREVO_EMAIL_URL, HttpMethod.POST, requestEntity, String.class);
            System.out.println("Brevo Email Response: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
        }
    }
}