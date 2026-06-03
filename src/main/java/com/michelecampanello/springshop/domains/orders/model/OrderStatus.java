package com.michelecampanello.springshop.domains.orders.model;

public enum OrderStatus {
    PENDING,     // Ricevuto, in attesa di pagamento
    PAID,        // Pagato, pronto per la preparazione
    SHIPPED,     // Spedito
    CANCELLED;   // Annullato

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PENDING  -> next == PAID || next == CANCELLED;
            case PAID     -> next == SHIPPED || next == CANCELLED;
            case SHIPPED, CANCELLED -> false;
        };
    }
}