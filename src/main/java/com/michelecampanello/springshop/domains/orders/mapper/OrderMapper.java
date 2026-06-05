package com.michelecampanello.springshop.domains.orders.mapper;

import com.michelecampanello.springshop.domains.orders.dto.OrderItemResponse;
import com.michelecampanello.springshop.domains.orders.dto.OrderProductResponse;
import com.michelecampanello.springshop.domains.orders.dto.OrderResponse;
import com.michelecampanello.springshop.domains.orders.dto.OrderUserResponse;
import com.michelecampanello.springshop.domains.orders.model.Order;
import com.michelecampanello.springshop.domains.orders.model.OrderItem;
import com.michelecampanello.springshop.domains.products.model.Product;
import com.michelecampanello.springshop.domains.products.repository.ProductRepository;
import com.michelecampanello.springshop.domains.users.model.User;
import com.michelecampanello.springshop.domains.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderResponse toResponse(Order order) {
        var itemResponses = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                userRepository.findById(order.getUserId())
                        .map(this::toUserResponse)
                        .orElse(null),
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
                productRepository.findById(item.getProductId())
                        .map(this::toProductResponse)
                        .orElse(null),
                item.getPriceAtPurchase(),
                item.getQuantity(),
                item.getRowTotal()
        );
    }

    private OrderUserResponse toUserResponse(User user) {
        return new OrderUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getActive()
        );
    }

    private OrderProductResponse toProductResponse(Product product) {
        return new OrderProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getSlug(),
                product.getImageUrl(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus()
        );
    }
}
