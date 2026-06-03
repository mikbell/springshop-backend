package com.michelecampanello.springshop.domains.orders.service;

import com.michelecampanello.springshop.core.dto.PageResponse;
import com.michelecampanello.springshop.core.exceptions.InsufficientStockException;
import com.michelecampanello.springshop.core.exceptions.InvalidOrderStatusTransitionException;
import com.michelecampanello.springshop.core.exceptions.ResourceNotFoundException;
import com.michelecampanello.springshop.domains.carts.model.Cart;
import com.michelecampanello.springshop.domains.carts.model.CartItem;
import com.michelecampanello.springshop.domains.carts.repository.CartRepository;
import com.michelecampanello.springshop.domains.orders.dto.OrderItemResponse;
import com.michelecampanello.springshop.domains.orders.dto.OrderResponse;
import com.michelecampanello.springshop.domains.orders.mapper.OrderMapper;
import com.michelecampanello.springshop.domains.orders.model.Order;
import com.michelecampanello.springshop.domains.orders.model.OrderStatus;
import com.michelecampanello.springshop.domains.orders.repository.OrderRepository;
import com.michelecampanello.springshop.domains.products.model.Product;
import com.michelecampanello.springshop.domains.products.repository.ProductRepository;
import com.michelecampanello.springshop.domains.users.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock CartRepository cartRepository;
    @Mock ProductRepository productRepository;
    @Mock OrderMapper orderMapper;

    @InjectMocks OrderService orderService;

    private final UUID ORDER_ID = UUID.randomUUID();
    private final UUID USER_ID  = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID OTHER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID PRODUCT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private Order buildOrder(UUID userId, OrderStatus status) {
        Order o = new Order();
        o.setId(ORDER_ID);
        o.setUserId(userId);
        o.setOrderNumber("ORD-TEST-001");
        o.setStatus(status);
        o.setTotalAmount(new BigDecimal("50.00"));
        return o;
    }

    private User buildUser(UUID id, User.Role role) {
        User u = new User();
        u.setId(id);
        u.setEmail("test@test.it");
        u.setRole(role);
        return u;
    }

    private OrderResponse buildResponse(UUID userId, OrderStatus status) {
        return new OrderResponse(ORDER_ID, "ORD-TEST-001", userId,
                status, new BigDecimal("50.00"), List.of(), LocalDateTime.now());
    }

    private Product buildProduct(int stockQuantity) {
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setName("Test Product");
        product.setSku("TEST-SKU");
        product.setPrice(new BigDecimal("25.00"));
        product.setStockQuantity(stockQuantity);
        return product;
    }

    private Cart buildCart(int quantity) {
        Cart cart = new Cart();
        cart.setUserId(USER_ID);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProductId(PRODUCT_ID);
        item.setSku("TEST-SKU");
        item.setQuantity(quantity);
        item.setPriceAtAdded(new BigDecimal("25.00"));

        cart.getItems().add(item);
        return cart;
    }

    // ── checkout ─────────────────────────────────────────────────────────────

    @Test
    void checkout_availableStock_decrementsStockCreatesOrderAndClearsCart() {
        Cart cart = buildCart(2);
        Product product = buildProduct(5);
        OrderResponse expected = new OrderResponse(
                ORDER_ID,
                "ORD-TEST-001",
                USER_ID,
                OrderStatus.PENDING,
                new BigDecimal("50.00"),
                List.of(new OrderItemResponse(
                        UUID.randomUUID(),
                        PRODUCT_ID,
                        "Test Product",
                        "TEST-SKU",
                        new BigDecimal("25.00"),
                        2,
                        new BigDecimal("50.00")
                )),
                LocalDateTime.now()
        );

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdForUpdate(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(ORDER_ID);
            order.setOrderNumber("ORD-TEST-001");
            return order;
        });
        when(orderMapper.toResponse(any(Order.class))).thenReturn(expected);

        OrderResponse result = orderService.checkout(USER_ID);

        assertThat(result).isEqualTo(expected);
        assertThat(product.getStockQuantity()).isEqualTo(3);
        assertThat(cart.getItems()).isEmpty();
        verify(productRepository).findByIdForUpdate(PRODUCT_ID);
        verify(orderRepository).save(any(Order.class));
        verify(cartRepository).save(cart);
    }

    @Test
    void checkout_insufficientStock_throwsAndDoesNotCreateOrderOrClearCart() {
        Cart cart = buildCart(4);
        Product product = buildProduct(3);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdForUpdate(PRODUCT_ID)).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class, () -> orderService.checkout(USER_ID));

        assertThat(product.getStockQuantity()).isEqualTo(3);
        assertThat(cart.getItems()).hasSize(1);
        verify(orderRepository, never()).save(any());
        verify(cartRepository, never()).save(any());
    }

    // ── getOrderById ──────────────────────────────────────────────────────────

    @Test
    void getOrderById_ownerAccess_returnsResponse() {
        Order order = buildOrder(USER_ID, OrderStatus.PENDING);
        User user = buildUser(USER_ID, User.Role.CUSTOMER);
        OrderResponse expected = buildResponse(USER_ID, OrderStatus.PENDING);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(expected);

        OrderResponse result = orderService.getOrderById(ORDER_ID, user);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getOrderById_adminAccess_returnsResponse() {
        Order order = buildOrder(OTHER_ID, OrderStatus.PENDING);
        User admin = buildUser(USER_ID, User.Role.ADMIN);
        OrderResponse expected = buildResponse(OTHER_ID, OrderStatus.PENDING);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(expected);

        OrderResponse result = orderService.getOrderById(ORDER_ID, admin);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getOrderById_differentUser_throwsAuthorizationDenied() {
        Order order = buildOrder(OTHER_ID, OrderStatus.PENDING);
        User user = buildUser(USER_ID, User.Role.CUSTOMER);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(AuthorizationDeniedException.class,
                () -> orderService.getOrderById(ORDER_ID, user));
    }

    @Test
    void getOrderById_notFound_throwsResourceNotFound() {
        User user = buildUser(USER_ID, User.Role.CUSTOMER);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderById(ORDER_ID, user));
    }

    // ── updateOrderStatus ────────────────────────────────────────────────────

    @Test
    void updateOrderStatus_validTransition_returnsUpdatedResponse() {
        Order order = buildOrder(USER_ID, OrderStatus.PENDING);
        OrderResponse expected = buildResponse(USER_ID, OrderStatus.PAID);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expected);

        OrderResponse result = orderService.updateOrderStatus(ORDER_ID, OrderStatus.PAID);

        assertThat(result.status()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatus_invalidTransition_throwsException() {
        Order order = buildOrder(USER_ID, OrderStatus.SHIPPED);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusTransitionException.class,
                () -> orderService.updateOrderStatus(ORDER_ID, OrderStatus.PENDING));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateOrderStatus_orderNotFound_throwsResourceNotFound() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.updateOrderStatus(ORDER_ID, OrderStatus.PAID));
    }

    // ── getAllOrders ─────────────────────────────────────────────────────────

    @Test
    void getAllOrders_withoutStatusFilter_returnsAllPaginated() {
        Order order = buildOrder(USER_ID, OrderStatus.PENDING);
        OrderResponse response = buildResponse(USER_ID, OrderStatus.PENDING);
        Page<Order> page = new PageImpl<>(List.of(order));
        PageRequest pageable = PageRequest.of(0, 10);

        when(orderRepository.findAll(pageable)).thenReturn(page);
        when(orderMapper.toResponse(order)).thenReturn(response);

        PageResponse<OrderResponse> result = orderService.getAllOrders(null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(orderRepository).findAll(pageable);
        verify(orderRepository, never()).findByStatus(any(), any());
    }

    @Test
    void getAllOrders_withStatusFilter_returnsFilteredPaginated() {
        Order order = buildOrder(USER_ID, OrderStatus.PENDING);
        OrderResponse response = buildResponse(USER_ID, OrderStatus.PENDING);
        Page<Order> page = new PageImpl<>(List.of(order));
        PageRequest pageable = PageRequest.of(0, 10);

        when(orderRepository.findByStatus(OrderStatus.PENDING, pageable)).thenReturn(page);
        when(orderMapper.toResponse(order)).thenReturn(response);

        PageResponse<OrderResponse> result = orderService.getAllOrders(OrderStatus.PENDING, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).status()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository).findByStatus(OrderStatus.PENDING, pageable);
        verify(orderRepository, never()).findAll(pageable);
    }
}
