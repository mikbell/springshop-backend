package com.michelecampanello.springshop.core.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlugGeneratorTest {

    @Test
    void convertsToLowercaseHyphenated() {
        assertThat(SlugGenerator.toSlug("Maglietta Cotone")).isEqualTo("maglietta-cotone");
    }

    @Test
    void removesAccents() {
        assertThat(SlugGenerator.toSlug("Caffè Forte")).isEqualTo("caffe-forte");
    }

    @Test
    void collapsesAndTrimsSeparators() {
        assertThat(SlugGenerator.toSlug("  Hello---World!!  ")).isEqualTo("hello-world");
    }

    @Test
    void returnsEmptyForNull() {
        assertThat(SlugGenerator.toSlug(null)).isEmpty();
    }
}
