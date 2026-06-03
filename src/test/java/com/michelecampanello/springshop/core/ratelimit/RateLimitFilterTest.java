package com.michelecampanello.springshop.core.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.ServletResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RateLimitFilterTest {

    private static final Instant NOW = Instant.parse("2026-06-03T12:00:00Z");

    private final RateLimitProperties properties = new RateLimitProperties();
    private final RateLimitFilter filter = new RateLimitFilter(
            properties,
            new ObjectMapper().registerModule(new JavaTimeModule()),
            Clock.fixed(NOW, ZoneOffset.UTC)
    );

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void allowsRequestsWithinLimit() throws Exception {
        properties.setCapacity(2);
        properties.setRefillTokens(2);
        properties.setRefillPeriod(Duration.ofMinutes(1));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/products");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("X-RateLimit-Limit")).isEqualTo("2");
        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("1");
    }

    @Test
    void rejectsRequestsOverLimitWith429() throws Exception {
        properties.setCapacity(2);
        properties.setRefillTokens(2);
        properties.setRefillPeriod(Duration.ofMinutes(1));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/products");
        request.setRemoteAddr("127.0.0.1");
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, new MockHttpServletResponse(), chain);
        filter.doFilterInternal(request, new MockHttpServletResponse(), chain);

        MockHttpServletResponse limitedResponse = new MockHttpServletResponse();
        filter.doFilterInternal(request, limitedResponse, chain);

        verify(chain, times(2)).doFilter(eq(request), any(ServletResponse.class));
        assertThat(limitedResponse.getStatus()).isEqualTo(429);
        assertThat(limitedResponse.getHeader("Retry-After")).isEqualTo("60");
        assertThat(limitedResponse.getHeader("X-RateLimit-Remaining")).isEqualTo("0");
        assertThat(limitedResponse.getContentAsString()).contains("Troppe richieste");
    }

    @Test
    void usesAuthenticatedUserAsBucketKey() throws Exception {
        properties.setCapacity(1);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("customer@example.com", null);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        MockHttpServletRequest firstUserRequest = new MockHttpServletRequest("GET", "/api/v1/me");
        firstUserRequest.setRemoteAddr("127.0.0.1");
        MockHttpServletRequest anonymousRequest = new MockHttpServletRequest("GET", "/api/v1/products");
        anonymousRequest.setRemoteAddr("127.0.0.1");
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(firstUserRequest, new MockHttpServletResponse(), chain);
        SecurityContextHolder.clearContext();

        MockHttpServletResponse anonymousResponse = new MockHttpServletResponse();
        filter.doFilterInternal(anonymousRequest, anonymousResponse, chain);

        assertThat(anonymousResponse.getStatus()).isEqualTo(200);
    }
}
