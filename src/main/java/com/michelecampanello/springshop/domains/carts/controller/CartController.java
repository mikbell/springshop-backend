package com.michelecampanello.springshop.domains.carts.controller;

import com.michelecampanello.springshop.domains.carts.dto.AddToCartRequest;
import com.michelecampanello.springshop.domains.carts.dto.CartResponse;
import com.michelecampanello.springshop.domains.carts.dto.UpdateQuantityRequest;
import com.michelecampanello.springshop.domains.carts.service.CartService;
import com.michelecampanello.springshop.domains.users.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Recupera il carrello dell'utente corrente.
     * GET /api/v1/carts
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal User currentUser) {
        CartResponse cart = cartService.getCartByUserId(currentUser.getId());
        return ResponseEntity.ok(cart);
    }

    /**
     * Aggiunge un prodotto al carrello (o ne incrementa la quantità).
     * POST /api/v1/carts/items
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AddToCartRequest request) {

        CartResponse updatedCart = cartService.addItemToCart(currentUser.getId(), request);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Aggiorna la quantità di un prodotto specifico nel carrello (sovrascrittura).
     * PUT /api/v1/carts/items/{productId}
     */
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateQuantityRequest request) {

        CartResponse updatedCart = cartService.updateItemQuantity(currentUser.getId(), productId, request);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Rimuove completamente un prodotto dal carrello.
     * DELETE /api/v1/carts/items/{productId}
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItemFromCart(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID productId) {

        CartResponse updatedCart = cartService.removeItemFromCart(currentUser.getId(), productId);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Svuota completamente il carrello dell'utente.
     * DELETE /api/v1/carts
     */
    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(@AuthenticationPrincipal User currentUser) {
        CartResponse clearedCart = cartService.clearCart(currentUser.getId());
        return ResponseEntity.ok(clearedCart);
    }
}