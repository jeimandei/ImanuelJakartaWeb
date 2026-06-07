package com.jeimandei.imanuelbytes.interaction.entity;

/**
 * Lifecycle status shared by all interaction-related submissions
 * (prayer requests, contact messages, testimonies, volunteer applications).
 */
public enum RequestStatus {

    /** Newly submitted; not yet reviewed by any staff member. */
    NEW,

    /** A staff member has reviewed the submission. */
    REVIEWED,

    /** The prayer request has been prayed over (prayer requests only). */
    PRAYED,

    /** The submission has been archived and is no longer active. */
    ARCHIVED
}
