package com.jeimandei.imanuelbytes.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utility class providing date/time formatting and comparison helpers shared
 * across all church platform microservices.
 *
 * <p>All methods are static; this class is not meant to be instantiated.</p>
 */
public final class DateUtils {

    /** Pattern used by {@link #formatDate(LocalDateTime)}: e.g. "07 June 2026". */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

    /** Pattern used by {@link #formatDateTime(LocalDateTime)}: e.g. "07 June 2026 14:30". */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", Locale.ENGLISH);

    private DateUtils() {
        throw new UnsupportedOperationException("DateUtils is a utility class");
    }

    /**
     * Formats a {@link LocalDateTime} as a human-readable date string.
     *
     * <p>Output format: {@code dd MMMM yyyy} — e.g. {@code "07 June 2026"}.</p>
     *
     * @param dateTime the date-time value to format; must not be {@code null}
     * @return the formatted date string
     * @throws NullPointerException if {@code dateTime} is {@code null}
     */
    public static String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DATE_FORMATTER);
    }

    /**
     * Formats a {@link LocalDateTime} as a human-readable date-and-time string.
     *
     * <p>Output format: {@code dd MMMM yyyy HH:mm} — e.g. {@code "07 June 2026 14:30"}.</p>
     *
     * @param dateTime the date-time value to format; must not be {@code null}
     * @return the formatted date-time string
     * @throws NullPointerException if {@code dateTime} is {@code null}
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Returns {@code true} when the given {@link LocalDateTime} is strictly after
     * the current system time, i.e. the event or deadline has not yet occurred.
     *
     * @param dateTime the date-time to test; must not be {@code null}
     * @return {@code true} if {@code dateTime} is in the future
     * @throws NullPointerException if {@code dateTime} is {@code null}
     */
    public static boolean isUpcoming(LocalDateTime dateTime) {
        return dateTime.isAfter(LocalDateTime.now());
    }
}
