package com.callkeypoints.backend.config;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTest {

    @Test
    void allowsUpToCapacityThenBlocks() {
        RateLimiter limiter = new RateLimiter(2);
        UUID user = UUID.randomUUID();

        assertThat(limiter.tryConsume(user)).isTrue();
        assertThat(limiter.tryConsume(user)).isTrue();
        assertThat(limiter.tryConsume(user)).isFalse();
    }

    @Test
    void bucketsAreIndependentPerUser() {
        RateLimiter limiter = new RateLimiter(1);
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();

        assertThat(limiter.tryConsume(a)).isTrue();
        assertThat(limiter.tryConsume(a)).isFalse();
        // b has its own fresh window
        assertThat(limiter.tryConsume(b)).isTrue();
    }
}
