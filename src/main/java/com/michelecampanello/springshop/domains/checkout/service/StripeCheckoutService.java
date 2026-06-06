package com.michelecampanello.springshop.domains.checkout.service;

import com.michelecampanello.springshop.domains.checkout.config.StripeProperties;
import com.michelecampanello.springshop.domains.checkout.dto.StripeCheckoutSessionResponse;
import com.michelecampanello.springshop.domains.orders.model.Order;
import com.michelecampanello.springshop.domains.orders.service.OrderService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

@Service
public class StripeCheckoutService {

    private final StripeProperties stripeProperties;
    private final OrderService orderService;

    public StripeCheckoutService(StripeProperties stripeProperties, OrderService orderService) {
        this.stripeProperties = stripeProperties;
        this.orderService = orderService;
    }

    @Transactional(rollbackFor = StripeException.class)
    public StripeCheckoutSessionResponse createCheckoutSession(UUID userId) throws StripeException {
        requireStripeSecretKey();

        Order order = orderService.createPendingOrderFromCart(userId);
        SessionCreateParams.Builder params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripeProperties.successUrl())
                .setCancelUrl(stripeProperties.cancelUrl())
                .setClientReferenceId(order.getId().toString())
                .putMetadata("orderId", order.getId().toString())
                .putMetadata("userId", userId.toString());

        for (var item : order.getItems()) {
            params.addLineItem(SessionCreateParams.LineItem.builder()
                    .setQuantity(item.getQuantity().longValue())
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(stripeProperties.currency())
                            .setUnitAmount(toMinorUnit(item.getPriceAtPurchase()))
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(item.getProductName())
                                    .putMetadata("productId", item.getProductId().toString())
                                    .putMetadata("sku", item.getSku())
                                    .build())
                            .build())
                    .build());
        }

        Session session = Session.create(params.build(), requestOptions());
        order.setStripeCheckoutSessionId(session.getId());

        return new StripeCheckoutSessionResponse(order.getId(), session.getId(), session.getUrl());
    }

    @Transactional
    public void handleWebhook(String payload, String signatureHeader) throws SignatureVerificationException {
        requireWebhookSecret();

        Event event = Webhook.constructEvent(payload, signatureHeader, stripeProperties.webhookSecret());
        if (!"checkout.session.completed".equals(event.getType())
                && !"checkout.session.async_payment_succeeded".equals(event.getType())) {
            return;
        }

        StripeObject stripeObject = event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalArgumentException("Payload Stripe non deserializzabile"));

        if (stripeObject instanceof Session session) {
            orderService.markOrderAsPaidFromStripe(session.getId(), session.getPaymentIntent());
        }
    }

    private RequestOptions requestOptions() {
        return RequestOptions.builder()
                .setApiKey(stripeProperties.secretKey())
                .build();
    }

    private long toMinorUnit(BigDecimal amount) {
        return amount
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    private void requireStripeSecretKey() {
        if (!StringUtils.hasText(stripeProperties.secretKey())) {
            throw new IllegalStateException("Configura STRIPE_SECRET_KEY per creare sessioni di checkout Stripe.");
        }
    }

    private void requireWebhookSecret() {
        if (!StringUtils.hasText(stripeProperties.webhookSecret())) {
            throw new IllegalStateException("Configura STRIPE_WEBHOOK_SECRET per validare i webhook Stripe.");
        }
    }
}
