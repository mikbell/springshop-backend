package com.michelecampanello.springshop.domains.orders.service;

import com.michelecampanello.springshop.core.dto.PageResponse;
import com.michelecampanello.springshop.core.exceptions.EmptyCartException;
import com.michelecampanello.springshop.core.exceptions.InsufficientStockException;
import com.michelecampanello.springshop.core.exceptions.InvalidOrderStatusTransitionException;
import com.michelecampanello.springshop.core.exceptions.ResourceNotFoundException;
import com.michelecampanello.springshop.domains.carts.repository.CartRepository;
import com.michelecampanello.springshop.domains.orders.dto.OrderResponse;
import com.michelecampanello.springshop.domains.orders.mapper.OrderMapper;
import com.michelecampanello.springshop.domains.orders.model.Order;
import com.michelecampanello.springshop.domains.orders.model.OrderItem;
import com.michelecampanello.springshop.domains.orders.model.OrderStatus;
import com.michelecampanello.springshop.domains.orders.repository.OrderRepository;
import com.michelecampanello.springshop.domains.products.repository.ProductRepository;
import com.michelecampanello.springshop.domains.users.model.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository,
                        ProductRepository productRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "products", allEntries = true),
            @CacheEvict(cacheNames = "product", allEntries = true)
    })
    @Transactional
    public OrderResponse checkout(UUID userId) {
        // 1. Recuperiamo il carrello dell'utente
        var cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrello non trovato per l'utente: " + userId));

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException("Impossibile procedere al checkout: il carrello dell'utente " + userId + " è vuoto.");
        }

        // 2. Inizializziamo l'ordine
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderNumber(generateOrderNumber());
        order.setTotalAmount(cart.getTotalCartPrice());

        // 3. Convertiamo i CartItem in OrderItem facendo lo snapshot del Prodotto
        for (var cartItem : cart.getItems()) {
            var product = productRepository.findByIdForUpdate(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Prodotto non trovato durante il checkout. ID: " + cartItem.getProductId()));

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        "Stock insufficiente per il prodotto " + product.getSku() +
                                ": richiesti " + cartItem.getQuantity() +
                                ", disponibili " + product.getStockQuantity()
                );
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName()); // Snapshot del nome corrente
            orderItem.setSku(product.getSku());         // Snapshot dello SKU corrente
            orderItem.setPriceAtPurchase(cartItem.getPriceAtAdded()); // Congeliamo il prezzo pattuito nel carrello
            orderItem.setQuantity(cartItem.getQuantity());

            order.getItems().add(orderItem);
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
        }

        // 4. Salviamo l'ordine nel database
        Order savedOrder = orderRepository.save(order);

        // 5. Svuotiamo il carrello dell'utente (operazione atomica grazie a @Transactional)
        cart.getItems().clear();
        cartRepository.save(cart);

        return orderMapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(UUID userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId, User currentUser) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordine non trovato: " + orderId));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !order.getUserId().equals(currentUser.getId())) {
            throw new AuthorizationDeniedException("Non autorizzato ad accedere a questo ordine", () -> false);
        }

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordine non trovato: " + orderId));

        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new InvalidOrderStatusTransitionException(order.getStatus(), newStatus);
        }

        order.setStatus(newStatus);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable) {
        Page<Order> page = (status != null)
                ? orderRepository.findByStatus(status, pageable)
                : orderRepository.findAll(pageable);
        return PageResponse.from(page.map(orderMapper::toResponse));
    }

    private String generateOrderNumber() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String shortRandom = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return "ORD-" + dateStr + "-" + shortRandom;
    }
}
