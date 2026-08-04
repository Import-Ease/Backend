package com.example.importease.service;

import com.example.importease.model.dto.SendGridEmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @Value("${sendgrid.api.key}")
    private String sendgridApiKey;

    @Value("${sendgrid.from.name}")
    private String emailSenderName;

    @Value("${sendgrid.from.email}")
    private String emailSenderEmail;

    @Value("${twilio.account.sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token}")
    private String twilioAuthToken;

    @Value("${twilio.from.number}")
    private String twilioFromNumber;

    private final String SENDGRID_EMAIL_URL = "https://api.sendgrid.com/v3/mail/send";
    private final RestTemplate restTemplate = new RestTemplate();

    // === METHOD 1: SEND OTP VIA SMS (Twilio) ===
    public void sendOtpSms(String phoneNumber, String otpCode) {
        if (isBlank(twilioAccountSid) || isBlank(twilioAuthToken)) {
            log.warn("Twilio SMS skipped: missing TWILIO_ACCOUNT_SID or TWILIO_AUTH_TOKEN.");
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(twilioAccountSid, twilioAuthToken);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("From", twilioFromNumber);
        form.add("To", phoneNumber);
        form.add("Body", "Your ImportEase login code is: " + otpCode + ". Do not share this code with anyone.");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(form, headers);
        String url = "https://api.twilio.com/2010-04-01/Accounts/" + twilioAccountSid + "/Messages.json";

        log.info("Sending OTP SMS to {}", phoneNumber);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Twilio SMS failed: status {} body {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Twilio SMS request failed with status " + response.getStatusCode());
            }
            log.info("Twilio SMS sent successfully to {}", phoneNumber);
        } catch (HttpStatusCodeException e) {
            log.error("Twilio SMS rejected: status {} body {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Twilio SMS request rejected with status " + e.getStatusCode(), e);
        }
    }

    // === METHOD 2: SEND OTP VIA EMAIL (SendGrid) ===
    public void sendOtpEmail(String recipientEmail, String otpCode) {
        if (isBlank(sendgridApiKey)) {
            log.warn("SendGrid email skipped: missing SENDGRID_API_KEY.");
            return;
        }

        String htmlContent = "<html><body>"
                + "<h2>Welcome to ImportEase</h2>"
                + "<p>Your security verification code is:</p>"
                + "<h1 style='color: #4F46E5; letter-spacing: 2px;'>" + otpCode + "</h1>"
                + "<p>This code is valid for 5 minutes. Please do not share it with anyone.</p>"
                + "</body></html>";
        String textContent = "Your ImportEase verification code is " + otpCode + ". This code is valid for 5 minutes.";

        SendGridEmailRequest requestPayload = new SendGridEmailRequest(
                emailSenderName, emailSenderEmail, recipientEmail,
                "Your ImportEase Login Code", htmlContent, textContent);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("accept", "application/json");
        headers.set("Authorization", "Bearer " + sendgridApiKey);

        HttpEntity<SendGridEmailRequest> requestEntity = new HttpEntity<>(requestPayload, headers);

        log.info("Sending OTP email to {}", recipientEmail);
        try {
            ResponseEntity<String> response = restTemplate.exchange(SENDGRID_EMAIL_URL, HttpMethod.POST, requestEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("SendGrid email failed: status {} body {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("SendGrid email request failed with status " + response.getStatusCode());
            }
            log.info("SendGrid email success: status {}", response.getStatusCode());
        } catch (HttpStatusCodeException e) {
            log.error("SendGrid email rejected: status {} body {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("SendGrid email request rejected with status " + e.getStatusCode(), e);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}