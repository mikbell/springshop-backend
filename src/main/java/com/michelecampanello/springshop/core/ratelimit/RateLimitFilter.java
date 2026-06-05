package com.michelecampanello.springshop.core.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.michelecampanello.springshop.core.exceptions.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
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
import java.util.List;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final String RATE_LIMIT_LIMIT_HEADER = "X-RateLimit-Limit";
    private static final String RATE_LIMIT_REMAINING_HEADER = "X-RateLimit-Remaining";
    private static final RedisScript<List> RATE_LIMIT_SCRIPT = buildRateLimitScript();

    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final StringRedisTemplate redisTemplate;

    public RateLimitFilter(RateLimitProperties properties, StringRedisTemplate redisTemplate) {
        this(properties, redisTemplate, JsonMapper.builder().findAndAddModules().build(), Clock.systemUTC());
    }

    RateLimitFilter(RateLimitProperties properties, StringRedisTemplate redisTemplate, ObjectMapper objectMapper, Clock clock) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
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

        Consumption consumption;
        try {
            consumption = consume(resolveClientKey(request), Instant.now(clock));
        } catch (RuntimeException ex) {
            handleRateLimitStoreFailure(response, filterChain, request, ex);
            return;
        }

        addRateLimitHeaders(response, consumption.remainingTokens());

        if (consumption.allowed()) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds(consumption.retryAfter())));
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

    private Consumption consume(String clientKey, Instant now) {
        List result = redisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                List.of(clientKey),
                String.valueOf(properties.getCapacity()),
                String.valueOf(properties.getRefillTokens()),
                String.valueOf(properties.getRefillPeriod().toMillis()),
                String.valueOf(now.toEpochMilli())
        );

        if (result == null || result.size() != 3) {
            throw new IllegalStateException("Redis rate limit script returned an invalid result");
        }

        boolean allowed = asLong(result.get(0)) == 1L;
        int remainingTokens = Math.toIntExact(asLong(result.get(1)));
        Duration retryAfter = Duration.ofMillis(asLong(result.get(2)));
        return new Consumption(allowed, remainingTokens, retryAfter);
    }

    private String resolveClientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            return properties.getKeyPrefix() + ":user:" + authentication.getName();
        }

        return properties.getKeyPrefix() + ":ip:" + resolveClientIp(request);
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (properties.isUseForwardedHeaders()) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                return forwardedFor.split(",", 2)[0].trim();
            }

            String forwarded = request.getHeader("Forwarded");
            if (forwarded != null && !forwarded.isBlank()) {
                for (String part : forwarded.split(";")) {
                    String trimmed = part.trim();
                    if (trimmed.regionMatches(true, 0, "for=", 0, 4)) {
                        return trimmed.substring(4).replace("\"", "");
                    }
                }
            }
        }

        return request.getRemoteAddr();
    }

    private void addRateLimitHeaders(HttpServletResponse response, int remainingTokens) {
        response.setHeader(RATE_LIMIT_LIMIT_HEADER, String.valueOf(properties.getCapacity()));
        response.setHeader(RATE_LIMIT_REMAINING_HEADER, String.valueOf(remainingTokens));
    }

    private void handleRateLimitStoreFailure(
            HttpServletResponse response,
            FilterChain filterChain,
            HttpServletRequest request,
            RuntimeException ex
    ) throws ServletException, IOException {
        if (properties.isFailOpen()) {
            log.warn("Rate limit store unavailable; allowing request: {}", ex.getMessage());
            addRateLimitHeaders(response, properties.getCapacity());
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiError body = new ApiError(
                LocalDateTime.now(clock),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                "Rate limit non disponibile. Riprova piu tardi.",
                null
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private static long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private static long retryAfterSeconds(Duration retryAfter) {
        long millis = retryAfter.toMillis();
        return millis <= 0 ? 0 : (millis + 999) / 1000;
    }

    private static RedisScript<List> buildRateLimitScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setResultType(List.class);
        script.setScriptText("""
                local key = KEYS[1]
                local capacity = tonumber(ARGV[1])
                local refill_tokens = tonumber(ARGV[2])
                local refill_period_ms = tonumber(ARGV[3])
                local now_ms = tonumber(ARGV[4])

                local tokens = tonumber(redis.call('HGET', key, 'tokens'))
                local last_refill = tonumber(redis.call('HGET', key, 'last_refill'))

                if tokens == nil or last_refill == nil then
                    tokens = capacity
                    last_refill = now_ms
                end

                local elapsed_ms = math.max(0, now_ms - last_refill)
                if elapsed_ms >= refill_period_ms then
                    local elapsed_periods = math.floor(elapsed_ms / refill_period_ms)
                    tokens = math.min(capacity, tokens + (elapsed_periods * refill_tokens))
                    last_refill = last_refill + (elapsed_periods * refill_period_ms)
                end

                local allowed = 0
                if tokens > 0 then
                    allowed = 1
                    tokens = tokens - 1
                end

                local retry_after_ms = 0
                if allowed == 0 then
                    retry_after_ms = math.max(0, refill_period_ms - (now_ms - last_refill))
                end

                redis.call('HSET', key, 'tokens', tokens, 'last_refill', last_refill)
                local ttl_ms = refill_period_ms * (math.ceil(capacity / refill_tokens) + 1)
                redis.call('PEXPIRE', key, ttl_ms)

                return { allowed, tokens, retry_after_ms }
                """);
        return script;
    }

    private record Consumption(boolean allowed, int remainingTokens, Duration retryAfter) {
    }
}
