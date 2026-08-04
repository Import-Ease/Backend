package com.example.importease.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SmsServiceTest {

    @Test
    void skipsSendingWhenApiKeyMissing() {
        SmsService smsService = new SmsService();
        ReflectionTestUtils.setField(smsService, "sendgridApiKey", "");
        ReflectionTestUtils.setField(smsService, "emailSenderName", "ImportEase");
        ReflectionTestUtils.setField(smsService, "emailSenderEmail", "hello@example.com");
        ReflectionTestUtils.setField(smsService, "twilioAccountSid", "");
        ReflectionTestUtils.setField(smsService, "twilioAuthToken", "");

        assertDoesNotThrow(() -> smsService.sendOtpSms("+1234567890", "123456"));
        assertDoesNotThrow(() -> smsService.sendOtpEmail("test@example.com", "123456"));
    }
}