package com.jeimandei.imanuelbytes.interaction.token;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UnsubscribeTokenStore {

    private static final long EXPIRY_SECONDS = 86400; // 24 hours

    private final Map<String, TokenEntry> store = new ConcurrentHashMap<>();

    public void put(String token, String email) {
        store.put(token, new TokenEntry(email, Instant.now().plusSeconds(EXPIRY_SECONDS)));
    }

    public String getEmail(String token) {
        TokenEntry entry = store.get(token);
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            store.remove(token);
            return null;
        }
        return entry.email();
    }

    public void remove(String token) {
        store.remove(token);
    }

    public record TokenEntry(String email, Instant expiresAt) {}
}
