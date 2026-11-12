package com.yerandis.sge.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int    MAX_ATTEMPTS            = 5;
    private static final long   LOCK_DURATION_SECONDS   = 300; // 5 minutos

    private record Attempt(int count, Instant lockedUntil) {}

    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Attempt a = attempts.get(key);
        if (a == null) return false;
        if (a.lockedUntil() != null && Instant.now().isBefore(a.lockedUntil())) return true;
        if (a.lockedUntil() != null) attempts.remove(key); // el bloqueo ya expiró
        return false;
    }

    public void recordFailure(String key) {
        attempts.compute(key, (k, current) -> {
            int count = (current == null ? 0 : current.count()) + 1;
            Instant lockedUntil = count >= MAX_ATTEMPTS
                    ? Instant.now().plusSeconds(LOCK_DURATION_SECONDS)
                    : null;
            return new Attempt(count, lockedUntil);
        });
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }
}
