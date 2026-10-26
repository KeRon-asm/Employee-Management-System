package com.companyz.ems.model;

import java.time.LocalDate;

/** Row of the {@code demographics} table. */
public record Demographics(
        LocalDate dateOfBirth,
        String mobilePhone,
        String emergencyContactName,
        String emergencyContactPhone) {
}
