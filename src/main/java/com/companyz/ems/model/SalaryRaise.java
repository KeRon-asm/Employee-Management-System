package com.companyz.ems.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * A percentage raise for every employee whose salary falls in a range.
 *
 * <ul>
 *   <li>Feature 5 / deliverable 3a: {@link #inRange} -- min and max are both <b>inclusive</b>,
 *       so "$50,000 to $80,000" includes someone earning exactly $80,000.</li>
 *   <li>Deliverable 2 task (c): {@link #below} -- salary <b>strictly less than</b> the threshold,
 *       so someone earning exactly the threshold is not raised.</li>
 * </ul>
 *
 * New salaries are rounded to the cent, half-up (MySQL's ROUND does the same).
 */
public record SalaryRaise(BigDecimal percent, BigDecimal minSalary, BigDecimal maxSalary, UpperBound upperBound) {

    public enum UpperBound { INCLUSIVE, EXCLUSIVE }

    /** Sanity cap so a typo like 300 instead of 3.00 is rejected. */
    public static final BigDecimal MAX_PERCENT = new BigDecimal("100");

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public SalaryRaise {
        Objects.requireNonNull(percent, "percent");
        Objects.requireNonNull(minSalary, "minSalary");
        Objects.requireNonNull(maxSalary, "maxSalary");
        Objects.requireNonNull(upperBound, "upperBound");
        if (percent.signum() <= 0) {
            throw new IllegalArgumentException("Raise percentage must be greater than 0");
        }
        if (percent.compareTo(MAX_PERCENT) > 0) {
            throw new IllegalArgumentException("Raise percentage cannot exceed " + MAX_PERCENT + "%");
        }
        if (minSalary.signum() < 0) {
            throw new IllegalArgumentException("Minimum salary cannot be negative");
        }
        if (maxSalary.compareTo(minSalary) < 0) {
            throw new IllegalArgumentException("Maximum salary must be at least the minimum salary");
        }
    }

    /** Feature 5: raise everyone with {@code minSalary <= salary <= maxSalary}. */
    public static SalaryRaise inRange(BigDecimal percent, BigDecimal minSalary, BigDecimal maxSalary) {
        return new SalaryRaise(percent, minSalary, maxSalary, UpperBound.INCLUSIVE);
    }

    /** Task (c): raise everyone with {@code salary < threshold}. */
    public static SalaryRaise below(BigDecimal percent, BigDecimal threshold) {
        return new SalaryRaise(percent, BigDecimal.ZERO, threshold, UpperBound.EXCLUSIVE);
    }

    public boolean appliesTo(BigDecimal salary) {
        if (salary.compareTo(minSalary) < 0) {
            return false;
        }
        int vsMax = salary.compareTo(maxSalary);
        return upperBound == UpperBound.INCLUSIVE ? vsMax <= 0 : vsMax < 0;
    }

    public BigDecimal newSalary(BigDecimal currentSalary) {
        BigDecimal multiplier = BigDecimal.ONE.add(percent.divide(HUNDRED));
        return currentSalary.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    /** SQL comparison for the upper bound. A fixed constant, never user input. */
    public String upperBoundOperator() {
        return upperBound == UpperBound.INCLUSIVE ? "<=" : "<";
    }
}
