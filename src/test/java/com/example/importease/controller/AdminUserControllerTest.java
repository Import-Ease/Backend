package com.example.importease.controller;

import com.example.importease.model.AppUser;
import com.example.importease.repository.AppUserRepository;
import com.example.importease.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppUserRepository appUserRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtService jwtService;

    private final UUID testUserId = UUID.randomUUID();

    @Test
    @WithMockUser(roles = "ADMIN")
    void toggleStatus_enablesDisabledUser() throws Exception {
        AppUser user = new AppUser();
        user.setId(testUserId);
        user.setEnabled(false);

        when(appUserRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(appUserRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(patch("/api/admin/users/" + testUserId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":true}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toggleStatus_userNotFound_returns404() throws Exception {
        when(appUserRepository.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/admin/users/" + UUID.randomUUID() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRole_returns204() throws Exception {
        AppUser user = new AppUser();
        user.setId(testUserId);
        user.setRole("USER");

        when(appUserRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(appUserRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(put("/api/admin/users/" + testUserId + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRole_userNotFound_returns404() throws Exception {
        when(appUserRepository.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/admin/users/" + UUID.randomUUID() + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetPassword_returns204() throws Exception {
        AppUser user = new AppUser();
        user.setId(testUserId);

        when(appUserRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-pass");
        when(appUserRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(post("/api/admin/users/" + testUserId + "/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"newPass123!\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_returns204() throws Exception {
        when(appUserRepository.existsById(testUserId)).thenReturn(true);
        doNothing().when(appUserRepository).deleteById(testUserId);

        mockMvc.perform(delete("/api/admin/users/" + testUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_notFound_returns404() throws Exception {
        when(appUserRepository.existsById(any())).thenReturn(false);

        mockMvc.perform(delete("/api/admin/users/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}