package com.companyz.ems.model;

import java.time.LocalDateTime;

/** Row of the {@code app_users} table. {@code empId} is null for HR accounts with no employee record. */
public record UserAccount(
        int userId,
        String username,
        String passwordHash,
        Role role,
        Integer empId,
        int failedAttempts,
        LocalDateTime lockedUntil) {

    public boolean isLocked(LocalDateTime now) {
        return lockedUntil != null && now.isBefore(lockedUntil);
    }
}
