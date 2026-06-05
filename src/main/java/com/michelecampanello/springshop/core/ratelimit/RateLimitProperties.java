package com.michelecampanello.springshop.core.ratelimit;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "security.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    @Min(1)
    private int capacity = 100;

    @Min(1)
    private int refillTokens = 100;

    @NotNull
    private Duration refillPeriod = Duration.ofMinutes(1);

    private boolean failOpen = true;

    private boolean useForwardedHeaders = false;

    private String keyPrefix = "rate-limit";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getRefillTokens() {
        return refillTokens;
    }

    public void setRefillTokens(int refillTokens) {
        this.refillTokens = refillTokens;
    }

    public Duration getRefillPeriod() {
        return refillPeriod;
    }

    public void setRefillPeriod(Duration refillPeriod) {
        this.refillPeriod = refillPeriod;
    }

    public boolean isFailOpen() {
        return failOpen;
    }

    public void setFailOpen(boolean failOpen) {
        this.failOpen = failOpen;
    }

    public boolean isUseForwardedHeaders() {
        return useForwardedHeaders;
    }

    public void setUseForwardedHeaders(boolean useForwardedHeaders) {
        this.useForwardedHeaders = useForwardedHeaders;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    @AssertTrue(message = "refillPeriod must be positive")
    public boolean isRefillPeriodPositive() {
        return refillPeriod != null && !refillPeriod.isZero() && !refillPeriod.isNegative();
    }
}
