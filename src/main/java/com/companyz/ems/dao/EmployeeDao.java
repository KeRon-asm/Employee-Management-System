package com.companyz.ems.dao;

import com.companyz.ems.model.Employee;
import com.companyz.ems.model.SearchCriteria;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Data access for employees. The UI and services depend on this interface,
 * never on JDBC directly, so the database code can change or be faked in tests.
 *
 * <p>Implementations live in {@code com.companyz.ems.dao.jdbc} and must use
 * PreparedStatement for every query (no string-built SQL).
 */
public interface EmployeeDao {

    Optional<Employee> findById(int empId);

    /** Features 2 and 3: search by any combination of empID, name, DOB, SSN. */
    List<Employee> search(SearchCriteria criteria);

    /** Inserts the employee and its address/demographics rows; returns the generated empID. */
    int insert(Employee employee);

    /** Feature 4 / test case (a): updates every field of an existing employee. */
    void update(Employee employee);

    /** Test case (b): returns true if a row was deleted, false if no match. */
    boolean delete(int empId);

    /**
     * Feature 5: raises salary by {@code percent} for everyone whose salary is
     * {@code >= minSalary} and {@code < maxSalary}. Runs in one transaction.
     *
     * @return number of employees updated (0 is valid -- nobody was in range)
     */
    int applyRaiseInRange(BigDecimal percent, BigDecimal minSalary, BigDecimal maxSalary);

    /** Test case (c): raise for everyone whose salary is strictly less than {@code threshold}. */
    default int applyRaiseBelow(BigDecimal percent, BigDecimal threshold) {
        return applyRaiseInRange(percent, BigDecimal.ZERO, threshold);
    }
}
