package com.michelecampanello.springshop.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Espone il {@link PasswordEncoder} in una configurazione dedicata, separata da
 * {@code SecurityConfig}. Questo evita un ciclo di dipendenze a runtime:
 * SecurityConfig -> JwtAuthenticationFilter -> UserService -> PasswordEncoder,
 * che altrimenti impedirebbe l'avvio del contesto applicativo.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt (standard enterprise) per hashare le password in modo sicuro
        return new BCryptPasswordEncoder();
    }
}
