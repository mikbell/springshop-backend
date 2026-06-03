package com.michelecampanello.springshop.domains.orders.mapper;

import com.michelecampanello.springshop.domains.orders.dto.OrderItemResponse;
import com.michelecampanello.springshop.domains.orders.dto.OrderResponse;
import com.michelecampanello.springshop.domains.orders.model.Order;
import com.michelecampanello.springshop.domains.orders.model.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        var itemResponses = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                itemResponses,
                order.getCreatedAt()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getSku(),
                item.getPriceAtPurchase(),
                item.getQuantity(),
                item.getRowTotal()
        );
    }
}