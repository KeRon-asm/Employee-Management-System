package com.companyz.ems.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SsnMaskerTest {

    @Test
    void showsOnlyLastFourDigits() {
        assertEquals("***-**-6789", SsnMasker.mask("123-45-6789"));
        assertEquals("***-**-6789", SsnMasker.mask("123456789"));
    }

    @Test
    void handlesMissingOrShortValues() {
        assertEquals("", SsnMasker.mask(null));
        assertEquals("***-**-****", SsnMasker.mask("12"));
    }
}
