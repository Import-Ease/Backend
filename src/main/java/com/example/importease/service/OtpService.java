package com.example.importease.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final SmsService smsService;

    private static final long OTP_EXPIRY_SECONDS = 300; // 5 minutes
    private static final long RESEND_COOLDOWN_SECONDS = 60; // prevent abuse

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // Maps identifier -> hashed OTP code (stored securely, never plaintext)
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    // Maps identifier -> creation timestamp
    private final Map<String, Instant> otpTimestamps = new ConcurrentHashMap<>();
    // Maps identifier -> last send time (used to throttle resend requests)
    private final Map<String, Instant> otpLastSent = new ConcurrentHashMap<>();

    public OtpService(SmsService smsService) {
        this.smsService = smsService;
    }

    private String generateAndStore(String identifier) {
        int otp = 100000 + SECURE_RANDOM.nextInt(900000);
        String otpString = String.valueOf(otp);

        otpStorage.put(identifier, hash(otpString));
        otpTimestamps.put(identifier, Instant.now());
        return otpString;
    }

    /**
     * Generates a 6-digit code and dispatches it via SMS
     */
    public String generateAndSendOtp(String phoneNumber) {
        String otpString = generateAndStore(phoneNumber);
        smsService.sendOtpSms(phoneNumber, otpString);
        return "OTP sent successfully to " + phoneNumber;
    }

    /**
     * Generates a 6-digit code and dispatches it via Transactional Email
     */
    public String generateAndSendEmailOtp(String emailAddress) {
        String otpString = generateAndStore(emailAddress);
        smsService.sendOtpEmail(emailAddress, otpString);
        return "OTP email dispatched successfully to " + emailAddress;
    }

    /**
     * Generates a fresh verification OTP and dispatches it via email (SendGrid).
     * Overwrites any previous code, so there is exactly one active OTP per user.
     * Throttles rapid resend attempts to prevent abuse.
     *
     * @throws IllegalArgumentException if a code was sent too recently
     */
    public void sendVerificationOtp(String emailAddress) {
        Instant last = otpLastSent.get(emailAddress);
        if (last != null) {
            long elapsed = Duration.between(last, Instant.now()).getSeconds();
            if (elapsed < RESEND_COOLDOWN_SECONDS) {
                throw new IllegalArgumentException(
                        "Please wait " + (RESEND_COOLDOWN_SECONDS - elapsed) + " seconds before requesting a new code.");
            }
        }
        String otpString = generateAndStore(emailAddress);
        smsService.sendOtpEmail(emailAddress, otpString);
        otpLastSent.put(emailAddress, Instant.now());
    }

    /**
     * Verifies the provided OTP code against the cached email or phone number.
     * Returns true only if the code matches and has not expired (5 min window).
     */
    public boolean verifyOtp(String identifier, String userProvidedOtp) {
        String storedHash = otpStorage.get(identifier);
        Instant createdAt = otpTimestamps.get(identifier);

        // Always clean up so it can't be reused regardless of outcome
        otpStorage.remove(identifier);
        otpTimestamps.remove(identifier);

        if (storedHash == null || createdAt == null) {
            return false;
        }

        boolean expired = Instant.now().isAfter(createdAt.plusSeconds(OTP_EXPIRY_SECONDS));
        if (expired) {
            return false;
        }

        return storedHash.equals(hash(userProvidedOtp));
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
