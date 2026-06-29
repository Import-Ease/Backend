package com.example.ImportEase.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final SmsService smsService;

    // In-memory map to store phone numbers or emails linked to their active OTP codes
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public OtpService(SmsService smsService) {
        this.smsService = smsService;
    }

    /**
     * Generates a 6-digit code and dispatches it via SMS
     */
    public String generateAndSendOtp(String phoneNumber) {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Guarantees a 6-digit number
        String otpString = String.valueOf(otp);

        otpStorage.put(phoneNumber, otpString);
        smsService.sendOtpSms(phoneNumber, otpString);

        return "OTP sent successfully to " + phoneNumber;
    }

    /**
     * Generates a 6-digit code and dispatches it via Transactional Email
     */
    public String generateAndSendEmailOtp(String emailAddress) {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Guarantees a 6-digit number
        String otpString = String.valueOf(otp);

        otpStorage.put(emailAddress, otpString);
        smsService.sendOtpEmail(emailAddress, otpString); // Talk to your Brevo email method

        return "OTP email dispatched successfully to " + emailAddress;
    }

    /**
     * Verifies the provided OTP code against the cached email or phone number
     */
    public boolean verifyOtp(String identifier, String userProvidedOtp) {
        String storedOtp = otpStorage.get(identifier);

        if (storedOtp != null && storedOtp.equals(userProvidedOtp)) {
            otpStorage.remove(identifier); // Clear it out so it can't be reused!
            return true;
        }
        return false;
    }
}