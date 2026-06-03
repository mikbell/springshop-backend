package com.michelecampanello.springshop.domains.users.controller;

import com.michelecampanello.springshop.core.exceptions.TokenRefreshException;
import com.michelecampanello.springshop.core.security.JwtService;
import com.michelecampanello.springshop.domains.users.dto.AuthenticationRequest;
import com.michelecampanello.springshop.domains.users.dto.AuthenticationResponse;
import com.michelecampanello.springshop.domains.users.dto.RefreshTokenRequest;
import com.michelecampanello.springshop.domains.users.dto.UserRequest;
import com.michelecampanello.springshop.domains.users.dto.UserResponse;
import com.michelecampanello.springshop.domains.users.model.User;
import com.michelecampanello.springshop.domains.users.service.RefreshTokenService;
import com.michelecampanello.springshop.domains.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = (User) userService.loadUserByUsername(request.email());
        String jwtToken = jwtService.generateToken(user, user.getId());
        var refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return ResponseEntity.ok(new AuthenticationResponse(
                jwtToken,
                refreshToken.getToken(),
                user.getId(),
                user.getEmail()
        ));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return refreshTokenService.findByToken(request.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(com.michelecampanello.springshop.domains.users.model.RefreshToken::getUser)
                .map(user -> {
                    String token = jwtService.generateToken(user, user.getId());
                    return ResponseEntity.ok(new AuthenticationResponse(
                            token,
                            request.refreshToken(),
                            user.getId(),
                            user.getEmail()
                    ));
                })
                .orElseThrow(() -> new TokenRefreshException(request.refreshToken(), "Refresh token non trovato nel database"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.findByToken(request.refreshToken())
                .ifPresent(token -> refreshTokenService.deleteByUserId(token.getUser().getId()));
        return ResponseEntity.noContent().build();
    }
}