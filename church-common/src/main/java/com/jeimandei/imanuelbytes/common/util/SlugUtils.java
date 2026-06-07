package com.jeimandei.imanuelbytes.common.util;

/**
 * Utility class for generating URL-friendly slugs from arbitrary text.
 *
 * <p>All methods are static; this class is not meant to be instantiated.</p>
 */
public final class SlugUtils {

    private SlugUtils() {
        throw new UnsupportedOperationException("SlugUtils is a utility class");
    }

    /**
     * Converts an arbitrary input string into a URL-friendly slug.
     *
     * <p>The transformation steps are:</p>
     * <ol>
     *   <li>Trim leading/trailing whitespace.</li>
     *   <li>Convert to lower case (using the root locale to avoid locale-specific
     *       surprises with characters such as the Turkish dotless-i).</li>
     *   <li>Replace one or more whitespace characters with a single hyphen.</li>
     *   <li>Remove any character that is not a lowercase ASCII letter, digit,
     *       or hyphen.</li>
     *   <li>Collapse consecutive hyphens into a single hyphen.</li>
     *   <li>Strip leading and trailing hyphens that may remain after the above
     *       steps.</li>
     * </ol>
     *
     * <p>Examples:</p>
     * <pre>
     * toSlug("Hello World")          // "hello-world"
     * toSlug("  Kebaktian Minggu! ") // "kebaktian-minggu"
     * toSlug("A &amp; B / C")            // "a-b-c"
     * toSlug("already-a-slug")       // "already-a-slug"
     * </pre>
     *
     * @param input the raw text to slugify; {@code null} returns an empty string
     * @return a non-null, lowercase, hyphen-separated slug
     */
    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        return input
                .trim()
                .toLowerCase(java.util.Locale.ROOT)
                // Replace runs of whitespace with a single hyphen
                .replaceAll("\\s+", "-")
                // Remove characters that are not lowercase letters, digits, or hyphens
                .replaceAll("[^a-z0-9-]", "")
                // Collapse consecutive hyphens
                .replaceAll("-{2,}", "-")
                // Strip leading/trailing hyphens
                .replaceAll("^-+|-+$", "");
    }
}
