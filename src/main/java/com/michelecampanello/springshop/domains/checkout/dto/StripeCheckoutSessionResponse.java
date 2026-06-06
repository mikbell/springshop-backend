package com.michelecampanello.springshop.domains.checkout.dto;

import java.util.UUID;

public record StripeCheckoutSessionResponse(
        UUID orderId,
        String sessionId,
        String checkoutUrl
) {
}
