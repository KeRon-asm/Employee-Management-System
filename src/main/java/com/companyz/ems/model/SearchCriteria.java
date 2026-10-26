package com.companyz.ems.model;

import java.time.LocalDate;

/**
 * Search fields from features 2 and 3. Any field left null is ignored;
 * the fields that are set are combined with AND.
 */
public record SearchCriteria(Integer empId, String name, LocalDate dateOfBirth, String ssn) {

    public static SearchCriteria byEmpId(int empId) {
        return new SearchCriteria(empId, null, null, null);
    }

    public static SearchCriteria byName(String name) {
        return new SearchCriteria(null, name, null, null);
    }

    public boolean isEmpty() {
        return empId == null && (name == null || name.isBlank()) && dateOfBirth == null
                && (ssn == null || ssn.isBlank());
    }
}
