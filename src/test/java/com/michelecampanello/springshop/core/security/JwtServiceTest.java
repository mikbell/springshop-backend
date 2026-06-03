package com.michelecampanello.springshop.core.security;

import com.michelecampanello.springshop.domains.users.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86_400_000L);
    }

    private User userWith(String email, User.Role role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    @Test
    void generatedTokenCarriesEmailAsSubject() {
        User user = userWith("mario@x.it", User.Role.CUSTOMER);

        String token = jwtService.generateToken(user, user.getId());

        assertThat(jwtService.extractUsername(token)).isEqualTo("mario@x.it");
    }

    @Test
    void generatedTokenCarriesUserId() {
        User user = userWith("mario@x.it", User.Role.CUSTOMER);

        String token = jwtService.generateToken(user, user.getId());

        assertThat(jwtService.extractUserId(token)).isEqualTo(user.getId());
    }

    @Test
    void generatedTokenCarriesRoleClaimDerivedFromRole() {
        User admin = userWith("admin@x.it", User.Role.ADMIN);

        String token = jwtService.generateToken(admin, admin.getId());

        @SuppressWarnings("unchecked")
        List<String> roles = jwtService.extractClaim(token, claims -> claims.get("roles", List.class));
        assertThat(roles).containsExactly("ROLE_ADMIN");
    }

    @Test
    void tokenIsValidForTheSameUser() {
        User user = userWith("mario@x.it", User.Role.CUSTOMER);

        String token = jwtService.generateToken(user, user.getId());

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void tokenIsInvalidForADifferentUser() {
        User user = userWith("mario@x.it", User.Role.CUSTOMER);
        User other = userWith("luigi@x.it", User.Role.CUSTOMER);

        String token = jwtService.generateToken(user, user.getId());

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void expiredTokenThrowsWhenParsed() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1_000L);
        User user = userWith("mario@x.it", User.Role.CUSTOMER);
        String token = jwtService.generateToken(user, user.getId());

        assertThatThrownBy(() -> jwtService.extractUsername(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
