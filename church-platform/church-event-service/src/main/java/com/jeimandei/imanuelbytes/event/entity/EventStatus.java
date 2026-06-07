package com.jeimandei.imanuelbytes.event.entity;

/**
 * Lifecycle states for a church {@link Event}.
 */
public enum EventStatus {

    /** Event is being prepared and is not visible to the public. */
    DRAFT,

    /** Event is live and visible to the public. */
    PUBLISHED,

    /** Event has been cancelled and is no longer taking place. */
    CANCELLED,

    /** Event has concluded successfully. */
    COMPLETED
}
