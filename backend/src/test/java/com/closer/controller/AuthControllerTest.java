package com.closer.controller;

import com.closer.dto.AuthResponse;
import com.closer.dto.UserResponse;
import com.closer.model.User;
import com.closer.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private AuthResponse buildAuthResponse() {
        UserResponse userResponse = UserResponse.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .name("Test User")
                .provider(User.AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .maxContacts(150)
                .build();

        return AuthResponse.builder()
                .token("jwt-token")
                .refreshToken("refresh-token")
                .user(userResponse)
                .build();
    }

    @Test
    void register_validRequest_returns201() throws Exception {
        when(authService.register(any(), any(), any())).thenReturn(buildAuthResponse());

        Map<String, String> request = Map.of(
                "email", "test@example.com",
                "password", "password123",
                "name", "Test User"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        Map<String, String> request = Map.of(
                "email", "not-an-email",
                "password", "password123",
                "name", "Test User"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        Map<String, String> request = Map.of(
                "email", "test@example.com",
                "password", "123",
                "name", "Test User"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validCredentials_returns200() throws Exception {
        when(authService.login(any(), any())).thenReturn(buildAuthResponse());

        Map<String, String> request = Map.of(
                "email", "test@example.com",
                "password", "password123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        when(authService.login(any(), any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        Map<String, String> request = Map.of(
                "email", "test@example.com",
                "password", "wrongpassword"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_validToken_returns200() throws Exception {
        when(authService.refreshToken(any())).thenReturn(buildAuthResponse());

        Map<String, String> request = Map.of("refreshToken", "valid-refresh-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void refresh_missingToken_returns400() throws Exception {
        Map<String, String> request = Map.of();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
