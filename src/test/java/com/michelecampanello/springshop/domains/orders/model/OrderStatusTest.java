package com.michelecampanello.springshop.domains.orders.model;

import org.junit.jupiter.api.Test;
import static com.michelecampanello.springshop.domains.orders.model.OrderStatus.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

    @Test
    void pending_canTransitionTo_paid() {
        assertTrue(PENDING.canTransitionTo(PAID));
    }

    @Test
    void pending_canTransitionTo_cancelled() {
        assertTrue(PENDING.canTransitionTo(CANCELLED));
    }

    @Test
    void pending_cannotTransitionTo_shipped() {
        assertFalse(PENDING.canTransitionTo(SHIPPED));
    }

    @Test
    void paid_canTransitionTo_shipped() {
        assertTrue(PAID.canTransitionTo(SHIPPED));
    }

    @Test
    void paid_canTransitionTo_cancelled() {
        assertTrue(PAID.canTransitionTo(CANCELLED));
    }

    @Test
    void paid_cannotTransitionTo_pending() {
        assertFalse(PAID.canTransitionTo(PENDING));
    }

    @Test
    void shipped_isTerminal() {
        assertFalse(SHIPPED.canTransitionTo(PENDING));
        assertFalse(SHIPPED.canTransitionTo(PAID));
        assertFalse(SHIPPED.canTransitionTo(CANCELLED));
    }

    @Test
    void cancelled_isTerminal() {
        assertFalse(CANCELLED.canTransitionTo(PENDING));
        assertFalse(CANCELLED.canTransitionTo(PAID));
        assertFalse(CANCELLED.canTransitionTo(SHIPPED));
    }
}
