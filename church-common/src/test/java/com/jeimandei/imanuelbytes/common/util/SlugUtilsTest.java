package com.jeimandei.imanuelbytes.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("SlugUtils.toSlug()")
class SlugUtilsTest {

    // -------------------------------------------------------------------------
    // Normal / happy-path cases
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("converts simple two-word title to hyphenated lowercase slug")
    void toSlug_normalText_returnsHyphenatedLowercase() {
        assertEquals("hello-world", SlugUtils.toSlug("Hello World"));
    }

    @Test
    @DisplayName("already lowercase slug is returned unchanged")
    void toSlug_alreadySlug_returnsSame() {
        assertEquals("already-a-slug", SlugUtils.toSlug("already-a-slug"));
    }

    @Test
    @DisplayName("collapses multiple consecutive whitespace characters into one hyphen")
    void toSlug_multipleSpaces_collapsedToSingleHyphen() {
        assertEquals("hello-world", SlugUtils.toSlug("Hello   World"));
    }

    // -------------------------------------------------------------------------
    // Uppercase input
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("converts fully uppercase input to lowercase slug")
    void toSlug_allUppercase_returnsLowercase() {
        assertEquals("church-service", SlugUtils.toSlug("CHURCH SERVICE"));
    }

    @Test
    @DisplayName("converts mixed-case sentence to lowercase slug")
    void toSlug_mixedCase_returnsLowercase() {
        assertEquals("sunday-worship", SlugUtils.toSlug("Sunday Worship"));
    }

    // -------------------------------------------------------------------------
    // Special characters
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("removes ampersand and extra hyphens that result from it")
    void toSlug_ampersandBetweenWords_removedCleanly() {
        // "A & B / C" -> "a-b-c" (& and / are stripped, spaces become hyphens,
        //  consecutive hyphens are collapsed)
        assertEquals("a-b-c", SlugUtils.toSlug("A & B / C"));
    }

    @Test
    @DisplayName("strips punctuation marks from the slug")
    void toSlug_punctuation_stripped() {
        assertEquals("kebaktian-minggu", SlugUtils.toSlug("Kebaktian Minggu!"));
    }

    @Test
    @DisplayName("preserves hyphens that are already in the input")
    void toSlug_hyphenInInput_preserved() {
        assertEquals("praise-and-worship", SlugUtils.toSlug("praise-and-worship"));
    }

    @Test
    @DisplayName("strips leading and trailing hyphens that may arise from special chars")
    void toSlug_leadingTrailingSpecialChars_hyphensStripped() {
        // e.g. "!hello!" -> "" -> after lowercasing/removing non-slug chars -> "hello"
        assertEquals("hello", SlugUtils.toSlug("!hello!"));
    }

    // -------------------------------------------------------------------------
    // Whitespace / empty / null
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("returns empty string for null input")
    void toSlug_null_returnsEmptyString() {
        assertEquals("", SlugUtils.toSlug(null));
    }

    @Test
    @DisplayName("returns empty string for empty string input")
    void toSlug_emptyString_returnsEmptyString() {
        assertEquals("", SlugUtils.toSlug(""));
    }

    @Test
    @DisplayName("returns empty string for blank (whitespace-only) input")
    void toSlug_blankString_returnsEmptyString() {
        assertEquals("", SlugUtils.toSlug("   "));
    }

    @Test
    @DisplayName("trims leading and trailing whitespace before processing")
    void toSlug_leadingTrailingWhitespace_trimmed() {
        assertEquals("kebaktian-minggu", SlugUtils.toSlug("  Kebaktian Minggu! "));
    }

    // -------------------------------------------------------------------------
    // Indonesian / accented characters
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("removes accented Indonesian characters that are not ASCII letters")
    void toSlug_indonesianAccentedChars_removed() {
        // 'é' is not in [a-z0-9-], so it gets stripped.
        // "Générale" -> "gnrale" is an example; for Indonesian the pattern is similar.
        // "kebaktian pujian" is plain ASCII so this test uses a word with an accent.
        // Input: "Pujián" -> lowercase "pujián" -> strip 'á' -> "pujin" -- not ideal but
        // the method does NOT transliterate; it simply strips non-ASCII chars.
        assertEquals("pujin", SlugUtils.toSlug("Pujián"));
    }

    @Test
    @DisplayName("plain Indonesian ASCII words are slugified normally")
    void toSlug_indonesianAsciiWords_slugifiedNormally() {
        assertEquals("kebaktian-minggu", SlugUtils.toSlug("Kebaktian Minggu"));
    }

    // -------------------------------------------------------------------------
    // Numbers
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("preserves digits in the slug")
    void toSlug_withDigits_digitsPreserved() {
        assertEquals("sermon-2026", SlugUtils.toSlug("Sermon 2026"));
    }
}
