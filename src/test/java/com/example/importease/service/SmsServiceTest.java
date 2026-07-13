package com.example.importease.service;

import com.example.importease.model.dto.BrevoEmailRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmsServiceTest {

    @Test
    void skipsSendingWhenApiKeyMissing() {
        SmsService smsService = new SmsService();
        ReflectionTestUtils.setField(smsService, "apiKey", "");
        ReflectionTestUtils.setField(smsService, "senderName", "ImportEase");
        ReflectionTestUtils.setField(smsService, "emailSenderName", "ImportEase");
        ReflectionTestUtils.setField(smsService, "emailSenderEmail", "hello@example.com");

        assertDoesNotThrow(() -> smsService.sendOtpSms("+1234567890", "123456"));
        assertDoesNotThrow(() -> smsService.sendOtpEmail("test@example.com", "123456"));
    }

    @Test
    void serializesPayloadUsingBrevoTemplateStructure() throws Exception {
        BrevoEmailRequest request = new BrevoEmailRequest(
                new BrevoEmailRequest.Sender("ImportEase", "noreply@importease.com"),
                Collections.singletonList(new BrevoEmailRequest.Recipient("user@example.com", "ImportEase User")),
                "Your ImportEase Login Code",
                "<p>Your code is 123456</p>"
        );
        request.setTextContent("Your code is 123456");
        request.setParams(Map.of("otp", "123456"));

        String json = new ObjectMapper().writeValueAsString(request);

        assertTrue(json.contains("\"sender\""));
        assertTrue(json.contains("\"to\""));
        assertTrue(json.contains("\"subject\""));
        assertTrue(json.contains("\"htmlContent\""));
        assertTrue(json.contains("\"textContent\""));
        assertTrue(json.contains("\"params\""));
    }
}
