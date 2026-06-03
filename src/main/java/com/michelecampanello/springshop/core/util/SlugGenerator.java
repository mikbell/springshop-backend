package com.michelecampanello.springshop.core.util;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Genera slug URL-friendly a partire da testo libero.
 */
public final class SlugGenerator {

    private SlugGenerator() {
    }

    public static String toSlug(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
    }
}
