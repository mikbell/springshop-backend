package com.michelecampanello.springshop.domains.users.service;

import com.michelecampanello.springshop.core.exceptions.ResourceNotFoundException;
import com.michelecampanello.springshop.core.exceptions.TokenRefreshException;
import com.michelecampanello.springshop.domains.users.model.RefreshToken;
import com.michelecampanello.springshop.domains.users.repository.RefreshTokenRepository;
import com.michelecampanello.springshop.domains.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${security.jwt.refresh-token.expiration-time:604800000}")
    private long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + userId));

        // 1. Eliminiamo il vecchio token esistente
        refreshTokenRepository.deleteByUser(user);

        // 2. CRIS_FLUSH: Forza l'eliminazione immediata su Postgres liberando il vincolo UNIQUE
        refreshTokenRepository.flush();

        // 3. Ora possiamo inserire il nuovo record in totale sicurezza
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token scaduto. Effettuare nuovamente il login.");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + userId));
        refreshTokenRepository.deleteByUser(user);
    }
}
