package com.example.importease.controller;

import com.example.importease.model.AppUser;
import com.example.importease.repository.AppUserRepository;
import com.example.importease.service.JwtService;
import com.example.importease.service.OtpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OtpService otpService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AppUserRepository appUserRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void forgotPasswordShouldSendOtpForKnownEmail() throws Exception {
        AppUser user = new AppUser();
        user.setEmail("user@example.com");
        when(appUserRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(otpService.generateAndSendEmailOtp(anyString())).thenReturn("OTP sent");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isOk());

        verify(otpService).generateAndSendEmailOtp("user@example.com");
    }
}
