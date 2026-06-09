package com.jeimandei.imanuelbytes.user.otp;

import java.time.Instant;

public record OtpEntry(String code, Instant expiresAt) {
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
