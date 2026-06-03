package com.michelecampanello.springshop.domains.wishlists.controller;

import com.michelecampanello.springshop.domains.users.model.User;
import com.michelecampanello.springshop.domains.wishlists.dto.WishlistItemResponse;
import com.michelecampanello.springshop.domains.wishlists.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<List<WishlistItemResponse>> getWishlist() {
        return ResponseEntity.ok(wishlistService.getWishlist(currentUserId()));
    }

    @PostMapping("/items/{productId}")
    public ResponseEntity<WishlistItemResponse> addToWishlist(@PathVariable UUID productId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(wishlistService.addToWishlist(currentUserId(), productId));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable UUID productId) {
        wishlistService.removeFromWishlist(currentUserId(), productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearWishlist() {
        wishlistService.clearWishlist(currentUserId());
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user) || user.getId() == null) {
            throw new AccessDeniedException("Utente autenticato non valido");
        }
        return user.getId();
    }
}
