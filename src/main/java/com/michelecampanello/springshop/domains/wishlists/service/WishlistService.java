package com.michelecampanello.springshop.domains.wishlists.service;

import com.michelecampanello.springshop.core.exceptions.ResourceNotFoundException;
import com.michelecampanello.springshop.domains.products.model.Product;
import com.michelecampanello.springshop.domains.products.repository.ProductRepository;
import com.michelecampanello.springshop.domains.wishlists.dto.WishlistItemResponse;
import com.michelecampanello.springshop.domains.wishlists.model.WishlistItem;
import com.michelecampanello.springshop.domains.wishlists.repository.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<WishlistItemResponse> getWishlist(UUID userId) {
        return wishlistItemRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public WishlistItemResponse addToWishlist(UUID userId, UUID productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Prodotto non trovato: " + productId));

        WishlistItem item = wishlistItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> {
                    WishlistItem newItem = new WishlistItem();
                    newItem.setUserId(userId);
                    newItem.setProductId(productId);
                    return wishlistItemRepository.save(newItem);
                });

        return toResponse(item);
    }

    @Transactional
    public void removeFromWishlist(UUID userId, UUID productId) {
        if (!wishlistItemRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ResourceNotFoundException("Prodotto non presente nella wishlist: " + productId);
        }
        wishlistItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Transactional
    public void clearWishlist(UUID userId) {
        wishlistItemRepository.deleteByUserId(userId);
    }

    private WishlistItemResponse toResponse(WishlistItem item) {
        Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Prodotto non trovato: " + item.getProductId()));
        return new WishlistItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice(),
                product.getImageUrl(),
                item.getCreatedAt()
        );
    }
}
