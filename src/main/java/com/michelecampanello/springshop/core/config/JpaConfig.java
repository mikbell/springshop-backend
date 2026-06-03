package com.michelecampanello.springshop.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Abilita l'auditing JPA in una configurazione dedicata invece che sulla classe
 * principale dell'applicazione, così gli slice test (es. {@code @WebMvcTest}) non
 * tentano di inizializzare il metamodel JPA quando non serve.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
