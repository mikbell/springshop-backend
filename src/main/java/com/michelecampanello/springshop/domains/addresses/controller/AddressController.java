package com.michelecampanello.springshop.domains.addresses.controller;

import com.michelecampanello.springshop.domains.addresses.dto.AddressRequest;
import com.michelecampanello.springshop.domains.addresses.dto.AddressResponse;
import com.michelecampanello.springshop.domains.addresses.service.AddressService;
import com.michelecampanello.springshop.domains.users.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/me/address")
    public ResponseEntity<AddressResponse> getMyAddress() {
        return ResponseEntity.ok(addressService.getByUserId(currentUserId()));
    }

    @PutMapping("/me/address")
    public ResponseEntity<AddressResponse> upsertMyAddress(@Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.upsertByUserId(currentUserId(), request));
    }

    @DeleteMapping("/me/address")
    public ResponseEntity<Void> deleteMyAddress() {
        addressService.deleteByUserId(currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/address")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddressResponse> getAddress(@PathVariable UUID userId) {
        return ResponseEntity.ok(addressService.getByUserId(userId));
    }

    @PutMapping("/{userId}/address")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddressResponse> upsertAddress(@PathVariable UUID userId,
                                                         @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.upsertByUserId(userId, request));
    }

    @DeleteMapping("/{userId}/address")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID userId) {
        addressService.deleteByUserId(userId);
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
