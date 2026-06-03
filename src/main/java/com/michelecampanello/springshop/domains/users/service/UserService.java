package com.michelecampanello.springshop.domains.users.service;

import com.michelecampanello.springshop.core.exceptions.DuplicateResourceException;
import com.michelecampanello.springshop.core.exceptions.ImmutableFieldException;
import com.michelecampanello.springshop.core.exceptions.ResourceNotFoundException;
import com.michelecampanello.springshop.domains.users.dto.UserRequest;
import com.michelecampanello.springshop.domains.users.dto.UserResponse;
import com.michelecampanello.springshop.domains.users.mapper.UserMapper;
import com.michelecampanello.springshop.domains.users.model.User;
import com.michelecampanello.springshop.domains.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> fetchAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse fetchUser(UUID id) {
        return userMapper.toResponse(getUserOrThrow(id));
    }

    public UserResponse registerUser(UserRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("L'email inserita è già associata a un account");
        }
        User user = userMapper.toEntity(req);
        user.setPassword(passwordEncoder.encode(req.password()));
        return userMapper.toResponse(userRepository.save(user));
    }

    public UserResponse updateUser(UUID id, UserRequest req) {
        User user = getUserOrThrow(id);
        if (!user.getEmail().equalsIgnoreCase(req.email())) {
            throw new ImmutableFieldException("L'indirizzo email non può essere modificato dopo la registrazione.");
        }
        userMapper.updateEntity(user, req);
        if (req.password() != null && !req.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.password()));
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utente non trovato: " + id);
        }
        userRepository.deleteById(id);
    }

    private User getUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + id));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Logica per cercare l'utente sul DB Postgres tramite email
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con email: " + email));
    }
}
