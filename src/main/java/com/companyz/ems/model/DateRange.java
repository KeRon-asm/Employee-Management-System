package com.companyz.ems.model;

import java.time.LocalDate;
import java.util.Objects;

/** An inclusive date range, e.g. for report 6d (new hires). */
public record DateRange(LocalDate start, LocalDate end) {

    public DateRange {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date " + end + " is before start date " + start);
        }
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(start) && !date.isAfter(end);
    }
}
