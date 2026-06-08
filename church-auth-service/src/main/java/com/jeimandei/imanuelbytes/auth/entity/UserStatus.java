package com.jeimandei.imanuelbytes.auth.entity;

/**
 * Lifecycle status of a {@link User} account.
 *
 * <ul>
 *   <li>{@code ACTIVE}   – account can authenticate normally.</li>
 *   <li>{@code INACTIVE} – account has been deactivated (e.g. self-requested).</li>
 *   <li>{@code LOCKED}   – account has been locked by an administrator (e.g.
 *       after too many failed login attempts or policy violation).</li>
 * </ul>
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    LOCKED
}
