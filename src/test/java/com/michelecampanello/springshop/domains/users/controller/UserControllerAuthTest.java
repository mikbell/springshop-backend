package com.michelecampanello.springshop.domains.users.controller;

import com.michelecampanello.springshop.core.security.JwtService;
import com.michelecampanello.springshop.domains.users.service.UserService;
import com.michelecampanello.springshop.support.TestCacheConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({UserControllerAuthTest.MethodSecurityConfig.class, TestCacheConfig.class})
class UserControllerAuthTest {

    // @WebMvcTest with addFilters=false bypasses SecurityConfig, so method security is NOT
    // automatically active. This inner config re-enables it so @PreAuthorize is enforced.
    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {}

    @Autowired MockMvc mockMvc;
    @MockitoBean UserService userService;
    @MockitoBean JwtService jwtService;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerNonPuoListareUtenti() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPuoListareUtenti() throws Exception {
        when(userService.fetchAllUsers()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerNonPuoAccedereProfiloAltroUtente() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + UUID.randomUUID()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerNonPuoEliminareAltroUtente() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + UUID.randomUUID()))
            .andExpect(status().isForbidden());
    }

    @Test
    void postUsersNonEsisteRestituisce404() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                .contentType("application/json")
                .content("{}"))
            .andExpect(status().is4xxClientError());
    }
}
