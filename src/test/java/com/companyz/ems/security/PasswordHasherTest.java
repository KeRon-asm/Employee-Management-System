package com.companyz.ems.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    // Low iteration count keeps the tests fast; production uses the default.
    private final PasswordHasher hasher = new PasswordHasher(1_000);

    @Test
    void correctPasswordVerifies() {
        String stored = hasher.hash("Secret#123".toCharArray());
        assertTrue(hasher.verify("Secret#123".toCharArray(), stored));
    }

    @Test
    void wrongPasswordIsRejected() {
        String stored = hasher.hash("Secret#123".toCharArray());
        assertFalse(hasher.verify("secret#123".toCharArray(), stored));
    }

    @Test
    void samePasswordGetsDifferentSalts() {
        String a = hasher.hash("Secret#123".toCharArray());
        String b = hasher.hash("Secret#123".toCharArray());
        assertNotEquals(a, b);
    }

    @Test
    void storedValueNeverContainsThePassword() {
        String stored = hasher.hash("Secret#123".toCharArray());
        assertFalse(stored.contains("Secret#123"));
    }

    @Test
    void malformedStoredHashIsRejectedNotThrown() {
        assertFalse(hasher.verify("x".toCharArray(), "not-a-hash"));
        assertFalse(hasher.verify("x".toCharArray(), "pbkdf2$abc$$"));
        assertFalse(hasher.verify("x".toCharArray(), null));
    }

    @Test
    void emptyPasswordCannotBeHashed() {
        assertThrows(IllegalArgumentException.class, () -> hasher.hash(new char[0]));
    }

    @Test
    void hashesFromDifferentIterationCountsStillVerify() {
        String stored = new PasswordHasher(2_000).hash("Secret#123".toCharArray());
        assertTrue(hasher.verify("Secret#123".toCharArray(), stored));
    }
}
