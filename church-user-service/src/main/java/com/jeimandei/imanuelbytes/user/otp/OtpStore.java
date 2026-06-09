package com.jeimandei.imanuelbytes.user.otp;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpStore {

    private final ConcurrentHashMap<Long, OtpEntry> store = new ConcurrentHashMap<>();

    public void put(Long userId, OtpEntry entry) {
        store.put(userId, entry);
    }

    public OtpEntry get(Long userId) {
        return store.get(userId);
    }

    public void remove(Long userId) {
        store.remove(userId);
    }
}
