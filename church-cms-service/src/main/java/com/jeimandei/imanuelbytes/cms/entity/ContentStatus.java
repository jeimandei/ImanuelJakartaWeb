package com.jeimandei.imanuelbytes.cms.entity;

/**
 * Lifecycle status for CMS content (pages, news articles).
 */
public enum ContentStatus {

    /** Content is being authored and is not yet visible to the public. */
    DRAFT,

    /** Content is live and visible to authorised audiences. */
    PUBLISHED,

    /** Content has been retired and is no longer displayed. */
    ARCHIVED
}
