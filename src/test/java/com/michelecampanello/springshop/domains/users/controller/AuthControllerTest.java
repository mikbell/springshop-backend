package com.michelecampanello.springshop.domains.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michelecampanello.springshop.core.security.JwtService;
import com.michelecampanello.springshop.support.TestCacheConfig;
import com.michelecampanello.springshop.domains.users.dto.*;
import com.michelecampanello.springshop.domains.users.model.RefreshToken;
import com.michelecampanello.springshop.domains.users.model.User;
import com.michelecampanello.springshop.domains.users.service.RefreshTokenService;
import com.michelecampanello.springshop.domains.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestCacheConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @Test
    void loginReturnsTokenOnValidCredentials() throws Exception {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setEmail("mario@x.it");
        user.setRole(User.Role.CUSTOMER);

        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh-123")
                .user(user)
                .build();

        when(userService.loadUserByUsername("mario@x.it")).thenReturn(user);
        when(jwtService.generateToken(any(), eq(id))).thenReturn("jwt-123");
        when(refreshTokenService.createRefreshToken(id)).thenReturn(refreshToken);

        AuthenticationRequest request = new AuthenticationRequest("mario@x.it", "secret1");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-123"))
                .andExpect(jsonPath("$.userId").value(id.toString()))
                .andExpect(jsonPath("$.email").value("mario@x.it"));
    }

    @Test
    void loginReturns401OnBadCredentials() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenziali errate"));

        AuthenticationRequest request = new AuthenticationRequest("mario@x.it", "wrong-password");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerReturnsCreatedUser() throws Exception {
        UserRequest request = new UserRequest("Mario", "Rossi", "mario@x.it", "secret1", null, null, null);
        UserResponse response = new UserResponse(
                UUID.randomUUID(), "Mario", "Rossi", "mario@x.it", null, null, User.Role.CUSTOMER, true);

        when(userService.registerUser(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("mario@x.it"));
    }

    @Test
    void refreshTokenReturnsNewAccessToken() throws Exception {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setEmail("mario@x.it");

        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh-123")
                .user(user)
                .build();

        when(refreshTokenService.findByToken("refresh-123")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(any())).thenReturn(refreshToken);
        when(jwtService.generateToken(any(), eq(id))).thenReturn("new-jwt-123");

        RefreshTokenRequest request = new RefreshTokenRequest("refresh-123");

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-jwt-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-123"));
    }

    @Test
    void logoutDeletesToken() throws Exception {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);

        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh-123")
                .user(user)
                .build();

        when(refreshTokenService.findByToken("refresh-123")).thenReturn(Optional.of(refreshToken));

        RefreshTokenRequest request = new RefreshTokenRequest("refresh-123");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(refreshTokenService).deleteByUserId(id);
    }
}
