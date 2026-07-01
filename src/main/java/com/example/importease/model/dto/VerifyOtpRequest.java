package com.example.importease.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VerifyOtpRequest {

    @NotBlank(message = "Identifier is required")
    private String identifier; // This can be either the email address or phone number used to request the OTP

    @NotBlank(message = "OTP code is required")
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
    private String otpCode;

    // Constructors
    public VerifyOtpRequest() {}

    public VerifyOtpRequest(String identifier, String otpCode) {
        this.identifier = identifier;
        this.otpCode = otpCode;
    }

    // Getters and Setters
    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}