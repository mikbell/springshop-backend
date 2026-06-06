package com.michelecampanello.springshop.domains.checkout.controller;

import com.michelecampanello.springshop.domains.checkout.dto.StripeCheckoutSessionResponse;
import com.michelecampanello.springshop.domains.checkout.service.StripeCheckoutService;
import com.michelecampanello.springshop.domains.users.model.User;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout/stripe")
public class StripeCheckoutController {

    private final StripeCheckoutService stripeCheckoutService;

    public StripeCheckoutController(StripeCheckoutService stripeCheckoutService) {
        this.stripeCheckoutService = stripeCheckoutService;
    }

    @PostMapping("/session")
    public ResponseEntity<StripeCheckoutSessionResponse> createSession(@AuthenticationPrincipal User user) throws StripeException {
        return ResponseEntity.ok(stripeCheckoutService.createCheckoutSession(user.getId()));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signatureHeader) throws SignatureVerificationException {
        stripeCheckoutService.handleWebhook(payload, signatureHeader);
        return ResponseEntity.ok().build();
    }
}
