package com.michelecampanello.springshop.core.exceptions;

import com.michelecampanello.springshop.domains.orders.model.OrderStatus;

public class InvalidOrderStatusTransitionException extends RuntimeException {
    public InvalidOrderStatusTransitionException(OrderStatus from, OrderStatus to) {
        super("Transizione di stato non valida: " + from + " → " + to);
    }
}
