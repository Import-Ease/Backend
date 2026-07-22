package com.example.importease.controller;

import com.example.importease.model.AppUser;
import com.example.importease.repository.AppUserRepository;
import com.example.importease.repository.ProductRepository;
import com.example.importease.service.JwtService;
import com.example.importease.service.SearchLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
@AutoConfigureMockMvc(addFilters = false)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private SearchLogService searchLogService;

    @MockBean
    private AppUserRepository appUserRepository;

    @MockBean
    private JwtService jwtService;

    private final UUID testUserId = UUID.randomUUID();

    @Test
    @WithMockUser
    void deleteHistoryItem_ownLog_returns204() throws Exception {
        AppUser user = new AppUser();
        user.setId(testUserId);
        user.setEmail("test@example.com");

        when(appUserRepository.findByUsernameOrEmail("user")).thenReturn(Optional.of(user));
        doNothing().when(searchLogService).deleteSearchLog(eq(1L), eq(testUserId));

        mockMvc.perform(delete("/api/search/history/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(searchLogService).deleteSearchLog(eq(1L), eq(testUserId));
    }

    @Test
    @WithMockUser
    void deleteHistoryItem_logNotFound_returns409() throws Exception {
        AppUser user = new AppUser();
        user.setId(testUserId);
        user.setEmail("test@example.com");

        when(appUserRepository.findByUsernameOrEmail("user")).thenReturn(Optional.of(user));
        doThrow(new IllegalArgumentException("Search log not found"))
                .when(searchLogService).deleteSearchLog(eq(999L), eq(testUserId));

        mockMvc.perform(delete("/api/search/history/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deleteHistoryItem_notOwnLog_returns403() throws Exception {
        AppUser user = new AppUser();
        user.setId(testUserId);
        user.setEmail("test@example.com");
        UUID otherUserId = UUID.randomUUID();

        when(appUserRepository.findByUsernameOrEmail("user")).thenReturn(Optional.of(user));
        doThrow(new org.springframework.security.access.AccessDeniedException("Not authorized"))
                .when(searchLogService).deleteSearchLog(eq(2L), eq(testUserId));

        mockMvc.perform(delete("/api/search/history/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}