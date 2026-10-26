package com.companyz.ems.model;

import java.time.LocalDate;

/**
 * Search fields from features 2 and 3: name, DOB, SSN, empID.
 * Any field left null or blank is ignored; the fields that are set are combined with AND.
 * {@code name} matches first name, last name, or "first last", case-insensitive.
 */
public record SearchCriteria(Integer empId, String name, LocalDate dateOfBirth, String ssn) {

    public static SearchCriteria byEmpId(int empId) {
        return new SearchCriteria(empId, null, null, null);
    }

    public static SearchCriteria byName(String name) {
        return new SearchCriteria(null, name, null, null);
    }

    public static SearchCriteria byDateOfBirth(LocalDate dateOfBirth) {
        return new SearchCriteria(null, null, dateOfBirth, null);
    }

    public static SearchCriteria bySsn(String ssn) {
        return new SearchCriteria(null, null, null, ssn);
    }

    public boolean isEmpty() {
        return empId == null && (name == null || name.isBlank()) && dateOfBirth == null
                && (ssn == null || ssn.isBlank());
    }

    /** Never prints the SSN. */
    @Override
    public String toString() {
        return "SearchCriteria{empId=" + empId + ", name=" + name + ", dateOfBirth=" + dateOfBirth
                + ", ssn=" + (ssn == null || ssn.isBlank() ? "none" : "(given)") + "}";
    }
}
