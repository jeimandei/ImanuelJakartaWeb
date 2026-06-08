package com.jeimandei.imanuelbytes.user.entity;

/**
 * Lifecycle status of a user account.
 *
 * <ul>
 *   <li>{@link #ACTIVE}   – account is fully operational.</li>
 *   <li>{@link #INACTIVE} – soft-deleted or deactivated by an administrator.</li>
 *   <li>{@link #LOCKED}   – temporarily locked due to suspicious activity or policy.</li>
 * </ul>
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    LOCKED
}
