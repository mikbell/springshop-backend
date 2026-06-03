package com.michelecampanello.springshop.domains.addresses.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michelecampanello.springshop.core.security.JwtService;
import com.michelecampanello.springshop.domains.addresses.dto.AddressRequest;
import com.michelecampanello.springshop.domains.addresses.dto.AddressResponse;
import com.michelecampanello.springshop.domains.addresses.service.AddressService;
import com.michelecampanello.springshop.support.TestCacheConfig;
import com.michelecampanello.springshop.support.WithMockAuthUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({AddressControllerAuthTest.MethodSecurityConfig.class, TestCacheConfig.class})
class AddressControllerAuthTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {}

    @Autowired MockMvc mockMvc;
    @MockitoBean AddressService addressService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @WithMockAuthUser(id = "11111111-1111-1111-1111-111111111111")
    void getMyAddress_usesAuthenticatedUserId() throws Exception {
        AddressResponse response = new AddressResponse(
                UUID.randomUUID(), "Via Roma 1", "Roma", "RM", "Italia", "00100"
        );

        when(addressService.getByUserId(USER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me/address"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Roma"));

        verify(addressService).getByUserId(USER_ID);
    }

    @Test
    @WithMockAuthUser(id = "11111111-1111-1111-1111-111111111111")
    void upsertMyAddress_usesAuthenticatedUserId() throws Exception {
        AddressRequest request = new AddressRequest("Via Roma 1", "Roma", "RM", "Italia", "00100");
        AddressResponse response = new AddressResponse(
                UUID.randomUUID(), request.street(), request.city(), request.state(), request.country(), request.zipcode()
        );

        when(addressService.upsertByUserId(USER_ID, request)).thenReturn(response);

        mockMvc.perform(put("/api/v1/users/me/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zipcode").value("00100"));

        verify(addressService).upsertByUserId(USER_ID, request);
    }

    @Test
    @WithMockAuthUser(id = "11111111-1111-1111-1111-111111111111")
    void deleteMyAddress_usesAuthenticatedUserId() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me/address"))
                .andExpect(status().isNoContent());

        verify(addressService).deleteByUserId(USER_ID);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotAccessAddressByUserId() throws Exception {
        mockMvc.perform(get("/api/v1/users/{userId}/address", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessAddressByUserId() throws Exception {
        UUID targetUserId = UUID.randomUUID();
        AddressResponse response = new AddressResponse(
                UUID.randomUUID(), "Via Milano 2", "Milano", "MI", "Italia", "20100"
        );

        when(addressService.getByUserId(targetUserId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/{userId}/address", targetUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Milano"));

        verify(addressService).getByUserId(targetUserId);
    }
}
