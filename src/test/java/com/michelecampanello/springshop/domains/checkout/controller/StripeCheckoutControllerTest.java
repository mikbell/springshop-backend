package com.michelecampanello.springshop.domains.checkout.controller;

import com.michelecampanello.springshop.core.security.JwtService;
import com.michelecampanello.springshop.domains.checkout.dto.StripeCheckoutSessionResponse;
import com.michelecampanello.springshop.domains.checkout.service.StripeCheckoutService;
import com.michelecampanello.springshop.domains.users.model.User;
import com.michelecampanello.springshop.support.TestCacheConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StripeCheckoutController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestCacheConfig.class)
class StripeCheckoutControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean StripeCheckoutService stripeCheckoutService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserDetailsService userDetailsService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createSession_authenticatedUser_returnsCheckoutSession() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("customer@test.it");
        user.setRole(User.Role.CUSTOMER);
        Authentication authentication = authenticateAs(user);

        when(stripeCheckoutService.createCheckoutSession(any()))
                .thenReturn(new StripeCheckoutSessionResponse(orderId, "cs_test_123", "https://checkout.stripe.com/c/pay/cs_test_123"));

        mockMvc.perform(post("/api/v1/checkout/stripe/session").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.sessionId").value("cs_test_123"))
                .andExpect(jsonPath("$.checkoutUrl").value("https://checkout.stripe.com/c/pay/cs_test_123"));
    }

    @Test
    void webhook_validRequest_delegatesToService() throws Exception {
        String payload = "{\"type\":\"checkout.session.completed\"}";
        String signature = "t=123,v1=abc";

        mockMvc.perform(post("/api/v1/checkout/stripe/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", signature)
                        .content(payload))
                .andExpect(status().isOk());

        verify(stripeCheckoutService).handleWebhook(eq(payload), eq(signature));
    }

    private Authentication authenticateAs(User user) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
        ctx.setAuthentication(authentication);
        SecurityContextHolder.setContext(ctx);
        return authentication;
    }
}
