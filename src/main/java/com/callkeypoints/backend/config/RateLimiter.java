package com.callkeypoints.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * In-memory, per-user fixed-window rate limiter. Self-contained (no external dependency).
 * Suited to a single instance; for a multi-replica deployment back it with a shared store.
 */
@Component
public class RateLimiter {

    private static final long WINDOW_NANOS = TimeUnit.MINUTES.toNanos(1);

    private final int capacity;
    private final ConcurrentHashMap<Object, Window> windows = new ConcurrentHashMap<>();

    public RateLimiter(@Value("${app.rate-limit.calls-per-minute}") int capacity) {
        this.capacity = capacity;
    }

    /** @return true if the request is allowed, false if the per-minute quota is exceeded. */
    public boolean tryConsume(Object key) {
        long now = System.nanoTime();
        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || now - existing.start >= WINDOW_NANOS) {
                return new Window(now);
            }
            existing.count++;
            return existing;
        });
        return window.count <= capacity;
    }

    private static final class Window {
        final long start;
        int count;

        Window(long start) {
            this.start = start;
            this.count = 1;
        }
    }
}
