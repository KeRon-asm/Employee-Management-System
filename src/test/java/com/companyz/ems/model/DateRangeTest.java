package com.companyz.ems.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DateRangeTest {

    @Test
    void includesBothEndDates() {
        DateRange q3 = new DateRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 30));
        assertTrue(q3.contains(LocalDate.of(2026, 7, 1)));
        assertTrue(q3.contains(LocalDate.of(2026, 9, 30)));
        assertFalse(q3.contains(LocalDate.of(2026, 6, 30)));
        assertFalse(q3.contains(LocalDate.of(2026, 10, 1)));
    }

    @Test
    void endBeforeStartIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new DateRange(LocalDate.of(2026, 9, 30), LocalDate.of(2026, 7, 1)));
    }
}
