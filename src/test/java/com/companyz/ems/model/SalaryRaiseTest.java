package com.companyz.ems.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Boundary cases use the shared seed salaries from sql/05_seed_data.sql. */
class SalaryRaiseTest {

    private static BigDecimal d(String s) {
        return new BigDecimal(s);
    }

    // Feature 5: range is inclusive at both ends ($50,000 to $80,000)
    @Test
    void rangeIncludesBothEndpoints() {
        SalaryRaise raise = SalaryRaise.inRange(d("3"), d("50000"), d("80000"));
        assertTrue(raise.appliesTo(d("50000.00")));
        assertTrue(raise.appliesTo(d("59999.99")));
        assertTrue(raise.appliesTo(d("80000.00")));
        assertFalse(raise.appliesTo(d("49999.99")));
        assertFalse(raise.appliesTo(d("80000.01")));
    }

    // Task (c): strictly less than the threshold ($60,000)
    @Test
    void belowThresholdExcludesTheThresholdItself() {
        SalaryRaise raise = SalaryRaise.below(d("3"), d("60000"));
        assertTrue(raise.appliesTo(d("45000.00")));
        assertTrue(raise.appliesTo(d("59999.99")));
        assertFalse(raise.appliesTo(d("60000.00")));
        assertFalse(raise.appliesTo(d("60000.01")));
    }

    @Test
    void newSalaryRoundsHalfUpToTheCent() {
        SalaryRaise raise = SalaryRaise.below(d("3"), d("60000"));
        assertEquals(d("61799.99"), raise.newSalary(d("59999.99"))); // 61,799.9897
        assertEquals(d("46350.00"), raise.newSalary(d("45000.00")));
        assertEquals(d("100.01"), SalaryRaise.below(d("0.005"), d("1000")).newSalary(d("100.00"))); // 100.005
    }

    @Test
    void sqlOperatorMatchesTheBoundType() {
        assertEquals("<=", SalaryRaise.inRange(d("1"), d("0"), d("10")).upperBoundOperator());
        assertEquals("<", SalaryRaise.below(d("1"), d("10")).upperBoundOperator());
    }

    @Test
    void invalidInputsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> SalaryRaise.inRange(d("0"), d("1"), d("2")));
        assertThrows(IllegalArgumentException.class, () -> SalaryRaise.inRange(d("-3"), d("1"), d("2")));
        assertThrows(IllegalArgumentException.class, () -> SalaryRaise.inRange(d("300"), d("1"), d("2")));
        assertThrows(IllegalArgumentException.class, () -> SalaryRaise.inRange(d("3"), d("80000"), d("50000")));
        assertThrows(IllegalArgumentException.class, () -> SalaryRaise.inRange(d("3"), d("-1"), d("50000")));
    }
}
