package com.michelecampanello.springshop.domains.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michelecampanello.springshop.core.security.JwtService;
import com.michelecampanello.springshop.domains.users.dto.UserRequest;
import com.michelecampanello.springshop.domains.users.dto.UserResponse;
import com.michelecampanello.springshop.domains.users.model.User;
import com.michelecampanello.springshop.domains.users.service.UserService;
import com.michelecampanello.springshop.support.TestCacheConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestCacheConfig.class)
class UserMeControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean UserService userService;
    @MockitoBean JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private User buildAuthUser(UUID id) {
        User u = new User();
        u.setId(id);
        u.setEmail("mario@x.it");
        u.setRole(User.Role.CUSTOMER);
        return u;
    }

    private void authenticateAs(User user) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities())
        );
        SecurityContextHolder.setContext(ctx);
    }

    @Test
    @WithMockUser(username = "mario@x.it", roles = "CUSTOMER")
    void getMeRestituisceProfiloProprio() throws Exception {
        UUID id = UUID.randomUUID();
        User authUser = buildAuthUser(id);
        UserResponse response = new UserResponse(id, "Mario", "Rossi", "mario@x.it",
                null, null, User.Role.CUSTOMER, true);
        when(userService.fetchUser(any())).thenReturn(response);

        authenticateAs(authUser);

        mockMvc.perform(get("/api/v1/users/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("mario@x.it"));

        org.mockito.ArgumentCaptor<UUID> captor = org.mockito.ArgumentCaptor.forClass(UUID.class);
        org.mockito.Mockito.verify(userService).fetchUser(captor.capture());
    }

    @Test
    @WithMockUser(username = "mario@x.it", roles = "CUSTOMER")
    void putMeAggiornaProfiloProprio() throws Exception {
        UUID id = UUID.randomUUID();
        User authUser = buildAuthUser(id);
        UserRequest req = new UserRequest("Mario", "Rossi", "mario@x.it", "secret1", null, null, null);
        UserResponse response = new UserResponse(id, "Mario", "Rossi", "mario@x.it",
                null, null, User.Role.CUSTOMER, true);
        when(userService.updateUser(any(), any())).thenReturn(response);

        authenticateAs(authUser);
        mockMvc.perform(put("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("mario@x.it"));
    }
}
