package com.michelecampanello.springshop.core.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Estrae l'header "Authorization" dalla richiesta HTTP
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Controlla se l'header è presente e se inizia con il prefisso standard "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Passa la richiesta al filtro successivo (senza autenticare)
            return;
        }

        // 3. Estrae il token vero e proprio eliminando la stringa "Bearer " (7 caratteri)
        jwt = authHeader.substring(7);

        // 4. Estrae il subject (l'email dell'utente) dal token tramite il JwtService.
        //    Se il token è malformato, scaduto o con firma non valida, jjwt lancia una JwtException:
        //    in quel caso proseguiamo SENZA autenticare e l'endpoint protetto risponderà 401
        //    tramite l'AuthenticationEntryPoint (evitando un 500 Internal Server Error).
        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (JwtException e) {
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Se l'email è presente e l'utente NON è ancora autenticato nel contesto corrente di Spring Security
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Recupera i dettagli dell'utente dal database (o dalla memoria) tramite l'email
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 6. Verifica se il token crittografico è valido e coerente con i dati dell'utente
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Crea l'oggetto di autenticazione specifico per Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // Arricchisce l'oggetto con i dettagli della richiesta HTTP corrente (es. IP, sessione)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Aggiorna il contesto di sicurezza globale: da questo momento l'utente risulterà ufficialmente LOGGATO
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 7. Passa il controllo al filtro successivo della catena
        filterChain.doFilter(request, response);
    }
}