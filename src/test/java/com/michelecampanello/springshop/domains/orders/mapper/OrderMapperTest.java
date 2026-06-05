package com.michelecampanello.springshop.domains.orders.mapper;

import com.michelecampanello.springshop.domains.orders.model.Order;
import com.michelecampanello.springshop.domains.orders.model.OrderItem;
import com.michelecampanello.springshop.domains.orders.model.OrderStatus;
import com.michelecampanello.springshop.domains.products.model.Product;
import com.michelecampanello.springshop.domains.products.repository.ProductRepository;
import com.michelecampanello.springshop.domains.users.model.User;
import com.michelecampanello.springshop.domains.users.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderMapperTest {

    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final OrderMapper orderMapper = new OrderMapper(productRepository, userRepository);

    @Test
    void toResponse_includesUserAndProductDetails() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setFirstName("Mario");
        user.setLastName("Rossi");
        user.setEmail("mario@example.com");
        user.setPhoneNumber("+39000000000");
        user.setRole(User.Role.CUSTOMER);
        user.setActive(true);

        Product product = new Product();
        product.setId(productId);
        product.setName("Borsa in pelle");
        product.setSku("SKU-001");
        product.setSlug("borsa-in-pelle");
        product.setImageUrl("/uploads/products/borsa.png");
        product.setPrice(new BigDecimal("79.90"));
        product.setStockQuantity(7);
        product.setStatus(Product.ProductStatus.AVAILABLE);

        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setOrderNumber("ORD-TEST-001");
        order.setUserId(userId);
        order.setStatus(OrderStatus.PAID);
        order.setTotalAmount(new BigDecimal("79.90"));
        order.setCreatedAt(LocalDateTime.of(2026, 6, 5, 12, 0));

        OrderItem item = new OrderItem();
        item.setId(UUID.randomUUID());
        item.setOrder(order);
        item.setProductId(productId);
        item.setProductName("Borsa in pelle");
        item.setSku("SKU-001");
        item.setPriceAtPurchase(new BigDecimal("79.90"));
        item.setQuantity(1);
        order.getItems().add(item);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        var response = orderMapper.toResponse(order);

        assertThat(response.user()).isNotNull();
        assertThat(response.user().email()).isEqualTo("mario@example.com");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().product()).isNotNull();
        assertThat(response.items().getFirst().product().slug()).isEqualTo("borsa-in-pelle");
        assertThat(response.items().getFirst().product().currentStockQuantity()).isEqualTo(7);
    }
}
