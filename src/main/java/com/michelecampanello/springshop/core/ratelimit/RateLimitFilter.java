package com.michelecampanello.springshop.core.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.michelecampanello.springshop.core.exceptions.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final String RATE_LIMIT_LIMIT_HEADER = "X-RateLimit-Limit";
    private static final String RATE_LIMIT_REMAINING_HEADER = "X-RateLimit-Remaining";

    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final ConcurrentMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties properties) {
        this(properties, JsonMapper.builder().findAndAddModules().build(), Clock.systemUTC());
    }

    RateLimitFilter(RateLimitProperties properties, ObjectMapper objectMapper, Clock clock) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!properties.isEnabled() || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        TokenBucket bucket = buckets.computeIfAbsent(resolveClientKey(request),
                key -> new TokenBucket(properties.getCapacity(), Instant.now(clock)));

        TokenBucket.Consumption consumption = bucket.tryConsume(properties, Instant.now(clock));
        addRateLimitHeaders(response, consumption.remainingTokens());

        if (consumption.allowed()) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(consumption.retryAfter().toSeconds()));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiError body = new ApiError(
                LocalDateTime.now(clock),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                "Troppe richieste. Riprova piu tardi.",
                null
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private String resolveClientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            return "user:" + authentication.getName();
        }

        return "ip:" + request.getRemoteAddr();
    }

    private void addRateLimitHeaders(HttpServletResponse response, int remainingTokens) {
        response.setHeader(RATE_LIMIT_LIMIT_HEADER, String.valueOf(properties.getCapacity()));
        response.setHeader(RATE_LIMIT_REMAINING_HEADER, String.valueOf(remainingTokens));
    }

    private static class TokenBucket {
        private int tokens;
        private Instant lastRefill;

        private TokenBucket(int capacity, Instant lastRefill) {
            this.tokens = capacity;
            this.lastRefill = lastRefill;
        }

        private synchronized Consumption tryConsume(RateLimitProperties properties, Instant now) {
            refill(properties, now);

            if (tokens > 0) {
                tokens--;
                return new Consumption(true, tokens, Duration.ZERO);
            }

            return new Consumption(false, tokens, retryAfter(properties, now));
        }

        private void refill(RateLimitProperties properties, Instant now) {
            Duration elapsed = Duration.between(lastRefill, now);
            Duration refillPeriod = properties.getRefillPeriod();
            if (elapsed.compareTo(refillPeriod) < 0) {
                return;
            }

            long elapsedPeriods = elapsed.toNanos() / refillPeriod.toNanos();
            long refilledTokens = elapsedPeriods * properties.getRefillTokens();
            tokens = (int) Math.min(properties.getCapacity(), tokens + refilledTokens);
            lastRefill = lastRefill.plus(refillPeriod.multipliedBy(elapsedPeriods));
        }

        private Duration retryAfter(RateLimitProperties properties, Instant now) {
            Duration wait = properties.getRefillPeriod().minus(Duration.between(lastRefill, now));
            return wait.isNegative() || wait.isZero() ? Duration.ZERO : wait;
        }

        private record Consumption(boolean allowed, int remainingTokens, Duration retryAfter) {
        }
    }
}
