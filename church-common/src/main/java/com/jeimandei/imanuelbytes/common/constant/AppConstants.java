package com.jeimandei.imanuelbytes.common.constant;

/**
 * Application-wide constants shared across all church platform microservices.
 *
 * <p>This class is not meant to be instantiated; all members are public static
 * constants.</p>
 */
public final class AppConstants {

    private AppConstants() {
        throw new UnsupportedOperationException("AppConstants is a utility class");
    }

    // -------------------------------------------------------------------------
    // Pagination defaults
    // -------------------------------------------------------------------------

    /** Default number of items returned per page. */
    public static final int PAGE_SIZE = 10;

    /** Hard upper limit on page size accepted from clients. */
    public static final int MAX_PAGE_SIZE = 100;

    /** Default first page index (0-based, matching Spring Data conventions). */
    public static final int DEFAULT_PAGE_NUMBER = 0;

    // -------------------------------------------------------------------------
    // HTTP / JWT
    // -------------------------------------------------------------------------

    /** Name of the HTTP header that carries the JWT bearer token. */
    public static final String JWT_HEADER = "Authorization";

    /** Prefix expected before the JWT value in the Authorization header. */
    public static final String JWT_PREFIX = "Bearer ";

    // -------------------------------------------------------------------------
    // Role constants
    // -------------------------------------------------------------------------

    /** Full system administrator — unrestricted access to all services. */
    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    /** Church administrator — manages most content and users. */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /** Content editor — can create and edit content but not manage users. */
    public static final String ROLE_EDITOR = "ROLE_EDITOR";

    /** Registered congregation member — access to member-only features. */
    public static final String ROLE_MEMBER = "ROLE_MEMBER";

    /** Unauthenticated / public visitor — access to public content only. */
    public static final String ROLE_GUEST = "ROLE_GUEST";
}
