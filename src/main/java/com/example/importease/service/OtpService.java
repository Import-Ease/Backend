package com.example.importease.service;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final SmsService smsService;

    private static final long OTP_EXPIRY_SECONDS = 600; // 10 minutes

    // Maps identifier -> OTP code
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    // Maps identifier -> creation timestamp
    private final Map<String, Instant> otpTimestamps = new ConcurrentHashMap<>();

    public OtpService(SmsService smsService) {
        this.smsService = smsService;
    }

    private String generateAndStore(String identifier) {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        String otpString = String.valueOf(otp);

        otpStorage.put(identifier, otpString);
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
     * Verifies the provided OTP code against the cached email or phone number.
     * Returns true only if the code matches and has not expired (10 min window).
     */
    public boolean verifyOtp(String identifier, String userProvidedOtp) {
        String storedOtp = otpStorage.get(identifier);
        Instant createdAt = otpTimestamps.get(identifier);

        // Always clean up so it can't be reused regardless of outcome
        otpStorage.remove(identifier);
        otpTimestamps.remove(identifier);

        if (storedOtp == null || createdAt == null) {
            return false;
        }

        boolean expired = Instant.now().isAfter(createdAt.plusSeconds(OTP_EXPIRY_SECONDS));
        if (expired) {
            return false;
        }

        return storedOtp.equals(userProvidedOtp);
    }
}