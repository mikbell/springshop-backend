package com.michelecampanello.springshop.domains.carts.mapper;

import com.michelecampanello.springshop.domains.carts.dto.CartItemResponse;
import com.michelecampanello.springshop.domains.carts.dto.CartResponse;
import com.michelecampanello.springshop.domains.carts.model.Cart;
import com.michelecampanello.springshop.domains.carts.model.CartItem;
import org.springframework.stereotype.Component;

@Component
public class CartMapper {

    public CartResponse toResponse(Cart cart) {
        var itemResponses = cart.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                itemResponses,
                cart.getTotalCartPrice()
        );
    }

    private CartItemResponse toItemResponse(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                item.getSku(),
                item.getQuantity(),
                item.getPriceAtAdded(),
                item.getTotalPrice()
        );
    }
}