package com.michelecampanello.springshop.domains.users.mapper;

import com.michelecampanello.springshop.domains.addresses.mapper.AddressMapper;
import com.michelecampanello.springshop.domains.addresses.model.Address;
import com.michelecampanello.springshop.domains.users.dto.UserRequest;
import com.michelecampanello.springshop.domains.users.dto.UserResponse;
import com.michelecampanello.springshop.domains.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final AddressMapper addressMapper;

    /** Crea l'entità impostando l'email (immutabile). Non imposta la password. */
    public User toEntity(UserRequest req) {
        User user = new User();
        user.setEmail(req.email());
        applyMutableFields(user, req);
        return user;
    }

    /** Aggiorna i campi mutabili. Non tocca email né password. */
    public void updateEntity(User user, UserRequest req) {
        applyMutableFields(user, req);
    }

    private void applyMutableFields(User user, UserRequest req) {
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setPhoneNumber(req.phoneNumber());
        if (req.role() != null) {
            user.setRole(req.role());
        }
        if (req.address() != null) {
            Address address = user.getAddress() != null ? user.getAddress() : new Address();
            addressMapper.updateEntity(address, req.address());
            user.setAddress(address);
        }
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAddress() != null ? addressMapper.toResponse(user.getAddress()) : null,
                user.getRole(),
                user.getActive()
        );
    }
}
