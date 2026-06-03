package com.michelecampanello.springshop.domains.addresses.service;

import com.michelecampanello.springshop.core.exceptions.ResourceNotFoundException;
import com.michelecampanello.springshop.domains.addresses.dto.AddressRequest;
import com.michelecampanello.springshop.domains.addresses.dto.AddressResponse;
import com.michelecampanello.springshop.domains.addresses.mapper.AddressMapper;
import com.michelecampanello.springshop.domains.addresses.model.Address;
import com.michelecampanello.springshop.domains.users.model.User;
import com.michelecampanello.springshop.domains.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Transactional(readOnly = true)
    public AddressResponse getByUserId(UUID userId) {
        User user = getUserOrThrow(userId);
        if (user.getAddress() == null) {
            throw new ResourceNotFoundException("Nessun indirizzo per l'utente: " + userId);
        }
        return addressMapper.toResponse(user.getAddress());
    }

    @Transactional
    public AddressResponse upsertByUserId(UUID userId, AddressRequest req) {
        User user = getUserOrThrow(userId);
        Address address = user.getAddress() != null ? user.getAddress() : new Address();
        addressMapper.updateEntity(address, req);
        user.setAddress(address);
        userRepository.save(user);
        return addressMapper.toResponse(user.getAddress());
    }

    @Transactional
    public void deleteByUserId(UUID userId) {
        User user = getUserOrThrow(userId);
        if (user.getAddress() == null) {
            throw new ResourceNotFoundException("Nessun indirizzo per l'utente: " + userId);
        }
        user.setAddress(null); // orphanRemoval elimina la riga in addresses
        userRepository.save(user);
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + userId));
    }
}
