package com.michelecampanello.springshop.domains.carts.service;

import com.michelecampanello.springshop.core.exceptions.ResourceNotFoundException;
import com.michelecampanello.springshop.domains.carts.dto.AddToCartRequest;
import com.michelecampanello.springshop.domains.carts.dto.CartResponse;
import com.michelecampanello.springshop.domains.carts.dto.UpdateQuantityRequest;
import com.michelecampanello.springshop.domains.carts.mapper.CartMapper;
import com.michelecampanello.springshop.domains.carts.model.Cart;
import com.michelecampanello.springshop.domains.carts.model.CartItem;
import com.michelecampanello.springshop.domains.carts.repository.CartRepository;
import com.michelecampanello.springshop.domains.products.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository; // Usato solo in lettura per cross-domain
    private final CartMapper cartMapper;

    public CartService(CartRepository cartRepository, ProductRepository productRepository, CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.cartMapper = cartMapper;
    }

    @Transactional(readOnly = true)
    public CartResponse getCartByUserId(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
        return cartMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse addItemToCart(UUID userId, AddToCartRequest request) {
        
        // 1. Recuperiamo o creiamo il carrello dell'utente
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        // 2. Verifichiamo se il prodotto esiste nel modulo prodotti
        var product = productRepository.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("Prodotto non trovato con ID: " + request.productId()));

        // 3. Controlliamo se il prodotto è già presente nel carrello
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.productId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Se esiste già, incrementiamo la quantità
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.quantity());
        } else {
            // Se è nuovo, creiamo una nuova riga (CartItem)
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductId(product.getId());
            newItem.setSku(product.getSku());
            newItem.setQuantity(request.quantity());
            newItem.setPriceAtAdded(product.getPrice()); // Congeliamo il prezzo corrente

            cart.getItems().add(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toResponse(savedCart);
    }

    private Cart createNewCart(UUID userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cartRepository.save(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(UUID userId, UUID productId, UpdateQuantityRequest request) {
        // 1. Recuperiamo il carrello
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Carrello non trovato per l'utente: " + userId));

        // 2. Cerchiamo l'articolo nel carrello
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Impossibile aggiornare la quantità: il prodotto con ID " + productId + " non è presente nel tuo carrello."
                ));

        // 3. Se la nuova quantità è 0, rimuoviamo l'elemento, altrimenti aggiorniamo
        if (request.quantity() == 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(request.quantity());
        }

        // 4. Salviamo e restituiamo la risposta aggiornata
        Cart updatedCart = cartRepository.save(cart);
        return cartMapper.toResponse(updatedCart);
    }

    @Transactional
    public CartResponse removeItemFromCart(UUID userId, UUID productId) {
        // 1. Recuperiamo il carrello dell'utente
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Carrello non trovato per l'utente: " + userId));

        // 2. Cerchiamo l'elemento all'interno della lista usando l'UUID del prodotto
        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        if (!removed) {
            throw new IllegalArgumentException("Prodotto non presente nel carrello.");
        }

        // 3. Salviamo il carrello aggiornato (Hibernate eliminerà la riga orfana da cart_items)
        Cart updatedCart = cartRepository.save(cart);
        return cartMapper.toResponse(updatedCart);
    }

    @Transactional
    public CartResponse clearCart(UUID userId) {
        // 1. Recuperiamo il carrello dell'utente
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Carrello non trovato per l'utente: " + userId));

        // 2. Svuotiamo la lista degli elementi (Hibernate cancellerà i record orfani dal DB)
        cart.getItems().clear();

        // 3. Salviamo lo stato del carrello vuoto
        Cart clearedCart = cartRepository.save(cart);
        return cartMapper.toResponse(clearedCart);
    }
}