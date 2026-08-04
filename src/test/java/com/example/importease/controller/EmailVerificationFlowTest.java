package com.example.importease.controller;

import com.example.importease.service.OtpService;
import com.example.importease.service.SmsService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end coverage of the registration OTP email-verification workflow:
 * register -> OTP dispatched (SendGrid) -> login blocked until verified ->
 * verify-otp -> resend-otp throttling -> login succeeds.
 *
 * The OTP is captured at the SmsService.sendOtpEmail seam so no real email
 * is sent (the local SENDGRID_API_KEY is empty) and no plaintext code is
 * ever exposed by the application.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmailVerificationFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OtpService otpService;

    @SpyBean
    private SmsService smsService;

    private static int counter = 0;

    private String registerJson(String username, String email) {
        return """
                {"username": "%s", "email": "%s", "password": "TestPass123!"}
                """.formatted(username, email);
    }

    private String loginJson(String identifier) {
        return """
                {"username": "%s", "password": "TestPass123!"}
                """.formatted(identifier);
    }

    private String verifyJson(String email, String code) {
        return """
                {"identifier": "%s", "otpCode": "%s"}
                """.formatted(email, code);
    }

    private String registerAndCaptureCode(String email, String codeHolderName) throws Exception {
        String username = "flowuser" + (++counter);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(username, email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requiresVerification").value(true))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.accessToken").doesNotExist());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(smsService).sendOtpEmail(eq(email), captor.capture());
        return captor.getValue();
    }

    private void backdate(String fieldName, String key, Instant time) {
        @SuppressWarnings("unchecked")
        Map<String, Instant> map = (Map<String, Instant>) ReflectionTestUtils.getField(otpService, fieldName);
        map.put(key, time);
    }

    @Test
    void fullLifecycle_registerLoginBlockedVerifyThenLogin() throws Exception {
        String email = "flow.happy" + (++counter) + "@example.com";
        String code = registerAndCaptureCode(email, "");

        // Login is blocked while email is unverified
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", containsString("Email not verified")));

        // Correct code verifies the email
        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyJson(email, code)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Email verified")));

        // Login now succeeds and issues a token
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void wrongCodeRejectedAndConsumed() throws Exception {
        String email = "flow.wrong" + (++counter) + "@example.com";
        String code = registerAndCaptureCode(email, "");

        // Wrong code is rejected
        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyJson(email, "000000")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Invalid or expired")));

        // A failed attempt consumes the code, so the real one can no longer be replayed
        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyJson(email, code)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resendThrottledUntilCooldownElapsesAndFreshCodeWorks() throws Exception {
        String email = "flow.resend" + (++counter) + "@example.com";

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        registerAndCaptureCode(email, "");

        // Immediate resend is throttled by the 60s cooldown
        mockMvc.perform(post("/api/auth/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"%s\"}".formatted(email)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Please wait")));

        // Simulate the cooldown elapsing so the resend succeeds
        backdate("otpLastSent", email, Instant.now().minusSeconds(61));

        mockMvc.perform(post("/api/auth/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"%s\"}".formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("sent")));

        verify(smsService, times(2)).sendOtpEmail(eq(email), captor.capture());
        List<String> codes = captor.getAllValues();
        String secondCode = codes.get(codes.size() - 1);

        // The freshly resent code works
        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyJson(email, secondCode)))
                .andExpect(status().isOk());
    }

    @Test
    void newCodeInvalidatesPreviousCode() throws Exception {
        String email = "flow.invalidate" + (++counter) + "@example.com";

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        registerAndCaptureCode(email, "");
        verify(smsService).sendOtpEmail(eq(email), captor.capture());
        String firstCode = captor.getValue();

        // Simulate the cooldown elapsing so a fresh code can be generated
        backdate("otpLastSent", email, Instant.now().minusSeconds(61));

        mockMvc.perform(post("/api/auth/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"%s\"}".formatted(email)))
                .andExpect(status().isOk());

        // Exactly one active OTP per user: the old code is invalidated by the resend
        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyJson(email, firstCode)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void expiredCodeRejected() throws Exception {
        String email = "flow.expired" + (++counter) + "@example.com";
        String code = registerAndCaptureCode(email, "");

        // Simulate the 5-minute window having elapsed
        backdate("otpTimestamps", email, Instant.now().minusSeconds(301));

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyJson(email, code)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Invalid or expired")));
    }

    @Test
    void resendRejectedAfterVerificationAndForUnknownUser() throws Exception {
        String email = "flow.postverify" + (++counter) + "@example.com";
        String code = registerAndCaptureCode(email, "");

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyJson(email, code)))
                .andExpect(status().isOk());

        // Verified users cannot request more codes
        mockMvc.perform(post("/api/auth/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"%s\"}".formatted(email)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("already verified")));

        // Unknown users get a 404
        mockMvc.perform(post("/api/auth/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"nobody@example.com\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void syntheticLocalSignupSkipsVerification() throws Exception {
        String username = "localsignup" + (++counter);
        String email = "localtester" + counter + "@importease.local";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(username, email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.requiresVerification").doesNotExist());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }
}
